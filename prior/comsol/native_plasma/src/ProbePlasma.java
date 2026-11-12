import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.Arrays;

public class ProbePlasma {
  public static void main(String[] args) {
    try {
      ModelUtil.initStandalone(false);
      Model m=ModelUtil.create("probe");
      m.component().create("c",true);
      m.component("c").geom().create("g",2);
      m.component("c").geom("g").axisymmetric(true);
      m.component("c").geom("g").create("rect","Rectangle");
      m.component("c").geom("g").run();
      m.component("c").physics().create("plas","Plasma","g");
      System.out.println("NATIVE_PLASMA_CREATED "+Arrays.toString(m.component("c").physics("plas").feature().tags()));
      m.save(args[0]+"/probe_native.java","java");
      ModelUtil.remove("probe");
      for(int i=1;i<args.length;i+=2) {
        Model a=ModelUtil.load("example",args[i]);
        a.save(args[0]+"/"+args[i+1]+".java","java");
        System.out.println("EXPORTED "+args[i+1]);
        ModelUtil.remove("example");
      }
      System.exit(0);
    } catch(Exception e) {e.printStackTrace();System.exit(1);}
  }
}
