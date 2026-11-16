"""Build property tables for the PLUTO user modules.

    python -m lipsim.tables.build --config configs/tables/v0.yaml --out <dir> --workers N [--only eos]

Outputs (little-endian LIPTAB01, documented in `write_bin`), all on (log10 ρ, log10 T, Y):
  eos.bin   e [J/kg], p [Pa], ne [m^-3], dchi_eV (IP lowering; validity flag)
  opt.bin   alpha1064 [1/m], nec_thin, nec_trap [W/m^3/sr], kappa [W/m/K] (v1: electron + heavy, no reactive)
  pop.bin   nQ_<El><z> = n_z/Q_z, n_<El><z>, nion_z<k> [m^-3] for in-band levels and continuum (imaging)
  manifest.json  config, registry hash, NIST manifest hash, code commit, per-file SHA256
"""
from __future__ import annotations

import argparse
import hashlib
import json
import struct
import subprocess
import time
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
import yaml

from lipsim import params

P_get = params.get

MAGIC = b"LIPTAB01"


def grid(cfg) -> tuple[np.ndarray, np.ndarray, np.ndarray]:
    n = cfg["numerics"]
    lr = np.linspace(n["log10_rho"]["min"], n["log10_rho"]["max"], n["log10_rho"]["n"])
    lt = np.linspace(n["log10_T"]["min"], n["log10_T"]["max"], n["log10_T"]["n"])
    Y = np.asarray(n["Y_cu"], float)
    return lr, lt, Y


EOS_FIELDS = ["e", "p", "ne", "dchi_eV"]
OPT_FIELDS = ["alpha1064", "nec_thin", "nec_trap", "kappa"]


def _row(args):
    """All T for one (log10 rho, Y) pair: EOS + optics + level-population fields."""
    from lipsim.eos import mixture as mx
    from lipsim.optics import props as op
    from lipsim.transport.kappa import kappa
    lrho, Y, lts, wl = args
    rho = 10.0 ** lrho
    rows = []
    for lt in lts:
        s = mx.equilibrium(rho, 10.0 ** lt, float(Y))
        d = {"e": s.e, "p": s.p, "ne": s.ne, "dchi_eV": s.dchi_eV,
             "alpha1064": op.alpha_at(s, wl)["total"], "nec_thin": op.nec(s, "thin"), "nec_trap": op.nec(s, "trap"), "kappa": kappa(s)["total"]}
        d.update(op.pop_fields(s))
        rows.append(d)
    return rows


def build_eos(cfg, workers: int) -> dict:
    lr, lt, Y = grid(cfg)
    wl = P_get("wavelength_laser")
    jobs = [(a, y, lt, wl) for y in Y for a in lr]
    t0 = time.time()
    if workers > 1:
        with ProcessPoolExecutor(max_workers=workers) as ex:
            rows = list(ex.map(_row, jobs, chunksize=max(1, len(jobs) // (workers * 8))))
    else:
        rows = [_row(j) for j in jobs]
    names = list(rows[0][0].keys())
    arr = np.array([[[d[k] for k in names] for d in r] for r in rows])  # [Y*rho, T, field]
    arr = arr.reshape(len(Y), len(lr), len(lt), len(names)).transpose(1, 2, 0, 3)  # -> [rho, T, Y, field]
    fields = {k: arr[..., i] for i, k in enumerate(names)}
    return {"axes": {"log10_rho": lr, "log10_T": lt, "Y_cu": Y}, "fields": fields, "seconds": time.time() - t0}


def write_bin(path: Path, axes: dict, fields: dict):
    """Format: MAGIC(8) | int32 n_axes | per axis: int32 len, char[16] name, float64[len] |
    int32 n_fields | per field: char[16] name, float64[prod(len)] in C order of the axes."""
    with open(path, "wb") as f:
        f.write(MAGIC)
        f.write(struct.pack("<i", len(axes)))
        for name, v in axes.items():
            v = np.ascontiguousarray(v, "<f8")
            f.write(struct.pack("<i", len(v)))
            f.write(name.encode()[:16].ljust(16, b"\0"))
            f.write(v.tobytes())
        f.write(struct.pack("<i", len(fields)))
        for name, v in fields.items():
            f.write(name.encode()[:16].ljust(16, b"\0"))
            f.write(np.ascontiguousarray(v, "<f8").tobytes())


def read_bin(path: Path) -> tuple[dict, dict]:
    b = Path(path).read_bytes()
    assert b[:8] == MAGIC, "not a LIPTAB01 file"
    o = 8
    (na,) = struct.unpack_from("<i", b, o); o += 4
    axes = {}
    for _ in range(na):
        (n,) = struct.unpack_from("<i", b, o); o += 4
        name = b[o:o + 16].rstrip(b"\0").decode(); o += 16
        axes[name] = np.frombuffer(b, "<f8", n, o).copy(); o += 8 * n
    shape = tuple(len(v) for v in axes.values())
    (nf,) = struct.unpack_from("<i", b, o); o += 4
    fields = {}
    size = int(np.prod(shape))
    for _ in range(nf):
        name = b[o:o + 16].rstrip(b"\0").decode(); o += 16
        fields[name] = np.frombuffer(b, "<f8", size, o).reshape(shape).copy(); o += 8 * size
    return axes, fields


def _sha(p: Path) -> str:
    return hashlib.sha256(Path(p).read_bytes()).hexdigest()


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("--config", type=Path, required=True)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--workers", type=int, default=1)
    a = ap.parse_args(argv)
    cfg = yaml.safe_load(a.config.read_text(encoding="utf-8"))
    a.out.mkdir(parents=True, exist_ok=True)
    repo = Path(__file__).resolve().parents[3]
    try:
        commit = subprocess.run(["git", "-C", str(repo), "rev-parse", "HEAD"], capture_output=True, text=True).stdout.strip()
    except Exception:  # noqa: BLE001
        commit = "unknown"
    man = {"config": cfg, "config_file": str(a.config), "registry_sha256": params.registry_hash(),
           "nist_manifest_sha256": _sha(repo / "data/atomic/nist/manifest.json"), "code_commit": commit, "files": {}}
    r = build_eos(cfg, a.workers)
    f = r["fields"]
    groups = {"eos.bin": EOS_FIELDS, "opt.bin": OPT_FIELDS,
              "pop.bin": [k for k in f if k.startswith(("nQ_", "n_", "nion_"))]}
    for fn, keys in groups.items():
        write_bin(a.out / fn, r["axes"], {k: f[k] for k in keys})
        man["files"][fn] = {"sha256": _sha(a.out / fn), "fields": keys,
                            "shape": [len(v) for v in r["axes"].values()]}
    man["seconds"] = round(r["seconds"], 1)
    print(f"tables {man['files']['eos.bin']['shape']} built in {r['seconds']:.1f}s -> {list(groups)}")
    (a.out / "manifest.json").write_text(json.dumps(man, indent=1, default=str))


if __name__ == "__main__":
    main()
