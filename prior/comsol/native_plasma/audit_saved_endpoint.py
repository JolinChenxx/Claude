"""Audit the final accepted state after a named batch has saved normally."""
from pathlib import Path
import sys, os, subprocess, time
R=Path(__file__).resolve().parent
case=sys.argv[1]
env=os.environ.copy();env.update(ROUTE1_THREADS='8',ROUTE1_LOG_TAG=case+'_endpoint')
while True:
    p=R/'logs'/f'{case}_solver.log'
    s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if '总时间:' in s or 'Total time:' in s:break
    time.sleep(10)
for cls,suffix,args in [('NodeStiffnessAudit','_endpoint_nodes.csv',[]),('ChargeRateAudit','_charge_rates_order2.csv',['2']),('ExportBalances','_balances.csv',['trace'])]:
    subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java',cls,str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}{suffix}'),*args],env=env,check=True)
print('ENDPOINT_AUDITS_COMPLETE',case,flush=True)
