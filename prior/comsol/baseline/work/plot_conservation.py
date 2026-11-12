"""Actual integrated optical, energy and copper accounting; no fit parameters."""
from pathlib import Path
import argparse,json
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from plot_s4 import ROOT,FIG
from audit_panel_alignment import require_matplotlib_panel_alignment
plt.rcParams.update({'font.family':'Arial','font.size':8,'pdf.fonttype':42,'svg.fonttype':'none'})

def plot(case):
 b=np.loadtxt(ROOT/f'results/{case}_budget.csv',delimiter=',',skiprows=1)
 a=json.loads((ROOT/f'results/{case}_audit.json').read_text());t=b[:,0]*1e6
 fig,axes=plt.subplots(2,2,figsize=(7.2,5.6),gridspec_kw={'wspace':.36,'hspace':1.1})
 def curve(ax,values,label,color,style='-'):ax.plot(t,values,style,lw=1.15,label=label,color=color)
 ax=axes[0,0]
 curve(ax,b[:,1]*1e6,'Incident','#171717');curve(ax,b[:,2]*1e6,'Reflected escape','#1876a5');curve(ax,b[:,3]*1e6,'Total absorption','#bd5232')
 ax.set(title='Optical accounting',ylabel='Cumulative energy (μJ)')
 ax=axes[0,1]
 curve(ax,b[:,3]*1e6,'Absorption','#171717');curve(ax,(b[:,4]+b[:,5]+b[:,6])*1e6,'Storage + outflow + radiation','#bd5232','--')
 ax.set(title='System energy closure',ylabel='Energy (μJ)')
 ax=axes[1,0]
 curve(ax,b[:,8]*1e12,'Net copper from target','#171717');curve(ax,(b[:,7]+b[:,9])*1e12,'Gas copper + outflow','#1876a5','--')
 ax.set(title='Copper mass closure',ylabel='Mass (ng)')
 ax=axes[1,1]
 activeE=b[:,3]>b[:,3].max()*.01;activeM=abs(b[:,8])>abs(b[:,8]).max()*.01
 er=np.full(len(t),np.nan);mr=er.copy()
 er[activeE]=100*(b[activeE,4]+b[activeE,5]+b[activeE,6]-b[activeE,3])/b[activeE,3]
 mr[activeM]=100*(b[activeM,7]+b[activeM,9]-b[activeM,8])/abs(b[activeM,8])
 curve(ax,er,'Energy residual','#bd5232');curve(ax,mr,'Copper residual','#1876a5')
 ax.axhline(0,color='.6',lw=.6,zorder=0);ax.set(title='Time-resolved relative residual',ylabel='Residual (%)')
 for ax in axes.flat:
  ax.set(xlabel='Numerical time (μs)',xlim=(0,2.12));ax.spines[['top','right']].set_visible(False)
  ax.legend(frameon=False,fontsize=6,loc='lower left',bbox_to_anchor=(0,1.025),borderaxespad=0)
  ax.set_title(ax.get_title(),y=1.50,fontsize=8)
 fig.subplots_adjust(left=.09,right=.97,bottom=.18,top=.78)
 fig.suptitle(f'{case} | conserved exchanges in the real LTE solution',fontsize=10,y=.975)
 fig.text(.5,.055,'Residuals begin after 1% absorption / net ablation. Dashed curves may overlap the reference.',ha='center',fontsize=7)
 fig.text(.5,.022,f"Final copper residual {100*a['mass_relative_residual']:.4f}%; energy {100*a['energy_relative_residual']:.4f}%.",ha='center',fontsize=7)
 fig.canvas.draw();require_matplotlib_panel_alignment(fig,json_out=FIG/f'{case}_conservation.alignment.json')
 fig.savefig(FIG/f'{case}_conservation.pdf')
 fig.savefig(FIG/f'{case}_conservation.svg')
 fig.savefig(FIG/f'{case}_conservation.png',dpi=300)
 plt.close(fig)
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('case');plot(p.parse_args().case)
