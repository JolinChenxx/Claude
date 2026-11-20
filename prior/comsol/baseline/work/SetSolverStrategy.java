import com.comsol.model.*;import com.comsol.model.util.*;
/** Change nonlinear iteration strategy only; preserve all discretization settings. */
public class SetSolverStrategy {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);
 m.sol("sol1").feature("t1").feature("fc1").set("jtech",a[2]);
 m.sol("sol1").feature("t1").feature("fc1").set("dtech",a[3]);
 if(a.length>4)m.sol("sol1").feature("t1").feature("fc1").set("damp",Double.parseDouble(a[4]));
 m.sol("sol1").feature("t1").feature("fc1").set("maxiter",25);
 m.save(a[1]);m.save(a[1].replace(".mph","_model.java"),"java");
 ModelUtil.clear();System.out.println("STRATEGY_SAVED "+a[1]);System.exit(0);
}catch(Exception e){e.printStackTrace();System.exit(1);}}}
