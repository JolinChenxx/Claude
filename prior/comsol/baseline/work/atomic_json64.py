"""Atomic JSON replacement resilient to transient Windows reader/AV locks."""
import json,os,time,uuid

def write_json(path,obj,timeout=15):
    temporary=path.with_name(path.name+'.'+str(os.getpid())+'.'+uuid.uuid4().hex+'.tmp')
    temporary.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
    deadline=time.monotonic()+timeout
    try:
        while True:
            try:
                os.replace(temporary,path)
                return
            except PermissionError:
                if time.monotonic()>=deadline:raise
                time.sleep(.1)
    finally:
        temporary.unlink(missing_ok=True)
