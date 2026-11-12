# 原LTE高能铜羽流 / S4结果

主例：**B64A**。数值主解：**通过**。网格/时间步10%目标：**未全部通过**。

- [中文最终报告](docs/最终报告.md)：逐项实验支持、未吻合特征、守恒和局限。
- [真实主解MPH](work/B64A.mph)；[含结果视图的MPH](work/B64A_view.mph)。
- [S4同尺度对照图](figures/B64A_S4_comparison.png)；[演化曲线](figures/B64A_evolution_metrics.png)；[动画](figures/B64A_S4.gif)。
- [29帧实验/仿真指标CSV](results/B64A_camera/metrics.csv)；[主例数值审计](results/B64A_audit.json)；[预算与真实时刻记录](results/run_ledger.csv)。
- [复现说明](docs/环境与复现64.md)；[本轮执行记录](docs/COMSOL64执行记录.md)。

H00和未通过的H系列结果均为诊断。早期0–80ns仅供一次标定，不能算独立实验验证；150–740ns为留出比较。不用逐帧增益造吻合，不自动转入非平衡升级。
