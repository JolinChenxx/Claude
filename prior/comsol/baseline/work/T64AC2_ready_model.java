/*
 * T64AC2_ready_model.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

/** Model exported on Nov 5 2026, 21:07 by COMSOL 6.4.0.429. */
public class T64AC2_ready_model {

  public static Model run() {
    Model model = ModelUtil.create("Model");

    model.label("Cu-air LTE laser plume: conservative PDE prototype");

    model.modelPath("E:/COMSOL_CuPlasma_20261102/work");

    model.param().set("Eabs", "20[uJ]");
    model.param().set("tau", "60[ns]");
    model.param().set("tp", "120[ns]");
    model.param().set("w0", "15.5[um]");
    model.param().set("Rdom", "1[mm]");
    model.param().set("Zdom", "2[mm]");
    model.param().set("Dcu", "100[um]");
    model.param().set("tEnd", "300[ns]");
    model.param().set("rho0", "1[kg/m^3]");
    model.param().set("u0", "1000[m/s]");
    model.param().set("e0", "1e6[J/kg]");
    model.param().set("rhoAmb", "1.1993359313106413[kg/m^3]");
    model.param().set("eAmb", "211277.55514080767[J/kg]");
    model.param().set("pAmb", "101325[Pa]");
    model.param().set("Bcu", "5815069.632191036[J/kg]");
    model.param().set("mCu", "63.546*1.66053906660e-27[kg]");
    model.param().set("kb", "1.380649e-23[J/K]");
    model.param().set("hplanck", "6.62607015e-34[J*s]");
    model.param().set("nuLight", "c_const/1064[nm]");
    model.param().set("nuFactor", "0.15");
    model.param().set("meshFactor", "1");

    model.func().create("lte", "Interpolation");
    model.func("lte").set("source", "file");
    model.func("lte").set("filename", "E:/COMSOL_CuPlasma_20261102/data/lte_grid.txt");
    model.func("lte").set("struct", "grid");
    model.func("lte").set("nargs", 3);
    model.func("lte")
         .set("funcs", new String[][]{{"logTpl", "1"}, {"logPrho", "2"}, {"logNe", "3"}, {"logNiZ2", "4"}, {"xCuLTE", "5"}});
    model.func("lte").importData();

    model.component().create("c", true);

    model.component("c").geom().create("g", 2);
    model.component("c").geom("g").axisymmetric(true);
    model.component("c").geom("g").lengthUnit("um");
    model.component("c").geom("g").create("cu", "Rectangle");
    model.component("c").geom("g").feature("cu").set("pos", new String[]{"0", "-Dcu"});
    model.component("c").geom("g").feature("cu").set("size", new String[]{"Rdom", "Dcu"});
    model.component("c").geom("g").create("gas", "Rectangle");
    model.component("c").geom("g").feature("gas").set("size", new String[]{"Rdom", "Zdom"});
    model.component("c").geom("g").run();

    model.component("c").variable().create("v");
    model.component("c").variable("v").set("rhoP", "rho0*qR");
    model.component("c").variable("v").set("rhoSafe", "max(rhoP,1e-9[kg/m^3])");
    model.component("c").variable("v").set("Yraw", "qC/max(qR,1e-9)");
    model.component("c").variable("v").set("Ycu", "min(1,max(0,Yraw))");
    model.component("c").variable("v").set("urP", "u0*qMr/max(qR,1e-9)");
    model.component("c").variable("v").set("uzP", "u0*qMz/max(qR,1e-9)");
    model.component("c").variable("v").set("speedP", "sqrt(urP^2+uzP^2)");
    model.component("c").variable("v").set("ethRaw", "e0*(qE-(qMr^2+qMz^2)/(2*max(qR,1e-9)))-Bcu*qC");
    model.component("c").variable("v").set("leArg", "log10(max(ethRaw/rhoSafe*rho0,6400[J/kg])/1[J/kg])");
    model.component("c").variable("v").set("lrArg", "log10(rhoSafe/1[kg/m^3])");
    model.component("c").variable("v").set("Tpl", "10^logTpl(leArg,lrArg,Ycu)*1[K]");
    model.component("c").variable("v").set("pPl", "rhoSafe*10^logPrho(leArg,lrArg,Ycu)*1[J/kg]");
    model.component("c").variable("v").set("nePl", "10^logNe(leArg,lrArg,Ycu)*1[1/m^3]");
    model.component("c").variable("v").set("niZ2", "10^logNiZ2(leArg,lrArg,Ycu)*1[1/m^3]");
    model.component("c").variable("v").set("xionCu", "xCuLTE(leArg,lrArg,Ycu)");
    model.component("c").variable("v").set("csBound", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("v").set("Dnum", "nuFactor*h*(speedP+csBound)");
    model.component("c").variable("v").set("kGas", "(0.025+0.00002*max(Tpl/1[K]-293.15,0))*1[W/(m*K)]");
    model.component("c").variable("v")
         .set("alphaIB", "0.03692*1[m^5*K^0.5/s^3]*nePl*niZ2/(sqrt(Tpl)*nuLight^3)*(1-exp(-hplanck*nuLight/(kb*Tpl)))");
    model.component("c").variable("v").set("Qrad", "1.426e-40*1[W*m^3/K^0.5]*sqrt(Tpl)*nePl*niZ2");
    model.component("c").variable("v").set("Ipeak", "Eabs*2/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");
    model.component("c").variable("v").set("Ipulse", "exp(-4*log(2)*((t-tp)/tau)^2)");
    model.component("c").variable("v").set("Irad", "exp(-2*(r/w0)^2)");
    model.component("c").variable("v").set("Igas", "Ipeak*Irad*opt");
    model.component("c").variable("v").set("QlaserP", "alphaIB*Igas");
    model.component("c").variable("v").set("lambdaD", "sqrt(epsilon0_const*kb*Tpl/(e_const^2*max(nePl,1[1/m^3])))");
    model.component("c").variable("v")
         .set("Ts", "if(hs*e0<385[J/(kg*K)]*(1346[K]-293.15[K]),293.15[K]+hs*e0/(385[J/(kg*K)]),if(hs*e0<385[J/(kg*K)]*(1346[K]-293.15[K])+196000[J/kg],1346[K]+(hs*e0-385[J/(kg*K)]*(1346[K]-293.15[K]))/(9800[J/(kg*K)]),1366[K]+(hs*e0-385[J/(kg*K)]*(1346[K]-293.15[K])-196000[J/kg])/(572[J/(kg*K)])))");
    model.component("c").variable("v").set("kCu", "(400-235*flc2hs(Ts-1356[K],10[K]))*1[W/(m*K)]");
    model.component("c").variable("v")
         .set("psatCu", "101325[Pa]*exp(mCu*5.3e6[J/kg]/kb*(1/2840[K]-1/max(Ts,150[K])))");
    model.component("c").variable("v").set("mdot", "psatCu*sqrt(mCu/(2*pi*kb*max(Ts,150[K])))");
    model.component("c").variable("v").set("vKn", "sqrt(kb*max(Ts,150[K])/mCu)");
    model.component("c").variable("v").set("hInj", "Bcu+2.5*kb*Ts/mCu+vKn^2/2");
    model.component("c").variable("v").set("qExchange", "kGas*(Tpl-Ts)/(max(h,0.2[um])/2)");

    model.component("c").physics()
         .create("flow", "GeneralFormPDE", "g", new String[]{"qR", "qMr", "qMz", "qE", "qC"});
    model.component("c").physics("flow").selection().set(2);
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"u0*qMr-Dnum*qRr", "u0*qMz-Dnum*qRz"}, {"u0*qMr*qMr/max(qR,1e-9)+(pPl-pAmb)/(rho0*u0)-Dnum*qMrr", "u0*qMr*qMz/max(qR,1e-9)-Dnum*qMrz"}, {"u0*qMr*qMz/max(qR,1e-9)-Dnum*qMzr", "u0*qMz*qMz/max(qR,1e-9)+(pPl-pAmb)/(rho0*u0)-Dnum*qMzz"}, {"urP*(qE+pPl/(rho0*e0))-Dnum*qEr-kGas*d(Tpl,r)/(rho0*e0)", "uzP*(qE+pPl/(rho0*e0))-Dnum*qEz-kGas*d(Tpl,z)/(rho0*e0)"}, {"urP*qC-Dnum*qCr", "uzP*qC-Dnum*qCz"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"0", "(pPl-pAmb)/(rho0*u0*max(r,1e-10[m]))", "0", "(QlaserP-Qrad)/(rho0*e0)", "0"});
    model.component("c").physics("flow").feature("init1").set("qMr", "0");
    model.component("c").physics("flow").feature("init1").set("qMz", "0");
    model.component("c").physics("flow").feature("init1").set("qC", "0");
    model.component("c").physics("flow").feature("init1").set("qR", "rhoAmb/rho0");
    model.component("c").physics("flow").feature("init1").set("qE", "rhoAmb*eAmb/(rho0*e0)");
    model.component("c").physics("flow").create("abl", "FluxBoundary", 1);
    model.component("c").physics("flow").feature("abl").selection().set(4);
    model.component("c").physics("flow").feature("abl")
         .set("g", new String[]{"mdot/rho0", "0", "(pPl-pAmb+mdot*vKn)/(rho0*u0)", "(mdot*hInj-qExchange)/(rho0*e0)", "mdot/rho0"});
    model.component("c").physics("flow").create("axis", "DirichletBoundary", 1);
    model.component("c").physics("flow").feature("axis").selection().set(3);
    model.component("c").physics("flow").feature("axis").set("r", new String[]{"0", "0", "0", "0", "0"});
    model.component("c").physics("flow").feature("axis").set("useDirichletCondition", new int[]{0, 1, 0, 0, 0});
    model.component("c").physics("flow").create("far", "DirichletBoundary", 1);
    model.component("c").physics("flow").feature("far").selection().set(5, 7);
    model.component("c").physics("flow").feature("far")
         .set("r", new String[]{"rhoAmb/rho0", "0", "0", "rhoAmb*eAmb/(rho0*e0)", "0"});
    model.component("c").physics().create("solid", "GeneralFormPDE", "g", new String[]{"hs"});
    model.component("c").physics("solid").selection().set(1);
    model.component("c").physics("solid").feature("gfeq1")
         .set("Ga", new String[]{"-kCu*d(Ts,r)/(8940[kg/m^3]*e0)", "-kCu*d(Ts,z)/(8940[kg/m^3]*e0)"});
    model.component("c").physics("solid").feature("gfeq1").set("f", "0");
    model.component("c").physics("solid").create("heat", "FluxBoundary", 1);
    model.component("c").physics("solid").feature("heat").selection().set(4);
    model.component("c").physics("solid").feature("heat").set("g", "(Igas-mdot*hInj+qExchange)/(8940[kg/m^3]*e0)");
    model.component("c").physics().create("optics", "GeneralFormPDE", "g", new String[]{"opt"});
    model.component("c").physics("optics").selection().set(2);
    model.component("c").physics("optics").feature("gfeq1").set("da", "0");
    model.component("c").physics("optics").feature("gfeq1").set("Ga", new String[]{"0", "-opt-0.02*h*optz"});
    model.component("c").physics("optics").feature("gfeq1").set("f", "-alphaIB*opt");
    model.component("c").physics("optics").feature("init1").set("opt", "Ipulse");
    model.component("c").physics("optics").create("in", "DirichletBoundary", 1);
    model.component("c").physics("optics").feature("in").selection().set(5);
    model.component("c").physics("optics").feature("in").set("r", "Ipulse");
    model.component("c").physics("optics").create("exit", "FluxBoundary", 1);
    model.component("c").physics("optics").feature("exit").selection().set(4);
    model.component("c").physics("optics").feature("exit").set("g", "-opt");
    model.component("c").physics().create("rec", "BoundaryODE", "g", new String[]{"depth"});
    model.component("c").physics("rec").selection().set(4);
    model.component("c").physics("rec").feature("dode1").set("f", "mdot/8940[kg/m^3]");

