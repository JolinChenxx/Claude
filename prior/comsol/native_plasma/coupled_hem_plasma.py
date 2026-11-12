"""Conservative material predictor with native COMSOL Plasma reaction substeps.

First bulk-kinetic diagnostic: common advection, no electric drift, no thermionic
wall/sheath, no electron diffusion. Therefore not equivalent to spatial baseline.
All heavy reaction rates and electronic energy exchanges run in native Plasma.
"""
from pathlib import Path
import os,sys,time,json
from concurrent.futures import ThreadPoolExecutor
import numpy as np
from numba import njit,prange,set_num_threads
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from axisymmetric_hem_fv import fields,step,stable_dt
from hem_warm_thermo import fields_warm
NA=6.02214076e23;KB=1.380649e-23;EV=1.602176634e-19
MASS=np.array([.0280134,.0319988,.0140067,.0159994,.0280134,.0319988,.0140067,.0159994,.0159994,.0319988]+[.063546]*8)/NA
ENERGY=np.array([0,0,4.88,2.58,15.58,12.06,19.414,16.198,1.119,-.45,0,1.388948,1.6422256,3.7858977,3.816692,6.1911751,6.1920252,7.72638])*EV
CV=np.array([2.5,2.5,1.5,1.5,2.5,2.5,1.5,1.5,1.5,2.5]+[1.5]*8)*KB
MULTICHARGE=os.environ.get('ROUTE1_MULTICHARGE')=='1'
HEAVY=np.r_[np.arange(18),np.arange(20,23)].astype(int) if MULTICHARGE else np.arange(18)
CU_INDEX=HEAVY[10:]
if MULTICHARGE:
 MASS=np.r_[MASS,np.full(3,MASS[10])];ENERGY=np.r_[ENERGY,np.array([28.01877,64.85977,122.23977])*EV];CV=np.r_[CV,np.full(3,1.5*KB)]
NOUTPUT=19+len(HEAVY)
PHASE_FEED=os.environ.get('ROUTE1_PHASE_FEED','ground_neutral')
EOS_INVERSION=os.environ.get('ROUTE1_EOS_INVERSION','original')
PHASE_FLASH=os.environ.get('ROUTE1_PHASE_FLASH','sequential')
REACTION_STEP_NS=float(os.environ.get('ROUTE1_REACTION_STEP_NS','2'))
TRACE_PROJECTION_TOL=1e-4 # Same relative tolerance as the native collision solve.
dependent_n2_corrections=0
max_n2_nuclei_fraction=0.
trace_charge_corrections=0
max_trace_charge_fraction=0.
neutral_molecule_corrections=0
max_molecule_nuclei_fraction=0.

def repair_dependent_n2(S):
 # COMSOL obtains the dependent N2 fraction as 1-sum(w). In Cu-rich gas
 # this subtraction can produce a small negative exhausted N2 population.
 # Return those N nuclei from atomic N: exact mass/nuclei/charge conservation.
 # Total material energy is unchanged; the EOS inversion handles the tiny
 # change in chemical energy. Reject a correction larger than1% of N nuclei;
 # the largest actual correction is exported, not hidden behind this ceiling.
 global dependent_n2_corrections,max_n2_nuclei_fraction,trace_charge_corrections,max_trace_charge_fraction
 global neutral_molecule_corrections,max_molecule_nuclei_fraction
 amount=np.maximum(-S[...,0],0.)
 if np.any(amount):
  fraction=2*amount/np.maximum(S[...,2]+2*np.maximum(S[...,0],0)+2*S[...,4]+S[...,6],1.)
  worst=float(fraction.max())
  if worst>1e-2 or np.any(S[...,2]<2*amount):raise RuntimeError(f'Dependent N2 negativity exceeds conservative projection: {worst}')
  S[...,0]+=amount;S[...,2]-=2*amount
  dependent_n2_corrections+=int(np.count_nonzero(amount));max_n2_nuclei_fraction=max(max_n2_nuclei_fraction,worst)
 # Hot copper-rich cells may exhaust neutral O2 as well. Borrow its two
 # oxygen nuclei from O, rather than requiring an exhausted O2 donor later.
 amount=np.maximum(-S[...,1],0.)
 if np.any(amount):
  totalO=2*np.maximum(S[...,1],0)+S[...,3]+2*S[...,5]+S[...,7]+S[...,8]+2*S[...,9]
  fraction=2*amount/np.maximum(totalO,1.)
  if fraction.max()>1e-2 or np.any(S[...,3]<2*amount):raise RuntimeError('Neutral O2 rounding exceeds conservative nuclei projection')
  S[...,1]+=amount;S[...,3]-=2*amount
  neutral_molecule_corrections+=int(np.count_nonzero(amount));max_molecule_nuclei_fraction=max(max_molecule_nuclei_fraction,float(fraction.max()))
 for ion,neutral,charge in [(4,0,1),(5,1,1),(6,2,1),(7,3,1),(8,3,-1),(9,1,-1),(17,10,1)]+([(20,10,2),(21,10,3),(22,10,4)] if MULTICHARGE else []):
  amount=np.maximum(-S[...,ion],0.)
  if not np.any(amount):continue
  fraction=abs(charge)*amount/np.maximum(S[...,18],1.)
  if fraction.max()>TRACE_PROJECTION_TOL:raise RuntimeError(f'Native trace ion negativity exceeds native relative tolerance: {fraction.max():.6g}')
  direct=np.minimum(np.maximum(S[...,neutral],0),amount);remaining=amount-direct
  if np.any(remaining):
   if neutral not in (0,1):raise RuntimeError('Trace ion repair has insufficient neutral donor')
   atom=2 if neutral==0 else 3
   if np.any(S[...,atom]<2*remaining):raise RuntimeError('Trace molecular ion repair has insufficient atomic donor')
   S[...,atom]-=2*remaining
  # Preserve nuclei and net charge. Electron mean energy is preserved; the
  # corresponding energy repartition is taken from fixed total material energy.
  oldne=S[...,18].copy();S[...,ion]+=amount;S[...,neutral]-=direct;S[...,18]+=charge*amount
  S[...,19]*=S[...,18]/np.maximum(oldne,1e-100)
  trace_charge_corrections+=int(np.count_nonzero(amount));max_trace_charge_fraction=max(max_trace_charge_fraction,float(fraction.max()))
 return S

