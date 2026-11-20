import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *

def run(case):
    p=RES/case;read=lambda name:np.genfromtxt(str(p)+'_'+name+'.csv',delimiter=',',skip_header=1)
    g=read('gas');s=read('solid');w=read('wall');far=read('far');t=np.array(json.loads((RES/f'{case}_fields.json').read_text())['times_s'])
    integral=lambda v:np.r_[0,np.cumsum((v[1:]+v[:-1])*.5*np.diff(t))]
    energy=g[:,2]+s;power=g[:,3]-g[:,4]+w[:,2]-far[:,1];expected=integral(power);res=energy-energy[0]-expected
    dm=g[:,0]-g[0,0];expectedmass=integral(w[:,0]-far[:,0]);massres=dm-expectedmass
    scale=max(abs(expected).max(),1e-30);mscale=max(abs(expectedmass).max(),1e-30)
    plt=setup_plot();fig,axs=plt.subplots(1,3,figsize=(16,4.7),layout='constrained');tn=t*1e9
    axs[0].plot(tn,(energy-energy[0])*1e6,label='gas + solid stored change');axs[0].plot(tn,expected*1e6,'--',label='laser minus radiation / far outflow')
    axs[1].plot(tn,res*1e9,label='energy residual (nJ)');axs[2].plot(tn,dm*1e12,label='gas Cu increase');axs[2].plot(tn,expectedmass*1e12,'--',label='wall supply minus far outflow')
    for ax,title,y in zip(axs,['Total coupled energy balance','Energy residual','Copper balance'],['microjoule','nJ','ng']):ax.set(title=title,xlabel='Numerical time (ns)',ylabel=y);ax.grid(alpha=.2);ax.legend(fontsize=8)
    fig.suptitle(case+' | both gas and target energy included | 2 ns source integration')
    fig.savefig(FIG/f'{case}_budget.png');plt.close(fig)
    write_json(RES/f'{case}_budget_summary.json',dict(last_numerical_ns=float(t[-1]*1e9),energy_change_J=float(energy[-1]-energy[0]),net_input_J=float(expected[-1]),max_abs_energy_residual_J=float(abs(res).max()),max_energy_residual_fraction=float(abs(res).max()/scale),
        copper_change_kg=float(dm[-1]),net_copper_supply_kg=float(expectedmass[-1]),max_abs_copper_residual_kg=float(abs(massres).max()),max_copper_residual_fraction=float(abs(massres).max()/mscale),
        scope='Gas total energy + solid energy; incident energy reaching target + gas laser absorption - gas radiative escape - far gas energy flux. Internal evaporation/exchange cancels. Source integration uses 2 ns saved values.'))

if __name__=='__main__':run(sys.argv[1])
