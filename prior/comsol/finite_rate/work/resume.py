import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from comsol_driver import *
source,out,end=sys.argv[1:4]
args=[ROOT/'models'/f'{source}.mph',ROOT/'models'/f'{out}_ready.mph',end]+sys.argv[4:]
java('R2Resume',args,ROOT/'logs'/f'{out}_resume.log')
