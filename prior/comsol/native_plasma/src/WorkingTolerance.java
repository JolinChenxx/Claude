import com.comsol.model.*;
import com.comsol.model.util.*;
/** Working tolerance for mechanism runs; validate the final case separately. */
public class WorkingTolerance {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);double tol=Double.parseDouble(a[2]);
 m.study("std").feature("time").set("rtol",tol);m.sol("sol1").feature("t1").set("rtol",tol);
 m.save(a[1]);System.out.println("WORKING RTOL "+tol+"; physical equations unchanged");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
