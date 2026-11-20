import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
import argparse

def run(folder):
    p=RES/folder;t=np.load(p/'axes.npz')['times_s'];a=np.load(p/'photon_radiance.npy',mmap_mode='r')[:,0]
    centres=np.array([120.,150.,170.,240.,350.,490.,670.,830.]);centres=centres[(centres-5>=t[0]*1e9)&(centres+5<=t[-1]*1e9)]
    ids=np.unique(np.r_[np.arange(0,len(t),2),len(t)-1]);full=gate(t,a,centres);coarse=gate(t[ids],a[ids],centres)
    scale=OMEGA*(PIXEL*1e-6)**2;tot=full.sum((1,2))*scale;ctot=coarse.sum((1,2))*scale
    rows=[dict(numerical_ns=float(ns),full_photons=float(x),coarse_photons=float(y),relative_total_difference=float(abs(y-x)/max(x,1e-300)),absolute_difference_photons=float(abs(y-x))) for ns,x,y in zip(centres,tot,ctot)]
    write_json(p/'gate_time_resolution.json',dict(method='Exposure integrated using all available frames vs every other saved radiation frame; physical solver time stepping unchanged.',frames=rows))
    print(rows)

if __name__=='__main__':run(sys.argv[1])
