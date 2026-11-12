import com.comsol.model.*;
import com.comsol.model.util.*;
public class BuildS4 {
 static Model m;
 static void v(String n,String e){m.component("c").variable("v").set(n,e);}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("M",a[0]);
  m.modelPath(new java.io.File(a[1]).getAbsoluteFile().getParent());
  m.label("S4 Cu-air LTE: 400uJ incident, w=11um, two-pass optics");
  m.param().set("Ein","400[uJ]");m.param().set("Asurf","0.1");
  m.param().set("w0","11[um]");m.param().set("tEnd","2.12[us]");
  m.param().set("nuFactor",a.length>2?a[2]:"1.5");m.param().set("netEvap","0");
  m.param().set("meshFactor",a.length>4?a[4]:"1");
  m.func("lte").set("filename",new java.io.File(new java.io.File(a[1]).getAbsoluteFile().getParentFile(),"../data/lte_grid.txt").getCanonicalPath());m.func("lte").importData();
  v("Ipeak","Ein*2/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");
  v("Idown","Ipeak*Irad*Ipulse*opt");v("Iup","Ipeak*Irad*Ipulse*optUp");
  v("Igas","Asurf*Idown");v("QlaserP","alphaIB*(Idown+Iup)");
  // Normalized, quasistatic optical transmissions remove pulse-tail conditioning.
  m.component("c").physics("optics").feature("init1").set("opt","1");
  m.component("c").physics("optics").feature("in").set("r","1");
  m.component("c").physics().create("opticsUp","GeneralFormPDE","g",new String[]{"optUp"});
  m.component("c").physics("opticsUp").selection().set(new int[]{2});
  m.component("c").physics("opticsUp").feature("gfeq1").set("da","0");
  m.component("c").physics("opticsUp").feature("gfeq1").set("Ga",new String[]{"0","axisWeight*(optUp-0.02*h*d(optUp,z))"});
  m.component("c").physics("opticsUp").feature("gfeq1").set("f","-axisWeight*alphaIB*optUp");
  m.component("c").physics("opticsUp").feature("init1").set("optUp","1-Asurf");
  m.component("c").physics("opticsUp").create("reflect","DirichletBoundary",1);
  m.component("c").physics("opticsUp").feature("reflect").selection().set(new int[]{4});
  m.component("c").physics("opticsUp").feature("reflect").set("r","(1-Asurf)*opt");
  m.component("c").physics("opticsUp").create("escape","FluxBoundary",1);
  m.component("c").physics("opticsUp").feature("escape").selection().set(new int[]{5});
  m.component("c").physics("opticsUp").feature("escape").set("g","-axisWeight*optUp");
  v("mdotEvap","psatCu*sqrt(mCu/(2*pi*kb*max(Ts,150[K])))");
  v("mdotReturn","rhoSafe*Ycu*(1-xionCu)*sqrt(kb*Tpl/(2*pi*mCu))");
  v("returnFraction","mdotReturn/max(mdotEvap,1e-30[kg/(m^2*s)])");
  v("mdot","mdotEvap-netEvap*mdotReturn");
  v("hReturn","Bcu+2.5*kb*Tpl/mCu+speedP^2/2");
  v("Qmass","mdotEvap*hInj-netEvap*mdotReturn*hReturn");
  // netEvap=0 first run retains the original free-evaporation boundary.
  m.component("c").physics("flow").feature("abl").set("g",new String[]{"axisWeight*mdot/rho0","-axisWeight*netEvap*mdotReturn*urP/(rho0*u0)","axisWeight*(pPl-pAmb+mdotEvap*vKn-netEvap*mdotReturn*uzP)/(rho0*u0)","axisWeight*(Qmass-qExchange)/(rho0*e0)","axisWeight*mdot/rho0"});
  m.component("c").physics("solid").feature("heat").set("g","axisWeight*(Igas-Qmass+qExchange)/(8940[kg/m^3]*e0)");
  for(String ph:new String[]{"flow","solid","optics","opticsUp"})m.component("c").physics(ph).prop("ShapeProperty").set("order",1);
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  m.component("c").mesh("mesh").run();
  // Regenerate solver variables so optUp participates in the study.
  m.sol().remove("sol1");m.study("std").createAutoSequences("sol");
  String times="range(0,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])";
  m.study("std").feature("time").set("tlist",times);
  m.sol("sol1").feature("t1").set("tlist",times);
  m.sol("sol1").feature("t1").set("rtol",0.002);
  m.sol("sol1").feature("t1").set("maxorder",1);
  m.sol("sol1").feature("t1").set("initialstepbdfactive",true);
  m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");
  m.sol("sol1").feature("t1").set("maxstepbdf",a.length>3?a[3]:"1[ns]");
  m.sol("sol1").feature("t1").set("atolglobalmethod","scaled");
  m.sol("sol1").feature("t1").set("atolglobalvaluemethod","factor");
  m.sol("sol1").feature("t1").set("atolglobalfactor",0.0001);
  m.sol("sol1").feature("t1").set("plot","off");
  m.save(a[1]);System.out.println("READY "+a[1]);
  m.save(a[1].replace(".mph","_model.java"),"java");
  ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
