import com.comsol.model.*;import com.comsol.model.util.*;
/** Second partitioned iterate, preserving the predictor's mesh and discretization. */
public class ApplyCopperFeedback {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("copperFeedback",a[0]);
  m.func().create("gasFeedback","Interpolation");m.func("gasFeedback").set("source","file");m.func("gasFeedback").set("filename",a[2]);m.func("gasFeedback").set("nargs",2);
  String[] names={"Transmission","Pressure","Heat","Return","ReturnEnergy","ElectronHeat","GasUr","GasUz"};String[] units={"","*1[Pa]","*1[W/m^2]","*1[kg/(m^2*s)]","*1[W/m^2]","*1[W/m^2]","*1[m/s]","*1[m/s]"};String[][] funcs=new String[names.length][2];
  for(int i=0;i<names.length;i++){funcs[i]=new String[]{"fb"+names[i],Integer.toString(i+1)};m.component("c").variable("boundv").set("feedback"+names[i],"fb"+names[i]+"(R/1[um],t/1[ns])"+units[i]);}
  boolean thermal=a.length>4&&a[4].equals("states");
  if(thermal){String[][] expanded=new String[9][2];System.arraycopy(funcs,0,expanded,0,8);expanded[8]=new String[]{"fbStateEnergy","9"};funcs=expanded;}
  m.component("c").variable("boundv").set("feedbackStateEnergy",thermal?"fbStateEnergy(R/1[um],t/1[ns])*1[W/m^2]":"0[W/m^2]");
  m.func("gasFeedback").set("funcs",funcs);m.func("gasFeedback").set("interp","linear");m.func("gasFeedback").set("extrap","const");m.func("gasFeedback").importData();
  m.component("c").variable("boundv").set("Qmass","mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))+feedbackStateEnergy-feedbackReturnEnergy");
  m.component("c").variable("boundv").set("Qthermion","feedbackElectronHeat");
  m.component("c").physics("melt").feature("surf").set("Fbnd",new String[]{"-(feedbackPressure-pAmb+recoil)*ncr+mdotReturn*(feedbackGasUr-urCu)","0","-(feedbackPressure-pAmb+recoil)*ncz+mdotReturn*(feedbackGasUz-uzCu)"});
  m.component("c").physics("ht").feature("laser").set("q0","Qsurface-Qmass+mdot*hCond+Qexchange-Qthermion");
  m.param().set("tEnd",a[3]);String times="range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)";
  m.study("std").feature("time").set("tlist",times);m.sol("sol1").feature("t1").set("tlist",times);m.sol("sol1").feature("v1").set("initmethod","init");
  m.label("Copper partitioned feedback iterate; same C201 discretization; actual gas history; not converged two-way solution");m.save(a[1]);
  System.out.println("COPPER FEEDBACK READY TO "+a[3]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
