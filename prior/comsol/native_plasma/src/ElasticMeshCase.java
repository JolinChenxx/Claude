import com.comsol.model.*;import com.comsol.model.util.*;
/** Preserve the moving thin sheath layers with nonlinear elastic mesh smoothing. */
public class ElasticMeshCase {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);m.component("c").common("deform").set("smoothingType","yeoh");m.save(a[1]);System.out.println("YEOH MOVING MESH READY");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
