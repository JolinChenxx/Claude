import com.comsol.model.*;import com.comsol.model.util.*;import java.io.*;import java.util.*;
public class ExportReactionOperator{public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("reactionExport",a[0]);String s=ExportNative.savedSolution(m);ExportNative.dataset(m,s);double[] times=m.sol(s).getPVals();
 ArrayList<String> ex=new ArrayList<>(Arrays.asList("Tg","TeK","plas.ne","plas.en","rhoP","rhoCopper","nHeavy","pPl","Uchem","Ue","CvVol*Tg","qrE*eScale","Qrad","Qlaser","jVisible","plas.scharge","Qei","QeRR","QeAttach","alphaLaser"));
 for(String n:Chemistry.NAMES)ex.add("plas.n_w"+n);
 String[] expr=ex.toArray(new String[0]);double[][] coords=new double[2][16];for(int i=0;i<4;i++)for(int j=0;j<4;j++){coords[0][i*4+j]=(i+.5)*5e-6;coords[1][i*4+j]=(j+.5)*5e-6;}
 m.result().numerical().create("rx","Interp");m.result().numerical("rx").set("data","dset1");m.result().numerical("rx").set("coord",coords);m.result().numerical("rx").set("expr",expr);
 try(PrintWriter f=new PrintWriter(a[1]+"_fields.json")){f.println("{\"times_s\":"+Arrays.toString(times)+",\"shape\":["+times.length+","+expr.length+",16],\"expressions\":[\""+String.join("\",\"",expr)+"\"],\"dtype\":\">f8\"}");}
 try(DataOutputStream f=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(a[1]+"_fields.bin")))){for(int i=0;i<times.length;i++){m.result().numerical("rx").set("solnum",new int[]{i+1});double[][][] d=m.result().numerical("rx").getData();for(int k=0;k<expr.length;k++)for(int j=0;j<16;j++)f.writeDouble(d[k][0][j]);}}
 System.out.println("REACTION EXPORTED "+times[times.length-1]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
