import com.comsol.model.*;
import com.comsol.model.util.*;
public class PlasmaTransport {
 public static void apply(Model m){
  for(String p:new String[]{"StreamlineDiffusionElectronsLog","StreamlineDiffusionIonsLog","CrosswindDiffusionElectronsLog","CrosswindDiffusionIonsLog"})m.component("c").physics("plas").prop("ConsistentStabilization").set(p,true);
 }
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);apply(m);m.save(a[1]);System.out.println("LOG TRANSPORT READY");System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}
}
