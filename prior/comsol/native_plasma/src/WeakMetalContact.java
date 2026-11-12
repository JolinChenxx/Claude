import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class WeakMetalContact {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);m.component("c").physics("plas").feature("metal").set("constraintOptions","weakConstraints");
 // A boundary multiplier has no normal spatial derivative. Accumulate its
 // total charge on the moving surface using the surface transport theorem.
 m.component("c").variable("boundv").set("surfaceAreaRate","dtang(d(r,TIME),r)+dtang(d(z,TIME),z)+d(r,TIME)/max(r,1e-12[m])");
 m.component("c").physics("plas").feature("metal").featureInfo("info").set("plas.I_1",new String[]{"-plas.metal.int(2*pi*r*plas.d*(d(V_lm,TIME)+V_lm*surfaceAreaRate+Iwall))"});
 for(String table:new String[]{"Weak","Constraint","Shape","Expression"})System.out.println(table+" "+Arrays.deepToString(m.component("c").physics("plas").feature("metal").featureInfo("info").getInfoTable(table)));
 for(String s:m.sol().tags())m.sol().remove(s);m.study("std").createAutoSequences("sol");SolverSettings.apply(m);
 m.study("std").feature("time").set("rtol",1e-4);m.sol("sol1").feature("t1").set("rtol",1e-4);
 m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxorder",2);
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
 String s=f.equals("c_ODE1")?"0.001":f.equals("c_V_lm")?"1e-5":f.endsWith("Tg")||f.endsWith("T")?"1000":f.endsWith("umelt")?"100":f.contains("disp")?"1e-5":f.equals("c_etaSurface")?"1":f.contains("V0")?"1":null;
 if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}System.out.println("VARIABLE "+f);}
 AddRemesh.apply(m);m.sol("sol1").feature("t1").feature("remesh").set("stopexpr","c.spatial.relVolMin");m.sol("sol1").feature("t1").feature("remesh").set("stopval","0.35");m.sol("sol1").feature("t1").feature("remesh").set("consistentremesh",true);
 m.save(a[1]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
