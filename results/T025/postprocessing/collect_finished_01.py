"""Collect newly finished production evidence using the established collector."""
import importlib.util
from pathlib import Path


REPO = Path(__file__).resolve().parents[3]
SOURCE = REPO / 'results/T021/postprocessing/collect_finished_03.py'


def main():
    spec = importlib.util.spec_from_file_location('completed_evidence', SOURCE)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    for batch, member, iteration in [('T023', 'high_thin_G0', 2),
                                      ('T025', 'low_thin_G0', 0)]:
        module.collect(batch, member, iteration)


if __name__ == '__main__':
    main()
