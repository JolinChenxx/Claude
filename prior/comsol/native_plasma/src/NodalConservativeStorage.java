import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Positive nodal physical densities with conservative BDF storage.
 * Interpolate positive nodal values, rather than exponentiating an interpolated
 * log field. Physical density and storage then occupy the same linear space.
 * Native Plasma reactions, field equations and boundary laws remain in place.
 * Input: F100_ready (partial densities, physical storage, order-8 assembly).
 * Fixed mesh only. Fresh initial-state solve required.
 */
public class NodalConservativeStorage {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("nodalStore",a[0]);
  String den="0",projection="0",weak="0";
  for(String n:Chemistry.NAMES){
   String v="W"+n,s="store"+n;
   den+="+laginterp(1,exp("+v+"))";
   PartialMassCoordinates.put(m,n,"plas.w"+n,"laginterp(1,exp("+v+"))/compositionDen");
   projection+="+laginterp(1,1-"+s+"/exp("+v+"))*test("+s+")";
   weak+="+(plas.rho_w"+n+"*d("+v+",TIME)-rho0*d("+s+",TIME))*test("+v+")";
  }
  m.component("c").variable("gasv").set("compositionDen",den);
  SimplexSpecies.put(m,"pes1","plas.ne","laginterp(1,exp(Ne))*1[1/m^3]");
  SimplexSpecies.put(m,"pes1","plas.en","laginterp(1,exp(En))*1[V/m^3]");
  for(String d:new String[]{"r","z"}){
   SimplexSpecies.put(m,"pes1","plas.dflux_ne"+d,"-plas.De"+d+"r*d(plas.ne,r)-plas.De"+d+"z*d(plas.ne,z)");
   SimplexSpecies.put(m,"pes1","plas.dflux_en"+d,"-plas.Den"+d+"r*d(plas.en,r)-plas.Den"+d+"z*d(plas.en,z)");
  }
  projection+="+laginterp(1,1-storeElectron*storeElectronScale/(exp(Ne)*1[1/m^3]))*test(storeElectron)+laginterp(1,1-storeEnergy*storeEnergyScale/(exp(En)*1[V/m^3]))*test(storeEnergy)";
  weak+="+(plas.ne*d(Ne,TIME)-storeElectronScale*d(storeElectron,TIME))*test(Ne)+(plas.en*d(En,TIME)-storeEnergyScale*d(storeEnergy,TIME))*test(En)";
  m.component("c").physics("storage").prop("ShapeProperty").set("shapeFunctionType","shlag");
  m.component("c").physics("storage").prop("ShapeProperty").set("order",1);
  m.component("c").physics("storage").prop("ShapeProperty").set("valueType","real");
  m.component("c").physics("storage").feature("projection").set("weakExpression",projection);
  m.component("c").physics("plas").feature("conservedStorage").set("weakExpression",weak);
  PositiveGasTemperature.solver(m);
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");
  m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<60[ns],1[ns],if(t<80[ns],0.5[ns],0.1[ns]))");
  m.sol("sol1").feature("t1").set("tout","tstepsclosest");
  m.label("Native Plasma with positive nodal physical density and conservative BDF storage");
  m.save(a[1]);System.out.println("NODAL PHYSICAL STORAGE READY; needs transient positivity/conservation checks");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
