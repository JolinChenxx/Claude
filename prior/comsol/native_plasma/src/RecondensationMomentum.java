import com.comsol.model.*;import com.comsol.model.util.*;
/** Return the gas momentum carried by recondensing Cu to the liquid target.
 * Previous net-mdot*u_liquid omitted return_mass*(u_gas-u_liquid).
 */
public class RecondensationMomentum {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
String[] g=m.component("c").physics("flow").feature("feed").getStringArray("g");
g[1]+="-axisWeight*mdotReturn*(urP-urCu)/(rho0*u0)";g[2]+="-axisWeight*mdotReturn*(uzP-uzCu)/(rho0*u0)";m.component("c").physics("flow").feature("feed").set("g",g);
String[] f=m.component("c").physics("melt").feature("surf").getStringArray("Fbnd");f[0]+="+mdotReturn*(urP-urCu)";f[2]+="+mdotReturn*(uzP-uzCu)";m.component("c").physics("melt").feature("surf").set("Fbnd",f);
m.save(a[1]);System.out.println("RECONDENSATION MOMENTUM EXCHANGED WITH TARGET");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
