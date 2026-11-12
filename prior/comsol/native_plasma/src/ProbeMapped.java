import com.comsol.model.*;
import com.comsol.model.util.*;
public class ProbeMapped {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.create("map");m.component().create("c",true);m.component("c").geom().create("g",2);
 m.component("c").geom("g").create("rect","Rectangle");m.component("c").geom("g").run();m.component("c").mesh().create("mesh");
 m.component("c").mesh("mesh").create("map","Map");m.component("c").mesh("mesh").feature("map").create("dist","Distribution");
 InspectFeatures.dump(m.component("c").mesh("mesh").feature("map"),"MAP");
 InspectFeatures.dump(m.component("c").mesh("mesh").feature("map").feature("dist"),"DISTRIBUTION");
 System.exit(0);
}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
