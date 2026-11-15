"""The provenance check passes on the repository and catches unregistered numbers (PLAN v2 §1.1, M0.4)."""
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))
sys.path.insert(0, str(ROOT / "src"))
import check_provenance  # noqa: E402


def _mini_repo(tmp_path):
    (tmp_path / "data" / "params").mkdir(parents=True)
    shutil.copy(ROOT / "data" / "params" / "registry.yaml", tmp_path / "data" / "params" / "registry.yaml")
    (tmp_path / "configs").mkdir()
    (tmp_path / "src" / "lipsim" / "eos").mkdir(parents=True)
    return tmp_path


def test_repository_is_clean():
    assert check_provenance.main(["--root", str(ROOT)]) == 0


def test_catches_bare_number_in_config(tmp_path):
    r = _mini_repo(tmp_path)
    (r / "configs" / "bad.yaml").write_text("laser:\n  energy: 0.4e-3\nnumerics:\n  cfl: 0.3\n")
    assert check_provenance.main(["--root", str(r)]) == 1
    (r / "configs" / "bad.yaml").write_text("laser:\n  energy: '@E_pulse_high'\nnumerics:\n  cfl: 0.3\n")
    assert check_provenance.main(["--root", str(r)]) == 0


def test_catches_unregistered_reference(tmp_path):
    r = _mini_repo(tmp_path)
    (r / "configs" / "bad.yaml").write_text("laser:\n  absorptivity: '@fitted_absorptivity'\n")
    assert check_provenance.main(["--root", str(r)]) == 1


def test_catches_untagged_literal_in_physics_source(tmp_path):
    r = _mini_repo(tmp_path)
    f = r / "src" / "lipsim" / "eos" / "x.py"
    f.write_text("def f(T):\n    return 7.72638 * T\n")
    assert check_provenance.main(["--root", str(r)]) == 1
    f.write_text("def f(T):\n    return 7.72638 * T  # reg:IP_Cu_I\n")
    assert check_provenance.main(["--root", str(r)]) == 0
    f.write_text("def f(T):\n    return 7.72638 * T  # reg:not_there\n")
    assert check_provenance.main(["--root", str(r)]) == 1


def test_params_loader():
    from lipsim import params
    assert params.get("k_B") == 1.380649e-23
    assert params.resolve({"a": "@c", "b": [1, "@h"]}) == {"a": 299792458.0, "b": [1, 6.62607015e-34]}
    try:
        params.get("nonexistent")
    except KeyError:
        pass
    else:
        raise AssertionError("unregistered name must raise")
