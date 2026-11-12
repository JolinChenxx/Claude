import com.comsol.model.*;
import com.comsol.model.util.*;
public class ReviseCase {
 public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("r",a[0]);
 CouplingFixes.apply(m);GasFields.apply(m);
 ThermalBalance.apply(m);
 MeltDiscretization.apply(m);
 if(a.length>3&&a[3].equals("fixed")){
  m.param().set("meltOn","0");
  for(String f:m.component("c").common().tags())m.component("c").common(f).active(false);
 }
 if(a.length>3&&a[3].equals("coarse")){m.param().set("meshFactor","2");m.component("c").mesh("mesh").run();}
 if(a.length>3&&a[3].equals("graph"))MeshGraph.velocity(m);
 if(a.length>3&&a[3].equals("displacement"))MeshGraph.apply(m);
 if(a.length>4)m.param().set("tEnd",a[4]);
 if(a.length>3&&a[3].equals("trace")){
  for(String n:Chemistry.NAMES)if(!n.equals("N2")&&!n.equals("O2"))m.component("c").physics("plas").feature(n).set("x0","1e-12");
  for(String n:new String[]{"O2_1p","N_1p","O_1p","O_1m","O2_1m","Cu_1p"})m.component("c").physics("plas").feature(n).set("n0","1e7[1/m^3]");
  m.component("c").physics("plas").feature("N2_1p").set("n0","seedNe-2e7[1/m^3]");
 }
 m.sol().remove("sol1");m.study("std").createAutoSequences("sol");
 m.sol("sol1").feature("t1").set("maxorder",2);
 m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.5[ns]");
 m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String s=f.endsWith("Tg")||f.endsWith("T")?"1000":f.endsWith("umelt")?"1000":f.contains("disp")?"1e-5":f.equals("c_etaSurface")?"1":null;
  if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}
 }
 if(a.length>2&&a[2].equals("coupled"))SolverSettings.apply(m);
 if(a.length>2&&a[2].equals("split"))SolverSettings.splitMesh(m);
 m.save(a[1]);System.out.println("REVISED "+a[1]);System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
