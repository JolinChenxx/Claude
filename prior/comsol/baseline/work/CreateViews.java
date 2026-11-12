import com.comsol.model.*;import com.comsol.model.util.*;
public class CreateViews {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);String active=java.util.Arrays.asList(m.sol().tags()).contains("merged")?"merged":"sol1";m.result().dataset("dset1").set("solution",active);boolean ale=false;try{ale=m.param().evaluate("aleOn")>.5;}catch(Exception ignored){}
 String[] expr={"Tpl","nePl","rhoP*Ycu","pPl/pAmb","Ts","rhoP","Yraw","speedP"};String[] label={"LTE gas temperature (K)","Electron density (1/m3)","Copper mass density (kg/m3)","Pressure / ambient","Target temperature (K)","Total gas density (kg/m3)","Raw copper mass fraction","Gas speed (m/s)"};
 for(int i=0;i<expr.length;i++){String pg="s4view"+i;if(java.util.Arrays.asList(m.result().tags()).contains(pg))m.result().remove(pg);
  m.result().create(pg,"PlotGroup2D");m.result(pg).label(label[i]);m.result(pg).set("data","dset1");m.result(pg).set("looplevel",new int[]{Math.min(m.sol(active).getPVals().length,71)});
  m.result(pg).create("s","Surface");m.result(pg).feature("s").set("expr",expr[i]);m.result(pg).feature("s").create("domain","Selection");m.result(pg).feature("s").feature("domain").selection().set(i==4?1:2);
  if(ale){m.result(pg).feature("s").create("move","Deform");m.result(pg).feature("s").feature("move").set("expr",new String[]{"0","-sALE"});m.result(pg).feature("s").feature("move").set("scaleactive",true);m.result(pg).feature("s").feature("move").set("scale",1);}
  m.result(pg).run();
 }
 m.label(a.length>2?a[2]:"S4 LTE result with actual solution and physical-coordinate views");m.save(a[1]);ModelUtil.clear();System.out.println("VIEWS_SAVED "+a[1]);System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}}
