import com.comsol.model.*;import com.comsol.model.util.*;
/** Same coupled equations, compact thermal mesh; compare source histories
 * against the original 0.5 um heat mesh before accepting this discretization. */
public class PrepareCompactThermalMesh {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("compactThermal",a[0]);
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  m.component("c").mesh("mesh").feature("hotSize").set("hmax",a.length>2?a[2]+"[um]":"1[um]");
  m.component("c").mesh("mesh").run();
  m.param().set("tEnd","1000[ns]");m.study("std").feature("time").set("tlist","range(0,1[ns],tEnd)");
  m.sol("sol1").feature("t1").set("tlist","range(0,1[ns],tEnd)");
  m.sol("sol1").feature("v1").set("initmethod","init");
  m.label("Compact copper heat mesh, unchanged native Plasma and two-way thermal equations; resolution sensitivity pending");
  m.save(a[1]);System.out.println("COMPACT THERMAL MESH READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
