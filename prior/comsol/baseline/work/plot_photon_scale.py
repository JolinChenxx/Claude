"""Physical photon-scale evidence beside the mathematical grayscale contour."""
from pathlib import Path
import argparse,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
from audit_panel_alignment import require_matplotlib_panel_alignment
from camera_s4 import gate,blur
from extract_experiment import contour_mask
ROOT=Path(__file__).resolve().parents[1];FIG=ROOT/'figures'
plt.rcParams.update({'font.family':'Arial','font.size':8,'pdf.fonttype':42,'svg.fonttype':'none'})
def plot(case):
 a=np.load(ROOT/f'results/{case}_radiation/radiance.npz');cam=np.load(ROOT/f'results/{case}_camera/images.npz');fit=json.loads((ROOT/f'results/{case}_camera/calibration.json').read_text())
 t=cam['times_ns'];raw=blur(gate(a['times_s']*1e9,a['radiance'][:,0],t+fit['time_shift_ns']),1)
 omega=2*np.pi*(1-np.sqrt(1-.4**2));factor=omega*(float(cam['pixel_um'])*1e-6)**2*10e-9*525e-9/(6.62607015e-34*299792458.)
 photons=raw*factor;lo=1e-10
 positive_peak=np.clip(float(photons.max()),1e-300,None)
 hi=max(1.,10**np.ceil(np.log10(positive_peak)))
 fig,axes=plt.subplots(2,3,figsize=(7.2,5.4),gridspec_kw={'wspace':.08,'hspace':.24});chosen=[0,60,150,260,400,740]
 for ax,ns in zip(axes.flat,chosen):
  i=int(np.flatnonzero(t==ns)[0]);p=photons[i]
  image=ax.imshow(np.maximum(p,lo),norm=LogNorm(lo,hi),cmap='magma',interpolation='nearest')
  if p.min()<1<p.max():ax.contour(p,levels=[1],colors=['cyan'],linewidths=.7)
  mask=contour_mask(cam['475_525_psf1'][i],27)
  if mask.any() and not mask.all():ax.contour(mask,levels=[.5],colors=['white'],linewidths=.6)
  ax.set_xticks([]);ax.set_yticks([]);ax.set_title(f'{ns} ns',fontsize=8,pad=4)
 fig.subplots_adjust(left=.04,right=.855,bottom=.14,top=.87)
 cax=fig.add_axes([.89,.22,.015,.57]);cb=fig.colorbar(image,cax=cax);cb.set_label('Ideal band photons / pixel / 10 ns',fontsize=8);cb.ax.tick_params(labelsize=8)
 fig.suptitle(f'{case} | photon scale before intensifier gain',fontsize=10,y=.97)
 fig.text(.46,.915,'Cyan: 1 ideal photon. White: grayscale 27 of the fitted mean response.',ha='center',fontsize=7)
 fig.text(.5,.075,'NA = 0.4; perfect throughput/QE; 525 nm conversion; 11.11 μm/pixel.',ha='center',fontsize=7)
 fig.text(.5,.035,'Display floor: 1e-10 photons. No photon counting realization is simulated.',ha='center',fontsize=7)
 fig.canvas.draw();require_matplotlib_panel_alignment(fig,exclude_axes=[cax],json_out=FIG/f'{case}_photon_scale.alignment.json')
 fig.savefig(FIG/f'{case}_photon_scale.pdf',dpi=300)
 fig.savefig(FIG/f'{case}_photon_scale.svg',dpi=300)
 fig.savefig(FIG/f'{case}_photon_scale.png',dpi=300)
 plt.close(fig)
 np.savez_compressed(ROOT/f'results/{case}_camera/photon_maps.npz',times_ns=t,ideal_photons=photons,NA=.4,throughput_QE=1)
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('case');plot(p.parse_args().case)
