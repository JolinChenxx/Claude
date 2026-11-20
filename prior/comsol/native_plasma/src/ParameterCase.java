import com.comsol.model.*;
import com.comsol.model.util.*;
/** Clone a case with explicit, logged parameter changes only. */
public class ParameterCase {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);for(int i=2;i<a.length;i+=2){m.param().set(a[i],a[i+1]);System.out.println("PARAMETER "+a[i]+" = "+a[i+1]);}m.save(a[1]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
