import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
public class Inspect64 {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);
  System.out.println("MODEL "+a[0]);
  for(String k:m.param().varnames())System.out.println("PARAM "+k+" = "+m.param().get(k));
  for(String k:m.component("c").variable("v").varnames())System.out.println("VAR "+k+" = "+m.component("c").variable("v").get(k));
  for(String phy:m.component("c").physics().tags()){
   try{System.out.println("SELECTION "+phy+" = "+Arrays.toString(m.component("c").physics(phy).selection().entities()));}catch(Exception e){}
   for(String ft:m.component("c").physics(phy).feature().tags()){
    try{System.out.println("SELECTION "+phy+"/"+ft+" = "+Arrays.toString(m.component("c").physics(phy).feature(ft).selection().entities()));}catch(Exception e){}
    for(String k:m.component("c").physics(phy).feature(ft).properties()){
     try{System.out.println("PHYSICS "+phy+"/"+ft+"/"+k+" = "+m.component("c").physics(phy).feature(ft).getString(k));}catch(Exception e){}
    }
   }
  }
  System.out.println("LTE_TYPE "+m.func("lte").getType());
  for(String k:m.func("lte").properties()){
   try{System.out.println("LTE "+k+" = "+m.func("lte").getString(k));}
   catch(Exception e){try{System.out.println("LTE "+k+" = "+Arrays.deepToString(m.func("lte").getStringMatrix(k)));}catch(Exception ee){System.out.println("LTE "+k+" = <nontext>");}}
  }
  System.out.println("MESH_ELEMENTS "+m.component("c").mesh("mesh").stat().getNumElem());
  for(String s:m.sol().tags()){try{double[] t=m.sol(s).getPVals();System.out.println("SOL "+s+" "+t.length+" "+(t.length>0?t[0]:0)+" "+(t.length>0?t[t.length-1]:0));}catch(Exception e){System.out.println("SOL "+s+" EMPTY");}}
  for(String k:new String[]{"initmethod","initsol","solnum"})try{System.out.println("INIT "+k+" = "+m.sol("sol1").feature("v1").getString(k));}catch(Exception e){}
  ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
