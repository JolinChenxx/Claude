"""Stream the completed rank-zero log, preserving counts and spaced context samples."""
from pathlib import Path
from collections import Counter, deque
import csv
import hashlib
import json
import re

src = Path('D:/sim_runs/T015/high_thin_G0/it0/pluto.0.log')
out = Path(__file__).resolve().parent
expected = int((out.parent/'high_thin_G0/it0/consToPrim_count.txt').read_text())
targets = {round(k*(expected-1)/39)+1 for k in range(40)}
numbers = re.compile(rb'(?<![A-Za-z_])[-+]?(?:\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?')
location = re.compile(rb'\[i,j\s*=\s*(-?\d+),\s*(-?\d+)\]')
step_re = re.compile(rb'@step\s*=\s*(\d+)')
types, ih, jh, cells = Counter(), Counter(), Counter(), Counter()
before, pending, samples = deque(maxlen=2), [], []
count = located = 0
first = last = None
digest = hashlib.sha256()
with src.open('rb') as stream:
    for line_no, line in enumerate(stream,1):
        digest.update(line)
        for sample in pending[:]:
            sample['after'].append({'line':line_no,'text':line.decode('utf-8',errors='replace').rstrip('\r\n')})
            if len(sample['after'])==2: pending.remove(sample)
        if b'ConsToPrim' in line:
            count += 1
            # Message ends before the per-event metadata. Strip numeric values only in that message.
            message=line.split(b'@step',1)[0].strip().rstrip(b';').strip()
            types[numbers.sub(b'<num>',message).decode('utf-8',errors='replace')] += 1
            m=location.search(line)
            if m:
                i,j=map(int,m.groups());ih[i]+=1;jh[j]+=1;cells[(i,j)]+=1;located+=1
            step=step_re.search(line)
            event={'line':line_no,'warning_ordinal':count,'step':int(step.group(1)) if step else None,
                   'i':int(m.group(1)) if m else None,'j':int(m.group(2)) if m else None,
                   'text':line.decode('utf-8',errors='replace').rstrip('\r\n')}
            if first is None:first=event
            last=event
            if count in targets:
                sample={**event,'before':list(before),'after':[]};samples.append(sample);pending.append(sample)
        before.append({'line':line_no,'text':line.decode('utf-8',errors='replace').rstrip('\r\n')})
assert count==expected and len(samples)==40
assert all(len(s['before'])==2 and len(s['after'])==2 for s in samples)
for name,header,rows in [
    ('warning_types.csv',['normalized_message','count'],sorted(types.items())),
    ('i_histogram.csv',['i','count'],sorted(ih.items())),
    ('j_histogram.csv',['j','count'],sorted(jh.items())),
    ('ij_histogram.csv',['i','j','count'],[(i,j,n) for (i,j),n in sorted(cells.items())])]:
    with (out/name).open('w',newline='',encoding='utf-8') as f:
        writer=csv.writer(f);writer.writerow(header);writer.writerows(rows)
(out/'samples.json').write_text(json.dumps(samples,indent=2)+'\n',encoding='utf-8')
with (out/'samples.log').open('w',encoding='utf-8') as f:
    for s in samples:
        f.write(f"=== warning {s['warning_ordinal']} of {count}, line {s['line']} ===\n")
        for row in s['before']+[s]+s['after']:f.write(f"{row['line']}: {row['text']}\n")
summary={'source':str(src),'source_bytes':src.stat().st_size,'source_sha256':digest.hexdigest(),
         'warning_lines':count,'located_warning_lines':located,'total_log_lines':line_no,
         'distinct_normalized_messages':len(types),'first_warning':first,'last_warning':last,
         'sample_count':40,'sampling':'40 evenly spaced warning ordinals including first and last; two log lines before and after each',
         'location_scope':'rank-zero log indices as printed; no conversion to global coordinates',
         'time_scope':'first/last exact warning steps are recorded; simulation times are not inferred'}
(out/'summary.json').write_text(json.dumps(summary,indent=2)+'\n',encoding='utf-8')
print(json.dumps(summary,indent=2))
print('types',dict(types));print('j histogram',dict(jh));print('i histogram',dict(ih))
