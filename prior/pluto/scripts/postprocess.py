"""Physical LOS projection, common camera fit, and S4 comparison artifacts."""
from pathlib import Path
import os
for _name in ['OMP_NUM_THREADS','OPENBLAS_NUM_THREADS','MKL_NUM_THREADS','NUMEXPR_NUM_THREADS']:
    os.environ[_name]='1'
import json,csv,shutil,argparse
import numpy as np
from scipy.ndimage import gaussian_filter, map_coordinates
from scipy.optimize import minimize
from PIL import Image,ImageDraw,ImageFont
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from common import ROOT,read_json,atomic_json
from extract_experiment import measurements,CALIBRATION
from budgets import load_budget

KB=1.380649e-23;EV=1.602176634e-19;MCU=1.055206e-25;HC=6.62607015e-34*299792458.

def load_grid(folder):
    lines=[x.strip() for x in (folder/'grid.out').read_text().splitlines() if x.strip() and not x.startswith('#')]
    axes=[];pos=0
    for _ in range(3):
        n=int(lines[pos]);pos+=1
        bounds=np.array([[float(q) for q in x.split()[1:3]] for x in lines[pos:pos+n]])
        axes.append(bounds);pos+=n
    return axes

def outputs(folder):
    rows=[]
    for line in (folder/'flt.out').read_text().splitlines():
        x=line.split()
        if len(x)>=7:rows.append((int(x[0]),float(x[1]),x[6:]))
    return rows

def field(folder,entry,shape):
    index,t,names=entry
    a=np.fromfile(folder/f'data.{index:04d}.flt',dtype='<f4')
    if a.size!=len(names)*np.prod(shape):raise ValueError(f'Incomplete float output {index}: {a.size}')
    a=a.reshape(len(names),*shape)
    return {k:a[i] for i,k in enumerate(names)}

def project(folder,band_nm=(480.,520.),cache_name='projection.npz'):
    cache=folder/cache_name
    if cache.exists():return np.load(cache)
    exp=np.load(ROOT/'data/experimental/s4_frames.npz');h,w=exp['gray'].shape[1:];pix=float(exp['pixel_um'])
    axes=load_grid(folder);rb,zb,_=axes
    params=read_json(folder/'params.json')['parameters'];width=params['line_width_nm']*1e-9
    x=(np.arange(w)-(w-1)/2)*pix
    zgrid=np.arange(0,1100+pix,pix)
    rc=rb.mean(1);zc=zb.mean(1)
    keep_r=np.flatnonzero(rc<900);keep_z=np.flatnonzero(zc<1150)
    rb=rb[keep_r];zc=zc[keep_z]
    # Half-chord lengths for every projected pixel and cylindrical shell.
    chord=(np.sqrt(np.maximum(rb[:,1,None]**2-x[None,:]**2,0))-
           np.sqrt(np.maximum(rb[:,0,None]**2-x[None,:]**2,0)))*1e-6
    planes=[];ts=[];diagnostics=[]
    for entry in outputs(folder):
        if entry[1]>790:continue
        f=field(folder,entry,(len(zb),len(axes[0])))
        def crop(key):return np.asarray(f[key][np.ix_(keep_z,keep_r)],dtype=float)
        rho=np.maximum(crop('rho'),0);T=np.maximum(crop('temperature'),50)
        y=np.clip(crop('tr1'),0,1);ion=np.clip(crop('tr2'),0,y)
        neutral=rho*(y-ion)/MCU;ne=rho*ion/MCU
        fraction=2*np.exp(-3.817*EV/(KB*T))
        emiss=neutral*fraction/(1+fraction)*2e6*(HC/(510.55e-9))/(4*np.pi)
        source=2*6.62607015e-34*299792458.**2/(510.55e-9)**5/np.expm1(np.minimum(HC/(510.55e-9*KB*T),700))*width
        opacity=emiss/np.maximum(source,1e-100)
        image=np.zeros((len(zc),w))
        # Far side (outer to axis), then near side (axis to outer).
        for indices in [range(len(rb)-1,-1,-1),range(len(rb))]:
            for i in indices:
                tau=opacity[:,i,None]*chord[i][None,:]
                absorbed=-np.expm1(-np.minimum(tau,700))
                image=image*(1-absorbed)+source[:,i,None]*absorbed
        # Optically thin bremsstrahlung integrated over the provisional 480-520 nm filter.
        band=np.exp(-HC/(band_nm[1]*1e-9*KB*T))-np.exp(-HC/(band_nm[0]*1e-9*KB*T))
        ff=1.426e-40*np.sqrt(T)*ne*ne*band/(4*np.pi)
        image+=ff@(2*chord)
        image_z=np.stack([np.interp(zgrid,zc,image[:,col],left=image[0,col],right=0) for col in range(w)],1)
        planes.append(image_z.astype('f4'));ts.append(entry[1])
        bright=emiss>emiss.max()*.01 if emiss.max()>0 else np.zeros_like(emiss,dtype=bool)
        diagnostics.append(dict(time_ns=entry[1],max_temperature_K=float(T.max()),
            max_ne_m3=float(ne.max()),max_line_tau=float((opacity@(2*chord)).max()),
            emissivity_fraction_above_30000K=float(emiss[T>30000].sum()/max(emiss.sum(),1e-100))))
    if not planes:raise ValueError('No usable frames')
    np.savez_compressed(cache,intensity=np.stack(planes),times_ns=ts,z_um=zgrid,pixel_um=pix)
    atomic_json(folder/(cache.stem+'_radiation_diagnostics.json'),diagnostics)
    return np.load(cache)

