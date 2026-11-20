import com.comsol.model.*;
import com.comsol.model.util.*;
public class NativeLiquidHeat {
 public static void apply(Model m){
  if(!java.util.Arrays.asList(m.component("c").physics("ht").feature().tags()).contains("fluid1"))m.component("c").physics("ht").create("fluid1","FluidHeatTransferModel",2);
  m.component("c").physics("ht").feature("fluid1").selection().named("g_cu_dom");
  m.component("c").physics("ht").feature("fluid1").set("u",new String[]{"urCu","0","uzCu"});
  m.component("c").physics("ht").feature("fluid1").set("fluidType","gasLiquid");
  m.component("c").physics("ht").feature("advection").set("Q0","0[W/m^3]");
 }
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);apply(m);
 if(a.length>2&&a[2].equals("remesh"))AddRemesh.apply(m);
 m.save(a[1]);System.out.println("NATIVE LIQUID HEAT READY");System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}
}
