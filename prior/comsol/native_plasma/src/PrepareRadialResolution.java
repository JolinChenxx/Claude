import com.comsol.model.*;import com.comsol.model.util.*;
/** Same saved-field equations on a reduced radial mesh; conservation and overlap
 * comparison are mandatory before accepting the continuation. Not a new source. */
public class PrepareRadialResolution {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("radialResolution",a[0]);
 String saved=ExportNative.savedSolution(m);m.sol(saved).copySolution("resolutionInitial");
 m.component("c").mesh("mesh").feature("fullMap").feature("radial").set("numelem",40);m.component("c").mesh("mesh").run();
 m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","resolutionInitial");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t","113[ns]");
 String list="range(113[ns],0.2[ns],125[ns])";m.param().set("tEnd","125[ns]");m.study("std").feature("time").set("tlist",list);m.sol("sol1").feature("t1").set("tlist",list);
 m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.2[ns]");m.sol("sol1").feature("t1").set("tout","tsteps");m.sol("sol1").feature("t1").set("tstepsstore",1);
 m.sol("sol1").feature("t1").feature("coupledAll").set("jtech","minimal");
 m.label("Spatial native Plasma radial resolution comparison 1.5um; same equations; interpolated113ns; conservation/overlap pending");m.save(a[1]);System.out.println("RADIAL RESOLUTION MODEL READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
