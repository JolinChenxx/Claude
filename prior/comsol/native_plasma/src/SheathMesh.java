import com.comsol.model.*;
import com.comsol.model.util.*;
/** Resolve normal electrostatic gradients without isotropic nanometre meshing. */
public class SheathMesh {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").mesh("mesh").create("sheath","BndLayer");m.component("c").mesh("mesh").feature("sheath").selection().named("g_gas_dom");
 m.component("c").mesh("mesh").feature("sheath").create("layers","BndLayerProp");
 m.component("c").mesh("mesh").feature("sheath").feature("layers").selection().named("beam");
 m.component("c").mesh("mesh").feature("sheath").feature("layers").set("inittype","blhmin");
 m.component("c").mesh("mesh").feature("sheath").feature("layers").set("blhmin",a.length>2?a[2]:"10[nm]");
 m.component("c").mesh("mesh").feature("sheath").feature("layers").set("blnlayers",12);
 m.component("c").mesh("mesh").feature("sheath").feature("layers").set("blstretch",1.3);
 m.component("c").mesh("mesh").run();m.save(a[1]);System.out.println("SHEATH MESH READY; wall-layer resolution must be assessed against local Debye length");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
