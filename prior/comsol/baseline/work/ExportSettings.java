import com.comsol.model.*;import com.comsol.model.util.*;import java.io.*;
/** Actual saved model parameters and final solver strategy, not inferred defaults. */
public class ExportSettings {
 static String esc(String s){return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);PrintWriter w=new PrintWriter(a[1]);w.print("{\"parameters\":{");
  String[] keys={"Ein","Asurf","w0","tau","tp","tEnd","Rdom","Zdom","Dcu","nuFactor","nuLocal","meshFactor","meshSmoothZ","netEvap","aleOn","rhoAmb","eAmb","pAmb","nuLight"};
  for(int i=0;i<keys.length;i++){String k=keys[i];if(i>0)w.print(",");w.print("\""+k+"\":{\"expression\":\""+esc(m.param().get(k))+"\",\"SI\":"+m.param().evaluate(k)+"}");}
  w.print("},\"solver\":{");String[] tk={"maxstepbdf","rtol","maxorder","atolglobalfactor"};
  for(int i=0;i<tk.length;i++){if(i>0)w.print(",");w.print("\""+tk[i]+"\":\""+esc(m.sol("sol1").feature("t1").getString(tk[i]))+"\"");}
  for(String k:new String[]{"jtech","dtech","maxiter","damp"})w.print(",\""+k+"\":\""+esc(m.sol("sol1").feature("t1").feature("fc1").getString(k))+"\"");
  w.println("},\"note\":\"Actual saved settings. Earlier real checkpoints retain their recorded Newton strategies; equations, tolerances and physical discretization unchanged across continuations.\"}");w.close();ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
