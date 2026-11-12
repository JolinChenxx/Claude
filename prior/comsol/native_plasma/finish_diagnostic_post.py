"""Finish budgets/optics after the accepted-step diagnostic has exported fields."""
from pathlib import Path
import sys,subprocess,time
R=Path(__file__).resolve().parent;case=sys.argv[1]
while not (R/'results'/f'{case}_surface.csv').exists():time.sleep(15)
# ExportNative writes the surface file before the binary/metadata on some builds;
# wait for its normal completion marker as well.
while True:
    p=R/'logs'/f'ExportNative_{case}_java.log';s=p.read_text(encoding='utf8',errors='replace') if p.exists() else ''
    if 'EXPORTED ' in s:break
    time.sleep(15)
with (R/'logs'/f'{case}_optical_round2.log').open('wb') as f:subprocess.Popen([sys.executable,'-X','utf8',str(R/'optical_round2.py'),case],stdout=f,stderr=subprocess.STDOUT)
subprocess.run([sys.executable,'-X','utf8',str(R/'run.py'),'java','MeshStateAudit',str(R/'models'/f'{case}.mph'),str(R/'results'/f'{case}_mesh_audit.csv')],check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'post_round2.py'),case,'ExportMechanism'],check=True)
