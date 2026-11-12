import com.comsol.model.*;import com.comsol.model.util.*;
/** Give the target coordinate an explicit component-scoped alias for global source integrals. */
public class RepairGridCoordinate {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("gridCoord",a[0]);
  m.component("c").variable("boundv").set("gridTargetRadius","r");
  String[] eq=m.component("c").physics("gridSources").feature("ge1").getStringArray("equation");
  for(int i=0;i<eq.length;i++)eq[i]=eq[i].replaceAll("\\bc\\.r\\b","c.gridTargetRadius");
  m.component("c").physics("gridSources").feature("ge1").set("equation",eq);
  for(String n:m.variable("axialFlux").varnames()){
   String s=m.variable("axialFlux").get(n);if(s.matches("(?s).*\\bc\\.r\\b.*"))m.variable("axialFlux").set(n,s.replaceAll("\\bc\\.r\\b","c.gridTargetRadius"));
  }
  m.save(a[1]);System.out.println("GRID TARGET COORDINATE ALIAS READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
