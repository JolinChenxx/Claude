"""Fetch pinned upstream sources and initialize the 72 h campaign (idempotent)."""
from pathlib import Path
import datetime as dt
import hashlib
import json
import tarfile
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
for folder in ['vendor', 'state', 'reports', 'reports/hourly', 'reports/daily',
               'data/experimental', 'runs', 'solver', 'figures', 'logs', 'config']:
    (ROOT / folder).mkdir(parents=True, exist_ok=True)
manifest = ROOT / 'state/campaign.json'
if not manifest.exists():
    now = dt.datetime.now(dt.timezone.utc)
    manifest.write_text(json.dumps({
        'started_at': now.isoformat(), 'deadline': (now + dt.timedelta(hours=72)).isoformat(),
        'timezone': 'Asia/Shanghai', 'status': 'building', 'duration_hours': 72,
        'model_version': 's4-pluto-cuair-v1', 'physical_cores': 64,
        'memory_budget_gib': 180, 'report_delivery': 'local_files_only'
    }, indent=2), encoding='utf-8')
url = 'https://plutocode.ph.unito.it/Downloads/pluto-4.4-patch4.tar.gz'
archive = ROOT / 'vendor/pluto-4.4-patch4.tar.gz'
if not archive.exists():
    urllib.request.urlretrieve(url, archive)
digest = hashlib.sha256(archive.read_bytes()).hexdigest()
print('PLUTO archive', archive.stat().st_size, 'sha256', digest, flush=True)
(ROOT / 'vendor/sources.json').write_text(json.dumps({
    'pluto': {'url': url, 'sha256': digest, 'version': '4.4-patch4'}
}, indent=2), encoding='utf-8')
with tarfile.open(archive) as tf:
    root = (ROOT / 'vendor').resolve()
    for member in tf.getmembers():
        target = (root / member.name).resolve()
        if root not in target.parents or member.issym() or member.islnk():
            raise ValueError('Unsafe archive member: ' + member.name)
    tf.extractall(root)
print(manifest.read_text(), flush=True)
