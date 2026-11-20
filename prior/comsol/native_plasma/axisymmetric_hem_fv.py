"""Axisymmetric conservative material predictor for split COMSOL coupling.

HLLC Euler flux, explicit Fourier conduction, common-pressure nuclear Cu EOS,
equilibrium vapor/condensed partition and conservative optical absorption.
This file alone does NOT solve Plasma or produce a camera-light prediction.
"""
from pathlib import Path
import sys,os,json,time
import numpy as np
from numba import njit,prange,set_num_threads
R=Path(__file__).resolve().parent;sys.path.insert(0,str(R))
from hem_common_thermo import thermo
from axial_hem_fv import interp

@njit(parallel=True)
def fields(U,lt,lr,table):
    nr,nz,_=U.shape;W=np.empty((nr,nz,10))
    for i in prange(nr):
        a=np.empty((nz,4))
        for k in range(nz):
            a[k,0]=U[i,k,0];a[k,1]=U[i,k,1];a[k,2]=np.sqrt(U[i,k,2]**2+U[i,k,3]**2);a[k,3]=U[i,k,4]
        b=thermo(a,lt,lr,table)
        for k in range(nz):
            W[i,k,:9]=b[k];rho=U[i,k,0]+U[i,k,1];W[i,k,3]=U[i,k,3]/rho;W[i,k,9]=U[i,k,2]/rho
    return W

@njit
def flux(UL,UR,WL,WR,radial):
    ix=2 if radial else 3;wi=9 if radial else 3
    rl=UL[0]+UL[1];rr=UR[0]+UR[1];ul=WL[wi];ur=WR[wi];pl=WL[1];pr=WR[1]
    sl=min(ul-WL[2],ur-WR[2]);sr=max(ul+WL[2],ur+WR[2])
    fl=UL*ul;fr=UR*ur;fl[ix]+=pl-101325.;fr[ix]+=pr-101325.;fl[4]+=pl*ul;fr[4]+=pr*ur
    if sl>=0:return fl
    if sr<=0:return fr
    sm=(pr-pl+rl*ul*(sl-ul)-rr*ur*(sr-ur))/(rl*(sl-ul)-rr*(sr-ur))
    if sm>=0:
        fact=(sl-ul)/(sl-sm);us=UL*fact;us[ix]=rl*fact*sm;us[4]=rl*fact*(UL[4]/rl+(sm-ul)*(sm+pl/(rl*(sl-ul))))
        return fl+sl*(us-UL)
    fact=(sr-ur)/(sr-sm);us=UR*fact;us[ix]=rr*fact*sm;us[4]=rr*fact*(UR[4]/rr+(sm-ur)*(sm+pr/(rr*(sr-ur))))
    return fr+sr*(us-UR)

