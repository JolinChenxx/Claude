"""Serial audited continuation + mesh/time verification within session limits.

Only time-budget stops with otherwise passing audits are continued unchanged.
Any numerical/physics failure returns for review. No parallel COMSOL solves.
Both verification settings first cover every S4 gate, then remaining budget
extends their late history. Read-only postprocessing is serial to reserve CPUs
for the fixed solver wall-time allowance.
"""
from pathlib import Path
import argparse,datetime,json,subprocess,sys,time,os
import numpy as np
from session_runtime import solver_seconds_left
from atomic_json64 import write_json
ROOT=Path(__file__).resolve().parents[1];WORK=ROOT/'work';RES=ROOT/'results';LOG=ROOT/'logs'
STATE=ROOT/'delivery_state.json'
def write_state(**kw):
    d=json.loads(STATE.read_text(encoding='utf8')) if STATE.exists() else {}
    d.update(kw,updated_beijing=datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat())
    write_json(STATE,d)
def call(*args):
    print('RUN',*map(str,args),flush=True)
    subprocess.run([sys.executable,*map(str,args)],cwd=WORK,check=True)
def budget_left():
    b=json.loads((ROOT/'solver_budget.json').read_text(encoding='utf-8-sig'));return min(solver_seconds_left(),b['budget_seconds']-sum(r['elapsed_seconds'] for r in b['runs']))
def run_case(case,stop_after_s4=False,early_switch=False):
    # The existing active worker owns its case; never spawn a duplicate.
    early_switch_ns=float(os.environ.get('S4_EARLY_SWITCH_NS','300'))
    if not np.isfinite(early_switch_ns) or early_switch_ns<=0:raise ValueError('Invalid early Newton checkpoint time')
    proc=None
    if (ROOT/'worker.lock').exists():
        s=json.loads((ROOT/'status.json').read_text(encoding='utf-8-sig'))
        if s['case']!=case:raise RuntimeError('Another serial case owns the worker: '+s['case'])
        write_state(phase='waiting_for_case_audit',active_case=case)
    elif not (WORK/f'{case}.mph').exists():
        write_state(phase='solving',active_case=case)
        proc=subprocess.Popen([sys.executable,str(WORK/'campaign.py'),case,'--threads',os.environ.get('S4_THREADS','8')],cwd=WORK)
        # Let the child acquire its serial worker lock before polling it.
        while proc.poll() is None and not (ROOT/'worker.lock').exists():time.sleep(.2)
    while (ROOT/'worker.lock').exists():
        s=json.loads((ROOT/'status.json').read_text(encoding='utf-8-sig'))
        if early_switch and s.get('case')==case and s.get('state')=='solving' and s.get('last_accepted_time_s',0)>=early_switch_ns*1e-9:
            request=ROOT/'STOP_REQUEST.json'
            if not request.exists():request.write_text(json.dumps({'case':case,'reason':f'early_Newton_checkpoint: early minimal/constant-1 strategy reached {early_switch_ns:g}ns; preserve real history and use automatic damping/current Jacobian across the known later nonlinear region.'}))
        if stop_after_s4 and s.get('case')==case and s.get('state')=='solving' and s.get('last_accepted_time_s',0)>=865e-9:
            request=ROOT/'STOP_REQUEST.json'
            if not request.exists():
                request.write_text(json.dumps({'case':case,'reason':'S4_window_checkpoint: save real history after all allowed S4 gates; reserve budget for the other independent verification.'}))
        time.sleep(2)
    if proc and proc.wait():raise RuntimeError('Campaign worker failed: '+case)
    p=RES/f'{case}_audit.json'
    if not p.exists():raise RuntimeError('Saved case lacks completed audit: '+case)
    return json.loads(p.read_text())
