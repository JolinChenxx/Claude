"""A self-contained explanatory report from saved scientific results; no solves."""
from pathlib import Path
import base64,datetime,html,json,re
from zoneinfo import ZoneInfo
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4
from reportlab.lib.colors import HexColor,white
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Paragraph,Table,TableStyle
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.utils import ImageReader

R=Path(__file__).resolve().parents[1]
O=R/'output/pdf';O.mkdir(parents=True,exist_ok=True)
NAME='铜羽流仿真结果解读与论文对照'
def load(p):return json.loads((R/p).read_text(encoding='utf8'))
a=load('results/B64A_audit.json');ca=load('results/B64A_camera/summary.json')
nc=load('results/numerical_comparison.json');ad=load('results/extra_grid_adjacent64.json')
cap=load('results/B64A_photon_upper_bound.json')
date='2026-11-06'  # Client-supplied report date; the execution host clock differs.
pages=[]
def page(title,subtitle):
    blocks=[];pages.append({'title':title,'subtitle':subtitle,'blocks':blocks});return blocks
def p(b,text):b.append(('p',text))
def note(b,text):b.append(('note',text))
def table(b,headers,rows,widths):b.append(('table',headers,rows,widths))
def fig(b,name,caption,maxheight):b.append(('figure',name,caption,maxheight))
def pct(v):return f'{v*100:.4g}%'

b=page('激光作用铜靶：羽流仿真结果报告','阅读版 | COMSOL 6.4 | 结果数据来自本轮已保存解 | '+date)
note(b,'本轮已得到完整的铜羽流流动与热状态计算。时间步检验通过，但网格独立性仍未完全通过；当前LTE发光模型也未复现论文S4的后期发光。')
p(b,'这项工作回答两个相连的问题：激光把多少能量和铜带入空气，形成怎样的羽流；这些温度、密度和铜分布能否产生论文相机中看到的发光。模型已建立这条计算链，本报告将逐步解释它的结果与差距。')
table(b,['输入条件','本轮采用的数值'],[
['激光','1064 nm；60 ns半高全宽；入射能量400 μJ'],
['空间分布','高斯光斑，1/e²半径11 μm'],
['铜表面与环境','表面吸收率0.1；常压空气101325 Pa；初始293.15 K'],
['几何与时间','二维轴对称；气体域半径2 mm、高3 mm；0至2.12 μs'],
['计算设置','基准最大步长0.5 ns；613个保存时刻；相对容差0.002']],[.23,.77])
p(b,'计算链：激光吸收 → 铜靶升温与净蒸发/凝结 → 铜蒸气与空气流动、混合和冷却 → LTE原子发射/吸收 → 沿视线积分与10 ns曝光 → 与论文图像比较。靶面退缩由移动网格（ALE）跟踪。')
table(b,['要判断的内容','本轮结论'],[
['完整时间范围是否算完','四个独立初态工况均到2.12 μs，既定数值审计全部通过'],
['时间步是否足够小','减半到0.25 ns后，S4四项指标变化均低于10%'],
['网格是否足够细','追加一级加密后，S4综合指标仍未通过10%目标'],
['是否复现论文实验','尚未；亮区尺寸、峰值时刻、衰减与光子量级存在明显差距']],[.29,.71])
p(b,'本报告以原网格基准B64A解释主结果，再用其他工况说明可信范围。依据当前检验，不能把任一工况视为已经验证的实验预测。')

b=page('1. 羽流中实际发生了什么','先看物质与温度，再看相机发光')
p(b,'图1从上到下依次是铜质量密度、气体温度、电子数密度和相对环境压力。各行共用固定色标；颜色表示真实物理量，不包含相机增益。')
fig(b,'B64A_physical_fields.png','图1  基准B64A物理场。列标题为相机实验时间0、60、150、260、740 ns；对应数值时间约118、178、268、378、858 ns（最近保存节点）。气体域比图中观察窗口更大。',470)
p(b,'铜物质持续向外扩展，而高温区、电子密度和压强扰动更快减弱。因而“有铜存在的区域”与“能发出可见光的区域”并不相同。LTE中，温度下降会强烈压低电子激发态布居，铜蒸气仍在时，可见原子发射已可能极弱。')
p(b,'LTE（局部热力学平衡）假定局部粒子电离与激发布居由本地温度和密度决定。本轮没有显式计算非平衡激发/复合动力学，也没有颗粒核化、颗粒输运或颗粒热发光。')

