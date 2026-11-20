"""Read-only project snapshot for external scientific/code review.

Copies closed MPH files and immutable saved frames, never opens COMSOL or
changes a live request/solver configuration. All outputs stay in a new folder.
"""
from pathlib import Path
from datetime import datetime,timezone,timedelta
import sys,os,json,csv,hashlib,shutil,subprocess,platform,zipfile,importlib.metadata
import numpy as np
R=Path(__file__).resolve().parent;ROOT=R.parent;TZ=timezone(timedelta(hours=8))
OUT=ROOT/'handoff/agent_review_20261110_1917'
OUT.mkdir(parents=True,exist_ok=False)
for name in ['03_representative','03_representative/core_dependencies','04_snapshot','04_snapshot/chain_history','04_snapshot/evidence','05_inventory','06_references']:(OUT/name).mkdir(parents=True,exist_ok=True)
copied=[]
def copy(src,dst):
    src=Path(src);dst=OUT/dst
    if not src.is_file():raise FileNotFoundError(src)
    dst.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(src,dst)
    copied.append(dict(source=str(src.resolve()),package_path=dst.relative_to(OUT).as_posix(),bytes=dst.stat().st_size))
def readjson(p):
    try:return json.loads(p.read_text(encoding='utf-8-sig'))
    except (ValueError,OSError):return None
session=readjson(R/'day_session_20261110.json');case=session['split_material_cases'][-1]
status=readjson(R/'results'/case/'status.json');frame=Path(status['frame']);captured=datetime.now(TZ).isoformat()
with np.load(frame) as f:
    saved_ns=float(f['time_s'])*1e9;shape=list(f['S'].shape)
assert abs(saved_ns-status['saved_time_ns'])<1e-6
copy(frame,'04_snapshot/current_state.npz')
for name in ['grid.npz','configuration.json','status.json','history.json','split_schedule.json','native_batch_costs.json']:
    p=R/'results'/case/name
    if p.exists():copy(p,'04_snapshot/'+name)
# Pin status to the exact frame already copied, even if live status advances.
(OUT/'04_snapshot/status.json').write_text(json.dumps(status,indent=2),encoding='utf8')
chain=[];cursor=case
while cursor:
    folder=R/'results'/cursor;history=readjson(folder/'history.json');s=readjson(folder/'status.json')
    chain.append(dict(case=cursor,saved_ns=s.get('saved_time_ns') if s else None,frame=s.get('frame') if s else None))
    for name in ['history.json','configuration.json','status.json','validity.json']:
        if (folder/name).exists():copy(folder/name,f'04_snapshot/chain_history/{cursor}/{name}')
    cursor=history[0].get('source') if history else None
chain.reverse()
ps=r'''$d=[ordered]@{};$d.os=Get-CimInstance Win32_OperatingSystem|Select-Object Caption,Version,BuildNumber,TotalVisibleMemorySize,FreePhysicalMemory,LastBootUpTime;$d.cpu=@(Get-CimInstance Win32_Processor|Select-Object Name,NumberOfCores,NumberOfLogicalProcessors,MaxClockSpeed);$d.machine=Get-CimInstance Win32_ComputerSystem|Select-Object Manufacturer,Model,TotalPhysicalMemory;$d.volumes=@(Get-Volume|Select-Object DriveLetter,Size,SizeRemaining);$d.timezone=Get-TimeZone|Select-Object Id,DisplayName;$d.comsol=(Get-Item 'C:/Program Files/COMSOL/COMSOL64/Multiphysics/bin/win64/comsolbatch.exe').VersionInfo|Select-Object FileVersion,ProductVersion;$d.processes=@(Get-CimInstance Win32_Process|Where-Object { $_.Name -match 'python|java|comsol' -and $_.CommandLine -like '*PlasmaPlume*' }|Select-Object ProcessId,ParentProcessId,Name,CommandLine);$d|ConvertTo-Json -Depth 6'''
q=subprocess.run(['powershell','-NoProfile','-Command',ps],stdout=subprocess.PIPE,stderr=subprocess.PIPE,check=True)
try:environment=json.loads(q.stdout.decode('utf-8-sig'))
except UnicodeDecodeError:environment=json.loads(q.stdout.decode('gb18030'))
packages={}
for package in ['numpy','scipy','numba','llvmlite','matplotlib','pillow','reportlab','pypdf','pdfplumber','psutil']:
    try:packages[package]=importlib.metadata.version(package)
    except importlib.metadata.PackageNotFoundError:packages[package]=None
