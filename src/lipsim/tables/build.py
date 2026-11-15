"""Build property tables for the PLUTO user modules.

    python -m lipsim.tables.build --config configs/tables/v0.yaml --out <dir> --workers N [--only eos]

Outputs (little-endian, documented in `write_bin`):
  eos.bin        e(ρ,T,Y) [J/kg], p [Pa], n_e [m^-3], dchi_eV (IP lowering; validity flag) on (log10 ρ, log10 T, Y)
  manifest.json  config, registry hash, NIST manifest hash, code commit, per-file SHA256
Further tables (alpha1064.bin, nec_*.bin, inband.npz) are added by the optics module (W2).
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

MAGIC = b"LIPTAB01"


def grid(cfg) -> tuple[np.ndarray, np.ndarray, np.ndarray]:
    n = cfg["numerics"]
    lr = np.linspace(n["log10_rho"]["min"], n["log10_rho"]["max"], n["log10_rho"]["n"])
    lt = np.linspace(n["log10_T"]["min"], n["log10_T"]["max"], n["log10_T"]["n"])
    Y = np.asarray(n["Y_cu"], float)
    return lr, lt, Y


def _eos_row(args):
    """All T for one (log10 rho, Y) pair; continuation in T keeps the solver cheap."""
    from lipsim.eos import mixture as mx
    lrho, Y, lts = args
    rho = 10.0 ** lrho
    out = np.empty((len(lts), 4))
    for j, lt in enumerate(lts):
        s = mx.equilibrium(rho, 10.0 ** lt, float(Y))
        out[j] = (s.e, s.p, s.ne, s.dchi_eV)
    return out


def build_eos(cfg, workers: int) -> dict:
    lr, lt, Y = grid(cfg)
    jobs = [(a, y, lt) for y in Y for a in lr]
    t0 = time.time()
    if workers > 1:
        with ProcessPoolExecutor(max_workers=workers) as ex:
            rows = list(ex.map(_eos_row, jobs, chunksize=max(1, len(jobs) // (workers * 8))))
    else:
        rows = [_eos_row(j) for j in jobs]
    arr = np.array(rows).reshape(len(Y), len(lr), len(lt), 4)
    arr = np.transpose(arr, (1, 2, 0, 3))  # -> [rho, T, Y, field]
    return {"axes": {"log10_rho": lr, "log10_T": lt, "Y_cu": Y},
            "fields": {"e": arr[..., 0], "p": arr[..., 1], "ne": arr[..., 2], "dchi_eV": arr[..., 3]},
            "seconds": time.time() - t0}


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
    ap.add_argument("--only", nargs="*", default=None)
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
    todo = a.only or ["eos"]
    if "eos" in todo:
        r = build_eos(cfg, a.workers)
        write_bin(a.out / "eos.bin", r["axes"], r["fields"])
        man["files"]["eos.bin"] = {"sha256": _sha(a.out / "eos.bin"), "seconds": round(r["seconds"], 1),
                                   "shape": [len(v) for v in r["axes"].values()]}
        print(f"eos.bin: {man['files']['eos.bin']['shape']} in {r['seconds']:.1f}s")
    # W2 hooks: alpha1064 / nec / inband are added by lipsim.optics when available
    try:
        from lipsim.optics import tables as optab  # noqa: F401
        man["files"].update(optab.build_all(cfg, a.out, a.workers, todo))
    except ImportError:
        pass
    (a.out / "manifest.json").write_text(json.dumps(man, indent=1, default=str))


if __name__ == "__main__":
    main()