b=page('2. 与原论文S4的直接图像对照','上排为论文实验重建图，下排为本次基准合成相机图')
fig(b,'B64A_S4_comparison.png','图2  Chen等论文补充图S4与B64A对照。两排共用11.111 μm/像素和0至255灰度色标；白线为阈值27轮廓。实验缺少独立靶面标记，竖直原点仅为暂定，不能用这张图验证绝对靶面高度。',195)
p(b,'论文实验亮斑先扩大、再减弱并收缩；当前合成图像呈现贴近靶面、持续扩大的饱和亮区，形状和后期衰减均不同。这里展示的是固定相机响应后的结果，不能据其颜色鲜亮推断物理发光已经正确。')
table(b,['特征','论文S4重建','基准合成图'],[
['最大可见半径','322.2 μm；180 ns','455.6 μm；250 ns'],
['最大可见面积','0.3031 mm²；260 ns','0.8099 mm²；580 ns'],
['轮廓内平均灰度峰值','119.5；60 ns','255（饱和）；630 ns'],
['末次可见采样时刻','630 ns','740 ns']],[.34,.33,.33])
p(b,'图像重建使用论文出版JPEG及S2伪彩色条，而非原始CCD数据。400 μm标尺对应36像素；标尺端点±1像素约带来±2.8%的长度尺度变化。图像反演及实验零点的不确定性应与模型误差一起考虑。')
note(b,'结论：当前模型尚未复现S4亮区。加热、蒸发及铜物质扩展的数值结果，与实验发光是否吻合，是需要分别检验的两件事。')
p(b,'图像来源：Chen等，Mechanism of Laser-Induced Self-Deposition of Nanoparticles Identified by In Situ Observation，补充图S2/S4；DOI：10.1002/lpor.202400388。仿真图来自本轮B64A保存解。')

b=page('3. 尺寸和亮度怎样随时间变化','曲线解释图像差异，灰色区域为参与标定的早期帧')
fig(b,'B64A_evolution_metrics.png','图3  论文S4重建指标（蓝色）与固定相机响应的LTE结果（橙色）。上/下发光边缘的绝对原点未独立配准；可比较跨度，不能把绝对高度当成已验证量。',330)
p(b,'实验半径约在180 ns达到峰值、面积约在260 ns达到峰值，随后下降；模拟半径在视场内趋于平台，面积和积分灰度继续上升。亮度峰值的时间与实验也不一致。尺寸曲线的接近程度不能替代发光机制检验。')
table(b,['比较规则','本轮实施与结果'],[
['标定与独立检查','只用0至80 ns的7帧标定；150至740 ns的22帧留出'],
['相机参数','全局增益4.4200×10⁶⁷；背景2；统一时移118 ns'],
['留出可见帧的半径','20帧中仅2帧满足20%或2像素的较宽容差'],
['留出可见帧的亮区高度跨度','20帧中6帧满足所列空间目标']],[.37,.63])
p(b,'实验时间 = 数值时间 - 118 ns。该时移对所有帧及验证工况固定；图中0 ns并非模型初态。主通带475至525 nm，曝光10 ns，空间模糊半高全宽1像素；没有逐帧调节增益来追随实验。')
p(b,'主通带、相机模糊等仍带有实验信息不足造成的不确定性。极大的最佳拟合增益本身不代表真实仪器增益，下一节光子量级检查表明其物理解释存在问题。')

b=page('4. 能量、铜质量与主要物理结果','守恒检查说明数值收支是否闭合，不能单独证明模型符合实验')
fig(b,'B64A_conservation.png','图4  基准的入射/吸收/反射、能量和铜质量收支。虚线与实线接近表示相应守恒账目闭合。下方逐时残差从吸收或净烧蚀超过1%后统计。',315)
table(b,['基准B64A量','计算结果'],[
['入射 / 逃逸反射 / 总吸收','约400 / 360 / 40.02 μJ'],
['靶面吸收 / 羽流双程吸收','约40.00 / 0.0187 μJ'],
['气体峰温 / 电子密度峰值',f'{a["peak_T_K"]:.0f} K / {a["peak_ne_m3"]:.3g} m⁻³'],
['气相铜质量峰值 / 终点气相铜','约1.871 ng / 1.239 ng（域内剩余量）'],
['最大靶面退缩 / 峰值速度',f'{a["max_recession_um"]:.3f} μm / {a["peak_speed_m_s"]:.0f} m/s'],
['终点铜质量 / 能量残差',pct(a['mass_relative_residual'])+' / '+pct(a['energy_relative_residual'])]],[.47,.53])
p(b,'总吸收约为入射的10%，与指定表面吸收率一致；羽流吸收在这个工况中很小。靶面最大退缩约2.05 μm，气相铜峰值约1.87 ng。这些是模型预测量，论文没有在此提供对应的独立直接测量用于验证。')
p(b,'基准全时域最大逐时能量残差为2.279%，终点光学残差0.003912%；原始密度和热内能为正，ALE最小雅可比约0.918。四个工况均通过既定审计。局部铜质量分数存在既定1×10⁻⁴容差内的小负值，未宣称数学上处处非负。')

