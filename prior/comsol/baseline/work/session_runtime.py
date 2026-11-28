"""One persisted wall-clock window; historical solver accounting is retained."""
from pathlib import Path
import datetime,json,time,os
from contextlib import contextmanager
ROOT=Path(__file__).resolve().parents[1]
CONFIG=ROOT/'session_64.json'

def config():
    return json.loads(CONFIG.read_text(encoding='utf-8-sig'))

def solver_seconds_left():
    c=config()
    deadline=datetime.datetime.fromisoformat(c['solver_deadline_utc']).timestamp()
    return max(0.,deadline-time.time())

def segment_seconds_left(elapsed,used,budget):
    c=config()
    return min(float(os.environ.get('S4_MAX_SEGMENT_SECONDS',c.get('max_segment_seconds',2700)))-elapsed,
               budget['budget_seconds']-used-elapsed,solver_seconds_left())

@contextmanager
def coordinator_lock(name):
    lock=ROOT/'coordinator64.lock'
    fd=os.open(str(lock),os.O_CREAT|os.O_EXCL|os.O_WRONLY)
    try:
        os.write(fd,json.dumps({'pid':os.getpid(),'name':name,'started_epoch':time.time()}).encode())
    finally:os.close(fd)
    try:yield
    finally:lock.unlink(missing_ok=True)
