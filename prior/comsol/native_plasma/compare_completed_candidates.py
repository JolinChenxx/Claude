"""Compact full-sequence comparisons from frozen camera outputs, no new fit."""
from pathlib import Path
import json,csv,sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parent
cases=sys.argv[1:] or ['G18-G19','G46','G49','G47','A01']
out=R/'figures/extended_final';out.mkdir(exist_ok=True)
records=[];fig,ax=plt.subplots(2,3,figsize=(14,8),layout='constrained');drawn=False
for case in cases:
    folder=R/'results'/f'{case}_global_camera'
    if not (folder/'summary.json').exists():continue
    s=json.loads((folder/'summary.json').read_text());a=np.load(folder/'images.npz');p=np.load(R/'results'/f'{case}_global_images.npz');t=a['times_ns']
    if len(t)!=29:continue
    with (folder/'metrics.csv').open() as f:rows=[r for r in csv.DictReader(f) if r['threshold']=='27']
    def col(key):return np.array([float(r[key]) if r[key] not in ('','None') else 0 for r in rows])
    weights=np.maximum(a['prediction']-2,0);obsw=np.maximum(a['experiment']-2,0);z=(a['prediction'].shape[1]-np.arange(a['prediction'].shape[1])-.5)*float(a['pixel_um'])
    def centroid(v):return (v.sum(2)*z).sum(1)/np.maximum(v.sum((1,2)),1e-30)
    ce=centroid(obsw);cs=centroid(weights)
    vals=[col('radius_um'),col('area_mm2'),weights.sum((1,2)),cs-cs[0],p['photons_upper_525nm'].sum((1,2)),p['radiance'].max((1,2))]
    for axis,v in zip(ax.flat,vals):axis.plot(t,v,'o-',ms=2,lw=1,label=case)
    if not drawn:
        for axis,v in zip(ax.flat,[col('experiment_radius_um'),col('experiment_area_mm2'),obsw.sum((1,2)),ce-ce[0]]):axis.plot(t,v,'k.-',lw=2,label='S4')
        drawn=True
    total=vals[4];peak=vals[5];th=s['thresholds']['27']
    records.append(dict(case=case,complete_frames=len(t),visible_frames=th['visible_frames'],radius_pass=th['radius_pass_count'],area_pass=th['area_within30percent_count'],height_pass=th['height_pass_count'],physical_total_peak_ns=float(t[total.argmax()]),physical_pixel_peak_ns=float(t[peak.argmax()]),first_to_peak_radiance=float(peak[0]/max(peak.max(),1e-300)),last_to_peak_radiance=float(peak[-1]/max(peak.max(),1e-300)),last_model_visible_ns=th['last_simulated_visible_frame_ns'],last_experiment_visible_ns=th['last_experimental_visible_frame_ns'],geometry=s.get('geometry','uniform global closure')))
for axis,label in zip(ax.flat,['Radius at27 [um]','Area at27 [mm2]','Integrated gray above background','Emission-centroid change from0ns [um]','Ideal collected photons /10ns','Peak physical radiance [W m^-2 sr^-1]']):
    axis.set_ylabel(label);axis.set_xlabel('Experimental time [ns]');axis.grid(alpha=.2);axis.axvspan(0,80,color='gray',alpha=.08);axis.legend(fontsize=7)
ax[1,1].set_yscale('symlog',linthresh=1);ax[1,2].set_yscale('symlog',linthresh=1)
fig.suptitle('Complete S4 sequence: each candidate uses its frozen early gain\nCentroid change removes unknown constant target registration; assumed geometry is NOT independent spatial validation',fontsize=11)
fig.savefig(out/'candidate_trends.png',dpi=160);plt.close(fig)
(R/'results/completed_candidate_comparison.json').write_text(json.dumps(records,indent=2))
print(json.dumps(records,indent=2))