def to_target(first,stop_after_s4=False,early_switch=False):
    case=first;prior=-1;restarts=0
    while True:
        a=run_case(case,stop_after_s4,early_switch);write_state(last_audited_case=case,last_audit=a['checks'])
        failed=[k for k,v in a['checks'].items() if not v and k!='complete']
        if failed:raise RuntimeError(case+' requires numerical review: '+','.join(failed))
        if a['checks']['complete']:return case
        if budget_left()<120:return case
        b=json.loads((ROOT/'solver_budget.json').read_text(encoding='utf-8-sig'));rr=[r for r in b['runs'] if r['case']==case][-1]
        end=a['metadata']['last_time_s']
        reason=rr.get('stop_reason') or ''
        if reason.startswith('early_Newton_checkpoint:'):
            new=first+'A'
            call(WORK/'run_standalone.py','ResumeS4',WORK/f'{case}.mph',WORK/f'{new}_ready.mph','onevery','auto')
            (WORK/f'{new}_resume.json').write_text(json.dumps({'history_source':case+'.mph','same_equations_and_mesh':True,'source_saved_end_s':end,'continuation_change':'Planned Newton strategy transition only: early minimal/constant-1 to current Jacobian/automatic damping at the recorded actual checkpoint. Discretization and tolerances unchanged.','stop_reason':reason,'merge':'Official CombineSolution of full real history.'},indent=2))
            early_switch=False;case=new;continue
        if not rr.get('official_stop_requested') or not (reason=='solve_time_budget' or reason.startswith('S4_window_checkpoint:')):raise RuntimeError('A non-budget stop requires explicit review before unchanged continuation: '+case)
        if stop_after_s4 and end>=865e-9:return case
        if end-prior<2e-9:raise RuntimeError('Unchanged continuation made insufficient progress: '+case)
        prior=end;restarts+=1;new=f'{first}C{restarts}'
        while (WORK/f'{new}.mph').exists() or (WORK/f'{new}_ready.mph').exists():
            restarts+=1;new=f'{first}C{restarts}'
        call(WORK/'run_standalone.py','ResumeS4',WORK/f'{case}.mph',WORK/f'{new}_ready.mph')
        (WORK/f'{new}_resume.json').write_text(json.dumps({'history_source':case+'.mph','same_equations_and_mesh':True,'source_saved_end_s':end,'continuation_change':'No physical or numerical setting change. Official session-limited checkpoint; restart with configured S4_THREADS='+os.environ.get('S4_THREADS','8')+'.','merge':'Official CombineSolution concatenation; full real history audited.'},indent=2))
        case=new
def visible(case):
    csvpath=RES/f'{case}_visible_budget.csv'
    if not csvpath.exists():call(WORK/'run_standalone.py','ExportVisible',WORK/f'{case}.mph',csvpath,ROOT/'data/visible_emission_tables.txt')
    x=np.loadtxt(csvpath,delimiter=',',skiprows=1,ndmin=2);energy=np.trapz(x[:,1:],x=x[:,0],axis=0)
    a=json.loads((RES/f'{case}_audit.json').read_text());ratio=float(sum(energy[:2])/(a['absorbed_uJ']*1e-6))
    d={'case':case,'wavelength_nm':[380,780],'time_range_s':[float(x[0,0]),float(x[-1,0])],'line_emitted_energy_J':float(energy[0]),'free_free_emitted_energy_J':float(energy[1]),'existing_all_frequency_ff_loss_J':float(energy[2]),'visible_upper_bound_to_absorbed_ratio':ratio,'feedback_threshold':.05,'upper_bound_below_feedback_threshold':ratio<.05,'full_FE_domain_with_ALE_J':True,'caveat':'Bound covers the specified atomic-line and free-free closure; omitted free-bound/molecular/higher-ion contributions are not estimated.'}
    (RES/f'{case}_visible_summary.json').write_text(json.dumps(d,indent=2));write_state(visible_energy=d)
    if ratio>=.05:raise RuntimeError('Visible upper bound exceeds 5%; resolve net escape/feedback before validation.')
def start_post(case,reference=None):
    args=[sys.executable,str(WORK/'postprocess_case.py'),case]
    a=json.loads((RES/f'{case}_audit.json').read_text())
    if a['status']!='passed':args+=['--diagnostic']
    if reference:args+=['--reference',reference]
    # FDs are inherited by the subprocess and closed locally immediately.
    with (LOG/f'{case}_post_stdout.txt').open('wb') as fo,(LOG/f'{case}_post_stderr.txt').open('wb') as fe:
        return subprocess.Popen(args,cwd=WORK,stdout=fo,stderr=fe)
