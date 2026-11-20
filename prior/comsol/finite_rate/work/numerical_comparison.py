"""Original 1T solve vs 1 ns maximum-step/manual-scaling repeat."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from plot_mechanisms import series
from morphology import quantiles
import csv

def run():
    cases=['R2_CR1T','R2_CR1T_R'];times=np.arange(35.,806.,10.);values=[]
    for case in cases:
        t,p=series(case);maps=blur(gate(t,p,times+90))*OMEGA*(PIXEL*1e-6)**2
        values.append(np.array([[im.sum(),quantiles(im)['width90_um'],metrics(im)['z_um']] for im in maps]))
    old,new=values;rows=[]
    for i,ns in enumerate(times):
        rows.append(dict(experiment_ns=float(ns),original_photons=float(old[i,0]),refined_photons=float(new[i,0]),
            relative_photon_change=float(new[i,0]/max(old[i,0],1e-300)-1),
            original_width90_um=float(old[i,1]),refined_width90_um=float(new[i,1]),
            centroid_change_um=float(new[i,2]-old[i,2])))
    with (RES/'R2_1T_numerical_comparison.csv').open('w',newline='',encoding='utf-8') as file:
        writer=csv.DictWriter(file,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)
    summary=dict(method='Original 1T vs repeated 1T with maximum internal step 1 ns, manual scaling and unused ee source disabled. Both use corrected total radiative linewidths and 4 ns radiation samples. Changes are a combined numerical robustness check, not a pure isolated step-size test.',
        max_relative_photon_change=float(np.max(abs(new[:,0]/old[:,0]-1))),
        max_relative_width_change=float(np.max(abs(new[:,1]/old[:,1]-1))),
        max_centroid_change_um=float(np.max(abs(new[:,2]-old[:,2]))),
        early_relative_photon_change=float(new[0,0]/old[0,0]-1),late_relative_photon_change=float(new[-1,0]/old[-1,0]-1))
    write_json(RES/'R2_1T_numerical_comparison.json',summary)
    plt=setup_plot();fig,axs=plt.subplots(1,3,figsize=(15,4.5),layout='constrained')
    for case,values in zip(cases,[old,new]):
        axs[0].semilogy(times,values[:,0],label=case);axs[1].plot(times,values[:,1],label=case)
    axs[2].plot(times,100*(new[:,0]/old[:,0]-1),label='Photon change (%)')
    for ax,title,y in zip(axs,['Ideal photons','90% photon width','Numerical sensitivity'],['Photons / 10 ns','um','percent']):ax.set(title=title,xlabel='Experimental delay, shift 90 ns (ns)',ylabel=y);ax.grid(alpha=.2);ax.legend(fontsize=8)
    fig.savefig(FIG/'R2_1T_numerical_comparison.png');plt.close(fig);print(summary)

if __name__=='__main__':run()
