"""Retrieve primary rate references and preserve exact sources locally."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import ROOT,write_json
import urllib.request,hashlib,concurrent.futures

SOURCES={
 'scd89_cu.dat':'https://open.adas.ac.uk/download/adf11/scd89/scd89_cu.dat',
 'acd89_cu.dat':'https://open.adas.ac.uk/download/adf11/acd89/acd89_cu.dat',
 'copper_excitation_review.pdf':'https://www.osti.gov/servlets/purl/5526164',
 'Cu_Johnson.yml':'https://raw.githubusercontent.com/polyanskiy/refractiveindex.info-database/master/database/data/main/Cu/nk/Johnson.yml',
 'adas_adf11_documentation.pdf':'https://www.adas.ac.uk/man/chap8-11.pdf',
}
def main():
    dest=ROOT/'data'/'references';dest.mkdir(exist_ok=True)
    def get(item):
        name,url=item;p=dest/name
        try:
            req=urllib.request.Request(url,headers={'User-Agent':'Research-plume-model/1.0'})
            data=urllib.request.urlopen(req,timeout=40).read()
            if name.endswith('.pdf') and not data.startswith(b'%PDF'):raise ValueError('Not a PDF response')
            p.write_bytes(data)
            return dict(file=name,url=url,bytes=len(data),sha256=hashlib.sha256(data).hexdigest(),status='downloaded')
        except Exception as e:return dict(file=name,url=url,status='unavailable',error=str(e))
    with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:rows=list(pool.map(get,SOURCES.items()))
    write_json(dest/'source_manifest.json',rows)
    for row in rows:print(row,flush=True)
if __name__=='__main__':main()
