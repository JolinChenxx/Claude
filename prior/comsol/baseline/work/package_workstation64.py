"""Collect portable reading artifacts and final solved MPH snapshots, verify copies."""
from pathlib import Path
import csv,datetime,hashlib,html,json,re,shutil
from urllib.parse import quote,unquote

R=Path(__file__).resolve().parents[1]
DEST=R.parents[1]/'铜羽流仿真_工作站工作汇总_20261106'
CASES=['B64A','G64AC2','T64AC2','G264R1C5','H15R64']
HAND='HANDOFF_工作站续作与讲解_20261106'
SRCFILES={
 'B64A_ready_model.java':'基准模型导出的建模历史与最终表达式；同名设置以最后覆盖值为准。',
 'Prepare64.java':'从初态派生独立网格/时间步工况并清空历史解。',
 'campaign.py':'worker、官方保存停止、进程锁、求解预算与状态保护。',
 'run_campaign64.py':'基准、时间步和网格验证的协调运行。',
 'radiation_s4.py':'LTE粒子布居、谱线/自由-自由发射、ALE物理坐标和视线积分。',
 'camera_s4.py':'10 ns曝光、模糊、全局标定、饱和与光子量级。',
 'compare_numerics.py':'四项S4观测量的离散敏感性判定。'}
CSS='body{font:16px/1.75 "Microsoft YaHei",sans-serif;max-width:1150px;margin:30px auto;padding:0 24px;color:#253849;background:#f5f8fa}h1,h2{color:#14556e}h1{font-size:28px}section,.card{background:white;border:1px solid #d9e2e8;padding:20px;margin:20px 0;border-radius:8px}a{color:#086ba0}table{border-collapse:collapse;width:100%;font-size:14px;background:white}td,th{border:1px solid #d2dce3;padding:9px;text-align:left;overflow-wrap:anywhere}th{background:#e4f0f4}.scroll{overflow:auto}pre{white-space:pre-wrap;overflow-wrap:anywhere;background:#eef3f6;padding:15px}img{max-width:100%;height:auto}small{color:#5c6d7b}.tag{background:#e6f0f5;padding:4px 9px;border-radius:5px}li{margin:7px 0}'

def sha(p):
    h=hashlib.sha256()
    with p.open('rb') as f:
        for b in iter(lambda:f.read(8*1024*1024),b''):h.update(b)
    return h.hexdigest()
def js(p,x):p.write_text(json.dumps(x,ensure_ascii=False,indent=2),encoding='utf8')
def docpage(title,body):return '<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>'+html.escape(title)+'</title><style>'+CSS+'</style><body><h1>'+html.escape(title)+'</h1>'+body+'</body></html>'
def a(path,label=None):return '<a href="'+quote(str(path).replace('\\','/'),safe='/')+'">'+html.escape(label or Path(path).name)+'</a>'

