import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
import java.io.*;
/** Surface-following sampling only; the saved solution is never changed. */
public class ExportMovingOptics {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("movingOptics",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
  double[] all=m.sol(saved).getPVals();ArrayList<Integer> selected=new ArrayList<>();
  double minimum=a.length>2?Double.parseDouble(a[2]):112.;
  for(int i=0;i<all.length;i++)if(all[i]*1e9>=minimum-1e-6)selected.add(i);
  if(selected.isEmpty())selected.add(all.length-1);
  double[] ts=new double[selected.size()];for(int i=0;i<ts.length;i++)ts[i]=all[selected.get(i)];
  double[] r=ExportNative.grid(1000);TreeSet<Double> near=new TreeSet<>();for(double z:ExportNative.grid(1000))near.add(z);
  for(double z:new double[]{.0001,.0005,.001,.002,.005,.01,.02,.05,.2,.5})near.add(z);
  double[] z=near.stream().mapToDouble(Double::doubleValue).toArray();
  String[] ex={"Tg","TeK","plas.ne","rhoCopper","rhoP","urP","uzP","pPl","plas.V","Qlaser","Qrad","Qff","nCuTotal","niZ2","jline3","jline4","jline5","jline6","tauLine3/w0","tauLine4/w0","tauLine5/w0","tauLine6/w0","nCu_1p","nCuH3","nCuH5"};
  if(Arrays.asList(m.func().tags()).contains("tailRates")){ArrayList<String> full=new ArrayList<>(Arrays.asList(ex));full.addAll(Arrays.asList("tailPopulation","tailFraction","tailUpperLoss","tailPhotoRate","tailQphoto","tailNetRate","tailQelectron"));ex=full.toArray(new String[0]);}
  m.result().numerical().create("mh","Interp");m.result().numerical("mh").set("data","dset1");m.result().numerical("mh").selection().named("g_gas_dom");m.result().numerical("mh").set("expr",new String[]{"surfaceHeight(etaSurface)"});
  double[][] hx=new double[2][r.length];for(int j=0;j<r.length;j++){hx[0][j]=r[j]*1e-6;hx[1][j]=500e-6;}
  m.result().numerical("mh").set("coord",hx);
  m.result().numerical().create("mf","Interp");m.result().numerical("mf").set("data","dset1");m.result().numerical("mf").selection().named("g_gas_dom");m.result().numerical("mf").set("expr",ex);
  double[][] heights=new double[ts.length][];int n=r.length*z.length;double[][] xy=new double[2][n];
  try(DataOutputStream f=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(a[1]+"_fields.bin")))){
   for(int it=0;it<ts.length;it++){
    int solnum=selected.get(it)+1;m.result().numerical("mh").set("solnum",new int[]{solnum});heights[it]=m.result().numerical("mh").getData()[0][0];
    for(int j=0;j<z.length;j++)for(int k=0;k<r.length;k++){int p=j*r.length+k;xy[0][p]=r[k]*1e-6;xy[1][p]=(heights[it][k]+z[j])*1e-6;}
    m.result().numerical("mf").set("coord",xy);m.result().numerical("mf").set("solnum",new int[]{solnum});double[][][] data=m.result().numerical("mf").getData();
    for(int e=0;e<ex.length;e++)for(int k=0;k<n;k++)f.writeFloat((float)data[e][0][k]);
    System.out.println("MAPPED OPTICS "+(it+1)+"/"+ts.length+" time_ns="+ts[it]*1e9);
   }
  }
  try(PrintWriter p=new PrintWriter(a[1]+"_fields.json")){
   p.print("{\"shape\":["+ts.length+","+ex.length+","+z.length+","+r.length+"],\"dtype\":\">f4\",\"times_s\":"+Arrays.toString(ts)+",\"r_um\":"+Arrays.toString(r)+",\"z_um\":"+Arrays.toString(z)+",\"expressions\":[\""+String.join("\",\"",ex)+"\"],\"surface_height_um\":[");
   for(int i=0;i<ts.length;i++){if(i>0)p.print(",");p.print(Arrays.toString(heights[i]));}
   p.println("],\"coordinates\":\"z_um is vertical distance above the actual interface; physical z = surface_height_um + z_um\",\"note\":\"Nondeforming radial columns, exact COMSOL surface extrusion; no optical ray approximation is made at export.\"}");
  }
  System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
