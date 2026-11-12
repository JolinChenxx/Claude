"""Build a Windows Markdown index and a browser-readable local image index."""
from pathlib import Path
import html
import re
from urllib.parse import quote, unquote

ROOT = Path(__file__).resolve().parents[1]
LINK = re.compile(r'\[([^\]]+)\]\(<?([^)>]+)>?\)')

def local_target(target):
    target = unquote(target)
    path = Path(target)
    return path.resolve() if path.is_absolute() else (ROOT / path).resolve()

def write_indexes(source):
    # Resolve against the real index directory, never the viewer's working folder.
    links = []
    def markdown_link(match):
        label, target = match.groups()
        path = local_target(target)
        if not path.is_file():
            raise FileNotFoundError(path)
        links.append((label, path))
        return f'[{label}](<{path.as_posix()}>)'
    markdown = LINK.sub(markdown_link, source)
    intro = '[浏览器图片预览版](<'+(ROOT/'交付索引64.html').as_posix()+'>)：若Markdown阅读器限制本地链接，请直接双击同目录HTML文件。\n\n'
    markdown = markdown.replace('\n\n', '\n\n'+intro, 1)
    (ROOT/'交付索引64.md').write_text(markdown, encoding='utf8')

    def inline(value):
        parts=[];start=0
        for match in LINK.finditer(value):
            parts.append(html.escape(value[start:match.start()]))
            label,target=match.groups();path=local_target(target)
            url=quote(path.relative_to(ROOT).as_posix(),safe='/')
            parts.append(f'<a href="{url}">{html.escape(label)}</a>')
            start=match.end()
        parts.append(html.escape(value[start:]))
        return ''.join(parts)

    body=[];table=False;listing=False
    for line in source.splitlines():
        if table and not line.startswith('|'):
            body.append('</tbody></table></div>');table=False
        if listing and not line.startswith('- '):
            body.append('</ul>');listing=False
        if line.startswith('|'):
            cells=[x.strip() for x in line.strip('|').split('|')]
            if all(re.fullmatch(r':?-+:?',x) for x in cells):continue
            if not table:
                body.append('<div class="table"><table><thead><tr>'+''.join('<th>'+inline(c)+'</th>' for c in cells)+'</tr></thead><tbody>');table=True
            else:body.append('<tr>'+''.join('<td>'+inline(c)+'</td>' for c in cells)+'</tr>')
        elif line.startswith('# '):body.append('<h1>'+html.escape(line[2:])+'</h1>')
        elif line.startswith('- '):
            if not listing:body.append('<ul>');listing=True
            body.append('<li>'+inline(line[2:])+'</li>')
        elif line.strip():body.append('<p>'+inline(line)+'</p>')
    if table:body.append('</tbody></table></div>')
    if listing:body.append('</ul>')
    body.append('<h2>本地图片与动画预览</h2><p>点击图片可打开原图；动画在本页直接播放。</p>')
    seen=set()
    for label,path in links:
        if path.suffix.lower() not in {'.png','.gif','.jpg','.jpeg'} or path in seen:continue
        seen.add(path);url=quote(path.relative_to(ROOT).as_posix(),safe='/')
        body.append(f'<figure><figcaption>{html.escape(label)} · {html.escape(path.name)}</figcaption><a href="{url}"><img src="{url}" alt="{html.escape(label)}" loading="lazy"></a></figure>')
    page='''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>COMSOL 6.4 交付索引</title><style>
body{font:16px/1.65 "Microsoft YaHei",sans-serif;max-width:1250px;margin:36px auto;padding:0 24px;color:#203044;background:#f8fafc}a{color:#0868b2}h1{font-size:28px}h2{margin-top:40px}table{border-collapse:collapse;width:100%;background:white}th,td{border:1px solid #ccd6df;padding:10px;text-align:left}.table{overflow-x:auto}figure{background:white;margin:24px 0;padding:16px;border:1px solid #d5dfe7;border-radius:8px}figcaption{font-weight:bold;margin-bottom:12px}img{display:block;max-width:100%;height:auto;margin:auto}</style><body>'''+''.join(body)+'</body></html>'
    (ROOT/'交付索引64.html').write_text(page,encoding='utf8')
    return len(links),len(seen)

if __name__=='__main__':
    source=(ROOT/'交付索引64.md').read_text(encoding='utf8')
    # Remove the generated viewer note on repeated runs.
    source=re.sub(r'\[浏览器图片预览版\].*?\n\n','',source)
    print('LOCAL_INDEX',write_indexes(source))
