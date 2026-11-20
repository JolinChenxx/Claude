"""Fetch a pinned public ANEOS implementation for material-range diagnostics.

The legacy copper parameter set is marked obsolete by upstream. This fetch
does not adopt it as a validated material closure in the COMSOL model.
"""
from pathlib import Path
import urllib.request,json,concurrent.futures,hashlib
R=Path(__file__).resolve().parent
tree=json.loads((R/'data/round2_references/pyaneos_tree.json').read_text())
commit=tree['sha'];dest=R/'vendor/pyaneos_reference';dest.mkdir(parents=True,exist_ok=True)
paths=[x['path'] for x in tree['tree'] if x['path'].startswith('pyaneos/') and x['path'].endswith('.py')]
paths+=['README.md','LICENSE','CITATION.cff','docs/pyaneos-user-guide.md','docs/code/ANEOS.INPUT-format.md']
def fetch(path):
    url=f'https://raw.githubusercontent.com/ImpactsWiki/pyaneos/{commit}/{path}'
    target=dest/path;target.parent.mkdir(parents=True,exist_ok=True)
    for attempt in range(2):
        try:
            data=urllib.request.urlopen(url,timeout=25).read();target.write_bytes(data)
            return {'file':path,'url':url,'sha256':hashlib.sha256(data).hexdigest(),'bytes':len(data)}
        except Exception as e:
            if attempt==1:return {'file':path,'error':str(e)}
with concurrent.futures.ThreadPoolExecutor(4) as pool:records=list(pool.map(fetch,paths))
(dest/'source_manifest.json').write_text(json.dumps({'commit':commit,'files':records,'purpose':'legacy Cu EOS diagnostics only; not accepted for production'},indent=2),encoding='utf8')
print(json.dumps({'downloaded':sum('error' not in r for r in records),'errors':[r for r in records if 'error' in r]},indent=2))
