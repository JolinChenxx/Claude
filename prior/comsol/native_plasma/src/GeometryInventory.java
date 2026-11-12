import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
public class GeometryInventory {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("i",a[0]);
 Object g=m.component("c").geom("g");for(java.lang.reflect.Method mm:g.getClass().getMethods())if(mm.getName().matches(".*(Adj|Vertex|Edge|Coord).*"))System.out.println(mm);
 System.out.println("VERTICES "+Arrays.deepToString(m.component("c").geom("g").getVertexCoord()));
 for(String s:new String[]{"g_wallstrip_dom","mapTop","mapLeft","mapRight","beam","surface","g_gas_dom"})System.out.println(s+" "+Arrays.toString(m.component("c").selection(s).entities()));
 System.exit(0);
}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
