"""Plot the primary experimental trend metrics (PLAN v2 §8.1) from data/exp/metrics_*.csv."""
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

root = Path(sys.argv[1] if len(sys.argv) > 1 else "data/exp")
fig, ax = plt.subplots(1, 3, figsize=(13, 3.8))
for name, c, lab in [("high", "#c0392b", "high fluence (S4)"), ("low", "#2471a3", "low fluence (S3)")]:
    d = pd.read_csv(root / f"metrics_{name}.csv").groupby("t_ns", as_index=False).mean(numeric_only=True)
    L300 = np.interp(300, d.t_ns, d.L)
    sat = d.n_sat > 0
    ax[0].semilogy(d.t_ns[~sat], d.L[~sat] / L300, "o", c=c, label=lab)
    ax[0].semilogy(d.t_ns[sat], d.L[sat] / L300, "o", mfc="none", c=c, label=f"{name}: saturated (lower bound)")
    s0 = np.interp(300, d.t_ns, d.sigma_x2_um2 + d.sigma_z2_um2)
    ax[1].plot(d.t_ns, (d.sigma_x2_um2 + d.sigma_z2_um2 - s0) / 1e3, "o-", c=c, ms=4, label=lab)
    z0 = np.interp(300, d.t_ns, d.zc_um)
    ax[2].plot(d.t_ns, d.zc_um - z0, "o-", c=c, ms=4, label=lab)
ax[0].axvspan(0, 60, color="0.9"); ax[1].axvspan(0, 60, color="0.9"); ax[2].axvspan(0, 60, color="0.9")
ax[0].set(xlabel="t (ns)", ylabel="L(t) / L(300 ns)", title="P1/P2  total band signal")
ax[1].set(xlabel="t (ns)", ylabel="Δ(σx²+σz²) (10³ µm²)", title="P3  size (2nd moment, rel. 300 ns)")
ax[2].set(xlabel="t (ns)", ylabel="Δz_c (µm)", title="P5  centroid height (rel. 300 ns)")
for a in ax: a.legend(fontsize=7); a.grid(alpha=.3)
fig.tight_layout(); fig.savefig(root / "trends.png", dpi=130)
