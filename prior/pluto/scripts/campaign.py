"""Persistent, restartable parameter campaign. No LLM/API calls.

The controller only reads compact per-case result files. Windows Task Scheduler
can relaunch it; an OS file lock prevents overlapping controller instances.
"""
import os,sys,time,json,datetime as dt,subprocess,threading,traceback,shutil,ctypes
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import msvcrt
import yaml
from common import ROOT,TZ,utcnow,campaign,deadline,read_json,atomic_json,runtime_env
from case import DEFAULT,write_case,launch

LOCK=None
CONFIG=yaml.safe_load((ROOT/'config/campaign.yaml').read_text(encoding='utf-8'))

def admissible(result):
    return 'score' in result and abs(result.get('energy_residual',1))<.05 and abs(result.get('mass_residual',1))<.01
def lock_controller():
    global LOCK
    LOCK=(ROOT/'state/controller.lock').open('a+b');LOCK.seek(0)
    if LOCK.read(1)==b'':LOCK.write(b'0');LOCK.flush()
    LOCK.seek(0)
    try:msvcrt.locking(LOCK.fileno(),msvcrt.LK_NBLCK,1)
    except OSError:return False
    return True

def terminate_tree(proc):
    import psutil
    try:
        parent=psutil.Process(proc.pid)
        children=parent.children(recursive=True)
        for p in reversed(children):
            try:p.terminate()
            except psutil.Error:pass
        parent.terminate()
    except psutil.Error:pass

