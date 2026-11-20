import com.comsol.model.*;
public class SolverSettings {
 public static void splitMesh(Model m){
  apply(m);m.sol("sol1").feature("t1").feature().remove("coupledAll");
  m.sol("sol1").feature("t1").create("split","Segregated");
  for(String f:m.sol("sol1").feature("t1").feature("split").feature().tags())m.sol("sol1").feature("t1").feature("split").feature().remove(f);
  java.util.ArrayList<String> plasma=new java.util.ArrayList<>();
  for(String f:m.sol("sol1").feature("v1").feature().tags())if(!f.startsWith("c_spatial_")&&!f.equals("c_umelt")&&!f.equals("c_p"))plasma.add(f);
  String[][] groups={plasma.toArray(new String[0]),{"c_umelt","c_p"},{"c_spatial_disp","c_spatial_lm_nv"}};
  for(int i=0;i<3;i++){
   String f="block"+i;m.sol("sol1").feature("t1").feature("split").create(f,"SegregatedStep");
   m.sol("sol1").feature("t1").feature("split").feature(f).set("segvar",groups[i]);
   m.sol("sol1").feature("t1").feature("split").feature(f).set("linsolver","directAll");
   m.sol("sol1").feature("t1").feature("split").feature(f).set("subdamp",1);
   m.sol("sol1").feature("t1").feature("split").feature(f).set("subjtech","onevery");
  }
  m.sol("sol1").feature("t1").feature("split").set("maxsegiter",15);
  m.sol("sol1").feature("t1").feature("split").set("segstabacc","none");
 }
 public static void apply(Model m){
  for(String f:m.sol("sol1").feature("t1").feature().tags())if(m.sol("sol1").feature("t1").feature(f).getType().equals("Segregated")||m.sol("sol1").feature("t1").feature(f).getType().equals("FullyCoupled"))m.sol("sol1").feature("t1").feature().remove(f);
  m.sol("sol1").feature("t1").create("directAll","Direct");m.sol("sol1").feature("t1").feature("directAll").set("linsolver","pardiso");
  m.sol("sol1").feature("t1").create("coupledAll","FullyCoupled");
  m.sol("sol1").feature("t1").feature("coupledAll").set("linsolver","directAll");
  m.sol("sol1").feature("t1").feature("coupledAll").set("dtech","auto");
  m.sol("sol1").feature("t1").feature("coupledAll").set("maxiter",12);
  m.sol("sol1").feature("t1").feature("coupledAll").set("jtech","onevery");
  m.sol("sol1").feature("t1").set("maxstepbdf","2[ns]");
  m.sol("sol1").feature("t1").set("consistent","off");
  m.sol("sol1").feature("v1").feature("c_p").set("scalemethod","manual");m.sol("sol1").feature("v1").feature("c_p").set("scaleval","1e6");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String s=f.equals("c_Ne")||f.equals("c_En")||f.endsWith("_W")||f.startsWith("c_q")||f.startsWith("c_opt")||f.equals("c_ODE1")?"1":f.equals("c_V")||f.endsWith("V0e")?"10":null;
   if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}
  }
 }
}
