import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

/** Prepare the real moving interface on columns shared with the sheath mesh.
 * The physics is inherited from P85; structural changes restart from t=0.
 */
public class PrepareMappedSurface {
 static Model m;
 static void box(String tag,int dim,String x0,String x1,String z0,String z1){
  m.component("c").selection().create(tag,"Box");
  m.component("c").selection(tag).set("entitydim",dim);
  m.component("c").selection(tag).set("xmin",x0);m.component("c").selection(tag).set("xmax",x1);
  m.component("c").selection(tag).set("ymin",z0);m.component("c").selection(tag).set("ymax",z1);
  m.component("c").selection(tag).set("condition","inside");
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("mapped",a[0]);
  for(String s:m.sol().tags())m.sol().remove(s);
  m.component("c").geom("g").create("wallstrip","Rectangle");
  m.component("c").geom("g").feature("wallstrip").set("pos",new String[]{"0","0"});
  m.component("c").geom("g").feature("wallstrip").set("size",new String[]{"60[um]","2[um]"});
  m.component("c").geom("g").feature("wallstrip").set("selresult",true);
  m.component("c").geom("g").run();
  box("mapTop",1,"-1e-12","60[um]+1e-12[m]","2[um]-1e-12[m]","2[um]+1e-12[m]");
  box("mapLeft",1,"-1e-12","1e-12","-1e-12","2[um]+1e-12[m]");
  box("mapRight",1,"59.999[um]","60.001[um]","-0.001[um]","2.001[um]");
  m.component("c").selection().create("mapRadial","Union");
  m.component("c").selection("mapRadial").set("entitydim",1);m.component("c").selection("mapRadial").set("input",new String[]{"beam","mapTop"});
  m.component("c").selection().create("mapVertical","Union");
  m.component("c").selection("mapVertical").set("entitydim",1);m.component("c").selection("mapVertical").set("input",new String[]{"mapLeft","mapRight"});
  m.component("c").mesh("mesh").feature().remove("sheath");
  m.component("c").mesh("mesh").feature().remove("edge");
  m.component("c").mesh("mesh").feature().remove("tri");
  for(String side:new String[]{"Left","Right"}){
   String tag="normal"+side;
   m.component("c").mesh("mesh").create(tag,"Edge");m.component("c").mesh("mesh").feature(tag).selection().named("map"+side);
   m.component("c").mesh("mesh").feature(tag).create("dist","Distribution");
   m.component("c").mesh("mesh").feature(tag).feature("dist").set("type","predefined");
   m.component("c").mesh("mesh").feature(tag).feature("dist").set("elemcount",25);
   m.component("c").mesh("mesh").feature(tag).feature("dist").set("elemratio",210);
   m.component("c").mesh("mesh").feature(tag).feature("dist").set("growthrate","exponential");
   // The two geometric edges have opposite intrinsic orientation.
   m.component("c").mesh("mesh").feature(tag).feature("dist").set("reverse",side.equals("Right"));
   System.out.println("MAPPED "+side+" EDGES "+Arrays.toString(m.component("c").selection("map"+side).entities()));
  }
  m.component("c").mesh("mesh").create("wallMap","Map");m.component("c").mesh("mesh").feature("wallMap").selection().named("g_wallstrip_dom");
  m.component("c").mesh("mesh").feature("wallMap").create("radial","Distribution");
  m.component("c").mesh("mesh").feature("wallMap").feature("radial").selection().named("mapRadial");
  m.component("c").mesh("mesh").feature("wallMap").feature("radial").set("type","number");
  m.component("c").mesh("mesh").feature("wallMap").feature("radial").set("numelem",120);
  m.component("c").mesh("mesh").create("tri","FreeTri");
  m.component("c").mesh("mesh").run();
  // Quadratic quad geometry represents eta(R)*(1-Z/Ztop) when quadratic eta
  // shares the SAME radial nodes, including midpoints. Keep curvature for
  // capillary stress; first-order straight boundary segments would lose it.
  m.component("c").sorder("quadratic");
  m.component("c").physics("surfaceODE").prop("ShapeProperty").set("shapeFunctionType","shlag");
  m.component("c").physics("surfaceODE").prop("ShapeProperty").set("order",2);
  m.param().set("w0","11[um]");m.param().set("meltOn","1");m.param().set("tEnd","203[ns]");
  String times="range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)";
  m.study("std").feature("time").set("tlist",times);m.study("std").createAutoSequences("sol");
  SolverSettings.apply(m);
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("rtol",0.003);
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");
  m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
  m.sol("sol1").feature("v1").set("initmethod","init");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String s=f.equals("c_V_lm")?"1e-10":f.equals("c_T")||f.endsWith("Tg")?"1000":f.equals("c_umelt")?"100":f.equals("c_etaSurface")||f.equals("c_V")||f.contains("V0")||f.equals("c_Ne")||f.equals("c_En")||f.endsWith("_W")||f.startsWith("c_q")||f.startsWith("c_opt")||f.startsWith("c_ODE")?"1":null;
   if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}
  }
  m.label("Mapped shared-column moving surface: prepared, not yet solved");m.save(a[1]);
  // Export the reference nodes for normal-spacing and column alignment checks.
  double[][] v=m.component("c").mesh("mesh").getVertex();
  try(java.io.PrintWriter p=new java.io.PrintWriter(a[1].replace(".mph","_vertices.csv"))){p.println("r_m,z_m");for(int i=0;i<v[0].length;i++)p.println(v[0][i]+","+v[1][i]);}
  System.out.println("MAPPED SURFACE PREPARED: "+v[0].length+" mesh vertices");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
