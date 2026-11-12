from pathlib import Path
import os,json,datetime as dt,tempfile
ROOT=Path(__file__).resolve().parents[1]
TZ=dt.timezone(dt.timedelta(hours=8))
def utcnow():return dt.datetime.now(dt.timezone.utc)
def read_json(path,default=None):
    try:return Path(path).read_text(encoding='utf-8') and json.loads(Path(path).read_text(encoding='utf-8'))
    except (OSError,json.JSONDecodeError):return {} if default is None else default
def atomic_json(path,value):
    path=Path(path);path.parent.mkdir(parents=True,exist_ok=True)
    fd,tmp=tempfile.mkstemp(prefix=path.name+'.',suffix='.tmp',dir=path.parent)
    with os.fdopen(fd,'w',encoding='utf-8') as f:json.dump(value,f,ensure_ascii=False,indent=2)
    os.replace(tmp,path)
def campaign():return read_json(ROOT/'state/campaign.json')
def deadline():return dt.datetime.fromisoformat(campaign()['deadline'])
def runtime_env():
    env=os.environ.copy()
    paths=['C:/Program Files/ANSYS Inc/v261/SCADE Rapid Prototyper/contrib/Msys64/mingw64/bin',
           'C:/Program Files/Microsoft MPI/Bin']
    env['PATH']=os.pathsep.join(paths+[env.get('PATH','')])
    for k in ['OMP_NUM_THREADS','OPENBLAS_NUM_THREADS','MKL_NUM_THREADS','NUMEXPR_NUM_THREADS']:
        env[k]='1'
    env['PYTHONUNBUFFERED']='1'
    return env

