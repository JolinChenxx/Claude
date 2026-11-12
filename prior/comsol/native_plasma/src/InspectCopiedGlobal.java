import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
public class InspectCopiedGlobal {
 public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("copied",a[0]);if(!Arrays.asList(m.component().tags()).contains("shell"))m.component().copy("shell","c");
 System.out.println("PHYSICS "+Arrays.toString(m.component("shell").physics().tags()));
 System.out.println("VARIABLE GROUPS "+Arrays.toString(m.component("shell").variable().tags()));
 for(String n:new String[]{"Vp","Tg","plas.wCu","nCuTotal","rhoP","Msource","TeK"})System.out.println(n+"="+m.component("shell").variable("gasv1").get(n));
 System.out.println("GE "+Arrays.toString(m.component("shell").physics("ge2").feature("ge1").getStringArray("name")));
 System.out.println("WEAK "+Arrays.deepToString(m.component("shell").physics("plas2").feature("pes1").featureInfo("info").getInfoTable("Weak")));
 System.out.println("CU FEATURES "+Arrays.toString(m.component("shell").physics("plas2").feature().tags()));
 for(String[] row:m.component("shell").physics("plas2").feature("Cu").featureInfo("info").getInfoTable("Expression"))if(Arrays.toString(row).matches(".*(R_wCu|wCu|n_wCu).*"))System.out.println("COPIED_CU "+Arrays.toString(row));
 System.out.println("FIELDS "+Arrays.toString(m.component("shell").physics("plas2").field().tags()));
 m.save(a[1]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
