import com.comsol.model.*;
import com.comsol.model.util.*;
public class MeltDiscretization {
 public static void apply(Model m){
  // Inf-sup stable quadratic velocity / linear pressure avoids relying on
  // equal-order pressure stabilization in an extremely stiff Darcy layer.
  m.component("c").physics("melt").prop("ShapeProperty").set("order_fluid",2);
  m.component("c").physics("melt").prop("ConsistentStabilization").set("StreamlineDiffusion",false);
  m.component("c").physics("melt").prop("ConsistentStabilization").set("CrosswindDiffusion",false);
 }
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);apply(m);
 if(a.length>2&&a[2].equals("displacement"))MeshGraph.apply(m);
 m.sol().remove("sol1");m.study("std").createAutoSequences("sol");SolverSettings.apply(m);
 m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.01[ns]");m.sol("sol1").feature("t1").set("maxorder",2);
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String s=f.equals("c_T")?"1000":f.equals("c_umelt")?"100":f.contains("disp")?"1e-5":f.equals("c_etaSurface")?"1":null;
  if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}
 }
 m.save(a[1]);System.out.println("MIXED MELT READY");System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}
}
