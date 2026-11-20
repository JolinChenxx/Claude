import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.*;
public class R2Resume {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);double[] ts=m.sol("sol2").getPVals();double start=ts[ts.length-1];
  if(start<159.9e-9)throw new RuntimeException("Pilot did not reach 160 ns");
  if(java.util.Arrays.asList(m.sol().tags()).contains("r2history"))m.sol().remove("r2history");
  m.sol("sol2").copySolution("r2history");m.sol("sol2").clearSolutionData();
  m.param().set("r2resume",Double.toString(start)+"[s]");String times="range(r2resume,2[ns],"+a[2]+"[ns])";
  m.study("r2study").feature("time").set("tlist",times);m.sol("sol2").feature("t1").set("tlist",times);
  m.sol("sol2").feature("v1").set("control","user");m.sol("sol2").feature("v1").set("initmethod","sol");
  m.sol("sol2").feature("v1").set("initsol","r2history");m.sol("sol2").feature("v1").set("solnum","last");
  m.sol("sol2").feature("t1").set("initialstepbdf","0.05[ns]");
  if(Double.parseDouble(a[2])>200)m.sol("sol2").feature("t1").set("maxstepbdf","2[ns]");
  if(a.length>3){m.param().set("electronConduct",a[3]);m.sol("sol2").feature("t1").set("estrat","exclude");}
  m.modelPath(new File(a[1]).getParent());m.label(new File(a[1]).getName());m.save(a[1]);
  System.out.println("RESUME_READY "+start+" end "+a[2]);ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