def camera(proj,p,targets,h,w):
    loggain,surface,sigma,toffset,xshift=p
    ts=proj['times_ns'];cube=proj['intensity'];pix=float(proj['pixel_um'])
    yy,xx=np.indices((h,w),dtype=float)
    zcoords=surface-yy
    coords=np.array([zcoords,xx-xshift])
    result=[]
    # Simpson integration of a 10 ns rectangular exposure centered at target+offset.
    for target in targets:
        frame=np.zeros((h,w))
        for shift,weight in [(-5,1/6),(0,4/6),(5,1/6)]:
            t=target+toffset+shift
            if t<ts[0] or t>ts[-1]:continue
            idx=min(max(np.searchsorted(ts,t)-1,0),len(ts)-2)
            f=np.clip((t-ts[idx])/max(ts[idx+1]-ts[idx],1e-12),0,1)
            image=(1-f)*cube[idx]+f*cube[idx+1]
            frame+=weight*map_coordinates(image,coords,order=1,mode='constant',cval=0)
        if sigma>0:frame=gaussian_filter(frame,sigma,mode='constant')
        result.append(np.clip(np.exp(loggain)*frame+3.,0,255))
    return np.asarray(result)

def fit(folder):
    proj=project(folder);exp=np.load(ROOT/'data/experimental/s4_frames.npz')
    obs=exp['gray'];times=exp['times_ns'];h,w=obs.shape[1:]
    train=np.isin(times,CALIBRATION);indices=np.flatnonzero(train)
    gain0=np.log(230/max(float(proj['intensity'].max()),1e-30))
    bounds=[(gain0-9,gain0+9),(45,100),(.35,3),(-10,20),(-3,3)]
    def objective(p):
        sim=camera(proj,p,times[train],h,w);ref=obs[train]
        weight=.15+(ref>20)*.85
        intensity=np.mean(np.sum(weight*((sim-ref)/255)**2,axis=(1,2))/np.sum(weight,axis=(1,2)))
        # Soft visible-boundary overlap discourages a tiny saturated bright spot.
        a=1/(1+np.exp(np.clip((27-sim)/5,-60,60)));b=1/(1+np.exp(np.clip((27-ref)/5,-60,60)))
        dice=2*np.sum(a*b,axis=(1,2))/(np.sum(a*a+b*b,axis=(1,2))+1e-8)
        return float(intensity+.08*np.mean(1-dice))
    candidates=[]
    for surface in [60,78,92]:
        p=[gain0,surface,1.2,5,0]
        candidates.append((objective(p),p))
    _,start=min(candidates,key=lambda p:p[0])
    opt=minimize(objective,start,method='Powell',bounds=bounds,
                 options={'maxiter':28,'maxfev':650,'xtol':.015,'ftol':.0003})
    p=opt.x;sim=camera(proj,p,times,h,w)
    masks=obs>27;smasks=sim>27
    iou=np.sum(masks&smasks,axis=(1,2))/np.maximum(np.sum(masks|smasks,axis=(1,2)),1)
    validation=float(np.mean(((sim[~train]-obs[~train])/255)**2))
    metrics=[]
    for ti,frame,io in zip(times,sim,iou):
        mm=measurements(frame,float(exp['pixel_um']));mm.update(time_ns=int(ti),calibration=int(ti) in CALIBRATION,iou=float(io));metrics.append(mm)
    with (folder/'metrics.csv').open('w',newline='',encoding='utf-8') as f:
        wr=csv.DictWriter(f,fieldnames=list(metrics[0]));wr.writeheader();wr.writerows(metrics)
    np.savez_compressed(folder/'synthetic_frames.npz',gray=sim.astype('f4'),times_ns=times,
                        camera_params=p,pixel_um=exp['pixel_um'])
    budget=load_budget(folder)
    last=budget[-1]
    energy_res=float((last['excess_energy_uj']+last['radiated_segment_uj']-last['energy_in_uj'])/max(last['energy_in_uj'],1e-10))
    mass_res=float((last['cu_mass_ng']-last['mass_in_ng'])/max(last['mass_in_ng'],1e-10))
    score=dict(run_id=folder.name,score=float(opt.fun),held_out_pixel_mse=validation,
       median_iou_visible=float(np.median(iou[times<600])),
       camera=dict(zip(['log_gain','surface_y_px','psf_sigma_px','time_offset_ns','horizontal_shift_px'],map(float,p))),
       energy_residual=energy_res,mass_residual=mass_res,
       parameters=read_json(folder/'params.json'),optimizer_success=bool(opt.success),
       result_level='简化铜/铜离子-中性空气模型；实验约束的形貌复现，非完整烧蚀预测')
    atomic_json(folder/'result.json',score)
    return score

