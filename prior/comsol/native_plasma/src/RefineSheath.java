import com.comsol.model.*;
import com.comsol.model.util.*;
/** Refine only the sheath-normal resolution; keep comparable total thickness. */
public class RefineSheath {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").mesh("mesh").feature("sheath").feature("layers").set("blhmin","2[nm]");
 m.component("c").mesh("mesh").feature("sheath").feature("layers").set("blnlayers",18);
 m.component("c").mesh("mesh").run();
 m.save(a[1]);System.out.println("2 NM SHEATH READY; start from the pre-emission 62 ns saved state");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
