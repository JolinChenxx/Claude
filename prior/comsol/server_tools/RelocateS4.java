import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.File;
import java.util.Arrays;
/** Load/rebind/save only. No study or solver is executed. */
public class RelocateS4 {
  static double[] probes(Model m){
    double[] out=new double[135];int i=0;
    for(String f:new String[]{"logTpl","logPrho","logNe","logNiZ2","xCuLTE"})
      for(double e:new double[]{4.2,5.8,7.5})for(double r:new double[]{-5,0,2})for(double y:new double[]{0,.5,1})
        out[i++]=m.param().evaluate(f+"("+e+","+r+","+y+")");
    return out;
  }
  public static void main(String[] a) {try {
    if(a.length!=4)throw new IllegalArgumentException("input output lte_table project_work");
    ModelUtil.initStandalone(false);
    Model m=ModelUtil.load("M",a[0]);
    String[] tags=m.sol().tags();
    String[] before=new String[tags.length];
    for(int i=0;i<tags.length;i++) {
      try {before[i]=Arrays.toString(m.sol(tags[i]).getPVals());}
      catch(Exception e){before[i]="EMPTY";}
    }
    double[] oldValues=probes(m);
    int oldElements=m.component("c").mesh("mesh").stat().getNumElem();
    String[][] names=m.func("lte").getStringMatrix("funcnametable");
    m.modelPath(new File(a[3]).getAbsolutePath());
    // 6.4 converts imported 6.2 interpolation data into a model resource.
    // Import is only supported after explicitly selecting the user file source.
    m.func("lte").set("sourcetype","user");
    m.func("lte").set("source","file");
    m.func("lte").set("filename",new File(a[2]).getAbsolutePath());
    m.func("lte").set("funcnametable",names);
    m.func("lte").importData();
    double[] newValues=probes(m);double maxError=0;
    for(int i=0;i<oldValues.length;i++){
      double error=Math.abs(newValues[i]-oldValues[i])/Math.max(1,Math.abs(oldValues[i]));
      if(!Double.isFinite(error)||error>1e-12)throw new IllegalStateException("LTE value changed at probe "+i+": "+error);
      maxError=Math.max(maxError,error);
    }
    if(oldElements!=m.component("c").mesh("mesh").stat().getNumElem())throw new IllegalStateException("Mesh changed");
    for(int i=0;i<tags.length;i++) {
      String now;try {now=Arrays.toString(m.sol(tags[i]).getPVals());}
      catch(Exception e){now="EMPTY";}
      if(!before[i].equals(now))throw new IllegalStateException("Solution times changed: "+tags[i]);
    }
    m.save(a[1]);
    System.out.println("LTE_PROBES 135 max_relative_error="+maxError+" mesh_elements="+oldElements);
    System.out.println("RELOCATED_WITHOUT_SOLVING "+a[0]+" -> "+a[1]);
    ModelUtil.clear();System.exit(0);
  }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
