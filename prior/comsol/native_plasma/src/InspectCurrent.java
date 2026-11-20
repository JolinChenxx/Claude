import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectCurrent {public static void main(String[] a){try{
ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
for(String f:new String[]{"wall","wallQuadrature2","wallQuadrature8","pes1"}){
System.out.println("FEATURE "+f);InspectFeatures.dump(m.component("c").physics("plas").feature(f),f);try{for(String t:new String[]{"Weak","Expression"})for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable(t))System.out.println(t+" "+Arrays.toString(row));}catch(Exception e){System.out.println(e);}}
System.out.println("VARS "+Arrays.toString(m.component("c").variable("gasv").varnames()));
for(String n:new String[]{"compositionDen","Dnum","rhoSafe","Tgas","uMassR"})try{System.out.println(n+"="+m.component("c").variable("gasv").get(n));}catch(Exception e){}
System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