@njit(parallel=True)
def step(U,W,rc,zc,dr,dz,re,volume,t,dt,absorption,plasma_alpha=None):
    nr,nz,_=U.shape;Fr=np.zeros((nr+1,nz,5));Fz=np.zeros((nr,nz+1,5));Q=np.zeros((nr,nz));Qe=np.zeros((nr,nz));reflected=np.zeros((nr,nz));opacity=np.zeros((nr,nz))
    for i in prange(nr+1):
        for k in range(nz):
            if i==0:Fr[i,k,2]=W[0,k,1]-101325.
            elif i==nr:
                if zc[k]<0:Fr[i,k,2]=W[-1,k,1]-101325.
                else:
                    Fr[i,k]=U[-1,k]*W[-1,k,9];Fr[i,k,2]+=W[-1,k,1]-101325.;Fr[i,k,4]+=W[-1,k,1]*W[-1,k,9]
            else:
                Fr[i,k]=flux(U[i-1,k],U[i,k],W[i-1,k],W[i,k],True)
                km=2*W[i-1,k,4]*W[i,k,4]/(W[i-1,k,4]+W[i,k,4])
                Fr[i,k,4]-=km*(W[i,k,0]-W[i-1,k,0])/(rc[i]-rc[i-1])
    for i in prange(nr):
        for k in range(nz+1):
            if k==0:Fz[i,k,3]=W[i,0,1]-101325.
            elif k==nz:
                Fz[i,k]=U[i,-1]*W[i,-1,3];Fz[i,k,3]+=W[i,-1,1]-101325.;Fz[i,k,4]+=W[i,-1,1]*W[i,-1,3]
            else:
                Fz[i,k]=flux(U[i,k-1],U[i,k],W[i,k-1],W[i,k],False)
                km=2*W[i,k-1,4]*W[i,k,4]/(W[i,k-1,4]+W[i,k,4])
                Fz[i,k,4]-=km*(W[i,k,0]-W[i,k-1,0])/(zc[k]-zc[k-1])
        # Exact ring-average incident Gaussian; attenuated power is conserved.
        beam=(np.exp(-2*(re[i]/11e-6)**2)-np.exp(-2*(re[i+1]/11e-6)**2))/(2*(re[i+1]**2-re[i]**2)/(11e-6)**2)
        light=2*400e-6/(np.pi*(11e-6)**2)*np.sqrt(4*np.log(2)/np.pi)/60e-9*np.exp(-4*np.log(2)*((t+dt/2-120e-9)/60e-9)**2)*beam
        for k in range(nz-1,-1,-1):
            hot=.5*(1+(W[i,k,0]-9030.317)/np.sqrt((W[i,k,0]-9030.317)**2+10000))
            dense=min(1.,1-W[i,k,6]+hot*U[i,k,0]**2/(U[i,k,0]**2+1917.4313**2));alpha=dense/20e-9
            beta=0. if plasma_alpha is None else plasma_alpha[i,k];total=alpha+beta;opacity[i,k]=total
            removed=light*(-np.expm1(-total*dz[k]));fc=alpha/max(total,1e-100);fe=beta/max(total,1e-100)
            Q[i,k]=absorption*fc*removed/dz[k];Qe[i,k]=fe*removed/dz[k];reflected[i,k]=(1-absorption)*fc*removed;light-=removed
        if plasma_alpha is not None:
            back=0.
            for k in range(nz):
                total=opacity[i,k];beta=plasma_alpha[i,k];removed=back*(-np.expm1(-total*dz[k]));fe=beta/max(total,1e-100)
                Qe[i,k]+=fe*removed/dz[k];Q[i,k]+=absorption*(1-fe)*removed/dz[k];back=back-removed+reflected[i,k]
    ea=0.;eo=0.;mo=0.;bad=0
    for i in prange(nr):
        az=np.pi*(re[i+1]**2-re[i]**2)
        for k in range(nz):
            ar0=2*np.pi*re[i]*dz[k];ar1=2*np.pi*re[i+1]*dz[k]
            U[i,k]-=dt*(ar1*Fr[i+1,k]-ar0*Fr[i,k]+az*(Fz[i,k+1]-Fz[i,k]))/volume[i,k]
            U[i,k,2]+=dt*(W[i,k,1]-101325.)/rc[i]
            U[i,k,4]+=dt*(Q[i,k]+Qe[i,k])
            sf=W[i,k,8]*(1-W[i,k,5])*.5*(1-(W[i,k,0]-1358)/np.sqrt((W[i,k,0]-1358)**2+100))
            U[i,k,2]/=1+dt*1e10*sf*sf;U[i,k,3]/=1+dt*1e10*sf*sf
            if U[i,k,0]<-1e-8 or U[i,k,1]<-1e-8:bad+=1
            if U[i,k,0]<0:U[i,k,0]=0
            if U[i,k,1]<0:U[i,k,1]=0
            ea+=dt*(Q[i,k]+Qe[i,k])*volume[i,k]
        eo+=dt*az*(Fz[i,nz,4]-Fz[i,0,4]);mo+=dt*az*(Fz[i,nz,0]-Fz[i,0,0])
    for k in range(nz):
        ar=2*np.pi*re[-1]*dz[k];eo+=dt*ar*Fr[nr,k,4];mo+=dt*ar*Fr[nr,k,0]
    return U,ea,eo,mo,bad,Fr,Fz,Qe

