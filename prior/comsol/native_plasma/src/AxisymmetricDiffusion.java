import com.comsol.model.*;
import com.comsol.model.util.*;
public class AxisymmetricDiffusion {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 // No swirl is modeled. Native isotropic stabilization nevertheless
 // references this azimuthal convective velocity in COMSOL 6.4.
 m.component("c").variable("gasv").set("plas.ucphi","0[m/s]");
 m.component("c").variable("gasv").set("plas.ucr","urP");m.component("c").variable("gasv").set("plas.ucz","uzP");
 m.save(a[1]);System.out.println("AXISYMMETRIC DIFFUSION READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