def main():
    if DEST.exists():raise RuntimeError('Destination already exists; inspect before creating another package: '+str(DEST))
    DEST.mkdir()
    js(DEST/'PACKAGE_STATE.json',dict(status='assembling',source_project=str(R),created_beijing=datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat()))
    rows=[];mapping={};dedup=[];known={}
    old=json.loads((R/'DELIVERY64_manifest.json').read_text(encoding='utf8'))
    for x in old['files']:known[(R.parent/x['path']).resolve()]=x['sha256']
    def copy(src,rel,role):
        src=Path(src).resolve();rel=Path(rel);dst=DEST/rel
        if dst.exists():
            if mapping.get(src)==rel:return dst
            raise RuntimeError('Collision '+str(rel))
        dst.parent.mkdir(parents=True,exist_ok=True)
        stat=src.stat();shutil.copy2(src,dst)
        expected=known.get(src) or sha(src);actual=sha(dst)
        if actual!=expected or src.stat().st_mtime_ns!=stat.st_mtime_ns:raise RuntimeError('Copy verification failed '+str(src))
        rows.append(dict(path=rel.as_posix(),bytes=dst.stat().st_size,sha256=actual,source=str(src),role=role));mapping[src]=rel
        if dst.suffix=='.mph':print('MODEL_VERIFIED',dst.name,round(dst.stat().st_size/1e6,1),'MB',flush=True)
        if dst.suffix=='.bin':print('FIELD_VERIFIED',dst.name,round(dst.stat().st_size/1e6,1),'MB',flush=True)
        if len(rows)%100==0:print('FILES_VERIFIED',len(rows),flush=True)
        return dst
    # Main reports and papers precede figures in the duplicate-selection priority.
    pdfs=list(R.rglob('*.pdf'));finalset=set(CASES)
    def group(p):
        if p.parent==R/'output/pdf':return 0
        if 'sources' in p.relative_to(R).parts:return 1
        if any(p.name.startswith(c+'_') for c in CASES) or p.name=='S4_experiment_reconstruction.pdf':return 2
        return 3
    seenpdf={}
    for src in sorted(pdfs,key=lambda p:(group(p),str(p))):
        if src.name.endswith('_experiment_all29.pdf'):
            dedup.append(dict(source=str(src),reason='同一论文S4的29帧实验图，统一保留S4_experiment_reconstruction.pdf',retained='03_结果图与动画/S4_experiment_reconstruction.pdf'));continue
        digest=known.get(src.resolve()) or sha(src)
        if digest in seenpdf:
            dedup.append(dict(source=str(src),reason='PDF内容SHA256完全相同',retained=seenpdf[digest]));continue
        g=group(src)
        if src.parent==R/'output/pdf':
            rel=Path('01_交接说明' if src.stem==HAND else '02_报告')/src.name
        elif g==1:rel=Path('07_论文与实验素材/原论文')/src.name
        elif g==2:
            case=next((c for c in CASES if src.name.startswith(c+'_')),None)
            rel=Path('03_结果图与动画')/(case or '')/src.name
        else:rel=Path('09_历史与中间PDF归档')/src.relative_to(R)
        copy(src,rel,'PDF报告或图件（历史/中间版本已单列）');seenpdf[digest]=rel.as_posix()
    for src in (R/'output/pdf').glob('*.html'):
        copy(src,Path('01_交接说明' if src.stem==HAND else '02_报告')/src.name,'自包含浏览器报告')
    for src in (R/'docs').glob('*.md'):
        if src.name==HAND+'.md':copy(src,Path('01_交接说明')/src.name,'最新工作站handoff')
        else:copy(src,Path('02_报告/技术与方法文档')/src.name,'技术/方法/执行文档原文')
    # Final plot sets, with a shared experimental image instead of repeated titles.
    for src in (R/'figures').iterdir():
        if not src.is_file() or src.suffix.lower() not in ['.png','.gif','.svg']:continue
        if '_experiment_all29.' in src.name:continue
        case=next((c for c in CASES if src.name.startswith(c+'_')),None)
        if case:copy(src,Path('03_结果图与动画')/case/src.name,'正式工况/历史参考图像与动画')
        elif src.name in ['S4_numerical_convergence64.png','physical_numerical_comparison64.png','S4_experiment_reconstruction.png','S4_experiment_reconstruction.svg']:
            copy(src,Path('03_结果图与动画')/src.name,'统一实验/收敛图')
    for src in (R/'figures/analysis64').glob('*.png'):copy(src,Path('03_结果图与动画/问题推导示意')/src.name,'问题分析图与公式')
    # Keep complete exported fields as well as native histories for analysis without COMSOL.
    for src in (R/'results').iterdir():
        if src.is_file() and src.suffix in ['.json','.csv','.bin']:
            # Final cases and cross-case checks; old partial state files stay on the workstation.
            if any(src.name.startswith(c+'_') for c in CASES) or any(src.name.startswith(s) for s in ['numerical_comparison','extra_grid_adjacent64','extra_grid_vs_baseline64','physical_numerical_comparison64','post_sampling_check64','fine_speed_sensitivity_review64','physics_identity64','migration_integral_check64','thread_','grid_history_export_identity64','extra_grid_history_export_identity64','full_version_comparison64','early_version_comparison64','camera_unit_audit','radiation_unit_audit','free_free_transport_audit','delivery_','analysis_derivation64','G264A_recovery_review64','run_ledger']):
                if '_PP64_' not in src.name:copy(src,Path('04_数据与审计')/src.name,'审计/导出/比较数据')
    for c in CASES:
        for src in (R/f'results/{c}_camera').iterdir():
            if src.is_file():copy(src,Path('04_数据与审计')/(c+'_camera')/src.name,'相机参数、逐帧指标及图像数组')
        for src in (R/f'results/{c}_radiation').rglob('*'):
            if src.is_file():copy(src,Path('04_数据与审计')/(c+'_radiation')/src.relative_to(R/f'results/{c}_radiation'),'物理辐亮度及后处理数组')
    for name in SRCFILES:copy(R/'work'/name,Path('05_代表性源文件')/name,'代表性核心源代码')
    for c in CASES:copy(R/f'work/{c}_view.mph',Path('06_COMSOL结果模型')/(c+'_view.mph'),'真实全时域解与结果视图')
    for src in (R/'data').iterdir():
        if src.is_file():copy(src,Path('06_COMSOL结果模型/LTE与原子数据')/src.name,'LTE状态表/原子数据/来源')
    for src in (R/'experiment').iterdir():
        if src.is_file():copy(src,Path('07_论文与实验素材/实验重建')/src.name,'论文图像及重建数据')
    copy(R/'sources/supplement_relevant_text.txt',Path('07_论文与实验素材/补充材料相关摘录.txt'),'论文摘录')
    for src in (R/'logs').rglob('*'):
        if src.is_file():copy(src,Path('08_执行记录/日志')/src.relative_to(R/'logs'),'运行日志；按工况名区分历史与本轮')
    for name in ['session_64.json','delivery_state.json','solver_budget.json','transfer_verified_64.json','migration_relocation.json','migration_patch_record_64.json','requirements_64_lock.txt']:
        copy(R/name,Path('08_执行记录')/name,'环境/迁移/执行状态快照')
    for src in (R/'output/pdf').glob('*.json'):copy(src,Path('08_执行记录/报告核验')/src.name,'报告内容与排版核验')

    def generated(rel,text):
        dest=DEST/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(text,encoding='utf8');return dest
    source_readme=['# 代表性源文件说明','','以下7个文件用于理解当前实现。它们是完整工程的参考摘选；依赖的入口类、辅助模块和运行环境仍在工作站原工程。','',
                   '| 文件 | 作用 |','|---|---|']
    source_readme += [f'| {n} | {v} |' for n,v in SRCFILES.items()]
    source_readme+=['','源目录：'+str(R/'work'),'','B64A_ready_model.java保存了建模历史，包含多次参数覆盖。分析当前方程时采用同名设置的最后定义，并与B64A_settings.json对照。','模型查看无需编译这些代码；继续求解请用工作站完整工程的docs/环境与复现64.md。']
    generated('05_代表性源文件/源文件说明.md','\n'.join(source_readme))
    modeltxt='''# COMSOL结果模型查看说明

五个view.mph包含实际保存解和结果视图。B64A、G64AC2、T64AC2、G264R1C5是6.4从初态独立计算；H15R64是旧6.2流体解的6.4参考重导出。

四个正式工况均为0至2.12 μs、613个保存时刻。查看模型请用COMSOL 6.4；原电脑6.2可直接阅读本包PDF/HTML和图片。

LTE与原子数据子目录提供lte_grid.txt、lte_eos.npz和原子数据。模型仍记录工作站原绝对路径；已保存解和导入表用于浏览。若在新路径重新导出/求解，在COMSOL的全局定义/插值函数lte中，将数据文件重绑定到本目录的LTE与原子数据/lte_grid.txt并导入，另存一个新模型。工作站完整工程还有RelocateS4工具可执行同样操作。

本包选view.mph避免重复携带同一工况的无视图MPH。原工作站完整MPH、原始场导出和所有源代码仍保留原位。继续计算仍在工作站完成。
'''
    generated('06_COMSOL结果模型/模型查看说明.md',modeltxt)
    generated('04_数据与审计/数据读取说明.md','''# 数据读取说明

每个正式工况与H15R64历史参考均保留完整fields.bin及配套fields.json、积分CSV、审计JSON、camera与radiation数组。

fields.bin采用JSON中声明的数据类型与形状，当前为大端float32（>f4），维度顺序为[保存时刻, 场量, z采样点, r采样点]。字段名见expressions，时刻见times_s，参考采样坐标单位见coordinates_um。

```python
import json, numpy as np
from pathlib import Path
root = Path('.')  # 本数据目录
meta = json.loads((root/'B64A_fields.json').read_text(encoding='utf8'))
fields = np.memmap(root/'B64A_fields.bin', dtype=meta['dtype'], mode='r', shape=tuple(meta['shape']))
print(meta['expressions'])
```

这些是有限元解的规则采样，采样间距不等于求解网格尺寸。ALE的实际高度由zPhysical字段给出，Jale是体积雅可比；按实际空间画图或做视线积分时需正确还原坐标，代表性源代码radiation_s4.py中的physical_grid给出了当前做法。

camera目录的images.npz为合成相机灰度，photon_maps.npz为增益前理想光子诊断；radiation目录保留物理辐亮度。阅读时先看summary.json、calibration.json和CSV表头，以区分物理单位、相机灰度、数值时间与实验时间。
''')
    prompt='''请先读取同目录的HANDOFF_工作站续作与讲解_20261106.md，再读取02_报告中的“铜羽流仿真结果解读与论文对照”和“铜羽流仿真问题分析与方法推导”。我回到原电脑是为了理解和讲解此次工作，后续计算仍在课题组三人共用工作站。

请结合论文S4对照图，深入浅出地解释流场、温度、LTE与相机成像之间的关系。明确哪些是已完成计算、哪些是已定位问题，按我的问题逐步讲解，避免反复防御性表述。

当前四个6.4工况完整到2.12 μs，守恒审计通过，时间步检验通过，网格综合检验未过。后期LTE发光不足，极大相机增益使弱尾部饱和。请从这些真实结果展开，不把模拟明亮轮廓当作实验吻合。

旧版本和中间PDF在09_历史与中间PDF归档，仅供追溯。代表性代码是参考摘选。需要新计算时回工作站完整工程，先检查共享资源并建立新执行窗口。
'''
    generated('01_交接说明/给原电脑助手的接手提示词.txt',prompt)
    archive='''# 历史与中间PDF用途

本目录收录其余不重复的已生成PDF，供追溯原6.2阶段、旧发射截断诊断、部分时域检查点和早期图件审查。

最终结论以02_报告和四个正式工况B64A/G64AC2/T64AC2/G264R1C5为准。H15R64是修复后处理后的历史参考。H00、H15旧图、G64A、G264R1C1等不能代替最终全时域结果。文件名含collision-audit的PDF是排版/图形检查，不是新增物理工况。

所有源PDF均在PDF收录与去重清单.json列明去向；同一S4实验29帧图统一留一份。原文件均保留工作站原位。
'''
    generated('09_历史与中间PDF归档/历史图件说明.md',archive)
    js(DEST/'PDF收录与去重清单.json',dict(source_pdf_count=len(pdfs),retained_unique_PDFs=len(seenpdf),omitted=dedup,policy='主报告与原论文保留；其余PDF按SHA256去重；同一实验29帧图统一选S4_experiment_reconstruction.pdf；历史中间PDF另列。'))
    # Offline Markdown readers: convert copied documents to HTML and repair mapped links.
    def inline(text,source,rel):
        pieces=[];start=0
        for mt in re.finditer(r'\[([^\]]+)\]\(([^)]+)\)',text):
            pieces.append(html.escape(text[start:mt.start()]));label,target=mt.groups()
            target=target.strip('<>')
            if target.startswith(('http://','https://')):pieces.append('<a href="'+html.escape(target,quote=True)+'">'+html.escape(label)+'</a>')
            else:
                origin=(source.parent/unquote(target)).resolve();mapped=mapping.get(origin)
                if mapped:
                    import os
                    link=os.path.relpath(DEST/mapped,(DEST/rel).parent).replace('\\','/');pieces.append(a(link,label))
                else:pieces.append(html.escape(label+'（工程路径：'+target+'）'))
            start=mt.end()
        pieces.append(html.escape(text[start:]));return ''.join(pieces)
    mditems=list(DEST.rglob('*.md'))
    for destmd in mditems:
        rel=destmd.relative_to(DEST);origin=next((src for src,mapped in mapping.items() if mapped==rel),destmd)
        body=[];intable=False;incode=False
        for line in destmd.read_text(encoding='utf8').splitlines():
            if line.startswith('```'):
                if intable:body.append('</table></div>');intable=False
                body.append('</pre>' if incode else '<pre>');incode=not incode;continue
            if incode:body.append(html.escape(line)+'\n');continue
            if intable and not line.startswith('|'):body.append('</table></div>');intable=False
            if line.startswith('|'):
                vals=[x.strip() for x in line.strip('|').split('|')]
                if all(re.fullmatch(r':?-+:?',x) for x in vals):continue
                if not intable:body.append('<div class="scroll"><table>');intable=True
                body.append('<tr>'+''.join('<td>'+inline(x,origin,rel)+'</td>' for x in vals)+'</tr>')
            elif line.startswith('#'):
                n=min(4,len(line)-len(line.lstrip('#')));body.append(f'<h{n}>'+inline(line[n:].strip(),origin,rel)+f'</h{n}>')
            elif line.strip():body.append('<p>'+inline(line,origin,rel)+'</p>')
        if intable:body.append('</table></div>')
        if incode:body.append('</pre>')
        # Handoff already has a specifically laid-out self-contained HTML.
        if not destmd.with_suffix('.html').exists():destmd.with_suffix('.html').write_text(docpage(destmd.stem,''.join(body)),encoding='utf8')
    # Per-category catalogs and the primary entry page use package-relative URLs.
    for folder in [p for p in DEST.iterdir() if p.is_dir()]:
        fs=sorted(p for p in folder.rglob('*') if p.is_file() and p.name!='目录.html')
        body='<p>'+a('../00_先读.html','返回总入口')+'</p><ul>'+''.join('<li>'+a(p.relative_to(folder),str(p.relative_to(folder)))+'</li>' for p in fs)+'</ul>'
        (folder/'目录.html').write_text(docpage(folder.name,body),encoding='utf8')
    body='<section><p><span class="tag">工作站已完成的结果快照</span></p><p>本文件夹用于回原电脑讲解和查阅。后续计算继续留在工作站。</p><ol>'
    for path,label in [
        ('01_交接说明/'+HAND+'.pdf','先读：6页工作站handoff（PDF）'),
        ('01_交接说明/'+HAND+'.html','handoff浏览器版'),
        ('02_报告/铜羽流仿真结果解读与论文对照.pdf','8页结果解读与论文对照（PDF）'),
        ('02_报告/铜羽流仿真结果解读与论文对照.html','结果解读浏览器版（图片已内嵌）'),
        ('02_报告/铜羽流仿真问题分析与方法推导.pdf','20页问题分析与方法推导（PDF）'),
        ('02_报告/铜羽流仿真问题分析与方法推导.html','问题分析浏览器版（图片与公式已内嵌）'),
        ('02_报告/技术与方法文档/最终报告.html','技术最终报告与完整审计结论'),
        ('01_交接说明/给原电脑助手的接手提示词.txt','给原电脑助手的接手提示词')]:body+='<li>'+a(path,label)+'</li>'
    body+='</ol></section><section><h2>结论与讲解顺序</h2><p>四个6.4工况均完整到2.12 μs，守恒审计通过；时间步检验通过，网格综合目标未过。当前热历史与LTE原子发射缺少后期光子，极大相机增益把弱尾部画成饱和亮区。</p><p>按“铜分布 → 温度 → 发光 → 相机”依次理解。历史/中间图件已单列。</p></section><section><h2>文件分类</h2><ul>'
    for folder in sorted(p for p in DEST.iterdir() if p.is_dir()):body+='<li>'+a(folder.name+'/目录.html',folder.name)+'</li>'
    body+='</ul></section><section><h2>代表图与动画</h2>'
    for name,label in [('B64A_S4_comparison.png','基准与论文S4对照'),('B64A_physical_fields.png','真实温度、铜与电子密度场'),('B64A_physical_evolution.gif','真实物理场动画'),('B64A_S4.gif','固定相机响应的对照动画')]:
        path='03_结果图与动画/B64A/'+name;body+='<h3>'+html.escape(label)+'</h3><p>'+a(path,'打开原图/动画')+'</p><img loading="lazy" src="'+quote(path,safe='/')+'" alt="'+html.escape(label)+'">'
    body+='</section><section><h2>模型、溯源与复制</h2><p>五个真实view.mph在06_COMSOL结果模型。原电脑只看报告无需COMSOL；打开MPH需6.4。</p><p>整个文件夹一起复制后再打开本页，所有包内链接使用相对路径。PDF/HTML阅读文件已就绪；关键文件SHA256与来源见清单。</p><ul><li>'+a('文件清单_SHA256.json','文件清单与来源')+'</li><li>'+a('PDF收录与去重清单.json','PDF收录与去重说明')+'</li><li>'+a('汇总包核验.json','复制和链接核验')+'</li></ul></section>'
    generated('00_先读.html',docpage('铜羽流仿真 · 工作站工作汇总',body))
    generated('00_先读.md','# 铜羽流仿真工作汇总\n\n请双击同目录的00_先读.html。\n\n本包包含工作站handoff、所有去重PDF、技术/方法文档、正式图像与动画、CSV/JSON审计、7个代表源文件、五个真实结果模型、原论文及日志。\n\n后续计算仍在原工作站完成；本包用于原电脑阅读和讲解。\n\n工作站完整工程：'+str(R)+'\n')
    # Validate every internal HTML link before finalizing. Do not require workstation paths.
    generated('汇总包核验.json','{}')
    generated('文件清单_SHA256.json','{}')
    missing=[];outside=[];nlinks=0
    for hp in DEST.rglob('*.html'):
        for target in re.findall(r'(?:href|src)="([^"]+)"',hp.read_text(encoding='utf8')):
            if target.startswith(('http:','https:','data:','#','mailto:')):continue
            nlinks+=1;p=(hp.parent/unquote(target.split('#')[0])).resolve()
            if not p.is_relative_to(DEST.resolve()):outside.append([str(hp.relative_to(DEST)),target])
            if not p.is_file():missing.append([str(hp.relative_to(DEST)),target])
    if missing or outside:raise RuntimeError(json.dumps(dict(missing=missing,outside=outside),ensure_ascii=False))
    # Every original PDF must have an explicit disposition.
    assert len(seenpdf)+len(dedup)==len(pdfs)
    copied_pdf=[p for p in DEST.rglob('*.pdf')];pdfhash=[sha(p) for p in copied_pdf];assert len(set(pdfhash))==len(pdfhash)
    # Native copy hashes already match the original delivery manifest, including all five solved models.
    qa=dict(status='passed',source_copy_hashes_verified=len(rows),html_internal_links_checked=nlinks,missing_links=[],outside_package_links=[],native_models=[c+'_view.mph' for c in CASES],native_models_count=5,representative_source_count=7,source_PDFs=len(pdfs),unique_retained_PDFs=len(copied_pdf),omitted_PDFs=len(dedup),handoff_pages=6,main_report_pages=[8,20],new_solver_runs=False,raw_project_untouched=True)
    js(DEST/'汇总包核验.json',qa)
    js(DEST/'PACKAGE_STATE.json',dict(status='complete',source_project=str(R),date_beijing='2026-11-06',purpose='原电脑阅读讲解；计算留在工作站'))
    # Include generated indexes and documentation in the independent package manifest.
    bypath={x['path']:x for x in rows};allfiles=[]
    for dest in sorted(p for p in DEST.rglob('*') if p.is_file() and p.name!='文件清单_SHA256.json'):
        rel=dest.relative_to(DEST).as_posix();entry=bypath.get(rel) or dict(path=rel,bytes=dest.stat().st_size,sha256=sha(dest),source='本次汇总生成',role='说明/索引/核验')
        allfiles.append(entry)
    manifest=dict(date_beijing='2026-11-06',source_project=str(R),portable_relative_links=True,files=allfiles,file_count=len(allfiles),total_bytes=sum(x['bytes'] for x in allfiles),manifest_self_hash_excluded=True)
    js(DEST/'文件清单_SHA256.json',manifest)
    with (DEST/'文件清单.csv').open('w',encoding='utf-8-sig',newline='') as f:
        w=csv.DictWriter(f,fieldnames=['path','bytes','sha256','source','role']);w.writeheader();w.writerows(allfiles)
    # CSV is an additional convenience listing; add its hash without a recursive self-dependency.
    cp=DEST/'文件清单.csv';manifest['files'].append(dict(path=cp.name,bytes=cp.stat().st_size,sha256=sha(cp),source='本次汇总生成',role='便于Excel浏览的清单'))
    manifest['file_count']=len(manifest['files']);manifest['total_bytes']=sum(x['bytes'] for x in manifest['files']);js(DEST/'文件清单_SHA256.json',manifest)
    print(json.dumps(dict(folder=str(DEST),files=manifest['file_count'],GB=manifest['total_bytes']/1e9,PDFs=len(copied_pdf),omitted_PDFs=len(dedup),links_checked=nlinks,models=5,sources=7),ensure_ascii=False,indent=2),flush=True)

if __name__=='__main__':main()
