import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from transport import line_coefficients,ff_bands,line_transfer,continuum_transfer,ff_images
from rate_tables import ENERGIES,G,CHI,GION

def run():
    checks={}
    t=np.array([0.,2.,5.,10.])*1e-9;v=3.+2e9*t
    got=gate(t,v,[5.])[0]
    assert np.isclose(got,13e-8,rtol=1e-13)
    checks['exposure_linear_integral_relative_error']=float(abs(got/13e-8-1))
    T=6000.;nu=C/510.55e-9;A=2e6;gl,gu=6.,4.;nl=1e23
    nu_pop=nl*gu/gl*np.exp(-H*nu/(K*T));j,a=line_coefficients(nl,nu_pop,gl,gu,A,nu)
    B=2*H*nu**3/C**2/np.expm1(H*nu/(K*T))
    assert np.isclose(j/a,B,rtol=1e-13)
    checks['Einstein_coefficients_recover_Kirchhoff']=float(abs(j/a/B-1))
    # A dark foreground must still absorb photons emitted behind it.
    result=continuum_transfer(np.array([[1.,0.]]),np.array([[0.,2.]]),np.ones(2))[0]
    assert np.isclose(result,np.exp(-2),rtol=1e-13)
    checks['dark_foreground_absorption']='pass'
    q=np.ones((3,1));TT=np.array([1000.,6000.,12000.])[:,None]
    e,p=ff_bands(q,TT,20);e2,p2=ff_bands(q,TT,80)
    err=float(max(np.max(abs(e/e2-1)),np.max(abs(p/p2-1))))
    assert err<1e-8
    checks['continuum_20_vs_80_point_relative_error']=err
    class SlabRays:
        def project(self,a):return a*1e-5
    ce,cp,ct,ctp,tau=ff_images(np.ones((2,2)),np.full((2,2),6000.),SlabRays())
    original=ce.copy();ce+=1.;ct+=100.
    assert np.allclose(ce-original,1.) and np.allclose(ct-original,100.)
    checks['thick_line_plus_thin_continuum_independent_composition']='pass'
    rates=np.load(ROOT/'data/rates.npz');net=json.loads((ROOT/'data/network.json').read_text())
    temp=rates['T'];valid=(temp>2000)&(temp<30000);worst=0.
    for pair in net['pairs']:
        lo,up=pair['lo'],pair['up'];lte=G[up]/G[lo]*np.exp(-(ENERGIES[up]-ENERGIES[lo])*EV/(K*temp[valid]))
        ratio=rates[pair['up_func']][valid]/rates[pair['dn_func']][valid]
        worst=max(worst,float(np.max(abs(ratio/lte-1))))
    for i in range(7):
        saha=2*GION/G[i]*(2*np.pi*ME*K*temp[valid]/H**2)**1.5*np.exp(-(CHI-ENERGIES[i])*EV/(K*temp[valid]))
        worst=max(worst,float(np.max(abs(rates[f'ion{i}'][valid]/rates[f'tbr{i}'][valid]/saha-1))))
    assert worst<1e-12
    checks['collisional_detailed_balance_max_relative_error']=worst
    write_json(RES/'R2_physics_checks.json',checks);print(checks)
if __name__=='__main__':run()
