import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectReady {
 public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("p",a[0]);
 for(String fn:new String[]{"pes1","e","O_1m"}){InspectFeatures.dump(m.component("c").physics("plas").feature(fn),fn);for(String[] row:m.component("c").physics("plas").feature(fn).featureInfo("info").getInfoTable("Expression"))if(Arrays.toString(row).matches(".*(c_we|w_e|n_we|M_we|plas.Re|plas.Sen|plas.scharge).*"))System.out.println(fn+" "+Arrays.toString(row));}
 for(String pn:m.component("c").physics("plas").prop().tags())InspectFeatures.dump(m.component("c").physics("plas").prop(pn),pn);
 System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
