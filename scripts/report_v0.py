"""v0 comparison report (PLAN v3 §8, PREREG_v0 §3), generated from collected results — no hand editing.

    python scripts/report_v0.py results/T011 --out results/T011/COMPARISON_v0.md

Inputs: results/<card>/<member>/it<k>/{budget.csv, source_meta.json, image/metrics_*.csv, image/frames_b_*.npz}
        data/exp/metrics_{high,low}.csv, data/exp/curves/fig3d_shock.csv
Every member, iteration, filter (a/b/c), transfer (thin/sa) and defocus (low/baseline/high) is reported.
"""
from __future__ import annotations

import argparse
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
import pandas as pd  # noqa: E402

REPO = Path(__file__).resolve().parents[1]
CRIT = {"P1": (0.67, 1.5, 0.4, 2.5), "P2": (0.25, 0.5)}


def exp_metrics(series):
    from sys import path
    path.insert(0, str(REPO / "src"))
    from lipsim.pipeline.metrics import decay_time, time_to_fraction
    d = pd.read_csv(REPO / f"data/exp/metrics_{series}.csv").groupby("t_ns", as_index=False).mean(numeric_only=True)
    tau, _ = decay_time(d.t_ns, d.L)
    return d, tau, time_to_fraction(d.t_ns, d.L)


def turning_time(t, s2):
    t, s2 = np.asarray(t), np.asarray(s2)
    sel = (t >= 150) & (t <= 630)
    return float(t[sel][np.argmax(s2[sel])]) if sel.any() else np.nan


