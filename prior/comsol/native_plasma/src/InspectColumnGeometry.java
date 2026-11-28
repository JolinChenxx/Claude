import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
public class InspectColumnGeometry {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("geomAudit",a[0]);
 System.out.println("GEOMETRY FEATURES "+Arrays.toString(m.component("c").geom("g").feature().tags()));
 for(String f:new String[]{"cu","gas","core","mid","meltcore","wallstrip","fullColumn","fin"})if(Arrays.asList(m.component("c").geom("g").feature().tags()).contains(f))InspectFeatures.dump(m.component("c").geom("g").feature(f),f);
 m.component("c").selection().create("allEdges","Explicit");m.component("c").selection("allEdges").geom("g",1);m.component("c").selection("allEdges").all();
 for(int edge:m.component("c").selection("allEdges").entities()){
  m.component("c").geom("g").measureFinal().selection().geom(1);m.component("c").geom("g").measureFinal().selection().set(new int[]{edge});
  System.out.println("EDGE "+edge+" "+Arrays.toString(m.component("c").geom("g").measureFinal().getBoundingBox()));
 }
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
