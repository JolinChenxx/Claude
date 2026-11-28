import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Remove the obsolete target component pressure; new ring pressure is the boundary definition. */
public class RepairGridDuplicate {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("gridDuplicate",a[0]);
  HashMap<String,String> owners=new HashMap<>();
  for(String tag:m.component("c").variable().tags())for(String n:m.component("c").variable(tag).varnames()){
   String old=owners.put(n,tag);if(old!=null)System.out.println("DUPLICATE "+n+" "+old+" "+tag);
  }
  m.component("c").variable("gasv").remove("pPl");
  m.save(a[1]);System.out.println("GRID DUPLICATE PRESSURE REPAIRED");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