def grade_ratio(r, lo, hi, lo2, hi2):
    if not np.isfinite(r):
        return "n/a"
    return "pass" if lo <= r <= hi else ("partial" if lo2 <= r <= hi2 else "fail")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("root", type=Path)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--title", default="COMPARISON_v0 — v0 quick baseline vs experiment")
    ap.add_argument("--notes", type=Path, help="Prepend explicit coverage and limitations to the generated comparison")
    a = ap.parse_args()
    import sys
    sys.path.insert(0, str(REPO / "src"))
    from lipsim.pipeline.metrics import decay_time, time_to_fraction

    E = {s: exp_metrics(s) for s in ("high", "low")}
    rows = []
    for mdir in sorted(p for p in a.root.iterdir() if p.is_dir()):
        series = "high" if mdir.name.startswith("high") else "low"
        for it in sorted(p for p in mdir.iterdir() if p.is_dir() and p.name.startswith("it")):
            bud = pd.read_csv(it / "budget.csv") if (it / "budget.csv").exists() else None
            for f in sorted((it / "image").glob("metrics_*.csv")) if (it / "image").exists() else []:
                key = f.stem.replace("metrics_", "")
                filt, tr, dfc = key.split("_")
                m = pd.read_csv(f)
                tau, _ = decay_time(m.t_ns, m.L_radiance)
                d10 = time_to_fraction(m.t_ns, m.L_radiance)
                ttm = turning_time(m.t_ns, m.sigma_x2_um2 + m.sigma_z2_um2)
                de, te, d10e = E[series]
                tte = turning_time(de.t_ns, de.sigma_x2_um2 + de.sigma_z2_um2)
                row = {"member": mdir.name, "series": series, "picard": it.name, "filter": filt, "transfer": tr,
                       "defocus": dfc, "t_end_ns": float(m.t_ns.max()), "P1_tau_model": tau, "P1_tau_exp": te,
                       "P1_ratio": tau / te if np.isfinite(tau) else np.nan, "P2_dt10_model": d10, "P2_dt10_exp": d10e,
                       "P3_turn_model": ttm, "P3_turn_exp": tte}
                row["P1"] = grade_ratio(row["P1_ratio"], *CRIT["P1"])
                if np.isfinite(d10):
                    rel = abs(d10 / d10e - 1)
                    row["P2"] = "pass" if rel <= .25 else "partial" if rel <= .5 else "fail"
                else:
                    # 10% level not reached before the last frame: P2 > t_last - 300 ns
                    lim = float(m.t_ns.max()) - 300.0
                    row["P2_dt10_model"] = f"> {lim:.0f} (not reached)"
                    row["P2"] = "fail" if lim > 1.5 * d10e else "n/a (window)"
                rel3 = abs(ttm / tte - 1) if np.isfinite(ttm) and np.isfinite(tte) else np.nan
                row["P3"] = "n/a" if not np.isfinite(rel3) else ("pass" if rel3 <= .3 else "partial(sign)")
                if bud is not None:
                    last = bud.iloc[-1]
                    row.update({"E_inc_uJ": last.E_inc * 1e6, "E_gas_abs_uJ": last.E_gas_abs * 1e6,
                                "E_targ_abs_uJ": last.E_targ_abs * 1e6, "E_rad_uJ": last.E_rad * 1e6,
                                "M_Cu_ng": last.M_Cu * 1e12})
                rows.append(row)
    T = pd.DataFrame(rows)
    a.out.parent.mkdir(parents=True, exist_ok=True)
    T.to_csv(a.out.parent / "metrics_table.csv", index=False, float_format="%.4g")

    # P6: fluence ordering of tau (same filter/transfer/defocus, last Picard iteration, G0)
    p6 = []
    if len(T):
        last = T.sort_values("picard").groupby(["member", "filter", "transfer", "defocus"]).tail(1)
        for (nec, filt, tr, dfc), g in last.assign(nec=last.member.str.split("_").str[1]).groupby(["nec", "filter", "transfer", "defocus"]):
            h = g[(g.series == "high") & g.member.str.endswith("G0")]
            lo = g[(g.series == "low") & g.member.str.endswith("G0")]
            if len(h) and len(lo):
                r = float(lo.P1_tau_model.iloc[0] / h.P1_tau_model.iloc[0])
                p6.append({"nec": nec, "filter": filt, "transfer": tr, "defocus": dfc, "tau_low_over_high": r,
                           "exp": E["low"][1] / E["high"][1], "P6": "pass" if (r > 1) == (E["low"][1] / E["high"][1] > 1) else "fail"})
    P6 = pd.DataFrame(p6)

    # figure: normalised L(t), baseline defocus, filter b, both transfers, last iteration of each member
    fig, ax = plt.subplots(1, 2, figsize=(12, 4.2))
    for k, s in enumerate(("high", "low")):
        de = E[s][0]
        L300 = np.interp(300, de.t_ns, de.L)
        ax[k].semilogy(de.t_ns, de.L / L300, "ko", label="experiment")
        for mdir in sorted(p for p in a.root.iterdir() if p.is_dir() and p.name.startswith(s)):
            its = sorted(p for p in mdir.iterdir() if p.is_dir() and p.name.startswith("it"))
            if not its:
                continue
            for tr, ls in (("thin", "--"), ("sa", "-")):
                imaged = [it for it in its if (it / "image" / f"metrics_b_{tr}_baseline.csv").is_file()]
                if not imaged:
                    continue
                f = imaged[-1] / "image" / f"metrics_b_{tr}_baseline.csv"
                if f.exists():
                    m = pd.read_csv(f)
                    ax[k].semilogy(m.t_ns, m.L_radiance / np.interp(300, m.t_ns, m.L_radiance), ls,
                                   label=f"{mdir.name} {imaged[-1].name} {tr}")
        ax[k].axvspan(0, 60, color="0.9")
        ax[k].set(title=f"{s} fluence: L(t)/L(300 ns), filter b", xlabel="t (ns)", ylim=(1e-3, 30))
        ax[k].legend(fontsize=6)
        ax[k].grid(alpha=.3)
    fig.tight_layout()
    fig.suptitle("Latest available imaged iteration per member", fontsize=10)
    fig.tight_layout(rect=(0, 0, 1, .95))
    fig.savefig(a.out.parent / "L_normalized.png", dpi=130)

    with open(a.out, "w", encoding="utf-8") as fp:
        fp.write(f"# {a.title} (generated by scripts/report_v0.py)\n\n")
        if a.notes:
            fp.write(a.notes.read_text(encoding="utf-8") + "\n\n")
        fp.write("Non-frozen quick baseline (PREREG_v0). All members/scenarios listed; criteria from PREREG_v0 §3.\n\n")
        fp.write(f"Experiment: τ_L high {E['high'][1]:.0f} ns, low {E['low'][1]:.0f} ns; Δt10 high {E['high'][2]:.0f}, low {E['low'][2]:.0f} ns.\n\n")
        fp.write("## Main metrics (every member × iteration × scenario)\n\n")
        fp.write(T.to_markdown(index=False, floatfmt=".3g") if len(T) else "(no results)\n")
        fp.write("\n\n## P6 fluence ordering\n\n")
        fp.write(P6.to_markdown(index=False, floatfmt=".3g") if len(P6) else "(insufficient members)\n")
        fp.write("\n\nFigure: `L_normalized.png`.\n")
    print("wrote", a.out)


if __name__ == "__main__":
    main()
