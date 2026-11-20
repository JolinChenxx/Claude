/*
 * example_oxygen.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

/** Model exported on Nov 7 2026, 17:48 by COMSOL 6.4.0.429. */
public class example_oxygen {

  public static Model run() {
    Model model = ModelUtil.create("Model");

    model
         .modelPath("C:\\Program Files\\COMSOL\\COMSOL64\\Multiphysics\\applications\\Plasma_Module\\Global_Modeling");

    model.component().create("comp1", true);

    model.component("comp1").geom().create("geom1", 2);
    model.component("comp1").geom("geom1").axisymmetric(true);

    model.component("comp1").mesh().create("mesh1");

    model.component("comp1").physics().create("plas", "ColdPlasma", "geom1");

    model.study().create("std1");
    model.study("std1").create("time", "Transient");

//    To import content from file, use:
//    model.param().loadFile("FILENAME");
    model.param().set("R0", "8.0[cm]", "Radius");
    model.param().set("L0", "7.5[cm]", "Length");
    model.param().set("p0", "8[mTorr]", "Gas pressure");
    model.param().set("Tgas", "300[K]", "Gas temperature");
    model.param().set("xO2", "0.01", "O2 mole fraction");
    model.param().set("Qfeed", "50", "Feed mass flow in SCCM");
    model.param().set("pw", "120[W]", "Power absorbed");
    model.param().set("lambda_diff", "((pi/L0)^2+(2.405/R0)^2)^-0.5", "Diffusion length");
    model.param().set("hc", "0.4", "Correction factor");
    model.param().set("recO", "0.2", "O recombination factor");
    model.param().set("recF", "0.1", "F recombination factor");
    model.param().set("Vp", "15[V]", "Potential drop");

    model.component("comp1").geom("geom1").lengthUnit("cm");
    model.component("comp1").geom("geom1").create("r1", "Rectangle");
    model.component("comp1").geom("geom1").feature("r1").set("size", new String[]{"R0", "L0"});
    model.component("comp1").geom("geom1").run();

    model.component("comp1").physics("plas").prop("DiffusionModel").set("DiffusionModel", "GlobalModel");
    model.component("comp1").physics("plas").prop("ElectronProperties").set("ReducedProps", true);
    model.component("comp1").physics("plas").prop("Reactor").set("ReactorType", "ConstantPressure");
    model.component("comp1").physics("plas").feature("pes1").set("Tgm", "Tgas");
    model.component("comp1").physics("plas").feature("pes1").set("pAgm", "p0");
    model.component("comp1").physics("plas").feature("pes1").set("sccmmfr", "Qfeed");
    model.component("comp1").physics("plas").feature("pes1").set("Pgm", "pw");
    model.component("comp1").physics("plas").feature("pes1").set("EpsilonE", "2*plas.Te");
    model.component("comp1").physics("plas").feature("pes1").set("EpsilonI", "Vp");
    model.component("comp1").physics("plas").create("pci1", "PlasmaChemistryImport", -1);
    model.component("comp1").physics("plas").feature("pci1").set("Filepath", "CF4_O2_plasma_chemistry.txt");
    model.component("comp1").physics("plas").feature("pci1").runCommand("importData");
    model.component("comp1").physics("plas").feature("CF4").set("FromMassConstraint", true);
    model.component("comp1").physics("plas").feature("CF4").set("xfeed", "1-xO2");
    model.component("comp1").physics("plas").feature("O2").set("xfeed", "xO2");
    model.component("comp1").physics("plas").feature("srg1").selection().all();

    model.study("std1").setGenPlots(false);
    model.study("std1").createAutoSequences("all");

    model.sol("sol1").runAll();

    model.study("std1").label("Base Case");
    model.study().create("std2");
    model.study("std2").create("stat", "Stationary");
    model.study("std2").feature("stat").set("useinitsol", true);
    model.study("std2").feature("stat").set("initmethod", "sol");
    model.study("std2").feature("stat").set("initstudy", "std1");
    model.study("std2").feature("stat").set("solnum", "last");
    model.study("std2").feature("stat").set("useparam", true);
    model.study("std2").feature("stat").set("sweeptype", "filled");
    model.study("std2").feature("stat").setIndex("pname", "R0", 0);
    model.study("std2").feature("stat").setIndex("plistarr", "", 0);
    model.study("std2").feature("stat").setIndex("punit", "m", 0);
    model.study("std2").feature("stat").setIndex("pname", "R0", 0);
    model.study("std2").feature("stat").setIndex("plistarr", "", 0);
    model.study("std2").feature("stat").setIndex("punit", "m", 0);
    model.study("std2").feature("stat").setIndex("pname", "p0", 0);
    model.study("std2").feature("stat").setIndex("plistarr", "8 15 25", 0);
    model.study("std2").feature("stat").setIndex("punit", "mTorr", 0);
    model.study("std2").feature("stat").setIndex("pname", "R0", 1);
    model.study("std2").feature("stat").setIndex("plistarr", "", 1);
    model.study("std2").feature("stat").setIndex("punit", "m", 1);
    model.study("std2").feature("stat").setIndex("pname", "R0", 1);
    model.study("std2").feature("stat").setIndex("plistarr", "", 1);
    model.study("std2").feature("stat").setIndex("punit", "m", 1);
    model.study("std2").feature("stat").setIndex("pname", "xO2", 1);
    model.study("std2").feature("stat").setIndex("plistarr", "range(0.01,0.1,0.95)", 1);
    model.study("std2").feature("stat").set("preusesol", "yes");
    model.study("std2").showAutoSequences("all");

    model.sol("sol2").feature("s1").feature("fc1").set("initstep", 1);

    model.study("std2").label("xO2 and Pressure Sweep");
    model.study("std2").setGenPlots(false);
    model.study("std2").createAutoSequences("all");

    model.sol("sol2").runAll();

    model.result().create("pg1", "PlotGroup1D");
    model.result("pg1").run();
    model.result("pg1").label("Electron Density");
    model.result("pg1").set("data", "dset2");
    model.result("pg1").set("titletype", "none");
    model.result("pg1").create("glob1", "Global");
    model.result("pg1").feature("glob1").set("markerpos", "datapoints");
    model.result("pg1").feature("glob1").set("linewidth", "preference");
    model.result("pg1").feature("glob1").setIndex("expr", "plas.ne", 0);
    model.result("pg1").feature("glob1").set("data", "dset2");
    model.result("pg1").feature("glob1").setIndex("looplevelinput", "manual", 1);
    model.result("pg1").feature("glob1").setIndex("looplevel", new int[]{1}, 1);
    model.result("pg1").feature("glob1").set("autodescr", false);
    model.result("pg1").run();
    model.result("pg1").feature().duplicate("glob2", "glob1");
    model.result("pg1").run();
    model.result("pg1").feature("glob2").setIndex("looplevel", new int[]{2}, 1);
    model.result("pg1").run();
    model.result("pg1").feature().duplicate("glob3", "glob2");
    model.result("pg1").run();
    model.result("pg1").feature("glob3").setIndex("looplevel", new int[]{3}, 1);
    model.result("pg1").run();
    model.result("pg1").run();
    model.result("pg1").set("axislimits", true);
    model.result("pg1").set("ymin", "1e16");
    model.result("pg1").set("ymax", "3e17");
    model.result("pg1").run();
    model.result("pg1").set("ylog", true);
    model.result().duplicate("pg2", "pg1");
    model.result("pg2").run();
    model.result("pg2").label("Electron Temperature");
    model.result("pg2").run();
    model.result("pg2").feature("glob1").setIndex("expr", "plas.Te", 0);
    model.result("pg2").run();
    model.result("pg2").feature("glob2").setIndex("expr", "plas.Te", 0);
    model.result("pg2").run();
    model.result("pg2").feature("glob3").setIndex("expr", "plas.Te", 0);
    model.result("pg2").run();
    model.result("pg2").run();
    model.result("pg2").set("ylog", false);
    model.result("pg2").set("ymin", 0);
    model.result("pg2").set("ymax", 5);
    model.result("pg2").run();
    model.result().duplicate("pg3", "pg2");
    model.result("pg3").run();
    model.result("pg3").label("F Density");
    model.result("pg3").run();
    model.result("pg3").feature("glob1").setIndex("expr", "plas.n_wF", 0);
    model.result("pg3").run();
    model.result("pg3").feature("glob2").setIndex("expr", "plas.n_wF", 0);
    model.result("pg3").run();
    model.result("pg3").feature("glob3").setIndex("expr", "plas.n_wF", 0);
    model.result("pg3").run();
    model.result("pg3").set("ylabelactive", true);
    model.result("pg3").set("ylabel", "F number density (1/m<sup>3</sup>)");
    model.result("pg3").set("ylog", true);
    model.result("pg3").set("ymin", "1e18");
    model.result("pg3").set("ymax", "1e20");
    model.result("pg3").run();
    model.result().duplicate("pg4", "pg3");
    model.result("pg4").run();
    model.result("pg4").label("O Density");
    model.result("pg4").run();
    model.result("pg4").feature("glob1").setIndex("expr", "plas.n_wO", 0);
    model.result("pg4").run();
    model.result("pg4").feature("glob2").setIndex("expr", "plas.n_wO", 0);
    model.result("pg4").run();
    model.result("pg4").feature("glob3").setIndex("expr", "plas.n_wO", 0);
    model.result("pg4").run();
    model.result("pg4").set("ylabel", "O number density (1/m<sup>3</sup>)");
    model.result("pg4").set("ymax", "1e21");
    model.result("pg4").run();
    model.result("pg1").run();
    model.result("pg2").run();
    model.result("pg3").run();
    model.result("pg4").run();

    model.title("CF4/O2 \u7b49\u79bb\u5b50\u4f53\u53cd\u5e94\u5668\u7684\u5168\u5c40\u6a21\u578b");

    model
         .description("\u672c\u4f8b\u4f7f\u7528\u5168\u5c40\u6a21\u578b\u7814\u7a76\u4f4e\u538b\u6761\u4ef6\u4e0b\u7684 CF4/O2 \u7b49\u79bb\u5b50\u4f53\u53cd\u5e94\u5668\uff0c\u5e76\u5bf9\u538b\u529b\u548c\u6c27\u6c14\u6469\u5c14\u5206\u6570\u8fdb\u884c\u4e86\u53c2\u6570\u5316\u626b\u63cf\u8ba1\u7b97\u3002");

    model.mesh().clearMeshes();

    model.sol("sol1").clearSolutionData();
    model.sol("sol2").clearSolutionData();

    model.label("cf4_o2_global_model.mph");

    return model;
  }

  public static void main(String[] args) {
    run();
  }

}
