import com.comsol.model.*;import com.comsol.model.util.*;
/** COMSOL's standard minimal Jacobian update: refresh when Newton convergence
 * needs it, retaining all equations, tolerances and nonlinear iteration limits. */
public class SetJacobianReuse {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("reuse",a[0]);
  m.sol("sol1").feature("t1").feature("coupledAll").set("jtech","minimal");
  m.save(a[1]);System.out.println("JACOBIAN REUSE ENABLED; equations and tolerances unchanged");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
