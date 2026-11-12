import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InitialAudit{
public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);m.sol("sol1").runFromTo("st1","v1");
m.result().numerical().create("ip","Interp");m.result().numerical("ip").set("data","dset1");m.result().numerical("ip").set("coord",new double[][]{{1e-5},{1e-5}});
for(String e:new String[]{"qR","qE","Tg","plas.Te","plas.ne","plas.scharge","Uchem","CvVol*Tg","Ue","pPl","plas.Re","plas.Sen","Qlaser","Qrad","plas.r_1","plas.wO2","plas.n_wO2","plas.n_wN2_1p"}){try{m.result().numerical("ip").set("expr",e);System.out.println(e+" "+Arrays.deepToString(m.result().numerical("ip").getData()));}catch(Exception x){System.out.println("FAIL "+e+" "+x.getMessage());}}
System.exit(0);}catch(Exception e){e.printStackTrace();System.exit(1);}}
}
