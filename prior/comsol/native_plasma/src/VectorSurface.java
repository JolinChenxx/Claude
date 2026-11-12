import com.comsol.model.*;
import com.comsol.model.util.*;
public class VectorSurface {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 String start=a.length>2?a[2]:"130[ns]";m.sol("sol1").copySolution("beforeVector");
 m.component("c").physics().create("radialSurfaceODE","BoundaryODE","g",new String[]{"xiSurface"});m.component("c").physics("radialSurfaceODE").selection().named("surface");
 for(String ph:new String[]{"radialSurfaceODE","surfaceODE"}){
  m.component("c").physics(ph).prop("ShapeProperty").set("frame","material");m.component("c").physics(ph).prop("ShapeProperty").set("shapeFunctionType","shlag");m.component("c").physics(ph).prop("ShapeProperty").set("valueType","real");m.component("c").physics(ph).prop("ShapeProperty").set("order",1);m.component("c").physics(ph).feature("dode1").set("da","r/Rdom");
 }
 m.component("c").physics("radialSurfaceODE").feature("dode1").set("f","r/Rdom*meltOn*(urCu-mdot*ncr/rhoCu)/1[um]");
 m.component("c").physics("surfaceODE").feature("dode1").set("f","r/Rdom*meltOn*(uzCu-mdot*ncz/rhoCu)/1[um]");
 // Reference-frame normal fixes orientation without forcing ncz positive.
 m.component("c").variable("boundv").set("ncr","nr*if(nZ>=0,1,-1)");m.component("c").variable("boundv").set("ncz","nz*if(nZ>=0,1,-1)");
 m.component("c").variable("boundv").set("Qsurface","Acu*Ipeak*exp(-2*(r/w0)^2)*exp(-4*log(2)*((t-tp)/tau)^2)*max(ncz,0)");
 m.component("c").common("meshSurf").set("prescribedMeshDisplacement",new String[]{"xiSurface*1[um]","0","etaSurface*1[um]-Z"});
 for(String f:new String[]{"meshSurf","meshAxis","meshFixed"})m.component("c").common(f).set("constraintMethod","nodal");
 NativeLiquidHeat.apply(m);m.component("c").common("deform").set("smoothingType","yeoh");
 m.param().set("tEnd","1000[ns]");String times="range("+start+",2[ns],tEnd)";m.study("std").feature("time").set("tlist",times);m.sol().remove("sol1");m.study("std").createAutoSequences("sol");SolverSettings.apply(m);
 m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","beforeVector");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",start);
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String s=f.endsWith("Tg")||f.endsWith("T")?"1000":f.endsWith("umelt")?"100":f.contains("disp")?"1e-5":f.endsWith("Surface")?"1":null;
  if(s!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",s);}}
 m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("bwinitstepfrac","0.1");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");m.sol("sol1").feature("t1").set("maxorder",2);
 AddRemesh.apply(m);m.sol("sol1").feature("t1").feature("remesh").set("consistentremesh",true);
 m.save(a[1]);System.out.println("VECTOR SURFACE READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
