"""Build PLUTO 4.4-patch4 from the official archive without the interactive setup.py.

    python scripts/build_pluto.py --problem <dir with definitions.h, init.c[, *.c, *.h]> --out <build dir>
           [--platform linux|windows] [--mpi] [--eos ideal|pvte] [--states plm|weno3]
           [--extra-obj a.o b.o] [--pluto-dir <extracted PLUTO>]

The upstream source is unpacked once from third_party/pluto/pluto-4.4-patch4.tar.gz (SHA256 checked).
Project files in --problem are copied into the build directory; VPATH puts '.' first, so a file there
with an upstream name (e.g. split_source.c) replaces the upstream one. Every such replacement is listed
in build_provenance.json with a unified diff against the official file. On Windows the defs/adapters
from third_party/pluto/windows/ are used (T005/T008); on Linux Config/Linux.gcc.defs (+ mpicc if --mpi).
"""
from __future__ import annotations

import argparse
import difflib
import hashlib
import json
import os
import re
import shutil
import subprocess
import tarfile
from pathlib import Path

REPO = Path(__file__).resolve().parents[1]
TARBALL = REPO / "third_party/pluto/pluto-4.4-patch4.tar.gz"
TAR_SHA = "1ba5527b76d49fdd78ae24dbfbdad085ec83393748f1e618516a9d63bd945787"


def sha(p: Path) -> str:
    return hashlib.sha256(p.read_bytes()).hexdigest()


def unpack(dest: Path) -> Path:
    root = dest / "PLUTO"
    if (root / "Src").exists():
        return root
    if sha(TARBALL) != TAR_SHA:
        raise SystemExit("PLUTO archive SHA256 mismatch")
    dest.mkdir(parents=True, exist_ok=True)
    with tarfile.open(TARBALL) as tf:
        tf.extractall(dest, filter="data")
    return root


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("--problem", type=Path, required=True)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--platform", choices=["linux", "windows"], default="windows" if os.name == "nt" else "linux")
    ap.add_argument("--mpi", action="store_true")
    ap.add_argument("--eos", choices=["ideal", "pvte"], default="ideal")
    ap.add_argument("--states", default="plm")
    ap.add_argument("--extra-obj", nargs="*", default=[])
    ap.add_argument("--pluto-dir", type=Path, default=None)
    ap.add_argument("--jobs", type=int, default=8)
    a = ap.parse_args(argv)

    pluto = a.pluto_dir or unpack(a.out.parent / "_pluto_src")
    src = pluto / "Src"
    a.out.mkdir(parents=True, exist_ok=True)
    replaced = []
    for f in sorted(a.problem.iterdir()):
        if f.suffix in (".c", ".h") or f.name in ("definitions.h",):
            shutil.copy2(f, a.out / f.name)
            ups = list(src.rglob(f.name))
            if ups and f.name not in ("init.c", "definitions.h", "userdef_output.c", "pvte_law.c"):
                up = ups[0]
                diff = "".join(difflib.unified_diff(up.read_text(errors="replace").splitlines(True),
                                                    f.read_text(errors="replace").splitlines(True),
                                                    str(up.relative_to(pluto)), f.name))
                replaced.append({"file": str(up.relative_to(pluto)), "local": f.name, "upstream_sha256": sha(up),
                                 "local_sha256": sha(f), "diff": diff})
    sep = ";" if a.platform == "windows" else ":"
    if a.platform == "windows":
        win = REPO / "third_party/pluto/windows"
        arch = f"Windows.{'mpi8' if a.mpi else 'serial'}.gcc.defs"
        shutil.copy2(win / "Config" / arch, pluto / "Config" / arch)
        shutil.copy2(win / "Src/HD/makefile", src / "HD/makefile")
        for f in ("windows_compat.c", "windows_compat.h"):
            shutil.copy2(win / f, a.out / f)
        a.extra_obj.append("windows_compat.o")
    else:
        arch = "Linux.mpicc.defs" if a.mpi else "Linux.gcc.defs"
    t = (src / "Templates/makefile").read_text()
    t = re.sub(r"(?m)^ARCH\s*=.*$", f"ARCH = {arch}", t)
    t = re.sub(r"(?m)^PLUTO_DIR\s*=.*$", f"PLUTO_DIR = {pluto.resolve().as_posix()}", t)
    t = re.sub(r"(?m)^VPATH\s*=.*$", "VPATH = " + sep.join([".", "$(SRC)/New", "$(SRC)", "$(SRC)/Time_Stepping", "$(SRC)/States"]), t)
    eos_mk = "EOS/PVTE" if a.eos == "pvte" else "EOS/Ideal"
    extra = (f"include $(SRC)/HD/makefile\ninclude $(SRC)/{eos_mk}/makefile\n"
             f"OBJ += {a.states}_states.o rk_step.o update_stage.o {' '.join(a.extra_obj)}\n")
    if a.platform == "windows":
        t = t.replace("$(SRC)/HD:$(SRC)/MHD", "$(SRC)/HD;$(SRC)/MHD")
    t = t.replace("pluto: $(OBJ)", extra + "\npluto: $(OBJ)")
    (a.out / "makefile").write_text(t)
    make = shutil.which("mingw32-make") or shutil.which("make") or "make"
    r = subprocess.run([make, "-j", str(a.jobs)], cwd=a.out, capture_output=True, text=True)
    (a.out / "build.log").write_text(r.stdout + r.stderr)
    prov = {"tarball_sha256": TAR_SHA, "platform": a.platform, "arch": arch, "mpi": a.mpi, "eos": a.eos,
            "states": a.states, "replaced_upstream_files": replaced, "make_returncode": r.returncode,
            "problem_files": {f.name: sha(f) for f in sorted(a.problem.iterdir()) if f.is_file()}}
    exe = a.out / ("pluto.exe" if a.platform == "windows" else "pluto")
    if exe.exists():
        prov["executable_sha256"] = sha(exe)
    (a.out / "build_provenance.json").write_text(json.dumps(prov, indent=1))
    print(f"make rc={r.returncode}; replaced upstream files: {[x['file'] for x in replaced]}")
    if r.returncode:
        print(r.stderr[-3000:])
    return r.returncode


if __name__ == "__main__":
    raise SystemExit(main())
