import com.comsol.model.*;
import com.comsol.model.util.*;
public class ContinueCase {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 double[] ts=m.sol("sol1").getPVals();double start=a.length>4?Double.parseDouble(a[4])*1e-9:ts[ts.length-1];
 String previous="previous";if(java.util.Arrays.asList(m.sol().tags()).contains(previous))previous="previous"+m.sol().tags().length;
 m.sol("sol1").copySolution(previous);
 m.param().set("tEnd",a[2]);String times="range("+start+",2[ns],tEnd)";
 m.study("std").feature("time").set("tlist",times);
 m.sol("sol1").feature("t1").set("tlist",times);
 m.sol("sol1").feature("v1").set("control","user");
 m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol",previous);
 m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",Double.toString(start));
 m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
 m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","2[ns]");
 if(a.length>3&&a[3].equals("remesh"))AddRemesh.apply(m);
 if(a.length>3&&a[3].equals("yeoh")){NativeLiquidHeat.apply(m);m.component("c").common("deform").set("smoothingType","yeoh");m.component("c").common("deform").set("stiffeningFactor","10");}
 m.save(a[1]);System.out.println("CONTINUE READY FROM "+start+" retaining "+previous);System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}

