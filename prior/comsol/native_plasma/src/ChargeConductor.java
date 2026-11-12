import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Floating conductor with explicit particle-current accumulation. */
public class ChargeConductor {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").physics("plas").feature().remove("metal");
 m.component("c").physics("plas").create("floatCharge","FloatingPotential",1);m.component("c").physics("plas").feature("floatCharge").selection().named("surface");m.component("c").physics("plas").feature("floatCharge").set("Q0","qmetal*1[pC]");
 // qmetal is already advanced by qmetalt = intSurf(Iwall)/1[pC].
 // FloatingPotential alone would not accumulate the particle current.
 for(String s:m.sol().tags())m.sol().remove(s);m.study("std").createAutoSequences("sol");SolverSettings.apply(m);
 m.study("std").feature("time").set("rtol",1e-4);m.sol("sol1").feature("t1").set("rtol",1e-4);
 m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxorder",2);
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
 String s=f.equals("c_ODE1")?"0.001":f.endsWith("Tg")||f.endsWith("T")?"1000":f.endsWith("umelt")?"100":f.contains("disp")?"1e-5":f.equals("c_etaSurface")?"1":f.contains("V0")?"1":null;
 if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}}
 AddRemesh.apply(m);m.sol("sol1").feature("t1").feature("remesh").set("stopexpr","c.spatial.relVolMin");m.sol("sol1").feature("t1").feature("remesh").set("stopval","0.35");m.sol("sol1").feature("t1").feature("remesh").set("consistentremesh",true);
 System.out.println("FLOATING CHARGE "+Arrays.deepToString(m.component("c").physics("plas").feature("floatCharge").featureInfo("info").getInfoTable("Weak")));
 m.save(a[1]);System.out.println("PARTICLE-CONSERVING FLOATING CONDUCTOR READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
