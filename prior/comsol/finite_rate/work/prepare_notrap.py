import sys,subprocess
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from comsol_driver import *
subprocess.run(runtime.compile_command(ROOT/'work/R2NoTrap.java'),cwd=ROOT/'work',env=runtime.environment(),check=True)
java('R2NoTrap',[ROOT/'models/R2_COUPLED_Q_ready.mph',ROOT/'models/R2_COUPLED_NT_ready.mph'],ROOT/'logs/R2_COUPLED_NT_build.log')
