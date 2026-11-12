import concurrent.futures
import json
from pathlib import Path
import subprocess
import sys

ROOT=Path(__file__).resolve().parents[1]
SCRIPTS=ROOT.parents[0]/'vendor/nature_figure/scripts'

def inspect(pdf):
    output={}
    for tag,script,args in [
        ('text','audit_pdf_text.py',['--min-pt','5','--json']),
        ('collisions','audit_figure_collisions.py',['--json','--json-out',str(pdf.with_suffix('.collision-audit.json')),'--overlay-pdf',str(pdf.with_suffix('.collision-audit.pdf'))]),
    ]:
        proc=subprocess.run([sys.executable,'-X','utf8',str(SCRIPTS/script),str(pdf),*args],capture_output=True,text=True,encoding='utf8',errors='replace')
        output[tag]={'returncode':proc.returncode,'report':json.loads(proc.stdout) if proc.stdout.strip().startswith('{') else proc.stdout,'stderr':proc.stderr}
    return pdf.name,output

if __name__=='__main__':
    pattern=(sys.argv[1]+'*.pdf') if len(sys.argv)>1 else '*.pdf'
    files=[p for p in (ROOT/'figures').glob(pattern) if '.collision-audit.' not in p.name]
    with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:
        results=dict(pool.map(inspect,files))
    summary=('qa_'+sys.argv[1]+'.json') if len(sys.argv)>1 else 'qa_summary.json'
    (ROOT/'figures'/summary).write_text(json.dumps(results,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
    for name,reports in results.items():
        print(name,{tag:r['returncode'] for tag,r in reports.items()})
    sys.exit(1 if any(r['returncode'] for v in results.values() for r in v.values()) else 0)
