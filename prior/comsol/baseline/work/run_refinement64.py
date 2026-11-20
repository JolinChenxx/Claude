"""One explicitly planned extra mesh level, after the original coordinator exits."""
from pathlib import Path
import argparse,json,os
from finish_campaign import to_target,write_state,visible,start_post,call,WORK,ROOT,RES,budget_left
from session_runtime import coordinator_lock

def main(reviewed_restart=None):
    state=json.loads((ROOT/'delivery_state.json').read_text(encoding='utf-8-sig'))
    if state.get('phase')!='ready_for_final_review' and not reviewed_restart:raise RuntimeError('Original three-case campaign must finish first')
    if reviewed_restart:
        provenance=json.loads((WORK/f'{reviewed_restart}_resume.json').read_text())
        source=Path(provenance['history_source']).stem
        sourceaudit=json.loads((RES/f'{source}_audit.json').read_text())
        if any(not v for k,v in sourceaudit['checks'].items() if k!='complete'):raise RuntimeError('Reviewed restart source has numerical failures')
        if not (RES/f'{source}_recovery_review64.json').exists():raise RuntimeError('Restart requires recorded incident review')
    comparison=json.loads((RES/'numerical_comparison.json').read_text())
    base=state['main_case'];grid=state['grid_case'];timecase=state['time_case']
    if comparison['variants'][grid]['all_camera_and_physical_within_10pct']:
        raise RuntimeError('Original full grid comparison passed; inspect whether extra refinement is necessary')
    if not json.loads((RES/'G264_preparation64.json').read_text())['passed']:raise RuntimeError('Refined initial settings not verified')
    config=json.loads((ROOT/'session_64.json').read_text())
    os.environ['S4_THREADS']=str(config['solver_threads']);os.environ['S4_POST_THREADS']=str(config['post_threads'])
    # The previous refined mesh stalled at 259 ns with a reused Jacobian.
    # Use the already validated automatic Newton strategy before that region.
    os.environ['S4_EARLY_SWITCH_NS']='240'
    if budget_left()<120:raise RuntimeError('No remaining solve allowance')
    write_state(phase='extra_grid_refinement_64',extra_grid_initial='G264',error=None)
    fine=to_target(reviewed_restart or 'G264',early_switch=not bool(reviewed_restart))
    audit=json.loads((RES/f'{fine}_audit.json').read_text())
    write_state(extra_grid_case=fine,phase='extra_grid_postprocessing_64')
    if audit['metadata']['last_time_s']<865e-9:
        write_state(phase='ready_for_final_review',extra_grid_status='incomplete_S4_coverage',active_case=None);return
    visible(fine)
    if start_post(fine,base).wait():raise RuntimeError('Extra grid postprocessing failed')
    call(WORK/'compare_numerics.py',base,fine,'--output-stem','extra_grid_vs_baseline64')
    call(WORK/'compare_numerics.py',grid,fine,'--output-stem','extra_grid_adjacent64')
    call(WORK/'compare_flow64.py',base,grid,timecase,fine)
    call(WORK/'animate_fields64.py',fine)
    adjacent=json.loads((RES/'extra_grid_adjacent64.json').read_text())['variants'][fine]
    write_state(phase='ready_for_final_review',extra_grid_status=audit['status'],
                extra_grid_adjacent_converged=adjacent['all_camera_and_physical_within_10pct'],active_case=None)

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--reviewed-restart');args=parser.parse_args()
    try:
        with coordinator_lock('run_refinement64'):main(args.reviewed_restart)
    except Exception as e:
        write_state(phase='needs_review',error=str(e));raise
