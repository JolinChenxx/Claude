"""Apply an explicit file-based priority stage at an imaging batch boundary."""
import json
from pathlib import Path
import subprocess
import sys


def run_before(root, outname, repo):
    directive = repo / 'results/T016i/before_remaining.json'
    if not directive.exists():
        return
    spec = json.loads(directive.read_text(encoding='utf-8'))
    if outname != spec['outname'] or Path(root).name not in spec['before_batches']:
        return
    print('Priority stage:', spec['script'], flush=True)
    subprocess.run([sys.executable, str(repo / spec['script'])], cwd=repo, check=True)