def render(folder,publish=False):
    exp=np.load(ROOT/'data/experimental/s4_frames.npz');syn=np.load(folder/'synthetic_frames.npz')
    obs=exp['gray'];sim=syn['gray'];times=exp['times_ns'];lut=plt.get_cmap('jet')
    fig,axes=plt.subplots(6,5,figsize=(16,10))
    for i,ax in enumerate(axes.flat):
        ax.axis('off')
        if i<len(times):
            pair=np.concatenate([obs[i],np.full((obs.shape[1],3),np.nan),sim[i]],axis=1)
            ax.imshow(pair,cmap='jet',vmin=0,vmax=255);ax.set_title(f'{int(times[i])} ns | exp / model',fontsize=10)
        else:ax.text(.05,.6,'Common scale: 0-255\nOne camera setting for all frames\nCalibration: 20,60,150,260,400,580 ns',fontsize=9)
    result=read_json(folder/'result.json');fig.suptitle(f"S4 comparison | {folder.name} | loss {result['score']:.4f}",fontsize=15)
    fig.tight_layout();fig.savefig(folder/'comparison.png',dpi=160);plt.close(fig)
    me=[measurements(a,float(exp['pixel_um'])) for a in obs]
    ms=[measurements(a,float(exp['pixel_um'])) for a in sim]
    fig,axes=plt.subplots(2,2,figsize=(10,7))
    for ax,key,title in zip(axes.flat,['radius_um','mean_above27','area_mm2','centroid_y_px'],
                            ['Visible radius (um)','Mean grayscale > 27','Visible area (mm2)','Centroid y (px)']):
        ax.plot(times,[m[key] for m in me],'o',label='PDF');ax.plot(times,[m[key] for m in ms],'-',label='model')
        ax.set(xlabel='Delay (ns)',ylabel=title);ax.grid(alpha=.25);ax.legend()
    fig.tight_layout();fig.savefig(folder/'metrics.png',dpi=160);plt.close(fig)
    b=load_budget(folder)
    fig,axes=plt.subplots(1,2,figsize=(10,4))
    axes[0].plot(b['time_ns'],b['mass_in_ng'],label='injected');axes[0].plot(b['time_ns'],b['cu_mass_ng'],'--',label='in domain')
    axes[0].set(xlabel='Time (ns)',ylabel='Copper mass (ng)');axes[0].legend();axes[0].grid(alpha=.25)
    axes[1].plot(b['time_ns'],b['energy_in_uj'],label='input')
    axes[1].plot(b['time_ns'],b['excess_energy_uj'],label='gas excess')
    axes[1].plot(b['time_ns'],b['radiated_segment_uj'],label='radiated')
    axes[1].set(xlabel='Time (ns)',ylabel='Energy (uJ)');axes[1].legend();axes[1].grid(alpha=.25)
    fig.tight_layout();fig.savefig(folder/'budget.png',dpi=160);plt.close(fig)
    frames=[];font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',18)
    for t,a,b1 in zip(times,obs,sim):
        pair=np.concatenate([a,b1],axis=1)
        im=Image.fromarray((lut(pair/255)[:,:,:3]*255).astype('uint8')).resize((680,328))
        canvas=Image.new('RGB',(680,370),'white');canvas.paste(im,(0,42));draw=ImageDraw.Draw(canvas)
        draw.text((15,10),f'{int(t)} ns    Experiment',fill='black',font=font)
        draw.text((365,10),'PLUTO + camera',fill='black',font=font);frames.append(canvas)
    frames[0].save(folder/'comparison.gif',save_all=True,append_images=frames[1:],duration=250,loop=0)
    # Uniform physical time spacing; do not invent experimental frames between gates.
    targets=np.arange(0,741,5)
    sequence=camera(project(folder),syn['camera_params'],targets,*obs.shape[1:])
    smooth=[]
    for t,a in zip(targets,sequence):
        im=Image.fromarray((lut(a/255)[:,:,:3]*255).astype('uint8')).resize((340,328))
        canvas=Image.new('RGB',(340,370),'white');canvas.paste(im,(0,42))
        ImageDraw.Draw(canvas).text((12,10),f'Model | {t} ns | 10 ns gate',fill='black',font=font)
        smooth.append(canvas)
    smooth[0].save(folder/'evolution.gif',save_all=True,append_images=smooth[1:],duration=60,loop=0)
    if publish:
        for src,dst in [('comparison.png','best_comparison.png'),('metrics.png','best_metrics.png'),('budget.png','best_budget.png'),('comparison.gif','best_comparison.gif'),('evolution.gif','best_evolution.gif')]:
            tmp=ROOT/'figures'/(dst+'.tmp');shutil.copy2(folder/src,tmp);os.replace(tmp,ROOT/'figures'/dst)
        atomic_json(ROOT/'state/best.json',result)

