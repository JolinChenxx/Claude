import com.comsol.model.*;
import com.comsol.model.util.*;
/** Complete the native Fick flux in the mass-average velocity frame.
 * j_i = j_i(raw) - w_i sum_j j_j(raw), including ion drift. The interface
 * checkbox alone does not add this term to the inherited Fick formulation.
 */
public class MassAveragedDiffusion {
 public static void apply(Model m){
  if(java.util.Arrays.asList(m.component("c").variable("gasv").varnames()).contains("sumRawMassFluxr"))return;
  for(String d:new String[]{"r","z"}){
   String sum="0[kg/(m^2*s)]";
   for(String n:Chemistry.NAMES){
    String name="rawMassFlux"+d+"_"+n;
    String raw="-rhoP*plas.Dfs_w"+n+"*(d(plas.w"+n+","+d+")+plas.w"+n+"*d(plas.Mn,"+d+")/plas.Mn)-plas.DT_w"+n+"*d(Tgas,"+d+")/Tgas+rhoP*plas.w"+n+"*plas.z_w"+n+"*plas.um_w"+n+"*plas.Et"+d;
    m.component("c").variable("gasv").set(name,raw);sum+="+"+name;
   }
   m.component("c").variable("gasv").set("sumRawMassFlux"+d,sum);
   for(String n:Chemistry.NAMES){
    String original=SimplexSpecies.expr(m,"pes1","plas.j"+d+"_w"+n);
    SimplexSpecies.put(m,"pes1","plas.j"+d+"_w"+n,"("+original+")-plas.w"+n+"*sumRawMassFlux"+d);
   }
  }
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("massframe",a[0]);apply(m);
  m.param().set("tEnd",a.length>2?a[2]:"123[ns]");PositiveGasTemperature.solver(m);
  m.sol("sol1").feature("t1").set("maxstepbdf","0.1[ns]");
  m.save(a[1]);System.out.println("MASS-AVERAGE DIFFUSION READY FROM INITIAL STATE; NO RATE CHANGE");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
