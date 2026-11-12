import com.comsol.model.*;
import com.comsol.model.util.*;
/** Algebraically combine native time/convection and their exact cancellations.
 * Same fixed-mesh semidiscrete equations as NodalConservativeStorage, fewer
 * cancelling assembled terms. First-order BDF for the positivity diagnostic.
 */
public class DirectPhysicalStorage {
 static String replaceOnce(String in,String old,String value){
  if(!in.contains(old)||in.indexOf(old)!=in.lastIndexOf(old))throw new IllegalStateException("Expected one term: "+old);
  return in.replace(old,value);
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("directStore",a[0]);
  String weak=m.component("c").physics("plas").feature("nativeDomainQuadrature").getString("weakExpression"),conv="0";
  for(String[] pair:new String[][]{{"Ne","ne","storeElectronScale","storeElectron"},{"En","en","storeEnergyScale","storeEnergy"}}){
   String v=pair[0];weak=replaceOnce(weak,"-plas."+pair[1]+"*(d("+v+",TIME)-"+v+"r*d(r,TIME)-"+v+"z*d(z,TIME))*test("+v+")","-"+pair[2]+"*d("+pair[3]+",TIME)*test("+v+")");
  }
  for(String n:Chemistry.NAMES){String v="W"+n;
   weak=replaceOnce(weak,"-plas.rho*plas.w"+n+"*(d("+v+",TIME)-"+v+"r*d(r,TIME)-"+v+"z*d(z,TIME))*test("+v+")","-rho0*d(store"+n+",TIME)*test("+v+")");
   weak=replaceOnce(weak,"plas.rho*plas.w"+n+"*(plas.ur*"+v+"r+plas.uz*"+v+"z)*test("+v+")","0");
   conv+="+plas.rho_w"+n+"*(uMassR*test("+v+"r)+uMassZ*test("+v+"z))";
  }
  m.component("c").physics("plas").feature("nativeDomainQuadrature").set("weakExpression",weak);
  m.component("c").physics("plas").feature("heavyConservative").set("weakExpression",conv);
  m.component("c").physics("plas").feature("conservedStorage").active(false);
  m.sol("sol1").feature("t1").set("maxorder",1);
  m.save(a[1]);System.out.println("DIRECT PHYSICAL STORAGE: exact algebraic combination; first-order BDF");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
