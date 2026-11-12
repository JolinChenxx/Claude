import com.comsol.model.*;
import com.comsol.model.util.*;
public class RemeshContinue {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
  String start=a.length>2?a[2]:"110[ns]";m.sol("sol1").copySolution("beforeRemesh");
  NativeLiquidHeat.apply(m);
  // Displacement is relative to the MATERIAL frame, which is mapped across
  // remeshing. It is not relative to the newly generated mesh coordinates.
  m.component("c").common("meshSurf").set("prescribedMeshDisplacement",new String[]{"0","0","etaSurface*1[um]-Z"});
  for(String f:new String[]{"meshSurf","meshFixed","meshAxis"})m.component("c").common(f).set("constraintMethod","nodal");
  m.component("c").common("deform").set("smoothingType","laplace");
  m.param().set("tEnd","1000[ns]");String times="range("+start+",2[ns],tEnd)";
  m.study("std").feature("time").set("tlist",times);m.sol().remove("sol1");m.study("std").createAutoSequences("sol");SolverSettings.apply(m);
  m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","beforeRemesh");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",start);
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String s=f.endsWith("Tg")||f.endsWith("T")?"1000":f.endsWith("umelt")?"100":f.contains("disp")?"1e-5":f.equals("c_etaSurface")?"1":null;
   if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}}
  m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");m.sol("sol1").feature("t1").set("maxorder",2);
  AddRemesh.apply(m);m.sol("sol1").feature("t1").feature("remesh").set("consistentremesh",true);m.sol("sol1").feature("t1").feature("remesh").set("stopval","0.35");
  m.save(a[1]);System.out.println("CONSISTENT REMESH READY");System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
