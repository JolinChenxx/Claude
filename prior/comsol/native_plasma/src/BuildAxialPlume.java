import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Quasi-1D conservative axial Euler transport, separate native Plasma reactor
 * in every cell. Area(z)=pi*(w0^2+z^2) is an explicitly assumed expanding stream
 * tube, not fitted to S4. The target history is prescribed, not two-way. */
public class BuildAxialPlume {
 static Model m;static int N;static ArrayList<String> species=new ArrayList<>();
 static String c(int j){return "cell"+(char)('A'+j);}static String suf(int j){return ""+(char)('A'+j);}static String p(int j){return "plasma"+suf(j);}
 static String nn(String n,int j){return n.replaceFirst("(_[1-4][pm])$",suf(j)+"$1")+(n.contains("_")?"":suf(j));}
 static String x(int j,String n){return c(j)+"."+n;}static void v(int j,String n,String s){m.component(c(j)).variable("gasv"+suf(j)).set(n,s);}
 static void g(String n,String s){m.variable("axialFlux").set(n,s);}
 static String nval(double a){return String.format(java.util.Locale.US,"%.14g",a);}
 static String area(double z){return "pi*(w0^2+("+nval(z)+"[m])^2)";}
 static String ghost(String key){
  if(key.equals("rhoP"))return "rhoAmb";if(key.equals("pPl"))return "pAmb";if(key.equals("u"))return "(0[m/s])";if(key.equals("sound"))return "sqrt(1.4*pAmb/rhoAmb)";if(key.equals("Etot"))return "(2.5*pAmb)";if(key.equals("Ue"))return "(0[J/m^3])";
  if(key.equals("wO2"))return "(0.21*0.0319988[kg/mol]/Mair)";if(key.equals("wN2"))return "(0.79*0.0280134[kg/mol]/Mair)";return "0";
 }
 static String state(int j,String n){return j==N?ghost(n):x(j,n.startsWith("w")?p(j)+"."+n:n);}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("axialPlume",a[0]);N=a.length>3?Integer.parseInt(a[3]):24;
  for(String s:m.sol().tags())m.sol().remove(s);m.component().remove("c");m.component().remove("outer");
  String[][] ff=m.func("thermalFeed").getStringMatrix("funcs");m.func().remove("thermalFeed");m.func().create("thermalFeed","Interpolation");m.func("thermalFeed").set("source","file");m.func("thermalFeed").set("filename",a[2]);m.func("thermalFeed").set("nargs",1);m.func("thermalFeed").set("funcs",ff);m.func("thermalFeed").set("interp","linear");m.func("thermalFeed").set("extrap","const");m.func("thermalFeed").importData();
  species.addAll(Arrays.asList(Chemistry.NAMES));species.add("CuT5");species.add("Cu_2p");species.add("Cu_3p");species.add("Cu_4p");
  Map<String,String> chemical=AxialChemicalRates.rates();
  double[] z=new double[N+1];for(int j=0;j<=N;j++)z[j]=650e-6*Math.pow((double)j/N,1.35);
  m.param().set("tp","148[ns]");m.param().set("tEnd","1000[ns]");m.variable().create("axialFlux");
  g("axialIncident","(1-Acu)*Ein/(tau*sqrt(pi/(4*log(2))))*exp(-4*log(2)*((t-tp)/tau)^2)");
  g("targetMassSource","thermalMass(t/1[s])*1[kg/s]");g("targetPowerSource","thermalPower(t/1[s])*1[W]");g("targetElectronSource","thermalElectron(t/1[s])*1[W]");g("targetNormalSpeed","sqrt(pi*k_B_const*thermalTargetK(t/1[s])*1[K]/(2*Mcu/N_A_const))");
  for(int j=0;j<N;j++){
   AddAxialComponent.apply(m,c(j),suf(j));m.component(c(j)).physics().remove("bulk"+suf(j));
   String plasma=p(j),cell=c(j);double lo=z[j],hi=z[j+1],dz=hi-lo,mid=(hi+lo)/2;
   v(j,"zCenter",nval(mid)+"[m]");v(j,"zLower",nval(lo)+"[m]");v(j,"zUpper",nval(hi)+"[m]");v(j,"dz",nval(dz)+"[m]");
   v(j,"areaLeft",area(lo));v(j,"areaRight",area(hi));v(j,"Rp","sqrt(w0^2+zCenter^2)");v(j,"Hp","dz");
   v(j,"Vp","pi*(w0^2*dz+(zUpper^3-zLower^3)/3)");v(j,"Req","Rp");
   v(j,"logGasT","logT"+suf(j));v(j,"gasEnergy","energyGas"+suf(j));v(j,"u","moment"+suf(j)+"*momentumScale/Mgas");
   v(j,"volumeRate","0[1/s]");v(j,"Vdot","0[m^3/s]");v(j,"Msweep","0[kg/s]");v(j,"vRadius","u*zCenter/Rp");v(j,"vHeight","u");v(j,"Kinetic","0.5*Mgas*u^2");v(j,"Etot","gasEnergy*energyScale/Vp");
   v(j,"sound","sqrt((1+pPl/(CvVol*Tg+Ue))*pPl/rhoP)");
   v(j,"Msource","massF"+j+"-massF"+(j+1)+(j==0?"+targetMassSource":""));
   v(j,"Psource",j==0?"targetPowerSource":"0[W]");v(j,"PeSource","electronF"+j+"-electronF"+(j+1)+"-pe*(areaRight*velocityF"+(j+1)+"-areaLeft*velocityF"+j+")"+(j==0?"+targetElectronSource":""));
   v(j,"laserTau","alphaLaser*dz");v(j,"beamTransmission","exp(-laserTau)");v(j,"beamAbsorptance","if(laserTau<1e-4,laserTau*(1-laserTau/2+laserTau^2/6-laserTau^3/24),1-exp(-laserTau))");
   String forward="0",back="0";for(int k=j+1;k<N;k++)forward+="+"+x(k,"laserTau");for(int k=0;k<N;k++)back+="+"+x(k,"laserTau");for(int k=0;k<j;k++)back+="+"+x(k,"laserTau");
   v(j,"incomingForward","axialIncident*exp(-("+forward+"))");v(j,"incomingBack","axialIncident*exp(-("+back+"))");
   v(j,"Plaser","(incomingForward+incomingBack)*beamAbsorptance");v(j,"meanLaserIntensity","(incomingForward+incomingBack)*dz/Vp*if(laserTau<1e-4,1-laserTau/2+laserTau^2/6-laserTau^3/24,beamAbsorptance/laserTau)");
   for(String n:species){if(n.equals("N2"))continue;
    String rate;
    if(n.matches("Cu_[234]p")){int q=n.charAt(3)-'0';rate="(multNet"+(q-1)+(q<4?"-multNet"+q:"")+")/N_A_const";}
    else{rate=chemical.get(n);if(rate==null)throw new RuntimeException("No chemical rate "+n);rate=rate.replace("plas.",plasma+".");if(n.equals("Cu_1p"))rate="("+rate+")-multNet1/N_A_const";}
    String input="speciesF"+j+"_"+n+"-speciesF"+(j+1)+"_"+n;
    if(j==0&&n.startsWith("Cu")&&!n.matches("Cu_[234]p"))input+="+thermalFlux"+n+"(t/1[s])*1[kg/s]";
    m.component(cell).physics(plasma).feature(nn(n,j)).featureInfo("info").set(plasma+".R_w"+nn(n,j),new String[]{"("+rate+")+("+input+")/(Vp*"+plasma+".M_w"+n+")"});
   }
   m.component(cell).physics(plasma).feature("pes1").set("Pgm","Plaser+PeSource-Vp*(QeRR+QeAttach+Qff+Qei)-Vp*tailPhotoEvents*tailI*e_const");
   String bulk="axialBulk"+suf(j);m.component(cell).physics().create(bulk,"GlobalEquations","g"+suf(j));
   m.component(cell).physics(bulk).feature("ge1").set("name",new String[]{"energyGas"+suf(j),"logT"+suf(j),"moment"+suf(j)});
   m.component(cell).physics(bulk).feature("ge1").set("equation",new String[]{"energyGas"+suf(j)+"t-(energyF"+j+"-energyF"+(j+1)+"+Psource+Plaser-Qrad*Vp)/energyScale","(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue)-Kinetic)/energyScale","moment"+suf(j)+"t-(momentumF"+j+"-momentumF"+(j+1)+"+pPl*(areaRight-areaLeft)"+(j==0?"+targetMassSource*targetNormalSpeed":"")+")/momentumScale"});
   m.component(cell).physics(bulk).feature("ge1").set("initialValueU",new String[]{"2.5*pAmb*Vp/energyScale","log(Tamb/1[K])","0"});
   String mesh="axialDummyMesh"+suf(j);m.component(cell).mesh().create(mesh);m.component(cell).mesh(mesh).autoMeshSize(9);m.component(cell).mesh(mesh).run();
   System.out.println("NATIVE AXIAL CELL READY "+j+" "+cell);System.out.flush();
  }
  // Rusanov fluxes. Every internal face is referenced with exactly opposite
  // signs by adjacent cells; stationary conical-wall pressure enters momentum.
  g("massF0","0[kg/s]");g("energyF0","0[W]");g("electronF0","0[W]");g("velocityF0","0[m/s]");g("momentumF0",x(0,"areaLeft")+"*"+x(0,"pPl"));
  for(String n:species)g("speciesF0_"+n,"0[kg/s]");
  for(int f=1;f<=N;f++){
   int l=f-1,r=f;String A=area(z[f]),rhoL=state(l,"rhoP"),rhoR=state(r,"rhoP"),uL=state(l,"u"),uR=state(r,"u"),pL=state(l,"pPl"),pR=state(r,"pPl");
   g("waveF"+f,"max(abs("+uL+")+"+state(l,"sound")+",abs("+uR+")+"+state(r,"sound")+")");String w="waveF"+f;
   g("velocityF"+f,"0.5*("+uL+"+"+uR+")");
   g("massF"+f,A+"*0.5*("+rhoL+"*"+uL+"+"+rhoR+"*"+uR+"-"+w+"*("+rhoR+"-"+rhoL+"))");
   g("momentumF"+f,A+"*0.5*("+rhoL+"*"+uL+"^2+"+pL+"+"+rhoR+"*"+uR+"^2+"+pR+"-"+w+"*("+rhoR+"*"+uR+"-"+rhoL+"*"+uL+"))");
   String el=state(l,"Etot"),er=state(r,"Etot"),eeL=state(l,"Ue"),eeR=state(r,"Ue");
   g("energyF"+f,A+"*0.5*(("+el+"+"+pL+")*"+uL+"+("+er+"+"+pR+")*"+uR+"-"+w+"*("+er+"-"+el+"))");
   g("electronF"+f,A+"*0.5*("+eeL+"*"+uL+"+"+eeR+"*"+uR+"-"+w+"*("+eeR+"-"+eeL+"))");
   for(String n:species){String dl=rhoL+"*"+state(l,"w"+n),dr=rhoR+"*"+state(r,"w"+n);g("speciesF"+f+"_"+n,A+"*0.5*("+dl+"*"+uL+"+"+dr+"*"+uR+"-"+w+"*("+dr+"-"+dl+"))");}
  }
  String total="0[J]",cu="0[kg]",rad="0[W]",laser="0[W]",outCu="0[kg/s]";
  for(int j=0;j<N;j++){total+="+"+x(j,"gasEnergy")+"*energyScale";cu+="+"+x(j,"rhoCopper")+"*"+x(j,"Vp");rad+="+"+x(j,"Qrad")+"*"+x(j,"Vp");laser+="+"+x(j,"Plaser");}
  for(String n:species)if(n.startsWith("Cu"))outCu+="+speciesF"+N+"_"+n;
  g("axialTotalEnergy",total);g("axialCopper",cu);g("axialRadiation",rad);g("axialLaser",laser);g("axialOutCu",outCu);g("axialCellCount",Integer.toString(N));
  m.component(c(0)).physics().create("ledger","GlobalEquations","gA");
  m.component(c(0)).physics("ledger").feature("ge1").set("name",new String[]{"axialInputEnergy","axialRadiatedEnergy","axialOutEnergy","axialInputCu","axialOutputCu"});
  m.component(c(0)).physics("ledger").feature("ge1").set("equation",new String[]{"axialInputEnergyt-(targetPowerSource+axialLaser)/energyScale","axialRadiatedEnergyt-axialRadiation/energyScale","axialOutEnergyt-energyF"+N+"/energyScale","axialInputCut-targetMassSource/mScale","axialOutputCut-axialOutCu/mScale"});
  m.component(c(0)).physics("ledger").feature("ge1").set("initialValueU",new String[]{"0","0","0","0","0"});
  m.study("std").feature("time").set("tlist","range(0,2[ns],tEnd)");m.study("std").feature("time").set("rtol",1e-4);m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.matches(".*_energy[A-Z]$")?"1e7":f.endsWith("_pA")?"1e5":f.contains("ODE")?"1":f.endsWith("_w")?(f.matches(".*_O2[A-Z]_w")?"1":"1e-8"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Native Plasma in "+N+" conservative axial FV cells; prescribed H04 source, assumed expanding streamtube; no resolved transverse dynamics or moving target");m.save(a[1]);System.out.println("AXIAL PLASMA READY "+N+" cells");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
