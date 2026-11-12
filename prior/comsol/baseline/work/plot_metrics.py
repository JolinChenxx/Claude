from pathlib import Path
import argparse,csv,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
from audit_panel_alignment import require_matplotlib_panel_alignment
from radiation_s4 import physical_grid
from plot_s4 import save,ROOT,FIG
plt.rcParams.update({'font.family':'Arial','pdf.fonttype':42,'svg.fonttype':'none'})

def curves(case):
 rows=list(csv.DictReader((ROOT/f'results/{case}_camera/metrics.csv').open()))
 rows=[r for r in rows if r['band']=='475_525' and r['PSF_FWHM_pixels']=='1' and r['threshold']=='27']
 t=np.array([float(r['time_ns']) for r in rows]);fig,axes=plt.subplots(2,3,figsize=(7.2,4.6),gridspec_kw={'wspace':.4,'hspace':.45})
 for ax,key,title,unit in zip(axes.flat,['radius_um','area_mm2','mean_gray','integrated_gray','top_um','bottom_um'],['Luminous radius','Luminous area','Mean grayscale (mask)','Integrated grayscale','Upper luminous edge','Lower luminous edge'],['μm','mm²','counts','counts','μm','μm']):
  def col(k):return np.array([float(r[k]) if r[k] else np.nan for r in rows])
  ex=col('experiment_'+key);sim=col(key)
  ax.axvspan(0,80,color='.9',zorder=0);ax.plot(t,ex,'o-',color='#1876a5',ms=3,lw=1,label='S4 image');ax.plot(t,sim,'s-',color='#bd5232',ms=2.5,lw=1,label='LTE camera')
  if key=='radius_um':ax.fill_between(t,np.maximum(0,ex-np.maximum(.2*ex,2*400/36)),ex+np.maximum(.2*ex,2*400/36),color='#1876a5',alpha=.12,label='Spatial target')
  ax.set(title=title,xlabel='Experimental time (ns)',ylabel=unit,xlim=(0,760));ax.spines[['top','right']].set_visible(False)
 handles,labels=axes[0,0].get_legend_handles_labels()
 fig.legend(handles,labels,frameon=False,fontsize=7,loc='upper center',bbox_to_anchor=(.54,.94),ncol=3)
 fig.subplots_adjust(left=.12,right=.97,bottom=.15,top=.82);fig.suptitle(f'{case} | frozen early calibration, holdout 150–740 ns',fontsize=10)
 fig.text(.5,.025,'Gray zone: calibration. Upper/lower image origins are not independently registered.',ha='center',fontsize=7)
 save(fig,case+'_evolution_metrics')
 # Band/PSF sensitivity: same gain and offset; sensitivity is not refitted.
 fig,axes=plt.subplots(1,3,figsize=(7.2,2.7),gridspec_kw={'wspace':.4})
 for ax,key,title in zip(axes,['radius_um','area_mm2','integrated_gray'],['Radius (μm)','Area (mm²)','Integrated grayscale']):
  allrows=list(csv.DictReader((ROOT/f'results/{case}_camera/metrics.csv').open()))
  for band,psf,label,color in [('475_525','1','475–525 / 1 px','#171717'),('490_510','1','490–510 / 1 px','#0072b2'),('450_550','1','450–550 / 1 px','#d55e00'),('475_525','0','475–525 / 0 px','#009e73'),('475_525','2','475–525 / 2 px','#cc79a7')]:
   rr=[r for r in allrows if r['band']==band and r['PSF_FWHM_pixels']==psf and r['threshold']=='27'];ax.plot([float(r['time_ns']) for r in rr],[float(r[key]) if r[key] else np.nan for r in rr],lw=1,label=label,color=color)
  ax.set(xlabel='Time (ns)',ylabel=title);ax.spines[['top','right']].set_visible(False)
 handles,labels=axes[0].get_legend_handles_labels();fig.legend(handles,labels,ncol=3,loc='upper center',frameon=False,fontsize=6)
 fig.subplots_adjust(left=.08,right=.97,bottom=.19,top=.70);save(fig,case+'_camera_sensitivity')

def physical(case):
 p=ROOT/'results';meta=json.loads((p/f'{case}_fields.json').read_text());f=np.memmap(p/f'{case}_fields.bin',dtype=meta['dtype'],shape=tuple(meta['shape']),mode='r')
 r=np.array(meta['coordinates_um']['r']);z=np.array(meta['coordinates_um']['z']);ts=np.array(meta['times_s'])*1e9
 fit=json.loads((p/f'{case}_camera/calibration.json').read_text());expt=[0,60,150,260,740];fields=[]
 for t in expt:
  tc=t+fit['time_shift_ns'];i=np.argmin(abs(ts-tc));fields.append(physical_grid(np.array(f[i],dtype=float),r,z))
 fig,axes=plt.subplots(4,5,figsize=(7.2,8.7),gridspec_kw={'wspace':.15,'hspace':.35});coloraxes=[]
 settings=[('Cu density (kg/m³)',lambda q:np.maximum(q[2],0)*q[3],1e-7,300,'magma'),('Temperature (K)',lambda q:q[0],293.15,10000,'inferno'),('Electron density (1/m³)',lambda q:q[1],1e14,1e25,'viridis'),('Pressure / ambient',lambda q:q[7]/101325,.9,3000,'plasma')]
 for row,(label,fn,lo,hi,cmap) in enumerate(settings):
  for col,q in enumerate(fields):
   ax=axes[row,col];a=fn(q);im=ax.pcolormesh(r,z,np.maximum(a,lo),norm=LogNorm(lo,hi),cmap=cmap,shading='auto',rasterized=True)
   ax.set(xlim=(0,650),ylim=(0,1000),aspect='equal');ax.set_xticks([0,300,600]);ax.set_yticks([0,500,1000])
   if row==0:ax.set_title(f'{expt[col]} ns',fontsize=8)
   if col:ax.set_yticklabels([])
   else:ax.set_ylabel('Height (μm)')
   if row==3:ax.set_xlabel('Radius (μm)')
  cax=fig.add_axes([.88,.12+(3-row)*.205,.013,.15]);coloraxes.append(cax);cb=fig.colorbar(im,cax=cax);cb.set_label(label,fontsize=8);cb.ax.tick_params(labelsize=8)
 fig.subplots_adjust(left=.08,right=.855,bottom=.10,top=.90);fig.suptitle(f'{case} | material and thermodynamic fields',fontsize=10,y=.975)
 fig.text(.5,.935,'Actual LTE fields in physical ALE coordinates; these are not camera brightness.',ha='center',fontsize=7)
 fig.canvas.draw();require_matplotlib_panel_alignment(fig,exclude_axes=coloraxes,json_out=FIG/(case+'_physical_fields.alignment.json'))
 fig.savefig(FIG/(case+'_physical_fields.pdf'));fig.savefig(FIG/(case+'_physical_fields.svg'));fig.savefig(FIG/(case+'_physical_fields.png'),dpi=300);plt.close(fig)

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('case');a=p.parse_args();curves(a.case);physical(a.case)
