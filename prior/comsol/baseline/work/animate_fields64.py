"""Fixed-scale physical fields, true numerical time, no camera normalization."""
from pathlib import Path
import argparse,json,io
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
from PIL import Image
from radiation_s4 import physical_grid
R=Path(__file__).resolve().parents[1]

def main(case):
    m=json.loads((R/f'results/{case}_fields.json').read_text())
    data=np.memmap(R/f'results/{case}_fields.bin',dtype=m['dtype'],mode='r',shape=tuple(m['shape']))
    ts=np.asarray(m['times_s']);r=np.asarray(m['coordinates_um']['r']);z=np.asarray(m['coordinates_um']['z'])
    targets=np.r_[np.arange(0,301,10),np.arange(320,1001,20),np.arange(1080,2121,80),2120]*1e-9
    ids=np.unique([int(np.argmin(abs(ts-t))) for t in targets if t<=ts[-1]+1e-16])
    fig,axes=plt.subplots(1,4,figsize=(11,4.6),sharey=True)
    fig.subplots_adjust(left=.065,right=.98,bottom=.18,top=.78,wspace=.28)
    title=fig.suptitle('',fontsize=12,y=.98)
    fig.text(.5,.91,'Actual LTE fields in physical ALE coordinates; fixed scales; no camera gain',ha='center',fontsize=9)
    fig.text(.5,.035,'Fixed central observation window: r = 0–650 μm, z = 0–1000 μm; simulation domain extends to 2 × 3 mm.',ha='center',fontsize=8)
    names=['Copper mass density','Gas temperature','Electron density','Pressure / ambient']
    units=['kg/m³','K','1/m³','ratio'];norms=[LogNorm(1e-7,1e2),LogNorm(293.15,1e4),LogNorm(1e14,3e24),LogNorm(1,1e3)]
    plots=[]
    for ax,name,unit,norm in zip(axes,names,units,norms):
        pc=ax.pcolormesh(r,z,np.ones((len(z),len(r)))*norm.vmin,shading='auto',norm=norm,cmap='inferno')
        ax.set(title=name,xlabel='Radius (μm)',xlim=(0,650),ylim=(0,1000));ax.set_box_aspect(1000/650)
        cb=fig.colorbar(pc,ax=ax,location='bottom',fraction=.06,pad=.17);cb.set_label(unit,fontsize=8);cb.ax.tick_params(labelsize=7)
        plots.append(pc)
    axes[0].set_ylabel('Height (μm)')
    frames=[]
    for count,i in enumerate(ids):
        f=physical_grid(np.asarray(data[i],dtype=float),r,z)
        arrays=[f[3]*np.clip(f[2],0,1),f[0],f[1],f[7]/101325]
        for pc,a,norm in zip(plots,arrays,norms):pc.set_array(np.maximum(a,norm.vmin).ravel())
        title.set_text(f'{case} | numerical time {ts[i]*1e9:.0f} ns')
        buf=io.BytesIO();fig.savefig(buf,format='png',dpi=115);buf.seek(0);im=Image.open(buf).convert('RGB');frames.append(im.copy());buf.close()
        if abs(ts[i]*1e9-140)<1e-5:im.save(R/f'figures/{case}_physical_fields_140ns.png')
    times=ts[ids]*1e9;duration=np.maximum(20,np.r_[np.diff(times),100]/2120*10000).astype(int).tolist()
    frames[0].save(R/f'figures/{case}_physical_evolution.gif',save_all=True,append_images=frames[1:],duration=duration,loop=0,optimize=False)
    (R/f'figures/{case}_physical_animation.json').write_text(json.dumps({'case':case,'numerical_times_ns':times.tolist(),'duration_ms':duration,'fixed_color_scales':True,'camera_gain_used':False,'source':'actual saved FE field export mapped to physical ALE coordinates','window_um':[0,650,0,1000],'complete_domain_shown':False},indent=2))
    plt.close(fig);print('PHYSICAL_ANIMATION_COMPLETE',case,len(frames),flush=True)
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('case');main(p.parse_args().case)
