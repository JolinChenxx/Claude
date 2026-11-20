import com.comsol.model.*;
import com.comsol.model.util.*;
/** Complete energy exchange with the SAME approximate atomic momentum-transfer data. */
public class AtomicElasticExchange {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 for(String name:Chemistry.NAMES)if(name.equals("N")||name.equals("O")||(name.startsWith("Cu")&&!name.equals("Cu_1p"))){
  String tag="elasticAtom"+name;
  m.component("c").physics("plas").create(tag,"ElectronImpactReaction",2);
  m.component("c").physics("plas").feature(tag).set("formula","e+"+name+"=>e+"+name);
  m.component("c").physics("plas").feature(tag).set("type","Elastic");
  m.component("c").physics("plas").feature(tag).set("kf","kmCu*N_A_const");
  m.component("c").physics("plas").feature(tag).set("mratio","me_const*N_A_const/plas.M_w"+name);
 }
 String weighted="0[mol/(kg*m^3)]";
 for(String name:Chemistry.NAMES)if(name.endsWith("_1p")||name.endsWith("_1m"))weighted+="+plas.n_w"+name+"/plas.M_w"+name;
 m.component("c").variable("gasv").set("Qei","3*me_const*N_A_const*plas.ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*("+weighted+")");
 m.save(a[1]);System.out.println("ATOMIC ELASTIC ENERGY EXCHANGE READY; approximate cross section unchanged");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
