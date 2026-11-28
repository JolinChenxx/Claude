import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class TraceAudit {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 m.component("c").cpl().create("maxt","Maximum");m.component("c").cpl("maxt").selection().named("g_gas_dom");m.component("c").cpl("maxt").set("opname","maxt");
 ArrayList<String> ex=new ArrayList<>();String sum="0[mol/(m^3*s)]";for(String n:Chemistry.NAMES)if(n.startsWith("Cu")&&!n.equals("Cu")){sum+="+Snum"+n;ex.add("c.maxt(c.Snum"+n+")");}
 m.component("c").variable("gasv").set("traceCuDepletionRate","N_A_const*("+sum+")/max(plas.n_wCu,1e-100[1/m^3])");ex.add("c.maxt(c.traceCuDepletionRate)");ex.add("c.maxt(abs(c.SneNumerical)/max(c.plas.ne,1[1/m^3]))");m.sol(saved).updateSolution();
 m.result().numerical().create("tr","EvalGlobal");m.result().numerical("tr").set("data","dset1");m.result().numerical("tr").set("expr",ex.toArray(new String[0]));ExportNative.csv(a[1],ex.toArray(new String[0]),m.sol(saved).getPVals(),m.result().numerical("tr").getReal());System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
