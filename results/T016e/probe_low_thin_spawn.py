"""Small Windows-spawn check using the actual image worker and one snapshot."""
import multiprocessing as mp
from concurrent.futures import ProcessPoolExecutor
from lipsim.image.run import _init_worker, _snapshot_images
from pathlib import Path
from lipsim import params as P
from lipsim.io import pluto
import yaml, numpy as np, json

def main():
    repo=Path(__file__).resolve().parents[2]
    sc=P.resolve(yaml.safe_load((repo/'configs/imaging/v0.yaml').read_text()))
    meta=yaml.safe_load((repo/sc['series']['low']['frames']).read_text())['sets']['low']
    run=Path('D:/sim_runs/T016e/low_thin_G0/it1')
    idx=pluto.read_index(run/'dbl.out')
    n=min(idx,key=lambda x:abs(x[1]-300))[0]
    arg=(n,sc['filters'],tuple(sc['numerics']['self_absorption']),sc['numerics'],sc['series']['low']['um_per_px'],meta['frame_px'])
    with ProcessPoolExecutor(2,mp_context=mp.get_context('spawn'),initializer=_init_worker,initargs=('D:/sim_runs/tables/v1c',str(run))) as pool:
        number,images=pool.submit(_snapshot_images,arg).result(timeout=90)
    assert len(images)==6 and all(np.isfinite(x).all() for x in images.values())
    print(json.dumps(dict(snapshot=number,images=len(images),all_finite=True)))
if __name__=='__main__': main()
