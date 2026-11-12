import com.comsol.model.*;import com.comsol.model.util.*;
/** Continue the same equations/mesh from a real saved state, preserving history. */
public class ResumeS4 {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);String active=java.util.Arrays.asList(m.sol().tags()).contains("merged")?"merged":"sol1";double[] ts=m.sol(active).getPVals();double start=ts[ts.length-1];
 if(java.util.Arrays.asList(m.sol().tags()).contains("history"))m.sol().remove("history");m.sol(active).copySolution("history");
 if(active.equals("merged"))m.sol().remove("merged");m.sol("sol1").clearSolutionData();
 if(!java.util.Arrays.asList(m.result().dataset().tags()).contains("dset1"))m.result().dataset().create("dset1","Solution");m.result().dataset("dset1").set("solution","sol1");
 m.param().set("resumeTime",Double.toString(start)+"[s]");String times;
 if(start<1e-6)times="range(resumeTime,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])";
 else times="range(resumeTime,10[ns],2120[ns])";
 m.study("std").feature("time").set("tlist",times);m.sol("sol1").feature("t1").set("tlist",times);
 m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","history");m.sol("sol1").feature("v1").set("solnum","last");
 if(a.length>2){m.sol("sol1").feature("t1").feature("fc1").set("jtech",a[2]);m.sol("sol1").feature("t1").feature("fc1").set("maxiter",15);}
 if(a.length>3){m.sol("sol1").feature("t1").feature("fc1").set("dtech",a[3]);m.sol("sol1").feature("t1").feature("fc1").set("maxiter",25);}
 if(a.length>4&&a[4].equals("lagdiffusion"))m.component("c").variable("v").set("Dnum","nojac(nuFactor*h*(speedP+csBound)+DfrontALE)");
 if(a.length>5)m.sol("sol1").feature("t1").feature("fc1").set("damp",Double.parseDouble(a[5]));
 m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.save(a[1]);m.save(a[1].replace(".mph","_model.java"),"java");
 System.out.println("RESUME_READY "+start+" with "+ts.length+" saved history states");ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}}
