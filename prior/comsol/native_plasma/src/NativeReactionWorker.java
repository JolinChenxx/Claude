import com.comsol.model.*;import com.comsol.model.util.*;import java.io.*;import java.nio.file.*;import java.util.*;
/** Resident native Plasma collision operator. The abstract cells have no transport.
 * Hydro transport and electrostatic transport are separate operators. */
public class NativeReactionWorker{
 static Model m;static int nx=32,ny=1;static String dir;static String[] columns;
 static boolean multicharge=false;static String[] names=Chemistry.NAMES;
 static void v(String n,String e){m.component("c").variable("gasv").set(n,e);}
 static String input(int i){return "rxIn"+i+"(cellSlot)";}
 static void reloadInput(){
  if(Arrays.asList(m.func().tags()).contains("reactionInput"))m.func().remove("reactionInput");
  m.func().create("reactionInput","Interpolation");m.func("reactionInput").set("source","file");m.func("reactionInput").set("filename",dir+"/state.txt");m.func("reactionInput").set("nargs",1);
  String[][] fs=new String[6+names.length][2];for(int i=0;i<fs.length;i++)fs[i]=new String[]{"rxIn"+i,Integer.toString(i+1)};m.func("reactionInput").set("funcs",fs);m.func("reactionInput").set("interp","linear");m.func("reactionInput").set("extrap","const");m.func("reactionInput").importData();
 }
 static void setup(String template,String work,int rows)throws Exception{
  dir=work;ny=rows;m=ModelUtil.load("rxWorker",template);m.param().set("Bcu","0[J/kg]");
  if(multicharge){names=Arrays.copyOf(Chemistry.NAMES,21);for(int q=2;q<=4;q++)names[16+q]="Cu_"+q+"p";LocalCopperMulticharge.apply(m,Path.of(template).getParent().getParent().resolve("data").toString());}
  m.component("c").geom("g").feature("gas").set("size",new String[]{nx*5+"[um]",ny*5+"[um]"});m.component("c").geom("g").run();
  m.component("c").mesh("mesh").feature("map").feature("dist").set("numelem",nx);
  // Axisymmetric rectangle boundary 1/4 are vertical, 2/3 horizontal.
  m.component("c").mesh("mesh").feature("map").feature("dist").selection().set(new int[]{2,3});
  m.component("c").mesh("mesh").feature("map").create("zdist","Distribution");m.component("c").mesh("mesh").feature("map").feature("zdist").selection().set(new int[]{1,4});m.component("c").mesh("mesh").feature("map").feature("zdist").set("numelem",ny);m.component("c").mesh("mesh").run();
  m.func().create("reactionInput","Interpolation");m.func("reactionInput").set("source","file");m.func("reactionInput").set("filename",dir+"/state.txt");m.func("reactionInput").set("nargs",1);
  // rhoGas, T, CvNuclear/alphaGas, ne, en[eV/m3], laser[W/m3], followed by 18 number densities.
  String[][] funcs=new String[6+names.length][2];for(int i=0;i<funcs.length;i++)funcs[i]=new String[]{"rxIn"+i,Integer.toString(i+1)};m.func("reactionInput").set("funcs",funcs);m.func("reactionInput").set("interp","linear");m.func("reactionInput").set("extrap","const");m.func("reactionInput").importData();
  v("cellSlot","min("+(nx*ny-1)+",max(0,floor(r/5[um])+"+nx+"*floor(z/5[um])))");
  v("rhoP",input(0)+"*1[kg/m^3]");v("Tinitial",input(1)+"*1[K]");v("CvNuclear",input(2)+"*1[J/(m^3*K)]");v("Qlaser",input(5)+"*1[W/m^3]");
  String nm="0",cv="0",uc="0";double[] ev={0,0,4.88,2.58,15.58,12.06,19.414,16.198,1.119,-.45,0,1.388948,1.6422256,3.7858977,3.816692,6.1911751,6.1920252,7.72638,28.01877,64.85977,122.23977};
  for(int i=0;i<names.length;i++){
   String n=names[i],ni=input(i+6)+"*1[1/m^3]";v("initialN"+n,ni);nm+="+initialN"+n;
   double cvk=(i==0||i==1||i==4||i==5||i==9)?2.5:1.5;cv+="+"+cvk+"*k_B_const*initialN"+n;uc+="+"+ev[i]+"[V]*e_const*initialN"+n;
   m.component("c").physics("plas").feature(n).set("x0","initialN"+n+"/initialNTotal");
   if(n.matches(".*_[1-4][pm]"))m.component("c").physics("plas").feature(n).set("n0","initialN"+n);
  }
  v("initialNTotal",nm);v("CvInitial",cv);v("initialChemical",uc);
  v("nMol0","initialNTotal/N_A_const");v("initialEnergy","CvNuclear*Tinitial+initialChemical+e_const*"+input(4)+"*1[V/m^3]");
  m.component("c").physics("plas").feature("init1").set("neinit",input(3)+"*1[1/m^3]");m.component("c").physics("plas").feature("init1").set("ebarinit",input(4)+"/"+input(3)+"*1[V]");
  m.component("c").physics("reactionEnergy").feature("gfeq1").set("f",new String[]{"(Qlaser-Qrad)/eScale","(qrE*eScale-(CvNuclear+CvVol-CvInitial)*Tg-Uchem-Ue)/eScale"});m.component("c").physics("reactionEnergy").feature("init1").set("logTg","log(Tinitial/1[K])");
  ArrayList<String> ex=new ArrayList<>(Arrays.asList("Tg","plas.ne","plas.en","Uchem","CvVol-CvInitial","qrE*eScale-initialEnergy","Qrad","jVisible","jline3","jline4","jline5","jline6","tauLine3/w0","tauLine4/w0","tauLine5/w0","tauLine6/w0","alphaLaser","TeK","plas.scharge"));for(String n:names)ex.add("plas.n_w"+n);columns=ex.toArray(new String[0]);
  ExportNative.dataset(m,"sol1");m.result().numerical().create("rxOut","Interp");m.result().numerical("rxOut").set("data","dset1");m.result().numerical("rxOut").set("expr",columns);double[][] xy=new double[2][nx*ny];for(int j=0;j<ny;j++)for(int i=0;i<nx;i++){xy[0][j*nx+i]=(i+.5)*5e-6;xy[1][j*nx+i]=(j+.5)*5e-6;}m.result().numerical("rxOut").set("coord",xy);
  m.label("Native Plasma local collision operator; abstract cells, no plume transport or physical mesh");
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);dir=a[1];File root=new File(dir);root.mkdirs();int rows=Integer.parseInt(a[2]);multicharge=Arrays.asList(a).contains("multicharge");setup(a[0],dir,rows);
  if(a.length>3&&a[3].equals("dependentCu")){
   // In copper-rich cells N2 = 1-sum(w) loses its entire trace population
   // to cancellation. Eliminate abundant ground-state copper instead.
   m.component("c").physics("plas").feature("N2").set("FromMassConstraint",false);
   m.component("c").physics("plas").feature("Cu").set("FromMassConstraint",true);
   m.component("c").physics("plas").feature("N2").featureInfo("info").set("plas.sg_flux_wN2",new String[]{"0[kg/(m^2*s)]"});
   m.sol().remove("sol1");m.study("std").createAutoSequences("sol");
   for(String f:m.sol("sol1").feature("t1").feature().tags())if(Arrays.asList("Segregated","FullyCoupled").contains(m.sol("sol1").feature("t1").feature(f).getType()))m.sol("sol1").feature("t1").feature().remove(f);
   m.sol("sol1").feature("t1").create("direct","Direct");m.sol("sol1").feature("t1").feature("direct").set("linsolver","pardiso");
   m.sol("sol1").feature("t1").create("all","FullyCoupled");m.sol("sol1").feature("t1").feature("all").set("linsolver","direct");m.sol("sol1").feature("t1").feature("all").set("maxiter",20);
   m.sol("sol1").feature("t1").set("maxorder",1);m.sol("sol1").feature("t1").set("rtol",1e-4);m.sol("sol1").feature("t1").set("atolglobal","1e-8");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");
   for(String f:m.sol("sol1").feature("v1").feature().tags()){
    String scale=f.equals("c_Ne")||f.equals("c_En")?"1e12":f.equals("c_plas_N2_w")?"1e-18":f.endsWith("_w")?"1e-6":"1";
    m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);
   }
   ExportNative.dataset(m,"sol1");
   if(!Arrays.asList(m.result().numerical().tags()).contains("rxOut"))m.result().numerical().create("rxOut","Interp");
   m.result().numerical("rxOut").set("data","dset1");m.result().numerical("rxOut").set("expr",columns);
   double[][] xy=new double[2][nx*ny];for(int j=0;j<ny;j++)for(int i=0;i<nx;i++){xy[0][j*nx+i]=(i+.5)*5e-6;xy[1][j*nx+i]=(j+.5)*5e-6;}m.result().numerical("rxOut").set("coord",xy);
   System.out.println("COPPER DEPENDENT: N2 independently conserved; original reactions retained");
  }
  if(multicharge&&!Arrays.asList(a).contains("dependentCu")){
   m.sol().remove("sol1");m.study("std").createAutoSequences("sol");
   for(String f:m.sol("sol1").feature("t1").feature().tags())if(Arrays.asList("Segregated","FullyCoupled").contains(m.sol("sol1").feature("t1").feature(f).getType()))m.sol("sol1").feature("t1").feature().remove(f);
   m.sol("sol1").feature("t1").create("direct","Direct");m.sol("sol1").feature("t1").feature("direct").set("linsolver","pardiso");
   m.sol("sol1").feature("t1").create("all","FullyCoupled");m.sol("sol1").feature("t1").feature("all").set("linsolver","direct");m.sol("sol1").feature("t1").feature("all").set("maxiter",20);
   m.sol("sol1").feature("t1").set("maxorder",1);m.sol("sol1").feature("t1").set("rtol",1e-4);m.sol("sol1").feature("t1").set("atolglobal","1e-8");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");
   for(String f:m.sol("sol1").feature("v1").feature().tags()){
    String scale=f.equals("c_Ne")||f.equals("c_En")?"1e12":f.endsWith("_w")?"1e-6":"1";
    m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);
   }
   ExportNative.dataset(m,"sol1");
   if(!Arrays.asList(m.result().numerical().tags()).contains("rxOut"))m.result().numerical().create("rxOut","Interp");
   m.result().numerical("rxOut").set("data","dset1");m.result().numerical("rxOut").set("expr",columns);
   double[][] xy=new double[2][nx*ny];for(int j=0;j<ny;j++)for(int i=0;i<nx;i++){xy[0][j*nx+i]=(i+.5)*5e-6;xy[1][j*nx+i]=(j+.5)*5e-6;}m.result().numerical("rxOut").set("coord",xy);
  }
  if(Arrays.asList(a).contains("reactionOnly")){
   m.sol("sol1").feature("v1").set("control","user");
   m.sol("sol1").feature("v1").feature("c_V").set("solvefor","off");
   System.out.println("REACTION ONLY: abstract potential excluded; all transport and Joule terms already zero; not a spatial electrostatic solve");
  }
  if(Arrays.asList(a).contains("localEnergyScale")){
   v("eScale","max(abs(initialEnergy),1[J/m^3])");
   System.out.println("LOCAL ENERGY SCALE: one static initial-energy normalization per independent cell; energy equations unchanged");
  }
  if(Arrays.asList(a).contains("traceElectronScale")){
   m.sol("sol1").feature("v1").feature("c_Ne").set("scaleval","1");
   m.sol("sol1").feature("v1").feature("c_En").set("scaleval","1e-12");
   System.out.println("TRACE ELECTRON SCALE: absolute electron/energy tolerance tightened; input populations and physical rates unchanged");
  }
  if(Arrays.asList(a).contains("actualElectronRemoval")){
   String attach=m.component("c").variable("gasv").get("QeAttach");
   v("QeAttach",attach.replace("1.5*e_const*TeV","1.5*e_const*plas.Te"));
   System.out.println("ACTUAL ELECTRON REMOVAL ENERGY: existing losses use actual mean energy, not the rate-table temperature floor");
  }
  boolean directInputStartup=Arrays.asList(a).contains("directInputStartup");
  if(directInputStartup){m.sol("sol1").feature("t1").set("consistent","off");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);}
  String last="";Files.writeString(Path.of(dir,"ready.txt"),"native Plasma operator ready\n");
  while(!Files.exists(Path.of(dir,"stop.txt"))){Path request=Path.of(dir,"request.properties");if(!Files.exists(request)){Thread.sleep(1000);continue;}Properties p=new Properties();try(InputStream f=Files.newInputStream(request)){p.load(f);}String id=p.getProperty("id");if(id==null||id.equals(last)){Thread.sleep(1000);continue;}last=id;long begin=System.currentTimeMillis();
   try{
    reloadInput();String end=p.getProperty("dt_s");m.param().set("tEnd",end+"[s]");String list="0 "+end;
    if(directInputStartup)m.sol("sol1").feature("t1").set("initialstepbdf",p.getProperty("first_step_s",Double.toString(Double.parseDouble(end)/100))+"[s]");
    m.study("std").feature("time").set("tlist",list);m.sol("sol1").feature("t1").set("tlist",list);m.sol("sol1").feature("t1").set("maxstepbdf",Double.toString(Double.parseDouble(end)/4)+"[s]");m.sol("sol1").clearSolutionData();m.sol("sol1").runAll();
    m.result().numerical("rxOut").set("solnum",new int[]{m.sol("sol1").getPVals().length});double[][][] d=m.result().numerical("rxOut").getData();
    try(DataOutputStream f=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dir+"/output_"+id+".bin")))){for(int k=0;k<columns.length;k++)for(int i=0;i<nx*ny;i++)f.writeDouble(d[k][0][i]);}
    if("true".equals(p.getProperty("save")))m.save(dir+"/native_operator_"+id+".mph");
    Files.writeString(Path.of(dir,"complete_"+id+".json"),"{\"id\":\""+id+"\",\"seconds\":"+(System.currentTimeMillis()-begin)/1000.+",\"shape\":["+columns.length+","+nx*ny+"],\"expressions\":[\""+String.join("\",\"",columns)+"\"]}");System.out.println("REACTION DONE "+id+" ms "+(System.currentTimeMillis()-begin));
   }catch(Throwable e){e.printStackTrace();Files.writeString(Path.of(dir,"error_"+id+".txt"),e.toString());}
  }System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
