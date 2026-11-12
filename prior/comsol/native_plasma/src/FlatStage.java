import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

/** Fixed target-plane approximation, inheriting the corrected native Plasma equations.
 * No saved moving-mesh solution is used as an initial state.
 */
public class FlatStage {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("flat",a[0]);
  m.label("Route 1 round 2: fixed target plane, native non-LTE Plasma");
  for(String s:m.sol().tags())m.sol().remove(s);
  m.component("c").physics().remove("melt");
  m.component("c").physics().remove("surfaceODE");
  for(String s:m.component("c").common().tags())m.component("c").common().remove(s);
  m.component("c").common().create("fixedPlane","PrescribedDeformation");
  m.component("c").common("fixedPlane").selection().all();
  m.component("c").common("fixedPlane").set("prescribedDeformation",new String[]{"0","0","0"});
  m.component("c").variable().create("flatv");
  for(String s:new String[]{"urCu","uphiCu","uzCu"})m.component("c").variable("flatv").set(s,"0[m/s]");
  m.component("c").variable("flatv").set("etaSurface","0");
  m.component("c").variable("flatv").set("p","0[Pa]");
  for(String s:new String[]{"vnSurf","vnPhysical"})m.component("c").variable("boundv").set(s,"0[m/s]");
  for(String s:new String[]{"surfaceAreaRate","lineAreaRate"})m.component("c").variable("boundv").set(s,"0[1/s]");
  m.component("c").physics("ht").feature("fluid1").set("u",new String[]{"0","0","0"});
  // There is no moving-volume enthalpy removal. The fixed-domain heat flux
  // therefore removes the FULL supplied gas enthalpy, without +mdot*hCond.
  m.component("c").physics("ht").feature("laser").set("q0","Qsurface-Qmass+Qexchange-Qthermion");
  m.param().set("meltOn","0");m.param().set("w0","11[um]");m.param().set("tEnd",a.length>2?a[2]:"203[ns]");
  m.component("c").physics().create("flatLedger","GlobalEquations","g");
  m.component("c").physics("flatLedger").feature("ge1").set("name",new String[]{"flatMassNg","flatRemovedHuJ"});
  m.component("c").physics("flatLedger").feature("ge1").set("equation",new String[]{"flatMassNgt-intSurf(mdot)/1e-12[kg]","flatRemovedHuJt-intSurf(mdot*hCond)/1e-6[J]"});
  m.component("c").physics("flatLedger").feature("ge1").set("initialValueU",new String[]{"0","0"});
  m.component("c").physics("flatLedger").feature("ge1").set("valueType","real");
  String times="range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)";
  m.study("std").feature("time").set("tlist",times);
  m.study("std").feature("time").set("rtol",0.001);
  m.study("std").createAutoSequences("sol");
  for(String f:m.sol("sol1").feature("t1").feature().tags()){
   String type=m.sol("sol1").feature("t1").feature(f).getType();
   if(type.equals("Segregated")||type.equals("FullyCoupled"))m.sol("sol1").feature("t1").feature().remove(f);
  }
  m.sol("sol1").feature("t1").create("directAll","Direct");
  m.sol("sol1").feature("t1").feature("directAll").set("linsolver","pardiso");
  m.sol("sol1").feature("t1").create("coupledAll","FullyCoupled");
  m.sol("sol1").feature("t1").feature("coupledAll").set("linsolver","directAll");
  m.sol("sol1").feature("t1").feature("coupledAll").set("dtech","auto");
  m.sol("sol1").feature("t1").feature("coupledAll").set("maxiter",15);
  m.sol("sol1").feature("t1").feature("coupledAll").set("jtech","onevery");
  m.sol("sol1").feature("t1").set("tlist",times);
  m.sol("sol1").feature("t1").set("rtol",0.001);
  m.sol("sol1").feature("t1").set("maxorder",2);
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");
  m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");
  m.sol("sol1").feature("t1").set("initialstepbdfactive",true);
  m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
  m.sol("sol1").feature("t1").set("consistent","bweuler");
  m.sol("sol1").feature("v1").set("initmethod","init");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String s=f.equals("c_V_lm")?"1e-10":f.endsWith("Tg")||f.equals("c_T")?"1000":f.equals("c_V")||f.contains("V0")?"1":f.equals("c_Ne")||f.equals("c_En")||f.endsWith("_W")||f.startsWith("c_q")||f.startsWith("c_opt")||f.startsWith("c_ODE")?"1":null;
   if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}
  }
  m.save(a[1]);
  m.save(a[1].replace(".mph","_model.java"),"java");
  System.out.println("FIXED PLANE READY: "+times+"; mass loss ledger, full fixed-domain enthalpy flux");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
