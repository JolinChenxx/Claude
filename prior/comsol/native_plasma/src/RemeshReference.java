import com.comsol.model.*;
import com.comsol.model.util.*;
public class RemeshReference {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").common("meshSurf").set("prescribedMeshDisplacement",new String[]{"0","0","etaSurface*1[um]-Z"});
 m.save(a[1]);System.out.println("REFERENCE HEIGHT FIXED");System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}}
