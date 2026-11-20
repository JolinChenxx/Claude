import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Conservative variables determine T and phase locally by energy inversion.
 * There is no weakly imposed temperature/pressure algebraic field constraint. */
public class EnergyFlashHEM{public static void main(String[] a){try{
 ModelUtil.initStandalone(false);ModelUtil.setPreference("security.comsol.allowexternallibraries","on");Model m=ModelUtil.load("energyFlash",a[0]);
 m.func("mixtureEOS").set("path",a[2]);m.func("mixtureEOS").set("threadsafe",true);
 String[] names={"hemE","hemP","hemXv","hemAg","hemCs","hemAc","eeT","eeP","eeXv","eeAg","eeCs","eeAc"};String[] args={"x","y","z"};ArrayList<String[]> funcs=new ArrayList<>(),ders=new ArrayList<>();
 for(String n:names){funcs.add(new String[]{n,"x,y,z"});for(int i=0;i<3;i++){String df=n+"_d"+i;funcs.add(new String[]{df,"x,y,z"});ders.add(new String[]{n,args[i],df+"(x,y,z)"});}}
 m.func("mixtureEOS").set("funcs",funcs.toArray(new String[0][]));m.func("mixtureEOS").set("ders",ders.toArray(new String[0][]));
 m.component("c").physics().remove("thermal");
 String[][] vars={{"eMixSpecific","(Etot/rho-u^2/2)/1[J/kg]"},{"T","eeT(eMixSpecific,lMixC,lMixA)*1[K]"},
 {"pGas","eeP(eMixSpecific,lMixC,lMixA)*1[Pa]"},{"alphaG","eeAg(eMixSpecific,lMixC,lMixA)"},{"alphaCu","eeAc(eMixSpecific,lMixC,lMixA)"},{"xV","eeXv(eMixSpecific,lMixC,lMixA)"},{"cs","eeCs(eMixSpecific,lMixC,lMixA)*1[m/s]"}};
 for(String[] v:vars)m.component("c").variable("state").set(v[0],v[1]);
 for(String so:m.sol().tags())m.sol().remove(so);m.study("std").createAutoSequences("sol");
 m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.2[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","1e-5[ns]");
 for(String f:m.sol("sol1").feature("v1").feature().tags()){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",f.equals("c_qE")||f.equals("c_qm")?"1e-3":"1");}
 for(String f:m.sol("sol1").feature("t1").feature().tags())if(m.sol("sol1").feature("t1").feature(f).getType().equals("FullyCoupled")){m.sol("sol1").feature("t1").feature(f).set("jtech","onevery");m.sol("sol1").feature("t1").feature(f).set("maxiter",20);}
 m.label("Conservative Cu-air HEM with direct energy flash; no Plasma yet");m.save(a[1]);System.out.println("ENERGY FLASH HEM READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
