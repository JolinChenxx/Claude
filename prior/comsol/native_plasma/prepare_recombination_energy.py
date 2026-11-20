"""Energy moment of the SAME Kramers-Milne kernel used for RR rates."""
from pathlib import Path
import json,numpy as np
from numpy.polynomial.laguerre import laggauss
R=Path(__file__).resolve().parent;d=json.loads((R/'data/chemistry.json').read_text());T=np.geomspace(.015,100,501);x,w=laggauss(64)
rx=[a for a in d['reactions'] if a['name'].endswith('rr')];cols=[np.log10(T)]
for a in rx:
 I=a['de'];mean=1/np.sum(w[None,:]/(I+T[:,None]*x[None,:]),axis=1);cols.append(np.log10(mean))
np.savetxt(R/'data/rr_mean_energy.txt',np.column_stack(cols),fmt='%.12e')
J=['import com.comsol.model.*;','public class RecombinationEnergy { public static void apply(Model m){',
'if(java.util.Arrays.asList(m.func().tags()).contains("rrEnergy"))m.func().remove("rrEnergy");',
'm.func().create("rrEnergy","Interpolation");m.func("rrEnergy").set("source","file");m.func("rrEnergy").set("filename","D:/junlin/PlasmaPlume/route1/data/rr_mean_energy.txt");m.func("rrEnergy").set("nargs",1);',
'm.func("rrEnergy").set("funcs",new String[][]{'+','.join('{"rrMean_'+a['name']+'","'+str(i+1)+'"}' for i,a in enumerate(rx))+'});',
'm.func("rrEnergy").set("interp","piecewisecubic");m.func("rrEnergy").set("extrap","const");m.func("rrEnergy").importData();','}}']
(R/'src/RecombinationEnergy.java').write_text('\n'.join(J))
print('RR energy moments:',len(rx))
