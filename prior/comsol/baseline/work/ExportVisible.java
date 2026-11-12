import com.comsol.model.*;import com.comsol.model.util.*;import java.io.*;import java.util.*;
/** Read-only full-domain visible LTE emission bound, including actual ALE J. */
public class ExportVisible {
 static Model m;static void v(String n,String s){m.component("c").variable("v").set(n,s);}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("M",a[0]);String active=Arrays.asList(m.sol().tags()).contains("merged")?"merged":"sol1";
  double[] all=m.sol(active).getPVals();ArrayList<Integer> ids=new ArrayList<Integer>();double prev=-1;for(int i=0;i<all.length;i++)if(all[i]>prev+1e-18){ids.add(i+1);prev=all[i];}
  int[] uid=new int[a.length>3?1:ids.size()];for(int i=0;i<uid.length;i++)uid[i]=a.length>3?ids.get(ids.size()-1):ids.get(i);
  m.func().create("vcoeff","Interpolation");m.func("vcoeff").set("source","file");m.func("vcoeff").set("filename",a[2]);m.func("vcoeff").set("struct","spreadsheet");m.func("vcoeff").set("nargs",1);
  String[][] funcs=new String[15][2];for(int i=0;i<15;i++)funcs[i]=new String[]{i<6?"visP"+i:"visQ"+(i-6),Integer.toString(i+1)};m.func("vcoeff").set("funcs",funcs);m.func("vcoeff").set("interp","linear");m.func("vcoeff").importData();
  v("visS","2*(2*pi*9.1093837139e-31[kg]*kb*Tpl/hplanck^2)^1.5/max(nePl,1e-40[1/m^3])");
  String[][] chi={{"7.72638","20.29239"},{"14.53413","29.60125"},{"13.618055","35.12112"}};
  for(int j=0;j<3;j++)for(int s=0;s<2;s++)v("visR"+j+s,"visS*visQ"+(3*j+s+1)+"(Tpl/1[K])/visQ"+(3*j+s)+"(Tpl/1[K])*exp(-"+chi[j][s]+"[eV]/(kb*Tpl))");
  v("visN0","rhoSafe*Ycu/mCu");v("visN1","rhoSafe*(1-Ycu)*(.79*14.0067/(.79*14.0067+.21*15.999))/(14.0067*1.66053906660e-27[kg])");v("visN2","rhoSafe*(1-Ycu)*(.21*15.999/(.79*14.0067+.21*15.999))/(15.999*1.66053906660e-27[kg])");
  v("visn00","visN0*(1-xionCu)");v("visn01","visN0*xionCu/(1+visR01)");
  String[] mass={"14.0067","15.999"},diss={"9.759","5.116"},tr={"2.88","2.07"},tv={"3390","2270"},gg={"1","3"};
  for(int j=1;j<3;j++){
   int k=j-1;v("visF"+j,"1+visR"+j+"0+visR"+j+"0*visR"+j+"1");
   v("visM"+j,gg[k]+"*Tpl/(2*"+tr[k]+"[K])/(1-exp(-"+tv[k]+"[K]/Tpl))");
   v("visK"+j,"max((pi*"+mass[k]+"*1.66053906660e-27[kg]*kb*Tpl/hplanck^2)^1.5*visQ"+(3*j)+"(Tpl/1[K])^2/visM"+j+"*exp(-"+diss[k]+"[eV]/(kb*Tpl)),1e-260[1/m^3])");
   v("visn"+j+"0","2*visN"+j+"/(visF"+j+"+sqrt(visF"+j+"^2+8*visN"+j+"/visK"+j+"))");
   v("visn"+j+"1","visn"+j+"0*visR"+j+"0");
  }
  String total="0[W/m^3]";for(int j=0;j<3;j++)for(int s=0;s<2;s++)total+="+visn"+j+s+"*visP"+(j*2+s)+"(Tpl/1[K])*1[W]";v("visLineQ",total);
  // Stored solutions need a refreshed expression context for added diagnostics.
  m.sol(active).updateSolution();
  m.result().dataset().create("visdata","Solution");m.result().dataset("visdata").set("solution",active);
  m.result().numerical().create("visint","IntSurface");m.result().numerical("visint").selection().set(2);m.result().numerical("visint").set("data","visdata");m.result().numerical("visint").set("solnum",uid);m.result().numerical("visint").set("intvolume",true);m.result().numerical("visint").set("intorderactive",true);m.result().numerical("visint").set("intorder",12);
  m.result().numerical("visint").set("expr",new String[]{"c.Jale*c.visLineQ","Jale*Qrad*(exp(-hplanck*c_const/(780[nm]*kb*Tpl))-exp(-hplanck*c_const/(380[nm]*kb*Tpl)))","Jale*Qrad"});
  double[][] d=m.result().numerical("visint").getReal();PrintWriter out=new PrintWriter(a[1]);out.println("t_s,visible_line_emission_W,visible_ff_emission_W,all_frequency_ff_emission_W");for(int i=0;i<uid.length;i++)out.println(all[uid[i]-1]+","+d[0][i]+","+d[1][i]+","+d[2][i]);out.close();
  ModelUtil.clear();System.out.println("VISIBLE_EXPORTED "+uid.length);System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
