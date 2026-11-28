import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InspectTimeStorage {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("t",a[0]);
 for(String p:m.sol("sol1").feature("t1").properties())if(p.toLowerCase().matches(".*(store|out|last|plot).*")){
  String value="";try{value=m.sol("sol1").feature("t1").getString(p);}catch(Exception e){}
  String[] allowed=null;try{allowed=m.sol("sol1").feature("t1").getAllowedPropertyValues(p);}catch(Exception e){}
  System.out.println(p+"="+value+" ALLOWED="+Arrays.toString(allowed));
 }
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
