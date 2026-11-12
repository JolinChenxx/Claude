import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class InitialStorageAudit {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("initialStorage",a[0]);m.sol("sol1").runFromTo("st1","v1");
 m.result().numerical().create("initialStorage","Interp");m.result().numerical("initialStorage").set("data","dset1");m.result().numerical("initialStorage").set("coord",new double[][]{{1e-6},{1e-6}});
 ArrayList<String> ex=new ArrayList<>();for(String n:Chemistry.NAMES){ex.add("store"+n);ex.add("plas.rho_w"+n+"/rho0");ex.add("plas.w0_w"+n);}
 ex.addAll(Arrays.asList("storeElectron","plas.ne/storeElectronScale","storeEnergy","plas.en/storeEnergyScale"));
 m.result().numerical("initialStorage").set("expr",ex.toArray(new String[0]));double[][][] d=m.result().numerical("initialStorage").getData();for(int i=0;i<ex.size();i++)System.out.println(ex.get(i)+"="+d[i][0][0]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
