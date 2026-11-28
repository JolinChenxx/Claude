"""Download NIST ASD tables needed by M1 (EOS partition functions, full-spectrum NEC, in-band emission).

Run on the workstation (the cloud sandbox cannot reach physics.nist.gov):
    python scripts/fetch_nist.py --out data/atomic/nist
Respects HTTPS_PROXY/HTTP_PROXY. Writes one file per query plus manifest.json (url, UTC time, bytes, sha256,
data rows). Query forms follow those that worked in prior/comsol/baseline/data/source_urls.txt.
Existing files are skipped unless --force. Polite: 3 s between requests, 4 retries with backoff.
"""
from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import time
import urllib.parse
import urllib.request
from pathlib import Path

BASE = "https://physics.nist.gov/cgi-bin/ASD/"
ROMAN = ["I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"]

# element -> number of ionisation stages whose LEVELS are needed (neutral .. stage)
LEVEL_STAGES = {"Cu": 9, "N": 4, "O": 4, "Ar": 3}  # v1u: Cu VI-IX added (PREREG 2026-11-26)
# element -> stages whose LINES are needed for radiative cooling / in-band emission
LINE_STAGES = {"Cu": 3, "N": 2, "O": 2, "Ar": 2}


def ie_url(el):
    q = dict(spectra=el, units=1, format=2, order=0, at_num_out="on", sp_name_out="on",
             ion_charge_out="on", el_name_out="on", seq_out="on", shells_out="on", conf_out="on",
             level_out="on", ion_conf_out="on", e_out=0, unc_out="on", biblio="on")
    return BASE + "ie.pl?" + urllib.parse.urlencode(q)


def levels_url(spec):
    q = dict(de=0, spectrum=spec, units=1, format=2, output=0, page_size=15000, multiplet_ordered=0,
             conf_out="on", term_out="on", level_out="on", unc_out=1, j_out="on", g_out="on",
             lande_out="on", perc_out="on", biblio="on", temp="")
    return BASE + "energy1.pl?" + urllib.parse.urlencode(q)


def lines_url(spec, low_nm=10.0, upp_nm=30000.0):
    # format=3: tab-delimited; unit=1: nm; show_av=2: vacuum < 200 nm, air 200-2000 nm (NIST default convention)
    q = dict(spectra=spec, limits_type=0, low_w=low_nm, upp_w=upp_nm, unit=1, submit="Retrieve Data", de=0,
             format=3, line_out=0, en_unit=1, output=0, bibrefs=1, page_size=100000, show_obs_wl=1,
             show_calc_wl=1, unc_out=1, order_out=0, max_low_enrg="", show_av=2, max_upp_enrg="",
             tsb_value=0, min_str="", A_out=0, f_out="on", intens_out="on", max_str="", allowed_out=1,
             forbid_out=1, min_accur="", min_intens="", conf_out="on", term_out="on", enrg_out="on",
             J_out="on", g_out="on")
    return BASE + "lines1.pl?" + urllib.parse.urlencode(q)


def jobs():
    for el in LEVEL_STAGES:
        yield f"{el}_ionization.csv", ie_url(el)
    for el, n in LEVEL_STAGES.items():
        for k in range(n):
            spec = f"{el} {ROMAN[k]}"
            yield f"{el}_{ROMAN[k]}_levels.csv", levels_url(spec)
    for el, n in LINE_STAGES.items():
        for k in range(n):
            spec = f"{el} {ROMAN[k]}"
            yield f"{el}_{ROMAN[k]}_lines.tsv", lines_url(spec)


def fetch(url, tries=4):
    last = None
    for i in range(tries):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "lipsim-research-data-fetch/0.1"})
            with urllib.request.urlopen(req, timeout=180) as r:
                return r.read()
        except Exception as e:  # noqa: BLE001
            last = e
            time.sleep(5 * 2 ** i)
    raise RuntimeError(f"failed after {tries} tries: {last!r}")


def count_rows(b: bytes) -> int:
    t = b.decode("utf-8", "replace").splitlines()
    return max(len([x for x in t if x.strip()]) - 1, 0)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", type=Path, default=Path("data/atomic/nist"))
    ap.add_argument("--force", action="store_true")
    a = ap.parse_args()
    a.out.mkdir(parents=True, exist_ok=True)
    mf = a.out / "manifest.json"
    manifest = json.loads(mf.read_text()) if mf.exists() else {}
    for name, url in jobs():
        f = a.out / name
        if f.exists() and not a.force and name in manifest:
            print("skip", name)
            continue
        b = fetch(url)
        head = b[:400].decode("utf-8", "replace").lower()
        suspicious = ("<html" in head) or ("error" in head and len(b) < 5000)
        f.write_bytes(b)
        manifest[name] = {"url": url, "retrieved_utc": dt.datetime.now(dt.timezone.utc).isoformat(timespec="seconds"),
                          "bytes": len(b), "sha256": hashlib.sha256(b).hexdigest(), "data_rows": count_rows(b),
                          "suspicious_html_or_error": suspicious}
        mf.write_text(json.dumps(manifest, indent=1))
        print(f"{name}: {len(b)} bytes, {manifest[name]['data_rows']} rows" + ("  <-- CHECK" if suspicious else ""))
        time.sleep(3)


if __name__ == "__main__":
    main()
