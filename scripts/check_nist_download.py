"""T007 checks: provenance/completeness and the requested Cu spot checks only."""
from pathlib import Path
import csv,datetime,hashlib,io,json,re
from fetch_nist import jobs

ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/'data/atomic/nist'
OUT=ROOT/'results/T007'

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()

def clean(value):
    s=(value or '').strip()
    if s.startswith('="') and s.endswith('"'):s=s[2:-1]
    return s.strip().strip('"')

def table(path,delimiter):
    reader=csv.reader(io.StringIO(path.read_text(encoding='utf-8-sig')),delimiter=delimiter)
    header=None; result=[]
    for fields in reader:
        fields=[clean(x) for x in fields]
        if not any(fields):continue
        # NIST repeats headers when changing vacuum/air wavelength convention.
        if header is None or (delimiter=='\t' and fields[0].startswith('obs_wl_')):
            header=fields
            continue
        row=dict(zip(header,fields));row['_source_line']=str(reader.line_num)
        result.append(row)
    return result

def number(s):
    match=re.search(r'[-+]?\d+(?:\.\d*)?(?:[eE][-+]?\d+)?',s)
    return float(match[0]) if match else None

def main():
    manifest=json.loads((DATA/'manifest.json').read_text());expected=dict(jobs());checks=[];bad=[]
    for name,url in expected.items():
        p=DATA/name;meta=manifest.get(name,{})
        c=dict(file=name,exists=p.is_file(),bytes=p.stat().st_size if p.exists() else 0,
               data_rows=meta.get('data_rows'),suspicious_html_or_error=meta.get('suspicious_html_or_error'),
               url_matches=meta.get('url')==url,retrieved_utc=meta.get('retrieved_utc'),
               sha256_matches=p.is_file() and sha(p)==meta.get('sha256'),
               bytes_matches=p.is_file() and p.stat().st_size==meta.get('bytes'))
        head=p.read_bytes()[:1000].decode('utf-8','replace').lower() if p.exists() else ''
        c['html_in_first_1000_bytes']='<html' in head or '<!doctype html' in head
        c['passed']=bool(c['exists'] and c['bytes']>0 and (c['data_rows'] or 0)>0
                         and c['suspicious_html_or_error'] is False and not c['html_in_first_1000_bytes']
                         and c['url_matches'] and c['retrieved_utc'] and c['sha256_matches'] and c['bytes_matches'])
        if not c['passed']:
            bad.append(dict(file=name,first_1000_characters=p.read_text(encoding='utf-8',errors='replace')[:1000] if p.exists() else 'MISSING'))
        checks.append(c)
    level_hits=[];line_hits=[]
    if (DATA/'Cu_I_levels.csv').is_file():
        for row in table(DATA/'Cu_I_levels.csv',','):
            if row.get('Configuration')=='3d9.4s2' and row.get('Term')=='2D' and row.get('J')=='5/2':
                level_hits.append(row)
    if (DATA/'Cu_I_lines.tsv').is_file():
        rows=table(DATA/'Cu_I_lines.tsv','\t')
        for target in [510.55,515.32,521.82]:
            candidates=[]
            for row in rows:
                for key,value in row.items():
                    if 'wl' in key.lower() and '(nm)' in key.lower():
                        wavelength=number(value)
                        if wavelength is not None and abs(wavelength-target)<.05:
                            candidates.append((abs(wavelength-target),key,wavelength,row))
            if candidates:
                _,key,wavelength,row=min(candidates,key=lambda item:item[0])
                aki_key=next((k for k in row if k.startswith('Aki')),None)
                line_hits.append(dict(target_air_nm=target,wavelength_column=key,wavelength_nm=wavelength,
                                      Aki_column=aki_key,Aki_s_inverse=number(row.get(aki_key,'')),raw_cleaned_row=row))
            else:line_hits.append(dict(target_air_nm=target,found=False))
    line_coverage=[]
    for name in expected:
        if not name.endswith('_lines.tsv') or not (DATA/name).exists():continue
        records=table(DATA/name,'\t')
        line_coverage.append(dict(file=name,parsed_rows=len(records),
                                  rows_with_Aki=sum(number(r.get('Aki(s^-1)','')) is not None for r in records),
                                  rows_with_both_energies=sum(number(r.get('Ei(eV)','')) is not None and number(r.get('Ek(eV)','')) is not None for r in records),
                                  wavelength_headers=sorted({k for r in records for k in r if k.startswith('obs_wl_')})))
    level_pass=any(abs((number(r.get('Level (eV)','')) or 0)-1.389)<.001 for r in level_hits)
    lines_pass=len(line_hits)==3 and all(x.get('Aki_s_inverse') is not None and 'air' in x.get('wavelength_column','').lower() for x in line_hits)
    result=dict(task='T007',checked_utc=datetime.datetime.now(datetime.timezone.utc).isoformat(),
                expected_files=len(expected),manifest_entries=len(manifest),total_download_bytes=sum(c['bytes'] for c in checks),
                all_downloads_passed=all(c['passed'] for c in checks),files_over_20_MiB=[c['file'] for c in checks if c['bytes']>20*1024**2],
                file_checks=checks,suspicious_response_excerpts=bad,
                line_table_coverage=line_coverage,
                copper_level_spot_check=dict(passed=level_pass,matching_rows=level_hits),
                copper_lines_spot_check=dict(passed=lines_pass,lines=line_hits),
                fetch_script_sha256=sha(ROOT/'scripts/fetch_nist.py'),
                passed=all(c['passed'] for c in checks) and level_pass and lines_pass)
    OUT.mkdir(parents=True,exist_ok=True)
    (OUT/'validation.json').write_text(json.dumps(result,indent=2,ensure_ascii=False),encoding='utf-8')
    print(json.dumps({k:v for k,v in result.items() if k not in ['file_checks']},indent=2,ensure_ascii=False))
    if not result['passed']:raise SystemExit(1)

if __name__=='__main__':main()
