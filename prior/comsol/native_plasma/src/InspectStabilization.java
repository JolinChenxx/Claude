import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectStabilization {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").physics("plas").prop("Stabilization").set("ReactionSourceStabilization",true);
 for(String f:new String[]{"O_1m","Cu","pes1"}){for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Expression"))if(Arrays.toString(row).matches(".*(iota|zeta|stabil|R_wO_1m).*"))System.out.println(Arrays.toString(row));System.out.println(f+" "+Arrays.deepToString(m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Weak")));}
 System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}}
