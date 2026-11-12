import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectLogStabilization {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("stab",a[0]);
 for(String s:new String[]{"StreamlineDiffusionIonsLog","CrosswindDiffusionIonsLog","StreamlineDiffusionElectronsLog","CrosswindDiffusionElectronsLog"})m.component("c").physics("plas").prop("ConsistentStabilization").set(s,true);
 InspectFeatures.dump(m.component("c").physics("plas").prop("ConsistentStabilization"),"ConsistentStabilization");
 for(String f:m.component("c").physics("plas").feature().tags()){
  if(f.equals("Cu")||f.equals("Cu_1p")||f.equals("pes1"))for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Weak"))System.out.println(f+" WEAK "+Arrays.toString(row));
  for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Expression"))if(Arrays.toString(row).matches(".*(resid|Residual|tau_|tauS|delta|sd_|cd_|stab).*"))System.out.println(f+" EXPR "+Arrays.toString(row));
 }
 for(String table:new String[]{"Weak","Expression"})for(String[] row:m.component("c").physics("plas").featureInfo("info").getInfoTable(table))System.out.println("ROOT "+table+" "+Arrays.toString(row));
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
