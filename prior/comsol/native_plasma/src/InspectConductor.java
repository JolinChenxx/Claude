import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectConductor {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String f=a.length>1?a[1]:"floatCharge";
 InspectFeatures.dump(m.component("c").physics("plas").feature(f),f);
 System.out.println("SELECTION "+Arrays.toString(m.component("c").physics("plas").feature(f).selection().entities()));
 for(String table:new String[]{"Weak","Constraint","Shape","Expression"})System.out.println(table+" "+Arrays.deepToString(m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable(table)));
 for(String s:m.sol("sol1").feature("v1").feature().tags())System.out.println("SOLVER VARIABLE "+s);
 System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