@njit(parallel=True)
def advect(S,Uold,Unew,Fr,Fz,volume,re,dz,dt):
 nr,nz,nq=S.shape;out=S.copy()
 for i in prange(nr):
  az=np.pi*(re[i+1]**2-re[i]**2)
  for k in range(nz):
   rho=Uold[i,k,0]+Uold[i,k,1]
   for q in range(nq):
    change=0.
    for side in range(4):
     carrier=0 if 10<=q<18 or q>=20 else 1 if q<10 else 2
     if side<2:
      face=i+side;mass=Fr[face,k,carrier] if carrier<2 else Fr[face,k,0]+Fr[face,k,1];donor=max(0,min(nr-1,face-1 if mass>=0 else face));den=Uold[donor,k,carrier] if carrier<2 else Uold[donor,k,0]+Uold[donor,k,1];y=S[donor,k,q]/max(den,1e-100);area=2*np.pi*re[face]*dz[k]
     else:
      face=k+side-2;mass=Fz[i,face,carrier] if carrier<2 else Fz[i,face,0]+Fz[i,face,1];donor=max(0,min(nz-1,face-1 if mass>=0 else face));den=Uold[i,donor,carrier] if carrier<2 else Uold[i,donor,0]+Uold[i,donor,1];y=S[i,donor,q]/max(den,1e-100);area=az
     change+=(1 if side==0 or side==2 else -1)*area*mass*y
    out[i,k,q]+=dt*change/volume[i,k]
 return out

def initial(U,W):
 S=np.zeros((*U.shape[:2],23 if MULTICHARGE else 20));air=U[:,:,1]/(.79*.0280134+.21*.0319988)*NA
 S[:,:,0]=air*.79;S[:,:,1]=air*.21;S[:,:,10]=U[:,:,0]*W[:,:,5]/MASS[10]
 seed=1e9*np.maximum(W[:,:,6],0);S[:,:,4]=np.minimum(seed,S[:,:,0]*1e-8);S[:,:,0]-=S[:,:,4];S[:,:,18]=S[:,:,4];S[:,:,19]=1.5*S[:,:,18]*KB*293.15/EV
 return S

def reconcile(S,U,W):
 # Material phase change inserts neutral Cu or condenses gas species together.
 want=U[:,:,0]*W[:,:,5]/MASS[10];got=S[:,:,:18][:,:,10:].sum(2)
 shrink=np.minimum(1,want/np.maximum(got,1e-100));oldions=S[:,:,17].copy();S[:,:,10:18]*=shrink[:,:,None]
 # Condensing singly charged Cu takes its accompanying bulk electron.
 removed=oldions-S[:,:,17];fraction=np.maximum(0,1-removed/np.maximum(S[:,:,18],1e-100));S[:,:,18]*=fraction;S[:,:,19]*=fraction
 born=np.maximum(want-got,0)
 if PHASE_FEED=='thermal_neutral':
  # Neutral internal states equilibrated at material phase release. No ions or
  # electrons are inserted. Existing vapor retains its native non-LTE populations.
  # Chemical energy is included by the next total-energy EOS inversion.
  weights=np.array([2,6,4,2,4,4,6])*np.exp(-ENERGY[10:17]/(KB*np.maximum(W[:,:,0,None],1.)))
  S[:,:,10:17]+=born[:,:,None]*weights/weights.sum(2)[:,:,None]
 else:S[:,:,10]+=born
 # Air nuclei advect with material air, matching HLLC material flux to roundoff.
 target=U[:,:,1];got=(S[:,:,:10]*MASS[:10]).sum(2);scale=target/np.maximum(got,1e-100);S[:,:,:10]*=scale[:,:,None]
 return S

