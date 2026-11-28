import com.comsol.model.*;import java.util.*;
/** Reuse documented NIST/Lotz/detailed-balance data in the local native operator. */
public class LocalCopperMulticharge {
 static void append(Model m,String n,String x){m.component("c").variable("gasv").set(n,"("+m.component("c").variable("gasv").get(n)+")+("+x+")");}
 public static void apply(Model m,String data){
  m.func().create("multicu","Interpolation");m.func("multicu").set("source","file");m.func("multicu").set("filename",data+"/copper_multicharge_rates.txt");m.func("multicu").set("nargs",1);
  ArrayList<String[]> funcs=new ArrayList<>();int col=1;for(int q=1;q<=3;q++)for(String rate:new String[]{"ion","rec","rr","mean"})funcs.add(new String[]{"cu"+q+"_"+rate,Integer.toString(col++)});
  m.func("multicu").set("funcs",funcs.toArray(new String[0][]));m.func("multicu").set("interp","piecewisecubic");m.func("multicu").set("extrap","const");m.func("multicu").importData();
  double[] thresholds={20.29239,36.841,57.38},energies={28.01877,64.85977,122.23977};
  for(int q=1;q<=3;q++){
   String parent="Cu"+"+".repeat(q),child=parent+"+",name="Cu_"+(q+1)+"p",number="plas.n_w"+name;
   String[] formulas={"e+"+parent+"=>2e+"+child,"2e+"+child+"=>e+"+parent,"e+"+child+"=>"+parent};
   for(int j=0;j<3;j++){
    String tag="localCu"+q+"_"+j,key=new String[]{"ion","rec","rr"}[j];
    m.component("c").physics("plas").create(tag,j==2?"Reaction":"ElectronImpactReaction",2);
    m.component("c").physics("plas").feature(tag).set("formula",formulas[j]);
    m.component("c").physics("plas").feature(tag).set("kf","10^cu"+q+"_"+key+"(log10(TeV/1[V]))*1["+(j==1?"m^6/s":"m^3/s")+"]*N_A_const"+(j==1?"^2":""));
    if(j<2){m.component("c").physics("plas").feature(tag).set("type","Excitation");m.component("c").physics("plas").feature(tag).set("de",Double.toString(j==0?thresholds[q-1]:-thresholds[q-1]));}
   }
   m.component("c").physics("plas").feature(name).set("M","Mcu");m.component("c").physics("plas").feature(name).set("InitIon",false);
   m.component("c").physics("plas").feature(name).set("MobilityDiffusivitySpecification","ComputeMobilityAndDiffusivity");m.component("c").physics("plas").feature(name).set("Dfs","0[m^2/s]");
   m.component("c").physics("plas").feature(name).featureInfo("info").set("plas.sg_flux_w"+name,new String[]{"0[kg/(m^2*s)]"});
   append(m,"nCuTotal",number);append(m,"nHeavy",number);append(m,"CvVol","1.5*k_B_const*"+number);append(m,"Uchem",energies[q-1]+"[V]*e_const*"+number);append(m,"niZ2",((q+1)*(q+1))+"*"+number);
   // Qei and Qff already depend on niZ2; do not add a second exchange term.
   String rr="plas.ne*"+number+"*10^cu"+q+"_rr(log10(TeV/1[V]))*1[m^3/s]",mean="10^cu"+q+"_mean(log10(TeV/1[V]))*1[V]";
   append(m,"Qrad",rr+"*("+mean+")*e_const");append(m,"QeRR",rr+"*("+mean+"-"+thresholds[q-1]+"[V])*e_const");
  }
 }
}
