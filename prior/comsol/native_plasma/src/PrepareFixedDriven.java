import com.comsol.model.*;import com.comsol.model.util.*;
/** Flat-interface diagnostic with the same actual thermal/evaporation history. */
public class PrepareFixedDriven {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("fixedDriven",a[0]);
  m.component("c").common("columnMesh").set("prescribedDeformation",new String[]{"0","0","0"});
  m.component("c").variable("drivenCopper").set("etaSurface","0");
  m.component("c").variable("drivenCopper").set("urCu","0[m/s]");
  m.component("c").variable("drivenCopper").set("uzCu","0[m/s]");
  m.component("c").variable("boundv").set("vnSurf","0[m/s]");
  m.param().set("tEnd",a.length>2?a[2]:"133[ns]");
  m.sol("sol1").feature("v1").set("initmethod","init");
  m.sol("sol1").feature("t1").set("tout","tsteps");m.sol("sol1").feature("t1").set("tstepsstore",1);
  m.label("Flat target diagnostic; same C201 temperature and gross evaporation history; native reactions and explicit sheath retained; zero liquid velocity, no physical jet prediction");
  m.save(a[1]);System.out.println("FIXED DRIVEN GAS READY FROM INITIAL STATE");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