def thermo_coupled(U,S,lt,lr,tab,W):
 chem=(S[:,:,:18]*ENERGY).sum(2)+S[:,:,19]*EV
 base=U[:,:,1]*717.5+1.5*KB*U[:,:,0]*W[:,:,5]/MASS[10]
 dcv=(S[:,:,:18]*CV).sum(2)-base
 # Algebraic nuclear EOS inversion; retain total energy as conserved state.
 for _ in range(5):
  nuclear=U.copy();nuclear[:,:,4]-=chem+dcv*W[:,:,0]
  nxt=fields_warm(nuclear,lt,lr,tab,W) if EOS_INVERSION=='warm' else fields(nuclear,lt,lr,tab)
  if np.max(abs(nxt[:,:,0]-W[:,:,0])/np.maximum(W[:,:,0],1))<1e-5:W=nxt;break
  W=nxt
 baseline_n=U[:,:,1]/(.79*.0280134+.21*.0319988)*NA+U[:,:,0]*W[:,:,5]/MASS[10]
 W[:,:,1]+=(2*S[:,:,19]*EV/3+((S[:,:,:18]).sum(2)-baseline_n)*KB*W[:,:,0])
 return W

def update_thermo(U,S,lt,lr,tab,W,phase=False):
 if MULTICHARGE and PHASE_FLASH!='coupled':raise ValueError('Multicharge requires the coupled phase/energy projection')
 if PHASE_FLASH=='coupled':
  from hem_reactive_flash import reactive_flash
  W,S,res=reactive_flash(U,S,W,lt,lr,tab,ENERGY,CV,PHASE_FEED=='thermal_neutral')
  scale=np.maximum(abs(U[:,:,4]),(U[:,:,0]+U[:,:,1])*1e4)
  if not np.all(np.isfinite(res)) or np.max(abs(res)/scale)>1e-6:
   folder=R/'results'/sys.argv[1]
   np.savez_compressed(folder/'rejected_flash_state.npz',U=U,S=S,W=W,residual=res)
   raise RuntimeError(f'Coupled energy/phase flash failed: {np.count_nonzero(~np.isfinite(res))} cells outside EOS range; rejected state retained')
  return W,S
 W=thermo_coupled(U,S,lt,lr,tab,W)
 return W,reconcile(S,U,W) if phase else S

def publish_native_file(source,target):
 # Java briefly opens request.properties while polling. Windows can deny
 # atomic replacement during that read; retry that observed sharing conflict.
 deadline=time.monotonic()+5.
 while True:
  try:os.replace(source,target);return
  except PermissionError:
   if time.monotonic()>=deadline:raise
   time.sleep(.02)

