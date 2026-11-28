import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
import java.io.*;

/** Physical source budgets in the wall layer and expanding bulk; no model edits saved. */
public class ExportMechanism {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("mech",a[0]);
  String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
  for(String op:new String[]{"intGas","intSurf","intCu"}){
   m.component("c").cpl(op).set("axisym",true);m.component("c").cpl(op).set("intorder",8);
  }
  m.component("c").variable().create("mechVars");
  m.component("c").variable("mechVars").selection().named("g_gas_dom");
  String[] masks={"All","Wall","Near","Far"};
  String[] maskExpr={"1","if(z<2[um],1,0)","if(z>=2[um]&&z<50[um],1,0)","if(z>=50[um],1,0)"};
  for(int i=0;i<masks.length;i++)m.component("c").variable("mechVars").set("mech"+masks[i],maskExpr[i]);
  String ion="0[1/(m^3*s)]",rec="0[1/(m^3*s)]",rr="0[1/(m^3*s)]";
  for(String n:new String[]{"Cu","CuD5","CuD3","CuP1","CuP3","CuH3","CuH5"}){
   ion+="+10^lr_"+n+"_ion(log10(TeV/1[V]))*1[m^3/s]*plas.ne*plas.n_w"+n;
   rec+="+10^lr_"+n+"_3rec(log10(TeV/1[V]))*1[m^6/s]*plas.ne^2*plas.n_wCu_1p";
   rr+="+10^lr_"+n+"_rr(log10(TeV/1[V]))*1[m^3/s]*plas.ne*plas.n_wCu_1p";
  }
  m.component("c").variable("mechVars").set("mechCuIon",ion);
  m.component("c").variable("mechVars").set("mechCu3Rec",rec);
  m.component("c").variable("mechVars").set("mechCuRR",rr);
  m.component("c").variable("mechVars").set("mechElecLoss","QeAttach/(1.5*e_const*TeV)");
  m.component("c").variable("mechVars").set("mechCompression","-pe*divUMass");
  m.component("c").variable("mechVars").set("mechAbsSne","abs(SneNumerical)");
  m.component("c").variable("mechVars").set("mechVisible","4*pi*jVisible");
  String[] fields={"rhoCopper","plas.ne","nCu_1p","Ue","Qlaser","plas.Pcap","plas.Sen","Qei","QeRR","QeAttach","Qff","Qrad","mechVisible","mechCompression","mechCuIon","mechCu3Rec","mechCuRR","mechElecLoss","plas.Re","SneNumerical","mechAbsSne","QnumCuExc"};
  ArrayList<String> ex=new ArrayList<>();
  for(String mask:masks)for(String f:fields)ex.add("c.intGas(c.mech"+mask+"*c."+f+")");
  for(String f:new String[]{"mdotEvap","mdotReturn","mdot","Qsurface","Qmass","Qexchange","QelectronWall","QworkWall","Qthermion","Gtherm","GeIn","Iwall"})ex.add("c.intSurf(c."+f+")");
  if(Arrays.asList(m.component("c").variable().tags()).contains("surfaceFeed")){
   m.component("c").cpl().create("mechSurfMax","Maximum");m.component("c").cpl("mechSurfMax").selection().geom("g",1);m.component("c").cpl("mechSurfMax").selection().named("surface");m.component("c").cpl("mechSurfMax").set("opname","mechSurfMax");
   for(String n:new String[]{"Cu","CuD5","CuD3","CuP1","CuP3","CuH3","CuH5","Cu_1p"})ex.add("c.intSurf(c.Gemit_"+n+")");
   ex.add("c.intSurf(c.QemitStates)");
   ex.add("c.mechSurfMax(c.emitFraction_Cu_1p)");
  }
  ex.add("c.intGas(c.rhoCopper*r)");ex.add("c.intGas(c.rhoCopper*z)");
  for(int temp:new int[]{5000,6000,6550,7250,8440}){
   ex.add("c.intCu(if(c.T>"+temp+"[K],rhoCu,0[kg/m^3]))");
   ex.add("c.intCu(if(c.T>"+temp+"[K],rhoCu*c.hCond,0[J/m^3]))");
  }
  for(int radius:new int[]{10,25,50,100,200,400}){
   String name="mechR"+radius;m.component("c").variable("mechVars").set(name,"if(r>"+radius+"[um],rhoCopper,0[kg/m^3])");ex.add("c.intGas(c."+name+")");
  }
  if(Arrays.asList(m.component("c").physics().tags()).contains("flatLedger")){
   ex.add("c.flatMassNg*1e-12[kg]");ex.add("c.flatRemovedHuJ*1e-6[J]");
  }
  if(Arrays.asList(m.func().tags()).contains("tailRates")){
   for(String mask:masks)for(String f:new String[]{"tailPopulation","tailQphoto","tailQrad","tailNetRate","tailQelectron"})ex.add("c.intGas(c.mech"+mask+"*c."+f+")");
   m.component("c").cpl().create("mechMax","Maximum");m.component("c").cpl("mechMax").selection().named("g_gas_dom");m.component("c").cpl("mechMax").set("opname","mechMax");
   m.component("c").variable("mechVars").set("tailRelevantFraction","if(nCuP3>1e15[1/m^3]&&Tg>1000[K],tailFraction,0)");
   m.component("c").variable("mechVars").set("tailRelevantLifetime","if(nCuP3>1e15[1/m^3]&&Tg>1000[K],1/tailUpperLoss,0[s])");
   ex.add("c.mechMax(c.tailRelevantFraction)");ex.add("c.mechMax(c.tailRelevantLifetime)");
  }
  // Record the native source definitions so decomposed budgets can be checked
  // against the actual assembled equations, including external power terms.
  try(PrintWriter p=new PrintWriter(a[1].replace(".csv","_definitions.txt"))){
   for(String tag:new String[]{"pes1","power","bulkconv","Cuion","Cu3rec","CuP3_up","N2_0","elasticAtomCu"}){
    if(!Arrays.asList(m.component("c").physics("plas").feature().tags()).contains(tag))continue;
    for(String[] row:m.component("c").physics("plas").feature(tag).featureInfo("info").getInfoTable("Expression")){
     String s=Arrays.toString(row);if(s.matches(".*(plas.Sen|plas.Re|plas.Pcap|rr_|R_|Qgen|kf|de).*"))p.println(tag+" "+s);
    }
   }
  }
  m.sol(saved).updateSolution();m.result().numerical().create("mechanism","EvalGlobal");
  m.result().numerical("mechanism").set("data","dset1");m.result().numerical("mechanism").set("expr",ex.toArray(new String[0]));
  double[] allTimes=m.sol(saved).getPVals();
  int stride=a.length>2?Integer.parseInt(a[2]):1;
  ArrayList<Integer> indices=new ArrayList<>();for(int i=0;i<allTimes.length;i+=stride)indices.add(i);
  if(indices.get(indices.size()-1)!=allTimes.length-1)indices.add(allTimes.length-1);
  int[] solnum=new int[indices.size()];double[] times=new double[indices.size()];
  for(int i=0;i<indices.size();i++){solnum[i]=indices.get(i)+1;times[i]=allTimes[indices.get(i)];}
  m.result().numerical("mechanism").set("solnum",solnum);
  System.out.println("MECHANISM EVALUATING "+times.length+" selected times");
  ExportNative.csv(a[1],ex.toArray(new String[0]),times,m.result().numerical("mechanism").getReal());
  System.out.println("MECHANISM EXPORTED "+ex.size()+" quantities from "+saved);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
