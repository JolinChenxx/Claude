import com.comsol.model.*;import com.comsol.model.util.*;
/** Same kinematic continuum law, projected in normal flux rather than graph
 * velocity. The area-weighted weak residual then matches the copper ledger. */
public class NormalFluxSurface{public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("normalSurface",a[0]);
 System.out.println("OLD DA "+m.component("c").physics("surfaceODE").feature("dode1").getString("da"));System.out.println("OLD F "+m.component("c").physics("surfaceODE").feature("dode1").getString("f"));
 m.component("c").physics("surfaceODE").feature("dode1").set("da","ncz");m.component("c").physics("surfaceODE").feature("dode1").set("f","meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)/1[um]");
 for(String s:m.sol().tags())m.sol(s).clearSolutionData();m.sol("sol1").feature("v1").set("initmethod","init");m.param().set("tEnd","175[ns]");String list="range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)";m.study("std").feature("time").set("tlist",list);m.sol("sol1").feature("t1").set("tlist",list);
 m.label("Copper predictor: normal-flux surface weak form, same continuum kinematics; local residual and source comparison pending");m.save(a[1]);System.out.println("NORMAL FLUX SURFACE READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
