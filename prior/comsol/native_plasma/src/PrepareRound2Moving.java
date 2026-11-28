import com.comsol.model.*;
import com.comsol.model.util.*;
/** Apply the current fixed-plane corrections to the prepared shared-column mesh.
 * Construction only; solve after the early fixed-plane comparison is available.
 */
public class PrepareRound2Moving {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("moving",a[0]);
 PositiveGasTemperature.apply(m);SurfaceStateFeed.apply(m);MatchedHeavyFlux.apply(m);MassAveragedDiffusion.apply(m);
 m.param().set("meltOn","1");m.param().set("tEnd","203[ns]");PositiveGasTemperature.solver(m);
 m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");
 m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<80[ns],1[ns],0.05[ns])");
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String scale=f.equals("c_umelt")?"100":f.equals("c_etaSurface")?"1":null;
  if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
 }
 m.label("Prepared real moving surface: positive T, surface-state feed, mass-average diffusion");m.save(a[1]);
 System.out.println("ROUND2 MOVING STRUCTURE PREPARED, NO SOLUTION CLAIMED");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
