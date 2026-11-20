import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from comsol_driver import *
case=sys.argv[1]
java('R2Budget',[ROOT/'models'/f'{case}.mph',RES/case],ROOT/'logs'/f'{case}_budget.log')
