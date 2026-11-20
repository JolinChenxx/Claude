import com.comsol.model.*;
import com.comsol.model.util.*;
/** Allow the damped Newton iteration to finish before cutting a physical step. */
public class NonlinearBudget {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.sol("sol1").feature("t1").feature("coupledAll").set("maxiter",25);
 m.save(a[1]);System.out.println("NEWTON LIMIT 25; equations and accuracy unchanged");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
