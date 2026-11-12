import com.comsol.model.*;import com.comsol.model.util.*;import java.io.*;import java.util.*;
public class ExportFields {
 static double gridFactor=1;
 static double[] grid(double max){ArrayList<Double> list=new ArrayList<Double>();
  for(double x=0.05;x<30;x+=0.5*gridFactor)list.add(x);for(double x=30;x<150;x+=2*gridFactor)list.add(x);for(double x=150;x<=max;x+=5*gridFactor)list.add(x);
  double[] a=new double[list.size()];for(int i=0;i<a.length;i++)a[i]=list.get(i);return a;}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);String p=a[1];String active=Arrays.asList(m.sol().tags()).contains("merged")?"merged":"sol1";double[] ts=m.sol(active).getPVals();
  gridFactor=a.length>3?Double.parseDouble(a[3]):1;if(!Double.isFinite(gridFactor)||gridFactor<=0)throw new IllegalArgumentException("Positive grid factor required");
  double lo=a.length>4?Double.parseDouble(a[4])*1e-9:-1,hi=a.length>5?Double.parseDouble(a[5])*1e-9:Double.POSITIVE_INFINITY;
  ArrayList<Integer> uid=new ArrayList<Integer>();double prev=-1;for(int i=0;i<ts.length;i++)if(ts[i]>prev+1e-18&&ts[i]>=lo-1e-18&&ts[i]<=hi+1e-18){uid.add(i+1);prev=ts[i];}if(uid.isEmpty())throw new IllegalArgumentException("No saved states in export range");double[] uts=new double[uid.size()];for(int i=0;i<uts.length;i++)uts[i]=ts[uid.get(i)-1];ts=uts;
  m.result().dataset("dset1").set("solution",active);
  boolean ale=false;try{ale=m.param().evaluate("aleOn")>.5;}catch(Exception ignored){}
  if(!ale){m.component("c").variable("v").set("zPhysical","z");m.component("c").variable("v").set("Jale","1");}
  double[] rr=grid(650),zz=grid(1000);int n=rr.length*zz.length;double[][] coord=new double[2][n];
  m.result().numerical().create("fieldEval","Interp");m.result().numerical("fieldEval").set("data","dset1");m.result().numerical("fieldEval").selection().set(new int[]{2});
  m.result().numerical("fieldEval").set("solnum",new int[]{1});m.result().numerical("fieldEval").set("expr",new String[]{"r/1[um]"});
  m.result().numerical("fieldEval").set("coord",new double[][]{{10e-6},{10e-6}});
  double check=m.result().numerical("fieldEval").getData()[0][0][0];double cscale=Math.abs(check-10)<1e-4?1e-6:1;
  for(int j=0;j<zz.length;j++)for(int i=0;i<rr.length;i++){int k=j*rr.length+i;coord[0][k]=rr[i]*cscale;coord[1][k]=zz[j]*cscale;}
  String[] ex={"Tpl","nePl","Yraw","rhoP","xionCu","urP","uzP","pPl","niZ2","zPhysical","Jale"};
  String[] evalex=ex.clone();evalex[9]="zPhysical/1[m]";if(!ale){evalex[9]="z/1[m]";evalex[10]="1";}
  m.result().numerical("fieldEval").set("coord",coord);m.result().numerical("fieldEval").set("expr",evalex);
  int nt=ts.length;if(a.length>2)nt=Math.min(nt,Integer.parseInt(a[2]));
  PrintWriter meta=new PrintWriter(p+"_fields.json");meta.print("{\"shape\":["+nt+","+ex.length+","+zz.length+","+rr.length+"],\"dtype\":\">f4\",\"coordinates_um\":{\"r\":[");
  for(int i=0;i<rr.length;i++){if(i>0)meta.print(",");meta.print(rr[i]);}meta.print("],\"z\":[");for(int i=0;i<zz.length;i++){if(i>0)meta.print(",");meta.print(zz[i]);}
  meta.print("]},\"times_s\":[");for(int i=0;i<nt;i++){if(i>0)meta.print(",");meta.print(ts[i]);}meta.print("],\"expressions\":[");for(int i=0;i<ex.length;i++){if(i>0)meta.print(",");meta.print("\""+ex[i]+"\"");}meta.println("],\"coordinate_scale\":"+cscale+",\"note\":\"Finite-element interpolation; grid spacing is not mesh resolution.\"}");meta.close();
  DataOutputStream out=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(p+"_fields.bin"),1048576));
  for(int start=0;start<nt;start+=4){int count=Math.min(4,nt-start);int[] ids=new int[count];for(int j=0;j<count;j++)ids[j]=uid.get(start+j);
   m.result().numerical("fieldEval").set("solnum",ids);double[][][] values=m.result().numerical("fieldEval").getData();
   for(int t=0;t<count;t++)for(int e=0;e<ex.length;e++)for(int k=0;k<n;k++){double v=values[e][t][k];if(Double.isNaN(v)||Double.isInfinite(v))throw new RuntimeException("Nonfinite field at t="+(start+t)+" expr="+ex[e]+" k="+k);out.writeFloat((float)v);}
   if(start%20==0)System.out.println("FIELDS "+(start+count)+"/"+nt);
  }out.close();ModelUtil.clear();System.out.println("FIELDS_EXPORTED "+nt);System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
