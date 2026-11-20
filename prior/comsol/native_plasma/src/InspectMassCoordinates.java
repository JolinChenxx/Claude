import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectMassCoordinates {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("mass",a[0]);
 if(a.length<2){m.component("c").physics("plas").feature("N2").set("FromMassConstraint",false);
 m.component("c").physics("plas").feature("N2").set("x0","0.79");}
 InspectFeatures.dump(m.component("c").physics("plas").prop("TransportSettings"),"TransportSettings");
 for(String ft:m.component("c").physics("plas").feature().tags())for(String[] row:m.component("c").physics("plas").feature(ft).featureInfo("info").getInfoTable("Expression"))if(row.length>2&&"plas.jr_wCu".equals(row[1]))System.out.println("ALLFLUX "+ft+" "+Arrays.toString(row));
 for(String[] row:m.component("c").physics("plas").feature("pes1").featureInfo("info").getInfoTable("Expression"))if(Arrays.toString(row).contains("_wCu")||Arrays.toString(row).contains("_wN2")||Arrays.toString(row).contains("jdr"))System.out.println("PES "+Arrays.toString(row));
 for(String n:new String[]{"N2","Cu","Cu_1p","O2"}){
  for(String table:new String[]{"Weak","Expression"})for(String[] row:m.component("c").physics("plas").feature(n).featureInfo("info").getInfoTable(table))System.out.println(n+" "+table+" "+Arrays.toString(row));
 }
 for(String f:m.component("c").physics("flow").feature().tags()){
  System.out.println("FLOW "+f+" "+m.component("c").physics("flow").feature(f).getType());
  for(String table:new String[]{"Weak","Expression"})for(String[] row:m.component("c").physics("flow").feature(f).featureInfo("info").getInfoTable(table))System.out.println(f+" "+table+" "+Arrays.toString(row));
 }
 InspectFeatures.dump(m.component("c").physics("flow").feature("gfeq1"),"flow gfeq1");
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
