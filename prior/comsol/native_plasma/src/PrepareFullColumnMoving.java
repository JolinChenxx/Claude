import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Extend shared radial columns through gas and copper. M04's sheath quads
 * stayed positive; mismatched triangles above/below the strip inverted. */
public class PrepareFullColumnMoving {
 static Model m;
 static void box(String tag,int dim,String x0,String x1,String z0,String z1){
  m.component("c").selection().create(tag,"Box");m.component("c").selection(tag).set("entitydim",dim);
  m.component("c").selection(tag).set("xmin",x0);m.component("c").selection(tag).set("xmax",x1);m.component("c").selection(tag).set("ymin",z0);m.component("c").selection(tag).set("ymax",z1);m.component("c").selection(tag).set("condition","inside");
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("fullColumn",a[0]);for(String s:m.sol().tags())m.sol().remove(s);
  m.component("c").geom("g").create("fullColumn","Rectangle");m.component("c").geom("g").feature("fullColumn").set("pos",new String[]{"0","-Dcu"});m.component("c").geom("g").feature("fullColumn").set("size",new String[]{"60[um]","Zdom+Dcu"});m.component("c").geom("g").feature("fullColumn").set("selresult",true);m.component("c").geom("g").run();
  m.save(a[1].replace("_ready.mph","_geometry.mph"));
  for(String f:new String[]{"tri","wallMap","normalLeft","normalRight"})if(Arrays.asList(m.component("c").mesh("mesh").feature().tags()).contains(f))m.component("c").mesh("mesh").feature().remove(f);
  box("columnEdges",1,"-1e-10","60[um]+1e-10[m]","-Dcu-1e-10[m]","Zdom+1e-10[m]");
  box("columnLeft",1,"-1e-10","1e-10","-Dcu-1e-10[m]","Zdom+1e-10[m]");box("columnRight",1,"60[um]-1e-10[m]","60[um]+1e-10[m]","-Dcu-1e-10[m]","Zdom+1e-10[m]");
  m.component("c").selection().create("columnHorizontal","Difference");m.component("c").selection("columnHorizontal").set("entitydim",1);m.component("c").selection("columnHorizontal").set("add",new String[]{"columnEdges"});m.component("c").selection("columnHorizontal").set("subtract",new String[]{"columnLeft","columnRight"});
  String[][] bands={{"-Dcu","-15[um]","35","8","top"},{"-15[um]","0[um]","40","10","top"},{"0[um]","2[um]","25","210","bottom"},{"2[um]","80[um]","55","20","bottom"},{"80[um]","400[um]","40","3","bottom"},{"400[um]","Zdom","45","1","bottom"}};
  if(a.length>2&&a[2].equals("compact")){
   // Keep the shared radial nodes, sheath and heated copper resolution.
   // Coarsen remote columns that multiplied the full moving solve cost.
   bands[0][2]="10";bands[3][2]="30";bands[4][2]="12";bands[5][2]="15";
  }
  for(int k=0;k<bands.length;k++)for(int side=0;side<2;side++){
   String tag="columnEdge"+k+"_"+side;String[] b=bands[k];
   box(tag,1,side==0?"-1e-10":"60[um]-1e-10[m]",side==0?"1e-10":"60[um]+1e-10[m]",b[0]+"-1e-10[m]",b[1]+"+1e-10[m]");
   m.component("c").mesh("mesh").create(tag,"Edge");m.component("c").mesh("mesh").feature(tag).selection().named(tag);m.component("c").mesh("mesh").feature(tag).create("dist","Distribution");
   m.component("c").mesh("mesh").feature(tag).feature("dist").set("type","predefined");m.component("c").mesh("mesh").feature(tag).feature("dist").set("elemcount",Integer.parseInt(b[2]));m.component("c").mesh("mesh").feature(tag).feature("dist").set("elemratio",Double.parseDouble(b[3]));m.component("c").mesh("mesh").feature(tag).feature("dist").set("growthrate","exponential");m.component("c").mesh("mesh").feature(tag).feature("dist").set("reverse",(side==1)^b[4].equals("top"));
   System.out.println(tag+" "+Arrays.toString(m.component("c").selection(tag).entities()));
  }
  m.component("c").mesh("mesh").create("fullMap","Map");m.component("c").mesh("mesh").feature("fullMap").selection().named("g_fullColumn_dom");m.component("c").mesh("mesh").feature("fullMap").create("radial","Distribution");m.component("c").mesh("mesh").feature("fullMap").feature("radial").selection().named("columnHorizontal");m.component("c").mesh("mesh").feature("fullMap").feature("radial").set("type","number");m.component("c").mesh("mesh").feature("fullMap").feature("radial").set("numelem",120);
  m.component("c").mesh("mesh").create("tri","FreeTri");m.component("c").mesh("mesh").run();
  double[][] nodes=m.component("c").mesh("mesh").getVertex();int[][] triangles=m.component("c").mesh("mesh").getElem("tri");int inColumn=0;
  for(int i=0;i<triangles[0].length;i++){double r=0;for(int j=0;j<3;j++)r+=nodes[0][triangles[j][i]]/3;if(r<60e-6-1e-10)inColumn++;}
  if(inColumn!=0)throw new RuntimeException("Unmapped triangles remain in deforming columns: "+inColumn);
  System.out.println("COLUMN CHECK: "+nodes[0].length+" vertices, no triangles within60um");
  m.param().set("tEnd","123[ns]");PositiveGasTemperature.solver(m);
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<80[ns],1[ns],0.2[ns])");
  for(String f:new String[]{"c_umelt","c_etaSurface"}){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",f.equals("c_umelt")?"100":"1");}
  m.label("Fresh ground-state moving model with shared columns across entire copper and gas height");m.save(a[1]);System.out.println("FULL COLUMN MODEL READY; fresh initial solve");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
