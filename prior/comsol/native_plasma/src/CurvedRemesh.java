import com.comsol.model.*;import com.comsol.model.util.*;
/** Catch curved-element inversion missed by the relative-volume-only trigger. */
public class CurvedRemesh {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
m.sol("sol1").feature("t1").feature("remesh").set("stopexpr","min(c.spatial.relVolMin/0.35,c.spatial.minqual/0.0001)");m.sol("sol1").feature("t1").feature("remesh").set("stopval","1");
m.save(a[1]);System.out.println("CURVED ELEMENT AND VOLUME REMESH TRIGGER");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}

