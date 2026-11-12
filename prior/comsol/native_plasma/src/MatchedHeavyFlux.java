import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

/** Match native heavy-species boundary integration to the order-8 current ledger.
 * The physical flux is unchanged. This matters when emitted and returning ion
 * currents are large compared with their net current at a floating conductor.
 */
public class MatchedHeavyFlux {
 public static void apply(Model m){
  StringBuilder weak=new StringBuilder();
  for(String[] row:m.component("c").physics("plas").feature("feed").featureInfo("info").getInfoTable("Weak")){
   if(row.length<4||row[1]==null)continue;
   if(!row[3].equals("2"))throw new IllegalStateException("Unexpected native feed quadrature "+Arrays.toString(row));
   String expression=row[1];
   if(!expression.startsWith("2*")||!expression.endsWith("*pi*r"))throw new IllegalStateException("Unexpected axisymmetric weak form "+expression);
   if(weak.length()>0)weak.append("+");weak.append("(").append(expression.substring(2,expression.length()-5)).append(")");
  }
  if(weak.length()==0)throw new IllegalStateException("No native feed weak terms found");
  for(int order:new int[]{2,8}){
   String tag="heavyFeedQuadrature"+order;
   m.component("c").physics("plas").create(tag,"WeakContribution",1);
   m.component("c").physics("plas").feature(tag).selection().named("surface");
   m.component("c").physics("plas").feature(tag).set("automaticQuadrature",false);
   m.component("c").physics("plas").feature(tag).set("integrationOrder",order);
   m.component("c").physics("plas").feature(tag).set("weakExpression",(order==2?"-(":"(")+weak+")");
  }
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("matched",a[0]);apply(m);m.save(a[1]);
  System.out.println("HEAVY FEED QUADRATURE MATCHED TO ORDER 8; physical flux unchanged");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
