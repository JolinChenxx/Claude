"""T009 stock-PLUTO diagnostics; independent comparison, never calibration."""
from pathlib import Path
import argparse, csv, json
import numpy as np

ENERGIES = [20,30,45,65,90,130,180,250,320]
P_AMB = 0.101325

def dump(path, value):
    Path(path).write_text(json.dumps(value,indent=2,allow_nan=False),encoding='utf-8')

def table(path, rows):
    if not rows: return
    with Path(path).open('w',newline='',encoding='utf-8') as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0])); w.writeheader(); w.writerows(rows)

def entries(run):
    return [line.split() for line in (run/'flt.out').read_text().splitlines() if line.strip() and not line.startswith('#')]

def field(run, entry, n):
    dtype=np.dtype(('<' if entry[5]=='little' else '>')+'f4')
    names=entry[6:]
    data=np.fromfile(run/f'data.{int(entry[0]):04d}.flt',dtype=dtype)
    if data.size!=len(names)*n*n: raise ValueError(f'Wrong binary size: {run}, {entry[0]}')
    data=data.reshape(len(names),n,n)
    if not np.isfinite(data).all(): raise ValueError(f'Nonfinite fluid output: {run}, {entry[0]}')
    return dict(zip(names,data))

def crossing(x,p):
    """First threshold crossing when scanning from the outer edge inward."""
    hits=np.flatnonzero(p>1.2*P_AMB)
    if not len(hits): return None
    i=int(hits[-1])
    if i+1==len(x): return None
    return float(x[i]+(1.2*P_AMB-p[i])*(x[i+1]-x[i])/(p[i+1]-p[i]))

def analyze_case(run):
    recipe=json.loads((run/'recipe.json').read_text())
    n=recipe['n']; energy=recipe['energy_uJ']; dx=1200.0/n
    x=(np.arange(n)+0.5)*dx
    rows=[]; step0=None
    for entry in entries(run):
        fields=field(run,entry,n); p=fields['prs'].astype(float)
        rows.append(dict(t_sim=float(entry[1]),R_axis=crossing(x,p[:,0]),
                         R_wall=crossing(x,p[0,:]),R_45=crossing(np.sqrt(2)*x,p.diagonal())))
        if int(entry[3])==0:
            volume=2*np.pi*x[None,:]*dx*dx
            step0=float(np.sum((p-P_AMB)/0.4*volume)/1.e6)
    budget=np.atleast_1d(np.genfromtxt(run/'budget.csv',delimiter=',',names=True))
    initial=float(budget['excess_uJ'][0])
    before=budget['outer_max_p_MPa']<=1.2*P_AMB
    # Once the front reaches the boundary, do not include subsequent rows.
    hit=np.flatnonzero(~before)
    end=int(hit[0]) if len(hit) else len(budget)
    total=np.asarray(budget['total_uJ'][:end]); excess=np.asarray(budget['excess_uJ'][:end])
    total_drift=float(np.max(np.abs(total/total[0]-1))) if len(total) else None
    excess_drift=float(np.max(np.abs(excess/excess[0]-1))) if len(excess) else None
    diagnostics=dict(energy_uJ=energy,n=n,frames=len(rows),last_t_ns=rows[-1]['t_sim'],
                     initial_discrete_energy_uJ=initial,initial_relative_error=abs(initial/energy-1),
                     initial_flt_energy_uJ=step0,initial_flt_relative_error=abs(step0/energy-1) if step0 is not None else None,
                     total_energy_max_relative_drift_before_boundary=total_drift,
                     excess_energy_max_relative_drift_before_boundary=excess_drift,
                     boundary_arrival_ns=float(budget['t_ns'][end]) if len(hit) else None,
                     final_step=int(budget['step'][-1]),
                     complete=rows[-1]['t_sim']>=recipe['tstop']-0.01)
    diagnostics['energy_checks_passed']=(diagnostics['initial_relative_error']<0.001 and
          diagnostics['initial_flt_relative_error'] is not None and diagnostics['initial_flt_relative_error']<0.001 and
          total_drift is not None and total_drift<0.001)
    return rows,diagnostics

