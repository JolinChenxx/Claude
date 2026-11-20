import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Coarse axisymmetric Euler FV grid, one existing native Plasma per cell.
 * No prescribed plume radius. Four radial by six axial cells; diagnostic only. */
public class BuildSpatialGridThermal {
 static Model m;static int nr=8,nz=10;static ArrayList<String> sp=new ArrayList<>();
 static double[] re={0,4e-6,8e-6,11e-6,25e-6,60e-6,150e-6,400e-6,1200e-6},ze={0,1e-6,4e-6,12e-6,35e-6,90e-6,190e-6,350e-6,650e-6,1100e-6,1800e-6};
 static String suffix(int j){String s="";for(int k=j+1;k>0;k=(k-1)/26)s=(char)(65+(k-1)%26)+s;return s;} static String c(int j){return "cell"+suffix(j);}static String p(int j){return "plasma"+suffix(j);}
 static String num(double d){return String.format(Locale.US,"%.14g",d);}
 static void g(String n,String v){m.variable("axialFlux").set(n,v);}static void v(int j,String n,String s){m.component(c(j)).variable("gasv"+suffix(j)).set(n,s);}
 static String s(int j,String n){if(j>=0)return c(j)+"."+(n.startsWith("w")?p(j)+".":"")+n;
  if(n.equals("rhoP"))return "rhoAmb";if(n.equals("pPl"))return "pAmb";if(n.equals("uR")||n.equals("uZ"))return "(0[m/s])";if(n.equals("sound"))return "sqrt(1.4*pAmb/rhoAmb)";if(n.equals("Etot"))return "(2.5*pAmb)";if(n.equals("Ue"))return "(0[J/m^3])";
  if(n.equals("wO2"))return "(0.21*0.0319988[kg/mol]/Mair)";if(n.equals("wN2"))return "(0.79*0.0280134[kg/mol]/Mair)";return "0";}
 static String rf(int i,int k){return "gridR"+i+"Z"+k;}static String zf(int i,int k){return "gridZ"+k+"R"+i;}
 static void wall(String f,double ar,int j,boolean radial){
  String A=num(ar)+"[m^2]";g(f+"mass","0[kg/s]");g(f+"energy","0[W]");g(f+"electron","0[W]");g(f+"velocity","0[m/s]");
  g(f+"momentR",radial?A+"*"+s(j,"pPl"):"0[N]");g(f+"momentZ",radial?"0[N]":A+"*"+s(j,"pPl"));for(String n:sp)g(f+"species"+n,"0[kg/s]");
 }
 static void face(String f,double ar,int l,int r,boolean radial){
  String A=num(ar)+"[m^2]",rl=s(l,"rhoP"),rr=s(r,"rhoP"),pl=s(l,"pPl"),pr=s(r,"pPl"),ul=s(l,radial?"uR":"uZ"),ur=s(r,radial?"uR":"uZ"),a=f+"wave";
  g(a,"max(abs("+ul+")+"+s(l,"sound")+",abs("+ur+")+"+s(r,"sound")+")");g(f+"velocity","0.5*("+ul+"+"+ur+")");
  g(f+"mass",A+"*0.5*("+rl+"*"+ul+"+"+rr+"*"+ur+"-"+a+"*("+rr+"-"+rl+"))");
  for(String comp:new String[]{"R","Z"}){String vl=s(l,"u"+comp),vr=s(r,"u"+comp);boolean normal=radial==comp.equals("R");
   g(f+"moment"+comp,A+"*0.5*("+rl+"*"+ul+"*"+vl+"+"+rr+"*"+ur+"*"+vr+(normal?"+"+pl+"+"+pr:"")+"-"+a+"*("+rr+"*"+vr+"-"+rl+"*"+vl+"))");}
  g(f+"energy",A+"*0.5*(("+s(l,"Etot")+"+"+pl+")*"+ul+"+("+s(r,"Etot")+"+"+pr+")*"+ur+"-"+a+"*("+s(r,"Etot")+"-"+s(l,"Etot")+"))");
  g(f+"electron",A+"*0.5*("+s(l,"Ue")+"*"+ul+"+"+s(r,"Ue")+"*"+ur+"-"+a+"*("+s(r,"Ue")+"-"+s(l,"Ue")+"))");
  for(String n:sp){String dl=rl+"*"+s(l,"w"+n),dr=rr+"*"+s(r,"w"+n);g(f+"species"+n,A+"*0.5*("+dl+"*"+ul+"+"+dr+"*"+ur+"-"+a+"*("+dr+"-"+dl+"))");}
 }
 static String net(int i,int k,String n){return rf(i,k)+n+"-"+rf(i+1,k)+n+"+"+zf(i,k)+n+"-"+zf(i,k+1)+n;}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("gridPlume",a[0]);System.out.println("GRID INPUT LOADED");for(String ss:m.sol().tags())m.sol().remove(ss);
  
