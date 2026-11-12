"""Read contiguous saved-state chains without extrapolating model time."""
from pathlib import Path
import json
R=Path(__file__).resolve().parent
def read_history(case):
 h=json.loads((R/'results'/case/'history.json').read_text())
 if h and h[0].get('source'):
  old=read_history(h[0]['source']);start=h[0]['saved_time_ns']
  if abs(old[-1]['saved_time_ns']-start)>1e-6:raise ValueError('Noncontiguous split chain')
  h=[q for q in old if q['saved_time_ns']<start-1e-6]+h
 return h
