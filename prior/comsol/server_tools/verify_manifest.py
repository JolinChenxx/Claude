"""Verify every transfer payload file before running server setup."""
from pathlib import Path
import argparse, hashlib, json, sys
ROOT=Path(__file__).resolve().parents[1]
def verify():
    data=json.loads((ROOT/'MANIFEST_SHA256.json').read_text(encoding='utf8'));failed=[]
    for i,row in enumerate(data['files']):
        p=ROOT/row['path'];h=hashlib.sha256()
        if not p.is_file():failed.append([row['path'],'missing']);continue
        if p.stat().st_size!=row['bytes']:failed.append([row['path'],'size']);continue
        with p.open('rb') as f:
            for part in iter(lambda:f.read(8*1024*1024),b''):h.update(part)
        if h.hexdigest()!=row['sha256']:failed.append([row['path'],'sha256'])
        if i%100==0:print('VERIFIED',i,'/',len(data['files']),flush=True)
    out={'checked_files':len(data['files']),'failed':failed,'passed':not failed,'scope':'Transfer snapshot; run before compiling or relocating files.'}
    print(json.dumps(out,indent=2,ensure_ascii=False));return not failed
if __name__=='__main__':sys.exit(0 if verify() else 1)
