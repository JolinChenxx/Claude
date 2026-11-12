import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectBoundaryQuadrature {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("q",a[0]);
 for(String[] pair:new String[][]{{"plas","feed"},{"flow","feed"},{"ht","laser"}}){
  String p=pair[0],f=pair[1];System.out.println("NODE "+p+"/"+f);
  for(String n:m.component("c").physics(p).feature(f).properties())if(n.toLowerCase().matches(".*(quad|order|integr|weak).*"))System.out.println("PROPERTY "+n);
  for(String[] row:m.component("c").physics(p).feature(f).featureInfo("info").getInfoTable("Weak"))System.out.println(Arrays.toString(row));
 }
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
