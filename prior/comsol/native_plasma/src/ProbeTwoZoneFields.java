import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
public class ProbeTwoZoneFields {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("fields",a[0]);
  for(String f:new String[]{"Cu","O2","pes1"}){
   Object feature=m.component("outer").physics("plas2").feature(f);
   for(java.lang.reflect.Method mm:feature.getClass().getMethods())if(mm.getName().toLowerCase().contains("field"))System.out.println("FIELD METHOD "+f+" "+mm);
   for(String table:new String[]{"Shape","ShapeFunction","DependentVariable"})try{System.out.println(f+" "+table+" "+Arrays.deepToString(m.component("outer").physics("plas2").feature(f).featureInfo("info").getInfoTable(table)));}catch(Exception e){System.out.println(table+" unavailable");}
  }
  for(String f:m.component("outer").physics("plas2").field().tags())System.out.println("PHYSICS FIELD "+f+" "+m.component("outer").physics("plas2").field(f).field());
  System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
