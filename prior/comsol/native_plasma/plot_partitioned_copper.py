from pathlib import Path
import sys,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent;case=sys.argv[1]
feedback_meta=R/'results'/f'{case}_coupling_iteration.json'
feedback=json.loads(feedback_meta.read_text(encoding='utf-8')) if feedback_meta.exists() else {}
feedback_note='已接入实际气相反馈；尚未达到双向迭代收敛' if feedback else '环境气压初始迭代；尚未接回羽流反馈'
plt.rcParams.update({'font.sans-serif':['Microsoft YaHei','SimHei','DejaVu Sans'],'axes.unicode_minus':False})
d=np.genfromtxt(R/'results'/f'{case}_surface.csv',delimiter=',',names=True)
h=np.genfromtxt(R/'results'/f'{case}_melt_history.csv',delimiter=',',names=True)
ts=np.unique(d['time_s']);names=h.dtype.names
valid=np.array([all(np.isfinite(d[d['time_s']==t][n]).all() for n in ['r_m','z_m','T_K','ur_m_s','uz_m_s','mdot_kg_m2_s']) for t in ts])
if not valid.all():raise ValueError('Nonfinite saved endpoint; trim actual solution before plotting')
out=R/'figures'/case;out.mkdir(exist_ok=True)
selected=np.unique(np.searchsorted(ts,np.array([60,80,90,100,120,150,180,203])*1e-9).clip(0,len(ts)-1))
fig,ax=plt.subplots(2,2,figsize=(11.5,8),layout='constrained')
for k in selected:
 a=d[d['time_s']==ts[k]];rr,ix=np.unique(a['r_m'],return_index=True);a=a[ix];use=rr<=40e-6
 ax[0,0].plot(rr[use]*1e6,a['z_m'][use]*1e6,label=f'{ts[k]*1e9:.0f} ns')
 ax[0,1].plot(rr[use]*1e6,a['T_K'][use],label=f'{ts[k]*1e9:.0f} ns')
ax[0,0].axhline(0,c='k',lw=.6);ax[0,0].set(xlabel='半径 / μm',ylabel='相对初始铜面的高度 / μm',title='实际 ALE 液面，不指定上升轨迹');ax[0,0].legend(ncol=2,fontsize=8)
ax[0,1].axhline(6550,c='crimson',ls='--',label='文献临界温度估计下端，非触发开关');ax[0,1].set(xlabel='半径 / μm',ylabel='铜表面温度 / K',title='高温段材料闭合尚未验证')
maxT=np.array([d[d['time_s']==t]['T_K'].max() for t in ts]);top=np.array([d[d['time_s']==t]['z_m'].max() for t in ts]);bottom=np.array([d[d['time_s']==t]['z_m'].min() for t in ts]);speed=np.array([np.hypot(d[d['time_s']==t]['ur_m_s'],d[d['time_s']==t]['uz_m_s']).max() for t in ts])
ax[1,0].plot(ts*1e9,top*1e6,label='表面最高点');ax[1,0].plot(ts*1e9,bottom*1e6,label='凹坑底');ax[1,0].set(xlabel='模型时间 / ns',ylabel='高度 / μm',title='液面抬升与表面退缩分开统计');ax[1,0].legend()
ax[1,1].plot(ts*1e9,speed);ax[1,1].set(xlabel='模型时间 / ns',ylabel='最大熔铜速度 / m/s',title='计算得到的熔体运动')
for a in ax.flat:a.grid(alpha=.2)
fig.suptitle(f'{case}：铜运动分块计算；{feedback_note}\n本图不含等离子体发光，不是 S4 对照；模型时间90 ns暂对应曝光零点',fontsize=12)
fig.savefig(out/'copper_motion.png',dpi=170);plt.close(fig)
dt=np.diff(ts);flux=h[names[3]];cum=np.r_[0,np.cumsum((flux[1:]+flux[:-1])/2*dt)]
mass=h[names[1]];res=mass-mass[0]+cum
report=dict(case=case,saved_start_ns=float(ts[0]*1e9),saved_end_ns=float(ts[-1]*1e9),saved_states=len(ts),real_moving_surface=True,plasma_solved=False,two_way_converged=False,max_surface_T_K=float(maxT.max()),max_rise_um=float(top.max()*1e6),max_recession_um=float(bottom.min()*1e6),max_melt_speed_m_s=float(speed.max()),evaporated_mass_ng=float(cum[-1]*1e12),mass_residual_kg=float(res[-1]),mass_residual_percent_of_evaporated=float(res[-1]/abs(cum[-1])*100) if abs(cum[-1])>1000*np.finfo(float).eps*mass[0] else None,first_T_above6550_model_ns=float(ts[np.flatnonzero(maxT>=6550)[0]]*1e9) if np.any(maxT>=6550) else None,high_temperature_material_validated=False,figure=str(out/'copper_motion.png'),limitations=[('Actual gas feedback iterate; not converged' if feedback else 'Ambient gas first iterate; no plasma shielding or return flux yet'),'Inherited high-temperature Cu closure; values beyond its support are diagnostic','Single-valued continuous surface; no droplet detachment'])
(R/'results'/f'{case}_copper_summary.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False),flush=True)
