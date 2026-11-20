"""Compare two budget.csv files at their common final time (cross-platform / serial-vs-MPI check).
usage: python scripts/compare_budget.py A.csv B.csv [--tol 0.005]  -> exit 1 if any column differs > tol"""
import argparse
import sys

import numpy as np
import pandas as pd

ap = argparse.ArgumentParser()
ap.add_argument("a"); ap.add_argument("b"); ap.add_argument("--tol", type=float, default=0.005)
a = ap.parse_args()
A, B = pd.read_csv(a.a), pd.read_csv(a.b)
t = min(A.t_ns.max(), B.t_ns.max())
worst = 0.0
for c in A.columns[1:]:
    va, vb = np.interp(t, A.t_ns, A[c]), np.interp(t, B.t_ns, B[c])
    scale = max(abs(va), abs(vb), 1e-30)
    d = abs(va - vb) / scale if scale > 1e-12 else 0.0
    worst = max(worst, d)
    print(f"{c:16s} {va:.6e} {vb:.6e} rel {d:.2e}")
print(f"t = {t} ns, worst relative difference {worst:.2e} (tol {a.tol})")
sys.exit(0 if worst <= a.tol else 1)
