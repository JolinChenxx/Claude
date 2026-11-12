import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
public class QuickWall {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);
 m.result().numerical().create("qw","MaxLine");m.result().numerical("qw").selection().set(4);
 m.result().numerical("qw").set("expr",new String[]{"depth/1[um]","Ts","if(mdotEvap>1e-6[kg/(m^2*s)],returnFraction,0)"});
 double[][] d=m.result().numerical("qw").getReal();for(int i=0;i<d.length;i++){double x=-1e300;for(double y:d[i])x=Math.max(x,y);System.out.println("WALL "+i+" "+x);}
 ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}}
