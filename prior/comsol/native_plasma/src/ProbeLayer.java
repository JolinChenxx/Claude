import com.comsol.model.*;
import com.comsol.model.util.*;
public class ProbeLayer {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.create("p");m.component().create("c",true);m.component("c").geom().create("g",2);m.component("c").geom("g").create("r","Rectangle");m.component("c").geom("g").run();
 m.component("c").mesh().create("mesh");m.component("c").mesh("mesh").create("tri","FreeTri");m.component("c").mesh("mesh").create("bl","BndLayer");
 m.component("c").mesh("mesh").feature("bl").create("blp","BndLayerProp");
 InspectFeatures.dump(m.component("c").mesh("mesh").feature("bl"),"layer");for(String f:m.component("c").mesh("mesh").feature("bl").feature().tags())InspectFeatures.dump(m.component("c").mesh("mesh").feature("bl").feature(f),f);
 m.component("c").physics().create("plas","ColdPlasma","g");for(java.lang.reflect.Method method:m.component("c").physics("plas").feature("pes1").featureInfo("info").getClass().getMethods())if(method.getName().matches(".*[Ii]nteg.*|set|setIndex|setEntry"))System.out.println(method);
 System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
