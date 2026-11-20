import com.comsol.model.*;
import com.comsol.model.util.*;
public class WarmRefineCase {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.sol("sol1").copySolution("preheat");NativeLiquidHeat.apply(m);PlasmaTransport.apply(m);NearWallMesh.apply(m);
 String start=a.length>2?a[2]:"70[ns]";
 m.param().set("tEnd","1000[ns]");String times="range("+start+",2[ns],tEnd)";m.study("std").feature("time").set("tlist",times);
 m.sol().remove("sol1");m.study("std").createAutoSequences("sol");SolverSettings.apply(m);
 m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","preheat");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",start);
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
 String s=f.endsWith("Tg")||f.endsWith("T")?"1000":f.endsWith("umelt")?"100":f.contains("disp")?"1e-5":f.equals("c_etaSurface")?"1":null;
 if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}}
 m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxorder",2);
 if(a.length>3&&a[3].equals("yeoh"))m.component("c").common("deform").set("smoothingType","yeoh");else AddRemesh.apply(m);
 m.save(a[1]);System.out.println("WARM REFINED READY");System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}}
