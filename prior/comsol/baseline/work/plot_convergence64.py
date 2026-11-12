"""Four independent S4 convergence criteria; threshold and failures stay visible."""
from pathlib import Path
import json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parents[1]

def main():
    groups=[]
    for name in ['numerical_comparison','extra_grid_vs_baseline64','extra_grid_adjacent64']:
        p=R/f'results/{name}.json'
        if not p.exists():continue
        d=json.loads(p.read_text())
        for case,result in d['variants'].items():
            values=[result['metrics'][key]['max_relative_change_above_1pct_peak'] for key in ['radius_um','area_mm2','integrated_gray']]
            values.append(result['unsaturated_physical_brightness']['max_relative_change_above_1pct_peak'])
            groups.append({'label':d['main']+' → '+case,'values_pct':[None if v is None else 100*v for v in values],
                           'overall_pass':result['all_camera_and_physical_within_10pct']})
    if not groups:raise RuntimeError('No completed comparisons')
    fig,axes=plt.subplots(1,4,figsize=(12.5,5.3),layout='constrained')
    names=['Luminous radius','Luminous area','Net integrated grayscale','Unsaturated physical\nbrightness']
    for j,(ax,title) in enumerate(zip(axes,names)):
        for i,group in enumerate(groups):
            value=group['values_pct'][j]
            if value is None:ax.text(i,1,'N/E',ha='center',va='bottom',fontsize=9);continue
            ax.bar(i,value,color='#287c8e' if value<=10 else '#c55c36',width=.65)
            ax.annotate(f'{value:.2f}%',(i,value),xytext=(0,4),textcoords='offset points',ha='center',fontsize=8)
        maximum=max([10]+[g['values_pct'][j] for g in groups if g['values_pct'][j] is not None])
        ax.axhline(10,color='#444444',ls='--',lw=1)
        ax.set(title=title,ylabel='Maximum active-frame change (%)',ylim=(0,maximum*1.22+1))
        ax.set_xticks(np.arange(len(groups)),[g['label'] for g in groups],rotation=45,ha='right',fontsize=8)
        ax.grid(axis='y',alpha=.2);ax.set_axisbelow(True)
    fig.suptitle('S4 observation gates | frozen camera response | dashed line: 10% target',fontsize=12)
    fig.savefig(R/'figures/S4_numerical_convergence64.png',dpi=180,bbox_inches='tight');plt.close(fig)
    (R/'results/S4_numerical_convergence64_plot.json').write_text(json.dumps({'groups':groups,'criteria':names,'threshold_pct':10},indent=2),encoding='utf8')

if __name__=='__main__':main()
