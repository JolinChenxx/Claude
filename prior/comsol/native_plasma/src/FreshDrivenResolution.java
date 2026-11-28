import com.comsol.model.*;import com.comsol.model.util.*;
/** Recompute changed mesh from physical initial state; no interpolation of a
 * thin nonneutral sheath across different radial nodes. */
public class FreshDrivenResolution {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("freshResolution",a[0]);
 m.param().set("tEnd","133[ns]");PositiveGasTemperature.solver(m);
 m.sol("sol1").feature("t1").feature("coupledAll").set("jtech","minimal");
 String list="range(0,2[ns],80[ns]) range(81[ns],1[ns],133[ns])";m.study("std").feature("time").set("tlist",list);m.sol("sol1").feature("t1").set("tlist",list);m.sol("sol1").feature("t1").set("tout","tstepsclosest");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<80[ns],1[ns],0.2[ns])");
 m.label("Same driven spatial equations on 1.5um radial mesh; fresh initial state; overlap validation pending");m.save(a[1]);System.out.println("FRESH RESOLUTION READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
