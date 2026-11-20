import com.comsol.model.*;
import com.comsol.model.util.*;
/** Tighten only nonlinear convergence for cancelling thermionic wall currents. */
public class TightCurrentSolve {public static void main(String[] a){try{
ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
m.sol("sol1").feature("t1").feature("coupledAll").set("ntolfact",Double.parseDouble(a[2]));
m.sol("sol1").feature("t1").feature("coupledAll").set("maxiter",15);
m.save(a[1]);System.out.println("NONLINEAR TOLERANCE FACTOR "+a[2]);System.exit(0);
}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
