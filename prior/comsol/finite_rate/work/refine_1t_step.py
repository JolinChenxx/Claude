import sys,subprocess
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from comsol_driver import *
subprocess.run(runtime.compile_command(ROOT/'work/R2SetStep.java'),cwd=ROOT/'work',env=runtime.environment(),check=True)
ready=ROOT/'models/R2_CR1T_R_ready.mph'
java('R2SetStep',[ready,ready,'1'],ROOT/'logs/R2_CR1T_R_step.log')
