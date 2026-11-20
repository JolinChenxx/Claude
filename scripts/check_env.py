"""Environment self-check: versions + Numba parallel smoke test. Usage: python scripts/check_env.py [--threads N ...]"""
import argparse
import json
import os
import platform
import sys
import time


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--threads", type=int, nargs="*", default=[1, 8, 16, 32])
    ap.add_argument("--n", type=int, default=4_000_000)
    args = ap.parse_args()

    import numpy as np
    import scipy
    import numba
    from numba import njit, prange

    out = {
        "python": sys.version.split()[0],
        "platform": platform.platform(),
        "numpy": np.__version__,
        "scipy": scipy.__version__,
        "numba": numba.__version__,
        "numba_threading_layer_default": numba.config.THREADING_LAYER,
        "numba_num_threads_max": numba.config.NUMBA_NUM_THREADS,
        "os_cpu_count": os.cpu_count(),
    }
    for mod in ("h5py", "yaml", "pandas", "skimage", "sklearn", "matplotlib", "SALib", "emcee", "pytest"):
        try:
            m = __import__(mod)
            out[mod] = getattr(m, "__version__", "ok")
        except Exception as e:  # noqa: BLE001
            out[mod] = f"IMPORT FAILED: {e!r}"

    @njit(parallel=True, fastmath=False, cache=False)
    def stencil(a, b, reps):
        n = a.size
        for _ in range(reps):
            for i in prange(1, n - 1):
                b[i] = 0.25 * a[i - 1] + 0.5 * a[i] + 0.25 * a[i + 1] + np.sqrt(abs(a[i]) + 1.0)
            for i in prange(1, n - 1):
                a[i] = b[i] * 0.5
        return a[n // 2]

    a = np.random.default_rng(0).random(args.n)
    b = np.zeros_like(a)
    stencil(a.copy(), b, 1)  # JIT compile
    out["threading_layer_used"] = numba.threading_layer()
    timings = {}
    for t in args.threads:
        if t > numba.config.NUMBA_NUM_THREADS:
            continue
        numba.set_num_threads(t)
        aa = a.copy()
        t0 = time.perf_counter()
        stencil(aa, b, 20)
        dt = time.perf_counter() - t0
        timings[t] = round(args.n * 20 / dt / 1e6, 1)  # M cell-updates/s
    out["stencil_Mupdates_per_s_by_threads"] = timings
    print(json.dumps(out, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
