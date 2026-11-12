import com.comsol.model.*;
import com.comsol.model.util.*;
public class InspectHeat {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").physics("ht").create("fluid1","FluidHeatTransferModel",2);m.component("c").physics("ht").feature("fluid1").selection().named("g_cu_dom");
 InspectFeatures.dump(m.component("c").physics("ht").feature("fluid1"),"fluid1");
 for(String f:m.component("c").physics("ht").feature("fluid1").feature().tags())InspectFeatures.dump(m.component("c").physics("ht").feature("fluid1").feature(f),f);
 for(String p:m.component("c").physics("ht").prop().tags())if(p.toLowerCase().contains("stabil"))InspectFeatures.dump(m.component("c").physics("ht").prop(p),p);
 System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}}

