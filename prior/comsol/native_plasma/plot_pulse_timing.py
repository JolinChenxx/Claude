"""Compare specified laser inputs; no fitting to S4 intensities or reaction rates."""
from pathlib import Path
import json
import numpy as np
from scipy.special import erf
from scipy.integrate import cumulative_trapezoid
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

R=Path(__file__).resolve().parent
t=np.linspace(-150,200,3501)  # experimental ns; model time is t+118
sigma=60/np.sqrt(8*np.log(2))
old=400e-6/(np.sqrt(2*np.pi)*sigma*1e-9)*np.exp(-.5*((t-2)/sigma)**2)
finite=400e-6/60e-9*.5*(erf(t)-erf(t-60))
fig,axes=plt.subplots(1,2,figsize=(11,4),layout='constrained')
for y,label in [(old,'Inherited Gaussian: peak at experiment +2 ns'),(finite,'G10: finite 60 ns envelope; unmeasured sensitivity')]:
    axes[0].plot(t,y/1000,label=label)
    axes[1].plot(t,cumulative_trapezoid(y,t*1e-9,initial=0)*1e6,label=label)
for ax in axes:
    ax.axvspan(0,60,color='k',alpha=.05,label='Irradiation interval described in paper')
    ax.axvline(0,color='gray',lw=.8);ax.axvline(60,color='gray',lw=.8)
    ax.set_xlim(-80,140);ax.set_xlabel('Experimental time (ns)');ax.grid(alpha=.2)
axes[0].set_ylabel('Incident power (kW)');axes[1].set_ylabel('Cumulative incident energy (uJ)')
axes[0].legend(fontsize=7,loc='upper right')
fig.suptitle('Laser timing audit: comparison shift remains 118 ns; input timing is a separate assumption',fontsize=11)
out=R/'figures/material_data';out.mkdir(exist_ok=True)
fig.savefig(out/'pulse_timing_audit.png',dpi=180)
report={'inherited_fraction_delivered_at_exp0':float(.5*(1+erf(-2/(np.sqrt(2)*sigma)))),
        'finite_input_integral_J':float(np.trapz(finite,t*1e-9)),
        'measured_waveform':False,'camera_shift_ns':118,'pulse_FWHM_ns':60,
        'paper_basis':'Main paper p3: brightness rises during irradiation, pulse ends at60ns'}
(R/'results/pulse_timing_audit.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
