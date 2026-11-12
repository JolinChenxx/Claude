"""Matched-FOV observables and fixed time-alignment scenarios, no fitting."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from plot_mechanisms import series
from morphology import experimental_signal,quantiles
import csv

def run(cases):
    exp=np.load(SOURCE/'experiment/S4_reconstructed.npz');et=exp['times_ns'];h,w=exp['gray'].shape[-2:]
    rows=[];curves={};factor=OMEGA*(PIXEL*1e-6)**2
    def record(case,shift,scope,stack):
        rr=[]
        for ns,im in zip(et,stack):
            row=dict(case=case,shift_ns=shift,scope=scope,experiment_ns=float(ns),**metrics(im),**quantiles(im))
            rows.append(row);rr.append(row)
        return rr
    for threshold in (5,8,20):
        stack=np.array([experimental_signal(x,threshold) for x in exp['gray']])
        curves[f'exp{threshold}']=record(f'experiment_threshold_{threshold}',0,'relative_gray',stack)
    for case in ['LTE']+cases:
        t,p=series(case)
        for shift in SHIFTS:
            stack=blur(gate(t,p,et+shift))*factor;mid=stack.shape[-1]//2
            record(case,shift,'expanded',stack)
            crop=stack[:,-h:,mid-w//2:mid+(w-w//2)]
            curves[(case,shift)]=record(case,shift,'camera_crop',crop)
    with (RES/'R2_observable_comparison.csv').open('w',newline='',encoding='utf-8') as file:
        writer=csv.DictWriter(file,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)
    plt=setup_plot();fig,axs=plt.subplots(2,3,figsize=(17,9),layout='constrained')
    ref=int(np.argmin(abs(et-30)))
    for j,key in enumerate(['width90_um','height90_um','z_um']):
        vals=np.array([[r[key] for r in curves[f'exp{q}']] for q in (5,8,20)],dtype=float)
        if key=='z_um':vals-=vals[:,ref,None]
        axs[0,j].fill_between(et,np.nanmin(vals,axis=0),np.nanmax(vals,axis=0),color='gray',alpha=.25,label='Experiment threshold 5-20')
        axs[0,j].plot(et,vals[1],'k.-',label='Experiment threshold 8')
        for case in ['LTE']+cases:
            rr=curves[(case,90)];v=np.array([r[key] for r in rr],dtype=float);good=np.array([r['total'] for r in rr])>=1
            if key=='z_um':v-=v[ref]
            axs[0,j].plot(et,np.where(good,v,np.nan),'.-',label=case)
        axs[0,j].set(title=['90% signal width','90% signal height span','Centroid displacement from 30 ns'][j],ylabel='um')
    for j,shift in enumerate(SHIFTS):
        for case in ['LTE']+cases:
            vals=np.array([r['total'] for r in curves[(case,shift)]])
            axs[1,j].semilogy(et,np.maximum(vals,1e-40),'.-',label=case)
        axs[1,j].axhline(1,color='gray',ls=':');axs[1,j].set(title=f'Fixed alignment {shift:g} ns',ylabel='Ideal photons / camera crop / 10 ns',ylim=(1e-15,1e7))
    for ax in axs.ravel():ax.set_xlabel('Experimental delay (ns)');ax.grid(alpha=.2);ax.legend(fontsize=6)
    fig.suptitle('Matched camera crop | shape display at shift 90 ns | no gain/time fit | absolute target plane unknown')
    fig.savefig(FIG/'R2_observable_comparison.png');plt.close(fig)
    # Separate line, recombination-continuum and free-free contributions using the
    # same time gate and collection factors. Earlier inherited LTE is excluded.
    components=[]
    for case in cases:
        folder=RES/f'{case}_radiation';t=np.load(folder/'axes.npz')['times_s']
        ns=et[(et+90-5>=t[0]*1e9)&(et+90+5<=t[-1]*1e9)]
        totals={}
        for mechanism in ['line','fb','ff']:
            p=np.load(folder/f'{mechanism}_photon_radiance.npy',mmap_mode='r')[:,0].sum((1,2))
            totals[mechanism]=gate(t,p,ns+90)*factor
        for k,delay in enumerate(ns):
            total=sum(v[k] for v in totals.values())
            components.append(dict(case=case,experiment_ns=float(delay),total_photons=float(total),
                **{m+'_photons':float(v[k]) for m,v in totals.items()},
                **{m+'_fraction':float(v[k]/max(total,1e-300)) for m,v in totals.items()}))
    with (RES/'R2_photon_mechanisms.csv').open('w',newline='',encoding='utf-8') as file:
        writer=csv.DictWriter(file,fieldnames=list(components[0]));writer.writeheader();writer.writerows(components)
    write_json(RES/'R2_observable_notes.json',dict(cases=['LTE']+cases,alignment_ns=list(SHIFTS),
        shape_rule='Omitted when fewer than one ideal whole-crop photon is expected; this is not a camera threshold.',
        target='Model z=0 is at nominal target; experiment target is not registered. Compare height span and displacement, not absolute height.',
        experiment='Fixed background 2 and connected bright-component thresholds 5/8/20. Publication grayscale is not radiometrically linear/calibrated.',
        component_scope='Directional formal-transfer photons over expanded image, 10 ns gate, ideal collection. Line includes LTE N/O; free-bound is seven-state Cu Milne approximation.'))
    print([r for r in components if r['experiment_ns'] in (150,260,740)])

if __name__=='__main__':run(sys.argv[1:])
