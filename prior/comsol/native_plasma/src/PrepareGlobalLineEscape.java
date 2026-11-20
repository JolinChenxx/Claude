import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Replace grey line-centre escape by frequency-integrated expanding-sphere
 * transfer. The geometric bulk approximation remains explicit. */
public class PrepareGlobalLineEscape {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("lineEscape",a[0]);
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  m.func().create("globalLineEscape","Interpolation");
  m.func("globalLineEscape").set("source","file");
  m.func("globalLineEscape").set("filename","D:/junlin/PlasmaPlume/route1/data/global_escape_doppler.txt");
  m.func("globalLineEscape").set("nargs",2);
  m.func("globalLineEscape").set("funcs",new String[][]{{"logGlobalEscape","1"}});
  m.func("globalLineEscape").set("interp","linear");
  m.func("globalLineEscape").set("extrap","const");
  m.func("globalLineEscape").importData();
  m.component("c").variable("gasv").set("escapeSpeedRatio","abs((2*vRadius/Rp+vHeight/Hp)/3)*Req/sqrt(2*k_B_const*Tg/(Mcu/N_A_const))");
  for(int i=1;i<=8;i++)m.component("c").variable("gasv").set("beta"+i,"10^logGlobalEscape(log10(max(tauLine"+i+",1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
  if(Arrays.asList(m.component("c").physics("plas").feature().tags()).contains("CuT5"))m.component("c").variable("gasv").set("tailBeta","10^logGlobalEscape(log10(max(tailTau,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
  m.sol("sol1").feature("v1").set("initmethod","init");
  m.label("Native global Plasma: frequency-integrated Doppler escape with homologous expansion; approximate uniform geometry");
  m.save(a[1]);m.save(a[1].replace(".mph",".java"),"java");
  System.out.println("FREQUENCY-INTEGRATED LINE ESCAPE READY; fresh initial state");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
