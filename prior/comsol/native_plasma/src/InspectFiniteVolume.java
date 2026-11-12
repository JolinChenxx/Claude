import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectFiniteVolume {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").physics("plas").prop("ShapeProperty").set("Formulation","FVMConst");
 for(String n:new String[]{"pes1","dcont1","e","Cu","O_1m","wall","bulkconv","wallConv"}){
  for(String tab:new String[]{"Weak","Expression"})try{for(String[] row:m.component("c").physics("plas").feature(n).featureInfo("info").getInfoTable(tab)){
   String s=Arrays.toString(row);System.out.println(n+" "+tab+" "+s);
  }}catch(Exception e){System.out.println(e);}
 }
 m.save(a[1]);System.out.println("FV AUDIT READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
