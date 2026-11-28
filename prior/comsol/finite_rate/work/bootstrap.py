"""Create a self-contained route-2 workspace; source results stay read-only."""
from pathlib import Path
from datetime import datetime, timedelta, timezone
import json, shutil

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT.parent / 'COMSOL_CuPlasma_S4_server_handoff_20261105' / 'project'
for name in ('data', 'results', 'figures', 'logs', 'models', 'docs'):
    (ROOT / name).mkdir(exist_ok=True)
session = ROOT / 'session.json'
if not session.exists():
    now = datetime.now(timezone.utc)
    session.write_text(json.dumps(dict(started_utc=now.isoformat(),
        started_beijing=now.astimezone(timezone(timedelta(hours=8))).isoformat(),
        deadline_utc=(now+timedelta(hours=8)).isoformat(), budget_hours=8,
        report_interval_seconds=3600, threads=8, source=str(SOURCE),
        baseline='B64A', status='running', last_report_utc=now.isoformat()), indent=2), encoding='utf-8')
for name in ('atomic_levels.npz', 'radiation_line_manifest.json', 'Cu_I_visible_lines.tsv',
             'Cu_II_visible_lines.tsv', 'N_I_visible_lines.tsv', 'N_II_visible_lines.tsv',
             'O_I_visible_lines.tsv', 'O_II_visible_lines.tsv'):
    shutil.copy2(SOURCE/'data'/name, ROOT/'data'/name)
src = (SOURCE/'work'/'radiation_s4.py').read_text(encoding='utf-8')
# Atomic() writes a manifest, so its DATA directory must belong to this route.
(ROOT/'work'/'legacy_radiation.py').write_text(src, encoding='utf-8')
shutil.copy2(SOURCE/'work'/'line_transfer_fast.py', ROOT/'work'/'line_transfer_fast.py')
print(session.read_text(encoding='utf-8'))
