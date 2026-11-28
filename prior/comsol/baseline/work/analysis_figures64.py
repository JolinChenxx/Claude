"""Explanatory figures and arithmetic from saved solutions. No solver launches."""
from pathlib import Path
import json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.font_manager import FontProperties
from matplotlib.patches import FancyBboxPatch

R=Path(__file__).resolve().parents[1]; F=R/'figures/analysis64'; F.mkdir(parents=True,exist_ok=True)
fp=FontProperties(fname='C:/Windows/Fonts/msyh.ttc')
plt.rcParams.update({'font.family':fp.get_name(),'axes.unicode_minus':False,'font.size':11,'savefig.dpi':190})
BLUE='#176887'; ORANGE='#d57038'; GREEN='#367d67'

def save(fig,name):
    fig.savefig(F/name,bbox_inches='tight',facecolor='white');plt.close(fig)

def eq(name,lines):
    fig=plt.figure(figsize=(10,0.58*len(lines)+.20))
    for i,line in enumerate(lines):
        line=line.replace(r'\mathbf u',r'\mathbf{u}').replace(r'\frac12',r'\frac{1}{2}').replace(r'\frac E\rho',r'\frac{E}{\rho}').replace(r'\mathcal D',r'\mathcal{D}')
        fig.text(.025,1-(i+.78)/(len(lines)+.2),line,fontsize=15,color='#15354b')
    save(fig,name)

def equations():
    eq('eq_flow.png',[
      r'$\partial_t\rho+\nabla\!\cdot(\rho\mathbf{u})=\nabla\!\cdot(D_{\rm num}\nabla\rho)$',
      r'$\partial_t(\rho\mathbf{u})+\nabla\!\cdot(\rho\mathbf{u}\mathbf{u})+\nabla p=\nabla\!\cdot[D_{\rm num}\nabla(\rho\mathbf{u})]$',
      r'$\partial_t E+\nabla\!\cdot[(E+p)\mathbf{u}]=\nabla\!\cdot(D_{\rm num}\nabla E+k_g\nabla T)+Q_L-Q_{\rm ff}$',
      r'$\partial_t(\rho Y)+\nabla\!\cdot(\rho Y\mathbf{u})=\nabla\!\cdot[D_{\rm num}\nabla(\rho Y)]$'])
    eq('eq_diff.png',[
      r'$D_{\rm num}=2h(|\mathbf{u}|+c_b)+D_f,\qquad c_b=\sqrt{1.667p/\rho}$',
      r'$D_f=h(|\mathbf{u}|+c_b)e^{-(Y/0.02)^2}\,\frac{h|\nabla Y|}{0.02+h|\nabla Y|}$',
      r'$\ell_d\sim\sqrt{D_{\rm num}t},\qquad h\downarrow\ \Rightarrow\ D_{\rm num}\downarrow$'])
    eq('eq_laser.png',[
      r'$I(r,t)=I_0e^{-2r^2/w_0^2}e^{-4\ln2[(t-t_p)/\tau]^2}$',
      r'$I_0=\frac{2E_{\rm in}}{\pi w_0^2}\frac{\sqrt{4\ln2/\pi}}{\tau}$',
      r'$\frac{dI}{ds}=-\alpha_{\rm IB}I,\quad Q_L=\alpha_{\rm IB}(I_\downarrow+I_\uparrow),\quad q_{\rm abs}=0.1I_\downarrow$'])
    eq('eq_evap.png',[
      r'$p_{\rm sat}=p_a\exp\!\left[\frac{m_{\rm Cu}L_v}{k_B}\left(\frac{1}{2840\,{\rm K}}-\frac{1}{T_s}\right)\right]$',
      r'$\dot m_e=p_{\rm sat}\sqrt{\frac{m_{\rm Cu}}{2\pi k_BT_s}},\quad\dot m_c=\rho Y(1-x_i)\sqrt{\frac{k_BT}{2\pi m_{\rm Cu}}}$',
      r'$\dot m=\dot m_e-\dot m_c,\qquad\partial_t d=\dot m\sqrt{1+(\partial_r d)^2}/\rho_{\rm Cu}$'])
    eq('eq_ale.png',[
      r'$r=R,\quad z=Z-s(R,Z,t),\quad J=1-\partial_Zs,\quad w_z=-\partial_ts$',
      r'$\partial_t(JU)+\partial_R(JF_r)+\partial_Z(s_RF_r+F_z-w_zU)=JS$'])
    eq('eq_temp.png',[
      r'$E=\rho e_{\rm th}+\frac12\rho|\mathbf u|^2+\rho YB_{\rm Cu}$',
      r'$e_{\rm th}=\frac E\rho-\frac12|\mathbf u|^2-YB_{\rm Cu},\quad B_{\rm Cu}=5.8151\times10^6\,{\rm J\,kg^{-1}}$',
      r'$(T,p,n_e,x_i)=\operatorname{EOS}_{\rm LTE}(e_{\rm th},\rho,Y)$',
      r'$\rho\frac{De_{\rm th}}{Dt}\simeq-p\nabla\!\cdot\mathbf u+\nabla\!\cdot(k_g\nabla T)+Q_L-Q_{\rm ff}+\mathcal D$'])
    eq('eq_lte.png',[
      r'$Z_s(T)=\sum_i g_i e^{-E_i/(k_BT)},\quad n_u=n_s\frac{g_u}{Z_s(T)}e^{-E_u/(k_BT)}$',
      r'$\frac{n_{s+1}n_e}{n_s}=2\left(\frac{2\pi m_ek_BT}{h_P^2}\right)^{3/2}\frac{Z_{s+1}}{Z_s}e^{-\chi_s/(k_BT)}$',
      r'$j_{ul}=\frac{h_P\nu_{ul}}{4\pi}A_{ul}n_u,\quad\frac{\partial\ln j}{\partial\ln T}\simeq\frac{E_u}{k_BT}$'])
    eq('eq_nlte.png',[
      r'$\partial_t n_i+\nabla\!\cdot(n_i\mathbf u)=\sum_{j\ne i}(n_jR_{ji}-n_iR_{ij})$',
      r'$\tau_{\rm rec}\sim(\alpha_{\rm rec}n_e)^{-1},\quad\tau_{\rm exp}\sim L/u,\quad\tau_T\sim T/|DT/Dt|$',
      r'$\mathrm{LTE}:\quad\tau_{\rm relaxation}\ll\min(\tau_{\rm exp},\tau_T)$'])
    eq('eq_los.png',[
      r'$r=\sqrt{x^2+y^2},\quad\frac{dI_\nu}{dy}=j_\nu-\alpha_\nu I_\nu,\quad\alpha_\nu=j_\nu/B_\nu(T)$',
      r'$I_{\nu,\rm out}=I_{\nu,\rm in}e^{-\Delta\tau}+\frac{j_\nu}{\alpha_\nu}(1-e^{-\Delta\tau})$',
      r'$I(x,z)\simeq2\int_x^{R}\frac{j(r,z)r}{\sqrt{r^2-x^2}}\,dr\qquad(\tau\ll1,\ x\geq0)$'])
    eq('eq_camera.png',[
      r'$\bar I(x,z,t_e)=\frac{1}{10\,{\rm ns}}\int_{t_e+118\,{\rm ns}-5\,{\rm ns}}^{t_e+118\,{\rm ns}+5\,{\rm ns}}I_{475-525}(x,z,t)\,dt$',
      r'$C=\operatorname{clip}_{[0,255]}\{2+G[\operatorname{PSF}*\bar I]\},\quad G=4.4200\times10^{67}$',
      r'$I_{\rm threshold}=\frac{27-2}{G}=5.66\times10^{-67}\,{\rm W\,m^{-2}\,sr^{-1}}$'])
    eq('eq_shape.png',[
      r'$I(x,z)=2j\sqrt{R^2-x^2-z^2},\quad z\geq0$',
      r'$x^2+z^2\leq R^2-\left[\frac{C_{\rm th}-b}{2Gj}\right]^2$'])
    eq('eq_photon.png',[
      r'$N_{\rm pix}\lesssim\Omega A_{\rm pix}\Delta t\,\bar I\,\frac{525\,{\rm nm}}{h_Pc},\quad\Omega=2\pi(1-\sqrt{1-\mathrm{NA}^2})$',
      r'$N_{\rm all}\leq\frac{781\,{\rm nm}}{h_Pc}\int_{\rm gate}P_{380-780}(t)\,dt$'])

