import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Copper predictor for partitioned coupling; actual melt and ALE, no imposed lift.
 * First iterate has ambient pressure and unattenuated incident laser. It is not
 * a converged two-way result and retains an explicitly audited material closure. */
public class PreparePartitionedCopper {
 static Model m;static void b(String n,String x){m.component("c").variable("boundv").set(n,x);}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("partitionedCopper",a[0]);
  for(String s:m.sol().tags())m.sol().remove(s);
  for(String p:m.component("c").physics().tags())if(!Arrays.asList("ht","melt","surfaceODE").contains(p))m.component("c").physics().remove(p);
  m.param().set("tEnd",a.length>2?a[2]:"203[ns]");m.param().set("tp","120[ns]");m.param().set("cameraZero","90[ns]");m.param().set("w0","11[um]");m.param().set("meltOn","1");
  // Restore native consistent stabilization for the advecting molten copper.
  // This is a discretization correction, not a momentum or heating source.
  for(String n:new String[]{"StreamlineDiffusion","CrosswindDiffusion"})m.component("c").physics("melt").prop("ConsistentStabilization").set(n,true);
  // Feedback definitions are explicit so the next coupling iterate replaces
  // these four fields with a common (material radius,time) interpolation.
  b("feedbackTransmission","1");b("feedbackPressure","pAmb");b("feedbackHeat","0[W/m^2]");b("feedbackReturn","0[kg/(m^2*s)]");
  b("feedbackReturnEnergy","0[W/m^2]");b("feedbackElectronHeat","0[W/m^2]");b("feedbackGasUr","0[m/s]");b("feedbackGasUz","0[m/s]");
  if(a.length>3){
   m.func().create("gasFeedback","Interpolation");m.func("gasFeedback").set("source","file");m.func("gasFeedback").set("filename",a[3]);m.func("gasFeedback").set("nargs",2);
   String[] names={"Transmission","Pressure","Heat","Return","ReturnEnergy","ElectronHeat","GasUr","GasUz"};String[] units={"","*1[Pa]","*1[W/m^2]","*1[kg/(m^2*s)]","*1[W/m^2]","*1[W/m^2]","*1[m/s]","*1[m/s]"};String[][] funcs=new String[names.length][2];
   for(int i=0;i<names.length;i++){funcs[i]=new String[]{"fb"+names[i],Integer.toString(i+1)};b("feedback"+names[i],"fb"+names[i]+"(R/1[um],t/1[ns])"+units[i]);}
   m.func("gasFeedback").set("funcs",funcs);m.func("gasFeedback").set("interp","linear");m.func("gasFeedback").set("extrap","const");m.func("gasFeedback").importData();
  }
  b("pPl","feedbackPressure");b("urP","0[m/s]");b("uzP","0[m/s]");
  b("mdotReturn","feedbackReturn");b("mdot","mdotEvap-mdotReturn");
  b("recoil","mdotEvap*vKn");b("Qsurface","Acu*2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau*exp(-4*log(2)*((t-tp)/tau)^2)*exp(-2*(r/w0)^2)*feedbackTransmission*ncz");
  b("Qmass","mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-feedbackReturnEnergy");
  b("Qexchange","feedbackHeat");b("Qthermion","feedbackElectronHeat");b("QelectronWall","0[W/m^2]");b("QworkWall","0[W/m^2]");
  m.component("c").physics("melt").feature("surf").set("Fbnd",new String[]{"-(feedbackPressure-pAmb+recoil)*ncr+mdotReturn*(feedbackGasUr-urCu)","0","-(feedbackPressure-pAmb+recoil)*ncz+mdotReturn*(feedbackGasUz-uzCu)"});
  m.component("c").physics("ht").feature("laser").set("q0","Qsurface-Qmass+mdot*hCond+Qexchange-Qthermion");
  m.component("c").physics("ht").feature("fluid1").set("u",new String[]{"urCu","0","uzCu"});
  m.component("c").physics().create("couplingLedger","GlobalEquations","g");
  m.component("c").physics("couplingLedger").feature("ge1").set("name",new String[]{"removedCopperNg","absorbedLaseruJ","emittedEnthalpyuJ","recoilWorkuJ"});
  m.component("c").physics("couplingLedger").feature("ge1").set("equation",new String[]{"removedCopperNgt-intSurf(mdot)/1e-12[kg]","absorbedLaseruJt-intSurf(Qsurface)/1e-6[J]","emittedEnthalpyuJt-intSurf(Qmass)/1e-6[J]","recoilWorkuJt-intSurf(recoil*(urCu*ncr+uzCu*ncz))/1e-6[J]"});
  m.component("c").physics("couplingLedger").feature("ge1").set("initialValueU",new String[]{"0","0","0","0"});
  String times="range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)";
  m.study("std").feature("time").set("tlist",times);m.study("std").feature("time").set("rtol",0.001);m.study("std").createAutoSequences("sol");
  for(String f:m.sol("sol1").feature("t1").feature().tags())if(Arrays.asList("Segregated","FullyCoupled").contains(m.sol("sol1").feature("t1").feature(f).getType()))m.sol("sol1").feature("t1").feature().remove(f);
  m.sol("sol1").feature("t1").create("directCu","Direct");m.sol("sol1").feature("t1").feature("directCu").set("linsolver","pardiso");
  m.sol("sol1").feature("t1").create("coupledCu","FullyCoupled");m.sol("sol1").feature("t1").feature("coupledCu").set("linsolver","directCu");m.sol("sol1").feature("t1").feature("coupledCu").set("maxiter",20);m.sol("sol1").feature("t1").feature("coupledCu").set("jtech","minimal");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.5[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String sc=f.equals("c_T")?"1000":f.equals("c_umelt")?"100":f.equals("c_p")?"1e7":f.equals("c_etaSurface")?"1":null;
   if(sc!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",sc);}
  }
  m.label("Partitioned copper predictor: actual melt/ALE; ambient first iterate; inherited Cu closure, high-temperature applicability NOT validated");m.save(a[1]);
  m.save(a[1].replace(".mph",".java"),"java");System.out.println("PARTITIONED COPPER READY "+times);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
