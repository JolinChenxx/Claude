import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class FluxIdentityAudit {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 m.result().numerical().create("fid","EvalGlobal");m.result().numerical("fid").set("data","dset1");m.result().numerical("fid").set("solnum",new int[]{m.sol(saved).getPVals().length});
 for(String op:new String[]{"intGas","intSurf"})m.component("c").cpl(op).set("axisym",true);
 ArrayList<String> ex=new ArrayList<>();for(String n:new String[]{"Cu","N2","O2_1m"})for(String d:new String[]{"r","z"}){ex.add("c.intGas(abs(c.plas.tflux"+d+"_w"+n+"-c.plas.cflux"+d+"_w"+n+"-c.plas.j"+d+"_w"+n+"))");ex.add("c.intGas(abs(c.plas.j"+d+"_w"+n+"))");}
 ex.addAll(Arrays.asList("c.intSurf(c.Gtherm-c.GeIn)","c.intGas(d(c.plas.ne,TIME))","c.intGas(c.plas.Re)","c.intGas(c.plas.ne)"));
 m.sol(saved).updateSolution();m.result().numerical("fid").set("expr",ex.toArray(new String[0]));
 for(int q:new int[]{2,4,6,10,16}){for(String op:new String[]{"intGas","intSurf"})m.component("c").cpl(op).set("intorder",q);m.sol(saved).updateSolution();System.out.println("ORDER "+q);double[][] x=m.result().numerical("fid").getReal();for(int i=0;i<ex.size();i++)System.out.println(ex.get(i)+" = "+Arrays.toString(x[i]));}
 for(String p:m.component("c").physics("plas").prop().tags()){if(p.toLowerCase().contains("quadr")||p.toLowerCase().contains("shape"))InspectFeatures.dump(m.component("c").physics("plas").prop(p),p);}
 System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