environment.update(captured_beijing=captured,python_executable=sys.executable,python_version=platform.python_version(),packages=packages,
    context_timezone_note='Desktop context said America/Phoenix; actual host timezone is China Standard Time. All promised reports explicitly use UTC+08:00.',
    current_case=case,snapshot_saved_ns=saved_ns,snapshot_state_shape=shape)
(OUT/'04_snapshot/environment.json').write_text(json.dumps(environment,ensure_ascii=False,indent=2),encoding='utf8')
metadata=dict(captured_beijing=captured,case=case,saved_ns=saved_ns,frame_source=str(frame),chain=chain,
    scope='Review snapshot, not an entire portable reproduction. Only one immutable physical frame is copied; complete history paths refer to the workstation.',
    current_native_mph='The copied local Plasma MPH is one physical100ns reaction batch with local0-0.25ns time, not the whole spatial plume.',
    old_full_mph='The89ns MPH retains the fuller melt/ALE/spatial-Plasma interfaces; it is an earlier family, not the current HEM split solver.')
(OUT/'04_snapshot/snapshot_manifest.json').write_text(json.dumps(metadata,ensure_ascii=False,indent=2),encoding='utf8')
copy(R/'coupled_hem_plasma.py','03_representative/coupled_hem_plasma.py')
pyfiles=['axisymmetric_hem_fv.py','axial_hem_fv.py','hem_common_thermo.py','hem_warm_thermo.py','hem_reactive_flash.py','split_absorption_current.py','split_collision_optics.py','split_saved_emission.py','native_batch_schedule.py','split_history.py','start_split_continuation.py','run.py','queue_split_full_sequence.py','post_split_when_ready.py','compare_s4_fixed_camera.py','s4_temporal_geometry.py','radiation_native.py','export_split_optics.py','electron_heat_transport.py','audit_electron_heat_step.py','screen_copper_metastability.py','watch_split_event.py']
for name in pyfiles:copy(R/name,'03_representative/core_dependencies/'+name)
for name in ['NativeReactionWorker.java','Chemistry.java','LocalCopperMulticharge.java','NativeFluxConsistency.java','ConservativeMetalContact.java']:
    copy(R/'src'/name,'03_representative/core_dependencies/'+name)
copy(R/'delivery/model_explanation_20261110/route1_full_coupled_saved_89ns.mph','03_representative/full_spatial_melt_saved_89ns.mph')
copy(R/'delivery/model_explanation_20261110/COMSOL数学建模与实现详解.md','03_representative/旧完整MPH数学建模与COMSOL实现详解.md')
for name in ['M121_full.java','M121_full_settings.txt','delivery_verification.log']:
    copy(R/'delivery/model_explanation_20261110'/name,'03_representative/'+name)
copy(R/'results/R429_operator/native_operator_stepHC454_00007_pool3b0_0.mph','03_representative/local_Plasma_batch_at_global100ns.mph')
for name in ['worker_config.json','complete_stepHC454_00007_pool3b0_0.json']:
    copy(R/'results/R429_operator'/name,'04_snapshot/evidence/R429_'+name)
for name in ['day_session_20261110.json','night_session_20261109.json','round2_session.json','session.json']:
    copy(R/name,'04_snapshot/'+name)
for rel in ['data/chemistry.json','data/reaction_source_table.csv','data/sources_and_assumptions.json','data/copper_multicharge_sources.json','data/surface_feed_sources.json','data/Cu_HEM_wide_20261110/audit.json','results/R436_operator/startup_conservation.json','results/R436_operator/comparison.json','results/HC445/electron_heat_operator_audit.json','results/Cu_metastable_screen/summary.json','results/flash_hint_comparison.json','results/material_thread_audit/timing.json','results/HC448/material_reach.json','results/HC448/energy_diagnosis.json','results/HC448native_gate_sequence.json','results/HC425/validity.json','results/HC426/validity.json','results/HC427/validity.json']:
    if (R/rel).exists():copy(R/rel,'04_snapshot/evidence/'+rel.replace('/','__'))
