"""Readers for NIST ASD exports (format=2 CSV with ="..." quoting; format=3 TSV for lines).

Levels are returned as (energy_eV, g) for bound levels below the first ionisation limit of that
spectrum. Rows marked 'Limit' and levels above the first limit (autoionising) are excluded; the
treatment of the partition-function cutoff (IP lowering) is done in partition.py.
"""
from __future__ import annotations

import csv
import re
from dataclasses import dataclass
from fractions import Fraction
from pathlib import Path

import numpy as np

_NUM = re.compile(r"[-+]?\d+(\.\d*)?([eE][-+]?\d+)?")


def _clean(s: str) -> str:
    s = s.strip()
    if s.startswith('="') and s.endswith('"'):
        s = s[2:-1]
    return s.strip().strip('"')


def _float(s: str):
    s = _clean(s).replace("[", "").replace("]", "").replace("(", "").replace(")", "").replace("?", "")
    s = s.replace("+x", "").replace("+y", "").replace("+z", "")
    m = _NUM.search(s)
    return float(m.group(0)) if m else None


def read_rows(path: Path) -> tuple[list[str], list[list[str]]]:
    text = Path(path).read_text(encoding="utf-8", errors="replace")
    rdr = csv.reader(text.splitlines())
    header = [h.strip() for h in next(rdr)]
    rows = [[_clean(c) for c in r] for r in rdr if any(c.strip() for c in r)]
    return header, rows


def _col(header, *names):
    for n in names:
        for i, h in enumerate(header):
            if h.lower().startswith(n.lower()):
                return i
    raise KeyError(f"none of {names} in header {header}")


@dataclass
class Levels:
    spectrum: str
    energy_eV: np.ndarray
    g: np.ndarray
    first_limit_eV: float | None
    n_rows_total: int
    n_excluded_above_limit: int


def read_levels(path: Path, spectrum: str = "") -> Levels:
    header, rows = read_rows(path)
    iJ = _col(header, "J")
    iE = _col(header, "Level (eV)", "Level")
    iT = _col(header, "Term")
    try:
        ig = _col(header, "g")
    except KeyError:
        ig = None
    E, G = [], []
    limits = []
    for r in rows:
        if len(r) <= max(iJ, iE):
            continue
        if r[iT].strip().lower() == "limit":
            v = _float(r[iE])
            if v is not None:
                limits.append(v)
            continue
        e = _float(r[iE])
        if e is None:
            continue
        g = None
        if ig is not None and ig < len(r):
            g = _float(r[ig])
        if g is None:
            j = r[iJ].strip().replace("?", "")
            if not j or j == "---":
                continue
            jj = j.split(",")[0].strip()  # unresolved J ranges such as '3/2,5/2' -> handled below
            if "," in j:  # sum of statistical weights of the listed J values
                g = float(sum(2 * Fraction(x.strip()) + 1 for x in j.split(",") if x.strip()))  # math
            else:
                g = float(2 * Fraction(jj) + 1)  # math
        E.append(e)
        G.append(g)
    E = np.asarray(E)
    G = np.asarray(G)
    first = min(limits) if limits else None
    keep = E < first if first is not None else np.ones_like(E, bool)
    return Levels(spectrum, E[keep], G[keep], first, len(rows), int((~keep).sum()))


def read_ionization(path: Path) -> dict[str, float]:
    header, rows = read_rows(path)
    iS = _col(header, "Sp. Name")
    iE = _col(header, "Ionization Energy")
    out = {}
    for r in rows:
        if len(r) <= max(iS, iE):
            continue  # footnote rows
        v = _float(r[iE])
        if v is not None:
            out[r[iS].strip()] = v
    return out
