import com.comsol.model.*;
import com.comsol.model.util.*;
/** Restore field/flux coupling when switching native Plasma from FEM to FV. */
public class CompleteFiniteVolume {
 static void g(Model m,String n,String x){m.component("c").variable("gasv").set(n,x);}
 public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 for(String n:Chemistry.NAMES)if(!n.equals("N2"))g(m,"plas.w"+n,"w"+n);
 g(m,"plas.Etr","plas.Er");g(m,"plas.Etz","plas.Ez");
 for(String d:new String[]{"r","z"}){
  String vel=d.equals("r")?"relativeR":"relativeZ";
  g(m,"plas.gflux_ne"+d,"-plas.Jel"+d+"/e_const-plas.ne*"+vel);
  g(m,"plas.gflux_en"+d,"-plas.Jenl"+d+"/e_const-plas.en*"+vel);
  for(String n:Chemistry.NAMES){
   g(m,"plas.cflux"+d+"_w"+n,"plas.rho*plas.w"+n+"*"+(d.equals("r")?"urP":"uzP"));
   String reconstructed=n.endsWith("_1p")||n.endsWith("_1m")?"-plas."+n+".Ji_post"+d+"*plas.M_w"+n+"/F_const":"-plas."+n+".Jn_post"+d;
   m.component("c").physics("plas").feature(n).featureInfo("info").set("plas.tflux"+d+"_w"+n,new String[]{reconstructed+"+plas.rho*plas.w"+n+"*"+(d.equals("r")?"meshVr":"meshVz")});
  }
 }
 m.component("c").physics("plas").feature("pes1").featureInfo("info").set("plas.Pcap",new String[]{"(plas.Jelr+e_const*plas.ne*relativeR)*plas.Er+(plas.Jelz+e_const*plas.ne*relativeZ)*plas.Ez"});
 // SG mass flux already transports relative to the moving grid.
 String[] flux=m.component("c").physics("plas").feature("feed").getStringArray("influx");
 String[] names=m.component("c").physics("plas").feature("feed").getStringArray("specNames");
 for(int j=0;j<flux.length;j++)flux[j]="("+flux[j]+")-plas.rho_w"+names[j]+"*vnSurf";
 m.component("c").physics("plas").feature("feed").set("influx",flux);
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String scale=f.equals("c_Ne")?"1e9":f.equals("c_En")?"1e8":f.equals("c_ODE1")?"1e-3":null;
  if(f.startsWith("c_plas_")&&f.endsWith("_w"))scale=f.equals("c_plas_O2_w")||f.equals("c_plas_N2_w")?"1":f.endsWith("_1p_w")||f.endsWith("_1m_w")?"1e-18":"1e-12";
  if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
 }
 m.study("std").feature("time").set("rtol",1e-4);m.sol("sol1").feature("t1").set("rtol",1e-4);
 m.save(a[1]);System.out.println("FV FIELD COUPLING READY; REQUIRES COMPILE/TRANSPORT AUDIT");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
