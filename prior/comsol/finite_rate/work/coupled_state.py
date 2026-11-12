import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from render_kinetics import physical_all

def run():
    cases=['R2_CR1T','R2_COUPLED_FIN'];states=[];rows=[]
    for case in cases:
        meta=json.loads((RES/f'{case}_fields.json').read_text());a=np.memmap(RES/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r');t=np.array(meta['times_s']);i=np.argmin(abs(t*1e9-160));r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);s=physical_all(np.array(a[i],float),r,z);states.append((s,r,z))
        weight=s[33];tot=volume_integral(weight,r,z);active=weight>weight.max()*1e-3
        rows.append(dict(case=case,numerical_ns=float(t[i]*1e9),peak_Tg_K=float(s[0].max()),Cu_mass_weighted_Tg_K=float(volume_integral(s[0]*weight,r,z)/tot),Cu_mass_weighted_Te_K=float(volume_integral(s[19]*weight,r,z)/tot),active_peak_Te_K=float(s[19][active].max()),ionized_Cu_number=float(volume_integral(s[18],r,z)),Cu_mass_kg=float(tot)))
    plt=setup_plot();from matplotlib.colors import LogNorm
    fig,axs=plt.subplots(2,4,figsize=(15,8),layout='constrained')
    for i,(s,r,z) in enumerate(states):
        vals=[s[0],np.where(s[1]>=1e16,s[19],np.nan),s[18],s[33]]
        for j,(val,title) in enumerate(zip(vals,['Gas T (K)','Electron T (K)','Cu+ density (m-3)','Cu density (kg/m3)'])):
            kw=dict(vmin=293,vmax=8000) if j<2 else dict(norm=LogNorm(1e12,1e22) if j==2 else LogNorm(1e-6,100))
            cmap=plt.get_cmap('inferno').copy();cmap.set_bad('black')
            im=axs[i,j].pcolormesh(r,z,np.ma.masked_invalid(np.ma.masked_less_equal(val,0)),shading='auto',cmap=cmap,**kw)
            axs[i,j].set(xlim=(0,160),ylim=(0,200),xlabel='r (um)',ylabel=cases[i]+'\nz (um)')
            if i==0:axs[i,j].set_title(title)
            fig.colorbar(im,ax=axs[i,j],fraction=.04)
    fig.suptitle('Numerical 160 ns | fixed-flow 1T vs energy-coupled 2T (no electron conduction) | Te shown only for ne >= 1e16 m-3')
    fig.savefig(FIG/'R2_coupled_state_comparison.png');plt.close(fig);write_json(RES/'R2_coupled_state_comparison.json',rows)

if __name__=='__main__':run()