def main():
    if (ROOT/'PAUSED_BY_USER.json').exists():raise RuntimeError('Paused for server transfer; unpause explicitly before starting this coordinator.')
    parser=argparse.ArgumentParser();parser.add_argument('initial',nargs='?',default='H14');parser.add_argument('--grid-start',default='VG');parser.add_argument('--time-start',default='VT');parser.add_argument('--early-switch',action='store_true');parser.add_argument('--grid-current-strategy',action='store_true');parser.add_argument('--late-constant',action='store_true');args=parser.parse_args()
    def extend_late(case):
        aa=json.loads((RES/f'{case}_audit.json').read_text())
        if aa['checks']['complete']:return case
        if not args.late_constant:return to_target(case)
        # Direct late-cooling attempt, after both real S4 windows are saved.
        # Same equations/discretization/tolerances; no diffusion alteration.
        new=case+'L'
        call(WORK/'run_standalone.py','ResumeS4',WORK/f'{case}.mph',WORK/f'{new}_ready.mph','minimal','const','preserve','1')
        (WORK/f'{new}_resume.json').write_text(json.dumps({'history_source':case+'.mph','same_equations_and_mesh':True,'source_saved_end_s':aa['metadata']['last_time_s'],'continuation_change':'Late cooling after the saved S4 window: minimal Jacobian / constant Newton damping 1. Test actual Jacobian reuse; no change of physical equations, mesh, time-step limit, tolerance or stabilized fluxes. Stop for review on actual failure.','merge':'Official CombineSolution of real saved histories.'},indent=2))
        return to_target(new)
    write_state(phase='main_continuation',error=None,main_case=None,grid_case=None,time_case=None)
    initial=args.initial
    prior_audit=RES/f'{initial}_audit.json'
    maincase=initial if prior_audit.exists() and json.loads(prior_audit.read_text())['status']=='passed' else to_target(initial)
    write_state(main_case=maincase)
    a=json.loads((RES/f'{maincase}_audit.json').read_text())
    if not a['checks']['complete']:write_state(phase='budget_exhausted');return
    visible(maincase)
    post_audit=RES/f'{maincase}_postprocess.json'
    if not post_audit.exists():
        mainpost=start_post(maincase)
        write_state(phase='main_postprocessing',main_post_pid=mainpost.pid)
        if mainpost.wait():raise RuntimeError('Main postprocessing failed; see '+maincase+'_post_stderr.txt')
    write_state(phase='mesh_S4_window_verification')
    gridcase=to_target(args.grid_start,stop_after_s4=True,early_switch=args.early_switch and not args.grid_current_strategy) if budget_left()>=120 else None
    write_state(grid_case=gridcase)
    refresh=ROOT/'main_post_refresh_required.json'
    if refresh.exists():
        write_state(phase='main_camera_correction')
        if start_post(maincase).wait():raise RuntimeError('Main camera correction failed')
        correction=json.loads(refresh.read_text());correction['completed_epoch']=time.time()
        refresh.write_text(json.dumps(correction,indent=2),encoding='utf8')
        refresh.replace(ROOT/'main_post_refresh_completed.json')
    write_state(phase='time_step_S4_window_verification')
    timecase=to_target(args.time_start,stop_after_s4=True,early_switch=args.early_switch) if budget_left()>=120 else None
    write_state(time_case=timecase)
    # Extend the time-step history first, preserving both actual S4 windows.
    if timecase and budget_left()>=120:
        write_state(phase='time_step_late_extension');timecase=extend_late(timecase);write_state(time_case=timecase)
    if gridcase and budget_left()>=120:
        write_state(phase='mesh_late_extension');gridcase=extend_late(gridcase);write_state(grid_case=gridcase)
    write_state(phase='verification_postprocessing')
    for variant in [gridcase,timecase]:
        if variant and json.loads((RES/f'{variant}_audit.json').read_text())['metadata']['last_time_s']>=865e-9:
            visible(variant)
            if start_post(variant,maincase).wait():raise RuntimeError('Verification postprocessing failed: '+variant)
    variants=[c for c in [gridcase,timecase] if c and (RES/f'{c}_camera/summary.json').exists()]
    if variants:call(WORK/'compare_numerics.py',maincase,*variants)
    full=bool(gridcase and timecase and all(json.loads((RES/f'{c}_audit.json').read_text())['status']=='passed' for c in [gridcase,timecase]))
    write_state(phase='ready_for_final_review' if full else 'budget_exhausted',all_flow_cases_complete=full,active_case=None)
if __name__=='__main__':
    try:main()
    except Exception as e:write_state(phase='needs_review',error=str(e));raise
