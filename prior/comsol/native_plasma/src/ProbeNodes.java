import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class ProbeNodes {
 public static void main(String[] args){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.create("p");m.component().create("c",true);m.component("c").geom().create("g",2);m.component("c").geom("g").axisymmetric(true);m.component("c").geom("g").create("r","Rectangle");m.component("c").geom("g").run();
 m.component("c").physics().create("liq","LaminarFlow","g");m.component("c").physics("liq").create("wc","WeakContribution",1);m.component("c").physics("liq").feature("wc").selection().set(2);
 InspectFeatures.dump(m.component("c").physics("liq").feature("wc"),"weak");
 m.component("c").physics().create("plas","ColdPlasma","g");
 for(String[] n:new String[][]{{"mc","MetalContact"},{"ei","ElectronImpactReaction"}}){m.component("c").physics("plas").create(n[0],n[1],n[0].equals("ei")?2:1);if(!n[0].equals("ei"))m.component("c").physics("plas").feature(n[0]).selection().all();InspectFeatures.dump(m.component("c").physics("plas").feature(n[0]),n[1]);}
 System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
