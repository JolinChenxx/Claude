import com.comsol.model.*;
import com.comsol.model.util.*;
/** Row-sum mass lumping for the linear physical density storage.
 * Integral of the total linear density is unchanged (partition of unity),
 * while off-diagonal mass coupling cannot pull a depleted node below zero.
 * Input is DirectPhysicalStorage; reaction/flux assembly stays unchanged.
 */
public class LumpedPhysicalStorage {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("lumpedStore",a[0]);
 String weak=m.component("c").physics("plas").feature("nativeDomainQuadrature").getString("weakExpression");
 for(String n:Chemistry.NAMES){String term="rho0*d(store"+n+",TIME)*test(W"+n+")";weak=DirectPhysicalStorage.replaceOnce(weak,term,"laginterp(1,"+term+")");}
 for(String[] p:new String[][]{{"storeElectronScale","storeElectron","Ne"},{"storeEnergyScale","storeEnergy","En"}}){String term=p[0]+"*d("+p[1]+",TIME)*test("+p[2]+")";weak=DirectPhysicalStorage.replaceOnce(weak,term,"laginterp(1,"+term+")");}
 m.component("c").physics("plas").feature("nativeDomainQuadrature").set("weakExpression",weak);
 m.save(a[1]);System.out.println("ROW-SUM PHYSICAL MASS LUMPING PREPARED; validation required");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
