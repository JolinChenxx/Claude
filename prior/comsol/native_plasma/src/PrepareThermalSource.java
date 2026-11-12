import com.comsol.model.*;import com.comsol.model.util.*;
/** Thermal predictor: actual Cu heat PDE and thermal evaporation states,
 * before plume shielding/return feedback. The total reserved target energy is
 * Acu*Ein; downstream laser budget must reserve that same target share.
 */
public class PrepareThermalSource {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("thermalPredictor",a[0]);for(String s:m.sol().tags())m.sol().remove(s);
  m.component().remove("outer");m.component("c").physics().remove("plas");m.component("c").physics().remove("bulk");
  m.component("c").variable("boundv").set("Qsurface","Acu*Ipeak*pulse*exp(-2*(r/w0)^2)");
  m.component("c").variable("boundv").set("QthermalEmission","mdotEvap*(Bcu+3*R_const*T/Mcu+F_const*emitMeanEnergy/Mcu)+2*k_B_const*T*mdotEvap*N_A_const/Mcu*emitFraction_Cu_1p");
  m.component("c").physics("ht").feature("laser").set("q0","Qsurface-QthermalEmission");
  m.param().set("tEnd","1000[ns]");m.study("std").feature("time").set("tlist","range(0,1[ns],tEnd)");m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","2[ns]");m.sol("sol1").feature("v1").feature("c_T").set("scalemethod","manual");m.sol("sol1").feature("v1").feature("c_T").set("scaleval","1000");
  m.label("Thermal-source predictor: finite pulse, actual Cu heat and vaporization; no plume feedback; reserved10percent target energy");m.save(a[1]);System.out.println("THERMAL SOURCE PREDICTOR READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
