import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

/** Integrate the existing native Plasma equations at a common domain order.
 * F75 showed an order-2 to order-16 charge difference larger than the metal
 * charge. This changes quadrature only, not the native transport/reaction laws.
 * Native weak terms are locked to zero and their exact expressions are moved
 * to an explicit WeakContribution. Reactions, variables, and fields stay native.
 */
public class PlasmaDomainQuadrature {
 public static void apply(Model m,int order){
  ArrayList<String> features=new ArrayList<>();features.add("pes1");features.addAll(Arrays.asList(Chemistry.NAMES));
  StringBuilder weak=new StringBuilder();int count=0;
  for(String f:features){
   String[][] rows=m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Weak");
   for(String[] row:rows){
    if(row.length<7||!"2".equals(row[6]))throw new IllegalStateException("Unexpected native domain term "+Arrays.toString(row));
    String ex=row[1];
    if(ex==null||ex.equals("0"))continue;
    // All native expressions use the spatial axisymmetric factor 2*pi*r.
    if(ex.indexOf("*pi*r")<0||ex.indexOf("*pi*r")!=ex.lastIndexOf("*pi*r"))throw new IllegalStateException("Unexpected axisymmetric weak expression "+ex);
    if(ex.startsWith("2*"))ex=ex.substring(2).replace("*pi*r","");
    else if(ex.startsWith("-2*"))ex="-("+ex.substring(3).replace("*pi*r","")+")";
    else throw new IllegalStateException("Unexpected prefactor "+ex);
    if(weak.length()>0)weak.append("+");weak.append("(").append(ex).append(")");
    m.component("c").physics("plas").feature(f).featureInfo("info").set(row[2],new String[]{"0"});
    count++;
   }
   for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Weak"))
    if(row[1]!=null&&!row[1].equals("0"))throw new IllegalStateException("Native weak lock did not apply: "+Arrays.toString(row));
  }
  m.component("c").physics("plas").create("nativeDomainQuadrature","WeakContribution",2);
  m.component("c").physics("plas").feature("nativeDomainQuadrature").selection().named("g_gas_dom");
  m.component("c").physics("plas").feature("nativeDomainQuadrature").set("weakExpression",weak.toString());
  for(String f:new String[]{"nativeDomainQuadrature","heavyConservative","conservativeElectronFlux","conservedStorage"}){
   if(!Arrays.asList(m.component("c").physics("plas").feature().tags()).contains(f))continue;
   m.component("c").physics("plas").feature(f).set("automaticQuadrature",false);
   m.component("c").physics("plas").feature(f).set("integrationOrder",order);
  }
  if(Arrays.asList(m.component("c").physics().tags()).contains("storage"))m.component("c").physics("storage").feature("projection").set("integrationOrder",order);
  m.component("c").cpl("intGas").set("intorder",order);
  System.out.println("NATIVE DOMAIN TERMS REINTEGRATED "+count+" ORDER "+order);
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("quad",a[0]);apply(m,a.length>2?Integer.parseInt(a[2]):8);
  PositiveGasTemperature.solver(m);
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");
  m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<60[ns],2[ns],if(t<80[ns],1[ns],0.5[ns]))");
  m.save(a[1]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
