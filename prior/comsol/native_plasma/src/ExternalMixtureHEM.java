import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Direct audited EOS flash avoids coarse mixture-grid interpolation near the
 * dense contact and avoids adding pressure/density algebraic field unknowns. */
public class ExternalMixtureHEM {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);ModelUtil.setPreference("security.comsol.allowexternallibraries","on");Model m=ModelUtil.load("externalMixture",a[0]);
 m.func().create("mixtureEOS","External");m.func("mixtureEOS").set("path",a[2]);m.func("mixtureEOS").set("init",a[3]);
 String[] names={"hemE","hemP","hemXv","hemAg","hemCs","hemAc"};String[] args={"x","y","z"};ArrayList<String[]> funcs=new ArrayList<>(),ders=new ArrayList<>();
 for(String n:names){funcs.add(new String[]{n,"x,y,z"});for(int i=0;i<3;i++){String df=n+"_d"+i;funcs.add(new String[]{df,"x,y,z"});ders.add(new String[]{n,args[i],df+"(x,y,z)"});}}
 m.func("mixtureEOS").set("funcs",funcs.toArray(new String[0][]));m.func("mixtureEOS").set("ders",ders.toArray(new String[0][]));
 String[][] vars={{"lMixC","log10(max(rhoC,1e-100[kg/m^3])/1[kg/m^3])"},{"lMixA","log10(max(rhoA,1e-100[kg/m^3])/1[kg/m^3])"},
 {"pGas","hemP(lT,lMixC,lMixA)*1[Pa]"},{"Einternal","rho*hemE(lT,lMixC,lMixA)*1[J/kg]"},
 {"alphaG","hemAg(lT,lMixC,lMixA)"},{"alphaCu","hemAc(lT,lMixC,lMixA)"},{"xV","hemXv(lT,lMixC,lMixA)"},{"cs","hemCs(lT,lMixC,lMixA)*1[m/s]"},
 {"solidFrac","alphaCu*(1-xV)*(1-flc2hs(T-1358[K],10[K]))"},
 {"Ei","(rhoCi+rhoAi)*hemE(log10(Tamb/1[K]),log10(rhoCi/1[kg/m^3]),log10(rhoAi/1[kg/m^3]))*1[J/kg]"},
 {"Dnum","nojac(0.15*h*(sqrt(u^2+1e-8[m^2/s^2])+cs*shockSensor))"}};
 for(String[] v:vars)m.component("c").variable("state").set(v[0],v[1]);
 for(String so:m.sol().tags())m.sol().remove(so);m.study("std").createAutoSequences("sol");
 m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.2[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","1e-5[ns]");
 for(String f:m.sol("sol1").feature("v1").feature().tags()){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",f.equals("c_qE")||f.equals("c_qm")||f.equals("c_qr")?"1e-3":"1");}
 for(String f:m.sol("sol1").feature("t1").feature().tags())if(m.sol("sol1").feature("t1").feature(f).getType().equals("FullyCoupled")){m.sol("sol1").feature("t1").feature(f).set("jtech","onevery");m.sol("sol1").feature("t1").feature(f).set("maxiter",20);}
 m.param().set("pressureCheck","hemP(log10(293.15),log10(8942.230330721954),-12)*1[Pa]");
 System.out.println("DIRECT INITIAL PRESSURE CHECK "+m.param().evaluate("pressureCheck"));
 m.label("Conservative Cu-air direct common-pressure EOS; material stage without Plasma");m.save(a[1]);System.out.println("EXTERNAL MIXTURE HEM READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
