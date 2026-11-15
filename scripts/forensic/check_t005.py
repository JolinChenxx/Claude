"""Measure stock reference runs against the specified Sod/Sedov criteria."""
from pathlib import Path
import csv,json
import numpy as np
from scipy.optimize import brentq
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.ticker import FixedLocator, FuncFormatter, NullFormatter
REPO=Path(__file__).resolve().parents[2]
OUT=REPO/'results/T005'
RAW=Path('D:/sim_runs/T005')

def read_run(folder):
    lines=[s.split() for s in (folder/'grid.out').read_text().splitlines() if s.strip() and not s.startswith('#')]
    n=int(lines[0][0]); bounds=np.array([[float(x) for x in row[1:3]] for row in lines[1:n+1]])
    x=bounds.mean(1); series=[]
    for line in (folder/'dbl.out').read_text().splitlines():
        row=line.split(); idx=int(row[0]); t=float(row[1]); names=row[6:]
        data=np.fromfile(folder/f'data.{idx:04d}.dbl',dtype='<f8').reshape(len(names),-1)
        assert data.shape[1]==n, (folder,data.shape,n)
        series.append((t,{name:data[i] for i,name in enumerate(names)}))
    return x,bounds,series

def sod_exact(x,t):
    gamma=1.4;beta=(gamma-1)/(gamma+1); rL,pL,rR,pR=1.,1.,.125,.1
    aL=np.sqrt(gamma*pL/rL);aR=np.sqrt(gamma*pR/rR)
    def wave(p,r0,p0,a):
        if p>p0:return (p-p0)*np.sqrt(2/((gamma+1)*r0)/(p+beta*p0))
        return 2*a/(gamma-1)*((p/p0)**((gamma-1)/(2*gamma))-1)
    ps=brentq(lambda p:wave(p,rL,pL,aL)+wave(p,rR,pR,aR),1e-10,2)
    us=.5*(wave(ps,rR,pR,aR)-wave(ps,rL,pL,aL))
    astar=aL*(ps/pL)**((gamma-1)/(2*gamma))
    speed=aR*np.sqrt((gamma+1)/(2*gamma)*ps/pR+(gamma-1)/(2*gamma))
    xi=(x-.5)/t
    rho=np.full_like(x,rR);rho[xi<us]=rL*(ps/pL)**(1/gamma)
    rho[(xi>=us)&(xi<speed)]=rR*((ps/pR+beta)/(beta*ps/pR+1))
    rho[xi<=-aL]=rL
    fan=(xi>-aL)&(xi<us-astar)
    rho[fan]=rL*(2/(gamma+1)-(gamma-1)/(gamma+1)*xi[fan]/aL)**(2/(gamma-1))
    return rho,dict(p_star=ps,u_star=us,shock_speed=speed,shock_position=.5+speed*t)

def csv_write(path,rows):
    with path.open('w',newline='',encoding='utf-8') as f:
        wr=csv.DictWriter(f,fieldnames=list(rows[0]));wr.writeheader();wr.writerows(rows)

