import com.comsol.model.*;
import com.comsol.model.util.*;
public class InspectSolver{
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 for(String f:m.sol("sol1").feature("t1").feature().tags()){
  System.out.println("SOLVER "+f+" "+m.sol("sol1").feature("t1").feature(f).getType());
  if(m.sol("sol1").feature("t1").feature(f).getType().equals("Segregated"))for(String g:m.sol("sol1").feature("t1").feature(f).feature().tags())InspectFeatures.dump(m.sol("sol1").feature("t1").feature(f).feature(g),g);
 }
 m.sol("sol1").feature("t1").create("probeFC","FullyCoupled");InspectFeatures.dump(m.sol("sol1").feature("t1").feature("probeFC"),"FullyCoupled");
 System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}
}
