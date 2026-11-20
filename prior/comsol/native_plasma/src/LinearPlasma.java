import com.comsol.model.*;
import com.comsol.model.util.*;
/** Native linear FEM diagnostic: same conservation equations, no log singularity. */
public class LinearPlasma {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").physics("plas").prop("ShapeProperty").set("Formulation","FEMLinear");
 m.component("c").physics("plas").prop("Stabilization").set("SourceStabilization",false);m.component("c").physics("plas").prop("Stabilization").set("ReactionSourceStabilization",false);
 for(String s:new String[]{"StreamlineDiffusionElectrons","StreamlineDiffusionIons","CrosswindDiffusionElectrons","CrosswindDiffusionIons"})m.component("c").physics("plas").prop("ConsistentStabilization").set(s,true);
 NativeLiquidHeat.apply(m);m.component("c").physics("plas").feature("power").set("Qgen","Qlaser-QeRR-QeAttach-Qff-urP*d(Ue,r)-uzP*d(Ue,z)-(Ue+pe)*divU-Qei");
 for(String f:new String[]{"meshSurf","meshFixed","meshAxis"})m.component("c").common(f).set("constraintMethod","nodal");
 m.param().set("tEnd","1000[ns]");m.study("std").feature("time").set("tlist","range(0,2[ns],tEnd)");m.sol().remove("sol1");m.study("std").createAutoSequences("sol");SolverSettings.apply(m);
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String s=f.equals("c_Ne")||f.equals("c_En")?"1e15":f.startsWith("c_plas_")&&f.endsWith("_w")?(f.equals("c_plas_O2_w")||f.equals("c_plas_N2_w")?"1":"1e-10"):f.endsWith("Tg")||f.endsWith("T")?"1000":f.endsWith("umelt")?"100":f.contains("disp")?"1e-5":f.equals("c_etaSurface")?"1":null;
  if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}System.out.println("FIELD "+f+" SCALE "+s);
 }
 m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");m.sol("sol1").feature("t1").set("maxorder",2);
 AddRemesh.apply(m);m.sol("sol1").feature("t1").feature("remesh").set("consistentremesh",true);
 m.save(a[1]);System.out.println("LINEAR PLASMA READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
