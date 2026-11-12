"""Frequency-integrated escape of an expanding uniform sphere.

Uses the same Doppler formal transfer as the global image renderer. Table axes
are line-centre radial optical depth and edge speed / thermal Doppler speed.
No fitted optical length, lifetime or rate multiplier.
"""
from pathlib import Path
import json,time
import numpy as np
from scipy.special import erf
from numpy.polynomial.legendre import leggauss
R=Path(__file__).resolve().parent

def beta(tau,s,order=80):
    """3/(2 tau sqrt(pi)) integral y dy dw [1-exp(-tau(w,y))]."""
    tau=np.atleast_1d(tau)
    n,w=leggauss(order);y=(n+1)/2;yw=w/2*y
    half=np.sqrt(1-y*y);s=max(float(s),0)
    # For large bulk speed, separate flat interior and Doppler transition.
    cuts=[np.zeros_like(half),np.maximum(s*half-7,0),s*half+7]
    answer=np.zeros_like(tau,dtype=float)
    for lo,hi in zip(cuts[:-1],cuts[1:]):
        v=(hi-lo)[:,None]/2*n+(hi+lo)[:,None]/2
        vw=(hi-lo)[:,None]/2*w
        if s<1e-5:kernel=2*half[:,None]*np.exp(-v*v)
        else:kernel=np.sqrt(np.pi)/(2*s)*(erf(v+s*half[:,None])-erf(v-s*half[:,None]))
        integral=np.sum((-np.expm1(-tau[:,None,None]*kernel))*vw*yw[:,None],axis=(1,2))*2
        answer+=3/(2*tau*np.sqrt(np.pi))*integral
    return answer

def main():
    start=time.time();x=np.linspace(-7,10,171);y=np.linspace(-5,4,91)
    grid=np.array([beta(10**x,10**sy) for sy in y])
    assert np.all(grid>0) and grid.max()<1.000001
    rows=np.array([[tx,sy,np.log10(grid[j,i])] for j,sy in enumerate(y) for i,tx in enumerate(x)])
    np.savetxt(R/'data/global_escape_doppler.txt',rows,fmt='%.10g')
    tests=[]
    for tau,s in [(1e-6,0),(1,0),(100,0),(1e4,0),(100,10),(1e4,10),(100,1e3),(1e8,1e3)]:
        a=float(beta(tau,s,80)[0]);b=float(beta(tau,s,160)[0])
        tests.append(dict(tau=tau,speed_over_doppler=s,beta=a,refined=b,relative_difference=a/b-1))
    report={'definition':'Uniform sphere, homogeneous source, pure Doppler profile, homologous velocity gradient; frequency integrated formal transfer','used_in_COMSOL':False,'not_included':['Lorentz/Stark broadening','nonuniform material','time-dependent photon propagation','general ellipsoid anisotropy'],'quadrature_checks':tests,'max_quadrature_difference':max(abs(t['relative_difference']) for t in tests),'seconds':time.time()-start}
    (R/'data/global_escape_doppler_sources.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
if __name__=='__main__':main()
