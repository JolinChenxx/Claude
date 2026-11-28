import com.comsol.model.*;
import com.comsol.model.util.*;
public class MeltStage{
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").physics("ht").feature("advection").set("Q0","-rhoCu*cpCu*((urCu-d(r,TIME))*d(T,r)+(uzCu-d(z,TIME))*d(T,z))");
 for(String ph:new String[]{"flow","temp","plas","charge","ld","lu"})m.study("std").feature("time").activate(ph,false);
 m.component("c").variable("boundv").set("mdot","mdotEvap");
 m.component("c").variable("boundv").set("Qsurface","Acu*Ipeak*exp(-2*(r/w0)^2)*exp(-4*log(2)*((t-tp)/tau)^2)*ncz");
 m.component("c").variable("boundv").set("Qmass","mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))");
 m.component("c").variable("boundv").set("Qexchange","0[W/m^2]");m.component("c").variable("boundv").set("Qthermion","0[W/m^2]");
 m.component("c").physics("melt").feature("surf").set("Fbnd",new String[]{"-recoil*ncr","0","-recoil*ncz"});
 if(a.length>2&&a[2].startsWith("graph")){
  m.component("c").common().remove("meshSurf");
  m.component("c").common().create("meshSurf","PrescribedMeshVelocity");m.component("c").common("meshSurf").selection().named("surface");
  m.component("c").common("meshSurf").set("prescribedMeshVelocity",new String[]{"0","0","meltOn*(uzCu+urCu*ncr/ncz-mdot/(rhoCu*ncz))"});
  m.component("c").common("deform").set("smoothingType","laplace");
  m.component("c").common("meshSurf").set("constraintType","Nitsche");
 }
 if(a.length>2&&a[2].equals("displacement"))MeshGraph.apply(m);
 m.sol().remove("sol1");m.study("std").createAutoSequences("sol");
 m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");
 m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.01[ns]");
 m.sol("sol1").feature("t1").set("maxorder",2);
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String s=f.equals("c_T")?"1000":f.equals("c_p")?"1e6":f.equals("c_umelt")?"1000":f.contains("disp")?"1e-5":null;
  if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}
 }
 if(a.length>2&&(a[2].equals("graphcoupled")||a[2].equals("displacement")))SolverSettings.apply(m);
 if(a.length>3&&a[3].equals("fine")){
  m.param().set("meshFactor","1");m.component("c").mesh("mesh").feature("sz2").set("hmax","0.25[um]");m.component("c").mesh("mesh").feature("edge").feature("size").set("hmax","0.2[um]");m.component("c").mesh("mesh").run();
 }
 m.save(a[1]);System.out.println("MELT STAGE READY");System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
