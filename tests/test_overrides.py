"""Declared sensitivity overrides (OAT cards): thread-local and env paths, unknown names rejected, provenance hash changes."""
import json
import os
import sys
from pathlib import Path

import pytest
import yaml

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "src"))
from lipsim import params as P  # noqa: E402


def test_thread_override_and_hash():
    base, h0 = P.get("cu_removal_efficiency"), P.registry_hash()
    P.set_thread_overrides({"cu_removal_efficiency": 6e-12})
    try:
        assert P.get("cu_removal_efficiency") == 6e-12 and P.registry_hash() != h0
    finally:
        P.set_thread_overrides(None)
    assert P.get("cu_removal_efficiency") == base and P.registry_hash() == h0


def test_env_override(monkeypatch):
    monkeypatch.setenv(P.ENV_OVERRIDES, json.dumps({"cu_A1064_RT": 0.05}))
    assert P.get("cu_A1064_RT") == 0.05


def test_droplet_mass_follows_override():
    from lipsim.batch import droplet_params
    cfg = P.resolve(yaml.safe_load((ROOT / "configs/cases/v1bpde_high.yaml").read_text()))
    m0 = dict(droplet_params(cfg, 8e-13))["DROP_M"]
    P.set_thread_overrides({"cu_removal_efficiency": 3 * P.get("cu_removal_efficiency")})
    try:
        m3 = dict(droplet_params(cfg, 8e-13))["DROP_M"]
    finally:
        P.set_thread_overrides(None)
    assert m3 > 2.9 * m0


def test_unknown_override_rejected(tmp_path):
    from lipsim.batch import run_one
    with pytest.raises(KeyError):
        run_one({"id": "x", "case": "configs/cases/v1bpde_low.yaml", "grid": "smoke", "nec": "thin", "tstop_ns": 1,
                 "overrides": {"not_a_parameter": 1}}, {"root": str(tmp_path), "tables": str(tmp_path)}, dry=True)
