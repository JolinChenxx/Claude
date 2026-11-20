"""Supplementary physical-quantity sensitivity on identical saved time nodes.

This does not replace the S4 gate comparison or claim full-field convergence.
"""
from pathlib import Path
import argparse,csv,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'results'
QUANTITIES={
    'copper_gas_mass':('gas',0,1e12,'Copper in gas (ng)'),
    'gas_excess_energy':('gas',2,1e6,'Gas excess energy (μJ)'),
    'peak_temperature':('max',1,1,'Peak gas temperature (K)'),
    'peak_electron_density':('max',2,1,'Peak electron density (m⁻³)'),
    'peak_speed':('max',3,1,'Peak gas speed (m/s)'),
    'max_recession':('wallmax',2,1e6,'Maximum recession (μm)'),
}

def read(case):
    tables={tag:np.loadtxt(RES/f'{case}_{tag}.csv',delimiter=',',skiprows=1,ndmin=2) for tag in {'gas','max','wallmax'}}
    times=tables['max'][:,0]
    if not np.all(np.diff(times)>0):raise ValueError('Nonunique saved time array: '+case)
    for table in tables.values():
        if len(table)!=len(times) or not np.all(np.isfinite(table)):raise ValueError('Invalid saved table: '+case)
    return times,{key:tables[tag][:,col]*scale for key,(tag,col,scale,_) in QUANTITIES.items()}

def compare(main,variants):
    bt,base=read(main);output={};csvrows=[]
    fig,axes=plt.subplots(2,3,figsize=(12,6.6),layout='constrained')
    for ax,(key,(_,_,_,label)) in zip(axes.flat,QUANTITIES.items()):
        ax.plot(bt*1e9,base[key],label=main,lw=1.7);ax.set(xlabel='Numerical time (ns)',ylabel=label);ax.grid(alpha=.2)
    for case in variants:
        vt,values=read(case)
        # Integer picoseconds identify saved nodes without time interpolation.
        _,bi,vi=np.intersect1d(np.rint(bt*1e12).astype(np.int64),np.rint(vt*1e12).astype(np.int64),return_indices=True)
        if not len(bi) or np.max(abs(bt[bi]-vt[vi]))>1e-15:raise ValueError('No trustworthy common saved times')
        summary={}
        for ax,(key,(_,_,_,label)) in zip(axes.flat,QUANTITIES.items()):
            a=base[key][bi];b=values[key][vi];scale=max(float(np.max(abs(a))),1e-100)
            active=abs(a)>=.01*scale
            summary[key]={'max_absolute_difference_normalized_by_reference_peak':float(np.max(abs(b-a))/scale),
                          'max_relative_difference_on_1pct_active_times':float(np.max(abs(b[active]-a[active])/abs(a[active]))) if active.any() else None,
                          'reference_peak':float(a.max()),'variant_peak':float(b.max()),'label_and_units':label}
            ax.plot(vt*1e9,values[key],label=case,lw=1.1,ls='--')
            for i,j in zip(bi,vi):csvrows.append({'variant':case,'time_ns':float(bt[i]*1e9),'quantity':key,'main_value':float(base[key][i]),'variant_value':float(values[key][j])})
        output[case]={'common_saved_nodes':len(bi),'common_time_range_ns':[float(bt[bi[0]]*1e9),float(bt[bi[-1]]*1e9)],'metrics':summary}
    axes.flat[0].legend(fontsize=8)
    fig.suptitle('COMSOL 6.4 | physical-quantity sensitivity (true saved solutions)',fontsize=12)
    fig.savefig(ROOT/'figures/physical_numerical_comparison64.png',dpi=180);plt.close(fig)
    result={'main':main,'variants':output,'scope':'Supplementary integrated quantities and sampled extrema; no interpolation, no full-field convergence claim.'}
    (RES/'physical_numerical_comparison64.json').write_text(json.dumps(result,indent=2),encoding='utf8')
    with (RES/'physical_numerical_comparison64.csv').open('w',newline='',encoding='utf-8-sig') as f:
        writer=csv.DictWriter(f,fieldnames=csvrows[0]);writer.writeheader();writer.writerows(csvrows)
    print(json.dumps(result,indent=2))

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('main');p.add_argument('variants',nargs='+');a=p.parse_args();compare(a.main,a.variants)
