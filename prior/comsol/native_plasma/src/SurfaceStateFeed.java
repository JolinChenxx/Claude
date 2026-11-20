import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

/** Thermal surface-state partition of the EXISTING total Cu evaporation flux.
 * Saha-Langmuir applies only to particles leaving the conductor. Gas populations
 * and electron energy remain native non-LTE unknowns. See surface_feed_sources.json.
 */
public class SurfaceStateFeed {
 static final String[] CU={"Cu","CuD5","CuD3","CuP1","CuP3","CuH3","CuH5","Cu_1p"};
 static final double[] ENERGY={0,1.388948,1.6422256,3.7858977,3.816692,6.1911751,6.1920252,7.72638};
 static final int[] WEIGHT={2,6,4,2,4,4,6,1};
 public static void apply(Model m){
  m.component("c").variable().create("surfaceFeed");
  m.component("c").variable("surfaceFeed").selection().geom("g",1);
  m.component("c").variable("surfaceFeed").selection().named("surface");
  String sum="0",energy="0[V]";
  for(int i=0;i<CU.length;i++){
   String exponent=i==7?"(workCu-"+ENERGY[i]+"[V])*e_const/(k_B_const*T)":"-"+ENERGY[i]+"[V]*e_const/(k_B_const*T)";
   m.component("c").variable("surfaceFeed").set("emitWeight_"+CU[i],WEIGHT[i]+"*exp("+exponent+")");
   sum+="+emitWeight_"+CU[i];
  }
  m.component("c").variable("surfaceFeed").set("emitPartition",sum);
  for(int i=0;i<CU.length;i++){
   m.component("c").variable("surfaceFeed").set("emitFraction_"+CU[i],"emitWeight_"+CU[i]+"/emitPartition");
   m.component("c").variable("surfaceFeed").set("Gemit_"+CU[i],"mdotEvap*N_A_const/Mcu*emitFraction_"+CU[i]);
   energy+="+emitFraction_"+CU[i]+"*"+ENERGY[i]+"[V]";
  }
  m.component("c").variable("surfaceFeed").set("emitMeanEnergy",energy);
  m.component("c").variable("surfaceFeed").set("QemitStates","mdotEvap*N_A_const/Mcu*e_const*emitMeanEnergy");
  String[] names=m.component("c").physics("plas").feature("feed").getStringArray("specNames");
  String[] flux=m.component("c").physics("plas").feature("feed").getStringArray("influx");
  int count=0;
  for(int j=0;j<names.length;j++){
   String name=names[j];
   if(!Arrays.asList(CU).contains(name))continue;
   // Preserve the actual native return/mesh expression in the input model.
   if(name.equals("Cu")){
    if(!flux[j].contains("mdotEvap"))throw new IllegalStateException("No baseline Cu evaporation in "+flux[j]);
    flux[j]=flux[j].replace("mdotEvap","mdotEvap*emitFraction_Cu");
   }else flux[j]="mdotEvap*emitFraction_"+name+"+("+flux[j]+")";
   count++;
  }
  if(count!=8)throw new IllegalStateException("Matched "+count+" Cu fluxes");
  m.component("c").physics("plas").feature("feed").set("influx",flux);
  String mass=m.component("c").variable("boundv").get("Qmass");
  String current=m.component("c").variable("boundv").get("Iwall");
  m.component("c").variable("boundv").set("Qmass","("+mass+")+QemitStates");
  // Positive ion emission leaves an electron in the metal. No artificial
  // co-emitted electron source is introduced; the native sheath responds.
  m.component("c").variable("boundv").set("Iwall","("+current+")-e_const*Gemit_Cu_1p");
  // Include work-function energy for ALL changes of metal electron number,
  // including returning ions. For a neutral emitted ion+electron pair it cancels.
  m.component("c").variable("boundv").set("QworkWall","workCu*Iwall");
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("surface",a[0]);
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  apply(m);
  String end=a.length>2?a[2]:"123[ns]";
  m.param().set("tEnd",end);
  String times="range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)";
  m.study("std").feature("time").set("tlist",times);m.sol("sol1").feature("t1").set("tlist",times);
  m.sol("sol1").feature("v1").set("initmethod","init");
  m.label("Route 1 fixed plane: thermal Cu surface-state feed, native non-LTE gas");
  m.save(a[1]);System.out.println("SURFACE STATE FEED READY: total Cu feed conserved; thermal surface assumption");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
