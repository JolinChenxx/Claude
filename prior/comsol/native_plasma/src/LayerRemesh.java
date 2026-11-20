import com.comsol.model.*;
import com.comsol.model.util.*;
/** Thin layers are intentionally anisotropic; monitor deformation instead. */
public class LayerRemesh {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.sol("sol1").feature("t1").feature("remesh").set("stopexpr","c.spatial.relVolMin");
 m.sol("sol1").feature("t1").feature("remesh").set("stopval","0.35");
 m.save(a[1]);System.out.println("RELATIVE VOLUME REMESH READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