def causal():
    fig,ax=plt.subplots(figsize=(10,4));ax.set(xlim=(0,10),ylim=(0,4));ax.axis('off')
    entries=[(.15,2.6,'激光与铜靶','吸收、传热、蒸发'),(3.5,2.6,'流场与温度','守恒PDE + LTE状态方程'),(6.85,2.6,'原子发光','电离、激发、辐射转移'),(6.85,.45,'相机图像','10 ns曝光 + 增益 + 饱和'),(3.5,.45,'实验对照','尺寸、形状、亮度、时序'),(.15,.45,'问题定位','发光缺失与成像失真分开查')]
    for x,y,title,sub in entries:
        ax.add_patch(FancyBboxPatch((x,y),2.9,1,boxstyle='round,pad=.03',facecolor='#eaf3f6',edgecolor=BLUE))
        ax.text(x+1.45,y+.67,title,ha='center',va='center',color=BLUE,fontsize=14,weight='bold');ax.text(x+1.45,y+.28,sub,ha='center',fontsize=10)
    for x1,y1,x2,y2 in [(3.08,3.1,3.45,3.1),(6.43,3.1,6.80,3.1),(8.3,2.55,8.3,1.5),(6.8,.95,6.45,.95),(3.45,.95,3.1,.95)]:ax.annotate('',(x2,y2),(x1,y1),arrowprops=dict(arrowstyle='->',color=ORANGE,lw=2))
    save(fig,'causal.png')

