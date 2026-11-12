import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Last saved state: native species extrema and numerical-source budget. */
public class ExportStiffnessAudit {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("audit",a[0]);String s=ExportNative.savedSolution(m);ExportNative.dataset(m,s);
 for(String[] op:new String[][]{{"auditMin","Minimum"},{"auditMax","Maximum"},{"auditInt","Integration"}}){m.component("c").cpl().create(op[0],op[1]);m.component("c").cpl(op[0]).selection().named("g_gas_dom");m.component("c").cpl(op[0]).set("opname",op[0]);}
 m.component("c").cpl("auditInt").set("axisym",true);m.component("c").cpl("auditInt").set("intorder",8);
 ArrayList<String> ex=new ArrayList<>();
 for(String n:Chemistry.NAMES)for(String op:new String[]{"auditMin","auditMax"})ex.add("c."+op+"(c.plas.n_w"+n+")");
 for(String n:new String[]{"plas.ne","TeK","Tg","rhoP","SneNumerical","QnumCuExc"})for(String op:new String[]{"auditMin","auditMax"})ex.add("c."+op+"(c."+n+")");
 ex.add("c.auditInt(abs(c.SneNumerical))");ex.add("c.auditInt(abs(c.QnumCuExc))");
 for(String n:new String[]{"Qlaser","Qrad","Ue"})ex.add("c.auditInt(c."+n+")");
 m.sol(s).updateSolution();
 m.result().numerical().create("audit","EvalGlobal");m.result().numerical("audit").set("data","dset1");m.result().numerical("audit").set("expr",ex.toArray(new String[0]));double[] ts=m.sol(s).getPVals();m.result().numerical("audit").set("solnum",new int[]{ts.length});ExportNative.csv(a[1],ex.toArray(new String[0]),new double[]{ts[ts.length-1]},m.result().numerical("audit").getReal());System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
