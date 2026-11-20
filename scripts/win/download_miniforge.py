"""T003: official installer + official digest, proxy local to this process."""
import datetime,hashlib,json,urllib.request,urllib.parse
from pathlib import Path

def main():
    opener=urllib.request.build_opener(urllib.request.ProxyHandler({'http':'http://127.0.0.1:7897','https':'http://127.0.0.1:7897'}))
    name='Miniforge3-Windows-x86_64.exe'
    target=Path('D:/sim_tools/installers')/name;target.parent.mkdir(parents=True,exist_ok=True)
    requested='https://github.com/conda-forge/miniforge/releases/latest/download/'+name
    # Resolve the release tag separately; unauthenticated API may be rate limited.
    with opener.open('https://github.com/conda-forge/miniforge/releases/latest',timeout=90) as response:
        version=response.geturl().rstrip('/').rsplit('/',1)[-1]
    resolved='https://github.com/conda-forge/miniforge/releases/download/'+version+'/'+name
    checksum_url=resolved.rsplit('/',1)[0]+'/Miniforge3-'+version+'-Windows-x86_64.exe.sha256'
    with opener.open(checksum_url,timeout=90) as response:official=response.read().decode()
    expected=official.split()[0].lower()
    print('Official version',version,flush=True)
    with opener.open(resolved,timeout=120) as response,target.with_suffix('.part').open('wb') as out:
        while block:=response.read(1024*1024):out.write(block)
    partial=target.with_suffix('.part');actual=hashlib.sha256(partial.read_bytes()).hexdigest()
    if actual!=expected:raise RuntimeError('Official SHA256 mismatch')
    partial.replace(target)
    evidence=dict(downloaded_at=datetime.datetime.now(datetime.timezone.utc).isoformat(),
        requested_url=requested,resolved_url=resolved,version=version,bytes=target.stat().st_size,
        sha256=actual,official_sha256=expected,official_hash_verified=True,path=str(target))
    evidence['checksum_url']=checksum_url
    output=Path('results/T003');output.mkdir(parents=True,exist_ok=True)
    (output/'installer.json').write_text(json.dumps(evidence,indent=2)+'\n',encoding='utf-8')
    (output/'installer-official.sha256.txt').write_bytes(official.encode('utf-8'))
    print(json.dumps(evidence),flush=True)

if __name__=='__main__':main()