def export_hdf5(folder):
    import h5py
    axes=load_grid(folder);entries=outputs(folder)
    target=folder/'fields.h5'
    with h5py.File(target,'w') as h:
        h.attrs['model']='PLUTO cuair-frozen-species-v1'
        h.attrs['rho_unit']='kg m^-3';h.attrs['velocity_unit']='km s^-1';h.attrs['pressure_unit']='MPa'
        h.attrs['temperature_unit']='K';h.attrs['coordinate_unit']='um';h.attrs['time_unit']='ns'
        h.attrs['source_parameters']=json.dumps(read_json(folder/'params.json'))
        h.create_dataset('r_edges_um',data=np.r_[axes[0][:,0],axes[0][-1,1]])
        h.create_dataset('z_edges_um',data=np.r_[axes[1][:,0],axes[1][-1,1]])
        h.create_dataset('times_ns',data=[e[1] for e in entries])
        ds={name:h.create_dataset(name,(len(entries),len(axes[1]),len(axes[0])),dtype='f4',
             chunks=(1,len(axes[1]),len(axes[0])),compression='gzip',compression_opts=1) for name in entries[0][2]}
        for i,entry in enumerate(entries):
            for name,a in field(folder,entry,(len(axes[1]),len(axes[0]))).items():ds[name][i]=a
    return target

if __name__=='__main__':
    ap=argparse.ArgumentParser();ap.add_argument('run_id');ap.add_argument('--publish',action='store_true');ap.add_argument('--fit-only',action='store_true');a=ap.parse_args()
    folder=ROOT/'runs'/a.run_id
    result=fit(folder)
    if not a.fit_only:render(folder,a.publish)
    print(json.dumps(result,ensure_ascii=True))
