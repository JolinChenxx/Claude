"""Final local delivery index and SHA256 manifest, without copying large models."""
from pathlib import Path
import json,hashlib,datetime
R=Path(__file__).resolve().parents[1];B=R.parent

def sha(path):
    digest=hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda:stream.read(8*1024*1024),b''):digest.update(block)
    return digest.hexdigest()

def main():
    state=json.loads((R/'delivery_state.json').read_text(encoding='utf8'))
    if state.get('phase')!='ready_for_final_review':raise RuntimeError('Review unresolved campaign state before delivery')
    if (R/'worker.lock').exists() or (R/'coordinator64.lock').exists():raise RuntimeError('Active solver or coordinator still owns the project')
    report=json.loads((R/'results/delivery_summary.json').read_text(encoding='utf8'))
    cases=list(dict.fromkeys(c for c in [state.get('main_case'),state.get('grid_case'),state.get('time_case'),state.get('extra_grid_case')] if c))
    verification=json.loads((R/'results/delivery_verification64.json').read_text(encoding='utf8'))
    if not verification.get('artifact_checks_passed') or set(verification.get('cases',{}))!=set(cases):
        raise RuntimeError('Final artifact verification is missing or belongs to other cases')
    files=set();rows=[]
    for case in cases:
        audit=json.loads((R/f'results/{case}_audit.json').read_text(encoding='utf8'))
        post=R/f'results/{case}_postprocess.json'
        if not post.exists():raise RuntimeError('Incomplete postprocessing: '+case)
        marker=json.loads(post.read_text(encoding='utf8'))
        if marker.get('case')!=case or marker.get('numerical_status')!=audit.get('status'):
            raise RuntimeError('Postprocessing provenance mismatch: '+case)
        if audit.get('status')=='passed' and marker.get('diagnostic'):
            raise RuntimeError('Complete case has only diagnostic postprocessing: '+case)
        for name in ['_audit.json','_fields.json','_fields.bin','_camera/summary.json']:
            if not (R/'results'/(case+name)).is_file():raise FileNotFoundError(case+name)
        for name in [case+'.mph',case+'_view.mph']:
            p=R/'work'/name
            if not p.is_file():raise FileNotFoundError(p)
            files.add(p)
        for pattern in [case+'_resume.json',case+'.mph_join64.json']:
            files.update((R/'work').glob(pattern))
        for folder in ['results','figures']:
            for p in (R/folder).glob(case+'*'):
                if p.is_file():files.add(p)
                elif p.is_dir():files.update(q for q in p.rglob('*') if q.is_file())
        files.add(R/f'figures/qa_{case}.json')
        rows.append(f'| {case} | {audit["last_time_ns"]:.0f} ns / {audit["status"]} | [模型](work/{case}.mph) · [结果视图](work/{case}_view.mph) | [审计](results/{case}_audit.json) · [同尺度对照](figures/{case}_S4_comparison.png) |')
    for folder in ['docs','data','experiment','logs']:
        files.update(p for p in (R/folder).rglob('*') if p.is_file())
    files.update(p for p in (R/'output/pdf').rglob('*') if p.is_file())
    files.update(p for p in (R/'figures/analysis64').rglob('*') if p.is_file())
    # Preserve the repaired historical reference separately from new 6.4 cases.
    historical='H15R64'
    for name in [historical+'.mph',historical+'_view.mph']:
        files.add(R/'work'/name)
    for folder in ['results','figures']:
        for p in (R/folder).glob(historical+'*'):
            if p.is_file():files.add(p)
            elif p.is_dir():files.update(q for q in p.rglob('*') if q.is_file())
    files.add(R/f'figures/qa_{historical}.json')
    files.update((R/'work').glob('*_resume.json'))
    files.update((R/'work').glob('*.mph_join64.json'))
    # Include the current reproducible sources and Java entry classes. Historical
    # source code remains identifiable by the current reproduction documentation.
    files.update(p for p in (R/'work').iterdir() if p.suffix in ['.py','.java','.class'])
    files.add(R/'work/VTE_ready.mph')
    files.update(p for p in R.glob('*.json') if p.name!='DELIVERY64_manifest.json')
    files.update(R.glob('requirements*'))
    files.add(R/'README.md')
    files.update(p for p in (R/'results').glob('*.json') if not p.name.startswith(tuple(c+'_PP64' for c in ['B64A','G64A'])))
    for pattern in ['*comparison*.csv','extra_grid*.csv','run_ledger.csv']:
        files.update((R/'results').glob(pattern))
    for name in ['physical_numerical_comparison64.png','S4_numerical_convergence64.png']:
        if (R/'figures'/name).is_file():files.add(R/'figures'/name)
    files.update(p for p in (B/'server_tools').rglob('*') if p.is_file() and '__pycache__' not in p.parts)
    files.update(p for p in (B/'vendor').rglob('*') if p.is_file() and '__pycache__' not in p.parts)
    files.add(B/'run64.ps1')
    files.add(B/'MANIFEST_SHA256.json')
    maincase=state['main_case']
    content=['# COMSOL 6.4 铜羽流仿真交付索引','',
             '新增：[工作站handoff PDF](output/pdf/HANDOFF_工作站续作与讲解_20261106.pdf)或[浏览器版](output/pdf/HANDOFF_工作站续作与讲解_20261106.html)：说明本轮工作、实际结果、原电脑讲解入口及下一步。','',
             '集中汇总文件夹：D:/junlin/PlasmaPlume/铜羽流仿真_工作站工作汇总_20261106。整个文件夹复制到原电脑后，双击其中00_先读.html；包内链接均为相对路径。','',
             '新增：[问题分析与方法推导PDF](output/pdf/铜羽流仿真问题分析与方法推导.pdf)或[浏览器版](output/pdf/铜羽流仿真问题分析与方法推导.html)：解释流场、温度、LTE局限、成像、实验差异及改进顺序，共20页；图片与公式全部内嵌。','',
             '推荐先读[图文结果报告PDF](output/pdf/铜羽流仿真结果解读与论文对照.pdf)或[浏览器图文报告](output/pdf/铜羽流仿真结果解读与论文对照.html)：包含物理解释、论文S4直接对照及可信范围，图片已内嵌。','',
             '先阅读[中文最终报告](docs/最终报告.md)。数值守恒审计、S4离散敏感性和实验吻合分别判定；以报告及机器可读审计为准。','',
             '后续计算继续使用当前课题组三人共用工作站，详见[当前工作站与后续计算](docs/当前工作站与后续计算.md)。','',
             '| 工况 | 真实保存时域 / 数值审计 | COMSOL 文件 | 数据与对照 |','|---|---|---|---|',*rows,'',
             f'- [基准物理场动画](figures/{maincase}_physical_evolution.gif)：固定色标，真实数值时间，不使用相机增益。',
             f'- [基准实验对照动画](figures/{maincase}_S4.gif)：冻结全局相机响应，须结合光子量级图解释。',
             '- [H15R64历史参考视图](work/H15R64_view.mph)：6.2流体解在6.4重新导出和修复后处理，不属于四个全新初态验证工况。',
             '- [数值比较](results/numerical_comparison.json)及[逐帧CSV](results/numerical_comparison.csv)。',
             '- [四项收敛指标图](figures/S4_numerical_convergence64.png)：虚线为10%目标。',
             '- [补充物理量曲线](figures/physical_numerical_comparison64.png)。',
             '- [环境与复现](docs/环境与复现64.md)、[执行记录](docs/COMSOL64执行记录.md)、[运行账本](results/run_ledger.csv)。',
             '- 所有列入交付的文件及SHA256见`DELIVERY64_manifest.json`。使用的是当前目录内真实文件，不依赖文件快捷方式。','',
             '原始交接ZIP和migration_originals保留在原位置，未混入正式6.4验证工况。历史诊断文件仍可追溯，但不因留存在目录中而自动视为有效交付结果。',
             '迁移到另一目录或电脑时，按复现说明重配Python搜索路径、COMSOL安装路径与LTE表路径；历史墙钟预算不会自动重置。','']
    if state.get('extra_grid_case'):content.extend(['追加网格比较：[相对原基准](results/extra_grid_vs_baseline64.json)、[相邻加密级](results/extra_grid_adjacent64.json)。',''])
    from local_index64 import write_indexes
    write_indexes('\n'.join(content))
    index=R/'交付索引64.md';files.add(index);files.add(R/'交付索引64.html')
    payload=[]
    for i,p in enumerate(sorted(files)):
        if p.suffix in ['.lock','.tmp']:continue
        before=p.stat();digest=sha(p);after=p.stat()
        if (before.st_size,before.st_mtime_ns)!=(after.st_size,after.st_mtime_ns):raise RuntimeError('File changed during hashing: '+str(p))
        payload.append({'path':str(p.relative_to(B)).replace('\\','/'),'bytes':after.st_size,'sha256':digest})
        if i%100==0:print('MANIFEST',i,len(files),flush=True)
    manifest={'created_beijing':datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat(),
              'base_directory':str(B),'cases':cases,'delivery_status':report,'files':payload,
              'file_count':len(payload),'total_bytes':sum(x['bytes'] for x in payload),'manifest_self_hash_excluded':True}
    (R/'DELIVERY64_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf8')
    print('DELIVERY_INDEX_AND_HASHES',len(payload),manifest['total_bytes'],flush=True)

if __name__=='__main__':main()
