import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Inspect native volume-averaged Plasma equations before reduced coupling. */
public class InspectGlobalPlasma {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.create("globalProbe");
 m.component().create("c",true);m.component("c").geom().create("g",2);m.component("c").geom("g").axisymmetric(true);
 m.component("c").geom("g").create("box","Rectangle");m.component("c").geom("g").feature("box").set("size",new String[]{"11[um]","11[um]"});m.component("c").geom("g").run();
 m.component("c").mesh().create("mesh");m.component("c").physics().create("plas","ColdPlasma","g");
 m.component("c").physics("plas").prop("DiffusionModel").set("DiffusionModel","GlobalModel");
 m.component("c").physics("plas").prop("Reactor").set("ReactorType","ClosedReactor");
 Chemistry.apply(m,a[0]);
 for(String f:new String[]{"pes1","N2","Cu","Cu_1p","Cuion"}){
  InspectFeatures.dump(m.component("c").physics("plas").feature(f),f);
  for(String tab:new String[]{"Expression","Weak","Constraint"})
   for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable(tab))System.out.println(f+" "+tab+" "+Arrays.toString(row));
 }
 for(String f:m.component("c").physics("plas").feature().tags())if(f.startsWith("g")||f.startsWith("init")||f.startsWith("vol"))System.out.println("FEATURE "+f+" "+m.component("c").physics("plas").feature(f).getType());
 m.save(a[1]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
