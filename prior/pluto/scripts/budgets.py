"""Join radiation budgets across checkpoint restarts without assuming conservation."""
import csv
import numpy as np
from common import read_json,atomic_json

def load_budget(folder):
    current=np.atleast_1d(np.genfromtxt(folder/'budget.csv',delimiter=',',names=True))
    meta=read_json(folder/'budget_resume.json')
    current['radiated_segment_uj']+=meta.get('radiation_offset_uj',0.)
    history=folder/'budget_history.npy'
    if history.exists():
        old=np.load(history)
        if len(current):old=old[old['time_ns']<current['time_ns'][0]-1e-8]
        current=np.concatenate([old,current])
    return current

def prepare_restart(folder):
    rows=[x.split() for x in (folder/'dbl.out').read_text().splitlines() if x.strip()]
    candidates=[x for x in rows if len(x)>6 and (folder/f'data.{int(x[0]):04d}.dbl').exists()]
    if not candidates:raise RuntimeError('No available double-precision checkpoint')
    row=candidates[-1];index=int(row[0]);t=float(row[1])
    data=load_budget(folder)
    offset=float(np.interp(t,data['time_ns'],data['radiated_segment_uj']))
    serial=read_json(folder/'budget_resume.json').get('restart_count',0)+1
    (folder/f'budget_before_restart_{serial}.csv').write_bytes((folder/'budget.csv').read_bytes())
    np.save(folder/'budget_history.npy',data[data['time_ns']<=t+1e-8])
    atomic_json(folder/'budget_resume.json',dict(checkpoint_index=index,time_ns=t,
        radiation_offset_uj=offset,restart_count=serial,
        method='Interpolation of measured cumulative radiation at checkpoint; at most one analysis interval.'))
    (folder/'budget.csv').write_text(','.join(data.dtype.names)+'\n')
    return index
