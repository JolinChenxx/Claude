"""Generate the axisymmetric material predictor from the audited axial equations.
The resulting Java is inspectable; this is a hydro/material stage, not Plasma.
"""
from pathlib import Path
R=Path(__file__).resolve().parent
s=(R/'src/BuildAxialHEM.java').read_text()
s=s.replace('BuildAxialHEM','BuildAxisymmetricHEM').replace('axialHEM','axisymmetricHEM')
start=s.index('  m.component().create("c",true);')
end=s.index('  m.func().create("cuEOS"',start)
s=s[:start]+'''  m.component().create("c",true);m.component("c").geom().create("g",2);m.component("c").geom("g").axisymmetric(true);
  double[] b={-100,-20,-5,-1,-.2,0,.2,1,5,20,100,400,1500};int[] counts={8,12,20,40,20,20,40,40,30,30,15,15};
  double[] rb={0,60,500};int[] rn={30,24};
  for(int k=0;k<2;k++)for(int i=0;i<b.length-1;i++){String f="seg"+k+"_"+i;m.component("c").geom("g").create(f,"Rectangle");m.component("c").geom("g").feature(f).set("pos",new String[]{rb[k]+"[um]",b[i]+"[um]"});m.component("c").geom("g").feature(f).set("size",new String[]{(rb[k+1]-rb[k])+"[um]",(b[i+1]-b[i])+"[um]"});m.component("c").geom("g").feature(f).set("selresult",true);}
  m.component("c").geom("g").run();
  box("top",1,-.001,500.001,1499.999,1500.001);box("bottom",1,-.001,500.001,-100.001,-99.999);box("right",1,499.999,500.001,-100.001,1500.001);box("axis",1,-.001,.001,-100.001,1500.001);
  m.component("c").selection().create("far","Union");m.component("c").selection("far").set("entitydim",1);m.component("c").selection("far").set("input",new String[]{"top","bottom","right"});
'''+s[end:]
s=s.replace(' static Model m;', ''' static void box(String tag,int dim,double r0,double r1,double z0,double z1){m.component("c").selection().create(tag,"Box");m.component("c").selection(tag).set("entitydim",dim);m.component("c").selection(tag).set("xmin",r0+"[um]");m.component("c").selection(tag).set("xmax",r1+"[um]");m.component("c").selection(tag).set("ymin",z0+"[um]");m.component("c").selection(tag).set("ymax",z1+"[um]");m.component("c").selection(tag).set("condition","inside");}
 static Model m;''')
