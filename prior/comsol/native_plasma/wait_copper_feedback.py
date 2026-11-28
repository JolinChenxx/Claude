from pathlib import Path
import sys,time,subprocess
R=Path(__file__).resolve().parent;source,case=sys.argv[1:3]
while not (R/'results'/f'{source}_post_complete.json').exists():time.sleep(30)
subprocess.run([sys.executable,'-X','utf8',str(R/'run_copper_feedback.py'),case,source],check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'compare_copper_feedback.py'),'C201','C211',case],check=True)
