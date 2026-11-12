import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Physical-density storage for native logarithmic Plasma coordinates.
 * Algebraic L2 projections retain the positive native fields for transport and
 * reactions; BDF differentiates the projected physical densities. This removes
 * the discrete-time chain-rule defect in exp(W)*BDF(W), without changing rates.
 * Input must use PartialMassCoordinates. Prepared separately for charge tests.
 */
public class ConservativeStorage {
 public static void apply(Model m){
  ArrayList<String> vars=new ArrayList<>();ArrayList<String> target=new ArrayList<>();
  for(String n:Chemistry.NAMES){vars.add("store"+n);target.add("plas.rho_w"+n+"/rho0");}
  m.param().set("storeElectronScale","1e20[1/m^3]");m.param().set("storeEnergyScale","1e20[V/m^3]");
  vars.add("storeElectron");target.add("plas.ne/storeElectronScale");vars.add("storeEnergy");target.add("plas.en/storeEnergyScale");
  m.component("c").physics().create("storage","WeakFormPDE","g",vars.toArray(new String[0]));m.component("c").physics("storage").selection().named("g_gas_dom");
  String[] algebraic=new String[vars.size()];
  for(int i=0;i<vars.size();i++)algebraic[i]="("+target.get(i)+"-"+vars.get(i)+")*test("+vars.get(i)+")";
  String projection=String.join("+",algebraic);String[] zero=new String[vars.size()];Arrays.fill(zero,"0");
  m.component("c").physics("storage").feature("wfeq1").set("weak",zero);
  m.component("c").physics("storage").prop("ShapeProperty").set("order",1);
  m.component("c").physics("storage").create("projection","WeakContribution",2);m.component("c").physics("storage").feature("projection").selection().named("g_gas_dom");m.component("c").physics("storage").feature("projection").set("automaticQuadrature",false);m.component("c").physics("storage").feature("projection").set("integrationOrder",2);m.component("c").physics("storage").feature("projection").set("weakExpression",projection);
  String weak="0";
  for(String n:Chemistry.NAMES){
   String v="W"+n,s="store"+n;
   weak+="+(plas.rho_w"+n+"*(d("+v+",TIME)-"+v+"r*d(r,TIME)-"+v+"z*d(z,TIME))-rho0*(d("+s+",TIME)-"+s+"r*d(r,TIME)-"+s+"z*d(z,TIME)))*test("+v+")";
   m.component("c").physics("storage").feature("init1").set(s,"plas.w0_w"+n);
  }
  weak+="+(plas.ne*(d(Ne,TIME)-Ner*d(r,TIME)-Nez*d(z,TIME))-storeElectronScale*(d(storeElectron,TIME)-storeElectronr*d(r,TIME)-storeElectronz*d(z,TIME)))*test(Ne)";
  weak+="+(plas.en*(d(En,TIME)-Enr*d(r,TIME)-Enz*d(z,TIME))-storeEnergyScale*(d(storeEnergy,TIME)-storeEnergyr*d(r,TIME)-storeEnergyz*d(z,TIME)))*test(En)";
  m.component("c").physics("storage").feature("init1").set("storeElectron","seedNe/storeElectronScale");m.component("c").physics("storage").feature("init1").set("storeEnergy","seedNe*1.5*k_B_const*Tamb/e_const/storeEnergyScale");
  m.component("c").physics("plas").create("conservedStorage","WeakContribution",2);m.component("c").physics("plas").feature("conservedStorage").selection().named("g_gas_dom");m.component("c").physics("plas").feature("conservedStorage").set("automaticQuadrature",false);m.component("c").physics("plas").feature("conservedStorage").set("integrationOrder",2);m.component("c").physics("plas").feature("conservedStorage").set("weakExpression",weak);
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("store",a[0]);apply(m);m.param().set("tEnd",a.length>2?a[2]:"123[ns]");PositiveGasTemperature.solver(m);
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<80[ns],1[ns],0.05[ns])");m.save(a[1]);
  System.out.println("CONSERVATIVE STORAGE PREPARED; requires saved charge and mass validation");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
