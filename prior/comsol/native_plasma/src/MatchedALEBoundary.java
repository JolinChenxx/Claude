import com.comsol.model.*;import com.comsol.model.util.*;
/** The ALE flux uses the actual moving mesh velocity, not the unprojected
 * kinematic forcing of the surface ODE. Their mismatch created a false current. */
public class MatchedALEBoundary {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
m.component("c").variable("boundv").set("vnPhysical","meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
m.component("c").variable("boundv").set("vnSurf","d(r,TIME)*ncr+d(z,TIME)*ncz");
m.save(a[1]);System.out.println("ACTUAL MESH NORMAL VELOCITY IN ALL ALE BOUNDARY FLUXES");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
