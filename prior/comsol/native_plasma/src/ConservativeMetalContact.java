import com.comsol.model.*;
import com.comsol.model.util.*;
/** Charge balance consistent with the actual axisymmetric constraint weak form. */
public class ConservativeMetalContact {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 // COMSOL's constraint reaction is unweighted, whereas Poisson uses 2*pi*r.
 // Thus V_lm is minus charge per meridional arclength, not per surface area.
 m.component("c").variable("boundv").set("lineAreaRate","dtang(d(r,TIME),r)+dtang(d(z,TIME),z)");
 m.component("c").physics("plas").feature("metal").featureInfo("info").set("plas.I_1",new String[]{"-plas.metal.int(d(V_lm,TIME)+V_lm*lineAreaRate+2*pi*r*plas.d*Iwall)"});
 // Native weak constraint adds -V_lm*test(V0) as well as +V_lm*test(V).
 // V0 already has the current balance, so cancel only its extra reaction.
 m.component("c").physics("plas").create("metalReactionCorrection","WeakContribution",1);
 m.component("c").physics("plas").feature("metalReactionCorrection").selection().named("surface");
 m.component("c").physics("plas").feature("metalReactionCorrection").set("weakExpression","V_lm*test(plas.metal.V0e_1)/(2*pi*max(r,1e-12[m]))");
 m.sol("sol1").feature("v1").feature("c_V_lm").set("scaleval","1e-10");
 m.param().set("traceIota","2");
 for(String[] row:m.component("c").physics("plas").feature("metalReactionCorrection").featureInfo("info").getInfoTable("Weak"))System.out.println(java.util.Arrays.toString(row));
 m.save(a[1]);System.out.println("AXISYMMETRIC METAL CHARGE CORRECTED; requires charge audit");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
