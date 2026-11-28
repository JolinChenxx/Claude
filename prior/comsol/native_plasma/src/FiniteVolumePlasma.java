import com.comsol.model.*;
import com.comsol.model.util.*;
/** Native SG transport with conservative electron convection on moving cells. */
public class FiniteVolumePlasma {
 static void g(Model m,String n,String x){m.component("c").variable("gasv").set(n,x);}
 static void override(Model m,String f,String n,String x){m.component("c").physics("plas").feature(f).featureInfo("info").set(n,new String[]{x});}
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").physics("plas").prop("ShapeProperty").set("Formulation","FVMConst");NativeLiquidHeat.apply(m);
 m.component("c").physics("plas").prop("Stabilization").set("SourceStabilization",false);m.component("c").physics("plas").prop("Stabilization").set("ReactionSourceStabilization",false);
 g(m,"meshVr","d(r,TIME)");g(m,"meshVz","d(z,TIME)");g(m,"divMesh","d(meshVr,r)+meshVr/max(r,1e-12[m])+d(meshVz,z)");
 g(m,"relativeR","urP-meshVr");g(m,"relativeZ","uzP-meshVz");g(m,"divRhoRelative","d(rhoP*relativeR,r)+rhoP*relativeR/max(r,1e-12[m])+d(rhoP*relativeZ,z)");
 m.component("c").physics("plas").feature("pes1").set("u",new String[]{"relativeR","0","relativeZ"});
 String vel="(0.5*(up(relativeR)+down(relativeR))*plas.dngr+0.5*(up(relativeZ)+down(relativeZ))*plas.dngz)";
 override(m,"pes1","plas.sg_flux_ne","-plas.nJn_sg/e_const+"+vel+"*if("+vel+">=0,down(plas.ne),up(plas.ne))");
 override(m,"pes1","plas.sg_flux_en","-plas.nJen_sg/e_const+"+vel+"*if("+vel+">=0,down(plas.en),up(plas.en))");
 m.component("c").physics("plas").feature("bulkconv").set("Re","-plas.ne*divMesh");
 m.component("c").physics("plas").feature("power").set("Qgen","Qlaser-QeRR-QeAttach-Qff-pe*divU-Ue*divMesh-Qei");
 m.component("c").physics("plas").feature("wallConv").active(false);
 for(String n:Chemistry.NAMES)if(!n.equals("N2"))override(m,n,"plas.R_w"+n,"("+ConservativeTrace.rate(m,n)+")+plas.w"+n+"*divRhoRelative/plas.M_w"+n);
 m.param().set("tEnd","1000[ns]");m.study("std").feature("time").set("tlist","range(0,2[ns],tEnd)");m.sol().remove("sol1");m.study("std").createAutoSequences("sol");SolverSettings.apply(m);
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
 String s=f.equals("c_Ne")||f.equals("c_En")?"1e15":f.startsWith("c_plas_")&&f.endsWith("_w")?(f.equals("c_plas_O2_w")||f.equals("c_plas_N2_w")?"1":"1e-10"):f.endsWith("Tg")||f.endsWith("T")?"1000":f.endsWith("umelt")?"100":f.contains("disp")?"1e-5":f.equals("c_etaSurface")?"1":null;
 if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}}
 m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxorder",2);
 m.save(a[1]);System.out.println("FINITE VOLUME COUPLING READY, REQUIRES TRANSPORT AND ALE VALIDATION");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
