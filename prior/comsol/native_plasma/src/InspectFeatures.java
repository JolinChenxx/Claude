import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectFeatures {
 static Object call(Object f,String method,Class<?>[] types,Object...args)throws Exception{return f.getClass().getMethod(method,types).invoke(f,args);}
 static void dump(Object f,String label) throws Exception {
  System.out.println("NODE "+label);
  for(String p:(String[])call(f,"properties",new Class<?>[]{})) {
   String v="";
   try{v=Arrays.deepToString((String[][])call(f,"getStringMatrix",new Class<?>[]{String.class},p));}catch(Exception a){try{v=Arrays.toString((String[])call(f,"getStringArray",new Class<?>[]{String.class},p));}catch(Exception b){try{v=(String)call(f,"getString",new Class<?>[]{String.class},p);}catch(Exception c){v="[non-string]";}}}
   String[] allowed=null;try{allowed=(String[])call(f,"getAllowedPropertyValues",new Class<?>[]{String.class},p);}catch(Exception ignored){}
   System.out.println(p+"="+v+(allowed==null?"":" ALLOWED="+Arrays.toString(allowed)));
  }
 }
 public static void main(String[] args){try{
  ModelUtil.initStandalone(false);
  Model m=ModelUtil.create("i");m.component().create("c",true);m.component("c").geom().create("g",2);m.component("c").geom("g").axisymmetric(true);
  m.component("c").geom("g").create("r","Rectangle");m.component("c").geom("g").run();
  m.component("c").physics().create("plas","ColdPlasma","g");
  m.component("c").physics("plas").create("rx1","Reaction",2);m.component("c").physics("plas").feature("rx1").set("formula","e+Cu=>2e+Cu+");
  m.component("c").physics("plas").create("ei1","ElectronImpactReaction",2);
  int j=0;
  for(String type:new String[]{"GeneralPowerDeposition","Velocity","ElectronProductionRate","HeavySpeciesFlux","Outflow","ElectronOutlet","WallDriftDiffusion","FloatingPotential"}) {
   String tag="probe"+(j++);try{m.component("c").physics("plas").create(tag,type,type.contains("Deposition")||type.equals("Velocity")||type.contains("Production")?2:1);System.out.println("CREATED "+type);dump(m.component("c").physics("plas").feature(tag),type);}catch(Exception e){System.out.println("UNAVAILABLE "+type);}
  }
  m.component("c").physics().create("liq","LaminarFlow","g");
  for(String type:new String[]{"FreeSurface","BoundaryStress","VolumeForce"}){
   try{m.component("c").physics("liq").create(type,type,type.equals("VolumeForce")?2:1);dump(m.component("c").physics("liq").feature(type),type);}catch(Exception e){System.out.println("UNAVAILABLE "+type);}
  }
  for(String type:new String[]{"PrescribedNormalMeshVelocity","PrescribedMeshVelocity","FixedBoundary","DeformingDomain"}){
   try{m.component("c").common().create(type,type);dump(m.component("c").common(type),type);}catch(Exception e){System.out.println("UNAVAILABLE "+type);}
  }
  m.component("c").physics("plas").prop("TransportSettings").set("Convection",true);
  m.component("c").physics("plas").feature("pes1").set("u",new String[]{"urTest","0","uzTest"});
  try{System.out.println("PLASMA_WEAK "+Arrays.deepToString(m.component("c").physics("plas").feature("pes1").featureInfo("info").getInfoTable("Weak")));}catch(Exception e){System.out.println("NO_WEAK "+e);}
  for(String fn:new String[]{"pes1","Cu","rx1"})try{
   for(String[] row:m.component("c").physics("plas").feature(fn).featureInfo("info").getInfoTable("Expression"))
    if(fn.equals("Cu")||Arrays.toString(row).matches(".*(gflux_ne|gflux_en|rr_).*"))System.out.println("EQUATION_VAR "+Arrays.toString(row));
  }catch(Exception e){System.out.println("NO_VAR "+e);}
  for(String tab:new String[]{"Weak","Constraint","Expression"})try{System.out.println("FLOAT_"+tab+" "+Arrays.deepToString(m.component("c").physics("plas").feature("probe7").featureInfo("info").getInfoTable(tab)));}catch(Exception ignored){}
  for(String p:m.component("c").physics("plas").prop().tags())dump(m.component("c").physics("plas").prop(p),"PROP/"+p);
  for(String p:m.component("c").physics("plas").feature().tags())dump(m.component("c").physics("plas").feature(p),"FEATURE/"+p);
  m.save(args[0]+"/inspect_native.java","java");
  Model a=ModelUtil.load("m",args[1]);
  for(String c:a.component().tags())for(String p:a.component(c).physics().tags()){
   System.out.println("PHYSICS "+c+"/"+p+" "+a.component(c).physics(p).getType());
   for(String f:a.component(c).physics(p).feature().tags())if(p.equals("ale")||f.contains("free")||f.contains("ffi"))dump(a.component(c).physics(p).feature(f),p+"/"+f);
  }
  for(String c:a.component().tags())for(String p:a.component(c).common().tags())dump(a.component(c).common(p),"COMMON/"+p);
  System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