def thermal():
    a=np.loadtxt(R/'results/B64A_max.csv',delimiter=',',skiprows=1)
    v=np.loadtxt(R/'results/B64A_visible_budget.csv',delimiter=',',skiprows=1)
    t=a[:,0]*1e9-118
    fig,axs=plt.subplots(2,1,figsize=(10,6),sharex=True,layout='constrained')
    axs[0].plot(t,a[:,1],color=ORANGE,lw=2,label='全气体域最高温度');axs[0].set(ylabel='最高温度 / K',ylim=(0,7500));axs[0].legend(loc='upper right')
    for te in [60,150,260,400]:
        i=np.argmin(abs(t-te));axs[0].scatter(t[i],a[i,1],color=BLUE);axs[0].annotate(f'{te} ns: {a[i,1]:.0f} K',(t[i],a[i,1]),xytext=(5,12),textcoords='offset points',fontsize=10)
    axs[1].semilogy(v[:,0]*1e9-118,np.maximum(v[:,1]+v[:,2],1e-90),color=BLUE,lw=2,label='全域380–780 nm原子谱线 + 自由-自由发射功率')
    axs[1].set(xlim=(-118,750),ylim=(1e-70,1e7),xlabel='对应实验时间 / ns（数值时间 − 118 ns）',ylabel='发射功率 / W');axs[1].legend(fontsize=9)
    for ax in axs:ax.grid(alpha=.2);ax.axvline(2,color='#888888',ls=':',label='激光峰值')
    save(fig,'thermal_power.png')
    rows=[]
    for te in [0,60,150,260,400,740]:
        i=np.argmin(abs(t-te));rows.append({'experiment_ns':te,'model_ns':float(a[i,0]*1e9),'Tmax_K':float(a[i,1]),'ne_max_m3':float(a[i,2])})
    return rows

def boltz():
    t=np.linspace(700,7500,500); kb=8.617333262e-5;eu=3.816692
    fig,axs=plt.subplots(1,2,figsize=(10,4),layout='constrained')
    for e,c,label in [(eu,BLUE,'Cu I 510.55 nm；上能级3.817 eV'),(6.1911751,ORANGE,'Cu I 515.32 nm；上能级6.191 eV')]:
        f=np.exp(-e/kb*(1/t-1/7000));axs[0].semilogy(t,f,color=c,label=label)
    axs[0].set(ylim=(1e-45,10),xlabel='温度 / K',ylabel='仅Boltzmann因子 / 7000 K时的值');axs[0].legend(fontsize=9);axs[0].grid(alpha=.2)
    tt=np.linspace(1500,7000,250);axs[1].plot(tt,eu/(kb*tt),color=BLUE,lw=2)
    axs[1].set(xlabel='温度 / K',ylabel='发光对温度的对数敏感度',ylim=(0,32));axs[1].grid(alpha=.2)
    axs[1].text(.06,.84,'5000 K附近：\n温度变1%，激发因子约变9%',transform=axs[1].transAxes,color=BLUE)
    save(fig,'boltzmann.png')
    return {str(t):float(np.exp(-eu/kb*(1/t-1/7000))) for t in [5000,3000,1376.34587,860.318513]}

def toy():
    x=np.linspace(-1,1,350);z=np.linspace(0,1.05,180);xx,zz=np.meshgrid(x,z);q=xx**2+zz**2;rad=np.sqrt(np.maximum(1-q,0))
    fig,axs=plt.subplots(1,3,figsize=(10,3.6),layout='constrained')
    for ax,g,title in zip(axs,[1,4,100],['低增益：中心先可见','较高增益：亮区变大','极高增益：近满半球饱和']):
        c=np.clip(2+100*g*rad,0,255);ax.imshow(c,extent=(-1,1,1.05,0),vmin=0,vmax=255,cmap='inferno',aspect='equal',origin='upper');ax.invert_yaxis()
        # imshow origin mapping above is symmetric rad in x and increases from bottom row; use explicit pcolormesh.
        ax.clear();ax.pcolormesh(x,z,c,vmin=0,vmax=255,cmap='inferno',shading='auto');ax.contour(x,z,c,levels=[27],colors='cyan',linewidths=1)
        ax.set(xlim=(-1,1),ylim=(0,1.05),xlabel='横向 x/R',title=title);ax.axhline(0,color='white',lw=2)
    axs[0].set_ylabel('高度 z/R');save(fig,'threshold_toy.png')

if __name__=='__main__':
    equations();causal();rows=thermal();ratios=boltz();toy()
    evidence={'temperature_rows':rows,'510nm_boltzmann_factor_ratio_to_7000K_fixed_species_and_partition':ratios,'threshold_radiance_W_m2_sr':25/4.4200156217803074e67,'new_solver_runs':False,'illustrative_figures':['boltzmann.png','threshold_toy.png','causal.png']}
    (R/'results/analysis_derivation64.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2),encoding='utf8')
    print(json.dumps(evidence,ensure_ascii=False,indent=2))
