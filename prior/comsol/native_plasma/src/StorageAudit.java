import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Check the actual projected density inventory, positivity, and charge. */
public class StorageAudit {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("storageAudit",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 for(String op:new String[]{"saMin","saMax"}){m.component("c").cpl().create(op,op.equals("saMin")?"Minimum":"Maximum");m.component("c").cpl(op).selection().named("g_gas_dom");m.component("c").cpl(op).set("opname",op);}
 ArrayList<String> ex=new ArrayList<>();String charge="-storeElectron*storeElectronScale",mass="0[kg/m^3]";
 for(String n:Chemistry.NAMES){
  String v="store"+n;ex.add("c.saMin(c."+v+")");ex.add("c.saMax(abs(c."+v+"-c.plas.rho_w"+n+"/rho0))");
  ex.add("c.intGas(c."+v+"*rho0)");ex.add("c.intGas(c.plas.rho_w"+n+")");
  if(n.endsWith("_1p"))charge+="+"+v+"*rho0*N_A_const/plas.M_w"+n;
  if(n.endsWith("_1m"))charge+="-"+v+"*rho0*N_A_const/plas.M_w"+n;
  mass+="+rho0*"+v;
 }
 m.component("c").variable("gasv").set("saCharge","e_const*("+charge+")");m.component("c").variable("gasv").set("saMass",mass);
 for(String v:new String[]{"storeElectron","storeEnergy"})ex.add("c.saMin(c."+v+")");
 ex.add("c.intGas(c.saCharge)+c.qmetal*1[pC]");ex.add("c.intGas(c.plas.scharge)+c.qmetal*1[pC]");
 ex.add("c.intGas(c.saMass)");ex.add("c.intGas(c.rhoP)");
 m.result().numerical().create("sa","EvalGlobal");m.result().numerical("sa").set("data","dset1");m.result().numerical("sa").set("expr",ex.toArray(new String[0]));
 m.sol(saved).updateSolution();ExportNative.csv(a[1],ex.toArray(new String[0]),m.sol(saved).getPVals(),m.result().numerical("sa").getReal());System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
