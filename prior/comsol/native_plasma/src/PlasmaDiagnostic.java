import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class PlasmaDiagnostic {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 for(String type:new String[]{"Maximum","Minimum"}){String tag=type.equals("Maximum")?"mx":"mn";m.component("c").cpl().create(tag,type);m.component("c").cpl(tag).set("opname",tag);m.component("c").cpl(tag).selection().named("g_gas_dom");}
 m.sol(saved).updateSolution();m.result().numerical().create("di","EvalGlobal");m.result().numerical("di").set("data","dset1");
 ArrayList<String> ex=new ArrayList<>();for(String n:Chemistry.NAMES){ex.add("c.mx(c.plas.n_w"+n+")");ex.add("c.mn(c.plas.n_w"+n+")");}
 for(String n:new String[]{"plas.Te","plas.ne","pPl","urP","uzP","rhoP","plas.V","QeRR","QeAttach","Qrad"}){ex.add("c.mx(c."+n+")");ex.add("c.mn(c."+n+")");}
 m.result().numerical("di").set("expr",ex.toArray(new String[0]));ExportNative.csv(a[1],ex.toArray(new String[0]),m.sol(saved).getPVals(),m.result().numerical("di").getReal());System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}}
