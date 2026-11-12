"""Build an energy-consistent Cu/electron replacement of the LTE closure."""
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from common import *

def generate():
    base=(ROOT/'work/R2BuildKinetics.java').read_text()
    source=base.replace('public class R2BuildKinetics','public class R2BuildCoupled')
    # Remove the inherited LTE solution only as a time-dependent forcing; retain it
    # as the initial state. Snapshot old thermodynamic definitions before replacing.
    insertion=r'''
  v("oldT","10^logTpl(leArg,lrArg,Ycu)*1[K]");
  v("oldNe","10^logNe(leArg,lrArg,Ycu)*1[1/m^3]");
  v("oldX","xCuLTE(leArg,lrArg,Ycu)");
  m.func().create("r2eos","Interpolation");m.func("r2eos").set("source","file");
  m.func("r2eos").set("filename",new File(a[2]).getParent()+"/forward_eos.txt");m.func("r2eos").set("struct","grid");
  m.func("r2eos").set("nargs",3);m.func("r2eos").set("funcs",new String[][]{{"eRef","1"},{"pRef","2"},{"neRef","3"},{"z2Ref","4"},{"xRef","5"},{"eHeavy","6"},{"pHeavy","7"}});m.func("r2eos").importData();
  m.func().create("r2cueq","Interpolation");m.func("r2cueq").set("source","file");
  m.func("r2cueq").set("filename",new File(a[2]).getParent()+"/Cu_equilibrium_internal.txt");m.func("r2cueq").set("struct","spreadsheet");
  m.func("r2cueq").set("nargs",1);m.func("r2cueq").set("funcs",new String[][]{{"CuQ0","1"},{"CuQ1","2"},{"CuQ2","3"},{"CuE0","4"},{"CuE1","5"},{"CuE2","6"}});m.func("r2cueq").importData();
  m.func().create("r2inv","Interpolation");m.func("r2inv").set("source","file");
  m.func("r2inv").set("filename",new File(a[2]).getParent()+"/heavy_inverse.txt");m.func("r2inv").set("struct","grid");
  m.func("r2inv").set("nargs",3);m.func("r2inv").set("funcs",new String[][]{{"logTHeavy","1"}});m.func("r2inv").importData();
  v("eHeavyTarget","(ethRaw*rho0-Uchem-rho0*e0*ee)/rhoSafe");
  v("Tneq","10^logTHeavy(log10(max(eHeavyTarget,1e3[J/kg])/1[J/kg]),lrArg,Ycu)*1[K]");v("lTneq","log10(Tneq/1[K])");
  v("TeRaw","2*rho0*e0*ee/(3*kb*max(ne2,neVac))");
  v("eLTEref","10^eRef(lTneq,lrArg,Ycu)*1[J/kg]");
  v("pLTEref","10^pRef(lTneq,lrArg,Ycu)*1[Pa]");
  v("neLTEref","10^neRef(lTneq,lrArg,Ycu)*1[1/m^3]");
  v("z2LTEref","10^z2Ref(lTneq,lrArg,Ycu)*1[1/m^3]");
  v("xLTEref","xRef(lTneq,lrArg,Ycu)");
  v("CuRatio12","2*(2*pi*me_const*kb*Tneq/hplanck^2)^1.5*CuQ2(Tneq/1[K])/CuQ1(Tneq/1[K])*exp(-20.29239*e_const*1[V]/(kb*Tneq))/max(neLTEref,1e-50[1/m^3])");
  v("CuF2LTE","xLTEref*CuRatio12/(1+CuRatio12)");v("CuF1LTE","xLTEref/(1+CuRatio12)");
  v("nCuLTE","rho0*max(qC,0)/mCu");
  v("UCuLTE","nCuLTE*((1-xLTEref)*CuE0(Tneq/1[K])*1[J]+CuF1LTE*(CuE1(Tneq/1[K])*1[J]+7.72638*e_const*1[V])+CuF2LTE*(CuE2(Tneq/1[K])*1[J]+28.01877*e_const*1[V]))");
  v("nAirE","max(neLTEref-nCuLTE*(CuF1LTE+2*CuF2LTE),0[1/m^3])");
  v("nAirZ2","max(z2LTEref-nCuLTE*(CuF1LTE+4*CuF2LTE),0[1/m^3])");
  v("bT","Tneq");v("bRho","rhoP");v("bCu","qC");v("bUr","urP");v("bUz","uzP");v("bD","Dnum");
  v("bJ","Jale");v("bJt","JtALE");v("bS","sR");v("bW","wALE");v("bNe","neLTEref");v("bX","xLTEref");
  v("bP","pLTEref");v("bTs","Ts");v("bEvap","mdotEvap");v("bReturn","mdotReturn");v("bStretch","surfaceStretch");
  v("bLaser","QlaserP");v("bZ","zPhysical");v("bEth","ethRaw*rho0");
  v("Qff2","1.426e-40*1[W*m^3/K^0.5]*sqrt(Te2)*ne2*(rho0*c7/mCu+nAirZ2)");
  v("eeSource","(-Qcoll-Qrrkin-Qff2+Qelastic+bLaser-ne2*kb*Te2*divU)/(rho0*e0)");
  v("energyClosure","(rhoSafe*10^eHeavy(lTneq,lrArg,Ycu)*1[J/kg]+Uchem+Ue-ethRaw*rho0)/(rho0*e0)");
  m.component("c").variable("v").set("Tpl","Tneq");
  m.component("c").variable("v").set("pPl","10^pHeavy(lTneq,lrArg,Ycu)*1[Pa]+kb*ne2*Te2");
  m.component("c").variable("v").set("nePl","ne2");
  m.component("c").variable("v").set("niZ2","rho0*c7/mCu+nAirZ2");
  m.component("c").variable("v").set("xionCu","c7/max(cTotal,1e-30)");
  m.component("c").variable("v").set("Qrad","Qescape+Qff2");
  m.component("c").variable("v").set("alphaIB","0.03692*1[m^5*K^0.5/s^3]*nePl*niZ2/(sqrt(Te2)*nuLight^3)*(1-exp(-hplanck*nuLight/(kb*Te2)))");
  m.component("c").variable("v").set("FR3",m.component("c").variable("v").get("FR3")+"-keLimited*(d(Te2,r)+sR/Jale*d(Te2,z))/(rho0*e0)");
  m.component("c").variable("v").set("FZ3",m.component("c").variable("v").get("FZ3")+"-keLimited*d(Te2,z)/(Jale*rho0*e0)");
  for(String q:new String[]{"qR","qMr","qMz","qE","qC"})m.component("c").physics("flow").feature("init1").set(q,"withsol('"+flow+"',"+q+",setval(t,120[ns]))");
  m.component("c").physics("solid").feature("init1").set("hs","withsol('"+flow+"',hs,setval(t,120[ns]))");
  m.component("c").physics("rec").feature("init1").set("depth","withsol('"+flow+"',depth,setval(t,120[ns]))");
  m.component("c").physics("optics").feature("init1").set("opt","withsol('"+flow+"',opt,setval(t,120[ns]))");
  m.component("c").physics("opticsUp").feature("init1").set("optUp","withsol('"+flow+"',optUp,setval(t,120[ns]))");
'''
    point='  String[] states={"y0","y1","y2","y3","y4","y5","y6","y7","ee"};'
    source=source.replace(point,insertion+'\n'+point)
    old='m.component("c").physics("r2kin").feature("init1").set(q,init);'
    # Explicit frozen expressions avoid referring to the new theta/Te in old solutions.
    new=r'''String initT="withsol('"+flow+"',oldT,setval(t,120[ns]))";
   String initCu="withsol('"+flow+"',qC,setval(t,120[ns]))";
   String initX="withsol('"+flow+"',oldX,setval(t,120[ns]))";
   String initNe="withsol('"+flow+"',oldNe,setval(t,120[ns]))";
   String part="0";for(int j=0;j<7;j++)part+="+"+weights[j]+"*exp(-"+levels[j]+"*e_const*1[V]/(kb*("+initT+")))";
   init=i<7?initCu+"*(1-("+initX+"))*"+weights[i]+"*exp(-"+levels[i]+"*e_const*1[V]/(kb*("+initT+")))/("+part+")":(i==7?initCu+"*("+initX+")":"1.5*kb*("+initNe+")*("+initT+")/(rho0*e0)");
   m.component("c").physics("r2kin").feature("init1").set(q,init);'''
    source=source.replace(old,new)
    source=source.replace('activate(ph,ph.equals("r2kin")||ph.equals("r2te"))','activate(ph,true)')
    source=source.replace('set("teKelvin","bT/1[K]")','set("teKelvin","withsol(\'"+flow+"\',oldT,setval(t,120[ns]))/1[K]")')
    # withsol can only resolve variables already present in the stored solution.
    source=source.replace(',oldT,setval',',10^logTpl(leArg,lrArg,Ycu)*1[K],setval')
    source=source.replace(',oldNe,setval',',10^logNe(leArg,lrArg,Ycu)*1[1/m^3],setval')
    source=source.replace(',oldX,setval',',xCuLTE(leArg,lrArg,Ycu),setval')
    path=ROOT/'work/R2BuildCoupled.java';path.write_text(source,encoding='utf-8');print(path)

if __name__=='__main__':generate()
