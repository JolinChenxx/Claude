import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Numerical trace regularization; element and charge compensation is explicit. */
public class ConservativeTrace {
 static String rate(Model m,String n){for(String[] row:m.component("c").physics("plas").feature(n).featureInfo("info").getInfoTable("Expression"))if(row.length>2&&("plas.R_w"+n).equals(row[1]))return row[2];throw new RuntimeException("Missing rate "+n);}
 static void correct(Model m,String n,String correction){m.component("c").physics("plas").feature(n).featureInfo("info").set("plas.R_w"+n,new String[]{"("+rate(m,n)+")-("+correction+")"});}
 public static void apply(Model m){
  // Supply the net regularizer explicitly: native source plus a cancelling
  // sink can otherwise evaluate as Inf-Inf as ground-state Cu vanishes.
  m.component("c").physics("plas").prop("Stabilization").set("ReactionSourceStabilization",false);
  m.param().set("traceIota","2");
  m.param().set("traceCuIota","2");
  m.component("c").physics("plas").prop("Stabilization").set("iota","traceIota");
  m.component("c").variable("gasv").set("traceCuAvailable","plas.n_wCu/(plas.n_wCu+1e10[1/m^3])");
  for(String n:Chemistry.NAMES){
   String raw="exp(-traceIota*log(max(plas.n_w"+n+",1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]";
   m.component("c").variable("gasv").set("Sraw"+n,raw);
   String desired=n.equals("Cu")?"0[mol/(m^3*s)]":n.startsWith("Cu")?"traceCuAvailable*exp(-traceCuIota*log(max(plas.n_w"+n+",1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]":raw;
   m.component("c").variable("gasv").set("Snum"+n,desired);
   if(!n.equals("N2")&&!n.equals("Cu")&&!n.equals("O2"))correct(m,n,"-Snum"+n);
  }
  correct(m,"Cu","SnumCu+SnumCuD5+SnumCuD3+SnumCuP1+SnumCuP3+SnumCuH3+SnumCuH5+SnumCu_1p");
  correct(m,"O2","0.5*(SnumO+SnumO_1p+SnumO_1m)+SnumO2_1p+SnumO2_1m");
  // N2 is the dependent mass-constraint species; after Cu and O compensation
  // its remainder automatically balances the nitrogen atoms.
  String q="N_A_const*(SnumN2_1p+SnumN_1p+SnumO2_1p+SnumO_1p+SnumCu_1p-SnumO_1m-SnumO2_1m)";
  m.component("c").variable("gasv").set("SneNumerical",q);
  m.component("c").physics("plas").feature("bulkconv").set("Re","-urP*d(plas.ne,r)-uzP*d(plas.ne,z)-plas.ne*divU+SneNumerical");
  m.component("c").physics("plas").feature("power").set("Qgen","Qlaser-QeRR-QeAttach-Qff-urP*d(Ue,r)-uzP*d(Ue,z)-(Ue+pe)*divU-Qei+1.5*e_const*TeV*SneNumerical");
  m.component("c").variable("gasv").set("QnumCuExc","F_const*(1.388948[V]*SnumCuD5+1.6422256[V]*SnumCuD3+3.7858977[V]*SnumCuP1+3.816692[V]*SnumCuP3+6.1911751[V]*SnumCuH3+6.1920252[V]*SnumCuH5+7.72638[V]*SnumCu_1p)");
 }
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);apply(m);m.save(a[1]);System.out.println("CONSERVATIVE TRACE READY");System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}
}
