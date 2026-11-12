import com.comsol.model.*;
import com.comsol.model.util.*;
public class RestartCoupled {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
  String saved=ExportNative.savedSolution(m);m.sol(saved).copySolution("restartPhysical");
  String start=a[2];NativeLiquidHeat.apply(m);PlasmaTransport.apply(m);ConservativeTrace.apply(m);
  if(a.length>3)m.param().set("traceIota",a[3]);
  if(a.length>4&&a[4].equals("mixture"))m.component("c").physics("plas").prop("TransportSettings").set("MixtureDiffusionCorrection",true);
  if(a.length>5){m.component("c").physics("plas").prop("InconsistentStabilization").set("IsotropicDiffusionIons",true);m.component("c").physics("plas").prop("InconsistentStabilization").set("delidion",a[5]);}
  for(String f:new String[]{"meshSurf","meshFixed","meshAxis"})m.component("c").common(f).set("constraintMethod","nodal");
  m.param().set("tEnd","1000[ns]");String times="range("+start+",2[ns],tEnd)";
  m.study("std").feature("time").set("tlist",times);
  // Keep the solver sequence: remeshing stores data in sol2; regenerating
  // with that tag present can create sol3 instead of the expected sol1.
  m.sol("sol1").feature("t1").set("tlist",times);
  m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","restartPhysical");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",start);
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String s=f.endsWith("Tg")||f.endsWith("T")?"1000":f.endsWith("umelt")?"100":f.contains("disp")?"1e-5":f.equals("c_etaSurface")?"1":null;
   if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}}
  m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("bwinitstepfrac","0.1");
  m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxorder",2);
  if(!java.util.Arrays.asList(m.sol("sol1").feature("t1").feature().tags()).contains("remesh"))AddRemesh.apply(m);
  m.sol("sol1").feature("t1").feature("remesh").set("consistentremesh",true);
  m.save(a[1]);System.out.println("RESTART COUPLED READY FROM "+saved+" at "+start);System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
