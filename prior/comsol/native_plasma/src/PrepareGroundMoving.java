import com.comsol.model.*;
import com.comsol.model.util.*;
/** Moving-surface control matched to F80/F87 ground-state physics.
 * Starts from M01 shared-column geometry, before the thermal-state feed change.
 */
public class PrepareGroundMoving {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("groundMoving",a[0]);
 PositiveGasTemperature.apply(m);MatchedHeavyFlux.apply(m);PartialMassCoordinates.apply(m);MassAveragedDiffusion.apply(m);
 m.param().set("meltOn","1");m.param().set("w0","11[um]");m.param().set("tEnd","123[ns]");
 PositiveGasTemperature.solver(m);
 m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");
 m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<80[ns],1[ns],0.1[ns])");
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String scale=f.equals("c_umelt")?"100":f.equals("c_etaSurface")?"1":null;
  if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
 }
 m.label("Ground-state moving-surface control, matched to F80/F87 physics");m.save(a[1]);
 System.out.println("MOVING GROUND CONTROL PREPARED: no solution; fresh initial state required");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
