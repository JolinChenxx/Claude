from concurrent.futures import ProcessPoolExecutor
import multiprocessing as mp
import json,os,psutil,sys
from lipsim.image.run import _init_worker

def probe(x):
 import numpy as np
 from scipy.ndimage import gaussian_filter
 assert np.isfinite(gaussian_filter(np.ones((4,4)),1)).all()
 assert np.isfinite(np.linalg.svd(np.ones((3,3)))[1]).all()
 return {"pid":os.getpid(),"affinity":psutil.Process().cpu_affinity(),"value":x}

if __name__ == "__main__":
 if "--smt" in sys.argv: psutil.Process().cpu_affinity(list(range(1,48,2)))
 with ProcessPoolExecutor(24,mp_context=mp.get_context("spawn"),initializer=_init_worker,initargs=("D:/sim_runs/tables/v1c","D:/sim_runs/T021/high_thin_G0/it2")) as pool:
  print(json.dumps(list(pool.map(probe,range(48)))))
