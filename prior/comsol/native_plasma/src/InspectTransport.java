import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectTransport {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 for(String f:new String[]{"pes1","Cu","O2","N2","wall","feed","metal","out","eout"}){
  System.out.println("FEATURE "+f);
  for(String table:new String[]{"Expression","Weak"})for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable(table))System.out.println(table+" "+Arrays.toString(row));
 }
 System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
