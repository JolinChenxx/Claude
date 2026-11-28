"""Produce the two requested dated PDF snapshots; the active task reviews delivery.

This process reads saved artifacts only. It does not alter models or message users.
"""
from pathlib import Path
from datetime import datetime,timezone,timedelta
import json,time,subprocess,sys
R=Path(__file__).resolve().parent;TZ=timezone(timedelta(hours=8))
MARK=Path('C:/Users/jun/.codex/plugins/cache/openai-primary-runtime/pdf/26.1030.10813/skills/pdf/container_tools/mark_artifact_operation_started.mjs')

def content():
    cfg=json.loads((R/'day_session_20261110.json').read_text(encoding='utf8'))
    cases={};figures=[];findings=[]
    findings.append('16:52修正了材料通量的传播速度：电子压力已进入动量，但旧波速漏算电子贡献，造成强电离前沿非物理冷却。相同94 ns保存态补齐波速后通过94.25 ns材料短算，最低温约292 K。生产候选从初态重算，短算不计为新增完整曝光。')
    for case in cfg['cases']:
        path=R/'results'/f'{case}_stage.json'
        if not path.exists():continue
        s=json.loads(path.read_text());end=s.get('saved_end_ns')
        if end is None:continue
        fixed=s.get('fixed_plane',False)
        cases[case]=('固定液面' if fixed else '原空间模型')+'；原生空间Plasma，保存解覆盖以实际导出为准'
        for suffix in ['chainmapped','mapped']:
            gate=R/'results'/f'{case}{suffix}_gate_sequence.json'
            if gate.exists():
                q=json.loads(gate.read_text());times=[x['experiment_time_ns'] for x in q['rows']]
                findings.append(f'{case}已保存至{end:.3f} ns；已生成完整曝光{len(times)}/29帧，对应实验时刻{times} ns。')
                figures.append(dict(path=f'figures/{case}{suffix}/S4_complete_gates_physical.png',title=f'{case}：已完成的S4曝光',caption='相同空间标尺，10 ns完整曝光；上排实验灰度，下排物理辐亮度。无逐帧增益或空间缩放，S4铜面绝对位置尚未独立配准。'))
                break
    for case in cfg.get('split_material_cases',[]):
        path=R/'results'/case/'status.json'
        if not path.exists():continue
        s=json.loads(path.read_text());end=s['saved_time_ns']
        if not s.get('native_plasma_solved'):continue
        validity=R/'results'/case/'validity.json'
        if validity.exists():
            findings.append(f'{case}为修正前诊断：局部回凝电荷收支未闭合，不能用于实验吻合验收；保存场和图像保留以说明修正依据。')
            cases[case]='修正前诊断，局部电荷检查未通过；本次PDF不将其旧图作为新候选结果展示'
            continue
        phase=s.get('phase_feed','ground_neutral')
        charge='Cu0至Cu4+' if s.get('multicharge') else '仅单价铜对照'
        cases[case]=('热态中性铜供料' if phase=='thermal_neutral' else '基态中性铜供料')+f'；{charge}；材料有限体积与原生反应分步耦合，尚不含空间漂移、电子扩散和鞘层'
        findings.append(f'{case}真实保存至{end:.3f} ns，按总体积计的峰值电子数密度{ s["ne_max_bulk_m3"]:.3g} m^-3；累计吸收{ s["absorbed_J"]*1e6:.3g} μJ。电子峰值本身不代表亮区尺寸已对应。')
        err=R/'logs'/f'{case}.err'
        if err.exists() and 'Traceback' in err.read_text(encoding='utf8',errors='replace'):
            findings.append(f'{case}本段求解出现失败，以上仅为真实保存时间；失败后未保存的迭代不计入结果。')
        if s.get('trace_air_worker')=='R436_operator':
            findings.append('近零电子空气单元采用按实际脱附源确定的直接初始化首步；相同输入比较主要场量差异小于0.07%，未改变反应率或增加电子种子。')
        if case=='HC414':findings.append('HC414使用2 ns粗反应交换步，仅作为首帧诊断；不能代替强电离阶段的时间收敛结果。')
        gate=R/'results'/f'{case}native_gate_sequence.json'
        if gate.exists():
            q=json.loads(gate.read_text());times=[x['experiment_time_ns'] for x in q['rows']]
            findings.append(f'{case}已生成完整曝光{len(times)}/29帧：实验时刻{times} ns。曝光图覆盖以此为准，不以最新保存时间代替。')
        else:findings.append(f'{case}暂无已生成的完整10 ns曝光图；本次展示其实际保存空间场，不外推缺失曝光区间。')
        for page in sorted((R/'figures'/f'{case}native').glob('S4_complete_gates_page*.png')):
            figures.append(dict(path=str(page.relative_to(R)),title=f'{case}：S4完整曝光序列 {page.stem.rsplit("page",1)[-1]}',caption='每页最多四个时刻，沿用全序列共同色标与同一空间尺度。实验灰度和物理辐亮度分别标注；不逐帧归一化。'))
        audit=R/'results'/case/'hot_regime_audit.json'
        if audit.exists():
            rows=json.loads(audit.read_text())['rows']
            if rows:
                last=rows[-1];findings.append(f'{case}在{last["time_ns"]:.3f} ns的本征电子密度峰值为激光临界密度的{last["max_intrinsic_ne_over_critical"]:.3g}倍；Cu4+占气态铜{100*last["Cu4_fraction_of_all_gas_copper"]:.3g}%。据此判断光学近似与电离上界，不改变显示增益。')
        figures.extend([
            dict(path=f'figures/{case}/first_gate_baseline_comparison.png',title=f'{case}：首帧与原空间基线',caption='实验、原空间基线与多相分支使用相同空间尺度；两个仿真使用共同的物理辐亮度色标。完整曝光85–95 ns，未拟合相机增益。'),
            dict(path=f'figures/{case}native/S4_complete_gates_physical.png',title=f'{case}：多相供料发光对照',caption='仅展示已完成的曝光。色标为物理辐亮度；图像是否接近实验，应同时检查光子量、半径和离铜面位置。'),
            dict(path=f'figures/{case}/split_physical_fields.png',title=f'{case}：铜与等离子体空间场',caption='铜分布、电子密度、两种温度和相平衡更新后布居的可见发射率；青线为凝聚铜轮廓，虚线为初始铜面。相机图还包含视线输运与曝光积分。')])
        compare=R/'results'/f'{case}native_camera90/summary.json'
        if compare.exists():
            q=json.loads(compare.read_text());findings.append(f'{case}固定相机比较覆盖{q["complete_frames"]}/29帧；全局增益'+('已冻结。' if q['frozen_shared_gain'] else '仍为待审阅候选。'))
            figures.append(dict(path=f'figures/{case}native_camera90/metrics.png',title=f'{case}：同一相机增益的全过程指标',caption='灰色时窗为早期标定区，其后用于预测比较。实验铜面未独立配准，光质心绝对高度不可直接作为离面误差。'))
    figures.append(dict(path='figures/split_energy/supply_energy_comparison.png',title='供料、储能和发光位置的联系',caption='直接积分已保存状态；发射功率不是相机灰度，光质心为体发射率加权。比较中不调整反应率或相机增益。'))
    return dict(findings=findings,limitations='尚未完成的曝光不外推。未通过全过程、步长和网格验证的候选不标记为实验吻合。分步模型的反应MPH不是完整二维材料与电场统一求解的MPH。',next='继续优先推进能产生真实光子的候选；根据供料、吸收、反应储能和膨胀冷却的实际结果确定下一项修正。',cases=cases,figures=figures)

marked=False
remaining=sum(not (R/'output/pdf'/f'route1_progress_20261110_{h:02d}00.pdf').exists() for h in [18,23])
for hour in [18,23]:
    due=datetime(2026,11,10,hour,tzinfo=TZ);label=due.strftime('%Y%m%d_%H00')
    while datetime.now(TZ)<due:time.sleep(min(60,(due-datetime.now(TZ)).total_seconds()))
    output=R/'output/pdf'/f'route1_progress_{label}.pdf'
    if output.exists():continue
    # Capture only current facts; report content can be updated after scientific review.
    p=R/f'day_report_content_{label}.json';p.write_text(json.dumps(content(),ensure_ascii=False,indent=2),encoding='utf8')
    if not marked:
        subprocess.run([str(R.parent/'runtime/node22/node.exe'),str(MARK),'--operation-kind','create','--expected-output-count',str(remaining),'--output-format','pdf'],check=True);marked=True
    subprocess.run([sys.executable,'-X','utf8',str(R/'make_night_report.py'),label,str(p)],check=True)
    print('PDF_CREATED_VISUAL_REVIEW_PENDING',str(output),flush=True)
