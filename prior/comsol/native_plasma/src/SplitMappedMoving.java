import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Solver-only split on a saved positive mapped moving grid.
 * Plasma, energy and target heat remain coupled. Liquid velocity, pressure and
 * interface/mesh are a second block, iterated to the same time-step tolerance.
 */
public class SplitMappedMoving {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("splitMoving",a[0]);
  ArrayList<String> bulk=new ArrayList<>(),moving=new ArrayList<>();
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   if(f.equals("c_umelt")||f.equals("c_p")||f.equals("c_etaSurface")||f.startsWith("c_spatial_"))moving.add(f);else bulk.add(f);
  }
  System.out.println("BULK "+bulk);System.out.println("MOVING "+moving);
  m.sol("sol1").feature("t1").feature().remove("coupledAll");
  m.sol("sol1").feature("t1").create("splitMoving","Segregated");
  for(String f:m.sol("sol1").feature("t1").feature("splitMoving").feature().tags())m.sol("sol1").feature("t1").feature("splitMoving").feature().remove(f);
  String[][] groups={bulk.toArray(new String[0]),moving.toArray(new String[0])};
  for(int k=0;k<groups.length;k++){
   String tag="block"+k;m.sol("sol1").feature("t1").feature("splitMoving").create(tag,"SegregatedStep");
   m.sol("sol1").feature("t1").feature("splitMoving").feature(tag).set("segvar",groups[k]);
   m.sol("sol1").feature("t1").feature("splitMoving").feature(tag).set("linsolver","directAll");
   m.sol("sol1").feature("t1").feature("splitMoving").feature(tag).set("subdamp",1);
   m.sol("sol1").feature("t1").feature("splitMoving").feature(tag).set("subjtech","minimal");
   System.out.println(tag+" properties "+Arrays.toString(m.sol("sol1").feature("t1").feature("splitMoving").feature(tag).properties()));
  }
  m.sol("sol1").feature("t1").feature("splitMoving").set("maxsegiter",15);
  m.sol("sol1").feature("t1").feature("splitMoving").set("segstabacc","none");
  m.label("Mapped moving grid; solver-only plasma/thermal and liquid/interface block iteration from saved state");m.save(a[1]);System.out.println("SPLIT MOVING SOLVER READY; equations, geometry and tolerances unchanged");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
