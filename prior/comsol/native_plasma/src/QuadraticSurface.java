import com.comsol.model.*;import com.comsol.model.util.*;
public class QuadraticSurface {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
m.component("c").physics("surfaceODE").prop("ShapeProperty").set("order",2);
System.out.println("OLD GEOMETRY ORDER "+m.component("c").sorder());m.component("c").sorder("quadratic");
m.save(a[1]);System.out.println("QUADRATIC SURFACE KINEMATICS READY");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
