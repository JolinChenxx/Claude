import com.comsol.model.*;
import com.comsol.model.util.*;
/** Exclude charged heavy particles from the inherited neutral-collision proxy.
 * The same species already enter nuEI. All cross sections and Coulomb rates stay
 * unchanged; this fixes counting, not a fitted laser-absorption multiplier.
 */
public class NeutralCollisionCount {
 public static void apply(Model m){
  m.component("c").variable("gasv").set("nOtherNeutral","nN+nO+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5");
  m.component("c").variable("gasv").set("nuEN","nN2*kmN2+nO2*kmO2+nOtherNeutral*kmCu");
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("collisionCount",a[0]);apply(m);
  m.label("Native Plasma: physical storage, order-8 integration, distinct neutral/ion collision counts");
  m.save(a[1]);System.out.println("CHARGED HEAVY PARTICLES EXCLUDED FROM NEUTRAL COLLISION COUNT; FRESH INITIAL STATE");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