s=s.replace('v("u","rhoS*uS*qm/rho");','v("u","rhoS*uS*qm/rho");v("ur","rhoS*uS*qr/rho");v("axisWeight","max(r,1e-12[m])/500[um]");')
s=s.replace('rho*u^2/2','rho*(u^2+ur^2)/2')
s=s.replace('v("Dnum","0.15*h*(sqrt(u^2+1[m^2/s^2])+cs)");','v("shockSensor","min(1,h*sqrt(d(pGas,r)^2+d(pGas,z)^2+1[Pa^2/m^2])/(abs(pGas)+pAmb))");v("Dnum","0.15*h*(sqrt(u^2+ur^2+1e-8[m^2/s^2])+cs*shockSensor)");')
s=s.replace('Acu*alphaOpt*Ipeak*light','Acu*alphaOpt*Ipeak*exp(-2*(r/w0)^2)*light').replace('(1-Acu)*alphaOpt*Ipeak*light','(1-Acu)*alphaOpt*Ipeak*exp(-2*(r/w0)^2)*light')
s=s.replace('tanh(x/','tanh(z/').replace('max(-x,','max(-z,')
start=s.index('  m.component("c").physics().create("hydro"')
end=s.index('  m.component("c").physics().create("thermal"',start)
s=s[:start]+'''  m.component("c").physics().create("hydro","GeneralFormPDE","g",new String[]{"wC","wA","qr","qm","qE"});
  m.component("c").physics("hydro").feature("gfeq1").set("da",new String[]{"axisWeight*exp(wC)","axisWeight*exp(wA)","axisWeight","axisWeight","axisWeight"});
  m.component("c").physics("hydro").feature("gfeq1").set("Ga",new String[][]{{"axisWeight*(rhoC*ur-Dnum*d(rhoC,r))/rhoS","axisWeight*(rhoC*u-Dnum*d(rhoC,z))/rhoS"},{"axisWeight*(rhoA*ur-Dnum*d(rhoA,r))/rhoAmb","axisWeight*(rhoA*u-Dnum*d(rhoA,z))/rhoAmb"},{"axisWeight*((rho*ur^2+pGas-pAmb)/(rhoS*uS)-Dnum*d(qr,r))","axisWeight*(rho*ur*u/(rhoS*uS)-Dnum*d(qr,z))"},{"axisWeight*(rho*u*ur/(rhoS*uS)-Dnum*d(qm,r))","axisWeight*((rho*u^2+pGas-pAmb)/(rhoS*uS)-Dnum*d(qm,z))"},{"axisWeight*(((Etot+pGas)*ur-kMix*d(T,r))/(rhoS*eS)-Dnum*d(qE,r))","axisWeight*(((Etot+pGas)*u-kMix*d(T,z))/(rhoS*eS)-Dnum*d(qE,z))"}});
  m.component("c").physics("hydro").feature("gfeq1").set("f",new String[]{"0","0","(pGas-pAmb)/(rhoS*uS*500[um])-axisWeight*drag*ur/(rhoS*uS)","-axisWeight*drag*u/(rhoS*uS)","axisWeight*Qabs/(rhoS*eS)"});
  for(String[] q:new String[][]{{"wC","log(rhoCi/rhoS)"},{"wA","log(rhoAi/rhoAmb)"},{"qr","0"},{"qm","0"},{"qE","Ei/(rhoS*eS)"}})m.component("c").physics("hydro").feature("init1").set(q[0],q[1]);
'''+s[end:]
s=s.replace('.set("Ga","0")','.set("Ga",new String[]{"0","0"})')
s=s.replace('.set("f","(Etot-rho*(u^2+ur^2)/2-Einternal)/(rhoS*eS)")','.set("f","axisWeight*(Etot-rho*(u^2+ur^2)/2-Einternal)/(rhoS*eS)")')
s=s.replace('.set("Ga","-light-0.005*h*d(light,x)")','.set("Ga",new String[]{"0","axisWeight*(-light-0.005*h*d(light,z))"})').replace('.set("f","-alphaOpt*light")','.set("f","-axisWeight*alphaOpt*light")')
start=s.index('  int nb=b.length;')
end=s.index('  for(String ph:',start)
s=s[:start]+'''  m.component("c").physics("optics").create("incident","DirichletBoundary",1);m.component("c").physics("optics").feature("incident").selection().named("top");m.component("c").physics("optics").feature("incident").set("r","pulse");
  m.component("c").physics("hydro").create("farbc","DirichletBoundary",1);m.component("c").physics("hydro").feature("farbc").selection().named("far");m.component("c").physics("hydro").feature("farbc").set("r",new String[]{"log(rhoCi/rhoS)","log(rhoAi/rhoAmb)","0","0","Ei/(rhoS*eS)"});
  m.component("c").physics("hydro").create("axisbc","DirichletBoundary",1);m.component("c").physics("hydro").feature("axisbc").selection().named("axis");m.component("c").physics("hydro").feature("axisbc").set("r",new String[]{"0","0","0","0","0"});m.component("c").physics("hydro").feature("axisbc").set("useDirichletCondition",new int[]{0,0,1,0,0});
  m.component("c").mesh().create("mesh");
  for(int i=0;i<counts.length;i++){
   String f="zEdges"+i;box(f,1,-.001,500.001,b[i]-.001,b[i+1]+.001);
   m.component("c").selection().create(f+"vert","Box");m.component("c").selection(f+"vert").set("entitydim",1);m.component("c").selection(f+"vert").set("xmin","-0.001[um]");m.component("c").selection(f+"vert").set("xmax","0.001[um]");m.component("c").selection(f+"vert").set("ymin",(b[i]-.001)+"[um]");m.component("c").selection(f+"vert").set("ymax",(b[i+1]+.001)+"[um]");m.component("c").selection(f+"vert").set("condition","inside");
   m.component("c").mesh("mesh").create(f,"Edge");m.component("c").mesh("mesh").feature(f).selection().named(f+"vert");m.component("c").mesh("mesh").feature(f).create("dist","Distribution");m.component("c").mesh("mesh").feature(f).feature("dist").set("numelem",counts[i]);
  }
  for(int k=0;k<2;k++){String f="radial"+k;box(f,1,rb[k]-.001,rb[k+1]+.001,-100.001,-99.999);m.component("c").mesh("mesh").create(f,"Edge");m.component("c").mesh("mesh").feature(f).selection().named(f);m.component("c").mesh("mesh").feature(f).create("dist","Distribution");m.component("c").mesh("mesh").feature(f).feature("dist").set("numelem",rn[k]);}
  m.component("c").mesh("mesh").create("map","Map");m.component("c").mesh("mesh").run();
'''+s[end:]
s=s.replace('m.component("c").cpl("intAll").selection().all();','m.component("c").cpl("intAll").selection().all();m.component("c").cpl("intAll").set("opname","intAll");m.component("c").cpl("intAll").set("axisym",true);')
s=s.replace('  m.label(','''  for(String f:m.sol("sol1").feature("v1").feature().tags()){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",f.equals("c_qE")||f.equals("c_qm")||f.equals("c_qr")?"1e-3":"1");}
  for(String f:m.sol("sol1").feature("t1").feature().tags())if(m.sol("sol1").feature("t1").feature(f).getType().equals("FullyCoupled")){m.sol("sol1").feature("t1").feature(f).set("maxiter",15);m.sol("sol1").feature("t1").feature(f).set("jtech","minimal");}
  m.label(''')
s=s.replace('Axial HEM copper-air EOS/flux test','Axisymmetric HEM copper-air EOS/flux predictor').replace('AXIAL HEM READY','AXISYMMETRIC HEM READY')
(R/'src/BuildAxisymmetricHEM.java').write_text(s)
print('Generated BuildAxisymmetricHEM.java')
