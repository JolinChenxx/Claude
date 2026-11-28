import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Remove the obsolete two-zone outflow from the new conservative FV system.
 * Qualify copper-side expressions inside global integration operators. */
public class RepairSpatialGridCoupling {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("repairGrid",a[0]);
  for(String c:m.component().tags())if(c.startsWith("cell")){
   String suffix=c.substring(4);m.component(c).variable("gasv"+suffix).set("outElectronPower","0[W]");
   m.component(c).variable("gasv"+suffix).set("outPower","0[W]");
   m.component(c).variable("gasv"+suffix).set("outMassRate","0[kg/s]");
   m.component(c).variable("gasv"+suffix).set("forwardMassRate","0[kg/s]");
   m.component(c).variable("gasv"+suffix).set("reverseMassRate","0[kg/s]");
  }
  ArrayList<String> names=new ArrayList<>(),eq=new ArrayList<>(),init=new ArrayList<>();
  for(String n:m.variable("axialFlux").varnames()){
   String x=m.variable("axialFlux").get(n);
   if(x.contains("c.intSurf(")){
    x=x.replaceAll("(?<![A-Za-z0-9_.])(r|T|mdot|mdotEvap|Qmass|Qexchange|electronFeed|emitFraction_[A-Za-z0-9_]+)(?![A-Za-z0-9_])","c.$1");
    String scale=n.contains("Momentum")?"0.1[N]":n.contains("Species")||n.contains("Mass")?"1e-5[kg/s]":"1000[W]";
    String unknown="gridSource"+names.size();names.add(unknown);eq.add(unknown+"-("+x+")/("+scale+")");init.add("0");
    m.variable("axialFlux").set(n,"c."+unknown+"*("+scale+")");
   }
  }
  // Exact algebraic auxiliaries avoid repeatedly expanding nonlocal boundary
  // integrals in every native chemistry Jacobian. No lagging or rate change.
  m.component("c").physics().create("gridSources","GlobalEquations","g");
  m.component("c").physics("gridSources").feature("ge1").set("name",names.toArray(new String[0]));
  m.component("c").physics("gridSources").feature("ge1").set("equation",eq.toArray(new String[0]));
  m.component("c").physics("gridSources").feature("ge1").set("initialValueU",init.toArray(new String[0]));
  // Keep the generated solver. Its variables node refreshes when solving;
  // recreating 80 native interfaces repeats an expensive shape-function pass.
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");
  m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.equals("c_T")?"1000":f.matches(".*_energy[A-Z]+$")?"1e7":f.endsWith("_pA")?"1e5":f.contains("ODE")?"1":f.endsWith("_w")?(f.matches(".*_O2[A-Z]+_w")?"1":"1e-8"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.save(a[1]);System.out.println("SPATIAL COUPLING REPAIRED; old nozzle removed, FV flux retained");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
