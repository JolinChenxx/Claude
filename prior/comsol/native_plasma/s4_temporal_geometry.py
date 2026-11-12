"""Temporal landmarks and physical copper positions, without image registration."""
from pathlib import Path
import numpy as np
from scipy.ndimage import label


def peak_landmark(times, observed, predicted, complete):
    times=np.asarray(times);observed=np.asarray(observed);predicted=np.asarray(predicted)
    if not len(times):return None
    k=int(np.argmax(observed));j=int(np.argmax(predicted))
    lo=float(times[max(0,k-1)]);hi=float(times[min(len(times)-1,k+1)])
    return dict(experiment_peak_ns=float(times[k]),simulation_peak_ns=float(times[j]),
                experimental_neighbor_interval_ns=[lo,hi],within_neighbor_interval=bool(lo<=times[j]<=hi),
                scope='full_sequence' if complete else 'covered_frames_only')


def extinction_landmark(times, obs_visible, sim_visible, complete):
    times=np.asarray(times)
    def interval(visible):
        ids=np.flatnonzero(visible)
        if not len(ids):return None
        k=int(ids[-1])
        return [float(times[k]),float(times[k+1])] if k+1<len(times) else [float(times[k]),None]
    a=interval(obs_visible);b=interval(sim_visible)
    bounded=a is not None and b is not None and a[1] is not None and b[1] is not None
    return dict(experiment_interval_ns=a,simulation_interval_ns=b,
                intervals_overlap=bool(max(a[0],b[0])<=min(a[1],b[1])) if bounded else None,
                scope='full_sequence' if complete else 'covered_frames_only')


def copper_geometry(case, center_times_ns):
    """Connected condensed target, rho_cond>100 and alpha_g<0.5, at gate centers.

    Connectivity to the bottom boundary distinguishes the target from detached
    condensed material. No experimental crop edge is identified with this surface.
    """
    from split_history import read_history
    root=Path(__file__).resolve().parent
    base=case[:-6] if case.endswith('native') else case
    folder=root/'results'/base
    if not (folder/'grid.npz').exists():return [None]*len(center_times_ns)
    g=np.load(folder/'grid.npz');z=g['z_m']*1e6;ze=g['axial_edges_m']*1e6
    h=read_history(base);t=np.array([s['saved_time_ns'] for s in h]);cache={}
    def sample(i):
        if i in cache:return cache[i]
        with np.load(h[i]['frame']) as d:
            U,W=d['U'],d['W'];rho=U[:,:,0]*(1-W[:,:,5])
            ids,_=label((rho>100)&(W[:,:,6]<.5))
            roots=np.unique(ids[:,0]);roots=roots[roots>0]
            connected=np.isin(ids,roots)
            heights=np.full(len(connected),np.nan)
            for ir,row in enumerate(connected):
                js=np.flatnonzero(row)
                if len(js):heights[ir]=ze[js[-1]+1]
            cache[i]=(float(heights[0]),float(np.nanmax(heights)))
        return cache[i]
    result=[]
    for center in center_times_ns:
        if center<t[0]-1e-6 or center>t[-1]+1e-6:result.append(None);continue
        j=int(np.clip(np.searchsorted(t,center),1,len(t)-1));i=j-1
        f=np.clip((center-t[i])/(t[j]-t[i]),0,1);v=(1-f)*np.array(sample(i))+f*np.array(sample(j))
        result.append(dict(copper_axis_top_um=float(v[0]),copper_connected_top_um=float(v[1])))
    return result
