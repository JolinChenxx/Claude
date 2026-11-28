import com.comsol.model.*;
import com.comsol.model.util.*;
public class MeltTransport {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
  m.component("c").physics("melt").prop("ConsistentStabilization").set("StreamlineDiffusion",true);
  m.component("c").physics("melt").prop("ConsistentStabilization").set("CrosswindDiffusion",true);
  // P2/P1 is retained; consistent residual stabilization handles the fast jet.
  m.study("std").feature("time").set("tlist","range(0,2[ns],tEnd)");m.sol("sol1").feature("t1").set("tlist","range(0,2[ns],tEnd)");
  m.sol("sol1").feature("v1").set("initmethod","init");m.sol("sol1").feature("t1").set("consistent","bweuler");
  m.sol("sol1").feature("t1").set("bwinitstepfrac","0.1");
  m.sol("sol1").feature("t1").set("initialstepbdf","0.01[ns]");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");
  if(!java.util.Arrays.asList(m.sol("sol1").feature("t1").feature().tags()).contains("remesh"))AddRemesh.apply(m);
  m.sol("sol1").feature("t1").feature("remesh").set("consistentremesh",true);
  m.save(a[1]);System.out.println("MELT TRANSPORT READY");System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