b=page('5. 网格与时间步：结果稳定到什么程度','四项观测量同时低于10%才判为S4综合通过')
fig(b,'S4_numerical_convergence64.png','图5  从左到右为发光半径、面积、扣背景积分灰度和饱和前物理亮度。虚线为10%目标；蓝色低于目标，橙色超过目标。只在相应参考指标超过其峰值1%的活动帧上比较，并检查新增信号。',225)
table(b,['与参考相比','半径','面积','净灰度','物理亮度'],[
['基准→时间步减半','5.56%','2.91%','2.21%','0.45%'],
['基准→网格×0.75','14.29%','23.91%','20.21%','19.78%'],
['网格×0.75→×0.5625','10.34%','17.50%','17.76%','8.23%']],[.36,.16,.16,.16,.16])
p(b,'基准网格8143个单元，原加密网格14299个，追加网格25009个；最大时间步分别为0.5、0.5、0.5 ns。时间步验证采用原网格及0.25 ns。四个工况都从6.4初态独立计算到2.12 μs，共用同一物理条件与相机参数。')
note(b,'时间步检验通过。网格加密使相邻物理亮度变化降至8.23%，但半径、面积和净灰度仍有指标超过10%，所以整体网格独立性没有通过。原基准与追加网格的物理亮度仍差26.38%。')
p(b,'后处理采样也做了检查：把场导出网格和视线积分间距减半，五个早期活动亮度门宽的变化仅约0.04%至0.11%；原网格与×0.75网格间差异仍约19.77%。这项检查支持早期物理亮度的差异主要来自流体解网格敏感性，范围不延伸到后期极弱尾部。')
p(b,'局部流场更敏感：292 ns时基准峰速度约276 m/s，追加网格约1739 m/s；整个公共时域峰速度曲线的最大差值约为基准全时域峰值的84.73%。该差异已存在于早期保存解，不是后期历史合并引入。')

b=page('6. 为什么颜色很亮，却仍不能解释发光','必须检查进入相机增益之前的光子数量')
fig(b,'B64A_photon_scale.png','图6  相机增益之前的理想光子数（对数色标）。白色轮廓是增益后灰度27阈值；青色表示1个理想光子。即使白色亮区轮廓很大，其覆盖区也可能几乎没有可收集光子。',325)
p(b,'本轮全局增益约4.42×10⁶⁷，足以把极弱的连续均值尾部放大并饱和。因此图2的大块亮区可以在数学图像中形成，却不说明真实相机能够收到足够的光子。理想收集估计取数值孔径NA=0.4、透过率和量子效率均为1；约99.93%的模拟可见掩模像素低于1个理想光子/像素/10 ns。')
table(b,['实验门宽时刻','全域、全方向发射光子上界'],[
['260 ns，10 ns曝光','最多约9.24×10⁻⁶个光子'],
['400 ns，10 ns曝光','最多约6.21×10⁻¹³个光子']],[.4,.6])
p(b,'上述上界对允许的60至120 ns时移取最大值，使用全有限元域380至780 nm的已建模原子谱线与自由-自由连续谱，尚未扣除收集损失与吸收。即使如此，后期光子也远不足以解释论文明亮图像。')
note(b,'结论仅限定本轮LTE原子谱线/自由-自由闭合。它不约束尚未建模的颗粒发光、非平衡过程、分子或其他辐射贡献，不能由此唯一认定是哪一种机制导致实验差异。')

