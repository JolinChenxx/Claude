"""Plot actual saved copper energy/source histories, without a plasma proxy."""
from pathlib import Path
import sys,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
plt.rcParams.update({'font.sans-serif':['Microsoft YaHei','SimHei','DejaVu Sans'],'axes.unicode_minus':False})
case=sys.argv[1]
h=np.genfromtxt(R/'results'/f'{case}_melt_history.csv',delimiter=',',names=True)
d=np.genfromtxt(R/'results'/f'{case}_surface.csv',delimiter=',',names=True)
names=h.dtype.names;t=h['time_s'];dt=np.diff(t)
def cum(y):return np.r_[0,np.cumsum((y[1:]+y[:-1])*dt/2)]
laser=cum(h[names[4]]);emitted=cum(h[names[5]]);stored=h[names[2]]-h[names[2]][0]
recoil=cum(h[names[6]]);mass=cum(h[names[3]])
fig,ax=plt.subplots(2,2,figsize=(11.5,7.7),layout='constrained')
for a,label in [(laser,'靶面吸收'),(stored,'铜内热能与动能增量'),(emitted,'净送出焓与动能')]:ax[0,0].plot(t*1e9,a*1e6,label=label)
ax[0,0].legend(fontsize=9);ax[0,0].set(ylabel='能量 / μJ',title='实际计算的靶面能量分配')
ax[0,1].plot(t*1e9,mass*1e12);ax[0,1].set(ylabel='净移除铜质量 / ng',title='由实际蒸发通量积分；不代表相爆抛出')
ax[1,0].plot(t*1e9,h[names[3]]*1e3);ax[1,0].set(ylabel='净铜供料率 / kg/s × 10³',title='向羽流传递的质量源')
ax[1,1].plot(t*1e9,(laser-stored-emitted-recoil)*1e6)
ax[1,1].set(ylabel='能量差 / μJ',title='吸收 − 储存 − 送出 − 反冲功（部分能量账）')
for a in ax.flat:a.set_xlabel('模型时间 / ns');a.grid(alpha=.2)
fig.suptitle(f'{case}：保存解 {t[0]*1e9:.0f}–{t[-1]*1e9:.0f} ns 的铜供料与能量\n尚无羽流反馈；高温物性未验证；部分能量账尚未计全表面能与边界功',fontsize=12)
out=R/'figures'/case;out.mkdir(exist_ok=True);fig.savefig(out/'copper_energy_supply.png',dpi=170);plt.close(fig)
summary={'saved_start_ns':float(t[0]*1e9),'saved_end_ns':float(t[-1]*1e9),'target_absorbed_uJ':float(laser[-1]*1e6),'stored_change_uJ':float(stored[-1]*1e6),'net_emitted_uJ':float(emitted[-1]*1e6),'recoil_work_uJ':float(recoil[-1]*1e6),'partial_energy_difference_uJ':float((laser-stored-emitted-recoil)[-1]*1e6),'complete_energy_validation':False}
(R/'results'/f'{case}_energy_supply.json').write_text(json.dumps(summary,indent=2));print(summary)
