import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Locate thermodynamic, composition, or trace-source limits in a saved checkpoint. */
public class StateStiffnessDiagnostic {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("diag",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
  for(String op:new String[]{"diagMin","diagMax"}){m.component("c").cpl().create(op,op.equals("diagMin")?"Minimum":"Maximum");m.component("c").cpl(op).selection().named("g_gas_dom");m.component("c").cpl(op).set("opname",op);}
  ArrayList<String> ex=new ArrayList<>();
  m.component("c").variable("gasv").set("diagH","h");
  for(String v:new String[]{"Tg","rhoP","plas.wN2","plas.ne","nHeavy","CvVol","Ue","Uchem","diagH","Dnum","speedP"})for(String op:new String[]{"diagMin","diagMax"})ex.add("c."+op+"(c."+v+")");
  for(String n:Chemistry.NAMES){ex.add("c.diagMin(c.plas.n_w"+n+")");ex.add("c.diagMax(abs(d(c.plas.n_w"+n+",TIME))/max(c.plas.n_w"+n+",1[1/m^3]))");}
  for(String v:new String[]{"SneNumerical","QnumCuExc","Qlaser","plas.Re","plas.Sen"})ex.add("c.intGas(c."+v+")");
  ex.add("c.diagMax(abs(c.SneNumerical)/max(c.plas.ne,1[1/m^3]))");
  m.sol(saved).updateSolution();m.result().numerical().create("stiff","EvalGlobal");m.result().numerical("stiff").set("data","dset1");m.result().numerical("stiff").set("expr",ex.toArray(new String[0]));
  double[] tt=m.sol(saved).getPVals();int first=Math.max(0,tt.length-3);int[] selected=new int[tt.length-first];double[] t=new double[selected.length];for(int i=0;i<t.length;i++){selected[i]=first+i+1;t[i]=tt[first+i];}m.result().numerical("stiff").set("solnum",selected);
  ExportNative.csv(a[1],ex.toArray(new String[0]),t,m.result().numerical("stiff").getReal());System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
