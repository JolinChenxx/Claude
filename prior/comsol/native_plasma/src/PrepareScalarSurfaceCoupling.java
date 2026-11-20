import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Exact algebraic auxiliaries for surface integrals. This factorizes repeated
 * nonlocal heat-to-global chemistry dependencies without lagging their values. */
public class PrepareScalarSurfaceCoupling {
 static Model m;static LinkedHashMap<String,String> replacements=new LinkedHashMap<>();
 static ArrayList<String> names=new ArrayList<>(),equations=new ArrayList<>(),initial=new ArrayList<>();
 static void flux(String expr,String name,String scale){
  names.add(name);equations.add(name+"-("+expr+")/("+scale+")");initial.add("0");
  replacements.put(expr,"("+name+"*("+scale+"))");
 }
 static String factor(String expr){for(Map.Entry<String,String> e:replacements.entrySet())expr=expr.replace(e.getKey(),e.getValue());return expr;}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("scalarSurface",a[0]);for(String s:m.sol().tags())m.sol().remove(s);
  flux("intSurf(mdot)","surfaceMassFlux","1e-5[kg/s]");
  flux("intSurf(Qmass-Qexchange)","surfacePowerFlux","1000[W]");
  flux("intSurf(electronSourceEnergy)","surfaceElectronFlux","1000[W]");
  flux("intSurf(Qsurface)","surfaceLaserFlux","1000[W]");
  ArrayList<String> species=new ArrayList<>(Arrays.asList(SurfaceStateFeed.CU));species.add("CuT5");
  for(String n:species)flux("intSurf(source_"+n+")","surfaceSpecies"+n,"1e-5[kg/s]");
  for(int q=2;q<=4;q++){species.add("Cu_"+q+"p");flux("intSurf(mdotReturn*nCu"+q+"/nCuTotal)","surfaceReturn"+q,"1e-5[kg/s]");}
  // Preserve the original integrated smooth beam coverage too.
  for(String n:new String[]{"coveredInnerPower","coveredOuterPower"}){
   String expr=m.component("c").variable("gasv").get(n);flux(expr,"surface"+n,"1000[W]");
  }
  for(String n:m.component("c").variable("gasv").varnames()){
   String old=m.component("c").variable("gasv").get(n),now=factor(old);if(!old.equals(now))m.component("c").variable("gasv").set(n,now);
  }
  for(String n:species){String old=ConservativeTrace.rate(m,n),now=factor(old);if(!old.equals(now))m.component("c").physics("plas").feature(n).featureInfo("info").set("plas.R_w"+n,new String[]{now});}
  String[] eq=m.component("c").physics("bulk").feature("ge1").getStringArray("equation");for(int i=0;i<eq.length;i++)eq[i]=factor(eq[i]);m.component("c").physics("bulk").feature("ge1").set("equation",eq);
  m.component("c").physics().create("surfaceFlux","GlobalEquations","g");
  m.component("c").physics("surfaceFlux").feature("ge1").set("name",names.toArray(new String[0]));
  m.component("c").physics("surfaceFlux").feature("ge1").set("equation",equations.toArray(new String[0]));
  m.component("c").physics("surfaceFlux").feature("ge1").set("initialValueU",initial.toArray(new String[0]));
  m.component("c").variable("gasv").set("scalarSurfaceCoupling","1");
  m.param().set("tEnd","1000[ns]");m.study("std").feature("time").set("tlist","range(0,1[ns],tEnd)");m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","2[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.equals("c_T")?"1000":f.endsWith("En")?"1e7":f.endsWith("_pA")?"1e5":f.contains("ODE")?"1":f.endsWith("_w")?(f.endsWith("_O2_w")||f.endsWith("_O2X_w")?"1":"1e-8"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Exact simultaneous surface-flux auxiliaries; native multi-charge two-zone Plasma and original copper heat PDE");m.save(a[1]);
  System.out.println("SCALAR SURFACE COUPLING READY "+names.size()+" exact auxiliaries");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
