"""Recorded native Cu0-Cu4+ component factory; unique chemical names per cell."""
from pathlib import Path
import re,json
R=Path(__file__).resolve().parent
s=(R/'models/G46_ready.java').read_text()
statements=re.findall(r'(?m)^    model\.component(?:\(\)|\("c"\))[\s\S]*?;',s)
species=[x['name'] for x in json.loads((R/'data/chemistry.json').read_text())['species']]+['CuT5','Cu_2p','Cu_3p','Cu_4p']
neutral=[n for n in species if '_' not in n]
result=[]
for st in statements:
    if 'component().create(' in st and 'component().create("c",' not in st:continue
    # Retain recorded physics and material definitions; remove geometry mesh
    # refinement, because all surviving native variables are global unknowns.
    if '.mesh(' in st or '.mesh().' in st:continue
    st=st.replace('component("c")','component(cell)').replace('component().create("c",','component().create(cell,')
    st=st.replace('root.c.','root.CELLTOKEN.')
    for tag in ['g','gasv','boundv','solidv','surface','beam','bottom','intSurf','intCu','copper','bulk','ht']:
        st=re.sub(r'\b'+tag+r'\b',tag+'SUFFIXTOKEN',st)
    st=st.replace('g_','gSUFFIXTOKEN_')
    if '.set("formula"' in st:
        st=re.sub(r'("formula",\s*")([^"]+)(")',lambda m:m[1]+re.sub(r'(?<![A-Za-z])('+ '|'.join(sorted(neutral,key=len,reverse=True))+r')(?![A-Za-z0-9])',lambda q:q[0]+'SUFFIXTOKEN',m[2])+m[3],st)
    def nn(n):return re.sub(r'(_[1-4][pm])$',r'SUFFIXTOKEN\1',n) if '_' in n else n+'SUFFIXTOKEN'
    st=re.sub(r'\.feature\("('+ '|'.join(sorted(species,key=len,reverse=True))+r')"\)',lambda m:'.feature("'+nn(m[1])+'")',st)
    st=re.sub(r'w('+ '|'.join(sorted(species,key=len,reverse=True))+r')\b',lambda m:'w'+nn(m[1]),st)
    # The heavy pressure and electron-energy fields also have unique physics tags.
    st=re.sub(r'\bplas\b','PLASTOKEN',st)
    st=st.replace('"PLASTOKEN"','plasma')
    # Parameterize every affected string literal, preserving native API types.
    def subst(m):
        v=m[0]
        if any(t in v for t in ['CELLTOKEN','SUFFIXTOKEN','PLASTOKEN']):
            return v.replace('CELLTOKEN','"+cell+"').replace('SUFFIXTOKEN','"+suffix+"').replace('PLASTOKEN','"+plasma+"')
        return v
    st=re.sub(r'"(?:\\.|[^"\\])*"',subst,st)
    result.append(st)
    if '.physics().create(plasma,' in st:
        result.append('    model.component(cell).physics(plasma).identifier(plasma);')
        result.append('    model.component(cell).physics(plasma).field("logelectronenergydensity").field("energy"+suffix);')
    if '.physics().create("ht"+suffix+"",' in st:
        result.append('    model.component(cell).physics("ht"+suffix).field("temperature").field("unusedTemp"+suffix);')
# Keep common aliases to minimize differences from the audited energy closure.
for n in species:
    for stem in ['w','n_w','M_w']:
        name=re.sub(r'(_[1-4][pm])$',r'"+suffix+"\1',n) if '_' in n else n+'"+suffix+"'
        result.append(f'    model.component(cell).variable("gasv"+suffix).set(plasma+".{stem}{n}",plasma+".{stem}{name}");')
body='\n'.join(result);parts=body.split(';');blocks=[';'.join(parts[i:min(i+120,len(parts)-1)])+';' for i in range(0,len(parts)-1,120)]
code='import com.comsol.model.*;\npublic class AddAxialComponent {\npublic static void apply(Model model,String cell,String suffix){String plasma="plasma"+suffix;\n'+''.join(f'part{i}(model,cell,suffix,plasma);\n' for i in range(len(blocks)))+'}\n'
for i,b in enumerate(blocks):code+=f'static void part{i}(Model model,String cell,String suffix,String plasma){{\n{b}\n}}\n'
code+='}\n';(R/'src/AddAxialComponent.java').write_text(code)
# Pure chemical heavy-species rates, before source-layer transport was added.
base=(R/'models/G12_ready.java').read_text();rates={}
for n,expr in re.findall(r'\.set\("plas\.R_w([^" ]+)", new String\[\]\{"([^"]+)"\}\)',base):rates[n]=expr
for n in rates:
    rates[n]=rates[n].replace('intSurf(source_'+n+')','0[kg/s]')
    rates[n]=rates[n].replace('Msweep*(0.21*0.0319988[kg/mol]/Mair)/Vp','0[kg/(m^3*s)]')
assert all('outMass' not in v and 'intSurf' not in v for v in rates.values())
(R/'data/axial_chemical_rates.json').write_text(json.dumps(rates,indent=2))
java='import java.util.*; public class AxialChemicalRates { public static Map<String,String> rates(){Map<String,String> r=new LinkedHashMap<>();\n'
for n,v in rates.items():java+='r.put('+json.dumps(n)+','+json.dumps(v)+');\n'
java+='return r;} }\n';(R/'src/AxialChemicalRates.java').write_text(java)
print('FACTORY',len(statements),'statements;',len(rates),'pure chemistry rates')
