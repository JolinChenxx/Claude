import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Collocate physical storage with the native Plasma quadrature points.
 * Removes the spatial projection between linear storage fields and exponential
 * native densities. It also avoids interpolating a trace density through zero.
 * Fixed spatial mesh only; moving-volume storage needs a separate ALE form.
 */
public class GaussConservativeStorage {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("gaussStore",a[0]);
  m.component("c").physics("storage").prop("ShapeProperty").set("shapeFunctionType","shgp");
  m.component("c").physics("storage").prop("ShapeProperty").set("order",8);
  String projection="0",weak="0";
  for(String n:Chemistry.NAMES){
   String v="W"+n,s="store"+n,target="plas.rho_w"+n+"/rho0";
   projection+="+(1-"+s+"/("+target+"))*test("+s+")";
   weak+="+(plas.rho_w"+n+"*d("+v+",TIME)-rho0*d("+s+",TIME))*test("+v+")";
  }
  projection+="+(1-storeElectron/(plas.ne/storeElectronScale))*test(storeElectron)+(1-storeEnergy/(plas.en/storeEnergyScale))*test(storeEnergy)";
  weak+="+(plas.ne*d(Ne,TIME)-storeElectronScale*d(storeElectron,TIME))*test(Ne)+(plas.en*d(En,TIME)-storeEnergyScale*d(storeEnergy,TIME))*test(En)";
  m.component("c").physics("storage").feature("projection").set("weakExpression",projection);
  m.component("c").physics("plas").feature("conservedStorage").set("weakExpression",weak);
  PositiveGasTemperature.solver(m);
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");
  m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<60[ns],2[ns],if(t<80[ns],1[ns],0.5[ns]))");
  m.sol("sol1").feature("t1").set("tout","tstepsclosest");
  m.save(a[1]);InspectFeatures.dump(m.component("c").physics("storage").prop("ShapeProperty"),"storage Gauss data");
  System.out.println("GAUSS STORAGE PREPARED, REQUIRES INITIAL/TRANSIENT CHECK");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