@njit(parallel=True)
def stable_dt(W,dr,dz):
    nr,nz,_=W.shape;limit=np.ones(nr)*1e10
    for i in prange(nr):
        for k in range(nz):
            v=W[i,k];limit[i]=min(limit[i],.3/( (abs(v[9])+v[2])/dr[i]+(abs(v[3])+v[2])/dz[k]),.3*v[7]/max(v[4]*(1/dr[i]**2+1/dz[k]**2),1e-100))
    return limit.min()

def main():
    case=sys.argv[1];end=float(sys.argv[2]);set_num_threads(int(os.environ.get('ROUTE1_FV_THREADS','16')))
    out=R/'results'/case;(out/'frames').mkdir(parents=True,exist_ok=True)
    eos=R/'data'/os.environ.get('ROUTE1_EOS_DIR','Cu_HEM_20261110');d=np.load(eos/'eos.npz');ts=d['temperatures_K'];rs=d['densities_kg_m3'];tab=np.ascontiguousarray(d['table'][:,2:].reshape(len(ts),len(rs),7));lt=np.log10(ts);lr=np.log10(rs)
    re=np.unique(np.r_[np.linspace(0,60,41),np.linspace(60,500,25)])*1e-6
    ze=np.unique(np.r_[np.linspace(-100,-5,41),np.linspace(-5,0,126),np.linspace(0,10,101),np.geomspace(10,1500,81)])*1e-6
    rc=(re[:-1]+re[1:])/2;zc=(ze[:-1]+ze[1:])/2;dr=np.diff(re);dz=np.diff(ze);volume=np.pi*np.diff(re**2)[:,None]*dz[None,:]
    U=np.zeros((len(rc),len(zc),5));U[:,:,0]=np.where(zc[None,:]<0,8942.230330721954,0);U[:,:,1]=np.where(zc[None,:]>=0,101325/(287*293.15),0)
    for k,z in enumerate(zc):U[:,k,4]=U[:,k,0]*interp(293.15,U[0,k,0],lt,lr,tab)[1]+U[:,k,1]*717.5*293.15
    t=0.;source=None
    if len(sys.argv)>3:
        source=sys.argv[3];prior=json.loads((R/'results'/source/'status.json').read_text());saved=np.load(prior['frame']);U=saved['U'].copy();t=float(saved['time_s'])
    m0=np.sum(U[:,:,0]*volume);e0=np.sum(U[:,:,4]*volume);tstart=t;ea=eo=mo=0.;nstep=0;bad=0;start=time.time();history=[]
    W=fields(U,lt,lr,tab)
    np.savez(out/'grid.npz',r_m=rc,z_m=zc,radial_edges_m=re,axial_edges_m=ze,cell_volume_m3=volume)
    def save():
        idx=len(history);path=out/'frames'/f'frame_{idx:04d}.npz';np.savez_compressed(path,U=U,W=W,time_s=t)
        report={'case':case,'saved_time_ns':t*1e9,'steps':nstep,'seconds':time.time()-start,'Tmax_K':float(W[:,:,0].max()),'Tmin_K':float(W[:,:,0].min()),'absorbed_J':ea,'energy_residual_J':float(np.sum(U[:,:,4]*volume)-e0-ea+eo),'mass_residual_kg':float(np.sum(U[:,:,0]*volume)-m0+mo),'vapor_copper_kg':float(np.sum(U[:,:,0]*W[:,:,5]*volume)),'negative_density_events':bad,'native_plasma_solved':False,'solver':'independent axisymmetric FV material predictor for split COMSOL coupling','eos_directory':str(eos),'frame':str(path)}
        report.update(source=source,segment_start_ns=tstart*1e9)
        history.append(report);(out/'status.json').write_text(json.dumps(report,indent=2));(out/'history.json').write_text(json.dumps(history,indent=2));print(json.dumps(report),flush=True)
    save()
    for stop in np.arange(t*1e9+2,end+1e-6,2)*1e-9:
        while t<stop-1e-20:
            dt=min(stable_dt(W,dr,dz),stop-t)
            U,da,de,dm,bad,_,_,_=step(U,W,rc,zc,dr,dz,re,volume,t,dt,.1);ea+=da;eo+=de;mo+=dm;t+=dt;nstep+=1
            if bad:raise RuntimeError('Material predictor has negative partial density')
            W=fields(U,lt,lr,tab)
        save()
if __name__=='__main__':main()
