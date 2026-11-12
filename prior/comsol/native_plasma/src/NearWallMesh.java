import com.comsol.model.*;
import com.comsol.model.util.*;
public class NearWallMesh {public static void apply(Model m){
 m.component("c").mesh("mesh").feature("sz1").set("hmax","1[um]");
 m.component("c").mesh("mesh").feature("sz2").set("hmax","0.4[um]");
 m.component("c").mesh("mesh").feature("edge").feature("size").set("hmax","0.25[um]");
 m.component("c").mesh("mesh").feature("edge").feature("size").set("hmin","0.05[um]");
 m.component("c").mesh("mesh").run();
 m.component("c").common("meshSurf").set("prescribedMeshDisplacement",new String[]{"0","0","etaSurface*1[um]-Z"});
 }
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);apply(m);
 m.save(a[1]);System.out.println("NEAR WALL MESH READY");System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}}
