"""Chinese progress PDF from actual saved outputs; running times are distinguished."""
from pathlib import Path
from datetime import datetime,timezone,timedelta
import sys,json,re
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4,landscape
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Paragraph
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.utils import ImageReader
import fitz
R=Path(__file__).resolve().parent
pdfmetrics.registerFont(TTFont('CN','C:/Windows/Fonts/simhei.ttf'))
style=ParagraphStyle('body',fontName='CN',fontSize=12,leading=19,wordWrap='CJK',textColor='#243449')
small=ParagraphStyle('small',parent=style,fontSize=10,leading=15)
W,H=landscape(A4);margin=43
label=sys.argv[1] if len(sys.argv)>1 else datetime.now().strftime('%Y%m%d_%H%M')
out=R/'output/pdf'/f'route1_progress_{label}.pdf';out.parent.mkdir(parents=True,exist_ok=True)
content_path=Path(sys.argv[2]) if len(sys.argv)>2 else R/'night_report_content.json'
content=json.loads(content_path.read_text(encoding='utf-8'))
c=canvas.Canvas(str(out),pagesize=(W,H));c.setTitle('铜羽流与原生等离子体耦合 - 计算进展');c.setAuthor('Route 1 COMSOL study')
page=0
def begin(title):
 global page
 page+=1;c.setFillColorRGB(.06,.14,.23);c.rect(0,H-74,W,74,fill=1,stroke=0);c.setFont('CN',20);c.setFillColorRGB(1,1,1);c.drawString(margin,H-43,title)
 c.setFillColorRGB(.35,.4,.46);c.setFont('CN',9);c.drawString(margin,23,'方法一 | 仅报告实际保存与检查的结果 | 光斑直径22 μm');c.drawRightString(W-margin,23,f'{label} · {page}')
def para(text,y,sty=style):
 text=text.replace('\u2212','-')
 p=Paragraph(text.replace('&','&amp;').replace('<','&lt;').replace('>','&gt;'),sty);w,h=p.wrap(W-2*margin,y-48)
 if y-h<48:
  c.showPage();begin('计算进展（续）');y=H-96;w,h=p.wrap(W-2*margin,y-48)
  if y-h<48:raise ValueError('One paragraph exceeds report page')
 p.drawOn(c,margin,y-h);return y-h-13
begin('本轮计算进展与已有图像')
y=H-96;y=para('生成时间：'+datetime.now(timezone(timedelta(hours=8))).strftime('%Y-%m-%d %H:%M:%S 北京时间'),y,small)
for text in content['findings']:y=para(text,y)
y=para('主要限制：'+content['limitations'],y)
y=para('下一步：'+content['next'],y)
c.showPage();begin('求解覆盖与结果性质');y=H-96
for case,description in content['cases'].items():
 saved=None;details=[]
 status=R/'results'/case/'status.json'
 if status.exists():
  data=json.loads(status.read_text(encoding='utf-8'));saved=data.get('saved_time_ns');details.append('实际保存至'+str(round(saved,3))+' ns')
 for suffix in ['_copper_summary.json','_grid_summary.json','_history_summary.json','_post_complete.json','_stage.json']:
  p=R/'results'/(case+suffix)
  if p.exists():
   data=json.loads(p.read_text(encoding='utf-8'));saved=data.get('saved_end_ns');details.append('已导出保存解至 '+str(round(saved,3))+' ns' if saved is not None else '结果摘要已生成')
 if saved is None:
  p=R/'logs'/f'{case}_solver.log';text=p.read_text(encoding='utf-8',errors='replace') if p.exists() else ''
  rows=re.findall(r'^\s*(\d+)\s+([\d.eE+-]+)\s+([\d.eE+-]+)(?:\s+out)?\s+\d+\s+\d+\s+\d+',text,re.M)
  details.append('日志最近接受步：'+str(round(float(rows[-1][1])*1e9,3))+' ns；尚未以导出结果确认保存范围' if rows else '模型准备或初始化中，暂无已核实的时间覆盖')
 y=para(case+'：'+description+'。'+'；'.join(details)+'。',y)
y=para('铜运动预测图、气相场图、物理辐亮度图和S4相机对照属于不同层级；不能用铜面图替代发光结果。仅完整覆盖10 ns曝光的时间段才生成对应实验帧。',y)
y=para('时间零点和滤光片端点仍有实验信息不确定性。展示中注明采用的模型时间映射，不逐帧调节亮度、半径或位置。',y)
c.showPage()
replaced={item['replaces'] for item in content['figures'] if 'replaces' in item and (R/item['path']).exists()}
for item in content['figures']:
 if item['path'] in replaced:continue
 p=R/item['path']
 if not p.exists():continue
 begin(item['title']);y=H-91;y=para(item['caption'],y,small)
 im=ImageReader(str(p));iw,ih=im.getSize();scale=min((W-2*margin)/iw,(y-53)/ih);dw,dh=iw*scale,ih*scale
 c.drawImage(im,(W-dw)/2,53+(y-53-dh)/2,width=dw,height=dh,mask='auto');c.showPage()
c.save()
doc=fitz.open(out);png=R/'tmp/pdfs'/label;png.mkdir(parents=True,exist_ok=True)
for i,p in enumerate(doc):p.get_pixmap(matrix=fitz.Matrix(1.35,1.35)).save(png/f'page_{i+1}.png')
assert all(p.get_text().strip() for p in doc)
manifest=dict(pdf=str(out),pages=len(doc),render_dir=str(png),generated_beijing=datetime.now(timezone(timedelta(hours=8))).isoformat(),visual_review_pending=True)
(out.with_suffix('.json')).write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print(json.dumps(manifest),flush=True)
