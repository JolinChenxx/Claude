"""Access to the parameter registry (data/params/registry.yaml). Physical code reads numbers only through here."""
from __future__ import annotations

import hashlib
import json
import os
import threading
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


_LOCAL = threading.local()
ENV_OVERRIDES = "LIPSIM_OVERRIDES"


def overrides() -> dict:
    """Declared sensitivity overrides (PREREG OAT cards only): thread-local first, else the LIPSIM_OVERRIDES env JSON."""
    o = getattr(_LOCAL, "overrides", None)
    if o is not None:
        return o
    raw = os.environ.get(ENV_OVERRIDES)
    return json.loads(raw) if raw else {}


def set_thread_overrides(o: dict | None):
    _LOCAL.overrides = dict(o) if o else None


def registry_hash(path: str | None = None) -> str:
    p = Path(path) if path else REGISTRY
    h = hashlib.sha256(p.read_bytes())
    o = overrides()
    if o:  # a run with overrides must not share the baseline provenance hash
        h.update(json.dumps(o, sort_keys=True).encode())
    return h.hexdigest()


def get(name: str):
    """Value of a registered parameter. Raises KeyError for unregistered names (by design). A declared override
    (sensitivity runs only) replaces the value; overriding an unregistered name is an error."""
    reg = registry()
    if name not in reg:
        raise KeyError(f"parameter '{name}' is not registered in {REGISTRY.relative_to(REPO)}")
    o = overrides()
    if name in o:
        return o[name]
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
