import com.comsol.model.*;import com.comsol.model.util.*;import java.io.*;
/** Export gas-to-copper feedback in the material radial coordinate and time. */
public class ExportDrivenFeedback {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("feedbackExport",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);double[] ts=m.sol(saved).getPVals();
  m.result().numerical().create("feedbackBoundary","Eval");m.result().numerical("feedbackBoundary").set("data","dset1");m.result().numerical("feedbackBoundary").selection().named("surface");
  boolean thermal=false;for(String tag:m.component("c").variable().tags())if(tag.equals("surfaceFeed"))thermal=true;
  String stateEnergy=thermal?"QemitStates":"0[W/m^2]";
  String[] ex={"R/1[um]","optD/pulse","pPl","Qexchange","mdotReturn","mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))+"+stateEnergy+"-Qmass","Qthermion","urP","uzP","z/1[um]","T","Qsurface","mdotEvap","Qmass",stateEnergy};
  m.result().numerical("feedbackBoundary").set("expr",ex);
  try(PrintWriter f=new PrintWriter(a[1])){
   f.println("time_s,r_um,transmission,p_Pa,Qexchange,return_mass,return_energy,electron_heat,gas_ur,gas_uz,z_um,T_K,Qsurface,evap_mass,Qmass,emitted_state_energy");
   for(int i=0;i<ts.length;i++){m.result().numerical("feedbackBoundary").set("solnum",new int[]{i+1});double[][][] d=m.result().numerical("feedbackBoundary").getData();for(int k=0;k<d[0][0].length;k++){f.print(ts[i]);for(int j=0;j<d.length;j++)f.print(","+d[j][0][k]);f.println();}}
  }
  System.out.println("GAS FEEDBACK EXPORTED "+ts.length+" END_NS "+ts[ts.length-1]*1e9);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
