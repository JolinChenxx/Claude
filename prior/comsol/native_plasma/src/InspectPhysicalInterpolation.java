import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Inspect only definitions affected by the physical-density interpolation. */
public class InspectPhysicalInterpolation {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("interp",a[0]);
 for(String f:new String[]{"pes1","Cu","N2"})
  for(String[] r:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Expression"))
   if(r.length>2 && r[1]!=null && r[1].matches("plas\\.(ne|en|Nn|Ne|En|gflux_ne.|gflux_en.|.*grad.*|rho_wCu|wCu|rho_wN2|wN2|rho|.*Dne.*|.*Den.*)"))System.out.println(f+" "+Arrays.toString(r));
 System.out.println("DOMAIN_WEAK "+m.component("c").physics("plas").feature("nativeDomainQuadrature").getString("weakExpression"));
 System.out.println("HEAVY_WEAK "+m.component("c").physics("plas").feature("heavyConservative").getString("weakExpression"));
 InspectFeatures.dump(m.component("c").physics("plas").prop("ShapeProperty"),"shape");
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
