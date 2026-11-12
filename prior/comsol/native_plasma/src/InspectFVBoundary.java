import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectFVBoundary {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 for(String f:m.component("c").physics("plas").feature().tags())for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Expression"))if(row.length>2&&row[1]!=null&&row[1].matches("plas\\.(nJn_w.*|nJi_w.*|N2_1p\\.Ji_post.*|Cu\\.(.*bnd.*|.*inl.*)|.*wall|bc_total_fen|Jenl_post.*)"))System.out.println(f+" "+Arrays.toString(row));
 for(String[] row:m.component("c").physics("plas").featureInfo("info").getInfoTable("Expression"))if(row.length>2&&row[1]!=null&&row[1].matches("plas\\.(nJn_w.*|nJi_w.*|.*influx.*|.*_bnd.*)"))System.out.println("ROOT "+Arrays.toString(row));
 System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
