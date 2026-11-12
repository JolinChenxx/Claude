import com.comsol.model.*;import com.comsol.model.util.*;
/** Newton Jacobian refresh only; no equation, rate or source changes. */
public class RefreshGasJacobian {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("refreshGas",a[0]);
  m.sol("sol1").feature("t1").feature("coupledAll").set("jtech","onevery");
  m.sol("sol1").feature("t1").feature("coupledAll").set("maxiter",25);
  m.save(a[1]);System.out.println("GAS JACOBIAN REFRESHED EVERY NEWTON ITERATION");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
