import com.comsol.model.*;
public class CouplingFixes {
 public static void apply(Model m){
  RecombinationEnergy.apply(m);
  for(String op:new String[]{"intSurf","intGas","intCu"})m.component("c").cpl(op).set("axisym",true);
  // HeatTransfer solid storage follows its material/mesh frame. Advect
  // liquid sensible/latent heat with velocity relative to that frame.
  m.component("c").physics("ht").feature("advection").set("Q0","-rhoCu*cpCu*((urCu-d(r,TIME))*d(T,r)+(uzCu-d(z,TIME))*d(T,z))");
  m.component("c").physics("plas").feature("O2_1m").set("n0","1e7[1/m^3]");
  m.component("c").physics("plas").feature("O_1m").set("n0","1e7[1/m^3]");
  // Four other positive ions and two negative ions at the same tiny seed.
  for(String ion:new String[]{"O2_1p","N_1p","O_1p","Cu_1p"})m.component("c").physics("plas").feature(ion).set("n0","1e7[1/m^3]");
  m.component("c").physics("plas").feature("N2_1p").set("n0","seedNe-2e7[1/m^3]");
  m.component("c").variable("gasv").set("axisWeight","r/Rdom");
  // Generic PDE nabla is Cartesian even in an axisymmetric component.
  // See COMSOL Reference, Equation-Based Modeling, Notational Conventions.
  for(String ph:new String[]{"flow","temp","ld","lu"})if(java.util.Arrays.asList(m.component("c").physics().tags()).contains(ph)){
   m.component("c").physics(ph).prop("ShapeProperty").set("order",1);
   m.component("c").physics(ph).prop("ShapeProperty").set("frame","spatial");
   String[][] ga=m.component("c").physics(ph).feature("gfeq1").getStringMatrix("Ga");
   for(int i=0;i<ga.length;i++)for(int j=0;j<ga[i].length;j++)if(!ga[i][j].startsWith("axisWeight*("))ga[i][j]="axisWeight*("+ga[i][j]+")";
   m.component("c").physics(ph).feature("gfeq1").set("Ga",ga);
   String[] f=m.component("c").physics(ph).feature("gfeq1").getStringArray("f");
   for(int i=0;i<f.length;i++)if(!f[i].startsWith("axisWeight*("))f[i]="axisWeight*("+f[i]+")";
   m.component("c").physics(ph).feature("gfeq1").set("f",f);
  }
  m.component("c").physics("flow").feature("gfeq1").set("da","axisWeight");
  if(java.util.Arrays.asList(m.component("c").physics("flow").feature().tags()).contains("feed")){
   String[] fg=m.component("c").physics("flow").feature("feed").getStringArray("g");for(int i=0;i<fg.length;i++)if(!fg[i].startsWith("axisWeight*("))fg[i]="axisWeight*("+fg[i]+")";m.component("c").physics("flow").feature("feed").set("g",fg);
  }
  // Native Plasma metal contact, I_external=0, includes both displacement
  // and particle conduction currents. qmetal remains an independent audit.
  if(java.util.Arrays.asList(m.component("c").physics("plas").feature().tags()).contains("float"))m.component("c").physics("plas").feature().remove("float");
  if(!java.util.Arrays.asList(m.component("c").physics("plas").feature().tags()).contains("metal"))m.component("c").physics("plas").create("metal","MetalContact",1);
  m.component("c").physics("plas").feature("metal").selection().named("surface");
  m.component("c").physics("plas").feature("metal").set("TerminalType","Current");
  m.component("c").physics("plas").feature("metal").set("I0","0[A]");
  // Species feed is a flux condition, not a SurfaceReaction, so add its
  // electrical current explicitly to the native metal-contact balance.
  m.component("c").physics("plas").feature("metal").featureInfo("info").set("plas.metal.nJt",new String[]{"Iwall-plas.wall.nJe"});
  // Internal Cu/gas partitions can put gas on either geometric side.
  // These boundary-only variables use the known normal from gas into copper.
  m.component("c").variable("boundv").set("mueWallDotE","-e_const/(me_const*nuCollision)*(plas.Etr*ncr+plas.Etz*ncz)");
  m.component("c").physics("plas").feature("wall").featureInfo("info").set("plas.nemig_bnd",new String[]{"plas.ne*min(mueWallDotE,0[m/s])"});
  m.component("c").physics("plas").feature("wall").featureInfo("info").set("plas.enmig_bnd",new String[]{"(5/3)*plas.en*min(mueWallDotE,0[m/s])"});
  // In Fick mode COMSOL derives mobility from Dfs. Selecting the inverse
  // relation at the same time creates a symbolic Dfs <-> mobility cycle.
  for(String ion:new String[]{"N2_1p","O2_1p","N_1p","O_1p","O_1m","O2_1m","Cu_1p"}){
   m.component("c").physics("plas").feature(ion).set("MobilityDiffusivitySpecification","ComputeMobilityAndDiffusivity");
   m.component("c").physics("plas").feature(ion).set("Dfs","Dgas");
  }
  // The native drift-diffusion wall specifies only drift/diffusion flux.
  // Complete the laboratory-frame total flux on the moving material boundary.
  if(!java.util.Arrays.asList(m.component("c").physics("plas").feature().tags()).contains("wallConv"))m.component("c").physics("plas").create("wallConv","WeakContribution",1);
  m.component("c").physics("plas").feature("wallConv").selection().named("surface");
  m.component("c").physics("plas").feature("wallConv").set("weakExpression","(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
  // General-form PDE's default zero TOTAL flux would suppress the laser
  // precisely at its exit. Let advection leave, with zero diffusive flux.
  for(String ph:new String[]{"ld","lu"})if(java.util.Arrays.asList(m.component("c").physics().tags()).contains(ph)){
   if(!java.util.Arrays.asList(m.component("c").physics(ph).feature().tags()).contains("exit"))m.component("c").physics(ph).create("exit","FluxBoundary",1);
   m.component("c").physics(ph).feature("exit").selection().named(ph.equals("ld")?"surface":"top");
   m.component("c").physics(ph).feature("exit").set("g",ph.equals("ld")?"-axisWeight*optD*ncz":"-axisWeight*optU");
   m.component("c").physics(ph).feature("init1").set(ph.equals("ld")?"optD":"optU",ph.equals("ld")?"pulse":"(1-Acu)*pulse");
  }
 }
}
