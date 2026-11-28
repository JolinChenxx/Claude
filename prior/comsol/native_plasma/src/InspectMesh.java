import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectMesh{
public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
for(String fn:m.component("c").common().tags()){
System.out.println("COMMON "+fn+" SELECTION "+Arrays.toString(m.component("c").common(fn).selection().entities()));
InspectFeatures.dump(m.component("c").common(fn),fn);
for(String tab:new String[]{"Weak","Constraint"})try{System.out.println(tab+" "+Arrays.deepToString(m.component("c").common(fn).featureInfo("info").getInfoTable(tab)));}catch(Exception e){System.out.println(e);}}
if(Arrays.asList(m.component("c").physics().tags()).contains("surfaceODE")){
 InspectFeatures.dump(m.component("c").physics("surfaceODE").prop("ShapeProperty"),"surfaceODE_shape");
 System.out.println("ODE WEAK "+Arrays.deepToString(m.component("c").physics("surfaceODE").feature("dode1").featureInfo("info").getInfoTable("Weak")));
}
System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}
}