b=page('7. 这次工作得到什么，下一步怎样继续','后续计算以当前共享工作站为固定平台')
table(b,['可用于理解和继续研究','仍需解决'],[
['已有可继续使用的6.4模型、全时域物理场与审计','局部速度与S4观测量的网格敏感性'],
['建立从流体状态到合成相机图的计算链','论文后期发光的物理解释及绝对光子量级'],
['定量揭示铜物质扩展与可见发光的差别','实际滤光片、响应、时间零点和原始实验数据的不确定性']],[.5,.5])
p(b,'建议下一轮先定位250至310 ns附近的速度峰值位置和局部网格/密度状态，评估局部加密与保守稳定项对结果的影响，再重复关键S4门宽的网格对照。守恒通过与可见轮廓接近都不能替代这一步。')
p(b,'随后结合原始实验相机参数和光谱，确定发光机制升级的依据。非平衡电离/激发、复合与颗粒热发光可作为候选方向，但应先确定所需输入和可检验观测量，再选择路线。该报告不把这些候选过程写成已完成计算或已证实原因。')
p(b,'本工作站由课题组三人共用，后续仍在本机计算，无需迁回原电脑。原电脑如仅查看结果，可直接使用本PDF；本报告HTML也内嵌全部静态图片。正式模型、原始解和日志继续留在D盘工程目录。')
table(b,['本机查阅入口','对应文件'],[
['推荐首先打开','output/pdf/'+NAME+'.pdf 或同名.html'],
['基准COMSOL结果视图','work/B64A_view.mph'],
['追加网格结果视图','work/G264R1C5_view.mph'],
['完整报告、原始图与数据入口','交付索引64.html；docs/最终报告.md'],
['可继续计算的实际工况','B64A、G64AC2、T64AC2、G264R1C5']],[.38,.62])
p(b,'完整工程根目录：D:/junlin/PlasmaPlume/COMSOL_CuPlasma_S4_server_handoff_20261105/project。后续计算先记录新一轮可用时长与共享资源情况；本轮16核来自短算实测，不意味着预留整台机器。旧执行窗口已经结束，不能直接把旧协调器当作新的计算任务启动。')
p(b,'数据出处：各工况_audit.json、numerical_comparison.json、extra_grid_adjacent64.json、B64A_camera/summary.json与photon_scale.json、B64A_photon_upper_bound.json、physical_numerical_comparison64.json。实验来源为Chen等论文及补充图S2/S4/S7，DOI：10.1002/lpor.202400388；原子数据来源NIST ASD。')
p(b,'输入说明：原论文表格的22 μm尺寸标注与通量数值存在歧义，本轮明确采用交接模型的11 μm高斯半径。S4图像重建最大面积约0.303 mm²，与补充文字0.0296 mm²存在冲突，本轮没有把冲突值同时拟合。更完整的不确定性与运行证据见技术最终报告。')

pdfmetrics.registerFont(TTFont('YaHei','C:/Windows/Fonts/msyh.ttc',subfontIndex=0))
pdfmetrics.registerFont(TTFont('YaHeiBold','C:/Windows/Fonts/msyhbd.ttc',subfontIndex=0))
W,H=A4;M=42;CW=W-2*M
styles={
 'p':ParagraphStyle('body',fontName='YaHei',fontSize=10,leading=16,textColor=HexColor('#27374a'),wordWrap='CJK'),
 'note':ParagraphStyle('note',fontName='YaHeiBold',fontSize=10,leading=16,textColor=HexColor('#15485d'),wordWrap='CJK'),
 'caption':ParagraphStyle('caption',fontName='YaHei',fontSize=8.2,leading=12,textColor=HexColor('#5a6878'),wordWrap='CJK'),
 'cell':ParagraphStyle('cell',fontName='YaHei',fontSize=8.7,leading=13,wordWrap='CJK',textColor=HexColor('#27374a')),
}
def pdf_text(text):
    escaped=html.escape(str(text))
    trans=str.maketrans('⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻','0123456789+-')
    return re.sub('[⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻]+',lambda m:'<super>'+m.group().translate(trans)+'</super>',escaped)

def paragraph(c,text,y,kind='p',width=CW,x=M):
    q=Paragraph(pdf_text(text),styles[kind]);_,h=q.wrap(width,1000);q.drawOn(c,x,y-h);return y-h

