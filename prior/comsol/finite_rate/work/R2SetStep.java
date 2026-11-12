import com.comsol.model.*;
import com.comsol.model.util.*;
public class R2SetStep {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);
  m.sol("sol2").feature("t1").set("maxstepconstraintbdf","const");
  m.sol("sol2").feature("t1").set("maxstepbdf",a[2]+"[ns]");
  m.save(a[1]);System.out.println("MAX_STEP_NS="+a[2]);ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
