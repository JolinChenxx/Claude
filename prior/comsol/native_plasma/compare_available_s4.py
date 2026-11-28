"""S4 first-frame comparison with only mesh-screened P85 data.
No exposure extrapolation, radiometric fitting, translation or gain freeze.
"""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.font_manager import FontProperties
from scipy.ndimage import label
R=Path(__file__).resolve().parent
OLD=R.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
sys.path.insert(0,str(OLD/'work'))
from camera_s4 import gate,blur
font=FontProperties(fname='C:/Windows/Fonts/msyh.ttc')
plt.rcParams.update({'font.family':font.get_name(),'axes.unicode_minus':False,'font.size':10,'pdf.fonttype':42})
e=np.load(OLD/'experiment/S4_reconstructed.npz')
a=np.load(R/'results/P85_valid_radiation/radiance.npz')
t=a['times_s']*1e9
assert t[0]>=113-1e-6 and t[-1]<=119+1e-6
k=np.argmin(abs(t-118));assert abs(t[k]-118)<1e-6
instant=blur(a['radiance'][k:k+1],1)[0]
partial=blur(gate(t,a['radiance'],[116],width=6),1)[0]
gray=e['gray'][0].astype(float);bg=float(np.median(np.r_[gray[:5,:5].ravel(),gray[:5,-5:].ravel()]))
obs=np.maximum(gray-bg,0)
fields=[obs,instant,partial]
norm=[v/v.max() for v in fields]
px=float(e['pixel_um']);x=a['x_um'];z=a['z_um'];extent=[x[0]-px/2,x[-1]+px/2,0,z[0]+px/2]
fig=plt.figure(figsize=(16,7.6),facecolor='white')
gs=fig.add_gridspec(2,4,height_ratios=[5,1],left=.06,right=.95,top=.83,bottom=.14,hspace=.36,wspace=.22)
axs=[fig.add_subplot(gs[0,i]) for i in range(4)]
axs[0].imshow(e['rgb'][0],extent=extent,origin='upper',interpolation='nearest')
axs[0].set_title('(a) 原论文 S4 首帧\n实验 0 ns，曝光 10 ns',fontsize=11,pad=10)
for ax,v,title in zip(axs[1:],norm,['(b) 实验：重建灰度\n扣背景后独立归一化','(c) 模型：118 ns 瞬时\n对应实验时标 0 ns','(d) 模型：113–119 ns 平均\n仅覆盖首帧曝光的前 6 ns']):
 im=ax.imshow(v,extent=extent,origin='upper',interpolation='nearest',vmin=0,vmax=1,cmap='inferno')
 ax.set_title(title,fontsize=11,pad=10)
for ax in axs:
 ax.set(xlabel='横向位置 x (μm)',xlim=(extent[0],extent[1]),ylim=(0,extent[3]))
 ax.set_xticks([-400,-200,0,200,400]);ax.set_yticks([0,200,400,600,800]);ax.tick_params(labelsize=9)
axs[0].set_ylabel('竖向坐标 (μm；原点说明见图下注)')
for ax in axs[1:]:ax.set_yticklabels([])
cax=fig.add_axes([.961,.344,.009,.39]);fig.colorbar(im,cax=cax,label='各图自身峰值归一化强度')
ax=fig.add_subplot(gs[1,:]);ax.set(xlim=(111,125),ylim=(-.5,1.3),xlabel='模型时间 (ns)',yticks=[])
ax.barh(.7,10,left=113,height=.25,color='#d9dee5');ax.text(118,1.02,'S4 首帧要求：113–123 ns',ha='center',fontsize=10)
ax.barh(.7,6,left=113,height=.25,color='#4d91c6');ax.text(116,.7,'已有可用区间 6 ns',ha='center',va='center',color='white',fontsize=9)
ax.text(121,.7,'缺少有效解 4 ns',ha='center',va='center',fontsize=9)
ax.plot([118],[.15],marker='D',color='#cd533b',markersize=6);ax.text(118,.02,'瞬时图取 118 ns',ha='center',va='top',fontsize=9)
for s in ['top','right','left']:ax.spines[s].set_visible(False)
fig.suptitle('方法一与原论文 S4：现有数据的空间形态对比',fontsize=18,y=.97)
fig.text(.5,.895,'相同空间比例尺  |  模型通带 475–525 nm、视线积分与 1 像素点扩散  |  不比较绝对亮度',ha='center',fontsize=11,color='#444')
fig.text(.06,.065,'实验图来自论文 JPEG 裁剪；(b–d) 各自归一化仅用于比较形态，不是统一相机增益标定。',fontsize=10)
fig.text(.06,.031,'S4 未标出独立靶面位置，实验竖向原点暂取图像底部；模型原点为初始铜面。瞬时 / 6 ns 平均均不等同于完整 10 ns 曝光。',fontsize=9,color='#555')
out=R/'figures/P85_valid';out.mkdir(exist_ok=True)
for ext in ['png','pdf']:fig.savefig(out/f'S4_available_data_comparison.{ext}',dpi=220)
rows=[]
for name,v in zip(['experiment_reconstructed','model_118ns_instant','model_113_119ns_average'],norm):
 ids,n=label(v>=.1);sizes=np.bincount(ids.ravel());sizes[0]=0;mask=ids==sizes.argmax();iy,ix=np.where(mask)
 rows.append({'image':name,'threshold_fraction_of_own_peak':.1,'half_width_um':float((ix.max()-ix.min()+1)*px/2),'area_um2':float(mask.sum()*px*px)})
report={'case':'P85_valid','comparison':'qualitative shape only; exposure incomplete','experiment_ns':0,'time_offset_ns':118,'requested_gate_model_ns':[113,123],'available_gate_model_ns':[113,119],'instant_model_ns':118,'reconstructed_background':bg,'normalization':'independent maximum per image; no fitted gain','instant_peak_radiance_W_m2_sr':float(instant.max()),'partial_mean_peak_radiance_W_m2_sr':float(partial.max()),'shape_metrics':rows,'limitations':['No full 10 ns matched exposure','No independent target-plane registration','Saved states passed mesh sign screening, not convergence validation','Radiation omits Stark/natural wings and near-surface submicrometre export detail']}
(R/'results/S4_available_data_comparison.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
