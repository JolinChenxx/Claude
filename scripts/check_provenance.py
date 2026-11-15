"""Provenance check (PLAN v2 §1.1). Exit code 1 on any violation.

Checks
1. Registry schema: required fields, allowed grades (X is forbidden), unique names.
2. Configs (configs/**/*.yaml): every numeric leaf outside a top-level `numerics:` block is a violation;
   physical inputs must be written as "@name" and the name must exist in the registry.
3. Physics source (src/lipsim/{eos,transport,optics,hydro,target,laser,imaging,uq}): non-trivial numeric
   literals must carry an inline tag on the same line: `# reg:<name>` (registered value, name must exist),
   `# num` (numerical, grade N) or `# math` (pure mathematics, e.g. 4*pi, 1/3).
Usage: python scripts/check_provenance.py [--root .] [--warn-unverified]
"""
from __future__ import annotations

import argparse
import io
import re
import sys
import tokenize
from pathlib import Path

import yaml

GRADES = set("ABCDEFN")
REQUIRED = ("value", "unit", "grade", "source", "uncertainty", "date", "verified")
PHYSICS_PKGS = ("eos", "transport", "optics", "hydro", "target", "laser", "imaging", "uq")
TRIVIAL = {"0", "1", "2", "3", "0.0", "1.0", "2.0", "3.0", "0.5", "0.25", "-1", "4", "4.0", "10", "10.0"}
TAG = re.compile(r"#\s*(reg:([A-Za-z_][\w]*)|num\b|math\b)")


def check_registry(path: Path, warn_unverified: bool) -> tuple[list[str], dict]:
    errs = []
    text = path.read_text(encoding="utf-8")
    keys = re.findall(r"^([A-Za-z_][\w]*)\s*:", text, flags=re.M)
    dup = {k for k in keys if keys.count(k) > 1}
    if dup:
        errs.append(f"registry: duplicate names {sorted(dup)}")
    reg = yaml.safe_load(text) or {}
    for name, e in reg.items():
        if not isinstance(e, dict):
            errs.append(f"registry:{name}: entry is not a mapping")
            continue
        miss = [f for f in REQUIRED if f not in e]
        if miss:
            errs.append(f"registry:{name}: missing fields {miss}")
        g = str(e.get("grade", ""))
        if g == "X":
            errs.append(f"registry:{name}: grade X (derived from comparison data) is forbidden")
        elif g not in GRADES:
            errs.append(f"registry:{name}: unknown grade '{g}'")
        # numeric-looking strings (e.g. YAML 1.1 parses 1e5 without exponent sign as a string)
        leaves = []
        _walk(e.get("value"), [], leaves)
        for _, v in leaves:
            if isinstance(v, str) and re.fullmatch(r"[-+]?\d*\.?\d+([eE][-+]?\d+)?", v.strip()):
                errs.append(f"registry:{name}: value '{v}' is a string; write the exponent with a sign (e.g. 1.0e+5)")
        if not str(e.get("source", "")).strip():
            errs.append(f"registry:{name}: empty source")
        if warn_unverified and e.get("verified") is False:
            print(f"WARNING registry:{name}: not yet verified against its source", file=sys.stderr)
    return errs, reg


def _walk(obj, path, out):
    if isinstance(obj, dict):
        for k, v in obj.items():
            _walk(v, path + [str(k)], out)
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            _walk(v, path + [str(i)], out)
    else:
        out.append((path, obj))


def check_configs(root: Path, reg: dict) -> list[str]:
    errs = []
    for f in sorted((root / "configs").rglob("*.yaml")) if (root / "configs").exists() else []:
        cfg = yaml.safe_load(f.read_text(encoding="utf-8")) or {}
        leaves = []
        _walk(cfg, [], leaves)
        for path, v in leaves:
            where = f"{f.relative_to(root)}:{'.'.join(path)}"
            if isinstance(v, bool):
                continue
            if isinstance(v, (int, float)) and path and path[0] != "numerics":
                errs.append(f"{where}: bare number {v!r}; use '@<registered name>' or move it under numerics:")
            if isinstance(v, str) and v.startswith("@") and v[1:] not in reg:
                errs.append(f"{where}: '{v}' is not registered")
    return errs


def check_source(root: Path, reg: dict) -> list[str]:
    errs = []
    base = root / "src" / "lipsim"
    for pkg in PHYSICS_PKGS:
        for f in sorted((base / pkg).rglob("*.py")) if (base / pkg).exists() else []:
            src = f.read_text(encoding="utf-8")
            lines = src.splitlines()
            flagged = set()
            for tok in tokenize.generate_tokens(io.StringIO(src).readline):
                if tok.type != tokenize.NUMBER or tok.string in TRIVIAL:
                    continue
                ln = tok.start[0]
                line = lines[ln - 1]
                m = TAG.search(line)
                if not m:
                    if ln not in flagged:
                        errs.append(f"{f.relative_to(root)}:{ln}: untagged numeric literal {tok.string} "
                                    f"(add '# reg:<name>', '# num' or '# math')")
                        flagged.add(ln)
                elif m.group(2) and m.group(2) not in reg:
                    errs.append(f"{f.relative_to(root)}:{ln}: '# reg:{m.group(2)}' is not registered")
    return errs


def main(argv=None) -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    ap.add_argument("--warn-unverified", action="store_true")
    a = ap.parse_args(argv)
    errs, reg = check_registry(a.root / "data" / "params" / "registry.yaml", a.warn_unverified)
    errs += check_configs(a.root, reg)
    errs += check_source(a.root, reg)
    for e in errs:
        print("VIOLATION", e)
    print(f"check_provenance: {len(reg)} registered parameters, {len(errs)} violations")
    return 1 if errs else 0


if __name__ == "__main__":
    sys.exit(main())
