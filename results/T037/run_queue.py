"""T037: sequential replay of four admitted imaging commands, offset only."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
from datetime import datetime
import psutil

REPO = Path(__file__).resolve().parents[2]
FROZEN = Path('D:/sim_tools/T037-runtime-bcdfd02')
JOB = Path('D:/sim_runs/T037')
SLOTS = [('T031', 'high_v1rs', 'it2'), ('T031', 'low_v1rs', 'it1'),
         ('T026', 'high_thin_G0', 'it2'), ('T026', 'low_thin_G0', 'it1')]


def sha(p):
    h = hashlib.sha256()
    with Path(p).open('rb') as f:
        for block in iter(lambda: f.read(1024*1024), b''):
            h.update(block)
    return h.hexdigest()


def commands(batch, member, iteration):
    receipt = json.loads((REPO/'results'/batch/member/iteration/'priority_delivery.json').read_text())
    raw = Path('D:/sim_runs')/batch/member/iteration
    out = raw/'image_v2_t28'
    assert not out.exists(), str(out)
    for p, digest in receipt['input_sha256'].items():
        assert sha(p) == digest, p
    image = receipt['commands']['image_v2']['command'].copy()
    peak = receipt['commands']['peak_lift']['command'].copy()
    assert Path(image[3]) == raw and Path(peak[2]) == raw/'image_v2'
    assert image[image.index('--workers')+1] == ('8' if batch=='T031' else '24')
    tables = Path(image[image.index('--tables')+1]); manifest = json.loads((tables/'manifest.json').read_text())
    for f in ('eos.bin','opt.bin','pop.bin'):
        assert sha(tables/f) == manifest['files'][f]['sha256']
    image[image.index('--out')+1] = str(out)
    image += ['--t-offset-ns','28']; peak[2] = str(out); peak[peak.index('--out')+1] = str(out/'peak_lift.csv')
    return out, image, peak


def main(check):
    assert not os.environ.get('LIPSIM_OVERRIDES')
    assert sha(FROZEN/'src/lipsim/image/run.py') == '9cbfed2123604399dc54b9ae845c28beee8fb5021c5d4b4d1bebcd0ba63057da'
    work = [commands(*slot) for slot in SLOTS]
    if check:
        for _,image,peak in work: print(image,peak)
        return
    env = dict(os.environ, PYTHONPATH=str(FROZEN/'src'), PYTHONDONTWRITEBYTECODE='1', PYTHONUTF8='1', OMP_NUM_THREADS='1', MKL_NUM_THREADS='1', OPENBLAS_NUM_THREADS='1', NUMEXPR_NUM_THREADS='1'); psutil.Process().nice(psutil.BELOW_NORMAL_PRIORITY_CLASS)
    with (JOB/'stages.jsonl').open('x', encoding='utf-8') as log:
        for slot,(out,img,peak) in zip(SLOTS,work):
            assert not out.exists(), str(out)
            for name,cmd in [('image',img),('peak_lift',peak)]:
                record=dict(member=slot[1],stage=name,command=cmd,started=datetime.now().astimezone().isoformat()); print(record,flush=True)
                with (JOB/f'{slot[1]}_{name}.log').open('xb') as f:
                    rc=subprocess.run(cmd,cwd=FROZEN,env=env,stdout=f,stderr=subprocess.STDOUT).returncode
                record.update(returncode=rc,finished=datetime.now().astimezone().isoformat()); print(json.dumps(record),file=log,flush=True)
                if rc: raise SystemExit(rc)
                if name=='image': assert json.loads((out/'image_summary.json').read_text())['t_offset_ns']==28
                else: assert (out/'peak_lift.csv').is_file()


if __name__=='__main__': main(sys.argv[1:]==['--check-only'])