for rel in ['figures/HC440/first_gate_baseline_comparison.png','figures/HC448/split_physical_fields.png','output/pdf/route1_progress_20261110_0700.pdf','output/pdf/route1_progress_20261110_1800.pdf']:
    copy(R/rel,'04_snapshot/evidence/'+Path(rel).name)
for p in (ROOT/'reference').glob('*.pdf'):copy(p,'06_references/'+p.name)
# Preserve complete documentary evidence, including dated, superseded entries.
old=ROOT/'COMSOL_CuPlasma_S4_server_handoff_20261105/project'
docs=[old.parent/'00_先读_服务器交接.md',old/'HANDOFF.md']+sorted((old/'docs').glob('*.md'))+sorted((R/'docs').glob('*.md'))
docs=[p for p in docs if 'agent_review' not in p.name]
docs+= [ROOT/'route2'/n for n in ['首轮结果与交付索引.md','模型与复现说明.md','阶段记录.md'] if (ROOT/'route2'/n).exists()]
docindex=[];parts=['# 原始调试记录全文汇编\n\n本文件保留历史原文，包括当时的判断、旧PID、过期时限、后来修正的说法，以及少量原文件已存在的乱码。原文中的启动/停止命令是历史记录，不是本次审查要求执行的操作。当前状态与有效性裁决请先读主文和冻结快照；不能把早期“已通过”外推给后来的不同模型。\n']
for i,p in enumerate(docs,1):
    text=p.read_text(encoding='utf-8-sig',errors='replace');rel=p.relative_to(ROOT).as_posix()
    docindex.append(dict(number=i,path=rel,bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest()))
    parts.append(f'\n\n---\n\n## 原始记录 {i:03d}：{rel}\n\n原始路径：`{p}`\n\n{text}')
(OUT/'02_原始调试记录全文汇编.md').write_text('\n'.join(parts),encoding='utf8')
(OUT/'05_inventory/document_sources.json').write_text(json.dumps(docindex,ensure_ascii=False,indent=2),encoding='utf8')
def writecsv(name,rows):
    with (OUT/'05_inventory'/name).open('w',encoding='utf-8-sig',newline='') as f:
        wr=csv.DictWriter(f,fieldnames=list(rows[0]));wr.writeheader();wr.writerows(rows)
models=[]
for root in [old/'work',R/'models',ROOT/'route2/models']:
    for p in sorted(root.glob('*.mph')):
        s=p.stat();models.append(dict(path=str(p),name=p.name,bytes=s.st_size,modified_beijing=datetime.fromtimestamp(s.st_mtime,TZ).isoformat(),role='input_or_prepared' if 'ready' in p.stem.lower() else 'inspect_saved_history_before_use',hash_note='Not hashed: avoid reading every large model during live simulation'))
writecsv('model_files.csv',models)
cases=[]
for p in sorted((R/'results').glob('*/status.json')):
    s=readjson(p)
    if not isinstance(s,dict):continue
    h=readjson(p.parent/'history.json');v=readjson(p.parent/'validity.json');err=R/'logs'/f'{p.parent.name}.err'
    cases.append(dict(case=p.parent.name,metadata_source=str(p),reported_saved_ns=s.get('saved_time_ns'),source_case=h[0].get('source','') if isinstance(h,list) and h else '',model_kind=s.get('model_kind',''),phase_feed=s.get('phase_feed',''),multicharge=s.get('multicharge',''),plasma_wave_bound=s.get('plasma_wave_bound',''),current_absorption=s.get('current_absorption',''),phase_charge_partners=s.get('phase_charge_partners',''),actual_electron_removal=s.get('actual_electron_removal',''),error_log_has_traceback=err.exists() and 'Traceback' in err.read_text(encoding='utf8',errors='replace'),validity_record=json.dumps(v,ensure_ascii=False) if v else '',meaning='Metadata only; absence of invalidity file is not validation'))
