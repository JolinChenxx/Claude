"""Render every page and check report self-containment and source provenance."""
from pathlib import Path
import json,re
import fitz
from PIL import Image,ImageOps,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/'output/pdf';T=R/'tmp/pdfs/problem_analysis64';T.mkdir(parents=True,exist_ok=True)
name='铜羽流仿真问题分析与方法推导'
meta=json.loads((O/'问题分析报告内容与来源.json').read_text(encoding='utf8'))
doc=fitz.open(O/(name+'.pdf'));checks=[]
for i,page in enumerate(doc,1):
    pix=page.get_pixmap(matrix=fitz.Matrix(1.65,1.65),alpha=False);pix.save(T/f'page_{i:02}.png')
    text=page.get_text()
    checks.append(dict(page=i,has_title=meta['pages'][i-1]['title'] in text,footer_present=f'{i} / {len(doc)}' in text,text_length=len(text),replacement_chars='\ufffd' in text))
for start in range(0,len(doc),4):
    sheet=Image.new('RGB',(1120,1620),'#cbd5df')
    for j in range(start,min(start+4,len(doc))):
        im=Image.open(T/f'page_{j+1:02}.png').convert('RGB');im.thumbnail((550,790));x=(j-start)%2*560+5;y=(j-start)//2*810+18;sheet.paste(im,(x,y));ImageDraw.Draw(sheet).text((x+6,y-14),f'PAGE {j+1}',fill='black')
    sheet.save(T/f'contact_{start//4+1}.png')
html=(O/(name+'.html')).read_text(encoding='utf8');srcs=re.findall(r'<img src="([^"]+)"',html)
figs=[b[1] for p in meta['pages'] for b in p['blocks'] if b[0]=='figure']
result=dict(page_count=len(doc),pages=checks,all_images_embedded=all(s.startswith('data:image/png;base64,') for s in srcs),HTML_images=len(srcs),figures_exist=all((R/'figures'/p).is_file() for p in figs),minimum_content_bottom_pt=min(p['content_bottom_pt'] for p in meta['layout']),pdf_bytes=(O/(name+'.pdf')).stat().st_size,html_bytes=(O/(name+'.html')).stat().st_size,visual_review='pending')
assert result['page_count']==len(meta['pages']) and result['figures_exist'] and result['all_images_embedded']
assert all(c['has_title'] and c['footer_present'] and not c['replacement_chars'] for c in checks)
(O/'问题分析报告自动核验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps(result,ensure_ascii=False,indent=2))
