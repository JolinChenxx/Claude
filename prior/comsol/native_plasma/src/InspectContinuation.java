import com.comsol.model.*;import com.comsol.model.util.*;
public class InspectContinuation {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("audit",a[0]);
 InspectFeatures.dump(m.sol("sol1").feature("t1"),"time");
 InspectFeatures.dump(m.sol("sol1").feature("t1").feature("coupledAll"),"coupled");
 for(String f:m.sol("sol1").feature("v1").feature().tags())InspectFeatures.dump(m.sol("sol1").feature("v1").feature(f),f);
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