def call_native(worker,S,U,W,dt,idx,number,save=False,repair=True):
 cfg=json.loads((worker/'worker_config.json').read_text()) if (worker/'worker_config.json').exists() else {}
 nslot=int(cfg.get('slots',os.environ.get('ROUTE1_REACTION_SLOTS','6144')));a=np.zeros((nslot,7+len(HEAVY)));a[:,0]=np.arange(nslot)
 if bool(cfg.get('multicharge',False))!=MULTICHARGE:raise ValueError('Native worker and material species layouts differ')
 # Cold inert padding; all physical cells retain their own native state.
 a[:,1]=1.;a[:,2]=293.15;a[:,3]=717.5;a[:,4]=1e9;a[:,5]=1e9*1.5*KB*293.15/EV;a[:,7]=1/MASS[0]-1e9;a[:,11]=1e9
 if cfg.get('dependent_species')=='Cu':
  a[:,7:]=0.;a[:,3]=1.5*KB/MASS[10];a[:,17]=1/MASS[10]-1e9;a[:,24]=1e9
 for q,(i,k) in enumerate(idx):
  ag=W[i,k,6];rho=(S[i,k,HEAVY]*MASS).sum()/ag
  base=U[i,k,1]*717.5+1.5*KB*U[i,k,0]*W[i,k,5]/MASS[10]
  cv=(W[i,k,7]+np.sum(S[i,k,HEAVY]*CV)-base)/ag
  a[q,1:7]=[rho,W[i,k,0],cv,max(S[i,k,18]/ag,1e-12),max(S[i,k,19]/ag,1e-14),0]
  a[q,7:]=np.maximum(S[i,k,HEAVY]/ag,1e-20)
 if len(idx)>nslot:raise RuntimeError(f'Active reaction cells {len(idx)} exceed slots {nslot}')
 np.savetxt(worker/'state.next',a,fmt='%.16e');publish_native_file(worker/'state.next',worker/'state.txt')
 # Resolve the actual electron birth timescale without inserting seed charge.
 startup=''
 if cfg.get('direct_input_startup'):
  birth=3e-16*a[:,15]*a[:,10]+3.3e-16*a[:,16]*a[:,10]
  active=birth>0
  first=min(dt/100,float(np.min(.1*a[active,4]/birth[active]))) if np.any(active) else dt/100
  startup=f'first_step_s={first:.17g}\n'
 ident=f'step{number}';(worker/'request.next').write_text(f'id={ident}\ndt_s={dt:.17g}\nsave={str(save).lower()}\n'+startup);publish_native_file(worker/'request.next',worker/'request.properties')
 done=worker/f'complete_{ident}.json';err=worker/f'error_{ident}.txt'
 while not done.exists():
  if err.exists():raise RuntimeError(err.read_text())
  time.sleep(2)
 meta=json.loads(done.read_text());d=np.fromfile(worker/f'output_{ident}.bin',dtype='>f8').reshape(meta['shape']);ex=meta['expressions'];rad=0.;maxchange=0.
 for q,(i,k) in enumerate(idx):
  ag=W[i,k,6];oldT=W[i,k,0];S[i,k,HEAVY]=d[19:,q]*ag;S[i,k,18]=d[1,q]*ag;S[i,k,19]=d[2,q]*ag
  # Total energy changes only by laser/radiative loss in the native operator.
  delta=d[5,q]*ag;U[i,k,4]+=delta;W[i,k,0]=d[0,q];maxchange=max(maxchange,abs(W[i,k,0]/oldT-1))
 if repair:S=repair_dependent_n2(S)
 return S,U,W,d,meta,maxchange

def advance_native_group(operator,S,U,W,dt,idx,positions,number,group,save,slots):
 # Separate worker processes solve disjoint physical cells. Each task owns its
 # arrays; main-thread scatter and conservative roundoff repair stay sequential.
 localS=S.copy();localU=U.copy();localW=W.copy()
 cfg=json.loads((operator/'worker_config.json').read_text()) if (operator/'worker_config.json').exists() else {}
 size=int(cfg.get('slots',slots));out=np.zeros((NOUTPUT,len(positions)));elapsed=0.;largest=0.
 for begin in range(0,len(positions),size):
  pick=positions[begin:begin+size];chosen=idx[pick]
  localS,localU,localW,block,meta,change=call_native(operator,localS,localU,localW,dt,chosen,f'{number}_{group}_{begin//size}',save=save,repair=False)
  out[:,begin:begin+len(pick)]=block[:,:len(pick)];elapsed+=meta['seconds'];largest=max(largest,change)
 chosen=idx[positions];ii,jj=chosen[:,0],chosen[:,1]
 return positions,localS[ii,jj].copy(),localU[ii,jj,4].copy(),localW[ii,jj,0].copy(),out,elapsed,largest

def advance_native_batch_list(operator,S,U,W,dt,idx,tasks,number,save,slots):
 # Keep each original batch intact; distribute consecutive costly batches
 # across identical workers instead of serializing them on the first worker.
 return [advance_native_group(operator,S,U,W,dt,idx,pick,number,f'pool{ig}b{ib}',save,slots) for ig,ib,pick in tasks]