  m.component().remove("outer");m.component("c").physics().remove("plas");m.component("c").physics().remove("bulk");
  m.variable().create("axialFlux");m.param().set("tp","120[ns]");m.param().set("cameraZero","90[ns]");
  g("axialIncident","Ein/(tau*sqrt(pi/(4*log(2))))*exp(-4*log(2)*((t-tp)/tau)^2)");
  g("targetMassSource","c.intSurf(c.mdot)");g("targetPowerSource","c.intSurf(c.Qmass-c.Qexchange)");g("targetElectronSource","c.intSurf(c.electronFeed)");
  m.component("c").variable("gasv").set("pulse","exp(-4*log(2)*((t-tp)/tau)^2)");
  m.component("c").variable("gasv").set("Ipeak","2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");
  for(int j=0;j<nr*nz;j++){
   String sf=suffix(j);AddAxialComponent.apply(m,c(j),sf);m.component(c(j)).physics().remove("bulk"+sf);
   v(j,"logGasT","logT"+sf);v(j,"gasEnergy","energyGas"+sf);v(j,"Etot","gasEnergy*energyScale/Vp");v(j,"volumeRate","0[1/s]");v(j,"Vdot","0[m^3/s]");v(j,"Msweep","0[kg/s]");
   v(j,"sound","sqrt((1+pPl/(CvVol*Tg+Ue))*pPl/rhoP)");v(j,"laserTau","alphaLaser*dz");v(j,"beamTransmission","exp(-laserTau)");v(j,"beamAbsorptance","if(laserTau<1e-4,laserTau*(1-laserTau/2+laserTau^2/6),1-exp(-laserTau))");
   v(j,"Plaser","(incomingForward+incomingBack)*beamAbsorptance");v(j,"meanLaserIntensity","(incomingForward+incomingBack)*dz/Vp*if(laserTau<1e-4,1-laserTau/2+laserTau^2/6,beamAbsorptance/laserTau)");
   m.component(c(j)).physics().create("axialBulk"+sf,"GlobalEquations","g"+sf);
   m.component(c(j)).mesh().create("dummy"+sf);m.component(c(j)).mesh("dummy"+sf).autoMeshSize(9);m.component(c(j)).mesh("dummy"+sf).run();
   System.out.println("NATIVE CELL "+j+"/"+(nr*nz));System.out.flush();
  }

