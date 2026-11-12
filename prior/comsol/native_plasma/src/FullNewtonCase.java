import com.comsol.model.*;
import com.comsol.model.util.*;
/** Compare full Newton with auto damping, keeping the physical equations. */
public class FullNewtonCase {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.sol("sol1").feature("t1").feature("coupledAll").set("dtech","const");
 m.sol("sol1").feature("t1").feature("coupledAll").set("damp",1);
 m.sol("sol1").feature("t1").feature("coupledAll").set("maxiter",10);
 m.save(a[1]);System.out.println("FULL NEWTON; physical equations and rtol unchanged");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
