import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Independent copper domain with native ALE boundary velocity. Tangential
 * surface-node motion removes the height-graph advection projection. */
public class PrepareCopperLagrangian {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("copperALE",a[0]);
 for(String s:m.sol().tags())m.sol().remove(s);
 m.component("c").physics().remove("surfaceODE");
 for(String s:m.component("c").common().tags())m.component("c").common().remove(s);
 for(String s:new String[]{"gas","core","mid","wallstrip","fullColumn"})if(Arrays.asList(m.component("c").geom("g").feature().tags()).contains(s))m.component("c").geom("g").feature().remove(s);
 m.component("c").geom("g").run();
 for(String s:m.component("c").mesh().tags())m.component("c").mesh().remove(s);
 m.component("c").mesh().create("mesh");m.component("c").mesh("mesh").feature("size").set("custom",true);m.component("c").mesh("mesh").feature("size").set("hmax","30[um]");m.component("c").mesh("mesh").feature("size").set("hmin","0.05[um]");
 m.component("c").mesh("mesh").create("hotSize","Size");m.component("c").mesh("mesh").feature("hotSize").selection().named("g_meltcore_dom");
 m.component("c").mesh("mesh").feature("hotSize").set("custom",true);m.component("c").mesh("mesh").feature("hotSize").set("hmax","0.4[um]");m.component("c").mesh("mesh").feature("hotSize").set("hmin","0.05[um]");m.component("c").mesh("mesh").feature("hotSize").set("hmaxactive",true);
 m.component("c").mesh("mesh").create("tri","FreeTri");m.component("c").mesh("mesh").run();m.component("c").sorder("quadratic");
 m.component("c").common().create("moveCu","DeformingDomain");m.component("c").common("moveCu").selection().named("g_cu_dom");m.component("c").common("moveCu").set("smoothingType","yeoh");
 m.component("c").common().create("fixedBottom","FixedBoundary");m.component("c").common("fixedBottom").selection().named("bottom");
 m.component("c").selection().create("copperAxis","Box");m.component("c").selection("copperAxis").set("entitydim",1);m.component("c").selection("copperAxis").set("xmin","-1e-12[m]");m.component("c").selection("copperAxis").set("xmax","1e-12[m]");m.component("c").selection("copperAxis").set("condition","inside");
 m.component("c").common().create("meshAxis","PrescribedNormalMeshDisplacement");m.component("c").common("meshAxis").selection().named("copperAxis");m.component("c").common("meshAxis").set("prescribedNormalDisplacement","0[m]");
 m.component("c").common().create("liquidSurface","PrescribedMeshVelocity");m.component("c").common("liquidSurface").selection().named("surface");
 m.component("c").common("liquidSurface").set("prescribedMeshVelocity",new String[]{"urCu-mdot/rhoCu*ncr","0","uzCu-mdot/rhoCu*ncz"});m.component("c").common("liquidSurface").set("constraintType","auto");
 m.component("c").variable("boundv").set("vnSurf","d(r,TIME)*ncr+d(z,TIME)*ncz");
 String times="range(0,2[ns],80[ns]) range(81[ns],1[ns],203[ns])";m.study("std").feature("time").set("tlist",times);m.study("std").createAutoSequences("sol");
 for(String f:m.sol("sol1").feature("t1").feature().tags())if(Arrays.asList("Segregated","FullyCoupled").contains(m.sol("sol1").feature("t1").feature(f).getType()))m.sol("sol1").feature("t1").feature().remove(f);
 m.sol("sol1").feature("t1").create("directCu","Direct");m.sol("sol1").feature("t1").feature("directCu").set("linsolver","pardiso");m.sol("sol1").feature("t1").create("coupledCu","FullyCoupled");m.sol("sol1").feature("t1").feature("coupledCu").set("linsolver","directCu");m.sol("sol1").feature("t1").feature("coupledCu").set("jtech","minimal");m.sol("sol1").feature("t1").feature("coupledCu").set("maxiter",20);
 m.sol("sol1").feature("t1").set("rtol",0.001);m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.5[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String scale=f.equals("c_T")?"1000":f.equals("c_umelt")?"100":f.equals("c_p")?"1e7":f.equals("c_spatial_disp")?"1e-6":null;
  if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
 }
 m.sol("sol1").feature("t1").set("consistent","bweuler");
 m.label("Copper-only native Lagrangian ALE; computed surface velocity; no gas feedback yet");m.save(a[1]);System.out.println("COPPER LAGRANGIAN READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
