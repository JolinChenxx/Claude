import com.comsol.model.*;import com.comsol.model.util.*;
/** Solver-only absolute mass-fraction tolerance1e-15 for trace species.
 * Relative tolerance and all physical equations stay unchanged. This removes
 * a1e-27 absolute target after air traces are exhausted from the source layer.
 */
public class SetTwoZoneTraceTolerance {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("twoTrace",a[0]);
  for(String f:m.sol("sol1").feature("v1").feature().tags())if(f.endsWith("_w")&&!f.endsWith("_O2_w")&&!f.endsWith("_O2X_w")){
   m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval","1e-8");
  }
  m.sol("sol1").feature("t1").set("atolglobal","1e-7");m.save(a[1]);System.out.println("TWO ZONE TRACE TOLERANCE1e-15; same equations and saved initial state");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