def interp(rows,t,key='R_axis'):
    x=np.array([r['t_sim'] for r in rows]); y=np.array([r[key] if r[key] is not None else np.nan for r in rows])
    target=np.asarray(t,dtype=float)
    if np.any(target<x[0]) or np.any(target>x[-1]): raise ValueError('Extrapolation is not allowed')
    value=np.interp(target,x,y)
    if not np.isfinite(value).all(): raise ValueError('Missing shock crossing')
    return value

def acceptable_intervals(rows):
    ordered=sorted(rows,key=lambda r:r['energy_uJ']); seg=[]
    if not any(r['rms_relative']<=0.10 for r in ordered): return None
    for a,b in zip(ordered[:-1],ordered[1:]):
        x,y=a['energy_uJ'],b['energy_uJ']; fa,fb=a['rms_relative'],b['rms_relative']
        if fa<=0.10 and fb<=0.10: seg.append([float(x),float(y)])
        elif (fa<=0.10)!=(fb<=0.10):
            edge=x+(0.10-fa)*(y-x)/(fb-fa)
            seg.append([float(x),float(edge)] if fa<=0.10 else [float(edge),float(y)])
    merged=[]
    for pair in seg:
        if merged and abs(merged[-1][1]-pair[0])<1.e-9: merged[-1][1]=pair[1]
        else: merged.append(pair)
    return merged

def density_consistency(default,radial):
    ea,eb=entries(default)[-1],entries(radial)[-1]
    if abs(float(ea[1])-100)>0.01 or abs(float(eb[1])-100)>0.01: raise ValueError('Benchmark did not reach 100 ns')
    a=field(default,ea,480)['rho'].astype(float); b=field(radial,eb,480)['rho'].astype(float)
    weights=(np.arange(480)+0.5)[None,:]
    return dict(t_ns=100,relative_L1=float(np.sum(np.abs(a-b))/np.sum(np.abs(a))),
                volume_weighted_relative_L1=float(np.sum(np.abs(a-b)*weights)/np.sum(np.abs(a)*weights)))

