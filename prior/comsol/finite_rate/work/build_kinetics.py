"""Generate COMSOL passive Cu reaction-transport on the saved ALE flow.

The initial LTE reservoir at 120 ns is deliberately optimistic for an afterglow
test. No particle populations are prescribed again after initialization.
"""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *
import subprocess

def quoted(s):return json.dumps(str(s),ensure_ascii=True)

def generate():
    net=json.loads((ROOT/'data/network.json').read_text());E=net['energies_eV'];G=net['degeneracies'];chi=net['ionization_eV']
    commands=[]
    def v(n,e):commands.append(f'v({quoted(n)},{quoted(e)});')
    for name,expr in {'bT':'Tpl','bRho':'rhoP','bCu':'qC','bUr':'urP','bUz':'uzP','bD':'Dnum','bJ':'Jale',
        'bJt':'JtALE','bS':'sR','bW':'wALE','bNe':'nePl','bX':'xionCu','bP':'pPl','bTs':'Ts',
        'bEvap':'mdotEvap','bReturn':'mdotReturn','bStretch':'surfaceStretch','bLaser':'QlaserP',
        'bZ':'zPhysical','bEth':'ethRaw*rho0'}.items():
        v(name,f'withsol(\"FLOW_TAG\",{expr},setval(t,t))')
    v('nAirE','max(bNe-rho0*max(bCu,0)*bX/mCu,0[1/m^3])')
    for i in range(8):v(f'c{i}',f'max(y{i},0)')
    v('cTotal','+'.join(f'c{i}' for i in range(8)))
    v('nCu2','rho0*cTotal/mCu');v('ne2','rho0*c7/mCu+nAirE')
    # Temperature is undefined in electron vacuum; blend to gas T below neVac.
    # Physical charge density and reaction rates still use ne2 without a floor.
    v('TeRaw','(2*rho0*e0*ee/(3*kb)+neVac*bT)/(ne2+neVac)')
    v('Te2','if(twoT>0.5,if(electronConduct>0.5,max(1,teKelvin)*1[K],max(1[K],TeRaw)),bT)')
    v('Trate','min(1e6,max(1,Te2/1[K]))')
    v('nNeutralAir','max(bRho-rho0*max(bCu,0),0[kg/m^3])/(28.97*amu)')
    v('vEN','sqrt(8*kb*Te2/(pi*me_const))')
    v('nuEN','(nNeutralAir+rho0*(cTotal-c7)/mCu)*1e-19[m^2]*vEN')
    v('lnLambda','max(2,23-log(sqrt(max(ne2/1[1/m^3],1)*1e-6)/(max(Te2/11604.51812[K],1e-4)^1.5)))')
    v('nuEI','2.91e-12[m^3/s]*max(ne2,0[1/m^3])*lnLambda/max(Te2/11604.51812[K],1e-4)^1.5')
    v('nuEnergy','2*me_const/(28.97*amu)*nuEN+2*me_const/mCu*nuEI')
    v('Qelastic','1.5*kb*ne2*nuEnergy*(bT-Te2)')
    v('ke2','if(twoT>0.5,electronConduct*2.5*kb^2*ne2*Te2/(me_const*(nuEN+nuEI+1[1/s])),0[W/(m*K)])')
    v('gradTe2','sqrt((d(Te2,r)+bS/bJ*d(Te2,z))^2+(d(Te2,z)/bJ)^2+1e-12[K^2/m^2])')
    v('qElectronSat','0.3*ne2*kb*Te2*vEN')
    v('keLimited','ke2/(1+ke2*gradTe2/max(qElectronSat,1e-20[W/m^2]))')
    v('divU','d(bUr,r)+bS/bJ*d(bUr,z)+bUr/max(r,1e-9[m])+d(bUz,z)/bJ')
    v('Lescape','max(1[um],min(100[um],max(bCu,1e-30)/(sqrt(d(bCu,r)^2+(d(bCu,z)/bJ)^2+1e-12[1/m^2]))))')
    reactions=[[] for _ in range(8)];coll=[];rrkin=[];rad=[];quench=[]
    for pair in net['pairs']:
        lo,up=pair['lo'],pair['up'];tag=f'ce{lo}{up}'
        v(tag,f'ne2*({pair["up_func"]}(Trate)*1[m^3/s]*c{lo}-{pair["dn_func"]}(Trate)*1[m^3/s]*c{up})')
        reactions[lo].append('-'+tag);reactions[up].append('+'+tag)
        coll.append(f'({tag})*{E[up]-E[lo]:.12g}*e_const*1[V]*rho0/mCu')
    for i in range(7):
        v(f'ci{i}',f'ne2*ion{i}(Trate)*1[m^3/s]*c{i}-ne2^2*tbr{i}(Trate)*1[m^6/s]*c7')
        v(f'rrc{i}',f'ne2*rr{i}(Trate)*1[m^3/s]*c7')
        reactions[i].extend([f'-ci{i}',f'+rrc{i}']);reactions[7].extend([f'+ci{i}',f'-rrc{i}'])
        coll.append(f'ci{i}*{chi-E[i]:.12g}*e_const*1[V]*rho0/mCu')
        rrkin.append(f'ne2*rho0*c7/mCu*rrE{i}(Trate)*1[J*m^3/s]')
        rad.append(f'rrc{i}*{chi-E[i]:.12g}*e_const*1[V]*rho0/mCu')
    for k,line in enumerate(net['lines']):
        lo,up=line['lo'],line['up'];nu=line['nu'];A=line['A']
        v(f'tau{k}',f'max(0,{299792458.**2*A/(8*np.pi*nu**2):.13g}*1[m^2/s]*(rho0/mCu)*(c{lo}*{G[up]/G[lo]:.12g}-c{up})*Lescape/({nu/299792458.:.12g}*1[1/m]*sqrt(kb*bT/mCu)*sqrt(2*pi)))')
        v(f'beta{k}',f'if(trapping>0.5,if(tau{k}<1e-4,1-tau{k}/2,(1-exp(-tau{k}))/max(tau{k},1e-30)),1)')
        v(f'rad{k}',f'{A:.13g}*1[1/s]*beta{k}*c{up}')
        reactions[lo].append(f'+rad{k}');reactions[up].append(f'-rad{k}')
        rad.append(f'rad{k}*{line["energy_eV"]:.12g}*e_const*1[V]*rho0/mCu')
    v('kHeavy','quenchP*pi*(3e-10[m])^2*sqrt(8*kb*bT/(pi*(mCu*(28.97*amu)/(mCu+28.97*amu))))')
    for i in range(1,7):
        v(f'hq{i}',f'kHeavy*nNeutralAir*(c{i}-c0*{G[i]/G[0]:.12g}*exp(-{E[i]:.12g}*e_const*1[V]/(kb*bT)))')
        reactions[i].append(f'-hq{i}');reactions[0].append(f'+hq{i}')
        quench.append(f'hq{i}*{E[i]:.12g}*e_const*1[V]*rho0/mCu')
    for i,terms in enumerate(reactions):v(f'R{i}',''.join(terms))
    v('Qcoll','+'.join(coll));v('Qrrkin','+'.join(rrkin));v('Qescape','+'.join(rad)+'+Qrrkin')
    v('Qquench','+'.join(quench));v('Uchem','rho0/mCu*e_const*1[V]*('+ '+'.join([f'c{i}*{E[i]:.12g}' for i in range(1,7)]+[f'c7*{chi:.12g}'])+')')
    v('Ue','1.5*kb*ne2*Te2')
    v('deltaP','kb*(ne2*Te2-bNe*bT)')
    # The same truncated level set is used on both sides of this energy diagnostic.
    qsum='+'.join(f'{g:.12g}*exp(-{e:.12g}*e_const*1[V]/(kb*bT))' for e,g in zip(E,G))
    v('qLTE7',qsum)
    v('UbExc','rho0*max(bCu,0)/mCu*(1-bX)/qLTE7*('+ '+'.join(f'{G[i]*E[i]:.12g}*e_const*1[V]*exp(-{E[i]:.12g}*e_const*1[V]/(kb*bT))' for i in range(1,7))+')')
    v('UbIon',f'rho0*max(bCu,0)/mCu*bX*{chi:.12g}*e_const*1[V]')
    v('deltaU','Ue+Uchem-(1.5*kb*bNe*bT+UbIon+UbExc)')
    v('Qff2','1.426e-40*1[W*m^3/K^0.5]*sqrt(Te2)*ne2^2')
    v('eeSource','(-Qcoll-Qrrkin-Qff2+Qelastic+bLaser-ne2*kb*Te2*divU)/(rho0*e0)')
    commands_text='\n'.join(commands)
    funcs=','.join('{'+quoted(name)+','+quoted(str(net['columns'][name]))+'}' for name in net['functions'])
    source=r'''import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.*;
import java.util.*;
public class R2BuildKinetics {
 static Model m;
 static void v(String n,String e){m.component("c").variable("r2v").set(n,e);}
 static String dr(String q){return "(d("+q+",r)+bS/bJ*d("+q+",z))";}
 static String dz(String q){return "(d("+q+",z)/bJ)";}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("M",a[0]);
  String flow=Arrays.asList(m.sol().tags()).contains("merged")?"merged":"sol1";
  m.modelPath(new File(a[1]).getAbsoluteFile().getParent());
  m.label(new File(a[1]).getName());
  m.param().set("twoT",a[3]);m.param().set("quenchP",a[4]);m.param().set("trapping","1");
  m.param().set("neVac","1e14[1/m^3]");
  m.param().set("electronConduct",a.length>6?a[6]:"1");
  m.param().set("amu","1.66053906660e-27[kg]");
  m.component("c").variable().create("r2v");
  m.func().create("r2rates","Interpolation");m.func("r2rates").set("source","file");
  m.func("r2rates").set("filename",a[2]);m.func("r2rates").set("struct","spreadsheet");
  m.func("r2rates").set("nargs",1);m.func("r2rates").set("funcs",new String[][]{FUNCTIONS});
  m.func("r2rates").set("interp","linear");m.func("r2rates").importData();
  COMMANDS
  for(String key:m.component("c").variable("r2v").varnames()){
   String val=m.component("c").variable("r2v").get(key);
   if(val.contains("FLOW_TAG"))v(key,val.replace("\"FLOW_TAG\"","'"+flow+"'"));
  }
  String[] states={"y0","y1","y2","y3","y4","y5","y6","y7","ee"};
  m.component("c").physics().create("r2te","GeneralFormPDE","g",new String[]{"teKelvin"});
  m.component("c").physics("r2te").selection().set(2);
  m.component("c").physics("r2te").prop("ShapeProperty").set("order",1);
  m.component("c").physics("r2te").feature("gfeq1").set("da","0");
  m.component("c").physics("r2te").feature("gfeq1").set("Ga",new String[]{"0","0"});
  m.component("c").physics("r2te").feature("gfeq1").set("f","axisWeight*bJ*(teKelvin-if(twoT>0.5 && electronConduct>0.5,TeRaw/1[K],bT/1[K]))/max(bT/1[K],300)");
  m.component("c").physics("r2te").feature("init1").set("teKelvin","bT/1[K]");
  m.component("c").physics().create("r2kin","GeneralFormPDE","g",states);
  m.component("c").physics("r2kin").selection().set(2);
  m.component("c").physics("r2kin").prop("ShapeProperty").set("order",1);
  String[][] flux=new String[9][2];String[] src=new String[9];String[] bg=new String[9];
  double[] levels={0,1.388948,1.6422256,3.7858977,3.8166920,6.1911751,6.1920252};
  double[] weights={2,6,4,2,4,4,6};
  for(int i=0;i<9;i++){
   String q=states[i];
   String fr="bUr*"+q+"-bD*"+dr(q),fz="bUz*"+q+"-bD*"+dz(q);
   if(i==8){fr+="-keLimited*"+dr("Te2")+"/(rho0*e0)";fz+="-keLimited*"+dz("Te2")+"/(rho0*e0)";}
   flux[i][0]="axisWeight*bJ*("+fr+")";
   flux[i][1]="axisWeight*(bS*("+fr+")+("+fz+")-bW*"+q+")";
   src[i]="-axisWeight*bJt*"+q+"+axisWeight*bJ*"+(i<8?"R"+i:"if(twoT>0.5,eeSource,0[1/s])");
   bg[i]=i<7?"axisWeight*bStretch*(("+(i==0?"bEvap":"0[kg/(m^2*s)]")+")-bReturn*c"+i+"/max(cTotal-c7,1e-30))/rho0":"0";
   String init=i<7?"bCu*(1-bX)*"+weights[i]+"*exp(-"+levels[i]+"*e_const*1[V]/(kb*bT))/qLTE7":(i==7?"bCu*bX":"1.5*kb*bNe*bT/(rho0*e0)");
   m.component("c").physics("r2kin").feature("init1").set(q,init);
  }
  m.component("c").physics("r2kin").feature("gfeq1").set("da","axisWeight*bJ");
  m.component("c").physics("r2kin").feature("gfeq1").set("Ga",flux);
  m.component("c").physics("r2kin").feature("gfeq1").set("f",src);
  m.component("c").physics("r2kin").create("influx","FluxBoundary",1);
  m.component("c").physics("r2kin").feature("influx").selection().set(4);
  m.component("c").physics("r2kin").feature("influx").set("g",bg);
  m.component("c").physics("r2kin").create("far","DirichletBoundary",1);
  m.component("c").physics("r2kin").feature("far").selection().set(new int[]{5,7});
  m.component("c").physics("r2kin").feature("far").set("r",new String[]{"0","0","0","0","0","0","0","0","0"});
  m.study().create("r2study");m.study("r2study").create("time","Transient");
  for(String ph:m.component("c").physics().tags())m.study("r2study").feature("time").activate(ph,ph.equals("r2kin")||ph.equals("r2te"));
  String times=a.length>5?a[5]:"range(120[ns],2[ns],900[ns])";
  m.study("r2study").feature("time").set("tlist",times);
  m.study("r2study").createAutoSequences("sol");
  String[] tags=m.sol().tags();String kin=tags[tags.length-1];
  for(String tag:m.sol(kin).feature("v1").feature().tags()){
   String scale="1";
   if(tag.equals("c_depth"))scale="1e-6";
   if(tag.equals("c_theta"))scale="10";
   if(tag.equals("c_teKelvin"))scale="10000";
   if(tag.equals("c_ee"))scale="1e-4";
   if(tag.equals("c_y7"))scale="0.01";
   if(tag.equals("c_y1")||tag.equals("c_y2"))scale="0.1";
   if(tag.equals("c_y3")||tag.equals("c_y4"))scale="0.001";
   if(tag.equals("c_y5")||tag.equals("c_y6"))scale="1e-5";
   m.sol(kin).feature("v1").feature(tag).set("scalemethod","manual");
   m.sol(kin).feature("v1").feature(tag).set("scaleval",scale);
  }
  m.sol(kin).feature("t1").set("atolglobalmethod","scaled");
  m.sol(kin).feature("t1").set("atolglobalvaluemethod","factor");
  m.sol(kin).feature("t1").set("atolglobalfactor",1e-3);
  m.sol(kin).feature("t1").set("estrat","exclude");
  m.sol(kin).feature("t1").set("rtol",0.002);
  m.sol(kin).feature("t1").set("maxorder",2);
  m.sol(kin).feature("t1").set("maxstepconstraintbdf","const");m.sol(kin).feature("t1").set("maxstepbdf","2[ns]");
  m.sol(kin).feature("t1").set("initialstepbdfactive",true);m.sol(kin).feature("t1").set("initialstepbdf","0.001[ns]");
  m.sol(kin).feature("t1").set("plot","off");
  m.sol(kin).feature("t1").feature("fc1").set("jtech","onevery");
  m.sol(kin).feature("t1").feature("fc1").set("dtech","auto");
  m.sol(kin).feature("t1").feature("fc1").set("maxiter",25);
  m.save(a[1]);System.out.println("READY flow="+flow+" kin="+kin);ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
'''.replace('FUNCTIONS',funcs).replace('COMMANDS',commands_text)
    path=ROOT/'work/R2BuildKinetics.java';path.write_text(source,encoding='utf-8')
    print(path)

if __name__=='__main__':generate()
