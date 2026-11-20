"""Access to the parameter registry (data/params/registry.yaml). Physical code reads numbers only through here."""
from __future__ import annotations

import hashlib
from functools import lru_cache
from pathlib import Path

import yaml

REPO = Path(__file__).resolve().parents[2]
REGISTRY = REPO / "data" / "params" / "registry.yaml"
GRADES = set("ABCDEFN")
REQUIRED = ("value", "unit", "grade", "source", "uncertainty", "date", "verified")


@lru_cache(maxsize=1)
def registry(path: str | None = None) -> dict:
    p = Path(path) if path else REGISTRY
    return yaml.safe_load(p.read_text(encoding="utf-8")) or {}


def registry_hash(path: str | None = None) -> str:
    p = Path(path) if path else REGISTRY
    return hashlib.sha256(p.read_bytes()).hexdigest()


def get(name: str):
    """Value of a registered parameter. Raises KeyError for unregistered names (by design)."""
    reg = registry()
    if name not in reg:
        raise KeyError(f"parameter '{name}' is not registered in {REGISTRY.relative_to(REPO)}")
    return reg[name]["value"]


def resolve(obj):
    """Recursively replace '@name' strings in a config object by registry values."""
    if isinstance(obj, str) and obj.startswith("@"):
        return get(obj[1:])
    if isinstance(obj, dict):
        return {k: resolve(v) for k, v in obj.items()}
    if isinstance(obj, list):
        return [resolve(v) for v in obj]
    return obj
