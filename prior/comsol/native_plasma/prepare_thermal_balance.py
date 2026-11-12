"""Detailed balance for unresolved fast-thermal molecular internal states."""
from pathlib import Path
import json
R=Path(__file__).resolve().parent;d=json.loads((R/'data/chemistry.json').read_text())
j=['import com.comsol.model.*;','public class ThermalBalance { public static void apply(Model m){']
used=[]
for a in d['reactions']:
 if a['kind']!='Excitation' or a['formula'] not in ['e+N2=>e+N2','e+O2=>e+O2'] or a['de']>2:continue
 de=a['de'];expr=f'{de}*(1-exp({de}[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-{de}[V]*e_const/(k_B_const*Tgas)))'
 j.append(f'm.component("c").physics("plas").feature("{a["name"]}").set("de",{json.dumps(expr)});');used.append(a['name'])
j.append('}}');(R/'src/ThermalBalance.java').write_text('\n'.join(j))
(R/'data/unresolved_thermal_balance.json').write_text(json.dumps({'reactions':used,'closure':'Unresolved internal states at gas temperature, equal effective degeneracy; microscopic detailed balance k_down=k_up*exp(delta/kTe). Forward energy loss multiplied by (1-exp(delta/kTe-delta/kTg))/(1+exp(-delta/kTg)).','scope':'effective rotational/vibrational and low electronic channels <=2 eV','purpose':'zero net internal-state energy transfer at Te=Tg','limitation':'effective two-level thermal populations; not state-resolved molecular kinetics','source':'Detailed balance of Maxwellian excitation/deexcitation; same Phelps forward collision data; no adjusted rates'},indent=2))
print(used)
