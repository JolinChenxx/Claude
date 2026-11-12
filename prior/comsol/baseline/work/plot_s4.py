"""Scientific comparison figures. Experimental JPEG pseudocolor is inverted,
not presented as raw camera counts. All panels share pixel scale and response.
"""
from pathlib import Path
import argparse,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap,LogNorm
from audit_panel_alignment import require_matplotlib_panel_alignment
from extract_experiment import contour_mask
ROOT=Path(__file__).resolve().parents[1];FIG=ROOT/'figures';FIG.mkdir(exist_ok=True)
plt.rcParams.update({'font.family':'Arial','font.size':8,'axes.linewidth':.6,'pdf.fonttype':42,'ps.fonttype':42,'svg.fonttype':'none','savefig.dpi':300})

def save(fig,name):
 fig.canvas.draw();require_matplotlib_panel_alignment(fig,json_out=FIG/(name+'.alignment.json'))
 fig.savefig(FIG/(name+'.pdf'));fig.savefig(FIG/(name+'.svg'));fig.savefig(FIG/(name+'.png'),dpi=300);plt.close(fig)

def experiment():
 e=np.load(ROOT/'experiment/S4_reconstructed.npz');cm=ListedColormap(e['lut_rgb']/255)
 fig,axes=plt.subplots(6,5,figsize=(7.1,8.8),gridspec_kw={'wspace':.10,'hspace':.32})
 for i,ax in enumerate(axes.flat):
  ax.set_xticks([]);ax.set_yticks([]);ax.set_box_aspect(82/84)
  if i<29:
   ax.imshow(e['gray'][i],cmap=cm,vmin=0,vmax=255,interpolation='nearest');ax.set_title(f"{e['times_ns'][i]:g} ns",pad=3)
   mask=contour_mask(e['gray'][i],27)
   if mask.any():ax.contour(mask,levels=[.5],colors=['white'],linewidths=.5)
  else:
   ax.set_xlim(0,84);ax.set_ylim(0,82);ax.plot([20,56],[40,40],color='k',lw=2);ax.text(38,46,'400 μm',ha='center');ax.axis('off')
 fig.subplots_adjust(left=.025,right=.975,bottom=.04,top=.94)
 fig.suptitle('S4 reconstruction | white contour: grayscale 27',y=.985,fontsize=10)
 fig.text(.5,.01,'Published pseudocolor JPEG; 11.11 μm/pixel; scale uncertainty ±2.8%.',ha='center',fontsize=7)
 save(fig,'S4_experiment_reconstruction')

