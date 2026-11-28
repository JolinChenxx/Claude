import com.comsol.model.*;import com.comsol.model.util.*;
public class CheckExternalEOS{public static void main(String[] a){try{
 ModelUtil.initStandalone(false);ModelUtil.setPreference("security.comsol.allowexternallibraries","on");Model m=ModelUtil.load("check",a[0]);
 m.func().create("probe","Analytic");m.func("probe").set("args",new String[]{"x","y","z"});m.func("probe").set("expr","d(eeP(x,y,z),x)");
 m.param().set("nativeDerivative","eeP_d0(100,log10(8942.230330721954),-12)");m.param().set("symbolicDerivative","probe(100,log10(8942.230330721954),-12)");
 System.out.println("NATIVE "+m.param().evaluate("nativeDerivative"));System.out.println("SYMBOLIC "+m.param().evaluate("symbolicDerivative"));
 m.param().set("temp","eeT(100,log10(8942.230330721954),-12)");System.out.println("TEMP "+m.param().evaluate("temp"));System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