  sp.addAll(Arrays.asList(Chemistry.NAMES));sp.add("CuT5");sp.add("Cu_2p");sp.add("Cu_3p");sp.add("Cu_4p");Map<String,String> chemistry=AxialChemicalRates.rates();
  g("gridPlumeMode","1");g("gridRadialCount",Integer.toString(nr));g("gridAxialCount",Integer.toString(nz));g("axialCellCount",Integer.toString(nr*nz));
  for(int k=0;k<nz;k++)for(int i=0;i<nr;i++){
   int j=k*nr+i;String sf=suffix(j),pl=p(j);double dz=ze[k+1]-ze[k],az=Math.PI*(re[i+1]*re[i+1]-re[i]*re[i]);
   v(j,"rLower",num(re[i])+"[m]");v(j,"rUpper",num(re[i+1])+"[m]");v(j,"rCenter",num((re[i]+re[i+1])/2)+"[m]");v(j,"zLower",num(ze[k])+"[m]");v(j,"zUpper",num(ze[k+1])+"[m]");v(j,"zCenter",num((ze[k]+ze[k+1])/2)+"[m]");v(j,"dz",num(dz)+"[m]");
   v(j,"Vp",num(az*dz)+"[m^3]");v(j,"Rp","rUpper");v(j,"Hp","dz");v(j,"Req","(3*Vp/(4*pi))^(1/3)");v(j,"uZ","moment"+sf+"*momentumScale/Mgas");v(j,"uR","radMoment"+sf+"*momentumScale/Mgas");v(j,"u","uZ");v(j,"vRadius","uR");v(j,"vHeight","uZ");v(j,"Kinetic","0.5*Mgas*(uR^2+uZ^2)");
   v(j,"Msource",net(i,k,"mass")+(k==0?"+columnMass"+i:""));v(j,"Psource",k==0?"columnPower"+i:"0[W]");
   String div=num(2*Math.PI*re[i+1]*dz)+"[m^2]*"+rf(i+1,k)+"velocity-"+num(2*Math.PI*re[i]*dz)+"[m^2]*"+rf(i,k)+"velocity+"+num(az)+"[m^2]*("+zf(i,k+1)+"velocity-"+zf(i,k)+"velocity)";
   v(j,"PeSource",net(i,k,"electron")+"-pe*("+div+")"+(k==0?"+columnElectron"+i:""));
   String forward="0",back="0";for(int kk=k+1;kk<nz;kk++)forward+="+"+s(kk*nr+i,"laserTau");for(int kk=0;kk<nz;kk++)back+="+"+s(kk*nr+i,"laserTau");for(int kk=0;kk<k;kk++)back+="+"+s(kk*nr+i,"laserTau");
   String beam="(exp(-2*(rLower/w0)^2)-exp(-2*(rUpper/w0)^2))";
   v(j,"incomingForward","axialIncident*"+beam+"*exp(-("+forward+"))");v(j,"incomingBack","(1-Acu)*axialIncident*"+beam+"*exp(-("+back+"))");
   for(String n:sp){if(n.equals("N2"))continue;String nn=n.replaceFirst("(_[1-4][pm])$",sf+"$1")+(n.contains("_")?"":sf),rate;
    if(n.matches("Cu_[234]p")){int q=n.charAt(3)-'0';rate="(multNet"+(q-1)+(q<4?"-multNet"+q:"")+")/N_A_const";}
    else{rate=chemistry.get(n).replace("plas.",pl+".");if(n.equals("Cu_1p"))rate="("+rate+")-multNet1/N_A_const";}
    String input=net(i,k,"species"+n);if(k==0&&n.startsWith("Cu"))input+="+columnSpecies"+i+"_"+n;
    m.component(c(j)).physics(pl).feature(nn).featureInfo("info").set(pl+".R_w"+nn,new String[]{"("+rate+")+("+input+")/(Vp*"+pl+".M_w"+n+")"});
   }
   m.component(c(j)).physics("axialBulk"+sf).feature("ge1").set("name",new String[]{"energyGas"+sf,"logT"+sf,"moment"+sf,"radMoment"+sf});
   m.component(c(j)).physics("axialBulk"+sf).feature("ge1").set("equation",new String[]{"energyGas"+sf+"t-("+net(i,k,"energy")+"+Psource+Plaser-Qrad*Vp)/energyScale","(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue)-Kinetic)/energyScale","moment"+sf+"t-("+net(i,k,"momentZ")+(k==0?"+columnMomentum"+i:"")+")/momentumScale","radMoment"+sf+"t-("+net(i,k,"momentR")+"+pPl*"+num(2*Math.PI*dz*(re[i+1]-re[i]))+"[m^2])/momentumScale"});
   m.component(c(j)).physics("axialBulk"+sf).feature("ge1").set("initialValueU",new String[]{"2.5*pAmb*Vp/energyScale","log(Tamb/1[K])","0","0"});
  }
  for(int k=0;k<nz;k++)for(int i=0;i<=nr;i++){double ar=2*Math.PI*re[i]*(ze[k+1]-ze[k]);if(i==0)wall(rf(i,k),ar,k*nr,true);else face(rf(i,k),ar,k*nr+i-1,i==nr?-1:k*nr+i,true);}
  for(int i=0;i<nr;i++)for(int k=0;k<=nz;k++){double ar=Math.PI*(re[i+1]*re[i+1]-re[i]*re[i]);if(k==0)wall(zf(i,k),ar,i,false);else face(zf(i,k),ar,(k-1)*nr+i,k==nz?-1:k*nr+i,false);}
  
