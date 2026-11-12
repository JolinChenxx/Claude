import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Spatial native Plasma with a saved, computed moving copper boundary.
 * This is a partitioned predictor, NOT an imposed paper-image trajectory. */
public class PrepareDrivenPlasma {
 static Model m;static void b(String n,String s){m.component("c").variable("boundv").set(n,s);}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("drivenPlasma",a[0]);for(String s:m.sol().tags())m.sol().remove(s);
  for(String p:new String[]{"ht","melt","surfaceODE"})m.component("c").physics().remove(p);
  m.func().create("copperHistory","Interpolation");m.func("copperHistory").set("source","file");m.func("copperHistory").set("filename",a[2]);m.func("copperHistory").set("nargs",2);
  m.func("copperHistory").set("funcs",new String[][]{{"cuHeight","1"},{"cuTemperature","2"},{"cuUr","3"},{"cuUz","4"},{"cuEvap","5"}});m.func("copperHistory").set("interp","linear");m.func("copperHistory").set("extrap","const");m.func("copperHistory").importData();
  m.param().set("tEnd",a.length>3?a[3]:"203[ns]");m.param().set("tp","120[ns]");m.param().set("cameraZero","90[ns]");m.param().set("w0","11[um]");
  m.component("c").variable().create("drivenCopper");
  m.component("c").variable("drivenCopper").set("etaSurface","cuHeight(R/1[um],t/1[ns])");
  m.component("c").variable("drivenCopper").set("T","cuTemperature(R/1[um],t/1[ns])*1[K]");
  m.component("c").variable("drivenCopper").set("urCu","cuUr(R/1[um],t/1[ns])*1[m/s]");m.component("c").variable("drivenCopper").set("uzCu","cuUz(R/1[um],t/1[ns])*1[m/s]");m.component("c").variable("drivenCopper").set("uphiCu","0[m/s]");m.component("c").variable("drivenCopper").set("p","0[Pa]");
  // Same surface map, evaluated directly rather than through an eliminated ODE.
  m.component("c").common("columnMesh").set("prescribedDeformation",new String[]{"0","0","cuHeight(R/1[um],t/1[ns])*1[um]*if(Z>=0,1-Z/Zdom,1+Z/Dcu)"});
  b("mdotEvap","cuEvap(R/1[um],t/1[ns])*1[kg/(m^2*s)]");
  b("vnSurf","d(r,TIME)*ncr+d(z,TIME)*ncz");
  // The original ray PDE already propagates all incident and reflected light;
  // no reserved10/90 energy split is used in this spatial model.
  m.component("c").variable("gasv").set("pulse","exp(-4*log(2)*((t-tp)/tau)^2)");
  m.component("c").variable("gasv").set("Ipeak","2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");
  PositiveGasTemperature.solver(m);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<80[ns],2[ns],0.5[ns])");m.sol("sol1").feature("t1").feature("coupledAll").set("jtech","minimal");
  m.label("Spatial native Plasma + prescribed computed copper motion; partitioned first iterate, feedback NOT converged; camera zero=leading Gaussian half maximum");m.save(a[1]);System.out.println("DRIVEN SPATIAL PLASMA READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
