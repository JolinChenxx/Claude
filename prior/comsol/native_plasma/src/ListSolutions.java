import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class ListSolutions {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 for(String s:m.sol().tags())try{double[] t=m.sol(s).getPVals();System.out.println("SOLUTION "+s+" n="+t.length+" first="+(t.length>0?t[0]:0)+" last="+(t.length>0?t[t.length-1]:0));}catch(Exception e){System.out.println(s+" "+e.getMessage());}
 for(String d:m.result().dataset().tags())try{System.out.println("DATASET "+d+" "+m.result().dataset(d).getType()+" "+m.result().dataset(d).getString("solution"));}catch(Exception e){}
 for(String f:m.sol("sol1").feature("t1").feature().tags())if(m.sol("sol1").feature("t1").feature(f).getType().equals("AutoRemesh"))InspectFeatures.dump(m.sol("sol1").feature("t1").feature(f),f);
 System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}}