def comparison(case):
 e=np.load(ROOT/'experiment/S4_reconstructed.npz');a=np.load(ROOT/f'results/{case}_camera/images.npz');fit=json.loads((ROOT/f'results/{case}_camera/calibration.json').read_text());cm=ListedColormap(e['lut_rgb']/255)
 audit=json.loads((ROOT/f'results/{case}_audit.json').read_text());status='' if audit['status']=='passed' else ' | DIAGNOSTIC'
 choices=[0,30,60,80,150,260,400,580,740];fig,axes=plt.subplots(2,len(choices),figsize=(10.5,3.5),gridspec_kw={'wspace':.07,'hspace':.12})
 for j,t in enumerate(choices):
  i=int(np.flatnonzero(a['times_ns']==t)[0]);obs=a['experiment'][i];sim=a['475_525_psf1'][i]
  for k,img in enumerate([obs,sim]):
   ax=axes[k,j];ax.set_xticks([]);ax.set_yticks([])
   ax.imshow(img,cmap=cm if k<2 else 'RdBu_r',vmin=0 if k<2 else -255,vmax=255,interpolation='nearest')
   if k<2:
    mask=contour_mask(img,27)
    if mask.any():ax.contour(mask,levels=[.5],colors=['white'],linewidths=.4)
   if k==0:ax.set_title(f'{t} ns',fontsize=8,pad=4)
 for k,label in enumerate(['Experiment','LTE camera']):axes[k,0].set_ylabel(label,fontsize=8)
 fig.subplots_adjust(left=.07,right=.99,top=.82,bottom=.20)
 fig.suptitle(f'{case}: S4 / LTE | 10 ns gate, 475–525 nm, one frozen gain'+status,y=.97,fontsize=10)
 fig.text(.5,.90,f"Time offset {fit['time_shift_ns']:g} ns; 0–80 ns calibration; 150–740 ns holdout",ha='center',fontsize=8)
 fig.canvas.draw()
 box=axes[0,0].get_position();bar_width=box.width*36/84
 fig.add_artist(plt.Line2D([.12,.12+bar_width],[.12,.12],transform=fig.transFigure,color='black',lw=1.5))
 fig.text(.12+bar_width/2,.09,'400 μm',ha='center',fontsize=6)
 fig.text(.57,.115,'Both rows: one 0–255 scale, one physical pixel size',ha='center',fontsize=7)
 fig.text(.5,.075,'Same scale: 11.11 μm/pixel. Surface is unmarked in experiment; vertical origin is provisional.',ha='center',fontsize=7)
 fig.text(.5,.025,'Fitted mean response: check the photon-scale audit before interpreting apparent agreement.',ha='center',fontsize=7)
 save(fig,case+'_S4_comparison')
 # Full 29-frame comparison is retained, not replaced by selected frames.
 for field,label in [('experiment','experiment'),('475_525_psf1','simulation')]:
  fig,axes=plt.subplots(6,5,figsize=(7.1,8.8),gridspec_kw={'wspace':.1,'hspace':.32})
  for i,ax in enumerate(axes.flat):
   ax.set_xticks([]);ax.set_yticks([]);ax.set_box_aspect(82/84)
   if i<29:ax.imshow(a[field][i],cmap=cm,vmin=0,vmax=255,interpolation='nearest');ax.set_title(f"{a['times_ns'][i]:g} ns",pad=3)
   else:
    ax.set_xlim(0,84);ax.set_ylim(0,82);ax.plot([20,56],[40,40],color='k',lw=2);ax.text(38,46,'400 μm',ha='center');ax.axis('off')
  fig.subplots_adjust(left=.025,right=.975,bottom=.04,top=.94);fig.suptitle(f'{case} | {label} | frozen linear response'+status,fontsize=10,y=.985)
  save(fig,case+'_'+label+'_all29')
 # Animation has fixed physical dimensions, scale, color and response.
 from matplotlib.animation import FuncAnimation,PillowWriter
 fig,axes=plt.subplots(1,2,figsize=(6.5,3.5));artists=[]
 for ax,field,title in zip(axes,['experiment','475_525_psf1'],['S4 experiment',f'LTE {case}']):
  artists.append(ax.imshow(a[field][0],cmap=cm,vmin=0,vmax=255,interpolation='nearest'));ax.set_title(title);ax.set_xticks([]);ax.set_yticks([])
 stamp=fig.suptitle('0 ns');fig.subplots_adjust(left=.02,right=.98,top=.88,bottom=.08,wspace=.04)
 def frame(i):
  artists[0].set_data(a['experiment'][i]);artists[1].set_data(a['475_525_psf1'][i]);stamp.set_text(f"{a['times_ns'][i]} ns | frozen camera response");return [*artists,stamp]
 durations=np.r_[np.diff(a['times_ns']),10].astype(int)*10
 class SampleTimedWriter(PillowWriter):
  def finish(self):
   self._frames[0].save(self.outfile,save_all=True,append_images=self._frames[1:],duration=durations.tolist(),loop=0)
 movie=FuncAnimation(fig,frame,frames=len(a['times_ns']),interval=100,blit=False);movie.save(FIG/(case+'_S4.gif'),writer=SampleTimedWriter(fps=10),dpi=300);plt.close(fig)
 (FIG/(case+'_animation_timing.json')).write_text(json.dumps({'observed_times_ns':a['times_ns'].tolist(),'frame_duration_ms':durations.tolist(),'playback_scale_ms_per_physical_ns':10,'last_frame_hold_ns':10,'intermediate_experimental_frames_generated':False},indent=2))

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('case',nargs='?');a=p.parse_args();experiment() if not a.case else comparison(a.case)
