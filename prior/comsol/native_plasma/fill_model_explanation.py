"""Append exact model data to the authored explanation, without guessing defaults."""
from pathlib import Path
import re,json,csv,hashlib
R=Path(__file__).resolve().parent;D=R/'delivery/model_explanation_20261110'
p=D/'COMSOL数学建模与实现详解.md';doc=p.read_text(encoding='utf-8')
raw=(D/'M121_full_settings.txt').read_text(encoding='utf-8');lines=raw.splitlines()
def cell(s):return s.replace('|','&#124;').replace('\n',' ')
def table(headers,rows):return '\n'.join(['| '+' | '.join(headers)+' |','|'+'|'.join(['---']*len(headers))+'|']+['| '+' | '.join(cell(str(s)) for s in row)+' |' for row in rows])
params=[x.split('\t')[1:] for x in lines if x.startswith('PARAM\t')]
params_text=table(['参数名','实际表达式'],[(f'`{n}`',f'`{v}`') for n,v in params])
params_text+='\n\n此表来自原完整模型快照。交付副本仅将` tEnd `与时间求解列表改为89–95 ns续算，并增加`resume89`备份和结果图组。'
sources={r['name']:r for r in csv.DictReader((R/'data/reaction_source_table.csv').open(encoding='utf-8-sig'))}
blocks={}
for b in re.split(r'(?=^NODE )',raw,flags=re.M):
 h=b.splitlines()[0]
 m=re.match(r'NODE FEATURE (\S+) (\S+)',h)
 if m:blocks[m.group(1)]=(m.group(2),b)
def prop(b,key):
 m=re.search(r'^'+re.escape(key)+r'=\[\[(.*?)\]\](?: ALLOWED=.*)?$',b,re.M)
 return m.group(1) if m else ''
rx=[]
for path,(kind,b) in blocks.items():
 if not path.startswith('c/plas/') or kind not in ('Reaction','ElectronImpactReaction'):continue
 n=path.split('/')[-1];form=prop(b,'formula');kf=prop(b,'kf')
 if not form:continue
 ty=prop(b,'type');de=prop(b,'de') if ty=='Excitation' else '-'
 rx.append([f'`{n}`',f'`{form}`',ty or kind,f'`{kf}`',de,sources.get(n,{}).get('source','模型节点；查对应构建脚本')])
rx_text=f'共提取{len(rx)}个实际反应节点。`kf`已包含COMSOL所需的摩尔单位换算。\n\n'+table(['节点','反应式','类型','COMSOL kf','非弹性能损/eV','来源'],rx)
chosen={
 'c/flow/gfeq1':['f','Ga','da','ea'], 'c/flow/feed':['g'],
 'c/flow/farbc':['r','useDirichletCondition'], 'c/temp/gfeq1':['f','Ga','da'],
 'c/plas/power':['Qgen'], 'c/plas/bulkconv':['Re'],
 'c/plas/feed':['specNames','influx'],
 'c/plas/conservativeElectronFlux':['weakExpression'],
 'c/plas/wallConv':['weakExpression'], 'c/plas/electronFarConv':['weakExpression'],
 'c/plas/metalReactionCorrection':['weakExpression'],
 'c/melt/drag':['F'], 'c/melt/surf':['Fbnd'], 'c/melt/capillary':['weakExpression'],
 'c/ht/fluid1':['u'], 'c/ht/advection':['Q0'], 'c/ht/laser':['q0_input'],
 'c/ld/gfeq1':['f','Ga','da'], 'c/lu/gfeq1':['f','Ga','da'],
 'c/surfaceODE/dode1':['f','da'], 'c/charge/ge1':['name','equation']}
settings=[]
for path,keys in chosen.items():
 kind,b=blocks[path];out=[]
 for key in keys:
  for x in b.splitlines():
   if x.startswith(key+'='):out.append(x.split(' ALLOWED=')[0])
 settings.append('### '+path+'\n\n```text\n'+'\n'.join(out)+'\n```')
settings.append('### 金属接触的Equation View覆盖\n\n`plas.I_1`采用随移动线元的位移电荷变化与粒子电流之和：\n\n```text\n-plas.metal.int(d(V_lm,TIME)+V_lm*lineAreaRate+2*pi*r*plas.d*Iwall)\n```\n\n`qmetal`作为独立积分账，电势耦合主要通过金属接触的弱约束完成。')
groups={}
for x in lines:
 if x.startswith('VAR\t'):
  _,g,n,v=x.split('\t',3);groups.setdefault(g,[]).append((n,v))
index=[]
for g,vals in groups.items():
 index.append('### '+g+'\n\n'+str(len(vals))+'个变量：\n\n'+', '.join('`'+n+'`' for n,v in vals)+'。')
for marker,value in [('AUTO_PARAMETERS',params_text),('AUTO_REACTIONS',rx_text),('AUTO_INTERFACE_SETTINGS','\n\n'.join(settings)),('AUTO_VARIABLE_INDEX','\n\n'.join(index))]:
 token='<!-- '+marker+' -->';assert doc.count(token)==1,marker;doc=doc.replace(token,value)
p.write_text(doc,encoding='utf-8')
assert not re.search(r'<!-- AUTO_',doc)
assert doc.count('```')%2==0
assert doc.count('\\[')==doc.count('\\]')
missing=[]
for target in re.findall(r'\]\((D:/[^)]+)\)',doc):
 if not Path(target).exists():missing.append(target)
stats=dict(characters=len(doc),lines=len(doc.splitlines()),reaction_nodes=len(rx),variable_count=sum(map(len,groups.values())),missing_links=missing)
(D/'document_validation.json').write_text(json.dumps(stats,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(stats,ensure_ascii=False))
