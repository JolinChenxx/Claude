import com.comsol.model.*;import com.comsol.model.util.*;
/** Stabilize pressure waves with a viscous momentum flux while leaving a
 * resting material contact undiffused. Viscous work is included in total energy.
 * Mass and energy transport stabilization scales with the material velocity.
 */
public class ContactPreservingHEM{public static void main(String[] a){try{
 ModelUtil.initStandalone(false);ModelUtil.setPreference("security.comsol.allowexternallibraries","on");Model m=ModelUtil.load("contact",a[0]);
 m.func("mixtureEOS").set("path",a[2]);
 m.component("c").variable("state").set("Dnum","0.15*h*sqrt(u^2+1e-8[m^2/s^2])");
 m.component("c").variable("state").set("Dvisc","0.15*h*cs");
 m.component("c").variable("state").set("stressNum","rho*Dvisc*d(u,x)");
 m.component("c").physics("hydro").feature("gfeq1").set("Ga",new String[][]{{"(rhoC*u-Dnum*d(rhoC,x))/rhoS"},{"(rhoA*u-Dnum*d(rhoA,x))/rhoAmb"},{"(rho*u^2+pGas-pAmb-stressNum)/(rhoS*uS)"},{"((Etot+pGas)*u-kMix*d(T,x)-u*stressNum)/(rhoS*eS)-Dnum*d(qE,x)"}});
 m.label("Axial common-pressure HEM; contact-preserving mass flux and conservative viscous work; no Plasma");m.save(a[1]);System.out.println("CONTACT-PRESERVING HEM READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