  String trans="0",tg="0[K]",rho="0[kg/m^3]",pcu="0[Pa]",ret="0[kg/(m^2*s)]",returnH="0[W/m^2]";
  for(int i=0;i<nr;i++){
   String mask="if(r>="+num(re[i])+"[m]&&r<"+num(re[i+1])+"[m],1,0)",tau="0";
   for(int k=0;k<nz;k++)tau+="+"+s(k*nr+i,"laserTau");
   g("columnTransmission"+i,"exp(-("+tau+"))");trans+="+"+mask+"*columnTransmission"+i;
   tg+="+"+mask+"*"+s(i,"Tg");pcu+="+"+mask+"*"+s(i,"pPl");
   String returnMass="0[kg/(m^2*s)]",returnEnergy="0[W/m^2]",returnElectron="0[W/m^2]";
   for(String n:sp)if(n.startsWith("Cu")){
    String nn="n_"+n;
    String density=s(i,"rhoP")+"*"+s(i,"w"+n);
    String rr="("+density+")*sqrt(R_const*"+s(i,"Tg")+"/(2*pi*Mcu))";
    g("returnRing"+i+"_"+n,rr);returnMass+="+returnRing"+i+"_"+n;
    double ev=0;int charge=0;
    String[] cn={"Cu","CuD5","CuD3","CuP1","CuP3","CuH3","CuH5","CuT5","Cu_1p","Cu_2p","Cu_3p","Cu_4p"};
    double[] ce={0,1.388948,1.6422256,3.7858977,3.816692,6.1911751,6.1920252,6.8676456,7.72638,28.01877,64.85977,122.23977};
    for(int q=0;q<cn.length;q++)if(n.equals(cn[q])){ev=ce[q];charge=q>=8?q-7:0;}
    returnEnergy+="+returnRing"+i+"_"+n+"*(Bcu+2.5*R_const*"+s(i,"Tg")+"/Mcu+"+num(ev)+"[V]*F_const/Mcu+0.5*("+s(i,"uR")+"^2+"+s(i,"uZ")+"^2))";
    returnElectron+="+returnRing"+i+"_"+n+"*"+charge+"*2*R_const*"+s(i,"TeK")+"/Mcu";
    String emission=n.matches("Cu_[234]p")?"0[kg/(m^2*s)]":"mdotEvap*emitFraction_"+n;
    g("columnSpecies"+i+"_"+n,"c.intSurf("+mask+"*("+emission+"-returnRing"+i+"_"+n+"))");
   }
   g("columnReturn"+i,returnMass);g("columnReturnElectron"+i,returnElectron);g("columnReturnEnergy"+i,returnEnergy+"+columnReturnElectron"+i);
   ret+="+"+mask+"*columnReturn"+i;returnH+="+"+mask+"*columnReturnEnergy"+i;
   g("columnMass"+i,"c.intSurf("+mask+"*mdot)");g("columnPower"+i,"c.intSurf("+mask+"*(Qmass-Qexchange))");g("columnElectron"+i,"c.intSurf("+mask+"*(electronFeed-columnReturnElectron"+i+"))");
   g("columnMomentum"+i,"c.intSurf("+mask+"*(mdotEvap*sqrt(R_const*T/Mcu)-columnReturn"+i+"*"+s(i,"uZ")+"))");
  }
  m.component("c").variable("boundv").set("gridTransmission",trans);m.component("c").variable("boundv").set("gridGasT",tg);m.component("c").variable("boundv").set("pPl",pcu);
  m.component("c").variable("boundv").set("mdotReturn",ret);m.component("c").variable("boundv").set("mdot","mdotEvap-mdotReturn");
  m.component("c").variable("boundv").set("Qsurface","Acu*Ipeak*pulse*exp(-2*(r/w0)^2)*gridTransmission");
  m.component("c").variable("boundv").set("Qexchange","0[W/m^2]");
  m.component("c").variable("boundv").set("electronFeed","2*R_const*T/Mcu*mdotEvap*emitFraction_Cu_1p");
  m.component("c").variable("boundv").set("Qmass","mdotEvap*(Bcu+3*R_const*T/Mcu+F_const*emitMeanEnergy/Mcu)+electronFeed-("+returnH+")");
  m.component("c").physics("ht").feature("laser").set("q0","Qsurface-Qmass+Qexchange");
  m.component(c(0)).physics().create("ledger","GlobalEquations","gA");
  m.component(c(0)).physics("ledger").feature("ge1").set("name",new String[]{"axialInputEnergy","axialRadiatedEnergy","axialOutEnergy","axialInputCu","axialOutputCu","targetLaserEnergy"});
  m.component(c(0)).physics("ledger").feature("ge1").set("equation",new String[]{"axialInputEnergyt-(targetPowerSource+axialLaser)/energyScale","axialRadiatedEnergyt-axialRadiation/energyScale","axialOutEnergyt-energyF24/energyScale","axialInputCut-targetMassSource/mScale","axialOutputCut-axialOutCu/mScale","targetLaserEnergyt-c.intSurf(c.Qsurface)/energyScale"});
  m.component(c(0)).physics("ledger").feature("ge1").set("initialValueU",new String[]{"0","0","0","0","0","0"});

