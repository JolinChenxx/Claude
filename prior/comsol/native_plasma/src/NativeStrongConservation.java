import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Native heavy-species strong form with the missing compression term.
 * For rho_i=rho0*exp(W_i): rho_i*(W_it+u.grad(W_i))+rho_i*div(u)
 * equals d(rho_i)/dt+div(rho_i*u). Putting compression into the native
 * source lets native consistent stabilization see the complete equation.
 * Same continuum conservation laws; requires actual integral validation.
 */
public class NativeStrongConservation {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("nativeStrong",a[0]);
 for(String n:Chemistry.NAMES){String rate=ConservativeTrace.rate(m,n);
  PartialMassCoordinates.put(m,n,"plas.R_w"+n,"("+rate+")-plas.rho_w"+n+"*divUMass/plas.M_w"+n);
 }
 m.component("c").physics("plas").feature("heavyConservative").active(false);
 m.component("c").physics("plas").feature("heavyFarConvection").active(false);
 // The native strong convection form requires a diffusive wall flux equal to
 // prescribed total influx plus the outward advective flux. The conservative
 // weak-flux predecessor had deliberately disabled this native contribution.
 m.component("c").physics("plas").feature("feed").set("IncludeConvection",true);
 for(int q:new int[]{2,8})m.component("c").physics("plas").feature().remove("heavyFeedQuadrature"+q);
 MatchedHeavyFlux.apply(m);
 for(String s:new String[]{"StreamlineDiffusionIonsLog","CrosswindDiffusionIonsLog"})m.component("c").physics("plas").prop("ConsistentStabilization").set(s,true);
 NeutralCollisionCount.apply(m);PlasmaDomainQuadrature.apply(m,8);
 m.param().set("tEnd","123[ns]");PositiveGasTemperature.solver(m);
 m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<80[ns],1[ns],0.05[ns])");
 m.sol("sol1").feature("t1").set("tout","tstepsclosest");
 m.label("Native conservative strong-form heavy species with consistent stabilization, fresh diagnostic");
 m.save(a[1]);System.out.println("NATIVE COMPLETE STRONG-FORM DIAGNOSTIC READY; mass and charge checks required");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