def main():
 case=sys.argv[1];end=float(sys.argv[2]);worker=R/'results'/sys.argv[3];set_num_threads(int(os.environ.get('ROUTE1_MATERIAL_THREADS','16')))
 out=R/'results'/case;(out/'frames').mkdir(parents=True,exist_ok=True)
 # Recompute from unheated initial material. 2 ns reaction splitting is a
 # candidate resolution; compare 1 ns once useful exposures exist.
 g=np.load(R/'results/HF401/grid.npz');re=g['radial_edges_m'];ze=g['axial_edges_m'];rc=g['r_m'];zc=g['z_m'];dr=np.diff(re);dz=np.diff(ze);volume=g['cell_volume_m3']
 eos_name=os.environ.get('ROUTE1_EOS_DIR','Cu_HEM_20261110')
 d=np.load(R/'data'/eos_name/'eos.npz');ts=d['temperatures_K'];rs=d['densities_kg_m3'];tab=np.ascontiguousarray(d['table'][:,2:].reshape(len(ts),len(rs),7));lt=np.log10(ts);lr=np.log10(rs)
 saved=np.load(R/'results/HF401/frames/frame_0000.npz');U=saved['U'].copy();W=saved['W'].copy();S=initial(U,W);t=0.;history=[];nstep=0;ea=eo=mo=0.;start=time.time();nreact=0;alpha=np.zeros(U.shape[:2]);radiated=0.
 np.savez(out/'grid.npz',**{k:g[k] for k in g.files});m0=(U[:,:,0]*volume).sum();e0=(U[:,:,4]*volume).sum()
 (out/'configuration.json').write_text(json.dumps({'phase_feed':PHASE_FEED,'electron_seed_m3':1e9,'reaction_step_ns':REACTION_STEP_NS,'eos':eos_name,'eos_inversion':EOS_INVERSION,'phase_energy_projection':PHASE_FLASH,'multicharge':MULTICHARGE,'heavy_indices':HEAVY.tolist(),'Cu_reaction_worker':os.environ.get('ROUTE1_CU_REACTION_WORKER'),'second_air_worker':os.environ.get('ROUTE1_AIR_REACTION_WORKER'),'parallel_disjoint_reaction_groups':os.environ.get('ROUTE1_PARALLEL_REACTION_GROUPS')=='1','vacuum_heavy_number_floor_m3':1.,'note':'Thermal-neutral release is an internal-state equilibrium assumption for newly vaporized Cu only; no charge source. Total energy retained.'},indent=2))
 settings=json.loads((out/'configuration.json').read_text());settings['phase_charge_partners']=os.environ.get('ROUTE1_PHASE_CHARGE_PARTNERS')=='1';settings['current_absorption']=os.environ.get('ROUTE1_CURRENT_ABSORPTION')=='1';(out/'configuration.json').write_text(json.dumps(settings,indent=2))
 if len(sys.argv)>4:
  source=R/'results'/sys.argv[4];prior=json.loads((source/'status.json').read_text());saved=np.load(prior['frame'])
  U=saved['U'].copy();W=saved['W'].copy();S=saved['S'].copy();t=float(saved['time_s']);ea=prior['absorbed_J'];radiated=prior['radiated_J']
  mo=prior['mass_residual_kg']-(U[:,:,0]*volume).sum()+m0;eo=prior['energy_residual_J']-(U[:,:,4]*volume).sum()+e0+ea-radiated
  indices=saved['reaction_indices'];values=saved['reaction_outputs'];alpha[indices[:,0],indices[:,1]]=np.maximum(values[16],0)*W[indices[:,0],indices[:,1],6]
  S=repair_dependent_n2(S);W,S=update_thermo(U,S,lt,lr,tab,W)
  first=dict(prior,source=sys.argv[4]);history.append(first)
 settings=json.loads((out/'configuration.json').read_text());settings['plasma_wave_bound']=os.environ.get('ROUTE1_PLASMA_WAVE_BOUND')=='1';(out/'configuration.json').write_text(json.dumps(settings,indent=2))
 settings=json.loads((out/'configuration.json').read_text());settings['extra_hot_workers']=os.environ.get('ROUTE1_EXTRA_HOT_CU_WORKERS','');settings['material_threads']=int(os.environ.get('ROUTE1_MATERIAL_THREADS','16'));(out/'configuration.json').write_text(json.dumps(settings,indent=2))
 settings=json.loads((out/'configuration.json').read_text());settings['actual_electron_removal']=os.environ.get('ROUTE1_ACTUAL_ELECTRON_REMOVAL')=='1';(out/'configuration.json').write_text(json.dumps(settings,indent=2))
 rejected_advection=0
 schedule=os.environ.get('ROUTE1_REACTION_SCHEDULE','')
 if schedule:
  current=t*1e9;segments=[]
  for item in schedule.split(','):
   boundary,spacing=map(float,item.split(':'));boundary=min(boundary,end)
   if spacing<=0:raise ValueError('Reaction schedule spacing must be positive')
   if boundary<=current+1e-6:continue
   seg=np.arange(current+spacing,boundary+1e-6,spacing)
   if not len(seg) or seg[-1]<boundary-1e-6:seg=np.r_[seg,boundary]
   segments.extend(seg.tolist());current=boundary
   if current>=end-1e-6:break
  if current<end-1e-6:raise ValueError('Reaction schedule must cover requested end')
  stops=np.array(segments)*1e-9
 else:stops=np.arange(t*1e9+REACTION_STEP_NS,end+1e-6,REACTION_STEP_NS)*1e-9
 (out/'split_schedule.json').write_text(json.dumps(dict(schedule=schedule or f'{end}:{REACTION_STEP_NS}',hot_worker=os.environ.get('ROUTE1_HOT_CU_REACTION_WORKER'),second_hot_Cu_worker=os.environ.get('ROUTE1_SECOND_HOT_CU_WORKER'),hot_air_worker=os.environ.get('ROUTE1_HOT_AIR_REACTION_WORKER'),parallel_groups=os.environ.get('ROUTE1_PARALLEL_REACTION_GROUPS')=='1',scope='Numerical operator splitting; reaction equations and data unchanged'),indent=2))
 if not len(stops) or stops[-1]<end*1e-9-1e-16:stops=np.r_[stops,end*1e-9]
 batch_costs={}
 if len(sys.argv)>4:
  prior_cost=R/'results'/sys.argv[4]/'native_batch_costs.json'
  if prior_cost.exists():batch_costs=json.loads(prior_cost.read_text())
 for stop in stops:
  runtime_file=out/'execution.json'
  if runtime_file.exists():
   runtime=json.loads(runtime_file.read_text())
   if 'material_threads' in runtime:set_num_threads(int(runtime['material_threads']))
   if 'trace_air_worker' in runtime:os.environ['ROUTE1_TRACE_AIR_WORKER']=runtime['trace_air_worker']
  reaction_dt=stop-t
  while t<stop-1e-20:
   if os.environ.get('ROUTE1_CURRENT_ABSORPTION')=='1':
    from split_absorption_current import current_absorption
    alpha=current_absorption(S,W)
   dt=min(stable_dt(W,dr,dz),stop-t);old=U.copy();oldS=S.copy()
   divu=np.gradient(W[:,:,9],rc,axis=0)+W[:,:,9]/rc[:,None]+np.gradient(W[:,:,3],zc,axis=1)
   for attempt in range(24):
    U,da,de,dm,bad,Fr,Fz,Qe=step(old.copy(),W,rc,zc,dr,dz,re,volume,t,dt,.1,alpha)
    if bad:dt*=.5;rejected_advection+=1;continue
    S=advect(oldS,old,U,Fr,Fz,volume,re,dz,dt)
    if np.any(S<0):
     local=np.maximum.reduce([abs(oldS),abs(S),abs(np.roll(S,1,0)),abs(np.roll(S,-1,0)),abs(np.roll(S,1,1)),abs(np.roll(S,-1,1))]);relative=np.max(np.maximum(-S,0)/np.maximum(local,1))
     if relative>1e-12:
      dt*=.5;rejected_advection+=1;continue
    S[:,:,19]*=np.exp(-2/3*divu*dt);S[:,:,19]+=dt*Qe/EV
    S=np.maximum(S,0)
    try:
     nextW,nextS=update_thermo(U,S,lt,lr,tab,W,phase=True)
    except RuntimeError as error:
     if 'Coupled energy/phase flash failed' not in str(error):raise
     dt*=.5;rejected_advection+=1;continue
    W,S=nextW,nextS;break
   else:raise RuntimeError(f'Conservative material update has no positive thermodynamic state at {t*1e9}ns after24 step reductions')
   t+=dt;nstep+=1;ea+=da;eo+=de;mo+=dm
  # An effectively empty gas has no collision partners. Avoid normalizing
  # sub-particle roundoff into an artificial chemical mixture.
  idx=np.argwhere((W[:,:,6]>1e-6)&(S[:,:,HEAVY].sum(2)>W[:,:,6]*1.0));delta=0.;meta={};dout=np.zeros((NOUTPUT,len(idx)))
  if len(idx):
   nreact+=1;before=U[:,:,4].copy();slots=int(os.environ.get('ROUTE1_REACTION_SLOTS','6144'));elapsed=0.
   save_operator=abs(t*1e9/20-round(t*1e9/20))<1e-7
   groups=[(worker,np.arange(len(idx)))]
   if os.environ.get('ROUTE1_CU_REACTION_WORKER'):
    copper=(S[:,:,CU_INDEX]*MASS[10:]).sum(2);gas=(S[:,:,HEAVY]*MASS).sum(2)
    rich=copper[idx[:,0],idx[:,1]]>.5*gas[idx[:,0],idx[:,1]]
    groups=[(worker,np.flatnonzero(~rich)),(R/'results'/os.environ['ROUTE1_CU_REACTION_WORKER'],np.flatnonzero(rich))]
    if os.environ.get('ROUTE1_HOT_CU_REACTION_WORKER'):
     # Every physical cell still receives the same native reactions. Separate
     # hot cells so their stiff substeps do not set the steps for thousands of
     # colder independent cells. This threshold changes scheduling only.
     electronT=2/3*S[idx[:,0],idx[:,1],19]/np.maximum(S[idx[:,0],idx[:,1],18],1e-100)*EV/KB
     hot=rich&((W[idx[:,0],idx[:,1],0]>5000)|(electronT>5000))
     groups=[(worker,np.flatnonzero(~rich)),(R/'results'/os.environ['ROUTE1_CU_REACTION_WORKER'],np.flatnonzero(rich&~hot)),(R/'results'/os.environ['ROUTE1_HOT_CU_REACTION_WORKER'],np.flatnonzero(hot))]
     if os.environ.get('ROUTE1_SECOND_HOT_CU_WORKER'):
      first_hot,second_hot=np.array_split(groups[2][1],2)
      groups[2:]=[(groups[2][0],first_hot),(R/'results'/os.environ['ROUTE1_SECOND_HOT_CU_WORKER'],second_hot)]
     if os.environ.get('ROUTE1_EXTRA_HOT_CU_WORKERS'):
      operators=[groups[2][0],R/'results'/os.environ['ROUTE1_SECOND_HOT_CU_WORKER']]+[R/'results'/name for name in os.environ['ROUTE1_EXTRA_HOT_CU_WORKERS'].split(',')]
      groups[2:]=list(zip(operators,np.array_split(np.flatnonzero(hot),len(operators))))
     if os.environ.get('ROUTE1_HOT_AIR_REACTION_WORKER'):
      hot_air=(~rich)&((W[idx[:,0],idx[:,1],0]>5000)|(electronT>5000))
      groups[0]=(worker,np.flatnonzero((~rich)&(~hot_air)))
      groups.append((R/'results'/os.environ['ROUTE1_HOT_AIR_REACTION_WORKER'],np.flatnonzero(hot_air)))
   if os.environ.get('ROUTE1_TRACE_AIR_WORKER'):
    pick=groups[0][1];ii,jj=idx[pick,0],idx[pick,1]
    charged=S[ii,jj,4:10].sum(1)+S[ii,jj,17]
    if MULTICHARGE:charged+=(S[ii,jj,20:]*np.array([2,3,4])).sum(1)
    trace=(S[ii,jj,18]<1e-6*np.maximum(charged,1.))&(charged/np.maximum(W[ii,jj,6],1e-30)>1e12)
    groups[0]=(groups[0][0],pick[~trace]);groups.append((R/'results'/os.environ['ROUTE1_TRACE_AIR_WORKER'],pick[trace]))
   if os.environ.get('ROUTE1_AIR_REACTION_WORKER'):
    first,second=np.array_split(groups[0][1],2)
    groups=[(groups[0][0],first),(R/'results'/os.environ['ROUTE1_AIR_REACTION_WORKER'],second)]+groups[1:]
   if os.environ.get('ROUTE1_PARALLEL_REACTION_GROUPS')=='1' and len(groups)>1:
    with ThreadPoolExecutor(max_workers=len(groups)) as pool:
     hot_names=[os.environ.get('ROUTE1_HOT_CU_REACTION_WORKER'),os.environ.get('ROUTE1_SECOND_HOT_CU_WORKER')]+os.environ.get('ROUTE1_EXTRA_HOT_CU_WORKERS','').split(',')
     pooled=[(ig,operator,positions) for ig,(operator,positions) in enumerate(groups) if operator.name in hot_names] if os.environ.get('ROUTE1_BALANCED_HOT_BATCHES')=='1' else []
     pooled_ids={ig for ig,_,_ in pooled}
     pending=[pool.submit(advance_native_group,operator,S,U,W,reaction_dt,idx,positions,f'{case}_{nreact:05d}',ig,save_operator,slots) for ig,(operator,positions) in enumerate(groups) if ig not in pooled_ids]
     batched=[]
     if pooled:
      tasks=[]
      for ig,operator,positions in pooled:
       size=int(json.loads((operator/'worker_config.json').read_text())['slots'])
       for ib,begin in enumerate(range(0,len(positions),size)):tasks.append((ig,ib,positions[begin:begin+size]))
      from native_batch_schedule import distribute
      assignments=distribute(tasks,pooled,batch_costs)
      for k,(_,operator,_) in enumerate(pooled):
       batched.append((pool.submit(advance_native_batch_list,operator,S,U,W,reaction_dt,idx,assignments[k],f'{case}_{nreact:05d}',save_operator,slots),assignments[k]))
     results=[task.result() for task in pending]
     for task,assigned in batched:
      completed=task.result();results.extend(completed)
      for (ig,ib,_),result in zip(assigned,completed):batch_costs[f'{ig}:{ib}']=result[5]
     if batched:(out/'native_batch_costs.json').write_text(json.dumps(batch_costs,indent=2))
    for pick,ss,ee,tt,block,seconds,change in results:
     chosen=idx[pick];ii,jj=chosen[:,0],chosen[:,1];S[ii,jj]=ss;U[ii,jj,4]=ee;W[ii,jj,0]=tt;dout[:,pick]=block;elapsed+=seconds;delta=max(delta,change)
    S=repair_dependent_n2(S)
   else:
    for ig,(operator,positions) in enumerate(groups):
     cfg=json.loads((operator/'worker_config.json').read_text()) if (operator/'worker_config.json').exists() else {}
     size=int(cfg.get('slots',slots))
     for begin in range(0,len(positions),size):
      pick=positions[begin:begin+size];chosen=idx[pick]
      S,U,W,block,part,change=call_native(operator,S,U,W,reaction_dt,chosen,f'{case}_{nreact:05d}_{ig}_{begin//size}',save=save_operator);dout[:,pick]=block[:,:len(chosen)];elapsed+=part['seconds'];delta=max(delta,change)
   meta['seconds']=elapsed;radiated+=float(((before-U[:,:,4])*volume).sum());W,S=update_thermo(U,S,lt,lr,tab,W)
   alpha.fill(0.)
   for q,(i,k) in enumerate(idx):alpha[i,k]=max(dout[16,q],0)*W[i,k,6]
  path=out/'frames'/f'frame_{len(history):04d}.npz';np.savez_compressed(path,U=U,W=W,S=S,time_s=t,reaction_indices=idx,reaction_outputs=dout[:,:len(idx)])
  report={'case':case,'saved_time_ns':t*1e9,'native_plasma_solved':bool(nreact),'model_kind':'bulk split-reaction diagnostic; no sheath/charge drift/electron diffusion','reaction_cells':len(idx),'reaction_seconds':meta.get('seconds',0),'max_reaction_T_fraction':delta,'Tmax_K':float(W[:,:,0].max()),'ne_max_bulk_m3':float(S[:,:,18].max()),'absorbed_J':ea,'seconds':time.time()-start,'frame':str(path),'mass_residual_kg':float((U[:,:,0]*volume).sum()-m0+mo)}
  report.update(radiated_J=radiated,energy_residual_J=float((U[:,:,4]*volume).sum()-e0-ea+eo+radiated),rejected_advection_steps=rejected_advection,dependent_n2_corrections=dependent_n2_corrections,max_n2_nuclei_fraction=max_n2_nuclei_fraction,phase_feed=PHASE_FEED,trace_charge_corrections=trace_charge_corrections,max_trace_charge_fraction=max_trace_charge_fraction)
  report.update(neutral_molecule_corrections=neutral_molecule_corrections,max_molecule_nuclei_fraction=max_molecule_nuclei_fraction,trace_projection_relative_tolerance=TRACE_PROJECTION_TOL)
  report['phase_energy_projection']=PHASE_FLASH
  report['multicharge']=MULTICHARGE
  report['eos_directory']=eos_name
  report['phase_charge_partners']=os.environ.get('ROUTE1_PHASE_CHARGE_PARTNERS')=='1'
  report['current_absorption']=os.environ.get('ROUTE1_CURRENT_ABSORPTION')=='1'
  report['plasma_wave_bound']=os.environ.get('ROUTE1_PLASMA_WAVE_BOUND')=='1'
  report['actual_electron_removal']=os.environ.get('ROUTE1_ACTUAL_ELECTRON_REMOVAL')=='1'
  report['trace_air_worker']=os.environ.get('ROUTE1_TRACE_AIR_WORKER')
  report['balanced_hot_batches']=os.environ.get('ROUTE1_BALANCED_HOT_BATCHES')=='1'
  charged_heavy=S[:,:,4:8].sum(2)-S[:,:,8:10].sum(2)+S[:,:,17]
  charge_inventory=S[:,:,4:10].sum(2)+S[:,:,17]+S[:,:,18]
  if MULTICHARGE:
   weighted=(S[:,:,20:]*np.array([2,3,4])).sum(2);charged_heavy+=weighted;charge_inventory+=weighted
  charge_error=charged_heavy-S[:,:,18]
  report['absolute_local_charge_C']=float((abs(charge_error)*volume).sum()*EV)
  report['absolute_charge_over_charged_inventory']=float((abs(charge_error)*volume).sum()/max((charge_inventory*volume).sum(),1e-100))
  history.append(report);(out/'history.json').write_text(json.dumps(history,indent=2));(out/'status.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
  if (out/'stop_requested.txt').exists():
   print('STOPPED_AT_SAVED_FRAME',case,t*1e9,flush=True);break
if __name__=='__main__':main()
