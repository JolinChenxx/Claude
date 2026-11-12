import com.comsol.model.*;import com.comsol.model.util.*;
/** Same common-pressure closure, pre-inverted to avoid two stiff local DAEs. */
public class TabulatedMixtureHEM {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("tabulatedMixture",a[0]);
 m.func().create("mixtureEOS","Interpolation");m.func("mixtureEOS").set("source","file");m.func("mixtureEOS").set("filename",a[2]);m.func("mixtureEOS").set("struct","grid");m.func("mixtureEOS").set("nargs",3);
 String[] names={"mixE","mixP","mixXv","mixAg","mixCs","mixAc"};String[][] fun=new String[names.length][2];for(int i=0;i<names.length;i++)fun[i]=new String[]{names[i],""+(i+1)};m.func("mixtureEOS").set("funcs",fun);m.func("mixtureEOS").set("interp","linear");m.func("mixtureEOS").set("extrap","const");m.func("mixtureEOS").importData();
 String[][] vars={{"lMixR","log10(rho/1[kg/m^3])"},{"lMixY","log10(rhoC/rhoA)"},
 {"pGas","mixP(lT,lMixR,lMixY)*1[Pa]"},{"Einternal","rho*mixE(lT,lMixR,lMixY)*1[J/kg]"},
 {"alphaG","mixAg(lT,lMixR,lMixY)"},{"alphaCu","mixAc(lT,lMixR,lMixY)"},{"xV","mixXv(lT,lMixR,lMixY)"},{"cs","mixCs(lT,lMixR,lMixY)*1[m/s]"},
 {"solidFrac","alphaCu*(1-xV)*(1-flc2hs(T-1358[K],10[K]))"},
 {"Ei","(rhoCi+rhoAi)*mixE(log10(Tamb/1[K]),log10((rhoCi+rhoAi)/1[kg/m^3]),log10(rhoCi/rhoAi))*1[J/kg]"}};
 for(String[] v:vars)m.component("c").variable("state").set(v[0],v[1]);
 // Keep fixed-mesh conservative PDEs and their initial conditions from source.
 for(String so:m.sol().tags())m.sol().remove(so);m.study("std").createAutoSequences("sol");
 m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.2[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","1e-5[ns]");
 for(String f:m.sol("sol1").feature("v1").feature().tags()){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",f.equals("c_qE")||f.equals("c_qm")||f.equals("c_qr")?"1e-3":"1");}
 for(String f:m.sol("sol1").feature("t1").feature().tags())if(m.sol("sol1").feature("t1").feature(f).getType().equals("FullyCoupled")){m.sol("sol1").feature("t1").feature(f).set("jtech","onevery");m.sol("sol1").feature("t1").feature(f).set("maxiter",20);}
 m.param().set("tablePressureCheck","mixP(log10(293.15),log10(8942.230330721954),log10(8942.230330721954/1e-12))*1[Pa]");
 System.out.println("DENSE INITIAL PRESSURE TABLE CHECK "+m.param().evaluate("tablePressureCheck"));
 m.label("Conservative Cu-air common-pressure tabulated HEM; material stage without Plasma");m.save(a[1]);System.out.println("TABULATED MIXTURE HEM READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