def pdf_report():
    c=canvas.Canvas(str(O/(NAME+'.pdf')),pagesize=A4)
    c.setTitle(NAME);c.setAuthor('铜羽流COMSOL仿真项目')
    layouts=[]
    for number,pg in enumerate(pages,1):
        c.setFillColor(HexColor('#0c5269'));c.rect(0,H-14,W,14,fill=1,stroke=0)
        c.setFont('YaHeiBold',18 if number==1 else 17);c.setFillColor(HexColor('#15354b'));c.drawString(M,H-53,pg['title'])
        y=paragraph(c,pg['subtitle'],H-69,'caption')-17
        for block in pg['blocks']:
            kind=block[0]
            if kind in ['p','note']:
                if kind=='note':
                    q=Paragraph(pdf_text(block[1]),styles['note']);_,h=q.wrap(CW-22,1000)
                    c.setFillColor(HexColor('#eaf3f6'));c.roundRect(M,y-h-18,CW,h+18,5,fill=1,stroke=0)
                    y=paragraph(c,block[1],y-9,'note',CW-22,M+11)-9
                else:y=paragraph(c,block[1],y)-10
            elif kind=='table':
                _,headers,rows,widths=block
                cells=[[Paragraph(pdf_text(x),styles['cell']) for x in row] for row in [headers]+rows]
                t=Table(cells,colWidths=[CW*x for x in widths],hAlign='LEFT')
                t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),HexColor('#dfeef3')),('ROWBACKGROUNDS',(0,1),(-1,-1),[white,HexColor('#f4f7f9')]),('VALIGN',(0,0),(-1,-1),'TOP'),('BOX',(0,0),(-1,-1),.5,HexColor('#c7d4dc')),('INNERGRID',(0,0),(-1,-1),.35,HexColor('#d8e1e6')),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8),('TOPPADDING',(0,0),(-1,-1),6),('BOTTOMPADDING',(0,0),(-1,-1),6)]))
                _,h=t.wrap(CW,1000);t.drawOn(c,M,y-h);y-=h+12
            elif kind=='figure':
                _,name,caption,maxheight=block;im=ImageReader(str(R/'figures'/name));iw,ih=im.getSize();scale=min(CW/iw,maxheight/ih);w,h=iw*scale,ih*scale
                c.drawImage(im,M+(CW-w)/2,y-h,width=w,height=h,mask='auto');y-=h+7;y=paragraph(c,caption,y,'caption')-12
            if y<51:raise RuntimeError(f'Page {number} overflow at {y:.1f}: {kind}')
        c.setStrokeColor(HexColor('#cbd5df'));c.line(M,38,W-M,38)
        c.setFont('YaHei',8);c.setFillColor(HexColor('#657584'));c.drawString(M,25,'铜羽流仿真 · 结果解读与论文对照');c.drawRightString(W-M,25,f'{number} / {len(pages)}')
        layouts.append({'page':number,'title':pg['title'],'content_bottom_pt':y});c.showPage()
    c.save();return layouts

def html_report():
    content=[]
    for pg in pages:
        content.append('<section><h1>'+html.escape(pg['title'])+'</h1><p class="sub">'+html.escape(pg['subtitle'])+'</p>')
        for block in pg['blocks']:
            k=block[0]
            if k in ['p','note']:content.append('<p class="'+k+'">'+html.escape(block[1])+'</p>')
            elif k=='table':
                content.append('<div class="scroll"><table><thead><tr>'+''.join('<th>'+html.escape(x)+'</th>' for x in block[1])+'</tr></thead><tbody>')
                content.extend('<tr>'+''.join('<td>'+html.escape(str(x))+'</td>' for x in row)+'</tr>' for row in block[2]);content.append('</tbody></table></div>')
            elif k=='figure':
                path=R/'figures'/block[1];data=base64.b64encode(path.read_bytes()).decode();content.append('<figure><img src="data:image/png;base64,'+data+'" alt="'+html.escape(block[2])+'"><figcaption>'+html.escape(block[2])+'</figcaption></figure>')
        content.append('</section>')
    style='body{font:16px/1.8 "Microsoft YaHei",sans-serif;color:#26394c;background:#f3f6f8;margin:0}section{max-width:1100px;margin:24px auto;padding:32px 44px;background:white;border-radius:10px}h1{font-size:27px;color:#124c63}p.sub,figcaption{color:#667688;font-size:13px}.note{background:#eaf3f6;padding:14px 18px;border-left:4px solid #13758d;font-weight:bold}table{border-collapse:collapse;width:100%;font-size:14px}td,th{border:1px solid #d4dee5;padding:9px;text-align:left}th{background:#dfeef3}tr:nth-child(even){background:#f4f7f9}figure{margin:22px 0}img{max-width:100%;height:auto;display:block;margin:auto}figcaption{margin-top:8px}.scroll{overflow-x:auto}@media(max-width:650px){section{padding:20px;margin:8px}}@media print{section{break-before:page;margin:0;padding:10px;border-radius:0}}'
    (O/(NAME+'.html')).write_text('<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>'+NAME+'</title><style>'+style+'</style><body>'+''.join(content)+'</body></html>',encoding='utf8')

if __name__=='__main__':
    layout=pdf_report();html_report()
    (O/'报告内容与来源.json').write_text(json.dumps({'created_date_toronto':date,'pages':pages,'layout':layout,'source_root':str(R),'new_solver_runs':False,'all_HTML_images_embedded':True},ensure_ascii=False,indent=2),encoding='utf8')
    print(json.dumps(layout,ensure_ascii=False,indent=2))