    model.component("c").mesh().create("mesh");
    model.component("c").mesh("mesh").create("tri", "FreeTri");
    model.component("c").mesh("mesh").feature("tri").create("sz", "SizeExpression");
    model.component("c").mesh("mesh").feature("tri").feature("sz")
         .set("sizeexpr", "meshFactor*min(80[um],0.8[um]+0.06*sqrt(r^2+z^2))");
    model.component("c").mesh("mesh").feature("tri").feature("sz").set("evaltype", "grid");
    model.component("c").mesh("mesh").feature("tri").feature("sz").set("numcell", 500);
    model.component("c").mesh("mesh").feature("tri").feature("sz").set("hmeshgrad", 1.25);
    model.component("c").mesh("mesh").run();

    model.study().create("std");
    model.study("std").create("time", "Transient");
    model.study("std").feature("time").set("tlist", "range(0,5[ns],tEnd)");

    model.sol().create("sol1");
    model.sol("sol1").study("std");
    model.sol("sol1").create("st1", "StudyStep");
    model.sol("sol1").feature("st1").set("study", "std");
    model.sol("sol1").feature("st1").set("studystep", "time");
    model.sol("sol1").create("v1", "Variables");
    model.sol("sol1").feature("v1").set("control", "time");
    model.sol("sol1").create("t1", "Time");
    model.sol("sol1").feature("t1").set("tlist", "range(0,5[ns],tEnd)");
    model.sol("sol1").feature("t1").set("plot", "off");
    model.sol("sol1").feature("t1").set("plotgroup", "Default");
    model.sol("sol1").feature("t1").set("plotfreq", "tout");
    model.sol("sol1").feature("t1").set("probesel", "all");
    model.sol("sol1").feature("t1").set("probes", new String[]{});
    model.sol("sol1").feature("t1").set("probefreq", "tsteps");
    model.sol("sol1").feature("t1").set("atolglobalvaluemethod", "factor");
    model.sol("sol1").feature("t1").set("endtimeinterpolation", true);
    model.sol("sol1").feature("t1").set("control", "time");
    model.sol("sol1").feature("t1").create("seDef", "Segregated");
    model.sol("sol1").feature("t1").create("fc1", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("fc1").set("linsolver", "dDef");
    model.sol("sol1").feature("t1").feature().remove("fcDef");
    model.sol("sol1").feature("t1").feature().remove("seDef");
    model.sol("sol1").attach("std");
    model.sol("sol1").feature("t1").set("rtol", 0.005);
    model.sol("sol1").feature("t1").set("maxorder", 1);
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("plot", "off");

    model.label("P01_20uJ_ready.mph");

    model.component("c").variable("v").set("speedP", "sqrt(urP^2+uzP^2+(1e-6[m/s])^2)");
    model.component("c").variable("v").set("qExchange", "kGas*(Tpl-Ts)/0.5[um]");

    model.param().set("Eabs", "0[uJ]");
    model.param().set("tEnd", "20[ns]");

    model.sol("sol1").clearSolutionData();

    model.label("P00b_zero_ready.mph");

    model.param().set("Eabs", "0[uJ]");
    model.param().set("tEnd", "20[ns]");

    model.sol("sol1").clearSolutionData();
    model.sol("sol1").feature("v1").feature("c_depth").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_depth").set("scaleval", "1e-6");
    model.sol("sol1").feature("v1").feature("c_hs").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_hs").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_opt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_opt").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qC").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qC").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("t1").set("atolglobalmethod", "scaled");
    model.sol("sol1").feature("t1").set("atolglobalvaluemethod", "factor");
    model.sol("sol1").feature("t1").set("atolglobalfactor", 0.01);

    model.label("P00c_zero_ready.mph");

    model.func("lte").discardData();
    model.func("lte").set("struct", "grid");
    model.func("lte").importData();

    model.param().set("tmp", "10^logTpl(log10(211277.55514080767),log10(1.1993359313106413),0)");

    model.sol("sol1").clearSolutionData();

    model.label("P00d_zero_ready.mph");

    model.param().set("Eabs", "20[uJ]");
    model.param().set("tEnd", "300[ns]");

    model.sol("sol1").clearSolutionData();

    model.label("P01b_20uJ_ready.mph");

    model.component("c").variable("v").set("axisWeight", "r/1[um]");

    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(u0*qMr-Dnum*qRr)", "axisWeight*(u0*qMz-Dnum*qRz)"}, {"axisWeight*(u0*qMr*qMr/max(qR,1e-9)+(pPl-pAmb)/(rho0*u0)-Dnum*qMrr)", "axisWeight*(u0*qMr*qMz/max(qR,1e-9)-Dnum*qMrz)"}, {"axisWeight*(u0*qMr*qMz/max(qR,1e-9)-Dnum*qMzr)", "axisWeight*(u0*qMz*qMz/max(qR,1e-9)+(pPl-pAmb)/(rho0*u0)-Dnum*qMzz)"}, {"axisWeight*(urP*(qE+pPl/(rho0*e0))-Dnum*qEr-kGas*d(Tpl,r)/(rho0*e0))", "axisWeight*(uzP*(qE+pPl/(rho0*e0))-Dnum*qEz-kGas*d(Tpl,z)/(rho0*e0))"}, {"axisWeight*(urP*qC-Dnum*qCr)", "axisWeight*(uzP*qC-Dnum*qCz)"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "(pPl-pAmb)/(rho0*u0*1[um])", "axisWeight*(0)", "axisWeight*((QlaserP-Qrad)/(rho0*e0))", "axisWeight*(0)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("abl")
         .set("g", new String[]{"axisWeight*(mdot/rho0)", "axisWeight*(0)", "axisWeight*((pPl-pAmb+mdot*vKn)/(rho0*u0))", "axisWeight*((mdot*hInj-qExchange)/(rho0*e0))", "axisWeight*(mdot/rho0)"});
    model.component("c").physics("solid").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(-kCu*d(Ts,r)/(8940[kg/m^3]*e0))", "axisWeight*(-kCu*d(Ts,z)/(8940[kg/m^3]*e0))"}});
    model.component("c").physics("solid").feature("gfeq1").set("f", new String[]{"axisWeight*(0)"});
    model.component("c").physics("solid").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("solid").feature("heat")
         .set("g", new String[]{"axisWeight*((Igas-mdot*hInj+qExchange)/(8940[kg/m^3]*e0))"});
    model.component("c").physics("optics").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-opt-0.02*h*optz)"}});
    model.component("c").physics("optics").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaIB*opt)"});
    model.component("c").physics("optics").feature("gfeq1").set("da", "0");
    model.component("c").physics("optics").feature("exit").set("g", new String[]{"axisWeight*(-opt)"});

    model.param().set("Eabs", "20[uJ]");
    model.param().set("tEnd", "300[ns]");

    model.sol("sol1").clearSolutionData();

    model.label("P02_20uJ_ready.mph");

    model.param().set("Eabs", "20[uJ]");
    model.param().set("tEnd", "300[ns]");
    model.param().set("nuFactor", "0.50");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("solid").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("optics").prop("ShapeProperty").set("order", 1);

    model.sol("sol1").clearSolutionData();

    model.label("P04_20uJ_ready.mph");

    model.param().set("Eabs", "20[uJ]");
    model.param().set("tEnd", "300[ns]");
    model.param().set("nuFactor", "1.0");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("solid").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("optics").prop("ShapeProperty").set("order", 1);

    model.sol("sol1").clearSolutionData();
    model.sol("sol1").feature("t1").set("rtol", 0.002);
    model.sol("sol1").feature("t1").set("atolglobalfactor", 1.0E-4);
    model.sol("sol1").feature("t1").set("maxstepbdf", "1[ns]");

    model.label("P05_20uJ_ready.mph");

    model.param().set("Eabs", "20[uJ]");
    model.param().set("tEnd", "300[ns]");
    model.param().set("nuFactor", "1.5");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("solid").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("optics").prop("ShapeProperty").set("order", 1);

    model.sol("sol1").clearSolutionData();
    model.sol("sol1").feature("t1").set("rtol", 0.002);
    model.sol("sol1").feature("t1").set("atolglobalfactor", 1.0E-4);
    model.sol("sol1").feature("t1").set("maxstepbdf", "1[ns]");

    model.label("P06_20uJ_ready.mph");

    model.param().set("Eabs", "20[uJ]");
    model.param().set("tEnd", "2[us]");
    model.param().set("nuFactor", "1.5");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("solid").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("optics").prop("ShapeProperty").set("order", 1);

    model.sol("sol1").clearSolutionData();
    model.sol("sol1").feature("t1").set("rtol", 0.002);
    model.sol("sol1").feature("t1").set("atolglobalfactor", 1.0E-4);
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");

    model.study("std").feature("time").set("tlist", "range(0,5[ns],300[ns]) range(320[ns],20[ns],tEnd)");

    model.sol("sol1").feature("t1").set("tlist", "range(0,5[ns],300[ns]) range(320[ns],20[ns],tEnd)");

    model.label("LTE_baseline_ready.mph");

    model.modelPath("E:/COMSOL_CuPlasma_S4_20261105/work");

    model.label("S4 Cu-air LTE: 400uJ incident, w=11um, two-pass optics");

    model.param().set("Ein", "400[uJ]");
    model.param().set("Asurf", "0.1");
    model.param().set("w0", "11[um]");
    model.param().set("tEnd", "2.12[us]");
    model.param().set("nuFactor", "1.5");
    model.param().set("netEvap", "0");
    model.param().set("meshFactor", "1");

    model.func("lte").set("filename", "E:/COMSOL_CuPlasma_S4_20261105/data/lte_grid.txt");

    model.component("c").variable("v").set("Ipeak", "Ein*2/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");
    model.component("c").variable("v").set("Idown", "Ipeak*Irad*Ipulse*opt");
    model.component("c").variable("v").set("Iup", "Ipeak*Irad*Ipulse*optUp");
    model.component("c").variable("v").set("Igas", "Asurf*Idown");
    model.component("c").variable("v").set("QlaserP", "alphaIB*(Idown+Iup)");

    model.component("c").physics("optics").feature("init1").set("opt", "1");
    model.component("c").physics("optics").feature("in").set("r", "1");
    model.component("c").physics().create("opticsUp", "GeneralFormPDE", "g", new String[]{"optUp"});
    model.component("c").physics("opticsUp").selection().set(2);
    model.component("c").physics("opticsUp").feature("gfeq1").set("da", "0");
    model.component("c").physics("opticsUp").feature("gfeq1")
         .set("Ga", new String[]{"0", "axisWeight*(optUp-0.02*h*d(optUp,z))"});
    model.component("c").physics("opticsUp").feature("gfeq1").set("f", "-axisWeight*alphaIB*optUp");
    model.component("c").physics("opticsUp").feature("init1").set("optUp", "1-Asurf");
    model.component("c").physics("opticsUp").create("reflect", "DirichletBoundary", 1);
    model.component("c").physics("opticsUp").feature("reflect").selection().set(4);
    model.component("c").physics("opticsUp").feature("reflect").set("r", "(1-Asurf)*opt");
    model.component("c").physics("opticsUp").create("escape", "FluxBoundary", 1);
    model.component("c").physics("opticsUp").feature("escape").selection().set(5);
    model.component("c").physics("opticsUp").feature("escape").set("g", "-axisWeight*optUp");

    model.component("c").variable("v").set("mdotEvap", "psatCu*sqrt(mCu/(2*pi*kb*max(Ts,150[K])))");
    model.component("c").variable("v").set("mdotReturn", "rhoSafe*Ycu*(1-xionCu)*sqrt(kb*Tpl/(2*pi*mCu))");
    model.component("c").variable("v").set("returnFraction", "mdotReturn/max(mdotEvap,1e-30[kg/(m^2*s)])");
    model.component("c").variable("v").set("mdot", "mdotEvap-netEvap*mdotReturn");
    model.component("c").variable("v").set("hReturn", "Bcu+2.5*kb*Tpl/mCu+speedP^2/2");
    model.component("c").variable("v").set("Qmass", "mdotEvap*hInj-netEvap*mdotReturn*hReturn");

    model.component("c").physics("flow").feature("abl")
         .set("g", new String[]{"axisWeight*mdot/rho0", "-axisWeight*netEvap*mdotReturn*urP/(rho0*u0)", "axisWeight*(pPl-pAmb+mdotEvap*vKn-netEvap*mdotReturn*uzP)/(rho0*u0)", "axisWeight*(Qmass-qExchange)/(rho0*e0)", "axisWeight*mdot/rho0"});
    model.component("c").physics("solid").feature("heat")
         .set("g", "axisWeight*(Igas-Qmass+qExchange)/(8940[kg/m^3]*e0)");
    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("solid").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("optics").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("opticsUp").prop("ShapeProperty").set("order", 1);

    model.sol("sol1").clearSolutionData();

    model.component("c").mesh("mesh").run();

    model.sol().remove("sol1");
    model.sol().create("sol1");
    model.sol("sol1").study("std");
    model.sol("sol1").create("st1", "StudyStep");
    model.sol("sol1").feature("st1").set("study", "std");
    model.sol("sol1").feature("st1").set("studystep", "time");
    model.sol("sol1").create("v1", "Variables");
    model.sol("sol1").feature("v1").set("control", "time");
    model.sol("sol1").create("t1", "Time");
    model.sol("sol1").feature("t1").set("tlist", "range(0,5[ns],300[ns]) range(320[ns],20[ns],tEnd)");
    model.sol("sol1").feature("t1").set("plot", "off");
    model.sol("sol1").feature("t1").set("plotgroup", "Default");
    model.sol("sol1").feature("t1").set("plotfreq", "tout");
    model.sol("sol1").feature("t1").set("probesel", "all");
    model.sol("sol1").feature("t1").set("probes", new String[]{});
    model.sol("sol1").feature("t1").set("probefreq", "tsteps");
    model.sol("sol1").feature("t1").set("atolglobalvaluemethod", "factor");
    model.sol("sol1").feature("t1").set("endtimeinterpolation", true);
    model.sol("sol1").feature("t1").set("control", "time");
    model.sol("sol1").feature("t1").create("seDef", "Segregated");
    model.sol("sol1").feature("t1").create("fc1", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("fc1").set("linsolver", "dDef");
    model.sol("sol1").feature("t1").feature().remove("fcDef");
    model.sol("sol1").feature("t1").feature().remove("seDef");
    model.sol("sol1").attach("std");

    model.study("std").feature("time").set("tlist", "range(0,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])");

    model.sol("sol1").feature("t1").set("tlist", "range(0,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])");
    model.sol("sol1").feature("t1").set("rtol", 0.002);
    model.sol("sol1").feature("t1").set("maxorder", 1);
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "1[ns]");
    model.sol("sol1").feature("t1").set("atolglobalmethod", "scaled");
    model.sol("sol1").feature("t1").set("atolglobalvaluemethod", "factor");
    model.sol("sol1").feature("t1").set("atolglobalfactor", 1.0E-4);
    model.sol("sol1").feature("t1").set("plot", "off");

    model.label("H00_ready.mph");

    model.param().set("netEvap", "1");
    model.param().set("aleOn", "1");
    model.param().set("columnALE", "0");
    model.param().set("nuFactor", "2");
    model.param().set("meshFactor", "1");
    model.param().set("Rdom", "2[mm]");
    model.param().set("Zdom", "3[mm]");
    model.param().set("nuLocal", "1");

    model.component("c").variable("v")
         .set("YgradALE", "sqrt((d(Yraw,r)+sR/Jale*d(Yraw,z))^2+(d(Yraw,z)/Jale)^2+1e-20[1/m^2])");
    model.component("c").variable("v")
         .set("DfrontALE", "nuLocal*h*(speedP+csBound)*exp(-(Yraw/0.02)^2)*(h*YgradALE/(0.02+h*YgradALE))");
    model.component("c").variable("v").set("Dnum", "nuFactor*h*(speedP+csBound)+DfrontALE");

    model.component("c").physics().create("map", "GeneralFormPDE", "g", new String[]{"sMap"});
    model.component("c").physics("map").selection().all();
    model.component("c").physics("map").feature("gfeq1").set("da", "0");

    model.param().set("meshSmoothZ", "100");

    model.component("c").physics("map").feature("gfeq1")
         .set("Ga", new String[]{"-axisWeight*d(sMap,r)", "-meshSmoothZ*axisWeight*d(sMap,z)"});
    model.component("c").physics("map").feature("gfeq1").set("f", "0");
    model.component("c").physics("map").create("surface", "DirichletBoundary", 1);
    model.component("c").physics("map").feature("surface").selection().set(4);
    model.component("c").physics("map").feature("surface").set("r", "depth/1[um]");
    model.component("c").physics("map").create("outer", "DirichletBoundary", 1);
    model.component("c").physics("map").feature("outer").selection().set(2, 5, 6, 7);
    model.component("c").physics("map").feature("outer").set("r", "0");
    model.component("c").physics("map").prop("ShapeProperty").set("order", 2);

    model.component("c").variable("v").set("sALE", "1[um]*sMap");
    model.component("c").variable("v").set("sR", "d(sALE,r)");
    model.component("c").variable("v").set("Jale", "1-d(sALE,z)");
    model.component("c").variable("v").set("wALE", "-d(sALE,t)");
    model.component("c").variable("v").set("JtALE", "d(wALE,z)");
    model.component("c").variable("v").set("zPhysical", "z-sALE");
    model.component("c").variable("v").set("surfaceStretch", "sqrt(1+dtang(depth,r)^2)");
    model.component("c").variable("v").set("surfaceR", "dtang(depth,r)/surfaceStretch");
    model.component("c").variable("v").set("surfaceZ", "1/surfaceStretch");
    model.component("c").variable("v").set("pressureWorkALE", "-pPl*mdot/8940[kg/m^3]");
    model.component("c").variable("v").set("FR0", "u0*qMr-Dnum*(d(qR,r)+sR/Jale*d(qR,z))");
    model.component("c").variable("v").set("FZ0", "u0*qMz-Dnum*(d(qR,z)/Jale)");
    model.component("c").variable("v")
         .set("FR1", "u0*qMr*qMr/max(qR,1e-9)+(pPl-pAmb)/(rho0*u0)-Dnum*(d(qMr,r)+sR/Jale*d(qMr,z))");
    model.component("c").variable("v").set("FZ1", "u0*qMr*qMz/max(qR,1e-9)-Dnum*(d(qMr,z)/Jale)");
    model.component("c").variable("v").set("FR2", "u0*qMr*qMz/max(qR,1e-9)-Dnum*(d(qMz,r)+sR/Jale*d(qMz,z))");
    model.component("c").variable("v")
         .set("FZ2", "u0*qMz*qMz/max(qR,1e-9)+(pPl-pAmb)/(rho0*u0)-Dnum*(d(qMz,z)/Jale)");
    model.component("c").variable("v")
         .set("FR3", "urP*(qE+pPl/(rho0*e0))-Dnum*(d(qE,r)+sR/Jale*d(qE,z))-kGas*(d(Tpl,r)+sR/Jale*d(Tpl,z))/(rho0*e0)");
    model.component("c").variable("v")
         .set("FZ3", "uzP*(qE+pPl/(rho0*e0))-Dnum*(d(qE,z)/Jale)-kGas*(d(Tpl,z)/Jale)/(rho0*e0)");
    model.component("c").variable("v").set("FR4", "urP*qC-Dnum*(d(qC,r)+sR/Jale*d(qC,z))");
    model.component("c").variable("v").set("FZ4", "uzP*qC-Dnum*(d(qC,z)/Jale)");

    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*Jale*FR0", "axisWeight*(sR*FR0+FZ0-wALE*qR)"}, {"axisWeight*Jale*FR1", "axisWeight*(sR*FR1+FZ1-wALE*qMr)"}, {"axisWeight*Jale*FR2", "axisWeight*(sR*FR2+FZ2-wALE*qMz)"}, {"axisWeight*Jale*FR3", "axisWeight*(sR*FR3+FZ3-wALE*qE)"}, {"axisWeight*Jale*FR4", "axisWeight*(sR*FR4+FZ4-wALE*qC)"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"-axisWeight*JtALE*qR", "-axisWeight*JtALE*qMr+axisWeight*Jale*(pPl-pAmb)/(rho0*u0*max(r,1e-10[m]))", "-axisWeight*JtALE*qMz", "-axisWeight*JtALE*qE+axisWeight*Jale*(QlaserP-Qrad)/(rho0*e0)", "-axisWeight*JtALE*qC"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight*Jale");
    model.component("c").physics("flow").feature("abl")
         .set("g", new String[]{"axisWeight*surfaceStretch*mdot/rho0", "axisWeight*surfaceStretch*((pPl-pAmb+mdotEvap*vKn)*surfaceR-mdotReturn*urP)/(rho0*u0)", "axisWeight*surfaceStretch*((pPl-pAmb+mdotEvap*vKn)*surfaceZ-mdotReturn*uzP)/(rho0*u0)", "axisWeight*surfaceStretch*(Qmass-qExchange+pressureWorkALE)/(rho0*e0)", "axisWeight*surfaceStretch*mdot/rho0"});

    model.component("c").variable("v").set("solidFR", "-kCu*(d(Ts,r)+sR/Jale*d(Ts,z))/(8940[kg/m^3]*e0)");
    model.component("c").variable("v").set("solidFZ", "-kCu*(d(Ts,z)/Jale)/(8940[kg/m^3]*e0)");

    model.component("c").physics("solid").feature("gfeq1").set("da", "axisWeight*Jale");
    model.component("c").physics("solid").feature("gfeq1")
         .set("Ga", new String[]{"axisWeight*Jale*solidFR", "axisWeight*(sR*solidFR+solidFZ-wALE*hs)"});
    model.component("c").physics("solid").feature("gfeq1").set("f", "-axisWeight*JtALE*hs");
    model.component("c").physics("solid").feature("heat")
         .set("g", "axisWeight*(Igas-surfaceStretch*(Qmass-qExchange+pressureWorkALE))/(8940[kg/m^3]*e0)");
    model.component("c").physics("rec").feature("dode1").set("f", "mdot*surfaceStretch/8940[kg/m^3]");
    model.component("c").physics("optics").feature("gfeq1")
         .set("Ga", new String[]{"0", "axisWeight*(-opt-0.02*h*d(opt,z)/Jale)"});
    model.component("c").physics("optics").feature("gfeq1").set("f", "-axisWeight*Jale*alphaIB*opt");
    model.component("c").physics("opticsUp").feature("gfeq1")
         .set("Ga", new String[]{"0", "axisWeight*(optUp-0.02*h*d(optUp,z)/Jale)"});
    model.component("c").physics("opticsUp").feature("gfeq1").set("f", "-axisWeight*Jale*alphaIB*optUp");

    model.sol("sol1").clearSolutionData();

    return model;
  }

  public static Model run2(Model model) {

    model.component("c").geom("g").run();

    model.component("c").mesh("mesh").run();

    model.sol().remove("sol1");
    model.sol().create("sol1");
    model.sol("sol1").study("std");
    model.sol("sol1").create("st1", "StudyStep");
    model.sol("sol1").feature("st1").set("study", "std");
    model.sol("sol1").feature("st1").set("studystep", "time");
    model.sol("sol1").create("v1", "Variables");
    model.sol("sol1").feature("v1").set("control", "time");
    model.sol("sol1").create("t1", "Time");
    model.sol("sol1").feature("t1").set("tlist", "range(0,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])");
    model.sol("sol1").feature("t1").set("plot", "off");
    model.sol("sol1").feature("t1").set("plotgroup", "Default");
    model.sol("sol1").feature("t1").set("plotfreq", "tout");
    model.sol("sol1").feature("t1").set("probesel", "all");
    model.sol("sol1").feature("t1").set("probes", new String[]{});
    model.sol("sol1").feature("t1").set("probefreq", "tsteps");
    model.sol("sol1").feature("t1").set("atolglobalvaluemethod", "factor");
    model.sol("sol1").feature("t1").set("endtimeinterpolation", true);
    model.sol("sol1").feature("t1").set("control", "time");
    model.sol("sol1").feature("t1").create("seDef", "Segregated");
    model.sol("sol1").feature("t1").create("fc1", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("fc1").set("linsolver", "dDef");
    model.sol("sol1").feature("t1").feature().remove("fcDef");
    model.sol("sol1").feature("t1").feature().remove("seDef");
    model.sol("sol1").attach("std");
    model.sol("sol1").feature("t1").set("tlist", "range(0,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])");
    model.sol("sol1").feature("t1").set("rtol", 0.002);
    model.sol("sol1").feature("t1").set("maxorder", 1);
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.25[ns]");
    model.sol("sol1").feature("t1").set("atolglobalmethod", "scaled");
    model.sol("sol1").feature("t1").set("atolglobalvaluemethod", "factor");
    model.sol("sol1").feature("t1").set("atolglobalfactor", 1.0E-4);
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qC").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qC").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_hs").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_hs").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_opt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_opt").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optUp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optUp").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_sMap").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_sMap").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_depth").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_depth").set("scaleval", "1e-6");
    model.sol("sol1").feature("t1").feature("fc1").set("jtech", "onevery");
    model.sol("sol1").feature("t1").feature("fc1").set("maxiter", 15);
    model.sol("sol1").feature("t1").feature("fc1").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("fc1").set("maxiter", 25);

    model.label("S4 LTE conservative mapped ALE + net evaporation");
    model.label("VT_ready.mph");

    model.sol("sol1").feature("t1").feature("fc1").set("jtech", "minimal");
    model.sol("sol1").feature("t1").feature("fc1").set("dtech", "const");
    model.sol("sol1").feature("t1").feature("fc1").set("damp", 1);
    model.sol("sol1").feature("t1").feature("fc1").set("maxiter", 25);

    model.label("VTE_ready.mph");

    model.modelPath("D:\\junlin\\PlasmaPlume\\COMSOL_CuPlasma_S4_server_handoff_20261105\\project\\work");

    model.func("lte").set("sourcetype", "user");
    model.func("lte").set("source", "file");
    model.func("lte")
         .set("filename", "D:\\junlin\\PlasmaPlume\\COMSOL_CuPlasma_S4_server_handoff_20261105\\project\\data\\lte_grid.txt");
    model.func("lte")
         .set("funcnametable", new String[][]{{"logTpl", "1"}, {"logPrho", "2"}, {"logNe", "3"}, {"logNiZ2", "4"}, {"xCuLTE", "5"}});
    model.func("lte").importData();

    model.label("VTE_ready.mph");

    model.sol("sol1").clearSolutionData();

    model.param().set("meshFactor", "1");

    model.sol("sol1").feature("v1").set("control", "user");
    model.sol("sol1").feature("v1").set("initmethod", "init");

    model.study("std").feature("time").set("tlist", "range(0,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])");

    model.sol("sol1").feature("t1").set("tlist", "range(0,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.25[ns]");
    model.sol("sol1").feature("t1").set("rtol", "0.002");
    model.sol("sol1").feature("t1").set("maxorder", 1);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("t1").feature("fc1").set("jtech", "minimal");
    model.sol("sol1").feature("t1").feature("fc1").set("dtech", "const");
    model.sol("sol1").feature("t1").feature("fc1").set("damp", 1);
    model.sol("sol1").feature("t1").feature("fc1").set("maxiter", 25);

    model.result().dataset("dset1").set("solution", "sol1");

    model.label("T64_ready.mph | COMSOL 6.4 fresh initial state");
    model.label("T64_ready.mph");
    model.label("T64.mph");

    model.sol("sol1").getPVals();
    model.sol("sol1").copySolution("history");
    model.sol("sol1").clearSolutionData();

    model.result().dataset("dset1").set("solution", "sol1");

    model.param().set("resumeTime", "3.04E-7[s]");

    model.study("std").feature("time")
         .set("tlist", "range(resumeTime,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])");

    model.sol("sol1").feature("t1").set("tlist", "range(resumeTime,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])");
    model.sol("sol1").feature("v1").set("control", "user");
    model.sol("sol1").feature("v1").set("initmethod", "sol");
    model.sol("sol1").feature("v1").set("initsol", "history");
    model.sol("sol1").feature("v1").set("solnum", "last");
    model.sol("sol1").feature("t1").feature("fc1").set("jtech", "onevery");
    model.sol("sol1").feature("t1").feature("fc1").set("maxiter", 15);
    model.sol("sol1").feature("t1").feature("fc1").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("fc1").set("maxiter", 25);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");

    model.label("T64A_ready.mph");
    model.label("T64A.mph");

    model.sol("history").getPVals();
    model.sol("sol1").getPVals();
    model.sol("history").getU(153);
    model.sol("sol1").getU(1);
    model.sol().create("merged");
    model.sol("merged").study("std");
    model.sol("merged").create("cs", "CombineSolution");
    model.sol("merged").feature("cs").set("soloper", "concatenation");
    model.sol("merged").feature("cs").set("cssol1", "history");
    model.sol("merged").feature("cs").set("cssol2", "sol1");
    model.sol("merged").runAll();
    model.sol("merged").getPVals();

    model.result().dataset("dset1").set("solution", "merged");

    model.label("T64A.mph");

    model.sol("merged").getPVals();
    model.sol().remove("history");
    model.sol("merged").copySolution("history");
    model.sol().remove("merged");
    model.sol("sol1").clearSolutionData();

    model.result().dataset().create("dset1", "Solution");
    model.result().dataset("dset1").set("solution", "sol1");

    model.param().set("resumeTime", "8.700000000000001E-7[s]");

    model.study("std").feature("time")
         .set("tlist", "range(resumeTime,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])");

    model.sol("sol1").feature("t1").set("tlist", "range(resumeTime,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])");
    model.sol("sol1").feature("v1").set("control", "user");
    model.sol("sol1").feature("v1").set("initmethod", "sol");
    model.sol("sol1").feature("v1").set("initsol", "history");
    model.sol("sol1").feature("v1").set("solnum", "last");
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");

    model.label("T64AC1_ready.mph");
    model.label("T64AC1.mph");

    model.sol("history").getPVals();
    model.sol("sol1").getPVals();
    model.sol("history").getU(437);
    model.sol("sol1").getU(1);
    model.sol().create("merged");
    model.sol("merged").study("std");
    model.sol("merged").create("cs", "CombineSolution");
    model.sol("merged").feature("cs").set("soloper", "concatenation");
    model.sol("merged").feature("cs").set("cssol1", "history");
    model.sol("merged").feature("cs").set("cssol2", "sol1");
    model.sol("merged").runAll();
    model.sol("merged").getPVals();

    model.result().dataset("dset1").set("solution", "merged");

    model.label("T64AC1.mph");

    model.sol("merged").getPVals();
    model.sol().remove("history");
    model.sol("merged").copySolution("history");
    model.sol().remove("merged");
    model.sol("sol1").clearSolutionData();

    model.result().dataset().create("dset1", "Solution");
    model.result().dataset("dset1").set("solution", "sol1");

    model.param().set("resumeTime", "1.98E-6[s]");

    model.study("std").feature("time").set("tlist", "range(resumeTime,10[ns],2120[ns])");

    model.sol("sol1").feature("t1").set("tlist", "range(resumeTime,10[ns],2120[ns])");
    model.sol("sol1").feature("v1").set("control", "user");
    model.sol("sol1").feature("v1").set("initmethod", "sol");
    model.sol("sol1").feature("v1").set("initsol", "history");
    model.sol("sol1").feature("v1").set("solnum", "last");
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");

    return model;
  }

  public static void main(String[] args) {
    Model model = run();
    run2(model);
  }

}
