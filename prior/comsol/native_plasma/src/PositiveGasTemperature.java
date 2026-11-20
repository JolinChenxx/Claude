import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

/** Use one positive gas temperature in pressure, enthalpy and total-energy closure.
 * The inherited max(Tg,150 K) pressure floor could keep doing pressure work after
 * the conserved thermal energy had cooled below 150 K. A logarithmic unknown
 * enforces positivity without adding energy or clamping the physical pressure.
 */
public class PositiveGasTemperature {
 public static void apply(Model m){
  m.component("c").physics("temp").field("dimensionless").field("logTgas");
  m.component("c").physics("temp").field("dimensionless").component(new String[]{"logTgas"});
  m.component("c").physics("temp").feature("init1").set("logTgas","log(Tamb/1[K])");
  m.component("c").variable("gasv").set("Tg","1[K]*exp(logTgas)");
  m.component("c").variable("gasv").set("Tgas","Tg");
 }
 public static void solver(Model m){
  for(String s:m.sol().tags())m.sol().remove(s);
  String times="range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)";
  m.study("std").feature("time").set("tlist",times);m.study("std").feature("time").set("rtol",0.001);
  m.study("std").createAutoSequences("sol");
  for(String f:m.sol("sol1").feature("t1").feature().tags()){
   String type=m.sol("sol1").feature("t1").feature(f).getType();
   if(type.equals("Segregated")||type.equals("FullyCoupled"))m.sol("sol1").feature("t1").feature().remove(f);
  }
  m.sol("sol1").feature("t1").create("directAll","Direct");
  m.sol("sol1").feature("t1").feature("directAll").set("linsolver","pardiso");
  m.sol("sol1").feature("t1").create("coupledAll","FullyCoupled");
  m.sol("sol1").feature("t1").feature("coupledAll").set("linsolver","directAll");
  m.sol("sol1").feature("t1").feature("coupledAll").set("dtech","auto");
  m.sol("sol1").feature("t1").feature("coupledAll").set("maxiter",15);
  m.sol("sol1").feature("t1").feature("coupledAll").set("jtech","onevery");
  m.sol("sol1").feature("t1").set("tlist",times);m.sol("sol1").feature("t1").set("rtol",0.001);
  m.sol("sol1").feature("t1").set("maxorder",2);
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");
  m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
  m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("v1").set("initmethod","init");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String s=f.equals("c_V_lm")?"1e-10":f.equals("c_T")?"1000":f.equals("c_logTgas")?"1":f.equals("c_V")||f.contains("V0")?"1":f.equals("c_Ne")||f.equals("c_En")||f.endsWith("_W")||f.startsWith("c_q")||f.startsWith("c_opt")||f.startsWith("c_ODE")?"1":null;
   if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}
  }
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("positive",a[0]);
  apply(m);m.param().set("tEnd",a.length>2?a[2]:"203[ns]");solver(m);
  m.label("Route 1: consistent positive gas temperature, native Plasma");m.save(a[1]);
  System.out.println("POSITIVE GAS TEMPERATURE READY: pressure/enthalpy/storage share Tg; no floor energy source");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
