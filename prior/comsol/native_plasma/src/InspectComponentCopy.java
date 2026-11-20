import com.comsol.model.*;import com.comsol.model.util.*;
public class InspectComponentCopy {
 public static void main(String[] a){ModelUtil.initStandalone(false);Model m=ModelUtil.create("copyAPI");
 for(java.lang.reflect.Method f:m.component().getClass().getMethods())if(f.getName().matches("copy|duplicate|insert|load|rename"))System.out.println(f.toString());
 System.exit(0);}
}
