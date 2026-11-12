# S4高能铜羽流执行交接

## 最新执行入口（优先于下方旧快照）

13:33更新：当前driver PID17336（`finish_campaign.py H15`），日志`finish_H15_v2_stdout.txt`/`finish_H15_v2_stderr.txt`；H15 solver PID22248/worker33560已至约2.07us，目标2.12us。旧driver35928仅作为协调器被替换，COMSOL求解器未中断。调度先完整主算及串行后处理，再VG/VT各自先覆盖全部S4门宽（保守865ns，仍保持部分解标签），用剩余预算优先延长VT、再VG；后处理与重求解串行，避免CPU竞争。不要重新启旧driver。

**相机关键修正**：旧H00的loggain±18标定触及人为上限，不能沿用其gain/offset结论。`camera_s4.py`已改为g≥0饱和分段二次目标的全局最小值，通过解析例子/独立稠密扫描；仍只拟合0–80ns的一gain一shift。新增`photon_scale.json`，用论文NA=.4及理想透过/QE估计增益前光子量级；若数学gain照亮远低于1光子的LTE尾部，不能据表观灰度吻合宣称物理支持。最终报告已包含此解释。新主例须重新标定，验证例冻结其响应。

`ExportS4`已切refine1并新增P1热比内能的严格顶点总比能上界；H13同解对照证明rho/Y/热内能极值完全不变、T/速度全局峰值不变。最大值采样不冒充FE网格验证。`run_standalone.py`日志现按类名+源工况名区分。`CreateViews`已实际run五个结果视图并保存测试通过。最终`deliver_report.py`必须在真实图像与JSON审查后运行；`work/requirements.txt`与复现入口已保存。

13:14更新：H14完整0–1.33us审计完成，除目标时域外全部通过，能量−.02775%、铜质量−.00948%。已准备并启动`finish_campaign.py H15`，H15从1.33us真实历史续算，保留自动阻尼，后半段minimal Jacobian，8核；完成后仍自动串行VG/VT。旧driver PID9940已正常进入人工复核后退出（这是计划内切换策略的官方停止，不是物理解失败）。后续查看`logs/finish_H15_stdout.txt`和`finish_H15_stderr.txt`。`work/deliver_report.py`已写好，待全部真实结果与图形人工审查后运行生成中文最终报告/README，不能在只有中途状态时把它当最终交付。

2026-11-05 12:55：H14正在从H13的484 ns真实解续算，自动Newton阻尼、每迭代更新Jacobian，至约0.9 us无拒步。后台`work/finish_campaign.py`（启动PID 9940）负责在主算审计通过后串行VG（网格×.75）/VT（最大步.25 ns），必要时只对45min预算停止且其余全项通过的解做原设置真实续算。先核实该driver PID及`delivery_state.json`，不要重复启动队列。一次只有一个COMSOL重求解，后处理可与其并行；后续求解使用8核，本来运行的H14仍4核。累计4h和每段45min仍强制执行。

H13已官方停止、真实合并0–484ns并审计，除时域未完成外全通过；fixed .8/minimal未降低成本，已撤回。修复了第二次移除merged解时dset1被级联删除的问题：ResumeS4/MergeHistory确保重建结果数据集，解本身未丢失。H14保留完整真实历史，不是从零再算。

新增`ExportVisible.java`/`visible_budget.py`与380–780nm NIST来源，整个FE域积分可见谱线+free-free发射上界。H11的0–428ns上界/吸收约3.6564e−6，远低于.05；最终主例仍须完整时域导出。新诊断变量加入保存解后必须`updateSolution()`刷新表达式上下文，不进行流体重算或覆盖源MPH。

driver在真实数值失败或后处理失败时会写needs_review并退出，需本聊天继续定位，不算完成。最终审核全部图/CSV/模型/预算与报告后再暂停每小时automation；目前保持ACTIVE。

本聊天已批准实施原LTE对照S4计划；不接续NLTE，不使用子代理。新项目独立于所有旧项目。启动日期2026-11-05，主输入400uJ入射、1064nm、60ns、w0=11um、Asurf=.1、tp=120ns，常压空气293.15K。

目标数值0–2.12us，前1us每2ns输出，随后每10ns。单个重求解串行，累计COMSOL预算4小时、每例45min；官方保存停止，不杀求解器。每小时本聊天automation简报，全部交付后暂停。

