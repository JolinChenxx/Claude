import com.comsol.model.*;import com.comsol.model.util.*;
/** Solver accuracy experiment: absolute mass-fraction tolerance 1e-15.
 * The prior 1e-27 tolerance over-resolved chemically irrelevant minor species.
 * Does not add species, seed electrons, reactions or heat. Negative trace
 * values and conservation must be audited on actual saved solutions.
 */
public class SetGlobalTraceTolerance {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("traceTolerance",a[0]);
  for(String f:m.sol("sol1").feature("v1").feature().tags())if(f.startsWith("c_plas_")&&f.endsWith("_w")&&!f.equals("c_plas_O2_w")){
   m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval","1e-8");
  }
  m.sol("sol1").feature("t1").set("atolglobal","1e-7");m.save(a[1]);
  System.out.println("GLOBAL TRACE TOLERANCE EXPERIMENT: 1e-15 absolute mass fraction; no physical source modification");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
