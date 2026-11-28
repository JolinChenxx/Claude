"""Reader for PLUTO binary output (.dbl, single_file, little/big endian) + grid.out + dbl.out.

grid.out: comment lines starting with '#', then for each dimension: N, followed by N lines "i  xL  xR".
dbl.out : one line per output: "nfile  t  dt  nstep  single_file  little|big  var1 var2 ...".
data.NNNN.dbl: all variables, each nx1*nx2*nx3 doubles, x1 fastest (Fortran order of (x1,x2,x3)).
"""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

import numpy as np


@dataclass
class Grid:
    x1_edges: np.ndarray
    x2_edges: np.ndarray

    @property
    def x1(self):
        return 0.5 * (self.x1_edges[1:] + self.x1_edges[:-1])

    @property
    def x2(self):
        return 0.5 * (self.x2_edges[1:] + self.x2_edges[:-1])


def read_grid(path: Path) -> Grid:
    lines = [ln for ln in Path(path).read_text().splitlines() if ln.strip() and not ln.lstrip().startswith("#")]
    dims, i = [], 0
    while i < len(lines) and len(dims) < 3:
        n = int(lines[i].split()[0])
        rows = [lines[i + 1 + k].split() for k in range(n)]
        edges = np.array([float(r[1]) for r in rows] + [float(rows[-1][2])])
        dims.append(edges)
        i += 1 + n
    return Grid(dims[0], dims[1])


@dataclass
class Snapshot:
    nfile: int
    t: float
    vars: dict


def read_index(path: Path) -> list[tuple[int, float, str, list[str]]]:
    out = []
    for ln in Path(path).read_text().splitlines():
        p = ln.split()
        if len(p) >= 7:
            out.append((int(p[0]), float(p[1]), p[5], p[6:]))
    return out


def read_snapshot(run_dir: Path, nfile: int, grid: Grid | None = None) -> Snapshot:
    run_dir = Path(run_dir)
    grid = grid or read_grid(run_dir / "grid.out")
    idx = {n: (t, endian, names) for n, t, endian, names in read_index(run_dir / "dbl.out")}
    t, endian, names = idx[nfile]
    n1, n2 = len(grid.x1), len(grid.x2)
    dt = np.dtype("<f8" if endian.startswith("little") else ">f8")
    raw = np.fromfile(run_dir / f"data.{nfile:04d}.dbl", dtype=dt)
    if raw.size != n1 * n2 * len(names):
        raise ValueError(f"size mismatch: {raw.size} vs {n1}x{n2}x{len(names)}")
    v = {nm: raw[k * n1 * n2:(k + 1) * n1 * n2].reshape(n2, n1) for k, nm in enumerate(names)}  # [z, r]
    return Snapshot(nfile, t, v)
