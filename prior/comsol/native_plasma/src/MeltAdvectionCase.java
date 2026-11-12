import com.comsol.model.*;import com.comsol.model.util.*;
/** Restore native consistent advection stabilization in the fast moving melt.
 * P2/P1 remains in use. Surface samples showed alternating +/- kilometre/s
 * velocities at adjacent elements; the flow is no longer creeping flow. */
public class MeltAdvectionCase {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
for(String n:new String[]{"StreamlineDiffusion","CrosswindDiffusion"})m.component("c").physics("melt").prop("ConsistentStabilization").set(n,true);
m.save(a[1]);System.out.println("NATIVE MELT ADVECTION STABILIZATION RESTORED");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
