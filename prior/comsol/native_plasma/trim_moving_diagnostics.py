"""Remove the failed M121 endpoint from exported diagnostic histories.

The MPH itself is trimmed by TrimSavedSolution, which preserves saved vectors.
"""
from pathlib import Path
import csv,subprocess,sys
R=Path(__file__).resolve().parent
for suffix in ['_balances.csv','_history.csv','_mechanism.csv','_moving_work.csv','_mesh_audit.csv']:
    src=R/'results'/('M121'+suffix)
    with src.open() as f:rows=list(csv.reader(f))
    valid=[rows[0]]+[r for r in rows[1:] if float(r[0])<=89e-9+1e-16]
    with (R/'results'/('M121_valid_to89ns'+suffix)).open('w',newline='') as f:csv.writer(f).writerows(valid)
subprocess.run([sys.executable,'-X','utf8',str(R/'summarize_moving_round2.py'),'M121_valid_to89ns'],check=True)
