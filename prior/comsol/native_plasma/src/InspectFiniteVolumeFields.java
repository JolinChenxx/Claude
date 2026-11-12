import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectFiniteVolumeFields {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 for(String f:m.component("c").physics("plas").feature().tags()){
 if(f.equals("Cu")||f.equals("pes1"))for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Shape"))System.out.println("SHAPE "+f+" "+Arrays.toString(row));
 for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Expression"))if(row.length>2&&row[1]!=null&&row[1].matches("plas\\.(Er|Ez|Etr|Etz|E_down|Cu\\..*post.*|.*flux.*Cu|Jelr|Jelz|Jenlr|Jenlz|metal\\.nD|metal\\.nJd|nD|D.*)"))System.out.println(f+" "+Arrays.toString(row));
 }
 for(String[] row:m.component("c").physics("plas").featureInfo("info").getInfoTable("Expression"))if(Arrays.toString(row).matches(".*(plas.Er|plas.Ez|plas.Dr|plas.Cu.*post).*"))System.out.println("ROOT "+Arrays.toString(row));
 System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