def main():
    parser=argparse.ArgumentParser(); parser.add_argument('--raw',type=Path,default=Path('D:/sim_runs/T009'))
    parser.add_argument('--out',type=Path,required=True); parser.add_argument('--experiment',type=Path,required=True)
    args=parser.parse_args(); args.out.mkdir(parents=True,exist_ok=True)
    curves={}; diagnostics={}; errors={}
    specs=[(f'E{e:03d}',args.raw/'scan'/f'E{e:03d}') for e in ENERGIES]
    specs += [('G240',args.raw/'convergence/G240'),('G960',args.raw/'convergence/G960')]
    for name,run in specs:
        try:
            rows,checks=analyze_case(run); curves[name]=rows; diagnostics[name]=checks
            table(args.out/(f'shock_{name}.csv'),rows)
        except Exception as exc: errors[name]=repr(exc)
    if 'E090' in curves: curves['G480']=curves['E090']
    summary=dict(description='Independent ideal-gas blast check; no v0 parameter calibration.',
                 diagnostics=diagnostics,errors=errors,complete=False,
                 early_exponent_fit='log R_axis versus log t_sim, E90, only 60 to 200 um',
                 time_offsets_ns=[25,55,85],speed_fit_interval_exp_ns=[300,900],
                 threshold_pressure_MPa=1.2*P_AMB,energy_interval_rule='Adjacent E RMS deviations linearly interpolated; no extrapolation')
    try:
        summary['partition_consistency']=density_consistency(args.raw/'benchmark/default_n08',args.raw/'benchmark/radial_n08')
    except Exception as exc: errors['partition']=repr(exc)
    experiment=np.genfromtxt(args.experiment,delimiter=',',names=True,comments='#',dtype=None,encoding='utf-8',skip_header=2)
    deviations=[]
    for energy in ENERGIES:
        rows=curves.get(f'E{energy:03d}')
        if not rows or not diagnostics[f'E{energy:03d}']['complete']: continue
        for tc in [25,55,85]:
            for group in ['high','low']:
                exp=experiment[experiment['fluence']==group]
                valid=exp[(exp['t_ns']>=100)&(exp['t_ns']<=900)]
                model=interp(rows,valid['t_ns']-tc)
                rms=float(np.sqrt(np.mean((model/valid['R_um']-1)**2)))
                exp_speed=exp[(exp['t_ns']>=300)&(exp['t_ns']<=900)]
                mt=np.array([r['t_sim']+tc for r in rows]); mr=np.array([r['R_axis'] for r in rows],dtype=float)
                mask=(mt>=300)&(mt<=900)
                deviations.append(dict(energy_uJ=energy,t_c_ns=tc,fluence=group,points=len(valid),rms_relative=rms,
                    speed_model_km_s=float(np.polyfit(mt[mask],mr[mask],1)[0]),
                    speed_exp_km_s=float(np.polyfit(exp_speed['t_ns'],exp_speed['R_um'],1)[0])))
    table(args.out/'deviation_table.csv',deviations)
    summary['deviation_rows']=len(deviations); summary['energy_intervals']={}
    for group in ['high','low']:
        blocks={}
        for tc in [25,55,85]:
            selected=[r for r in deviations if r['fluence']==group and r['t_c_ns']==tc]
            blocks[str(tc)]=dict(intervals_uJ=acceptable_intervals(selected),
                                best_three=sorted(selected,key=lambda r:r['rms_relative'])[:3])
        summary['energy_intervals'][group]=blocks
    if 'E090' in curves:
        rows=curves['E090']; t=np.array([r['t_sim'] for r in rows]); rad=np.array([r['R_axis'] for r in rows],dtype=float)
        mask=(t>0)&(rad>=60)&(rad<=200)
        summary['early_power_exponent']=float(np.polyfit(np.log(t[mask]),np.log(rad[mask]),1)[0]) if mask.sum()>=3 else None
        summary['early_fit_points']=int(mask.sum())
        angular=[float(interp(rows,300,k)) for k in ['R_axis','R_wall','R_45']]
        summary['radii_at_300ns_um']=dict(zip(['axis','wall','45deg'],angular))
        summary['angular_spread_at_300ns']=(max(angular)-min(angular))/float(np.mean(angular))
    summary['convergence']={}
    for grid in [240,480,960]:
        if f'G{grid}' in curves:
            summary['convergence'][str(grid)]={str(t):float(interp(curves[f'G{grid}'],t)) for t in [100,300,600]}
    if all(str(n) in summary['convergence'] for n in [480,960]):
        summary['grid_relative_differences']={str(t):abs(summary['convergence']['480'][str(t)]/summary['convergence']['960'][str(t)]-1) for t in [100,300,600]}
    complete=(len(diagnostics)==11 and all(d['complete'] for d in diagnostics.values()) and len(deviations)==54 and not errors)
    summary['complete']=complete
    checks=dict(all_cases_complete=complete,all_initial_and_total_energy_checks=all(d['energy_checks_passed'] for d in diagnostics.values()) and len(diagnostics)==11,
        exponent_0p40_plusminus_0p03=summary.get('early_power_exponent') is not None and abs(summary['early_power_exponent']-0.40)<=0.03,
        angular_spread_below_2pct=summary.get('angular_spread_at_300ns',1)<0.02,
        grid_300_600_below_1pct=all(summary.get('grid_relative_differences',{}).get(str(t),1)<0.01 for t in [300,600]),
        partition_relative_L1_below_PLAN_0p5pct=summary.get('partition_consistency',{}).get('relative_L1',1)<0.005)
    summary['checks']=checks; summary['passed']=all(checks.values())
    dump(args.out/'summary.json',summary)
    plots(args.out,curves,experiment,summary,args.raw)
    print(json.dumps(dict(complete=complete,passed=summary['passed'],checks=checks,errors=errors)),flush=True)
    raise SystemExit(0 if summary['passed'] else 2)

