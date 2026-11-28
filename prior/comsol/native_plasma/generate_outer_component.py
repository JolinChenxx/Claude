"""Rebuild a second native component from recorded API calls, with unique tags.

Preparation only; no solving, image fitting or implicit physical adoption.
Unlike component.copy, this preserves an explicit auditable variable namespace.
"""
from pathlib import Path
import re,json
R=Path(__file__).resolve().parent
s=(R/'models/G12_ready.java').read_text()
statements=re.findall(r'(?m)^    model\.component(?:\(\)|\("c"\))[\s\S]*?;',s)
mapping={'g':'og','mesh':'omesh','plas':'plas2','ht':'outerHeat','bulk':'outerBulk','gasv':'outerGas','boundv':'outerBoundary','solidv':'outerSolid','copper':'outerCopper','intSurf':'outerIntSurf','intCu':'outerIntCu'}
mapping.update(surface='outerSurface',beam='outerBeam',bottom='outerBottom')
result=[]
species=[x['name'] for x in json.loads((R/'data/chemistry.json').read_text())['species']]+['CuT5']
outer_names={n:re.sub(r'(_1[pm])$',r'X\1',n) if '_1' in n else n+'X' for n in species}
neutral_names=[n for n in species if '_1' not in n]
for st in statements:
 st=st.replace('component("c")','component("outer")').replace('component().create("c",','component().create("outer",').replace('root.c.','root.outer.')
 for old,new in mapping.items():st=re.sub(r'\b'+old+r'\b',new,st)
 st=st.replace('g_','og_').replace('.omesh(','.mesh(')
 # Native global species fields are declared at model scope (wCu etc.).
 # Separate chemical identifiers are therefore required in the second reactor.
 # Masses, charges, rate coefficients and stoichiometry remain unchanged.
 if '.set("formula"' in st:
  st=re.sub(r'("formula",\s*")([^"]+)(")',lambda m:m[1]+re.sub(r'(?<![A-Za-z])('+ '|'.join(sorted(neutral_names,key=len,reverse=True))+r')(?![A-Za-z0-9])',lambda q:q[0]+'X',m[2])+m[3],st)
 st=re.sub(r'\.feature\("('+ '|'.join(sorted(species,key=len,reverse=True))+r')"\)',lambda m:'.feature("'+outer_names[m[1]]+'")',st)
 st=re.sub(r'w('+ '|'.join(sorted(species,key=len,reverse=True))+r')\b',lambda m:'w'+outer_names[m[1]],st)
 result.append(st)
 if 'physics().create("plas2"' in st:
  result.append('    model.component("outer").physics("plas2").field("logelectronenergydensity").field("outerEn");')
 if 'physics().create("outerHeat"' in st:
  result.append('    model.component("outer").physics("outerHeat").field("temperature").field("outerT");')
# Any explicit references to the native primary field must follow the field name.
body='\n'.join(result);body=re.sub(r'\bEn\b','outerEn',body);body=re.sub(r'\bT\b','outerT',body)
for n,new in outer_names.items():
 for stem in ['w','n_w','M_w']:
  body+='\n    model.component("outer").variable("outerGas").set("plas2.'+stem+n+'", "plas2.'+stem+new+'");'
blocks=[];parts=body.split(';')
for i in range(0,len(parts)-1,150):blocks.append(';'.join(parts[i:min(i+150,len(parts)-1)])+';')
code='import com.comsol.model.*;\n/** Generated recorded API component with separate native field names. */\npublic class AddOuterComponent {\npublic static void apply(Model model){\n'+''.join(f'part{i}(model);\n' for i in range(len(blocks)))+'}\n'
for i,b in enumerate(blocks):code+=f'static void part{i}(Model model){{\n'+b+'\n}\n'
code+='}\n';(R/'src/AddOuterComponent.java').write_text(code)
print('RECORDED_COMPONENT_STATEMENTS',len(statements),'CHUNKS',len(blocks))
