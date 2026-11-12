import com.comsol.model.*;
import com.comsol.model.util.*;
public class RestartInitialization {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
  m.sol("sol1").feature("t1").set("consistent","bweuler");
  m.sol("sol1").feature("t1").set("bwinitstepfrac","0.1");
  for(String f:new String[]{"meshSurf","meshFixed","meshAxis"})m.component("c").common(f).set("constraintMethod","nodal");
  m.save(a[1]);System.out.println("RESTART INITIALIZATION READY");System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
