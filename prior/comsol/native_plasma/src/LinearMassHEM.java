import com.comsol.model.*;import com.comsol.model.util.*;
/** Axial conservative linear partial-density comparison. Reject any materially
 * negative density; logarithmic trace tails otherwise dominate the step size. */
public class LinearMassHEM {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("linearMass",a[0]);
 m.component("c").variable("state").set("rhoC","rhoS*wC");m.component("c").variable("state").set("rhoA","rhoAmb*wA");
 m.component("c").physics("hydro").feature("gfeq1").set("da",new String[]{"1","1","1","1"});
 m.component("c").physics("hydro").feature("init1").set("wC","rhoCi/rhoS");m.component("c").physics("hydro").feature("init1").set("wA","rhoAi/rhoAmb");
 m.component("c").physics("hydro").feature("far").set("r",new String[]{"rhoCi/rhoS","rhoAi/rhoAmb","0","Ei/(rhoS*eS)"});
 m.component("c").variable("state").set("shockMagnitude","h*sqrt(d(pGas,x)^2+1[Pa^2/m^2])");m.component("c").variable("state").set("shockSensor","shockMagnitude/(sqrt(pGas^2+pAmb^2)+shockMagnitude)");
 m.component("c").variable("state").set("Dnum","0.5*h*(sqrt(u^2+1e-8[m^2/s^2])+cs*shockSensor)");
 m.sol("sol1").feature("v1").feature("c_wC").set("scaleval","1");m.sol("sol1").feature("v1").feature("c_wA").set("scaleval","1");
 m.label("Axial HEM linear partial-mass discretization comparison; check density positivity and balances");m.save(a[1]);System.out.println("LINEAR MASS HEM READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
