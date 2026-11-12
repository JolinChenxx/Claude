import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
public class AuditColumnMap {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("mapAudit",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 InspectFeatures.dump(m.component("c").common("columnMesh"),"columnMesh");
 InspectFeatures.dump(m.component("c").physics("surfaceODE").prop("ShapeProperty"),"surfaceShape");
 String[] sels={"g_gas_dom","g_cu_dom","g_wallstrip_dom"};ArrayList<String> ex=new ArrayList<>();
 for(int i=0;i<sels.length;i++){
  String op="minColumn"+i;m.component("c").cpl().create(op,"Minimum");m.component("c").cpl(op).selection().named(sels[i]);m.component("c").cpl(op).set("opname",op);
  for(String expr:new String[]{"spatial.relVol","spatial.relVol,R","spatial.relVol,Z","spatial.relVol,r","spatial.relVol,z"})ex.add("c."+op+"("+expr+")");
 }
 m.result().numerical().create("columnAudit","EvalGlobal");m.result().numerical("columnAudit").set("data","dset1");m.result().numerical("columnAudit").set("expr",ex.toArray(new String[0]));m.sol(saved).updateSolution();ExportNative.csv(a[1],ex.toArray(new String[0]),m.sol(saved).getPVals(),m.result().numerical("columnAudit").getReal());System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
