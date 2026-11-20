"""Build pinned PLUTO with local problem overrides, using available MinGW/MS-MPI."""
from pathlib import Path
import os,re,subprocess,shutil,argparse,json
ROOT=Path(__file__).resolve().parents[1]
ap=argparse.ArgumentParser();ap.add_argument('--serial',action='store_true');args=ap.parse_args()
gcc=Path('C:/Program Files/ANSYS Inc/v261/SCADE Rapid Prototyper/contrib/Msys64/mingw64/bin')
mpi=Path('C:/Program Files/ANSYS Inc/v261/AnsysEM/common/fluent_mpi/multiport/mpi/win64/ms')
sdk=ROOT/'vendor/msmpi'
if not (sdk/'include/mpi.h').exists():
    shutil.copytree(mpi/'include',sdk/'include',dirs_exist_ok=True)
    (sdk/'lib').mkdir(parents=True,exist_ok=True)
    shutil.copy2(mpi/'lib/msmpi.lib',sdk/'lib/msmpi.lib')
template=(ROOT/'vendor/PLUTO/Src/Templates/makefile').read_text()
tc=(ROOT/'vendor/PLUTO/Src/Thermal_Conduction/tc_flux.c').read_text()
tc=tc.replace('#include "pluto.h"','#include "pluto.h"\n#include "cuair.h"')
tc=tc.replace('g1 = g_gamma - 1.0','g1 = 0.0')
tc=tc.replace('TC_kappa(vi, x1, x2, x3, &kpar, &knor, &phi);',
              'TC_kappa(vi, x1, x2, x3, &kpar, &knor, &phi);\n    g1=1.e6/cu_cv(vi);')
(ROOT/'solver/tc_flux.c').write_text(tc)
par=(ROOT/'vendor/PLUTO/Src/parabolic_update.c').read_text()
old='TOT_LOOP(k,j,i) d->Tc[k][j][i] = d->Vc[PRS][k][j][i]/d->Vc[RHO][k][j][i];'
assert old in par
par=par.replace(old,'TOT_LOOP(k,j,i) { double vv[NVAR]; int nn; for(nn=0;nn<NVAR;nn++) vv[nn]=d->Vc[nn][k][j][i]; GetPV_Temperature(vv,&d->Tc[k][j][i]); }')
(ROOT/'solver/parabolic_update.c').write_text(par)
main=(ROOT/'vendor/PLUTO/Src/main.c').read_text()
main=main.replace('  int    nv, idim, err;','  setvbuf(stdout,NULL,_IONBF,0);\n  setvbuf(stderr,NULL,_IONBF,0);\n  int    nv, idim, err;',1)
(ROOT/'solver/main.c').write_text(main)
template=re.sub(r'^ARCH\s*=.*$', 'ARCH = Windows.gcc.defs',template,flags=re.M)
template=re.sub(r'^PLUTO_DIR\s*=.*$','PLUTO_DIR = ../vendor/PLUTO',template,flags=re.M)
template=re.sub(r'^VPATH\s*=.*$','VPATH = .:$(SRC)/New:$(SRC):$(SRC)/Time_Stepping:$(SRC)/States',template,flags=re.M)
extra='''
include $(SRC)/HD/makefile
include $(SRC)/EOS/PVTE/makefile
include $(SRC)/Thermal_Conduction/makefile
OBJ += plm_states.o rk_step.o update_stage.o parabolic_update.o windows_compat.o
HEADERS += cuair.h
'''
template=template.replace('#   Additional_object_files_here', '#   Additional_object_files_here')
template=template.replace('pluto: $(OBJ)',extra+'\npluto: $(OBJ)')
# Native mingw make accepts semicolon-separated VPATH; colon conflicts with drive syntax.
template=template.replace('VPATH = .:$(SRC)/New:$(SRC):$(SRC)/Time_Stepping:$(SRC)/States',
                          'VPATH = .;$(SRC)/New;$(SRC);$(SRC)/Time_Stepping;$(SRC)/States')
# Included module VPATH colon paths must be normalized for native Windows GNU make.
for p in (ROOT/'vendor/PLUTO/Src').rglob('makefile'):
    s=p.read_text(errors='replace');s2=re.sub(r'(VPATH\s*\+=.*)',lambda m:m[0].replace(':',';'),s)
    if s2!=s:p.write_text(s2)
(ROOT/'solver/makefile').write_text(template)
flags='-c -O3 -std=c11 -Wno-implicit-function-declaration -DMSMPI_NO_SAL -include windows_compat.h'
parallel=not args.serial
if parallel:flags+=' -I../vendor/msmpi/include -I../vendor/msmpi/include/x64'
(ROOT/'vendor/PLUTO/Config/Windows.gcc.defs').write_text(
    'CC = gcc\nCFLAGS = '+flags+'\nLDFLAGS = -lm '+('../vendor/msmpi/lib/msmpi.lib' if parallel else '')+
    '\nPARALLEL = '+('TRUE' if parallel else 'FALSE')+'\nUSE_HDF5 = FALSE\nUSE_PNG = FALSE\n')
env=os.environ.copy();env['PATH']=str(gcc)+os.pathsep+env['PATH']
env['OMP_NUM_THREADS']='1';env['OPENBLAS_NUM_THREADS']='1'
with (ROOT/'logs/build.log').open('w') as log:
    proc=subprocess.run([str(gcc/'mingw32-make.exe'),'-j','16'],cwd=ROOT/'solver',env=env,stdout=log,stderr=subprocess.STDOUT)
print('build exit',proc.returncode)
if proc.returncode:
    logtext=(ROOT/'logs/build.log').read_text(errors='replace')
    print('\n'.join(x for x in logtext.splitlines() if any(t in x for t in ['error:', 'undefined reference','Error ','No rule']))[-5000:]);raise SystemExit(proc.returncode)
(ROOT/'state/build.json').write_text(json.dumps(dict(compiler=str(gcc/'gcc.exe'),parallel=parallel,
    executable=str(ROOT/'solver/pluto.exe')),indent=2))
print(ROOT/'solver/pluto.exe')