def plots(out,curves,experiment,summary,raw):
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    plt.rcParams.update({'font.size':10,'figure.dpi':130})
    fig,axs=plt.subplots(1,3,figsize=(16,5),sharey=True)
    colors=plt.cm.viridis(np.linspace(0,1,9))
    for ax,tc in zip(axs,[25,55,85]):
        for e,c in zip(ENERGIES,colors):
            rows=curves.get(f'E{e:03d}',[])
            if rows: ax.plot([r['t_sim']+tc for r in rows],[r['R_axis'] for r in rows],color=c,label=f'{e} uJ')
        for group,marker,color in [('high','o','crimson'),('low','s','black')]:
            exp=experiment[experiment['fluence']==group]
            ax.scatter(exp['t_ns'],exp['R_um'],marker=marker,s=18,facecolors='none',edgecolors=color,label=group+' experiment')
        ax.set(xlim=(0,950),ylim=(0,1250),xlabel='Experimental time (ns)',title=f't_exp = t_sim + {tc} ns'); ax.grid(alpha=.2)
    axs[0].set_ylabel('Axis shock distance (um)'); axs[-1].legend(fontsize=8,loc='upper left',ncol=2)
    fig.tight_layout();fig.savefig(out/'shock_vs_fig3d.png');plt.close(fig)
    fig,axs=plt.subplots(1,2,figsize=(10,4.5))
    for n in [240,480,960]:
        rows=curves.get(f'G{n}',[])
        if rows:
            t=np.array([r['t_sim'] for r in rows]); rad=np.array([r['R_axis'] for r in rows],dtype=float);mask=t>0
            axs[0].loglog(t[mask],rad[mask],label=f'{1200/n:g} um')
    axs[0].axhspan(60,200,color='gray',alpha=.1)
    exponent=summary.get('early_power_exponent')
    axs[0].set(xlabel='Simulation time (ns)',ylabel='Axis shock distance (um)',title=f'E=90 uJ; early exponent={exponent:.4f}' if exponent is not None else 'Early exponent unavailable')
    es=[];rs=[]
    for e in ENERGIES:
        if f'E{e:03d}' in curves: es.append(e);rs.append(float(interp(curves[f'E{e:03d}'],300)))
    if es:
        axs[1].loglog(es,rs,'o-',label='t_sim = 300 ns')
        axs[1].loglog(es,np.asarray(es,dtype=float)**.2*(rs[0]/es[0]**.2),'--',label='E^0.2 guide')
    axs[1].set(xlabel='Deposited energy (uJ)',ylabel='Axis shock distance (um)')
    for ax in axs:ax.grid(alpha=.2);ax.legend()
    fig.tight_layout();fig.savefig(out/'scaling.png');plt.close(fig)
    fig,axs=plt.subplots(1,2,figsize=(10,4.5))
    for n in [240,480,960]:
        rows=curves.get(f'G{n}',[])
        if rows:axs[0].plot([r['t_sim'] for r in rows],[r['R_axis'] for r in rows],label=f'{n} x {n}')
    if 'G960' in curves:
        t=np.arange(10,901,5)
        for n in [240,480]:
            if f'G{n}' in curves:axs[1].plot(t,100*(interp(curves[f'G{n}'],t)/interp(curves['G960'],t)-1),label=f'{n} vs 960')
    axs[0].set(xlabel='Simulation time (ns)',ylabel='Axis shock distance (um)',title='E = 90 uJ')
    axs[1].set(xlabel='Simulation time (ns)',ylabel='Radius difference (%)');axs[1].axhline(1,color='gray',ls='--');axs[1].axhline(-1,color='gray',ls='--')
    for ax in axs:ax.grid(alpha=.2);ax.legend()
    fig.tight_layout();fig.savefig(out/'convergence.png');plt.close(fig)
    if summary.get('angular_spread_at_300ns',0)>=0.02:
        run=raw/'scan/E090'; entry=min(entries(run),key=lambda e:abs(float(e[1])-300));p=field(run,entry,480)['prs'];x=(np.arange(480)+.5)*2.5
        fig,ax=plt.subplots(figsize=(7,4))
        ax.plot(x,p[:,0],label='axis');ax.plot(x,p[0,:],label='wall');ax.plot(np.sqrt(2)*x,p.diagonal(),label='45 degrees')
        ax.axhline(1.2*P_AMB,color='gray',ls='--');ax.set(xlabel='Radius (um)',ylabel='Pressure (MPa)',title=f'Directional profiles at {float(entry[1]):.3f} ns');ax.legend()
        fig.tight_layout();fig.savefig(out/'angular_profiles.png');plt.close(fig)

if __name__=='__main__': main()