def execute(spec):
    folder=ROOT/'runs'/spec['run_id'];folder.mkdir(exist_ok=True,parents=True)
    ranks=spec['ranks'];resume=(folder/'dbl.out').exists()
    if not (folder/'params.json').exists():write_case(folder,spec['parameters'],spec['level'])
    record={**spec,'started_at':utcnow().isoformat(),'status':'running','attempt':0}
    atomic_json(folder/'job.json',record)
    for attempt in range(2):
        if utcnow()>=deadline():return {'run_id':folder.name,'status':'expired'}
        record['attempt']=attempt+1
        last_float=(folder/'flt.out').read_text().splitlines()[-1].split() if (folder/'flt.out').exists() else []
        solver_finished=bool(last_float and float(last_float[1])>=read_json(folder/'params.json').get('tstop_ns',1000)-1e-6)
        reason=''
        if solver_finished:code=0
        else:
            proc=launch(folder,ranks,restart=resume)
            record['pid']=proc.pid;atomic_json(folder/'job.json',record)
            limit={'coarse':3600,'medium':10800,'fine':28800}[spec['level']]
            begin=time.monotonic()
            while proc.poll() is None:
                if utcnow()>=deadline() or (ROOT/'state/STOP').exists():reason='campaign_stop';terminate_tree(proc);break
                if time.monotonic()-begin>limit:reason='case_timeout';terminate_tree(proc);break
                time.sleep(10)
            code=proc.wait()
        if code==0:
            log=folder/'postprocess.log'
            with log.open('a',encoding='utf-8') as out:
                post=subprocess.Popen([sys.executable,str(ROOT/'scripts/postprocess.py'),folder.name,'--fit-only'],
                      cwd=ROOT,env=runtime_env(),stdout=out,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
                try:rc=post.wait(timeout=1800)
                except subprocess.TimeoutExpired:terminate_tree(post);rc=-1
            if rc==0:
                result=read_json(folder/'result.json')
                record.update(status='completed',finished_at=utcnow().isoformat(),result=result)
                atomic_json(folder/'job.json',record);return result
            reason='postprocess_failed';break
        if reason:break
        # One deterministic retry from a valid checkpoint, with a smaller CFL.
        resume=(folder/'dbl.out').exists()
        ini=folder/'pluto.ini';s=ini.read_text();s=s.replace('CFL 0.25','CFL 0.15');ini.write_text(s)
    record.update(status='failed',finished_at=utcnow().isoformat(),reason=reason or f'solver_exit_{code}')
    atomic_json(folder/'job.json',record);return {'run_id':folder.name,'status':'failed','reason':record['reason']}

def encode(p):
    import numpy as np
    return np.array([np.log(p['mass_ng']),np.log(p['energy_uj']),np.log(p['kinetic_frac']),p['tail_frac'],np.log(p['tail_ns'])])

def choose_parameters(rng,results,count,elapsed):
    import numpy as np
    low=np.array([np.log(.7),np.log(12),np.log(.0005),0,np.log(15)])
    high=np.array([np.log(16),np.log(180),np.log(.12),.4,np.log(180)])
    bounds=CONFIG['parameter_bounds']
    low=np.array([np.log(bounds[k][0]) if k!='tail_frac' else bounds[k][0] for k in ['mass_ng','energy_uj','kinetic_frac','tail_frac','tail_ns']])
    high=np.array([np.log(bounds[k][1]) if k!='tail_frac' else bounds[k][1] for k in ['mass_ng','energy_uj','kinetic_frac','tail_frac','tail_ns']])
    ranked=sorted([r for r in results if admissible(r) and np.isfinite(r['score'])],key=lambda r:r['score'])
    if ranked and count>=16 and rng.random()>.25:
        parent=ranked[int(rng.integers(min(8,len(ranked))))]['parameters']['parameters']
        shrink=.22 if elapsed<36 else .10
        vector=np.clip(encode(parent)+rng.normal(0,shrink,5)*(high-low),low,high)
        waist=parent['waist_um'] if rng.random()>.12 else float(rng.choice(bounds['waist_um_choices']))
    else:vector=rng.uniform(low,high);waist=float(rng.choice(bounds['waist_um_choices']))
    p=DEFAULT.copy();p.update(mass_ng=float(np.exp(vector[0])),energy_uj=float(np.exp(vector[1])),
        kinetic_frac=float(np.exp(vector[2])),tail_frac=float(vector[3]),tail_ns=float(np.exp(vector[4])),
        waist_um=waist,source_height_um=max(3,waist/4))
    p.update(CONFIG.get('fixed',{}))
    return p

def compact(folder):
    # Only discard generated raw field/checkpoint files of completed, non-best cases.
    # Retain parameters, budget, projection, synthesized frames, score, logs and source provenance.
    for pat in ['data.*.flt','data.*.dbl']:
        for p in folder.glob(pat):p.unlink(missing_ok=True)
    atomic_json(folder/'compacted.json',{'at':utcnow().isoformat(),'retained':'projection, images, metrics, budget, parameters, logs'})

def publish(result):
    from postprocess import render
    render(ROOT/'runs'/result['run_id'],publish=True)
    text=(f"# 当前最佳结果\n\n算例：{result['run_id']}\n\n综合损失：{result['score']:.6g}\n\n"
          f"模型范围：{result['result_level']}\n\n"
          "图片：figures/best_comparison.png；动画：figures/best_comparison.gif。\n")
    (ROOT/'reports/BEST.md').write_text(text,encoding='utf-8')

def finalize(results,reason='deadline'):
    from report import hourly,pdf_report
    from postprocess import export_hdf5
    eligible=[r for r in results if admissible(r)]
    refined=[r for r in eligible if r['parameters']['level']=='fine']
    best=min(refined or eligible,key=lambda r:r['score']) if eligible else read_json(ROOT/'state/best.json')
    if best:
        publish(best)
        try:
            if not (ROOT/'runs'/best['run_id']/'fields.h5').exists():export_hdf5(ROOT/'runs'/best['run_id'])
        except Exception as e:
            with (ROOT/'logs/finalize.log').open('a',encoding='utf-8') as f:f.write('HDF5: '+repr(e)+'\n')
    atomic_json(ROOT/'state/archive.json',results)
    from final_checks import compare_grids,imaging_sensitivity
    try:
        compare_grids(results)
        if best:imaging_sensitivity(ROOT/'runs'/best['run_id'])
    except Exception:
        with (ROOT/'logs/finalize.log').open('a',encoding='utf-8') as f:f.write(traceback.format_exc())
    st=read_json(ROOT/'state/status.json');st.update(phase='72 小时计算结束',running=[],
        next_action='查看最终报告、最佳参数及剩余差异',ended_at=utcnow().isoformat(),stop_reason=reason,
        best=best,completed=len(results))
    atomic_json(ROOT/'state/status.json',st);hourly();pdf_report('final')
    c=campaign();c.update(status='finished',finished_at=utcnow().isoformat());atomic_json(ROOT/'state/campaign.json',c)
    (ROOT/'reports/RESULT_INDEX.md').write_text(
        '# S4 结果索引\n\n- 最终报告：final_report.pdf\n- 最优信息：BEST.md\n'
        '- 拼图：../figures/best_comparison.png\n- 动画：../figures/best_comparison.gif\n'
        '- 连续时间演化：../figures/best_evolution.gif\n'
        '- 曲线：../figures/best_metrics.png\n- 预算：../figures/best_budget.png\n'
        '- 参数记录：../state/archive.json\n- 分辨率对比：refinement_comparison.json\n'
        '- 模型说明：../MODEL.md\n',encoding='utf-8')
    ps="'S4Plume_Hourly','S4Plume_Daily0700','S4Plume_Watchdog' | ForEach-Object { Disable-ScheduledTask -TaskName $_ -ErrorAction SilentlyContinue | Out-Null }"
    subprocess.run(['powershell.exe','-NoProfile','-Command',ps],creationflags=subprocess.CREATE_NO_WINDOW)

def main():
    if not lock_controller():return
    if campaign().get('status')=='finished':return
    import numpy as np
    import psutil
    # Keeps the workstation awake while this process is alive; no global power settings changed.
    ctypes.windll.kernel32.SetThreadExecutionState(0x80000001)
    atomic_json(ROOT/'state/controller.json',dict(pid=os.getpid(),started_at=utcnow().isoformat()))
    c=campaign();c['status']='running';atomic_json(ROOT/'state/campaign.json',c)
    started=dt.datetime.fromisoformat(c['started_at']);rng=np.random.default_rng(CONFIG['random_seed'])
    saved_rng=read_json(ROOT/'state/rng.json')
    if saved_rng:rng.bit_generator.state=saved_rng
    results=[];pending=[];counter=0
    for folder in sorted((ROOT/'runs').iterdir()):
        if not folder.is_dir() or folder.name=='smoke':continue
        result=read_json(folder/'result.json')
        if result:results.append(result)
        job=read_json(folder/'job.json')
        if job and not result and job.get('status')=='running':
            pid=job.get('pid')
            # A controller crash may leave a solver alive. End only its recorded process tree before restart.
            if pid and psutil.pid_exists(pid):
                try:
                    proc=psutil.Process(pid)
                    if 'mpiexec' in proc.name().lower() and str(ROOT/'solver/pluto.exe').lower() in ' '.join(proc.cmdline()).lower():terminate_tree(proc)
                except psutil.Error:pass
            pending.append(job)
        if folder.name.startswith('case_'):
            try:counter=max(counter,int(folder.name.split('_')[1])+1)
            except ValueError:pass
    best=read_json(ROOT/'state/best.json');bestscore=best.get('score',float('inf'))
    fine_seen={json.dumps(r['parameters']['parameters'],sort_keys=True) for r in results if r.get('parameters',{}).get('level')=='fine'}
    fine_seen.update(json.dumps(j['parameters'],sort_keys=True) for j in pending if j['level']=='fine')
    active={};failed=sum(read_json(p/'job.json').get('status')=='failed' for p in (ROOT/'runs').iterdir() if p.is_dir())
    pool=ThreadPoolExecutor(max_workers=8);last_save=0;prepared=False
    try:
        while utcnow()<deadline() and not (ROOT/'state/STOP').exists():
            elapsed=(utcnow()-started).total_seconds()/3600
            for fut,spec in list(active.items()):
                if not fut.done():continue
                del active[fut]
                try:result=fut.result()
                except Exception:
                    failed+=1
                    with (ROOT/'logs/controller_errors.log').open('a',encoding='utf-8') as f:f.write(traceback.format_exc())
                    continue
                if 'score' not in result:failed+=1;continue
                results.append(result)
                valid=admissible(result)
                if valid and result['score']<bestscore:
                    bestscore=result['score'];best=result;publish(result)
                # Keep full fields of the ten best and all refined runs.
                ranked=sorted([r for r in results if admissible(r)],key=lambda r:r['score']);keep={r['run_id'] for r in ranked[:10]}
                if best:keep.add(best['run_id'])
                for old in results:
                    folder=ROOT/'runs'/old['run_id']
                    if old['run_id'] not in keep and old['parameters']['level']!='fine' and not (folder/'compacted.json').exists():compact(folder)
            if elapsed<CONFIG['stages']['coarse_search_until_h']:
                phase='粗网格并行参数搜索';level='coarse';workers=CONFIG['workers'];ranks=CONFIG['ranks_per_case'];next_action='优选全局源项参数并完善全时序形貌'
            elif elapsed<CONFIG['stages']['medium_search_until_h']:
                phase='中等网格集中形貌优化';level='medium';workers=CONFIG['workers'];ranks=CONFIG['ranks_per_case'];next_action='围绕候选参数优化尺寸、亮核和衰减'
            elif elapsed<CONFIG['stages']['fine_search_until_h']:
                phase='最佳候选加密计算与有限对比';level='fine';workers=CONFIG['fine_workers'];ranks=CONFIG['fine_ranks'];next_action='完成最佳候选精算与分辨率比较'
            else:
                phase='冻结结果与最终整理';level='fine';workers=0;ranks=32;next_action='导出最终数据、动画及报告'
            used=sum(x['ranks'] for x in active.values())
            if elapsed>=CONFIG['stages']['fine_search_until_h'] and not active and not prepared:
                eligible=[r for r in results if admissible(r)]
                fine=[r for r in eligible if r['parameters']['level']=='fine']
                if eligible:
                    chosen=min(fine or eligible,key=lambda r:r['score'])
                    from final_checks import compare_grids,imaging_sensitivity
                    from postprocess import export_hdf5
                    try:
                        publish(chosen);best=chosen;bestscore=chosen['score']
                        compare_grids(results);imaging_sensitivity(ROOT/'runs'/chosen['run_id'])
                        export_hdf5(ROOT/'runs'/chosen['run_id'])
                        atomic_json(ROOT/'state/final_prepared.json',dict(run_id=chosen['run_id'],at=utcnow().isoformat()))
                    except Exception:
                        with (ROOT/'logs/finalize.log').open('a',encoding='utf-8') as f:f.write(traceback.format_exc())
                prepared=True
            reserve=max(30*1024**3,psutil.virtual_memory().total-CONFIG['memory_budget_gib']*1024**3)
            while len(active)<workers and used+(pending[0]['ranks'] if pending else ranks)<=CONFIG['cpu_budget'] and psutil.virtual_memory().available>reserve:
                if pending:spec=pending.pop(0)
                else:
                    if level=='fine':
                        ranked=sorted([r for r in results if admissible(r)],key=lambda r:r['score']);candidates=[]
                        for r in ranked:
                            p=r['parameters']['parameters'];key=json.dumps(p,sort_keys=True)
                            if key not in fine_seen:candidates.append((p,key))
                        if not candidates or len(fine_seen)>=6:break
                        p,key=candidates[0];fine_seen.add(key)
                    else:p=choose_parameters(rng,results,counter,elapsed)
                    spec=dict(run_id=f'case_{counter:05d}_{level}',parameters=p,level=level,ranks=ranks)
                    counter+=1
                active[pool.submit(execute,spec)]=spec;used+=spec['ranks']
            if time.monotonic()-last_save>20:
                attention=None
                if failed>max(5,len(results)//3):attention='失败比例偏高；详见各算例日志及 controller_errors.log。'
                status=dict(updated_at=utcnow().isoformat(),phase=phase,completed=len(results),failed=failed,
                    running=[dict(run_id=s['run_id'],ranks=s['ranks'],level=s['level']) for s in active.values()],
                    best=best,next_action=next_action,result_level=best.get('result_level','初版物理模型正在搜索参数'),
                    attention=attention,used_ranks=used)
                atomic_json(ROOT/'state/status.json',status);atomic_json(ROOT/'state/archive.json',results)
                atomic_json(ROOT/'state/rng.json',rng.bit_generator.state)
                last_save=time.monotonic()
            time.sleep(15)
    except Exception:
        with (ROOT/'logs/controller_errors.log').open('a',encoding='utf-8') as f:f.write(traceback.format_exc())
        # Leave recorded jobs/checkpoints for the watchdog's next controller instance.
        os._exit(1)
    finally:
        pool.shutdown(wait=True)
        for fut in active:
            try:
                r=fut.result()
                if 'score' in r:results.append(r)
            except Exception:pass
        finalize(results,'stop_file' if (ROOT/'state/STOP').exists() else 'deadline')
        ctypes.windll.kernel32.SetThreadExecutionState(0x80000000)

if __name__=='__main__':main()
