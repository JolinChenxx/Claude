"""Finite, nonempty convergence checks independent of camera saturation."""
import numpy as np

def assess(main,variant,times=None,floor=1e-100):
    a=np.asarray(main,dtype=float);b=np.asarray(variant,dtype=float)
    result={'within_10pct':False,'status':'failed','active_frames':0,
            'max_relative_change_above_1pct_peak':None,'new_signal_when_main_zero':False}
    if a.shape!=b.shape or not a.size:
        return dict(result,reason='shape_mismatch_or_empty')
    if not np.all(np.isfinite(a)) or not np.all(np.isfinite(b)):
        return dict(result,reason='nonfinite_values')
    threshold=max(float(np.max(a))*.01,floor)
    active=a>=threshold
    result.update(peak_main=float(a.max()),peak_variant=float(b.max()),
                  activity_threshold=threshold,active_frames=int(active.sum()),
                  new_signal_when_main_zero=bool(np.any((~active)&(b>threshold))))
    if not active.any():
        return dict(result,status='not_evaluable',reason='no_active_reference_signal')
    rel=np.abs(b[active]-a[active])/np.maximum(np.abs(a[active]),floor)
    error=float(rel.max());passed=error<=.1 and not result['new_signal_when_main_zero']
    result.update(max_relative_change_above_1pct_peak=error,within_10pct=passed,
                  status='passed' if passed else 'failed',reason=None if passed else 'relative_change_or_new_signal')
    if times is not None:result['active_times_ns']=np.asarray(times)[active].tolist()
    return result

def combined(camera,physical):
    return bool(all(x['within_10pct'] for x in camera.values()) and physical['within_10pct'])
