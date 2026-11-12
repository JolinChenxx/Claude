"""Assemble a Chinese evidence report from actual saved audits and observations."""
from pathlib import Path
import csv,datetime,hashlib,json,re
import numpy as np
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'results';TZ=datetime.timezone(datetime.timedelta(hours=8))
def load(p,default=None):return json.loads(p.read_text(encoding='utf-8-sig')) if p.exists() else default
def num(x,d=4):return '未取得' if x is None else f'{x:.{d}g}'
def percent(x):return '不可评估' if x is None or not np.isfinite(x) else num(100*x)+'%'
def main():
 if (ROOT/'main_post_refresh_required.json').exists():raise RuntimeError('Main camera correction is pending; previous camera metrics are diagnostic only.')
 s=load(ROOT/'delivery_state.json',{});case=s.get('main_case')
 if not case:raise RuntimeError('No reviewed main-case identity; do not relabel an arbitrary diagnostic run.')
 a=load(RES/f'{case}_audit.json');camera=load(RES/f'{case}_camera/summary.json');vis=load(RES/f'{case}_visible_summary.json');nc=load(RES/'numerical_comparison.json',{})
 budget=load(ROOT/'solver_budget.json');used=sum(r['elapsed_seconds'] for r in budget['runs'])
 if nc and nc.get('main')!=case:raise RuntimeError('Numerical comparison belongs to a different main case')
 if not load(RES/f'{case}_postprocess.json'):raise RuntimeError('Main postprocessing is incomplete')
 variants=[c for c in [s.get('grid_case'),s.get('time_case')] if c]
 complete=a['status']=='passed';conv=bool(len(variants)==2 and all(load(RES/f'{c}_audit.json',{}).get('status')=='passed' and nc.get('variants',{}).get(c,{}).get('all_camera_and_physical_within_10pct',False) for c in variants))
 window_conv=bool(len(variants)==2 and all(load(RES/f'{c}_audit.json',{}).get('metadata',{}).get('last_time_s',0)>=865e-9 and nc.get('variants',{}).get(c,{}).get('all_camera_and_physical_within_10pct',False) for c in variants))
 lines=[f'# COMSOL 6.4 铜羽流LTE仿真与S4验证：{case}', '',f'报告生成时间：{datetime.datetime.now(TZ).isoformat(timespec="seconds")}。', '',
        ('完整数值主解已得到。' if complete else '本轮主解尚未通过完整时域与数值门槛。')+('S4观测门宽内，网格及时间步的相机指标和饱和前物理亮度均达到10%目标。' if conv else '网格/时间步10%目标未全部通过；不能把该轮结果表述为全项数值验证完成。'),'',
        '固定输入为1064nm、60ns FWHM、单脉冲入射400μJ、1/e²半径11μm、空气101325Pa与293.15K、表面吸收率0.1。按E/(πw²)计算105.23J/cm²，高斯轴心积分通量为其2倍。论文表格写“半径22μm”，与此通量数值有尺寸歧义；本轮明确采用11μm，不另算22μm。', '',
        f'数值脉冲峰值120ns。真实主解覆盖0–{num(a["metadata"]["last_time_s"]*1e6)}μs，保存{a["metadata"]["saved_times"]}个唯一时刻。保留原LTE状态表、气体守恒方程、铜靶焓法，采用净蒸发/凝结及守恒ALE。初始自由蒸发固定界面结果H00仅作诊断。', '',
        '本次基准、网格及时间步验证三个工况，以及追加一级网格，均由COMSOL 6.4.0.429从初态求解。旧H15/H15R64流体解来自6.2，仅用于迁移核验和历史参考。交接包789项负载文件的SHA256已核验；原压缩包、原模型与代码保留。6.4导入LTE插值表时已修复模型资源路径，135个LTE探针值不变，旧H15关键积分量重新导出与原值一致；从初态派生前核对279项物理表达式、初值与选区。证据见transfer_verified_64.json、migration_relocation.json和results/physics_identity64.json。', '',
        '## 数值、界面与能量审计', '',
        '| 检查 | 真实结果 |', '|---|---|',
        f'| 铜质量残差 | {num(100*a["mass_relative_residual"])}% |',f'| 总能量残差 | {num(100*a["energy_relative_residual"])}% |',
        f'| 吸收超过1%后的最大逐时能量残差 | {num(100*a["max_time_resolved_energy_relative_residual_after_1pct_absorption"])}% |',
        f'| 入射 / 反射逃逸 / 吸收 | {num(a["incident_uJ"])} / {num(a["reflected_uJ"])} / {num(a["absorbed_uJ"])} μJ |',
        f'| 羽流双程吸收 / 靶面吸收 | {num(a.get("plume_absorbed_uJ"))} / {num(a.get("target_absorbed_uJ"))} μJ |',
        f'| 光学收支残差 | {num(100*a["optical_relative_residual"])}% |',
        f'| 原始Y范围 | {num(a["raw_Y_min"])} 至 {num(a["raw_Y_max"])} |',f'| 最低密度 / 最低热比内能 | {num(a["min_density"])} kg/m³ / {num(a["min_raw_thermal_specific_energy"])} J/kg |',
        f'| 最大退缩 / 最小ALE雅可比 | {num(a["max_recession_um"])} μm / {num(a.get("ALE_min_J"))} |',
        f'| ALE体积守恒残差 | {num(100*a.get("ALE_volume_relative_residual",float("nan")))}% |',
        f'| 总气体质量残差 | {num(100*a.get("total_mass_relative_residual",float("nan")))}% |',
        f'| 积分回流 / 正向蒸发 | {num(a["integrated_return_to_evap_ratio"])} |',
        f'| 外边界最大相对压强扰动 | {num(a["outer_pressure_disturbance"])} |','',
        f'逐项门槛见`results/{case}_audit.json`。未通过项：'+('无。' if complete else '、'.join(k for k,v in a['checks'].items() if not v)+'。'), '',
        '光学账目分别积分入射、双程羽流吸收、靶面吸收与向外逃逸反射光；40μJ只是完全不屏蔽时的靶面参考，未把400μJ当作吸收能量。原始变量检查不以绘图截断代替；Y仅允许既有1e−4误差。回流/退缩触发条件已导致净通量和ALE实算，不使用固定面H00支持实验。', '']
 if vis:
  lines += [f'380–780nm全FE气体域（含真实ALE雅可比）的谱线+free-free未衰减发射能量上界为{num((vis["line_emitted_energy_J"]+vis["free_free_emitted_energy_J"])*1e6)}μJ，占吸收能量{num(100*vis["visible_upper_bound_to_absorbed_ratio"])}%。'+('低于5%，在所采用辐射闭合内无需触发新增净辐射反馈重算。' if vis['upper_bound_below_feedback_threshold'] else '该上界未排除5%反馈门槛，需进一步求净逃逸并处理能量反馈；当前结果不能据此通过。'),'',
            '此上界包含有NIST跃迁概率、上能级低于电离阈值的Cu I/II、N I/II、O I/II及free-free；自由束缚、分子带、更高离子级、UV/IR谱线的贡献不在此闭合中。它不等同于测得的绝对辐射能量，也不为未建模辐射项提供上界。','']
 lines += ['## S4实验重建与相机对应', '',
           '29帧全部使用。400μm标尺对应36像素，即11.111μm/像素；标尺端点±1像素相当于约±2.8%长度尺度范围。S2伪彩色条用于反演出版JPEG，RGB最近色距离95分位约11.29；这不是原始CCD灰度或独立重复实验。轮廓主阈值27，另保留100/220；同样的3×3开闭及最大连通域用于实验/仿真。','',
           '实验最大面积约0.303mm²，S7散点约0.265mm²，均与补充文字0.0296mm²冲突；没有把这些冲突数值一起拟合。出版S4缺乏独立靶面标记，因此上下边界分别保留各自原点，绝对高度不作已注册的独立验证。半径、面积、亮区高度跨度及时间消退可直接比较。','',
           '合成图由LTE布居、NIST谱线与free-free发射/吸收、轴对称视线输运得到，优先铜线510.5541、515.3235、521.8202nm。主通带475–525nm；490–510、450–550nm及PSF 0/1/2像素共用主例增益和时移。10ns门宽对真实保存时间节点的分段线性辐亮度积分。自然+Doppler近似Voigt线型，含径向视线速度；未拟合Stark展宽。','']
 if camera:
  fit=camera['calibration'];pr=camera['peak_timing'];ex=camera['extinction_at_observed_frames']['27']
  rows=list(csv.DictReader((RES/f'{case}_camera/metrics.csv').open()));rr=[r for r in rows if r['band']=='475_525' and r['PSF_FWHM_pixels']=='1' and r['threshold']=='27' and r['split']=='holdout' and int(r['experiment_above_threshold_pixels'])>0]
  hpass=sum(r.get('height_within_target')=='True' for r in rr)
  lines += [f'唯一全局增益{num(fit["gain"],7)}，固定背景{num(fit["background"])}，固定时移{num(fit["time_shift_ns"])}ns（实验时间=数值时间−时移），统一线性响应后饱和至0–255。仅用0–80ns的7帧标定，排序像素灰度RMSE为{num(camera["calibration_sorted_pixel_RMSE_counts"])} counts；这些早期帧不能作为独立验证。150–740ns的22帧完全留出。', '',
            '| 特征 | S4实验 | 冻结响应的LTE | 判断 |', '|---|---|---|---|']
  for key,label in [('radius_um','最大半径（μm）'),('area_mm2','最大面积（mm²）'),('mean_gray','轮廓内平均灰度峰值'),('integrated_gray','全图积分灰度峰值')]:
   q=pr[key];ev=q['experiment_peak_value'];sv=q['simulated_peak_value']
   passed=abs(sv-ev)<=max(.2*ev,2*400/36) if key=='radius_um' else abs(sv-ev)<=.2*max(ev,1e-30)
   lines += [f'| {label} | {num(ev)}，{q["experiment_peak_time_ns"]}ns | {num(sv)}，{q["simulated_peak_time_ns"]}ns | '+('幅值在所列目标内' if passed else '幅值未达目标')+'；'+('峰值时刻相容' if q['time_peak_within_neighbor_interval'] else '峰值时刻不相容')+' |']
  lines += [f'| 留出实验可见帧的半径 | {camera["holdout_visible_experiment_frames"]}帧 | {camera["holdout_visible_radius_pass_count"]}帧通过 | 目标为20%或2像素中较宽者 |',
            f'| 留出实验可见帧的亮区高度跨度 | {len(rr)}帧 | {hpass}帧通过 | 不依赖绝对靶面原点 |',
            f'| 最后可见S4采样时刻（阈值27） | {ex["experiment_last_visible_ns"]}ns | {ex["simulation_last_visible_ns"]}ns | 仅在29个已观测时刻判定 |','',
            '面积和灰度表中采用20%相对幅值作为辅助比较；2像素空间容差只用于长度量。全图积分灰度含统一背景，CSV另提供扣除相同背景的净积分。峰值时刻按实验峰值相邻帧的较大间隔评价。阈值100/220的末次可见时刻、全部空间/亮度指标及通带/PSF敏感性均在指标CSV与summary.json中。','',
            '逐项解释：激光加热、净铜蒸发和铜物质向空气扩展由真实守恒主解支持；它们本身不证明S4发光轮廓已得到复现。可见半径/面积、峰值和消退是否得到支持，以本表留出结果为准。本轮冻结响应图像呈现饱和并持续扩张的亮区，与实验后期衰减不同；光子量级核验又表明这些晚期轮廓缺乏足够的已建模LTE发射支撑。因此，本轮未解释实验后期发光，不增加逐帧增益、额外热源或任意时间变换，也不自动切换非平衡模型。','']
  lines += ['本轮尚未包含颗粒核化、颗粒输运或颗粒热发光，不能把铜蒸气分布直接当作纳米颗粒或自沉积的验证。LTE激发布居随气体温度快速下降，可见发光会比铜物质分布更快消退；这解释了两类图为何不能互相替代。当前比较限定于固定吸收率、指定LTE原子谱线/free-free闭合及数值设置，不能据此排除所有LTE模型，也不能把偏差唯一归因于非平衡效应。','']
  photons=load(RES/f'{case}_camera/photon_scale.json')
  if photons:
   pf=[r for r in photons['frames'] if r['bright_mask_pixels']>0];pixels=sum(r['bright_mask_pixels'] for r in pf)
   fraction=sum(r['bright_mask_pixels']*r['bright_mask_fraction_below_one_ideal_photon'] for r in pf)/max(pixels,1)
   lines += [f'增益标定采用饱和响应各分段二次式的全局最小值，不再设任意增益上限。理想光子量级检查使用论文NA=0.4、透过率/QE均为1、10ns门宽及525nm保守换算；全部模拟可见掩模像素中约{num(100*fraction)}%低于1个理想收集光子/像素。详见photon_scale.json。若明亮轮廓主要来自远低于1光子的极弱LTE尾部，数学灰度拟合不能作为物理发光获得支持的证据；线性均值图没有模拟单光子统计，任意大增益不能补回缺失的入射光子。','']
  cap=load(RES/f'{case}_photon_upper_bound.json')
  if cap:
   at260=next(r for r in cap['frames'] if r['experimental_time_ns']==260)
   at400=next(r for r in cap['frames'] if r['experimental_time_ns']==400)
   lines += [f'进一步用全FE域380–780nm谱线+free-free发射积分给出不依赖相机增益、NA、视场和视线截断的光子上界：对所有60–120ns允许时移取最大值，并按781nm保守换算，实验260ns时10ns门宽内全域、全方向平均发射光子最多{num(at260["max_emitted_visible_photons_over_allowed_shifts"])}个；400ns时最多{num(at400["max_emitted_visible_photons_over_allowed_shifts"])}个。相机收集与吸收只会进一步降低此数量。它仅约束已建模的原子谱线/free-free闭合，不为遗漏发光机理提供上界；在这一闭合内，极大增益的平滑灰度轮廓不能解释S4后期明亮图像。详见photon_upper_bound.json/CSV。','']
 else:lines += ['相机后处理尚未取得完整输出，不能报告S4定量吻合。','']
 lines += ['## 网格和时间步验证', '',
           'S4观测门宽的三项相机指标与饱和前物理亮度综合10%判定：'+('通过。' if window_conv else ('未通过。' if len(variants)==2 and all(c in nc.get('variants',{}) for c in variants) else '缺少完整对照。'))+'该判断与验证解是否完整覆盖2.12μs分别记录；任何部分解仍标为diagnostic。','',
           '最终物理设置共用。网格×0.75和最大时间步减半均从293.15K初态独立求解；重启只拼接各自真实历史。验证例冻结主例相机参数。比较半径、面积、扣背景积分亮度；相对误差在主例大于其峰值1%的帧上报告；综合通过还要求饱和前物理积分辐亮度变化≤10%，无活动帧为不可评估。低信号区新增发光单独检查。此判定限于S4观测门宽，不代表全时空场收敛证明。','',
           '| 工况 | 数值时域/审计 | 半径最大变化 | 面积最大变化 | 净积分亮度最大变化 | 局部辐亮度峰值变化 |', '|---|---|---|---|---|---|']
 variant_notes=[]
 for c in variants:
  va=load(RES/f'{c}_audit.json',{});n=nc.get('variants',{}).get(c,{})
  vals=[num(100*n.get('metrics',{}).get(k,{}).get('max_relative_change_above_1pct_peak'))+'%' if n.get('metrics',{}).get(k,{}).get('max_relative_change_above_1pct_peak') is not None else '未完成' for k in ['radius_um','area_mm2','integrated_gray']]
  lines += [f'| {c} | {num(va.get("last_time_ns"))}ns / {va.get("status","未完成")} | '+' | '.join(vals)+' | '+percent(n.get('local_peak_radiance_relative_change'))+' |']
  variant_notes += ['',f'{c}局部峰值：T={num(va.get("peak_T_K"))}K，ne={num(va.get("peak_ne_m3"))}m⁻³，速度={num(va.get("peak_speed_m_s"))}m/s。原始极值与主例并列存于各自audit.json；局部峰值不代替积分量收敛。','']
  physical=n.get('unsaturated_physical_brightness',{})
  if physical:variant_notes += [f'{c}饱和前主通带物理积分辐亮度最大相对变化：{percent(physical.get("max_relative_change_above_1pct_peak"))}；判定状态：{physical.get("status","未完成")}；活动帧数：{physical.get("active_frames",0)}。只在主例超过其峰值1%的观测帧上评价。它独立于统一增益和相机饱和，用于披露饱和响应可能隐藏的误差。','']
 lines += variant_notes
 sampling=load(RES/'post_sampling_check64.json')
 if sampling:
  lines += ['', '为核查后处理采样误差，在五个活动物理亮度门宽（实验0、20、30、50、60ns）上，将场导出网格和视线积分间距均减半，读取相同已保存流体解；没有重拟合相机。', '']
  for identity,ss in sampling['cases'].items():lines += [f'- {identity}：更密后处理相对原采样的最大物理亮度变化{percent(ss["max_relative_change"])}。']
  lines += ['',f'更密后处理下，原网格与×0.75网格之间的最大差异为{percent(sampling["max_denser_post_flow_grid_relative_difference"])}。'+('仍超过10%，后处理采样加密未消除该流体网格敏感性。' if sampling['flow_mesh_difference_still_exceeds_10pct'] else '该采样检查影响了10%判定，不能单独归因于流体网格，须结合完整表格谨慎解释。')+'本检查只覆盖五个早期活动物理亮度门宽，不声称验证后期极弱尾部的采样独立性。详见results/post_sampling_check64.json。','']
 extra=s.get('extra_grid_case')
 adjacent=load(RES/'extra_grid_adjacent64.json',{})
 fine_baseline=load(RES/'extra_grid_vs_baseline64.json',{})
 if extra:
  ea=load(RES/f'{extra}_audit.json',{})
  lines += ['', '### 追加一级网格', '',f'由于原网格与×0.75网格未达10%目标，按计划追加×0.5625网格（25009单元），最大步长仍为0.5ns，从初态独立计算。最终保存工况{extra}，时域0–{num(ea.get("last_time_ns"))}ns，数值审计状态{ea.get("status","未完成")}。全部相机指标继续冻结B64A的原始标定，不重新拟合。', '', '| 比较 | 半径 | 面积 | 净积分灰度 | 饱和前物理亮度 | 综合10%判定 |', '|---|---|---|---|---|---|']
  for title,comparison in [('原网格→追加网格',fine_baseline),('×0.75→×0.5625',adjacent)]:
   cc=comparison.get('variants',{}).get(extra,{})
   vv=[percent(cc.get('metrics',{}).get(k,{}).get('max_relative_change_above_1pct_peak')) for k in ['radius_um','area_mm2','integrated_gray']]
   vv.append(percent(cc.get('unsaturated_physical_brightness',{}).get('max_relative_change_above_1pct_peak')))
   lines += ['| '+title+' | '+' | '.join(vv)+' | '+('通过' if cc.get('all_camera_and_physical_within_10pct') else ('未通过' if cc else '未完成'))+' |']
  lines += ['', '追加网格的相邻比较用于观察加密趋势，不追溯改变原基准网格验证的失败结论。本轮相邻比较仍未通过，只能交付已计算的敏感性证据，不能宣称已达到网格独立性。原网格至追加网格的物理亮度差异仍超过10%；相邻物理亮度低于10%仅说明这一项达到相邻目标，不能抵消其他指标失败。', '']
 flow=load(RES/'physical_numerical_comparison64.json',{})
 if flow.get('main')==case and all(c in flow.get('variants',{}) for c in variants):
  lines += ['', '补充物理量比较使用真实公共保存节点，不进行时间插值。下表为整个公共时域内最大绝对差值除以基准曲线峰值；它与逐时相对误差不同，不作为全场收敛证明。', '', '| 工况 | 公共时域 | 铜气体总量 | 气体能量 | 峰温曲线 | 峰电子密度曲线 | 峰速度曲线 |', '|---|---|---|---|---|---|---|']
  for c in variants+([extra] if extra and extra in flow.get('variants',{}) else []):
   ff=flow['variants'][c];mm=ff['metrics'];vv=[percent(mm[k]['max_absolute_difference_normalized_by_reference_peak']) for k in ['copper_gas_mass','gas_excess_energy','peak_temperature','peak_electron_density','peak_speed']]
   lines += [f'| {c} | 0–{num(ff["common_time_range_ns"][1])}ns | '+' | '.join(vv)+' |']
  lines += ['', '追加网格的峰速度曲线差异见上表，局部流场仍有显著网格敏感性；峰速度曲线的归一化最大差值不同于各工况全时域单个最高速度的差值。完整曲线、逐时值与局部极值差异见figures/physical_numerical_comparison64.png和results/physical_numerical_comparison64.json/CSV。局部速度或温度的差异可能大于积分质量/能量差异，不能用守恒残差代替离散敏感性。', '']
 speed_review=load(RES/'fine_speed_sensitivity_review64.json')
 if speed_review:
  lines += ['',f'追加网格峰速度差异的最大时刻为{num(speed_review["time_ns"])}ns：基准{num(speed_review["baseline_speed_m_s"])}m/s，追加网格{num(speed_review["fine_speed_m_s"])}m/s。该数值已存在于早期保存检查点G264A，最终历史合并未改变它；这是未解决的局部网格敏感性，未据此宣称全场收敛。该时刻全域最低密度仍为正，但未验证其空间位置与速度峰值重合，不能据此唯一归因。详见results/fine_speed_sensitivity_review64.json。','']
 allcases=[case]+variants+([extra] if extra else [])
 lines += ['', '### 各工况完整时域审计汇总', '', '| 工况 | 保存时域 / 唯一时刻 | 铜质量残差 | 能量残差 | 光学残差 | 全部审计 |', '|---|---|---|---|---|---|']
 for c in allcases:
  aa=load(RES/f'{c}_audit.json',{});meta=aa.get('metadata',{})
  vals=[percent(aa.get(k)) for k in ['mass_relative_residual','energy_relative_residual','optical_relative_residual']]
  lines += [f'| {c} | 0–{num(aa.get("last_time_ns"))}ns / {meta.get("saved_times","未取得")} | '+' | '.join(vals)+' | '+('通过' if aa.get('status')=='passed' and all(aa.get('checks',{}).values()) else '未通过或未完成')+' |']
 lines += ['', '完整时域审计通过表示满足既定守恒、正性、EOS、ALE及外边界门槛；不代表S4观测量已网格独立，也不代表实验发光机制已被验证。', '']
 if len(variants)<2:lines += ['', '仍缺少一项或两项完整独立数值对照。','']
 session=load(ROOT/'session_64.json',{})
 if session.get('started_utc'):
  elapsed=(datetime.datetime.now(datetime.timezone.utc)-datetime.datetime.fromisoformat(session['started_utc'])).total_seconds()
  lines += ['',f'本轮开始：{session.get("started_beijing")}；截至报告生成墙钟约{elapsed/3600:.2f}小时。机器为64个物理核心、128逻辑线程、约256GB内存，主求解配置{session.get("solver_threads")}核，常规后处理{session.get("post_threads")}线程；并行诊断使用8线程。核数选择依据实测短算，且机器上存在独立项目的共享负载。', '']
 lines += ['最终交付核验见results/delivery_verification64.json：四工况时间数组、二进制场尺寸、冻结相机参数、MPH及视图、32张PDF自动审查均通过。追加网格早期437个时刻的全部11个场变量与最终完整解逐点一致。判定与状态写入共10项测试通过，日志见logs/final_validation_tests64.txt；覆盖相同非零解、全零解、饱和隐藏真实亮度差、新增信号、NaN/Inf、门宽不足及短暂/持续文件锁。', '', '## 调试、预算及交付边界', '',f'跨平台累计COMSOL求解记账{used/3600:.4f}小时，其中本平台新增{(used-budget.get("historical_solver_seconds",0))/3600:.4f}小时；当前累计求解上限{budget["budget_seconds"]/3600:.4f}小时，本次另受墙钟截止时间约束。该数值包括失败和官方停止探索；保守计入worker轮询回收开销，不包含构建、只读导出及后处理。每次启动、日志保存完成时刻、时域与原因见`results/run_ledger.csv`；原始日志保留。', '',
           '交接模型已包含的历史修订：自由蒸发改净通量；超过1.1μm后改守恒ALE；纠正真实底边编号2（旧误选1为轴线）；保持全域nuFactor=2，低铜梯度前沿使用局部守恒稳定项。上述物理设置在本次迁移中保持一致。H00及未通过的H系列检查点均保留为诊断，不能替代最终主例。','',
           '本平台新增工作包括6.4路径与导入迁移、独立Python环境、全新初态验证工况、45分钟官方保存与真实历史合并核验、进程所有权和异常锁修复，以及相机输出隔离、固定响应和饱和前亮度判定。8/16/32核同一短算日志用时95/90/97秒，据此选16核。早期求解采用原Jacobian复用/固定阻尼，约300ns或出现已诊断的早期停滞时官方保存，再使用当前Jacobian/自动阻尼；未改变物理方程、离散方式或容差。追加网格在240ns请求策略切换，实际保存268ns；后续Windows状态文件替换曾短暂被拒绝，异常保护官方保存至418ns，完整非终点审计通过后原设置恢复。该异常耗时计入账本，并增加原子替换有界重试和显式UTF-8读取，未丢弃日志或假造结果。详细记录见docs/COMSOL64执行记录.md及results/G264A_recovery_review64.json。','',
           '未包括非平衡电离/激发、自由束缚连续谱、分子发光、Stark展宽、非轴对称喷溅和三维形貌；未知相机滤光片由所列矩形通带近似。LTE均匀平板的光薄/厚极限与谱线积分已测试，有限配分函数截断影响另存data/partition_cutoff_audit.json。所有这些限定随结果交付，不以视觉拟合掩盖。','',
           '复现入口为`work/reproduce.py`，完整命令见`docs/环境与复现64.md`。主结果MPH、视图MPH、图像、动画、逐帧指标、光学/质量/能量CSV及JSON审计均由真实保存解生成。当前结论是否为数值全项通过、是否支持实验，是两项独立判断。','',
           '来源：[Chen等，Mechanism of Laser-Induced Self-Deposition of Nanoparticles Identified by In Situ Observation](https://doi.org/10.1002/lpor.202400388)及其补充图S2/S4/S7；[NIST Atomic Spectra Database](https://physics.nist.gov/asd)。']
 (ROOT/'docs/最终报告.md').write_text('\n'.join(lines)+'\n',encoding='utf8')
 ledger=[]
 for r in budget['runs']:
  log=ROOT/'logs'/f'{r["case"]}.log';text=log.read_text(encoding='utf8',errors='replace') if log.exists() else ''
  ar=load(RES/f'{r["case"]}_audit.json',{});date=next((x for x in text.splitlines() if re.match(r'^(Mon|Tue|Wed|Thu|Fri|Sat|Sun) ',x)),None)
  ledger.append(dict(case=r['case'],started_log=date,saved_log_last_write_beijing=datetime.datetime.fromtimestamp(log.stat().st_mtime,TZ).isoformat(timespec='seconds') if log.exists() else None,elapsed_solver_budget_s=r['elapsed_seconds'],official_stop_requested=r.get('official_stop_requested',False) or ('瞬态求解器中断' in text),last_saved_numerical_ns=ar.get('last_time_ns'),audit_status=ar.get('status','no_valid_audit'),reason=r.get('stop_reason')))
 with (RES/'run_ledger.csv').open('w',newline='',encoding='utf-8-sig') as f:w=csv.DictWriter(f,fieldnames=ledger[0]);w.writeheader();w.writerows(ledger)
 result={'main':case,'main_audit_passed':complete,'mesh_time_all_within_10pct':conv,'S4_window_metrics_all_within_10pct':window_conv,'solver_budget_seconds':used,'driver_phase':s.get('phase'),'report':'docs/最终报告.md','variants':variants,'extra_grid_case':extra,'extra_grid_adjacent_converged':adjacent.get('variants',{}).get(extra,{}).get('all_camera_and_physical_within_10pct') if extra else None,'early_calibration_not_independent_validation':True}
 result['complete_time_audits']={c:load(RES/f'{c}_audit.json',{}).get('status') for c in allcases}
 result['original_grid_converged']=nc.get('variants',{}).get(s.get('grid_case'),{}).get('all_camera_and_physical_within_10pct')
 result['time_step_converged']=nc.get('variants',{}).get(s.get('time_case'),{}).get('all_camera_and_physical_within_10pct')
 result['late_experimental_emission_explained']=False
 result['late_emission_scope']='Specified LTE atomic-line and free-free closure; arbitrary camera gain does not establish photon support.'
 (RES/'delivery_summary.json').write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
 readme=f'''# 原LTE高能铜羽流 / S4结果

主例：**{case}**。数值主解：**{'通过' if complete else '尚未通过'}**。网格/时间步10%目标：**{'通过' if conv else '未全部通过'}**。

- [中文最终报告](docs/最终报告.md)：逐项实验支持、未吻合特征、守恒和局限。
- [真实主解MPH](work/{case}.mph)；[含结果视图的MPH](work/{case}_view.mph)。
- [S4同尺度对照图](figures/{case}_S4_comparison.png)；[演化曲线](figures/{case}_evolution_metrics.png)；[动画](figures/{case}_S4.gif)。
- [29帧实验/仿真指标CSV](results/{case}_camera/metrics.csv)；[主例数值审计](results/{case}_audit.json)；[预算与真实时刻记录](results/run_ledger.csv)。
- [复现说明](docs/环境与复现64.md)；[本轮执行记录](docs/COMSOL64执行记录.md)。

H00和未通过的H系列结果均为诊断。早期0–80ns仅供一次标定，不能算独立实验验证；150–740ns为留出比较。不用逐帧增益造吻合，不自动转入非平衡升级。
'''
 (ROOT/'README.md').write_text(readme,encoding='utf8');print(json.dumps(result,ensure_ascii=False,indent=2))
if __name__=='__main__':main()
