import com.comsol.model.*;import com.comsol.model.util.*;
/** Exact column-wise ALE mapping for a single-valued graph surface.
 * r=R; z=Z+eta(R)*(1-Z/Ztop) in gas, or *(1+Z/Dcu) in copper.
 * Its Jacobian is independent of surface slope, preserving nanometre layers.
 */
public class ColumnALE {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String start=a.length>2?a[2]:"123[ns]";String oldSol=a.length>3?a[3]:"continued";
m.component("c").cpl().create("surfaceHeight","GeneralExtrusion");m.component("c").cpl("surfaceHeight").selection().geom("g",1);m.component("c").cpl("surfaceHeight").selection().named("surface");m.component("c").cpl("surfaceHeight").set("opname","surfaceHeight");m.component("c").cpl("surfaceHeight").set("srcframe","material");m.component("c").cpl("surfaceHeight").set("usesrcmap",true);m.component("c").cpl("surfaceHeight").set("srcmap",new String[]{"R","0"});m.component("c").cpl("surfaceHeight").set("dstmap",new String[]{"R","0"});
for(String f:m.component("c").common().tags())m.component("c").common().remove(f);
m.component("c").common().create("columnMesh","PrescribedDeformation");m.component("c").common("columnMesh").selection().all();m.component("c").common("columnMesh").set("prescribedDeformation",new String[]{"withsol('"+oldSol+"',r-R,setval(t,"+start+"))","0","withsol('"+oldSol+"',z-Z,setval(t,"+start+"))+(surfaceHeight(etaSurface)-surfaceHeight(withsol('"+oldSol+"',etaSurface,setval(t,"+start+"))))*1[um]*if(Z>=0,1-Z/Zdom,1+Z/Dcu)"});
m.sol("sol1").feature("t1").feature("remesh").active(false);
m.save(a[1]);System.out.println("EXACT COLUMN ALE READY, SINGLE-VALUED SURFACE ONLY");System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}

