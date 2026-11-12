import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Exact log-ratio coordinates for the native heavy-species equations.
 * w_i=exp(W_i)/(1+sum exp(W_j)); w_N2=1/(1+sum exp(W_j)).
 * Native time/convection and diffusion terms receive the chain-rule correction.
 */
public class SimplexSpecies {
 static String expr(Model m,String f,String n){for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Expression"))if(row.length>2&&n.equals(row[1]))return row[2];throw new RuntimeException("Missing "+n);}
 static void put(Model m,String f,String n,String x){m.component("c").physics("plas").feature(f).featureInfo("info").set(n,new String[]{x});}
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 String den="1",weights="0[kg/m^3]";
 for(String n:Chemistry.NAMES)if(!n.equals("N2")){den+="+exp(W"+n+")";weights+="+plas.rho*plas.w"+n+"*test(W"+n+")";}
 m.component("c").variable("gasv").set("compositionDen",den);
 m.component("c").variable("gasv").set("compositionLogDen","log(compositionDen)");
 m.param().set("initialN2MassFraction","0.79*0.0280134/(0.79*0.0280134+0.21*0.0319988)");
 int corrections=0;
 for(String n:Chemistry.NAMES){
  if(n.equals("N2")){put(m,n,"plas.wN2","1/compositionDen");continue;}
  String[][] rows=m.component("c").physics("plas").feature(n).featureInfo("info").getInfoTable("Expression");
  for(String[] row:rows)if(row.length>2&&row[1]!=null&&row[1].startsWith("plas.")&&row[2]!=null){String original=row[2],changed=original;
   for(String d:new String[]{"r","z"})changed=changed.replaceAll("\\bW"+n+d+"\\b","(W"+n+d+"-d(compositionLogDen,"+d+"))");
   if(!original.equals(changed)){put(m,n,row[1],changed);corrections++;System.out.println("CHAIN RULE "+n+" "+row[1]+" "+changed);}
  }
  put(m,n,"plas.w"+n,"exp(W"+n+")/compositionDen");
  put(m,n,"plas.w0_w"+n,"("+expr(m,n,"plas.w0_w"+n)+")/initialN2MassFraction");
 }
 m.component("c").physics("plas").create("compositionChainRule","WeakContribution",2);m.component("c").physics("plas").feature("compositionChainRule").selection().named("g_gas_dom");
 m.component("c").physics("plas").feature("compositionChainRule").set("weakExpression","("+weights+")*(d(compositionLogDen,TIME)+(plas.ur-d(r,TIME))*d(compositionLogDen,r)+(plas.uz-d(z,TIME))*d(compositionLogDen,z))");
 // Native heavy residual stabilization assumes unnormalised log fractions.
 // Explicit conservative numerical diffusion is supplied by the gas transport case.
 for(String s:new String[]{"StreamlineDiffusionIonsLog","CrosswindDiffusionIonsLog"})m.component("c").physics("plas").prop("ConsistentStabilization").set(s,false);
 m.param().set("tEnd","1000[ns]");m.study("std").feature("time").set("tlist","range(0,2[ns],tEnd)");m.sol("sol1").feature("t1").set("tlist","range(0,2[ns],tEnd)");m.sol("sol1").feature("v1").set("initmethod","init");m.sol("sol1").feature("t1").set("consistent","bweuler");
 m.save(a[1]);System.out.println("SIMPLEX SPECIES READY; gradient overrides="+corrections);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
