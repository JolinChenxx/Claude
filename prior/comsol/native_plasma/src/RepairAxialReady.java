import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Repair terminal ghost velocity syntax and audit actual native reaction keys. */
public class RepairAxialReady {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("repairAxial",a[0]);
  for(String n:m.variable("axialFlux").varnames()){
   String s=m.variable("axialFlux").get(n);if(s.contains("0[m/s]^2"))m.variable("axialFlux").set(n,s.replace("0[m/s]^2","(0[m/s])^2"));
  }
  for(String f:new String[]{"O2A","CuA","CuA_1p","CuA_2p","pes1"}){
   System.out.println("AUDIT "+f);
   for(String[] row:m.component("cellA").physics("plasmaA").feature(f).featureInfo("info").getInfoTable("Expression"))System.out.println(Arrays.toString(row));
  }
  for(java.lang.reflect.Method mm:m.component("cellA").physics("plasmaA").getClass().getMethods())if(mm.getName().matches(".*(?i:identifier|namespace|name).*"))System.out.println("METHOD "+mm);
  m.save(a[1]);System.out.println("REPAIRED AND AUDITED");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
