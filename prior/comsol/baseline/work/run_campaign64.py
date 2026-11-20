"""One supervised 6.4 campaign; all verification branches start from initial data."""
from pathlib import Path
import json,os,sys,time
from finish_campaign import to_target,write_state,visible,start_post,call,WORK,ROOT,RES,budget_left
from session_runtime import coordinator_lock

def main():
    if (ROOT/'PAUSED_BY_USER.json').exists():raise RuntimeError('Migration has not been acknowledged')
    if not (RES/'H15R64_postprocess.json').exists():raise RuntimeError('Reference camera refresh is incomplete')
    if not (RES/'thread_benchmark64.json').exists():raise RuntimeError('Thread benchmark is incomplete')
    c=json.loads((ROOT/'session_64.json').read_text());os.environ['S4_THREADS']=str(c['solver_threads']);os.environ['S4_POST_THREADS']=str(c['post_threads'])
    write_state(phase='baseline_64',main_case=None,grid_case=None,time_case=None,error=None,all_flow_cases_complete=False)
    maincase=to_target('B64',early_switch=True)
    write_state(main_case=maincase)
    if not json.loads((RES/f'{maincase}_audit.json').read_text())['checks']['complete']:
        write_state(phase='wall_deadline',active_case=None);return
    visible(maincase)
    write_state(phase='baseline_64_postprocessing')
    if start_post(maincase).wait():raise RuntimeError('6.4 baseline postprocessing failed')
    write_state(phase='time_step_64_S4_window')
    timecase=to_target('T64',stop_after_s4=True,early_switch=True) if budget_left()>120 else None
    write_state(time_case=timecase,phase='grid_64_S4_window')
    gridcase=to_target('G64',stop_after_s4=True,early_switch=True) if budget_left()>120 else None
    write_state(grid_case=gridcase)
    for kind,case in [('time',timecase),('grid',gridcase)]:
        if case and budget_left()>120:
            write_state(phase=kind+'_64_late_extension')
            result=to_target(case)
            if kind=='time':timecase=result;write_state(time_case=result)
            else:gridcase=result;write_state(grid_case=result)
    write_state(phase='verification_64_postprocessing')
    usable=[]
    for case in [timecase,gridcase]:
        if case and json.loads((RES/f'{case}_audit.json').read_text())['metadata']['last_time_s']>=865e-9:
            visible(case)
            if start_post(case,maincase).wait():raise RuntimeError('Verification postprocessing failed '+case)
            usable.append(case)
    if usable:call(WORK/'compare_numerics.py',maincase,*usable)
    full=len(usable)==2 and all(json.loads((RES/f'{case}_audit.json').read_text())['status']=='passed' for case in usable)
    write_state(phase='ready_for_final_review',all_flow_cases_complete=full,active_case=None)

if __name__=='__main__':
    try:
        with coordinator_lock('run_campaign64'):main()
    except Exception as e:write_state(phase='needs_review',error=str(e));raise