  String ene="0[J]",cu="0[kg]",rad="0[W]",laser="0[W]",outE="0[W]",outCu="0[kg/s]";
  for(int j=0;j<nr*nz;j++){ene+="+"+s(j,"gasEnergy")+"*energyScale";cu+="+"+s(j,"rhoCopper")+"*"+s(j,"Vp");rad+="+"+s(j,"Qrad")+"*"+s(j,"Vp");laser+="+"+s(j,"Plaser");}
  for(int k=0;k<nz;k++){outE+="+"+rf(nr,k)+"energy";for(String n:sp)if(n.startsWith("Cu"))outCu+="+"+rf(nr,k)+"species"+n;}
  for(int i=0;i<nr;i++){outE+="+"+zf(i,nz)+"energy";for(String n:sp)if(n.startsWith("Cu"))outCu+="+"+zf(i,nz)+"species"+n;}
  g("axialTotalEnergy",ene);g("axialCopper",cu);g("axialRadiation",rad);g("axialLaser",laser);g("axialOutCu",outCu);g("energyF24",outE);
  m.study("std").feature("time").set("tlist","range(0,2[ns],1000[ns])");m.study("std").feature("time").set("rtol",1e-4);m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.matches(".*_energy[A-Z]$")?"1e7":f.endsWith("_pA")?"1e5":f.contains("ODE")?"1":f.endsWith("_w")?(f.matches(".*_O2[A-Z]_w")?"1":"1e-8"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Spatial FV native Plasma with direct copper thermal/laser shielding feedback; 8r x10z, fixed surface, no sheath; not spatially converged");m.save(a[1]);System.out.println("SPATIAL THERMAL GRID READY "+(nr*nz)+" native cells");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
