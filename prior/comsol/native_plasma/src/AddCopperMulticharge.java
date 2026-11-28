import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Cu+ to Cu4+ native kinetics with matching chemistry/electron/radiation energy. */
public class AddCopperMulticharge {
 static Model m;static String comp,p,vg;
 static String nn(int q){return (comp.equals("c")?"Cu":"CuX")+"_"+q+"p";}
 static String species(int q){return (comp.equals("c")?"Cu":"CuX")+"+".repeat(q);}
 static void v(String n,String x){m.component(comp).variable(vg).set(n,x);}
 static void append(String n,String x){v(n,"("+m.component(comp).variable(vg).get(n)+")+("+x+")");}
 static String rate(int q){String key=p+".R_w"+nn(q);for(String[] row:m.component(comp).physics(p).feature(nn(q)).featureInfo("info").getInfoTable("Expression"))if(row.length>2&&row[1].equals(key))return row[2];throw new RuntimeException(key);}
 static void setRate(int q,String x){m.component(comp).physics(p).feature(nn(q)).featureInfo("info").set(p+".R_w"+nn(q),new String[]{x});}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("multiCharge",a[0]);for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  m.func().create("multicu","Interpolation");m.func("multicu").set("source","file");m.func("multicu").set("filename",a[2]+"/copper_multicharge_rates.txt");m.func("multicu").set("nargs",1);
  ArrayList<String[]> funcs=new ArrayList<>();int col=1;for(int q=1;q<=3;q++)for(String rate:new String[]{"ion","rec","rr","mean"})funcs.add(new String[]{"cu"+q+"_"+rate,Integer.toString(col++)});
  m.func("multicu").set("funcs",funcs.toArray(new String[0][]));m.func("multicu").set("interp","piecewisecubic");m.func("multicu").set("extrap","const");m.func("multicu").importData();
  double[] thresholds={20.29239,36.841,57.38};double[] energies={0,7.72638,28.01877,64.85977,122.23977};
  for(String component:new String[]{"c","outer"}){
   comp=component;p=comp.equals("c")?"plas":"plas2";vg=comp.equals("c")?"gasv":"outerGas";
   String oldCuPlus=rate(1);
   for(int q=1;q<=3;q++){
    v("multCuIon"+q,"10^cu"+q+"_ion(log10(TeV/1[V]))*1[m^3/s]");v("multCuRec"+q,"10^cu"+q+"_rec(log10(TeV/1[V]))*1[m^6/s]");v("multCuRR"+q,"10^cu"+q+"_rr(log10(TeV/1[V]))*1[m^3/s]");v("multCuMean"+q,"10^cu"+q+"_mean(log10(TeV/1[V]))*1[V]");
    String[] tag={"multIon"+q,"multRec"+q,"multRR"+q};
    String[] formula={"e+"+species(q)+"=>2e+"+species(q+1),"2e+"+species(q+1)+"=>e+"+species(q),"e+"+species(q+1)+"=>"+species(q)};
    String[] kf={"multCuIon"+q+"*N_A_const","multCuRec"+q+"*N_A_const^2","multCuRR"+q+"*N_A_const"};
    for(int i=0;i<3;i++){m.component(comp).physics(p).create(tag[i],i==2?"Reaction":"ElectronImpactReaction",2);m.component(comp).physics(p).feature(tag[i]).set("formula",formula[i]);m.component(comp).physics(p).feature(tag[i]).set("kf",kf[i]);if(i<2){m.component(comp).physics(p).feature(tag[i]).set("type","Excitation");m.component(comp).physics(p).feature(tag[i]).set("de",Double.toString(i==0?thresholds[q-1]:-thresholds[q-1]));}}
    int charge=q+1;String name=nn(charge);m.component(comp).physics(p).feature(name).set("M","Mcu");m.component(comp).physics(p).feature(name).set("n0","1e-20[1/m^3]");m.component(comp).physics(p).feature(name).set("x0","1e-40");m.component(comp).physics(p).feature(name).set("InitIon",false);
    v(p+".w"+name,"w"+name);v("nCu"+charge,p+".n_w"+name);
    v("multNet"+q,p+".ne*"+p+".n_w"+nn(q)+"*multCuIon"+q+"-"+p+".ne^2*nCu"+charge+"*multCuRec"+q+"-"+p+".ne*nCu"+charge+"*multCuRR"+q);
    append("nCuTotal","nCu"+charge);append("nHeavy","nCu"+charge);append("CvVol","1.5*k_B_const*nCu"+charge);append("Uchem","e_const*"+energies[charge]+"[V]*nCu"+charge);
    append("niZ2",(charge*charge)+"*nCu"+charge);
    append("Qei","3*me_const*N_A_const*"+p+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*"+(charge*charge)+"*nCu"+charge+"/Mcu");
    append("Qrad",p+".ne*nCu"+charge+"*multCuRR"+q+"*multCuMean"+q+"*e_const");
    append("QeRR",p+".ne*nCu"+charge+"*multCuRR"+q+"*(multCuMean"+q+"-"+thresholds[q-1]+"[V])*e_const");
   }
   setRate(1,"("+oldCuPlus+")-multNet1/N_A_const");
   for(int q=2;q<=4;q++){
    String net="multNet"+(q-1)+(q<4?"-multNet"+q:"");
    String exchange=comp.equals("c")?"(reverseMassRate*outer.plas2.wCuX_"+q+"p-forwardMassRate*plas.wCu_"+q+"p)/(Vp*Mcu)-intSurf(mdotReturn*nCu"+q+"/nCuTotal)/(Vp*Mcu)":"(c.forwardMassRate*c.plas.wCu_"+q+"p-c.reverseMassRate*plas2.wCuX_"+q+"p)/(Vp*Mcu)";
    setRate(q,"("+net+")/N_A_const+"+exchange);
   }
  }
  // Recondensing high-charge copper carries the same chemical enthalpy and
  // quasineutral electron enthalpy as counted in the reservoir energy.
  comp="c";p="plas";vg="gasv";
  for(int q=2;q<=4;q++)append("returnMeanEnergy",energies[q]+"[V]*nCu"+q+"/nCuTotal");
  String electron=m.component("c").variable("boundv").get("electronSourceEnergy");
  m.component("c").variable("boundv").set("electronSourceEnergy","("+electron+")-2.5*k_B_const*TeK*mdotReturn*N_A_const/Mcu*(2*nCu2+3*nCu3+4*nCu4)/nCuTotal");
  for(String s:m.sol().tags())m.sol().remove(s);m.study("std").feature("time").set("tlist","range(0,1[ns],203[ns])");m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","2[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.equals("c_T")?"1000":f.endsWith("En")?"1e7":f.endsWith("_pA")?"1e5":f.endsWith("_ODE1")?"1":f.endsWith("_w")?(f.endsWith("_O2_w")||f.endsWith("_O2X_w")?"1":"1e-8"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Two native Plasma reservoirs with Cu0-Cu4+ energy-consistent kinetics; finite pulse; ground ionic states only");m.save(a[1]);m.save(a[1].replace(".mph",".java"),"java");System.out.println("MULTICHARGE READY; fresh initial state, unchanged atomic rate prescriptions");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
