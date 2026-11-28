"""Read GitHub Actions evidence using Git's existing credential helper.

Never print/store the credential. No messages, dispatches or remote mutations.
"""
import argparse, datetime, json, os, subprocess, urllib.request
from pathlib import Path

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--task',required=True);ap.add_argument('--sha',required=True)
    a=ap.parse_args()
    git=Path(os.environ['LOCALAPPDATA'])/'CodexTaskTools/MinGit-2.56.1/cmd/git.exe'
    env=os.environ.copy();env['GIT_TERMINAL_PROMPT']='0'
    r=subprocess.run([str(git),'credential','fill'],input='protocol=https\nhost=github.com\n\n',
        text=True,capture_output=True,env=env,timeout=30,check=True)
    fields=dict(x.split('=',1) for x in r.stdout.splitlines() if '=' in x)
    req=urllib.request.Request('https://api.github.com/repos/JolinChenxx/Claude/actions/workflows/handoff.yml/runs?branch=ws&per_page=30',
        headers={'Authorization':'Bearer '+fields['password'],'Accept':'application/vnd.github+json','User-Agent':'Codex-workstation'})
    opener=urllib.request.build_opener(urllib.request.ProxyHandler({'https':'http://127.0.0.1:7897'}))
    with opener.open(req,timeout=60) as response:data=json.load(response)
    runs=[{k:x.get(k) for k in ['id','head_sha','status','conclusion','html_url','created_at']} for x in data['workflow_runs'] if x['head_sha']==a.sha]
    out={'checked_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'commit':a.sha,'runs':runs}
    target=Path('results')/a.task/'handoff-actions.json';target.parent.mkdir(parents=True,exist_ok=True)
    target.write_text(json.dumps(out,indent=2)+'\n',encoding='utf-8');print(json.dumps(out))

if __name__=='__main__':main()
