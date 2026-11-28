import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Isobaric additive-volume mixture EOS. Separate copper/air mass densities are
 * per total volume; the copper EOS needs its intrinsic occupied-volume density.
 * This removes the artificial trace-air pressure from division by copper-only
 * gas volume near a dense target. */
public class CommonPressureHEM {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("commonPressure",a[0]);
 m.component("c").variable("state").set("rhoIntrinsicCu","1[kg/m^3]*exp(logRhoIntrinsic)");
 m.component("c").variable("state").set("lR","log10(rhoIntrinsicCu/1[kg/m^3])");
 m.component("c").variable("state").set("pGas","1[Pa]*exp(logPressure)");
 m.component("c").variable("state").set("alphaAir","rhoA*Ra*T/pGas");
 m.component("c").variable("state").set("alphaCu","rhoC/rhoIntrinsicCu");
 m.component("c").variable("state").set("solidFrac","alphaCu*(1-xV)*(1-flc2hs(T-1358[K],10[K]))");
 m.component("c").variable("state").set("alphaG","min(1,max(1e-12,alphaAir+alphaCu*cuAg(lT,lR)))");
 m.component("c").variable("state").set("Ei","rhoCi*cuE(log10(Tamb/1[K]),log10(rhoRef/1[kg/m^3]))*1[J/kg]+rhoAi*2.5*Ra*Tamb");
 m.component("c").variable("state").set("shockMagnitude","h*sqrt(d(pGas,x)^2+1[Pa^2/m^2])");m.component("c").variable("state").set("shockSensor","shockMagnitude/(sqrt(pGas^2+pAmb^2)+shockMagnitude)");
 m.component("c").physics().create("mixEOS","GeneralFormPDE","g",new String[]{"logRhoIntrinsic","logPressure"});
 m.component("c").physics("mixEOS").feature("gfeq1").set("da","0");m.component("c").physics("mixEOS").feature("gfeq1").set("Ga",new String[][]{{"0"},{"0"}});
 m.component("c").physics("mixEOS").feature("gfeq1").set("f",new String[]{"alphaCu+alphaAir-1","(pGas-pCu)/(pGas+pAmb)"});
 m.component("c").physics("mixEOS").feature("init1").set("logRhoIntrinsic","log(rhoRef/1[kg/m^3])");m.component("c").physics("mixEOS").feature("init1").set("logPressure","log(pAmb/1[Pa])");m.component("c").physics("mixEOS").prop("ShapeProperty").set("order",1);
 for(String so:m.sol().tags())m.sol().remove(so);m.study("std").createAutoSequences("sol");
 m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.2[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","1e-5[ns]");
 for(String f:m.sol("sol1").feature("v1").feature().tags()){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",f.equals("c_qE")||f.equals("c_qm")?"1e-3":"1");}
 for(String f:m.sol("sol1").feature("t1").feature().tags())if(m.sol("sol1").feature("t1").feature(f).getType().equals("FullyCoupled")){m.sol("sol1").feature("t1").feature(f).set("jtech","onevery");m.sol("sol1").feature("t1").feature(f).set("maxiter",30);m.sol("sol1").feature("t1").feature(f).set("dtech","auto");}
 m.label("Axial copper-air common-pressure additive-volume HEM; material stage, no Plasma");m.save(a[1]);System.out.println("COMMON PRESSURE HEM READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
