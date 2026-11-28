import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

/** Native heavy-species transport in log partial-density coordinates.
 * Every species, including N2, has a conservation equation. Total density is
 * their sum, so no trace species has to absorb the total-mass discretization
 * error. Momentum, total energy, electron equations and reaction data remain.
 */
public class PartialMassCoordinates {
 static void put(Model m,String f,String n,String x){m.component("c").physics("plas").feature(f).featureInfo("info").set(n,new String[]{x});}
 public static void apply(Model m){
  m.component("c").physics("plas").feature("N2").set("FromMassConstraint",false);
  m.component("c").physics("plas").feature("N2").set("x0","0.79");
  String den="0",domain="0",far="0";
  for(String n:Chemistry.NAMES)den+="+exp(W"+n+")";
  m.component("c").variable("gasv").set("compositionDen",den);
  m.component("c").variable("gasv").set("rhoP","rho0*compositionDen");
  m.component("c").variable("gasv").set("urP","rho0*u0*qMr/rhoP");
  m.component("c").variable("gasv").set("uzP","rho0*u0*qMz/rhoP");
  m.component("c").variable("boundv").set("QworkWall","workCu*Iwall");
  for(String n:Chemistry.NAMES){
   put(m,n,"plas.w"+n,"exp(W"+n+")/compositionDen");
   // Native W initial value is log(w0). Here it denotes rho_i/rho0.
   put(m,n,"plas.w0_w"+n,"plas.x0_w"+n+"*plas.M_w"+n+"/plas.w0denom*rhoAmb/rho0");
   String v="W"+n,rhoi="plas.rho_w"+n;
   // Native storage is already -rho_i*d(W_i,TIME). Cancel native strong
   // convection and supply its conservative flux, for every species.
   domain+="+"+rhoi+"*((uMassR*"+v+"r+uMassZ*"+v+"z)*test("+v+")+uMassR*test("+v+"r)+uMassZ*test("+v+"z))";
   far+="-"+rhoi+"*(uMassR*nr+uMassZ*nz)*test("+v+")";
  }
  m.component("c").physics("plas").feature("compositionChainRule").active(false);
  m.component("c").physics("plas").feature("heavyConservative").set("weakExpression",domain);
  m.component("c").physics("plas").feature("heavyFarConvection").set("weakExpression",far);
  // N2 was formerly the remainder. Its explicit equation must also carry
  // the nitrogen compensation for the inherited trace regularizer.
  String rn=ConservativeTrace.rate(m,"N2");
  put(m,"N2","plas.R_wN2","("+rn+")-SnumN2_1p-0.5*(SnumN+SnumN_1p)");
  String[] names=m.component("c").physics("plas").feature("feed").getStringArray("specNames");
  String[] flux=m.component("c").physics("plas").feature("feed").getStringArray("influx");
  names=Arrays.copyOf(names,names.length+1);flux=Arrays.copyOf(flux,flux.length+1);
  names[names.length-1]="N2";
  flux[flux.length-1]="Gwall_N2_1p*plas.M_wN2/N_A_const+plas.rho_wN2*vnSurf";
  m.component("c").physics("plas").feature("feed").set("specNames",names);
  m.component("c").physics("plas").feature("feed").set("influx",flux);
  for(int q:new int[]{2,8})m.component("c").physics("plas").feature().remove("heavyFeedQuadrature"+q);
  MatchedHeavyFlux.apply(m);
  // The sum of all partial-mass equations IS total continuity. qR becomes
  // an unused algebraic output projection; no second continuity is solved.
  String[][] ga=m.component("c").physics("flow").feature("gfeq1").getStringMatrix("Ga");
  ga[0]=new String[]{"0","0"};m.component("c").physics("flow").feature("gfeq1").set("Ga",ga);
  String[] f=m.component("c").physics("flow").feature("gfeq1").getStringArray("f");
  f[0]="axisWeight*(compositionDen-qR)/1[ns]";m.component("c").physics("flow").feature("gfeq1").set("f",f);
  String[] da=new String[16];for(int i=0;i<4;i++)for(int j=0;j<4;j++)da[i*4+j]=(i==j&&i>0)?"axisWeight":"0";
  m.component("c").physics("flow").feature("gfeq1").set("da",da);
  String[] g=m.component("c").physics("flow").feature("feed").getStringArray("g");g[0]="0";
  m.component("c").physics("flow").feature("feed").set("g",g);
  m.component("c").physics("flow").feature("farbc").set("useDirichletCondition",new int[]{0,1,1,1});
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("partial",a[0]);apply(m);MassAveragedDiffusion.apply(m);
  m.param().set("tEnd",a.length>2?a[2]:"123[ns]");PositiveGasTemperature.solver(m);
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");
  m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<80[ns],1[ns],0.05[ns])");
  m.label("Native Plasma: all heavy partial densities, summed total continuity");m.save(a[1]);
  System.out.println("PARTIAL MASS COORDINATES READY; same kinetics and physical fluxes; fresh initial state");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
