import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Namespace/assembly probe only; uncoupled duplicate outer target is not a model. */
public class PrepareTwoZoneProbe {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("twoZoneProbe",a[0]);for(String s:m.sol().tags())m.sol().remove(s);
  AddOuterComponent.apply(m);
  for(String tag:new String[]{"logelectrondensity","logelectronenergydensity","electricpotential"})System.out.println("OUTER FIELD "+tag+" "+m.component("outer").physics("plas2").field(tag).field());
  m.study("std").feature("time").set("tlist","range(0,.5[ns],2[ns])");m.study("std").createAutoSequences("sol");
  for(String n:new String[]{"pes1","Cu","O2"})for(String[] row:m.component("outer").physics("plas2").feature(n).featureInfo("info").getInfoTable("Expression"))if(row.length>2&&(row[1].contains("wtemp")||row[1].contains("R_wCu")||row[1].endsWith(".en")))System.out.println("OUTER VAR "+Arrays.toString(row));
  System.out.println("SOLVER FIELDS "+Arrays.toString(m.sol("sol1").feature("v1").feature().tags()));
  m.label("Uncoupled two-component namespace probe; not a physical simulation");m.save(a[1]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