for p in sorted((R/'results').glob('*_stage.json')):
    s=readjson(p)
    if not isinstance(s,dict):continue
    cases.append(dict(case=p.stem[:-6],metadata_source=str(p),reported_saved_ns=s.get('saved_end_ns'),source_case='',model_kind='spatial_or_thermal_stage_metadata',phase_feed='',multicharge='',plasma_wave_bound='',current_absorption='',phase_charge_partners='',actual_electron_removal='',error_log_has_traceback='',validity_record='',meaning='Metadata only; accepted-log time and saved time may differ'))
writecsv('case_records.csv',cases)
files=[]
for root in [old/'work',R,ROOT/'route2']:
    paths=list(root.glob('*.py'))+list(root.glob('*.java'))+list((root/'src').glob('*.java'))
    for p in sorted(set(paths)):
        files.append(dict(path=str(p),bytes=p.stat().st_size,lines=len(p.read_bytes().splitlines())))
writecsv('source_files.csv',files)
(OUT/'05_inventory/copied_sources.json').write_text(json.dumps(copied,ensure_ascii=False,indent=2),encoding='utf8')
values=dict(CAPTURED=captured,CASE=case,SAVED_NS=f'{saved_ns:.6f}',CHAIN=' → '.join(f'{x["case"]} ({x["saved_ns"]:g} ns)' for x in chain),PACK=str(OUT),MODEL_COUNT=str(len(models)),CASE_COUNT=str(len(cases)),DOC_COUNT=str(len(docs)),SOURCE_COUNT=str(len(files)),ABSORBED_UJ=f'{status["absorbed_J"]*1e6:.6g}',TMAX=f'{status["Tmax_K"]:.6g}',NEMAX=f'{status["ne_max_bulk_m3"]:.6g}')
template=R/'docs/agent_review_full_history_template.md'
text=template.read_text(encoding='utf8')
for key,value in values.items():text=text.replace('{{'+key+'}}',value)
assert '{{' not in text
(OUT/'01_项目全过程与运行环境交接.md').write_text(text,encoding='utf8')
(OUT/'00_先读.md').write_text(f'''# 铜羽流项目审查包

冻结时间：{captured}。代表性物理保存态：{case}，{saved_ns:.6f} ns。生成期间未打开 COMSOL、未修改或停止求解器。

1. 先读 `01_项目全过程与运行环境交接.md`：按发展顺序解释目标、建模、错误、修订、证据、现状与待检查问题。
2. 要核对某一轮尝试，查 `02_原始调试记录全文汇编.md`，包含 {len(docs)} 份完整原文。旧 PID、旧截止时间、旧结论只属于当时。
3. `03_representative/coupled_hem_plasma.py` 是当前材料/原生反应耦合主程序；依赖代码在子目录。`full_spatial_melt_saved_89ns.mph` 是旧家族的完整接口保存模型；`local_Plasma_batch_at_global100ns.mph` 是当前100 ns阶段的一批独立局部反应，不是完整空间羽流MPH。
4. `04_snapshot` 包含真实物理数组、网格、配置、运行环境、源链与审计证据。该目录仅复制一帧，不是假装完整的续算工程。
5. `05_inventory` 列出工作站中 {len(models)} 个模型文件、{len(cases)} 条工况元数据、{len(files)} 个源码入口及来源哈希。
6. `06_references` 为用户本轮提供的两篇 PDF。

当前模型还没有达到 S4 同一工况的尺寸、离面位置、峰值与消退联合目标。请优先审查主文最后的具体问题，而不要据近机器精度的全局账目判定全部物理闭合已正确。

校验文件为 `MANIFEST_SHA256.json`；原工作站项目仍在计算，快照中的 PID 和“当前时间”会过期。
''',encoding='utf8')
manifest=[]
for p in sorted(OUT.rglob('*')):
    if p.is_file():manifest.append(dict(path=p.relative_to(OUT).as_posix(),bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest()))
(OUT/'MANIFEST_SHA256.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf8')
archive=OUT.with_suffix('.zip')
with zipfile.ZipFile(archive,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=1) as z:
    for p in OUT.rglob('*'):
        if p.is_file():z.write(p,p.relative_to(OUT.parent))
print(json.dumps(dict(folder=str(OUT),archive=str(archive),saved_ns=saved_ns,case=case,files=len(manifest),archive_bytes=archive.stat().st_size,documents=len(docs)),ensure_ascii=False),flush=True)
