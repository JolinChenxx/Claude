"""Generate the Chinese result/index from completed and checked artifacts."""
import sys,csv
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
from datetime import datetime,timezone

def link(label,path):return f'[{label}](/{Path(path).as_posix()})'
def read(name):return json.loads((RES/name).read_text(encoding='utf-8'))
def run():
    cases=['LTE','R2_CR1T_R','R2_COUPLED','R2_COUPLED_Q','R2_COUPLED_NT']
    names={'LTE':'原LTE','R2_CR1T_R':'单温有限速率（1 ns复核）','R2_COUPLED':'双温耦合、无淬灭','R2_COUPLED_Q':'双温耦合、几何淬灭上限','R2_COUPLED_NT':'双温、关闭局部俘获的损失上界'}
    metricsrows=list(csv.DictReader((RES/'R2_observable_comparison.csv').open()))
    def photons(case,delay,scope='expanded'):
        return float(next(x['total'] for x in metricsrows if x['case']==case and x['scope']==scope and float(x['shift_ns'])==90 and float(x['experiment_ns'])==delay))
    table='| 工况 | 150 ns | 260 ns | 740 ns |\n|---|---:|---:|---:|\n'
    for case in cases:table+='| '+names[case]+' | '+' | '.join(f'{photons(case,t):.4g}' for t in [150,260,740])+' |\n'
    budgets='| 耦合工况 | 最大能量收支残差／净输入 | 铜收支残差／最大净供给 | 实际终点 |\n|---|---:|---:|---:|\n'
    for case in ['R2_COUPLED','R2_COUPLED_Q','R2_COUPLED_NT']:
        a=read(case+'_budget_summary.json');budgets+=f"| {case} | {100*a['max_energy_residual_fraction']:.4g}% | {100*a['max_copper_residual_fraction']:.4g}% | {a['last_numerical_ns']:.0f} ns |\n"
    num=read('R2_1T_numerical_comparison.json');ltecheck=read('R2_LTE_adapter_check.json')
    nt_ratio=photons('R2_COUPLED_NT',740)/max(photons('R2_COUPLED',740),1e-300)
    now=datetime.now(timezone.utc);session=json.loads((ROOT/'session.json').read_text());elapsed=(now-datetime.fromisoformat(session['started_utc'])).total_seconds()/3600
    # Completion is asserted only after the actual endpoint and all renders exist.
    for case in cases[1:]:
        m=read(case+'_fields.json');s=read(case+'_radiation/summary.json')
        if m['times_s'][-1]<899.9e-9 or s['status']!='completed':raise RuntimeError(case+' incomplete')
    text=f'''# 第二路线首轮结果与交付索引

本轮在8小时预算内执行，实际约{elapsed:.2f}小时。全部新增模型与数据保存在独立route2目录；未覆盖原B64A或另一条路线。结果支持继续研究非平衡机制，但**没有完成实验亮度与形态的同时匹配**。

## 主要结论

1. **旧无限灰度增益掩盖了LTE的真实熄灭。** 90 ns对齐约定下，LTE在实验150 ns仅约7.76×10⁻⁷个整幅光子／10 ns。60/90/120 ns三个固定情景在150 ns都低于1个整幅理想光子。旧4.42×10⁶⁷增益只能作为历史形态诊断。
2. **有限速率能够恢复后期铜谱线光子，但寿命对俘获与淬灭极敏感。** 无淬灭双温给出约10⁴光子的长尾，仍比实验相对灰度衰减慢；几何淬灭上限把740 ns降到约1个整幅光子以下。两种结果都满足全局能量收支，不能凭“变亮”选择机制。
3. **电离储能总量够用，释放通道和速度才是问题。** 无淬灭初始电离能约2.29 nJ、高激发态约1.91 nJ，均大于所算窗口约0.123 nJ的局部逃逸辐射。无淬灭后期510.6 nm占铜谱线光子约99.97%，主要涉及先前保存的4p激发态；强淬灭后这些态被清空，剩余复合供给只产生很弱的光。复合连续谱在所用通带内很小，不能把非平衡增强统称为复合连续谱。
4. **半圆既有成像因素，也有物理分布因素。** 原LTE的大半圆主要被极端增益和饱和放大；有限速率中，半圆在局部发射分布和无吸收投影中已存在，自吸收进一步加宽，PSF作用较小。双温150 ns的90%宽度从无吸收约246 μm增至约311 μm。早期30 ns三套网格的铜质量90%投影宽度约56 μm，实验亮斑约383 μm，提示物质与能量的空间供给仍不充分。
5. **热颗粒不能排除，但没有预测出其形成。** 约2000 K时，产生2500个理想光子／曝光需要约0.84—1.66 ng铜，接近现有蒸发铜约1.87 ng。假设150 ns形成2800 K颗粒、分配约0.18 ng铜，可在早期达到无淬灭原子参考的约37%—68%。较小颗粒迅速冷却，100 nm半径颗粒持续更久。形成时刻、温度与粒径仍需独立证据。

## 同口径光子比较

下表为扩展画幅、含自吸收和固定PSF的每10 ns曝光理想收集光子期望；NA=0.4，主通带475—525 nm，透过率与量子效率为1。时刻是实验标注延时，固定采用90 ns展示约定。1光子不是实验相机阈值。

{table}

按整幅理想期望的诊断口径，LTE在数值约203 ns降到1光子／曝光以下，强淬灭约737 ns；90 ns展示约定对应实验约113 ns和647 ns。无淬灭双温在900 ns前未降到1光子。上述时刻不是实验相机测得的熄灭阈值。

{link('完整光子、宽度和相对衰减图',FIG/'R2_mechanism_comparison.png')}；{link('统一固定色标图像序列',FIG/'R2_mechanism_frames.png')}；{link('相同相机视场与三种时间对齐',FIG/'R2_observable_comparison.png')}。

关闭俘获后，740 ns光子量为无淬灭俘获模型的{nt_ratio:.4g}倍。该工况仅用于确定模型长尾对局部逃逸近似的依赖；其动力学采用最大辐射损失，而相机仍计算自吸收，不能作为自洽的非局域辐射预测。反应供给与损失见{link('反应源项分解',RES/'R2_COUPLED_NT_reaction_budget.csv')}。

## 形态和颗粒

实验靶面缺少独立定位，因此没有宣称测得绝对脱离高度。比较使用亮斑宽度、高度跨度和相对位移，并保留灰度分割阈值5/8/20的差异。论文主文2.4节给出的高能量熔融飞溅上升速度约344 m/s，提示当前仅有靶面蒸发的模型还缺少运动中的铜源；本轮没有用任意附加质量去拟合它。

{link('双温逐层物理场',FIG/'R2_COUPLED_shape_layers.png')}；{link('自吸收与PSF形态分解',FIG/'R2_COUPLED_shape_ablation.png')}；{link('已有三套网格对照',FIG/'R2_shape_mesh_comparison.png')}；{link('实验亮斑位移与论文飞溅运动尺度',FIG/'R2_motion_spatter_comparison.png')}。

颗粒图使用同一成像尺度，保存气相铜扣减与能量账，展示的是单独颗粒贡献；没有与未扣减的原子图相加。现有铜质量上限不包括尚未建模的熔融飞溅。515 nm外照在补充材料中用于调焦，未找到它与正式发光曝光重叠的证据，因此未加入散射发光。

{link('颗粒质量—温度要求与冷却',FIG/'R2_particle_budget.png')}；{link('颗粒空间条件包络',FIG/'R2_particle_spatial.png')}；{link('颗粒与原子贡献比较',FIG/'R2_particle_atomic_comparison.png')}。

## 验证与实际完成范围

{budgets}

核对包括曝光单位、逐谱线与连续谱光子能量积分、Cu粒子数与电荷关系、碰撞详细平衡、有效高密度平衡极限、上下布居吸收、LTE接口回退、整体与发光区能量闭合。电荷中性由Cu⁺与空气LTE电子代数施加，并非另解电子连续性。相同LTE布居下四条铜谱线恢复原发射/吸收关系；七态截断在120 ns局部谱线发射总量上的差异约0.2%。

整体能量残差约4—6.5 nJ，虽远小于约20 μJ净输入，却大于所估可见余辉能量，因此该收支检查只证明宏观耦合没有显著漏能，不能独立验证亚nJ辐射预算。光子量还依赖逐反应、逐谱线计算及局部闭合，仍需可靠速率与辐射传输约束。

原单温与1 ns最大内部步长、改进缩放复核的最大光子差为{100*num['max_relative_photon_change']:.3g}%，最大宽度差{100*num['max_relative_width_change']:.3g}%，最大质心差{num['max_centroid_change_um']:.3g} μm。这是综合数值稳健性检查，不能称为仅改变一个时间步参数的试验。曝光后处理另比较4 ns与8 ns时间采样；强淬灭早期差约2.1%，主要晚期差显著更小。

{link('数值复核图',FIG/'R2_1T_numerical_comparison.png')}；{link('数值复核数据',RES/'R2_1T_numerical_comparison.json')}；{link('无淬灭能量收支',FIG/'R2_COUPLED_budget.png')}；{link('强淬灭能量收支',FIG/'R2_COUPLED_Q_budget.png')}。

## 适用范围与下一步

- 非平衡空间计算从数值120 ns继承原LTE布居，包含输运与新蒸发供给，完成至900 ns；更早时段使用已有LTE结果。未重新解决初始击穿。原2.12 μs流场保留作背景。
- 物理电子热传导试算未完成，主结果没有该项；时间尺度诊断表明它可能影响光谱。双温已包含加热、膨胀、反应能耗、碰撞交换、对流与扩散，并与流场、靶材、蒸发耦合。
- 铜低温碰撞和复合数据有明确近似，空气非平衡化学、Cu分子反应、完整双电子复合及更高能级未覆盖。未对反应率加拟合倍率。0/1空气淬灭只是几何模型内的边界，不是测得的真实概率。
- 局部逃逸长度近似不是全空间辐射传输。无淬灭的侧向各向同性等效光子量约为局部逃逸估计的0.3—0.4倍，说明仍有定量差异；不能把侧向图乘4π当作严格全方向通量。
- 实验无绝对亮度标定；滤片仅描述为约500 nm。490—510 nm理想通带不包含本网络四条主要绿光谱线，预测会根本不同。需要真实透过率曲线来判定通道。{link('通带对照',FIG/'R2_passband_comparison.png')}，{link('后期谱线组成',FIG/'R2_late_spectral_fingerprint.png')}。

下一轮优先处理真实Cu—空气淬灭数据与辐射俘获，并引入有质量/能量依据的运动铜源；颗粒继续作为有约束的候选贡献。保持同一套早晚参数，不以灰度增益、时间平移或反应率倍率追拟合。

## 可复查文件

{link('模型与复现说明',ROOT/'模型与复现说明.md')}；{link('逐小时记录',ROOT/'阶段记录.md')}；{link('反应网络与近似',ROOT/'data/network.json')}；{link('下载来源及哈希',ROOT/'data/references/source_manifest.json')}。

CSV：{link('统一可观测量',RES/'R2_observable_comparison.csv')}、{link('分机制光子数',RES/'R2_photon_mechanisms.csv')}、{link('颗粒质量—温度条件',RES/'R2_particle_required_mass.csv')}。每个工况的`*_radiation`目录包含带单位的辐亮度/光子数组、轴、全域局部发射率积分和曝光分辨率检查；`*_gated.npz`包含三种固定时间对齐的曝光图。

MPH：{link('1 ns单温复核',ROOT/'models/R2_CR1T_R.mph')}、{link('双温120—160 ns',ROOT/'models/R2_COUPLED_FIN.mph')}、{link('双温160—168 ns',ROOT/'models/R2_COUPLED_EXT.mph')}、{link('双温168—900 ns',ROOT/'models/R2_COUPLED_LONG.mph')}、{link('强淬灭120—900 ns',ROOT/'models/R2_COUPLED_Q.mph')}、{link('关闭俘获120—900 ns',ROOT/'models/R2_COUPLED_NT.mph')}。

含`invalid_alias`、`INVALID`的文件无效；中止的热传导试算和旧`transition_width`成像仅供历史诊断，不是本报告最终结果。
'''
    (ROOT/'首轮结果与交付索引.md').write_text(text,encoding='utf-8')
    write_json(ROOT/'delivery.json',dict(status='completed',completed_utc=now.isoformat(),elapsed_hours=elapsed,cases=cases,
        report=str(ROOT/'首轮结果与交付索引.md'),absolute_experiment_validation=False,fit_success_claimed=False,
        limitations=['No physical electron heat conduction','Inherited LTE before 120 ns','Approximate low-T rates and local trapping','Particle formation not predicted','Unknown measured filter curve and camera calibration']))
    session.update(status='completed',completed_utc=now.isoformat(),elapsed_hours=elapsed);write_json(ROOT/'session.json',session)
    print(ROOT/'首轮结果与交付索引.md')

if __name__=='__main__':run()
