import com.comsol.model.*;
import com.comsol.model.util.*;
public class InspectMelt {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 for(String p:m.component("c").physics("melt").prop().tags())InspectFeatures.dump(m.component("c").physics("melt").prop(p),p);
 InspectFeatures.dump(m.component("c").physics("melt").feature("drag"),"drag");
 InspectFeatures.dump(m.component("c").common("meshSurf"),"meshSurf");
 System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}
}
