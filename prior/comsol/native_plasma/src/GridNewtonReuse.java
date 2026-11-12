import com.comsol.model.*;import com.comsol.model.util.*;
/** Reuse the assembled Jacobian until nonlinear convergence requires an update. */
public class GridNewtonReuse {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("gridNewton",a[0]);
  for(String tag:m.sol("sol1").feature("t1").feature().tags()){
   String type=m.sol("sol1").feature("t1").feature(tag).getType();System.out.println("TIME SOLVER "+tag+" "+type);
   if(type.equals("FullyCoupled")){m.sol("sol1").feature("t1").feature(tag).set("jtech","minimal");m.sol("sol1").feature("t1").feature(tag).set("maxiter",20);}
  }
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<60[ns],2[ns],1[ns])");
  m.save(a[1]);System.out.println("GRID NEWTON REUSE READY; equations and rtol unchanged");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
