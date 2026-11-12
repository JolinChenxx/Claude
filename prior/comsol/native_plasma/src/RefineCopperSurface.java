import com.comsol.model.*;import com.comsol.model.util.*;
/** Resolve the narrow moving rim where the kinematic residual grew. */
public class RefineCopperSurface {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("refinedCopper",a[0]);
 for(String s:m.sol().tags())m.sol(s).clearSolutionData();
 m.component("c").mesh("mesh").feature("fullMap").feature("radial").set("numelem",240);
 m.component("c").mesh("mesh").run();
 m.sol("sol1").feature("t1").feature("coupledCu").set("jtech","minimal");
 m.label("Copper height transport: 0.25um shared radial mesh and consistent surface SUPG; ambient predictor");m.save(a[1]);System.out.println("REFINED COPPER SURFACE READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
