import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class FVInitialAudit {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);m.sol("sol1").runFromTo("st1","v1");
 m.result().numerical().create("fvcheck","Interp");m.result().numerical("fvcheck").set("data","dset1");
 m.result().numerical("fvcheck").set("coord",new double[][]{{30e-6,30e-6,1e-6},{0,1e-7,1e-7}});
 ArrayList<String> vv=new ArrayList<>(Arrays.asList("qE","Tg","plas.Te","plas.ne","Qmass","Qexchange","QelectronWall","QworkWall","vnSurf","pPl","Hdiffr","Hdiffz","plas.gflux_enr","plas.gflux_enz","plas.Cu.Jn_postr","plas.N2_1p.Ji_postr","plas.en_bnd","plas.ten_bnd","plas.enmig_bnd"));
 for(String n:Chemistry.NAMES){vv.add("plas.tfluxr_w"+n);vv.add("plas.tfluxz_w"+n);}String[] vars=vv.toArray(new String[0]);
 for(String v:vars)for(String x:new String[]{v,"d("+v+",qE)","d("+v+",Tg)"})try{m.result().numerical("fvcheck").set("expr",x);System.out.println(x+" = "+Arrays.deepToString(m.result().numerical("fvcheck").getData()));}catch(Exception e){System.out.println(x+" ERROR "+e.getMessage());}
 m.result().numerical().create("bcheck","Eval");m.result().numerical("bcheck").set("data","dset1");m.result().numerical("bcheck").selection().named("surface");
 for(String v:vars)for(String x:new String[]{v,"d("+v+",qE)","d("+v+",Tg)"})try{m.result().numerical("bcheck").set("expr",x);double[][][] d=m.result().numerical("bcheck").getData();double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;int bad=0;for(double y:d[0][0])if(Double.isFinite(y)){min=Math.min(min,y);max=Math.max(max,y);}else bad++;System.out.println("BOUND "+x+" min="+min+" max="+max+" bad="+bad);}catch(Exception e){System.out.println("BOUND "+x+" ERROR "+e.getMessage());}
 for(String f:new String[]{"wall","metal"})for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Expression"))System.out.println(f+" "+Arrays.toString(row));
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
