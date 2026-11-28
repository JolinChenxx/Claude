import markdown, pathlib
d=pathlib.Path(__file__).parent
md=(d/"report.md").read_text(encoding="utf-8")
body=markdown.markdown(md,extensions=["tables","fenced_code","sane_lists"])
css="""body{font-family:'WenQuanYi Zen Hei','Noto Sans CJK SC',sans-serif;max-width:980px;margin:2em auto;padding:0 1em;line-height:1.7;font-size:14px;color:#222}
h1{font-size:24px}h2{font-size:20px;border-bottom:2px solid #ddd;padding-bottom:4px;margin-top:1.8em}h3{font-size:16px}
table{border-collapse:collapse;margin:1em 0;font-size:13px}th,td{border:1px solid #bbb;padding:4px 8px}th{background:#f0f0f0}
img{max-width:100%;display:block;margin:1em auto}blockquote{background:#fff4e0;border-left:4px solid #e0a030;margin:1em 0;padding:.5em 1em}
code{background:#f3f3f3;padding:0 3px}h3,img,table{page-break-inside:avoid}"""
(d/"report.html").write_text(f"<!doctype html><html lang='zh'><head><meta charset='utf-8'><title>仿真进展汇报 1126</title><style>{css}</style></head><body>{body}</body></html>",encoding="utf-8")
