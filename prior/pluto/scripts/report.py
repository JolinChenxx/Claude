"""Template-only local reports. No API calls or language-model dependencies."""
import argparse,datetime as dt,json,os,shutil,traceback
from pathlib import Path
from common import ROOT,TZ,utcnow,campaign,read_json,atomic_json

def result_summary(best):
    """Measured discrepancies, not a claim of successful reproduction."""
    if not best:return '尚无完成结果。'
    import numpy as np
    from extract_experiment import measurements
    folder=ROOT/'runs'/best['run_id']
    exp=np.load(ROOT/'data/experimental/s4_frames.npz')
    syn=np.load(folder/'synthetic_frames.npz')
    a=[measurements(x,float(exp['pixel_um'])) for x in exp['gray']]
    b=[measurements(x,float(exp['pixel_um'])) for x in syn['gray']]
    radius=float(np.median([y['radius_um']-x['radius_um'] for x,y in zip(a,b)]))
    observed=exp['gray'].sum(axis=(1,2));predicted=syn['gray'].sum(axis=(1,2))
    late=exp['times_ns']>=400
    ratio=float(predicted[late].sum()/max(observed[late].sum(),1))
    return (f"29 帧可见半径的中位偏差 {radius:+.1f} 微米；400 ns 后总灰度模型/实验比 {ratio:.2f}。"
            f"总灰度峰值时刻：模型 {int(exp['times_ns'][predicted.argmax()])} ns，实验 {int(exp['times_ns'][observed.argmax()])} ns。")

def snapshot():
    c=campaign();s=read_json(ROOT/'state/status.json')
    started=dt.datetime.fromisoformat(c['started_at']);end=dt.datetime.fromisoformat(c['deadline'])
    now=utcnow();s.update(now=now.astimezone(TZ).isoformat(),
        elapsed_h=max(0,(now-started).total_seconds()/3600),
        remaining_h=max(0,(end-now).total_seconds()/3600))
    s.setdefault('phase','环境与模型搭建');s.setdefault('completed',0);s.setdefault('failed',0)
    s.setdefault('running',[]);s.setdefault('next_action','完成首个 PLUTO 算例及合成成像')
    return s

def hourly():
    s=snapshot();now=utcnow().astimezone(TZ)
    best=s.get('best',{}) or {}
    bestdir=ROOT/'runs'/best.get('run_id','none')
    resultfile=bestdir/'result.json'
    result_time=dt.datetime.fromtimestamp(resultfile.stat().st_mtime,TZ).strftime('%m-%d %H:%M') if resultfile.exists() else '暂无'
    text=(f"{now:%Y-%m-%d %H:%M} 北京时间｜已运行 {s['elapsed_h']:.1f} h，剩余 {s['remaining_h']:.1f} h。\n"
          f"阶段：{s['phase']}。完成 {s['completed']} 例，运行 {len(s['running'])} 例，失败 {s['failed']} 例。\n")
    if best:text+=f"当前最优：{best.get('run_id','')}（{result_time}），损失 {best.get('score',float('nan')):.4g}；图见 figures/best_comparison.png。\n"
    else:text+='暂无完成标定的模型；实验基准图见 figures/experimental_s4.png。\n'
    text+=f"下一步：{s['next_action']}。"
    if s.get('attention'):text+='\n注意：'+str(s['attention'])[:180]
    p=ROOT/'reports/hourly'/f'{now:%Y-%m-%d_%H00}.txt';p.parent.mkdir(exist_ok=True,parents=True)
    p.write_text(text,encoding='utf-8');(ROOT/'reports/latest.txt').write_text(text,encoding='utf-8')
    atomic_json(ROOT/'reports/latest_status.json',s)
    print(text)

