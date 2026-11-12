from pathlib import Path
import sys,os,time,subprocess
R=Path(__file__).resolve().parent;case=sys.argv[1];env=os.environ.copy();env.update(ROUTE1_THREADS='4',ROUTE1_LOG_TAG=case+'post')
while True:
 p=R/'logs'/f'{case}_solver.log';s=p.read_text(encoding='utf-8',errors='replace') if p.exists() else ''
 if '总时间:' in s or 'Total time:' in s:break
 time.sleep(30)
for args in [('run.py','java','ExportAxialHEM',str(R/'models'/f'{case}.mph'),str(R/'results'/case)),('plot_axial_hem.py',case)]:subprocess.run([sys.executable,'-X','utf8',str(R/args[0]),*args[1:]],env=env,check=True)
