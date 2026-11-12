import com.comsol.model.*;
import com.comsol.model.util.*;
/** Make the FV fluxes used by total gas energy match the solved face fluxes. */
public class FVFluxRepair {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 // Internal energy reconstructions already include e; external ones omit it.
 m.component("c").physics("plas").feature("pes1").featureInfo("info").set("plas.Jenl_post_ext_down",new String[]{"e_const*plas.bc_total_fen"});
 m.component("c").physics("plas").feature("pes1").featureInfo("info").set("plas.Jenl_post_ext_up",new String[]{"-e_const*plas.bc_total_fen"});
 m.component("c").variable().create("fvfar");m.component("c").variable("fvfar").selection().geom("g",1);m.component("c").variable("fvfar").selection().named("far");
 m.component("c").variable().create("fvaxis");m.component("c").variable("fvaxis").selection().geom("g",1);m.component("c").variable("fvaxis").selection().named("axis");
 for(String n:Chemistry.NAMES)if(!n.equals("N2")){
  boolean ion=n.endsWith("_1p")||n.endsWith("_1m");String v="plas."+(ion?"nJi_w":"nJn_w")+n;
  m.component("c").variable("boundv").set(v,"plas.influx_"+n+(ion?"*F_const/plas.M_w"+n:""));
  for(String b:new String[]{"fvfar","fvaxis"})m.component("c").variable(b).set(v,ion?"0[A/m^2]":"0[kg/(m^2*s)]");
  if(ion)for(String face:new String[]{"mesh","int_down","int_up"})m.component("c").physics("plas").feature(n).featureInfo("info").set("plas."+n+".Ji_post_"+face,new String[]{"-F_const/plas.M_w"+n+"*plas.sg_flux_w"+n});
 }
 // The dependent carrier transports precisely the remaining mixture mass.
 for(String d:new String[]{"r","z"}){
  String sum="plas.rho*"+(d.equals("r")?"urP":"uzP");
  for(String n:Chemistry.NAMES)if(!n.equals("N2"))sum+="-plas.tflux"+d+"_w"+n;
  m.component("c").physics("plas").feature("N2").featureInfo("info").set("plas.tflux"+d+"_wN2",new String[]{sum});
 }
 m.save(a[1]);System.out.println("FV RECONSTRUCTION REPAIRED; conservation remains to be checked");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
