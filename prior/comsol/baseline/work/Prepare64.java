import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Derive a fresh initial-value case; never carry solved history into validation. */
public class Prepare64 {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);
  double oldMesh=m.param().evaluate("meshFactor"),newMesh=Double.parseDouble(a[2]);
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  for(String s:new String[]{"merged","history"})if(Arrays.asList(m.sol().tags()).contains(s))m.sol().remove(s);
  m.param().set("meshFactor",a[2]);
  if(Math.abs(oldMesh-newMesh)>1e-12)m.component("c").mesh("mesh").run();
  m.sol("sol1").feature("v1").set("control","user");
  m.sol("sol1").feature("v1").set("initmethod","init");
  String list=a.length>4?"range(0,2[ns],"+a[4]+"[ns])":"range(0,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])";
  m.study("std").feature("time").set("tlist",list);m.sol("sol1").feature("t1").set("tlist",list);
  m.sol("sol1").feature("t1").set("maxstepbdf",a[3]+"[ns]");
  m.sol("sol1").feature("t1").set("rtol","0.002");m.sol("sol1").feature("t1").set("maxorder",1);
  m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
  m.sol("sol1").feature("t1").feature("fc1").set("jtech","minimal");
  m.sol("sol1").feature("t1").feature("fc1").set("dtech","const");
  m.sol("sol1").feature("t1").feature("fc1").set("damp",1);
  m.sol("sol1").feature("t1").feature("fc1").set("maxiter",25);
  m.result().dataset("dset1").set("solution","sol1");
  m.label(new java.io.File(a[1]).getName()+" | COMSOL 6.4 fresh initial state");
  m.save(a[1]);
  System.out.println("PREPARED_64 mesh="+newMesh+" elements="+m.component("c").mesh("mesh").stat().getNumElem()+" maxstep_ns="+a[3]+" tlist="+list);
  ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
