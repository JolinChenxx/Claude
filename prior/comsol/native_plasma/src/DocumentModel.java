import com.comsol.model.*;import com.comsol.model.util.*;import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;
/** Read a saved MPH and export its current settings for the explanatory document. */
public class DocumentModel {
 public static void main(String[] a){try{
  System.setOut(new PrintStream(new FileOutputStream(a[1]+"_settings.txt"),true,"UTF-8"));
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("documentation",a[0]);
  System.out.println("MODEL "+a[0]+" LABEL "+m.label());
  for(String n:m.param().varnames())System.out.println("PARAM\t"+n+"\t"+m.param().get(n));
  for(String so:m.sol().tags()){System.out.println("SOLUTION "+so);try{double[] t=m.sol(so).getPVals();System.out.println("TIMES "+Arrays.toString(t));}catch(Exception e){System.out.println("NO SAVED VALUES");}}
  for(String co:m.component().tags()){
   System.out.println("COMPONENT "+co);
   for(String g:m.component(co).variable().tags())for(String n:m.component(co).variable(g).varnames())System.out.println("VAR\t"+co+"."+g+"\t"+n+"\t"+m.component(co).variable(g).get(n));
   for(String ph:m.component(co).physics().tags()){
    System.out.println("PHYSICS "+co+"/"+ph+" "+m.component(co).physics(ph).getType());
    for(String pr:m.component(co).physics(ph).prop().tags())InspectFeatures.dump(m.component(co).physics(ph).prop(pr),"PROP "+co+"/"+ph+"/"+pr);
    for(String f:m.component(co).physics(ph).feature().tags()){
     InspectFeatures.dump(m.component(co).physics(ph).feature(f),"FEATURE "+co+"/"+ph+"/"+f+" "+m.component(co).physics(ph).feature(f).getType());
     for(String[] row:m.component(co).physics(ph).feature(f).featureInfo("info").getInfoTable("Weak"))System.out.println("WEAK "+ph+"/"+f+" "+Arrays.toString(row));
    }
   }
   for(String cm:m.component(co).common().tags())InspectFeatures.dump(m.component(co).common(cm),"COMMON "+co+"/"+cm);
  }
  m.save(a[1]+".java","java");System.out.println("DOCUMENT EXPORT COMPLETE");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