def pdf_report(kind='daily'):
    from reportlab.pdfgen import canvas
    from reportlab.lib.pagesizes import A4
    from reportlab.pdfbase import pdfmetrics
    from reportlab.pdfbase.cidfonts import UnicodeCIDFont
    from reportlab.lib.utils import ImageReader
    import pymupdf
    s=snapshot();now=utcnow().astimezone(TZ)
    out=ROOT/'reports'/('daily' if kind=='daily' else '')
    out.mkdir(exist_ok=True,parents=True)
    target=out/(f'{now:%Y-%m-%d}_0700.pdf' if kind=='daily' else f'{kind}_report.pdf')
    tmp=target.with_suffix('.building.pdf')
    pdfmetrics.registerFont(UnicodeCIDFont('STSong-Light'))
    cv=canvas.Canvas(str(tmp),pagesize=A4);W,H=A4
    cv.setTitle('S4 plume simulation - '+kind)
    page_no=0
    def page(title):
        nonlocal page_no
        if page_no:cv.showPage()
        page_no+=1
        cv.setFillColorRGB(.1,.18,.29);cv.rect(0,H-72,W,72,fill=1,stroke=0)
        cv.setFillColorRGB(1,1,1);cv.setFont('STSong-Light',19);cv.drawString(36,H-42,title)
        cv.setFillColorRGB(.25,.28,.33);cv.setFont('STSong-Light',9)
        cv.drawString(36,22,f'{now:%Y-%m-%d %H:%M} 北京时间 | 模板自动生成，无语言模型调用')
        cv.drawRightString(W-36,22,str(page_no))
    def lines(items,y=H-100,size=11):
        cv.setFillColorRGB(.12,.14,.18);cv.setFont('STSong-Light',size)
        for item in items:
            # Chinese font width ~font size; deterministic line wrap.
            row=''
            for ch in str(item):
                if pdfmetrics.stringWidth(row+ch,'STSong-Light',size)>W-76:
                    cv.drawString(38,y,row);y-=size*1.6;row=''
                row+=ch
            if row:cv.drawString(38,y,row);y-=size*1.6
            y-=6
        return y
    def figure(path,top=H-94,bottom=52):
        path=Path(path)
        if not path.exists():lines(['本阶段尚无此项计算图。'],top);return
        im=ImageReader(str(path));iw,ih=im.getSize();scale=min((W-66)/iw,(top-bottom)/ih)
        w,h=iw*scale,ih*scale;cv.drawImage(im,(W-w)/2,top-h,width=w,height=h)
    best=s.get('best',{}) or {}
    bestdir=ROOT/'runs'/best.get('run_id','none')
    resultfile=bestdir/'result.json'
    result_time=dt.datetime.fromtimestamp(resultfile.stat().st_mtime,TZ).strftime('%Y-%m-%d %H:%M') if resultfile.exists() else '暂无'
    previous=read_json(ROOT/'state/last_pdf.json')
    previous_score=previous.get('score')
    improvement=(f"较上一份报告损失变化 {best.get('score',0)-previous_score:+.5f}（负值表示改善）。"
                 if previous_score is not None and best else '首次报告，后续记录与上一阶段的变化。')
    page('S4 羽流复现 / 阶段进展')
    lines([f"已运行 {s['elapsed_h']:.2f} 小时；剩余 {s['remaining_h']:.2f} 小时。",
           '阶段：'+s['phase'],f"完成 {s['completed']} 例；运行 {len(s['running'])} 例；失败 {s['failed']} 例。",
           '最佳算例：'+str(best.get('run_id','暂无已完成算例')),
           '结果完成时间：'+result_time+'；综合损失：'+str(best.get('score','待计算')),
           '可见区域中位 IoU：'+str(round(best.get('median_iou_visible',0),3))+'；质量/能量残差：'+str(round(best.get('mass_residual',0),5))+' / '+str(round(best.get('energy_residual',0),5)),
           improvement,
           '当前差异：'+result_summary(best),
           '下一阶段：'+s['next_action'],
           '结果级别：'+s.get('result_level','实验基准已提取；计算模型正在搭建。'),
           '模型说明：二维轴对称 PLUTO 守恒流动；有效烧蚀源；铜/一价铜离子简化动力学；视线辐射与门控成像。',
           '适用限制：单温、连续介质近似；空气暂用中性分子气体；铜谱线采用简化激发与吸收闭合。尚不能据此证明唯一机制。',
           '数据限制：实验来自 PDF 压缩伪彩图；恢复灰度为近似值，不是原始 CCD 数据。',
           '异常提示：'+str(s.get('attention') or '无需要人工处理的已知异常。')])
    page('实验与当前最佳模型')
    fig=bestdir/'comparison.png'
    if not fig.exists():fig=ROOT/'figures/experimental_s4.png'
    figure(fig)
    page('尺寸与亮度演化')
    fig=bestdir/'metrics.png'
    if not fig.exists():fig=ROOT/'figures/experimental_metrics.png'
    figure(fig,top=H-100,bottom=170)
    lines(['统一空间标尺和全序列成像设置。PDF 原图约 10.81 微米/像素。',
           '曲线反映可见发光区域；不等同于全部铜物质的外边界。',
           '当前最优记录保留计算参数、运行日志、物理预算和图像文件。'],y=145,size=10)
    page('物理预算与后续动作')
    if (bestdir/'budget.png').exists():figure(bestdir/'budget.png',bottom=230)
    else:lines(['首个算例完成后，此页加入质量、能量及辐射损失曲线。'],y=H-110)
    lines(['下一步：'+s['next_action'],
           '运行目录：'+str(ROOT/'runs'),
           '每小时文字：reports/hourly；最新摘要：reports/latest.txt。',
           '生成时间：'+s['now']],y=200,size=10)
    cv.save()
    # Render every report; preserve preview for visual inspection without model calls.
    with pymupdf.open(tmp) as doc:
        preview=ROOT/'reports/previews'/target.stem;preview.mkdir(parents=True,exist_ok=True)
        for i,p in enumerate(doc):
            if not p.get_text().strip():raise RuntimeError('Empty report page')
            p.get_pixmap(matrix=pymupdf.Matrix(1,1)).save(preview/f'page_{i+1}.png')
        if len(doc)!=4:raise RuntimeError('Unexpected page count')
    os.replace(tmp,target)
    shutil.copy2(target,ROOT/'reports/latest.pdf')
    atomic_json(ROOT/'state/last_pdf.json',{'path':str(target),'created_at':now.isoformat(),'pages':page_no,
        'run_id':best.get('run_id'),'score':best.get('score')})
    if kind=='final':
        (ROOT/'reports/final_summary.txt').write_text(
            'S4 72 小时计算总结\n'+f"完成 {s['completed']} 个算例；最终候选 {best.get('run_id','无')}。\n"+
            result_summary(best)+'\n'+
            '结果属于有效烧蚀源和简化铜谱线闭合下的形貌标定；完整等离子体物理和唯一形成机制尚未验证。\n'+
            '网格依赖见 refinement_comparison.json；带宽对比见 imaging_sensitivity.json；模型范围见 ../MODEL.md。\n',encoding='utf-8')
    print(target)

if __name__=='__main__':
    ap=argparse.ArgumentParser();ap.add_argument('kind',choices=['hourly','daily','initial','final']);args=ap.parse_args()
    try:
        if args.kind=='hourly':hourly()
        else:pdf_report(args.kind)
    except Exception:
        with (ROOT/'logs/report_errors.log').open('a',encoding='utf-8') as f:
            f.write(utcnow().isoformat()+'\n'+traceback.format_exc())
        raise

