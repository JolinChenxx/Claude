import com.comsol.model.*;
import com.comsol.model.util.*;
public class AddRemesh {
 public static void apply(Model m){
  m.sol("sol1").feature("t1").create("remesh","AutoRemesh");
  m.sol("sol1").feature("t1").feature("remesh").set("autoremeshgeom","g");
  m.sol("sol1").feature("t1").feature("remesh").set("stopcondtype","quality");
  m.sol("sol1").feature("t1").feature("remesh").set("stopexpr","c.spatial.minqual");
  m.sol("sol1").feature("t1").feature("remesh").set("stopval","0.25");
  m.sol("sol1").feature("t1").feature("remesh").set("initialstepremeshactive",true);
  m.sol("sol1").feature("t1").feature("remesh").set("initialstepremesh","0.001[ns]");
 }
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);apply(m);m.save(a[1]);System.out.println("REMESH READY");System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}
}