def main():
    execution=json.loads((OUT/'execution.json').read_text());checks=[];trajectories=[];profiles={};fig,axs=plt.subplots(1,2,figsize=(11,4.5))
    for problem in ('Sod','Sedov'):
        for mode in ('serial','mpi8'):
            name=problem+'_'+mode;x,bounds,series=read_run(RAW/name);profiles[name]=series[-1][1]['rho']
            final_t,final=series[-1]; rec=next(c for c in execution['cases'] if c.get('case')==name)
            result=dict(case=name,build_exit=rec['build_exit'],run_exit=rec['run_exit'],t_final=final_t,
                        n_snapshots=len(series),n_cells=len(x),build_seconds=rec['build_seconds'],run_seconds=rec['run_seconds'])
            rows=[]
            for t,data in series:
                for i in range(len(x)):
                    rows.append(dict(time=t,x=x[i],rho=data['rho'][i],pressure=data['prs'][i],velocity=data['vx1'][i]))
            csv_write(OUT/f'{name}_density_profiles.csv',rows)
            if problem=='Sod':
                exact,solution=sod_exact(x,final_t)
                # Largest falling pressure jump isolates the shock from the contact.
                idx=int(np.argmin(np.diff(final['prs'])/np.diff(x)))
                measured=.5*(x[idx]+x[idx+1]);dx=bounds[idx,1]-bounds[idx,0]
                error=abs(measured-solution['shock_position'])/dx
                result.update(exact=solution,measured_shock=measured,shock_error_cells=error,passed=bool(error<=1 and abs(final_t-.2)<1e-12 and rec['run_exit']==0))
                axs[0].plot(x,final['rho'],label=mode,ls='-' if mode=='serial' else '--')
                if mode=='serial':
                    axs[0].plot(x,exact,'k:',label='Exact Riemann solution')
                    csv_write(OUT/'Sod_exact_density.csv',[dict(x=float(a),rho_exact=float(b)) for a,b in zip(x,exact)])
            else:
                tr=[]
                for t,data in series:
                    if t<=0:continue
                    idx=int(np.argmin(np.diff(data['rho'])/np.diff(x)))
                    tr.append(dict(case=name,time=t,shock_radius=float(.5*(x[idx]+x[idx+1]))))
                exponent,intercept=np.polyfit(np.log([r['time'] for r in tr]),np.log([r['shock_radius'] for r in tr]),1)
                endpoint=np.log(tr[-1]['shock_radius']/tr[0]['shock_radius'])/np.log(tr[-1]['time']/tr[0]['time'])
                result.update(radius_power_exponent=float(exponent),endpoint_exponent=float(endpoint),fit_time_range=[tr[0]['time'],tr[-1]['time']],
                              passed=bool(.38<=exponent<=.42 and abs(final_t-.5)<1e-12 and rec['run_exit']==0))
                trajectories.extend(tr)
                axs[1].loglog([r['time'] for r in tr],[r['shock_radius'] for r in tr],'o-' if mode=='serial' else 'x--',label=f'{mode}: slope {exponent:.5f}',markersize=4)
                if mode=='serial':
                    times=np.array([r['time'] for r in tr]);guide=tr[-1]['shock_radius']*(times/times[-1])**.4
                    axs[1].loglog(times,guide,'k:',label='t^0.4 guide (anchored at final radius)')
            checks.append(result)
    csv_write(OUT/'Sedov_shock_trajectory.csv',trajectories)
    comparison={p:dict(max_abs_density_difference=float(np.max(np.abs(profiles[p+'_serial']-profiles[p+'_mpi8']))),
                       relative_L1_density_difference=float(np.sum(np.abs(profiles[p+'_serial']-profiles[p+'_mpi8']))/np.sum(np.abs(profiles[p+'_serial'])))) for p in ('Sod','Sedov')}
    axs[0].set(xlabel='x',ylabel='Density',title='Sod01, t = 0.2');axs[1].set(xlabel='Time',ylabel='Shock radius',title='Sedov03, spherical')
    axs[1].xaxis.set_major_locator(FixedLocator([.025,.05,.1,.2,.5]))
    axs[1].xaxis.set_major_formatter(FuncFormatter(lambda v,pos:f'{v:g}'))
    axs[1].xaxis.set_minor_formatter(NullFormatter())
    for ax in axs:ax.legend(fontsize=8);ax.grid(alpha=.2)
    fig.tight_layout();fig.savefig(OUT/'reference_checks.png',dpi=160)
    summary=dict(passed=all(c['passed'] for c in checks),cases=checks,serial_mpi_comparison=comparison,
                 definitions={'Sod':'Shock at largest falling pressure jump face; analytical Riemann root and Rankine-Hugoniot shock speed; uncertainty roughly half a cell.',
                 'Sedov':'Shock at largest falling density jump face; log-log regression of ALL saved t>0 outputs (no time-window optimization); endpoint exponent also reported.',
                 'scope':'Unpatched ideal-gas reference tests, not validation of historical Cu source/EOS/radiation or new production plume.'})
    (OUT/'validation.json').write_text(json.dumps(summary,indent=2),encoding='utf-8')
    print(json.dumps(summary,indent=2))
    if not summary['passed']:raise SystemExit(1)

if __name__=='__main__':main()
