import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
public class InspectElasticOperator{public static void main(String[] a){try{
ModelUtil.initStandalone(false);Model m=ModelUtil.load("elastic",a[0]);
for(String n:new String[]{"elasticAtomCu","N2_0","power"}){InspectFeatures.dump(m.component("c").physics("plas").feature(n),n);for(String[] row:m.component("c").physics("plas").feature(n).featureInfo("info").getInfoTable("Expression"))System.out.println(Arrays.toString(row));}
System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
