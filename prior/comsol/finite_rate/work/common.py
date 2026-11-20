from pathlib import Path
import os, sys, json
if hasattr(sys.stdout,'reconfigure'):sys.stdout.reconfigure(encoding='utf-8',errors='replace')
for key in ('OMP_NUM_THREADS', 'OPENBLAS_NUM_THREADS', 'MKL_NUM_THREADS'):
    os.environ[key] = '1'
os.environ['NUMBA_NUM_THREADS']='8'
sys.path.insert(0, str(Path(__file__).resolve().parent))
import numpy as np
try:
    import psutil
    psutil.Process().cpu_affinity(list(range(8)))
except (ImportError,AttributeError):
    if os.name=='nt':
        from affinity import restrict
        restrict()
from scipy.ndimage import gaussian_filter
from scipy.interpolate import RegularGridInterpolator

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105'/'project'
RES, FIG = ROOT/'results', ROOT/'figures'
H, C, K, EV = 6.62607015e-34, 299792458., 1.380649e-23, 1.602176634e-19
MCU, ME = 63.546*1.66053906660e-27, 9.1093837139e-31
PIXEL = 400/36
OMEGA = 2*np.pi*(1-np.sqrt(1-.4**2))
BANDS = np.array([[475.,525.],[490.,510.],[450.,550.]])
PANELS = np.array([0.,30.,60.,80.,150.,260.,400.,580.,740.])
SHIFTS = (60.,90.,120.)

def write_json(path, value):
    Path(path).write_text(json.dumps(value, indent=2, ensure_ascii=False, allow_nan=False), encoding='utf-8')

def fields(case='B64A'):
    meta=json.loads((SOURCE/'results'/f'{case}_fields.json').read_text(encoding='utf-8'))
    f=np.memmap(SOURCE/'results'/f'{case}_fields.bin', dtype=meta['dtype'], mode='r', shape=tuple(meta['shape']))
    return f, np.array(meta['coordinates_um']['r']), np.array(meta['coordinates_um']['z']), np.array(meta['times_s'])

def gate(t, values, centres_ns, width_ns=10., average=False):
    """Integrate over a real exposure, using seconds throughout."""
    out=[]
    for tc in np.atleast_1d(centres_ns)*1e-9:
        lo,hi=tc-width_ns*.5e-9,tc+width_ns*.5e-9
        if lo<t[0]-1e-15 or hi>t[-1]+1e-15: raise ValueError('Exposure outside saved history')
        knots=np.r_[lo,t[(t>lo)&(t<hi)],hi]
        idx=np.clip(np.searchsorted(t,knots)-1,0,len(t)-2)
        w=(knots-t[idx])/(t[idx+1]-t[idx])
        sh=(-1,)+(1,)*(values.ndim-1)
        a=values[idx]*(1-w.reshape(sh))+values[idx+1]*w.reshape(sh)
        out.append(np.trapz(a,x=knots,axis=0)/(width_ns*1e-9 if average else 1))
    return np.array(out)

def blur(a, fwhm=1.):
    sigma=[0.]*(a.ndim-2)+[fwhm/2.354820045]*2
    return gaussian_filter(a,sigma,mode='constant') if fwhm else a.copy()

def volume_integral(a,r,z,jac=1.):
    return np.trapz(np.trapz(a*jac*(2*np.pi*r*1e-6),x=r*1e-6,axis=-1),x=z*1e-6,axis=-1)

class Rays:
    def __init__(self,r,z,pixel=PIXEL,rmax=650.,zmax=1000.,ray_factor=1.,subpixels=1,refine_pixels=6):
        self.x=(np.arange(int(np.floor(rmax/pixel)))+.5)*pixel
        self.z=(np.arange(int(np.floor(zmax/pixel)))+.5)*pixel
        self.subpixels=int(subpixels);self.refine_pixels=min(refine_pixels,len(self.x),len(self.z))
        n=self.refine_pixels;s=self.subpixels
        sx=np.r_[(np.arange(n*s)+.5)*pixel/s,self.x[n:]]
        sz=np.r_[(np.arange(n*s)+.5)*pixel/s,self.z[n:]]
        y=np.unique(np.r_[np.arange(0,30,ray_factor),np.arange(30,150,3*ray_factor),np.arange(150,650,7*ray_factor),650.])
        edges=np.r_[-y[:0:-1],y];self.y=(edges[1:]+edges[:-1])/2;self.ds=np.diff(edges)*1e-6
        rr=np.sqrt(sx[:,None]**2+self.y[None,:]**2)
        self.points=np.stack(np.broadcast_arrays(sz[:,None,None],rr[None,:,:]),axis=-1).reshape(-1,2)
        self.shape=(len(sz),len(sx),len(self.y));self.vproj=np.divide(self.y[None,:],rr)
        self.r,self.native_z,self.pixel=r,z,pixel
    def sample(self,a):
        return RegularGridInterpolator((self.native_z,self.r),a,bounds_error=False,fill_value=0.)(self.points).reshape(-1,len(self.y))
    def image(self,a):
        half=a.reshape(self.shape[:2]);n=self.refine_pixels;s=self.subpixels
        if s>1:
            half=np.concatenate([half[:,:n*s].reshape(len(half),n,s).mean(2),half[:,n*s:]],axis=1)
            half=np.concatenate([half[:n*s].reshape(n,s,half.shape[1]).mean(1),half[n*s:]],axis=0)
        return np.concatenate([half[:,::-1],half],axis=1)[::-1]
    def project(self,a): return self.image(self.sample(a)@self.ds)

def metrics(img, pixel=PIXEL, threshold=None):
    v=np.maximum(np.asarray(img,float),0.)
    if threshold is not None: v=np.where(v>=threshold,v,0.)
    total=v.sum()
    if total<=0:return dict(total=0.,x_um=None,z_um=None,sigma_x_um=None,sigma_z_um=None,aspect=None,width_um=None,height_um=None)
    zz=(v.shape[0]-np.arange(v.shape[0])-.5)*pixel
    xx=(np.arange(v.shape[1])+.5-v.shape[1]/2)*pixel
    x=(v.sum(0)*xx).sum()/total; z=(v.sum(1)*zz).sum()/total
    sx=np.sqrt((v.sum(0)*(xx-x)**2).sum()/total);sz=np.sqrt((v.sum(1)*(zz-z)**2).sum()/total)
    mask=v>0;rows,cols=np.where(mask)
    return dict(total=float(total),x_um=float(x),z_um=float(z),sigma_x_um=float(sx),sigma_z_um=float(sz),
        aspect=float(sz/sx) if sx else None,width_um=float((cols.max()-cols.min()+1)*pixel),height_um=float((rows.max()-rows.min()+1)*pixel))

def setup_plot():
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
    plt.rcParams.update({'font.family':'DejaVu Sans','font.size':10,'savefig.dpi':160})
    return plt