首算H00，保留LTE气体守恒、靶焓，双程准静态归一化光学传输；自由蒸发netEvap=0，但同时诊断回流。max_return_fraction_active_evaporation>=.1需净蒸发，depth>1.1um需守恒ALE。不能把失败的固定面结果用于最终S4支持。

入口status.json、solver_budget.json、results/*_audit.json。work/campaign.py一次只运行指定一例，导出审计后停止供本聊天判断，不会自动恢复任何旧队列。worker.lock和status PID需核对存活，禁止重复运行。ExportS4.java积分含轴对称面积/体积；光子入口30阶积分，体积分12阶，原始变量完整核验。

待完成：S4原图29帧/标尺/S2伪彩反演，NIST可见线数据，LTE发光和吸收、视线传输、10ns门宽、统一增益及全局时移早期标定，150–740ns留出比较。475–525nm主通带，490–510/450–550敏感性。辐射能量超过吸收5%需反馈重算。有效工况网格.75、时间步减半对照。图形Python，Miniconda运行时带matplotlib/PyMuPDF/Pillow/scipy。

原基线sources/LTE_baseline_ready.mph，SHA256=0C73727387DDAAB9EAF8999C92EE5BB2D39447250605E715F62577E7B350D8D2。所有旧项目保持原样。

## 11:55执行状态（优先于初始待办）

正在重算 **H08**（12:01启动）；先检查status/worker/COMSOL PID，不启动重复求解。H00完整但仅诊断；H01/H03无有效初值；H02/H04网格映射折叠；H05在324 ns时Jmin=.054且反复非线性失败，官方保存停止。H06只建了ready，没有求解。H07柱状ALE非局部装配过慢，在96ns官方保存停止。详见docs/调试记录.md与各例审计。

**重要纠正**：BoundaryMap.java逐条验证边界坐标，先前谐波ALE把solid轴线1误当底边，真正底边为2！H08改为固定2/5/6/7；1/3对称自然边界，4=退缩界面。此前ALE折叠来自此错误，不能归咎纯网格平滑。H08回到稀疏谐波保守ALE，meshSmoothZ100，Rdom=2mm，Zdom=3mm，nuFactor2，maxstep.5ns，nuLocal1低Y梯度前沿，手动物理尺度，Newton minimal Jacobian/15次迭代。H07保留为正确柱状映射诊断，但成本太高；不继续此分支。

已完成S4真实29帧提取、S2色条反演、NIST可见线收集、LTE谱线/free-free发射吸收/轴对称视线输运、10ns门积分、统一相机标定工作入口。H00场及诊断发光已完成，但不能作有效支持证据。camera_s4.py以0–80ns排序像素亮度分布标定唯一gain和60–120ns(2ns步距)offset；不拟合缺乏靶面参照的垂直平移。背景2。150–740ns留出。H00诊断offset106ns，后期消退过快。

ExportS4修正了边界法向量在up/down内部被当作零的审计错误；H00真实能量残差−1.58%，铜质量.022%，但Y/回流/退缩/外域失败。ExportFields长度表达式必须zPhysical/1[m]；早先H00/H02已转换SI并记录JSON。非均匀插值网格不代表FE加密。radiation_s4.py依据实际zPhysical反映射到相机物理坐标。

下一步：H07审计全门槛→有效设置mesh.75、maxstep减半验证；若失败，只围绕真实诊断修订。最终有效例全时间导出场，radiation_s4.py stride1；camera_s4.py主例标定，验证例--reference主例calibration.json冻结响应。plot_s4.py、plot_metrics.py生成对照/曲线/物理图；CreateViews.java编译已通过但未测试，生成含真实解和物理ALE坐标视图MPH。run_figure_qa.py审计所有PDF。尚需辐射能量上界、数值敏感性、支持/不吻合中文报告及复现入口。

截至H05累计求解1588秒，预算文件另加H07活动时间；总4小时，单探索45min。H02/H04早期人工官方停止标志仍记false，最终对账需纠正，真实时长已累计不可重复计时。每小时automation仍ACTIVE，完成/耗尽正式交付后暂停。

## 13:52 主解后处理核查
H15完整守恒主解保留不变，radiation PID37816继续处理全部613个能量节点及全部S4门宽；后续VG/VT仍由PID17336串行调度。不要另启主算。新增plot_conservation.py已实际渲染H15并通过源预检、对齐、PDF字号及碰撞审计。主post进程36420早于新增光子图/守恒图调用，因此camera文件生成后应手动运行plot_photon_scale.py H15；守恒图已经生成。所有新增后处理与原求解预算无关。
缓存谱线输运已通过真实H15射线及解析平板核验；原函数保留为transfer_line_reference，后续验证使用缓存函数。主H15在内存中的旧函数继续执行，不中断或重复处理。compare_numerics另保存饱和前物理积分亮度变化，报告生成器已可读取。


## 14:04 调度修正与主图核查
协调器已更新为 PID34012，日志finish_H15_v3_stdout/stderr；仅替换Python父调度进程，VG worker20780与COMSOL10356继续原求解。H15后处理及view/settings已完整生成，复用，不重复辐射处理或主算。
预算调度修正：验证一旦真实接受时间覆盖865ns，立即请求官方保存停止，理由明确记S4_window_checkpoint；不必空等下一个45min检查点。先保障两种离散设置都覆盖全部S4门宽，再用余下预算延长全时域。仍一解一次、每段≤45min、累计4h；不把这种真实窗口检查点标成完整解。
H15图形已人工逐帧检查：29帧实验/仿真、9时刻并排、6指标曲线、5通带/PSF敏感性、20个物理场面板、6个光子量级面板和4个守恒面板。所有最终PDF字号≥5pt、对齐与碰撞检查通过。光子图Arial缺失的上标负号已换为1e-10并重画审计。主例增益2.421e43、时移120ns触及允许上界；留出实验可见20帧中半径0帧通过，面积峰值0.06025 vs0.30309mm²；150ns理想整图光子仅5.62e−10，不能据极大增益均值图声称真实发光吻合。

## 主相机极弱信号修正（旧灰度指标撤为诊断）
当前协调器PID33936，日志finish_H15_v4_*；VG worker20780/COMSOL10356持续原求解，未中断。发现旧输运column<1e-30、max(q)<1e-30的绝对快速截断会被2.4e43量级增益放大，旧数学灰度消光时刻/面积不是最终值。旧相机及所有图已完整归档diagnostics/H15_transport_floor_v1；流体守恒、可见能量上界及物理上缺乏后期光子的结论不受此截断影响。
已把两种绝对截断改为只跳过严格零，并把辐亮度存为float64；解析单元验证增加1e-50发射非零保留。VG真实S4窗口检查点后、VT启动前，将依据main_post_refresh_required.json串行重做H15后处理，避免CPU竞争占求解预算。不要直接使用当前根目录H15_camera旧指标作最终交付；以main_post_refresh_completed.json和新summary完成为准。

## 14:12 验证早期求解成本修正
当前driver PID28484，命令finish_campaign.py H15 --grid-start VGM --time-start VTM，日志finish_H15_v5_*。VG已官方保存0–100ns并审计，除完整时域外全项通过；这是成本修订检查点，不是数值失败。以VGM从VG的真实100ns状态续算，完整history并用官方CombineSolution合并。VTM是VT原准备模型的独立新副本，从初态开始；最大步仍.25ns。
实际日志显示：VG每迭代更新Jacobian在307步已327次Jacobian；主H08同脉冲早期403步仅24次。两种验证改用minimal Jacobian+自动阻尼，保留网格、步长、容差、物理/稳定化方程；不再用全Newton支付早期不必要分解成本。此依据仅限脉冲早期，不能冒充H15后期复用已成功。若真实非线性失败仍须审计处理。VT/VG原ready与结果均保留。
后续顺序：VGM完成全部S4门宽检查点→主H15去掉绝对发射截断后重后处理→VTM→剩余预算延长晚期→验证后处理。main_post_refresh_required.json未消除前不许生成最终报告。

## 14:31 最新执行状态（优先）
新driver PID38188：finish_campaign.py H15 --grid-start VGE --time-start VTE --early-switch，日志finish_H15_v7_*。VGE已真实保存并合并0–260ns，除完整时域外全项通过，铜质量−.00131%、能量−.01394%。driver将自动生成VGEA：保持网格.75/最大步.5ns/全部物理，改每次Jacobian+自动阻尼越过细网格在258ns的实际非线性转折。然后S4窗口官方检查点→H15主相机修正后处理→独立VTE(.25ns)→后期延长。
minimal+auto的VGM确实未复用Jacobian，不能宣称收益；VGE的minimal+常数阻尼1在499步仅17次Jacobian、两分钟由116推进至203ns，实际提速；细网格的转折早于主网格315ns，258ns已出现18次非线性拒步，所以按实际诊断提前转自动阻尼，不强等300ns。
VGE停止请求因Windows默认GBK写入中文，旧worker读取UTF8失败退出；求解器未杀死或重复启动。recover_active_case.py已核实/接管原PID7848，官方保存停止，按原始started_epoch完整补记425.807秒（无预算漏记），MergeHistory及数值审计完成。原错误请求归档diagnostics/VGE_stop_request_wrong_encoding.json。campaign.py已可识别GB18030并规范为UTF8，不再因该编码遗弃求解器；后续调度状态也显式写UTF8。恢复进程已退出，没有孤立求解器。
main_post_refresh_required.json仍存在，当前旧主相机灰度指标仅诊断；禁止用其旧面积/消光时刻作最终交付。流体H15与全FE可见能量审计仍有效。之后只用修正无绝对发射截断的float64辐亮度。

不依赖主相机截断的物理上界已新增results/H15_photon_upper_bound.json/CSV：全FE域380–780nm、全发射方向、无吸收、780nm最小光子能量，并对所有60–120ns允许时移取最大。实验260ns的10ns门宽内最多9.242e−6个已建模可见光子，400ns最多6.195e−13。此上界不依赖NA、像素、增益、视线范围或待修正相机截断；只约束NIST原子谱线/free-free，不包含遗漏发光机理。最终报告已加入，谨慎结论为“这轮指定闭合未支持S4后期发光”，不排除全部LTE模型、不唯一归因非平衡。

后处理已取消谱线及free-free的绝对发射截断，float64保存。free-free光深上界<1e−4时使用精确通带解析积分（吸收忽略相对误差≤1e−4），其余保留频率分辨输运；解析薄极限在293–30000K误差<3e−16，厚平板对高精度积分误差2.68e−6，见free_free_transport_audit.json。此改进保留极弱发射，同时避免其高计算成本。
全FE光子上界改为按781nm的更低光子能量保守换算，以覆盖380–780nm空气波长的真空换算端点；最终数值读取JSON，不沿用上方780nm近似值。

## 14:54 当前调度入口
当前driver **PID13140**，日志`finish_H15_v8_stdout.txt`/`finish_H15_v8_stderr.txt`，命令`finish_campaign.py H15 --grid-start VGEA --time-start VTE --early-switch --grid-current-strategy --late-constant`。VGEA worker1088/COMSOL23380正在原任务，至约0.68μs；父调度器替换未触动求解。后续先两种S4窗口，再晚期延长；主相机修正仍在网格S4窗口后、VTE之前串行进行。
晚期新增一个明确的直接成本尝试：两种S4真实窗口保存后，分别从其真实状态改minimal Jacobian+常数Newton阻尼1求至完整2.12μs，保留网格、步长、容差和方程。目的是在激光已结束、冷却期状态更平缓时尝试实际矩阵复用；尚未运行、不能预称收益。实际失败即停审，不盲延续，最终用日志与审计判断。前期minimal+auto未获收益的记录仍有效，不混淆阶段。
复现入口新增`reproduce.py resume SOURCE NEW`，并有显式Newton策略选项。只对除时域外全项通过的保存解继续，自动真实history拼接且不清零预算。说明及示例已加入docs/复现说明.md。


## 用户暂停并要求服务器交接（最新，优先于前文）
2026-11-05T15:21:15.001768+08:00：已停止本机Python协调器与只读后处理，无COMSOL求解器在运行，未杀求解器。每小时automation已PAUSED。H15完整0–2.12us审计通过；VGEA真实0–880ns除complete外全部通过；VTE未启动。main_post_refresh_required.json仍在，修正主相机后处理被用户暂停，未完成的当前H15_radiation是新旧混合，不可用于定量交付；旧图/相机仅诊断。累计COMSOL10001.115395069122秒，剩余4398.884604930878秒；迁移不得清零。后续不在本机自动恢复，按服务器交接文档继续。
