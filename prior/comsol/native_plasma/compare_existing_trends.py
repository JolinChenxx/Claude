"""Read-only cross-model evidence at the route1 fixed 118 ns convention.

No new solution, kinetic rate fit, time fit, or camera gain is introduced.
"""
from pathlib import Path
import sys,json,csv
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import LogNorm
from matplotlib.font_manager import FontProperties
from scipy.ndimage import gaussian_filter
R=Path(__file__).resolve().parent;OTHER=R.parent/'route2/results'
OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(OLD/'work'))
from camera_s4 import gate as gate_average
from extract_experiment import contour_mask
font=FontProperties(fname='C:/Windows/Fonts/msyh.ttc')
plt.rcParams.update({'font.family':font.get_name(),'axes.unicode_minus':False,'font.size':10})
exp=np.load(OLD/'experiment/S4_reconstructed.npz');times=exp['times_ns'];obs=exp['gray'];px=float(exp['pixel_um']);h,w=obs.shape[1:]
omega=2*np.pi*(1-np.sqrt(1-.4**2));factor=omega*(px*1e-6)**2*10e-9
out=R/'figures/extension_reference';out.mkdir(exist_ok=True)
def series(case,key):
    p=OTHER/'R2_B64A_LTE';tb=np.load(p/'axes.npz')['times_s'];vb=np.load(p/(key+'.npy'),mmap_mode='r')[:,0]
    if case=='LTE':return tb,vb
    p=OTHER/(case+'_radiation');tc=np.load(p/'axes.npz')['times_s'];vc=np.load(p/(key+'.npy'),mmap_mode='r')[:,0];before=tb<tc[0]-1e-15
    return np.r_[tb[before],tc],np.concatenate([vb[before],vc])
def widths(im):
    total=im.sum()
    if total<=0:return np.nan,np.nan
    x=np.interp([.05,.95],np.cumsum(im.sum(0))/total,(np.arange(w)+.5-w/2)*px)
    z=np.interp([.05,.95],np.cumsum(im[::-1].sum(1))/total,(np.arange(h)+.5)*px)
    return x[1]-x[0],z[1]-z[0]
cases=['LTE','R2_COUPLED','R2_COUPLED_Q','R2_COUPLED_NT']
labels=['旧 LTE','双温：无空气淬灭','双温：几何淬灭上限','最大辐射损失边界']
stacks={};rows=[];curves={}
for case in cases:
    t,p=series(case,'photon_radiance');g=gate_average(t*1e9,p,times+118)
    g=gaussian_filter(g,[0,1/2.354820045,1/2.354820045],mode='constant')*factor
    mid=g.shape[-1]//2;g=g[:,-h:,mid-w//2:mid+(w-w//2)];stacks[case]=g
    curves[case]=[]
    for tt,im in zip(times,g):
        width,height=widths(im);row=dict(case=case,time_ns=float(tt),photons=float(im.sum()),width90_um=width,height90_um=height)
        rows.append(row);curves[case].append(row)
observed=np.maximum(obs.astype(float)-2,0)
# Published gray-27 support removes the camera/JPEG background from shape.
for i in range(len(observed)):observed[i]*=contour_mask(obs[i],27)
ov=np.array([widths(im) for im in observed]);ot=observed.sum((1,2))
fig,ax=plt.subplots(1,3,figsize=(15,4.7),layout='constrained')
for case,label in zip(cases,labels):
    rr=curves[case];phot=np.array([q['photons'] for q in rr]);good=phot>=1
    ax[0].plot(times,np.where(good,[q['width90_um'] for q in rr],np.nan),'.-',label=label)
    ax[1].semilogy(times,np.maximum(phot/phot.max(),1e-30),'.-',label=label)
    ax[2].semilogy(times,np.maximum(phot,1e-30),'.-',label=label)
ax[0].plot(times,ov[:,0],'ko-',label='S4（灰度≥27）')
ax[1].semilogy(times,ot/ot.max(),'ko-',label='S4 相对亮度')
for a,title,y in zip(ax,['亮区扩张','整段序列相对亮度','计算的物理光子量'],['90% 信号宽度（μm）','各序列统一峰值归一化','理想光子 / 10 ns']):
    a.set(title=title,xlabel='实验时间（ns）',ylabel=y);a.grid(alpha=.2);a.legend(fontsize=8)
ax[1].set_ylim(1e-5,2);ax[2].set_ylim(1e-3,1e7);ax[2].axhline(1,color='gray',ls=':')
fig.suptitle('已有完整时间窗的机制参照：统一 118 ns 时移、475–525 nm、10 ns 曝光\n此图读取既有简化模型，并非本轮原生 Plasma 新结果；数值 120 ns 前沿用 LTE')
fig.savefig(out/'existing_trend_comparison.png',dpi=170);plt.close(fig)
chosen=np.array([np.argmin(abs(times-v)) for v in [0,30,80,150,260,400,580,740]])
fig,axes=plt.subplots(3,len(chosen),figsize=(16,7.5),layout='constrained')
norm=LogNorm(1e-3,1e4)
for j,i in enumerate(chosen):
    axes[0,j].imshow(obs[i],vmin=0,vmax=255,cmap='inferno');axes[0,j].set_title(f'{times[i]:g} ns')
    for row,case in enumerate(['R2_COUPLED','R2_COUPLED_Q'],1):
        im=stacks[case][i];artist=axes[row,j].imshow(np.maximum(im,1e-20),norm=norm,cmap='inferno')
        axes[row,j].text(.03,.97,f'{im.sum():.2g} 光子',transform=axes[row,j].transAxes,va='top',color='white',fontsize=8)
for a in axes.flat:a.set_xticks([]);a.set_yticks([])
for row,label in enumerate(['论文 S4','无空气淬灭','几何淬灭上限']):axes[row,0].set_ylabel(label)
fig.colorbar(artist,ax=axes[1:].ravel().tolist(),label='同一固定色标：理想光子 / 像素 / 10 ns',fraction=.015,pad=.01)
fig.suptitle('已有非平衡模型的扩张与消退参照（尚未匹配实验）\n统一空间尺度与 118 ns 时移；靶面位置未独立标定；两种淬灭边界均不是拟合速率')
fig.savefig(out/'existing_trend_frames.png',dpi=170);plt.close(fig)
with (R/'results/extension_existing_trends.csv').open('w',newline='') as f:
    writer=csv.DictWriter(f,fieldnames=rows[0]);writer.writeheader();writer.writerows(rows)
summary={'source':'Read-only existing route2 models, no new solve','shift_ns':118,'frames':len(times),'full_gate_coverage':True,'caveats':['Inherited LTE until model120ns; the first route1 gate crosses that change','The no-quench and geometric-quench models are mechanism bounds, not calibrated rates','Not native Plasma electron transport or Poisson','NT uses maximal local radiation loss; not self-consistent global transfer'],'cases':{}}
for case in cases:
    rr=curves[case];ph=np.array([q['photons'] for q in rr]);summary['cases'][case]={'photon_peak_experiment_ns':float(times[ph.argmax()]),'last_frame_photons':float(ph[-1]),'photons_at_150ns':float(ph[np.argmin(abs(times-150))]),'max_width90_um_above_one_photon':float(max(q['width90_um'] for q in rr if q['photons']>=1))}
(R/'results/extension_existing_trends.json').write_text(json.dumps(summary,indent=2));print(json.dumps(summary,indent=2))
