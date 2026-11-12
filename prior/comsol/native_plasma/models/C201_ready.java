/*
 * C201_ready.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

/** Model exported on Nov 9 2026, 21:12 by COMSOL 6.4.0.429. */
public class C201_ready {

  public static Model run() {
    Model model = ModelUtil.create("Model");

    model.modelPath("D:/junlin/PlasmaPlume/route1/models");

    model.label("Route 1: Cu melt + compressible gas + native non-LTE Plasma");

    model.param().set("Ein", "400[uJ]");
    model.param().set("tau", "60[ns]");
    model.param().set("tp", "120[ns]");
    model.param().set("w0", "11[um]");
    model.param().set("Acu", "0.1");
    model.param().set("lambdaL", "1064[nm]");
    model.param().set("Rdom", "1[mm]");
    model.param().set("Zdom", "1.5[mm]");
    model.param().set("Dcu", "100[um]");
    model.param().set("Tamb", "293.15[K]");
    model.param().set("pAmb", "101325[Pa]");
    model.param().set("rhoCu", "8960[kg/m^3]");
    model.param().set("Mcu", "0.063546[kg/mol]");
    model.param().set("rho0", "1[kg/m^3]");
    model.param().set("u0", "1000[m/s]");
    model.param().set("e0", "1e6[J/kg]");
    model.param().set("Rair", "R_const/(0.79*0.0280134[kg/mol]+0.21*0.0319988[kg/mol])");
    model.param().set("rhoAmb", "pAmb/(Rair*Tamb)");
    model.param().set("Bcu", "5815069.632191036[J/kg]");
    model.param().set("Tm", "1358[K]");
    model.param().set("Lm", "204000[J/kg]");
    model.param().set("Lv", "5069000[J/kg]");
    model.param().set("Tv", "2836[K]");
    model.param().set("seedNe", "1e9[1/m^3]");
    model.param().set("workCu", "4.65[V]");
    model.param().set("ARich", "1.20173e6[A/(m^2*K^2)]");
    model.param().set("accom", "1");
    model.param().set("nuFactor", "0.3");
    model.param().set("meshFactor", "1");
    model.param().set("meltOn", "1");
    model.param().set("chemOn", "1");
    model.param().set("tEnd", "180[ns]");

    model.component().create("c", true);

    model.component("c").geom().create("g", 2);
    model.component("c").geom("g").axisymmetric(true);
    model.component("c").geom("g").create("cu", "Rectangle");
    model.component("c").geom("g").feature("cu").set("pos", new String[]{"0", "-Dcu"});
    model.component("c").geom("g").feature("cu").set("size", new String[]{"Rdom", "Dcu"});
    model.component("c").geom("g").feature("cu").set("selresult", true);
    model.component("c").geom("g").create("gas", "Rectangle");
    model.component("c").geom("g").feature("gas").set("pos", new String[]{"0", "0"});
    model.component("c").geom("g").feature("gas").set("size", new String[]{"Rdom", "Zdom"});
    model.component("c").geom("g").feature("gas").set("selresult", true);
    model.component("c").geom("g").create("core", "Rectangle");
    model.component("c").geom("g").feature("core").set("pos", new String[]{"0", "0"});
    model.component("c").geom("g").feature("core").set("size", new String[]{"60[um]", "80[um]"});
    model.component("c").geom("g").feature("core").set("selresult", true);
    model.component("c").geom("g").create("mid", "Rectangle");
    model.component("c").geom("g").feature("mid").set("pos", new String[]{"0", "0"});
    model.component("c").geom("g").feature("mid").set("size", new String[]{"300[um]", "400[um]"});
    model.component("c").geom("g").feature("mid").set("selresult", true);
    model.component("c").geom("g").create("meltcore", "Rectangle");
    model.component("c").geom("g").feature("meltcore").set("pos", new String[]{"0", "-15[um]"});
    model.component("c").geom("g").feature("meltcore").set("size", new String[]{"60[um]", "15[um]"});
    model.component("c").geom("g").feature("meltcore").set("selresult", true);
    model.component("c").geom("g").run();

    model.component("c").selection().create("surface", "Box");
    model.component("c").selection("surface").set("entitydim", 1);
    model.component("c").selection("surface").set("xmin", "-1e-10");
    model.component("c").selection("surface").set("xmax", "Rdom+1e-10[m]");
    model.component("c").selection("surface").set("ymin", "-1e-10");
    model.component("c").selection("surface").set("ymax", "1e-10");
    model.component("c").selection("surface").set("condition", "inside");
    model.component("c").selection().create("beam", "Box");
    model.component("c").selection("beam").set("entitydim", 1);
    model.component("c").selection("beam").set("xmin", "-1e-10");
    model.component("c").selection("beam").set("xmax", "60[um]+1e-10[m]");
    model.component("c").selection("beam").set("ymin", "-1e-10");
    model.component("c").selection("beam").set("ymax", "1e-10");
    model.component("c").selection("beam").set("condition", "inside");
    model.component("c").selection().create("axis", "Box");
    model.component("c").selection("axis").set("entitydim", 1);
    model.component("c").selection("axis").set("xmin", "-1e-10");
    model.component("c").selection("axis").set("xmax", "1e-10");
    model.component("c").selection("axis").set("ymin", "-Dcu-1e-10[m]");
    model.component("c").selection("axis").set("ymax", "Zdom+1e-10[m]");
    model.component("c").selection("axis").set("condition", "inside");
    model.component("c").selection().create("bottom", "Box");
    model.component("c").selection("bottom").set("entitydim", 1);
    model.component("c").selection("bottom").set("xmin", "-1e-10");
    model.component("c").selection("bottom").set("xmax", "Rdom+1e-10[m]");
    model.component("c").selection("bottom").set("ymin", "-Dcu-1e-10[m]");
    model.component("c").selection("bottom").set("ymax", "-Dcu+1e-10[m]");
    model.component("c").selection("bottom").set("condition", "inside");
    model.component("c").selection().create("top", "Box");
    model.component("c").selection("top").set("entitydim", 1);
    model.component("c").selection("top").set("xmin", "-1e-10");
    model.component("c").selection("top").set("xmax", "Rdom+1e-10[m]");
    model.component("c").selection("top").set("ymin", "Zdom-1e-10[m]");
    model.component("c").selection("top").set("ymax", "Zdom+1e-10[m]");
    model.component("c").selection("top").set("condition", "inside");
    model.component("c").selection().create("rightgas", "Box");
    model.component("c").selection("rightgas").set("entitydim", 1);
    model.component("c").selection("rightgas").set("xmin", "Rdom-1e-10[m]");
    model.component("c").selection("rightgas").set("xmax", "Rdom+1e-10[m]");
    model.component("c").selection("rightgas").set("ymin", "-1e-10");
    model.component("c").selection("rightgas").set("ymax", "Zdom+1e-10[m]");
    model.component("c").selection("rightgas").set("condition", "inside");
    model.component("c").selection().create("rightcu", "Box");
    model.component("c").selection("rightcu").set("entitydim", 1);
    model.component("c").selection("rightcu").set("xmin", "Rdom-1e-10[m]");
    model.component("c").selection("rightcu").set("xmax", "Rdom+1e-10[m]");
    model.component("c").selection("rightcu").set("ymin", "-Dcu-1e-10[m]");
    model.component("c").selection("rightcu").set("ymax", "1e-10");
    model.component("c").selection("rightcu").set("condition", "inside");
    model.component("c").selection().create("far", "Union");
    model.component("c").selection("far").set("entitydim", 1);
    model.component("c").selection("far").set("input", new String[]{"top", "rightgas"});
    model.component("c").selection().create("fixed", "Union");
    model.component("c").selection("fixed").set("entitydim", 1);
    model.component("c").selection("fixed").set("input", new String[]{"top", "rightgas", "rightcu", "bottom"});

    model.component("c").variable().create("gasv");
    model.component("c").variable().create("solidv");
    model.component("c").variable().create("boundv");
    model.component("c").variable("gasv").selection().named("g_gas_dom");
    model.component("c").variable("solidv").selection().named("g_cu_dom");
    model.component("c").variable("boundv").selection().geom("g", 1);
    model.component("c").variable("boundv").selection().named("surface");

    model.component("c").physics().create("flow", "GeneralFormPDE", "g", new String[]{"qR", "qMr", "qMz", "qE"});
    model.component("c").physics("flow").selection().named("g_gas_dom");
    model.component("c").physics().create("temp", "GeneralFormPDE", "g", new String[]{"Tg"});
    model.component("c").physics("temp").selection().named("g_gas_dom");
    model.component("c").physics().create("plas", "ColdPlasma", "g");
    model.component("c").physics("plas").selection().named("g_gas_dom");

    model.func().create("rates", "Interpolation");
    model.func("rates").set("source", "file");
    model.func("rates").set("filename", "D:/junlin/PlasmaPlume/route1/data/log_rates.txt");
    model.func("rates").set("nargs", 1);
    model.func("rates")
         .set("funcs", new String[][]{{"lr_N2_0", "1"}, 
         {"lr_N2_1", "2"}, 
         {"lr_N2_2", "3"}, 
         {"lr_N2_3", "4"}, 
         {"lr_N2_4", "5"}, 
         {"lr_N2_5", "6"}, 
         {"lr_N2_6", "7"}, 
         {"lr_N2_7", "8"}, 
         {"lr_N2_8", "9"}, 
         {"lr_N2_9", "10"}, 
         {"lr_N2_10", "11"}, 
         {"lr_N2_11", "12"}, 
         {"lr_N2_12", "13"}, 
         {"lr_N2_13", "14"}, 
         {"lr_N2_14", "15"}, 
         {"lr_N2_15", "16"}, 
         {"lr_N2_16", "17"}, 
         {"lr_N2_17", "18"}, 
         {"lr_N2_18", "19"}, 
         {"lr_N2_19", "20"}, 
         {"lr_N2_20", "21"}, 
         {"lr_N2_21", "22"}, 
         {"lr_N2_22", "23"}, 
         {"lr_N2_23", "24"}, 
         {"lr_N2_24", "25"}, 
         {"lr_N2_25", "26"}, 
         {"lr_O2_0", "27"}, 
         {"lr_O2_1", "28"}, 
         {"lr_O2_2", "29"}, 
         {"lr_O2_3", "30"}, 
         {"lr_O2_4", "31"}, 
         {"lr_O2_5", "32"}, 
         {"lr_O2_6", "33"}, 
         {"lr_O2_7", "34"}, 
         {"lr_O2_8", "35"}, 
         {"lr_O2_9", "36"}, 
         {"lr_O2_10", "37"}, 
         {"lr_O2_11", "38"}, 
         {"lr_O2_12", "39"}, 
         {"lr_O2_13", "40"}, 
         {"lr_O2_14", "41"}, 
         {"lr_O2_15", "42"}, 
         {"lr_O2_16", "43"}, 
         {"lr_CuD5_up", "44"}, 
         {"lr_CuD5_down", "45"}, 
         {"lr_CuD3_up", "46"}, 
         {"lr_CuD3_down", "47"}, 
         {"lr_CuP1_up", "48"}, 
         {"lr_CuP1_down", "49"}, 
         {"lr_CuP3_up", "50"}, 
         {"lr_CuP3_down", "51"}, 
         {"lr_lineup3", "52"}, 
         {"lr_linedn3", "53"}, 
         {"lr_lineup4", "54"}, 
         {"lr_linedn4", "55"}, 
         {"lr_lineup5", "56"}, 
         {"lr_linedn5", "57"}, 
         {"lr_lineup6", "58"}, 
         {"lr_linedn6", "59"}, 
         {"lr_lineup7", "60"}, 
         {"lr_linedn7", "61"}, 
         {"lr_lineup8", "62"}, 
         {"lr_linedn8", "63"}, 
         {"lr_Cu_ion", "64"}, 
         {"lr_Cu_3rec", "65"}, 
         {"lr_Cu_rr", "66"}, 
         {"lr_N_ion", "67"}, 
         {"lr_N_3rec", "68"}, 
         {"lr_N_rr", "69"}, 
         {"lr_O_ion", "70"}, 
         {"lr_O_3rec", "71"}, 
         {"lr_O_rr", "72"}, 
         {"lr_CuD5_ion", "73"}, 
         {"lr_CuD5_3rec", "74"}, 
         {"lr_CuD5_rr", "75"}, 
         {"lr_CuD3_ion", "76"}, 
         {"lr_CuD3_3rec", "77"}, 
         {"lr_CuD3_rr", "78"}, 
         {"lr_CuP1_ion", "79"}, 
         {"lr_CuP1_3rec", "80"}, 
         {"lr_CuP1_rr", "81"}, 
         {"lr_CuP3_ion", "82"}, 
         {"lr_CuP3_3rec", "83"}, 
         {"lr_CuP3_rr", "84"}, 
         {"lr_CuH3_ion", "85"}, 
         {"lr_CuH3_3rec", "86"}, 
         {"lr_CuH3_rr", "87"}, 
         {"lr_CuH5_ion", "88"}, 
         {"lr_CuH5_3rec", "89"}, 
         {"lr_CuH5_rr", "90"}});
    model.func("rates").set("interp", "piecewisecubic");
    model.func("rates").set("extrap", "const");
    model.func("rates").importData();

    model.component("c").physics("plas").create("N2_0", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_0").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_0").set("kf", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_0").set("type", "Elastic");
    model.component("c").physics("plas").feature("N2_0").set("mratio", "1.95e-05");
    model.component("c").physics("plas").create("N2_1", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_1").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_1").set("kf", "10^lr_N2_1(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_1").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_1").set("de", "0.02");
    model.component("c").physics("plas").create("N2_2", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_2").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_2").set("kf", "10^lr_N2_2(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_2").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_2").set("de", "0.29");
    model.component("c").physics("plas").create("N2_3", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_3").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_3").set("kf", "10^lr_N2_3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_3").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_3").set("de", "0.29");
    model.component("c").physics("plas").create("N2_4", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_4").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_4").set("kf", "10^lr_N2_4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_4").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_4").set("de", "0.59");
    model.component("c").physics("plas").create("N2_5", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_5").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_5").set("kf", "10^lr_N2_5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_5").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_5").set("de", "0.88");
    model.component("c").physics("plas").create("N2_6", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_6").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_6").set("kf", "10^lr_N2_6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_6").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_6").set("de", "1.17");
    model.component("c").physics("plas").create("N2_7", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_7").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_7").set("kf", "10^lr_N2_7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_7").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_7").set("de", "1.47");
    model.component("c").physics("plas").create("N2_8", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_8").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_8").set("kf", "10^lr_N2_8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_8").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_8").set("de", "1.76");
    model.component("c").physics("plas").create("N2_9", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_9").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_9").set("kf", "10^lr_N2_9(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_9").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_9").set("de", "2.06");
    model.component("c").physics("plas").create("N2_10", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_10").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_10")
         .set("kf", "10^lr_N2_10(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_10").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_10").set("de", "2.35");
    model.component("c").physics("plas").create("N2_11", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_11").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_11")
         .set("kf", "10^lr_N2_11(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_11").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_11").set("de", "6.17");
    model.component("c").physics("plas").create("N2_12", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_12").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_12")
         .set("kf", "10^lr_N2_12(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_12").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_12").set("de", "7.0");
    model.component("c").physics("plas").create("N2_13", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_13").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_13")
         .set("kf", "10^lr_N2_13(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_13").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_13").set("de", "7.35");
    model.component("c").physics("plas").create("N2_14", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_14").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_14")
         .set("kf", "10^lr_N2_14(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_14").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_14").set("de", "7.36");
    model.component("c").physics("plas").create("N2_15", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_15").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_15")
         .set("kf", "10^lr_N2_15(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_15").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_15").set("de", "7.8");
    model.component("c").physics("plas").create("N2_16", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_16").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_16")
         .set("kf", "10^lr_N2_16(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_16").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_16").set("de", "8.16");
    model.component("c").physics("plas").create("N2_17", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_17").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_17")
         .set("kf", "10^lr_N2_17(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_17").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_17").set("de", "8.4");
    model.component("c").physics("plas").create("N2_18", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_18").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_18")
         .set("kf", "10^lr_N2_18(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_18").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_18").set("de", "8.55");
    model.component("c").physics("plas").create("N2_19", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_19").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_19")
         .set("kf", "10^lr_N2_19(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_19").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_19").set("de", "8.89");
    model.component("c").physics("plas").create("N2_20", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_20").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_20")
         .set("kf", "10^lr_N2_20(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_20").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_20").set("de", "11.03");
    model.component("c").physics("plas").create("N2_21", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_21").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_21")
         .set("kf", "10^lr_N2_21(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_21").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_21").set("de", "11.87");
    model.component("c").physics("plas").create("N2_22", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_22").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_22")
         .set("kf", "10^lr_N2_22(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_22").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_22").set("de", "12.25");
    model.component("c").physics("plas").create("N2_23", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_23").set("formula", "e+N2=>e+N2");
    model.component("c").physics("plas").feature("N2_23")
         .set("kf", "10^lr_N2_23(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_23").set("type", "Excitation");
    model.component("c").physics("plas").feature("N2_23").set("de", "13.0");
    model.component("c").physics("plas").create("N2_24", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_24").set("formula", "e+N2=>2e+N2+");
    model.component("c").physics("plas").feature("N2_24")
         .set("kf", "10^lr_N2_24(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_24").set("type", "Ionization");
    model.component("c").physics("plas").feature("N2_24").set("de", "15.6");
    model.component("c").physics("plas").create("N2_25", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N2_25").set("formula", "e+N2=>2e+N2+");
    model.component("c").physics("plas").feature("N2_25")
         .set("kf", "10^lr_N2_25(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("N2_25").set("type", "Ionization");
    model.component("c").physics("plas").feature("N2_25").set("de", "18.8");
    model.component("c").physics("plas").create("O2_0", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_0").set("formula", "e+O2=>O+O-");
    model.component("c").physics("plas").feature("O2_0").set("kf", "10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_0").set("type", "Attachment");
    model.component("c").physics("plas").feature("O2_0").set("de", "0");
    model.component("c").physics("plas").create("O2_1", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_1").set("formula", "e+O2=>O+O-");
    model.component("c").physics("plas").feature("O2_1").set("kf", "10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_1").set("type", "Attachment");
    model.component("c").physics("plas").feature("O2_1").set("de", "0");
    model.component("c").physics("plas").create("O2_2", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_2").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_2").set("kf", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_2").set("type", "Elastic");
    model.component("c").physics("plas").feature("O2_2").set("mratio", "1.71e-05");
    model.component("c").physics("plas").create("O2_3", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_3").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_3").set("kf", "10^lr_O2_3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_3").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_3").set("de", "0.02");
    model.component("c").physics("plas").create("O2_4", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_4").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_4").set("kf", "10^lr_O2_4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_4").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_4").set("de", "0.19");
    model.component("c").physics("plas").create("O2_5", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_5").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_5").set("kf", "10^lr_O2_5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_5").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_5").set("de", "0.19");
    model.component("c").physics("plas").create("O2_6", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_6").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_6").set("kf", "10^lr_O2_6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_6").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_6").set("de", "0.38");
    model.component("c").physics("plas").create("O2_7", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_7").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_7").set("kf", "10^lr_O2_7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_7").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_7").set("de", "0.38");
    model.component("c").physics("plas").create("O2_8", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_8").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_8").set("kf", "10^lr_O2_8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_8").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_8").set("de", "0.57");
    model.component("c").physics("plas").create("O2_9", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_9").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_9").set("kf", "10^lr_O2_9(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_9").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_9").set("de", "0.75");
    model.component("c").physics("plas").create("O2_10", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_10").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_10")
         .set("kf", "10^lr_O2_10(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_10").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_10").set("de", "0.977");
    model.component("c").physics("plas").create("O2_11", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_11").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_11")
         .set("kf", "10^lr_O2_11(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_11").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_11").set("de", "1.627");
    model.component("c").physics("plas").create("O2_12", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_12").set("formula", "e+O2=>e+O2");
    model.component("c").physics("plas").feature("O2_12")
         .set("kf", "10^lr_O2_12(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_12").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_12").set("de", "4.5");
    model.component("c").physics("plas").create("O2_13", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_13").set("formula", "e+O2=>e+2O");
    model.component("c").physics("plas").feature("O2_13")
         .set("kf", "10^lr_O2_13(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_13").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_13").set("de", "6.0");
    model.component("c").physics("plas").create("O2_14", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_14").set("formula", "e+O2=>e+2O");
    model.component("c").physics("plas").feature("O2_14")
         .set("kf", "10^lr_O2_14(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_14").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_14").set("de", "8.4");
    model.component("c").physics("plas").create("O2_15", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_15").set("formula", "e+O2=>e+2O");
    model.component("c").physics("plas").feature("O2_15")
         .set("kf", "10^lr_O2_15(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_15").set("type", "Excitation");
    model.component("c").physics("plas").feature("O2_15").set("de", "9.97");
    model.component("c").physics("plas").create("O2_16", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O2_16").set("formula", "e+O2=>2e+O2+");
    model.component("c").physics("plas").feature("O2_16")
         .set("kf", "10^lr_O2_16(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("O2_16").set("type", "Ionization");
    model.component("c").physics("plas").feature("O2_16").set("de", "12.06");
    model.component("c").physics("plas").create("CuD5up", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuD5up").set("formula", "e+Cu=>e+CuD5");
    model.component("c").physics("plas").feature("CuD5up")
         .set("kf", "10^lr_CuD5_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuD5up").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuD5up").set("de", "1.388948");
    model.component("c").physics("plas").create("CuD5down", "ElectronImpactReaction", 2);

    return model;
  }

  public static Model run2(Model model) {
    model.component("c").physics("plas").feature("CuD5down").set("formula", "e+CuD5=>e+Cu");
    model.component("c").physics("plas").feature("CuD5down")
         .set("kf", "10^lr_CuD5_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuD5down").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuD5down").set("de", "-1.388948");
    model.component("c").physics("plas").create("CuD3up", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuD3up").set("formula", "e+Cu=>e+CuD3");
    model.component("c").physics("plas").feature("CuD3up")
         .set("kf", "10^lr_CuD3_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuD3up").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuD3up").set("de", "1.6422256");
    model.component("c").physics("plas").create("CuD3down", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuD3down").set("formula", "e+CuD3=>e+Cu");
    model.component("c").physics("plas").feature("CuD3down")
         .set("kf", "10^lr_CuD3_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuD3down").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuD3down").set("de", "-1.6422256");
    model.component("c").physics("plas").create("CuP1up", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuP1up").set("formula", "e+Cu=>e+CuP1");
    model.component("c").physics("plas").feature("CuP1up")
         .set("kf", "10^lr_CuP1_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuP1up").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuP1up").set("de", "3.7858977");
    model.component("c").physics("plas").create("CuP1down", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuP1down").set("formula", "e+CuP1=>e+Cu");
    model.component("c").physics("plas").feature("CuP1down")
         .set("kf", "10^lr_CuP1_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuP1down").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuP1down").set("de", "-3.7858977");
    model.component("c").physics("plas").create("CuP3up", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuP3up").set("formula", "e+Cu=>e+CuP3");
    model.component("c").physics("plas").feature("CuP3up")
         .set("kf", "10^lr_CuP3_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuP3up").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuP3up").set("de", "3.816692");
    model.component("c").physics("plas").create("CuP3down", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuP3down").set("formula", "e+CuP3=>e+Cu");
    model.component("c").physics("plas").feature("CuP3down")
         .set("kf", "10^lr_CuP3_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuP3down").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuP3down").set("de", "-3.816692");
    model.component("c").physics("plas").create("rad1", "Reaction", 2);
    model.component("c").physics("plas").feature("rad1").set("formula", "CuP3=>Cu");
    model.component("c").physics("plas").feature("rad1").set("kf", "139500000.0[1/s]*beta1");
    model.component("c").physics("plas").create("rad2", "Reaction", 2);
    model.component("c").physics("plas").feature("rad2").set("formula", "CuP1=>Cu");
    model.component("c").physics("plas").feature("rad2").set("kf", "137600000.0[1/s]*beta2");
    model.component("c").physics("plas").create("rad3", "Reaction", 2);
    model.component("c").physics("plas").feature("rad3").set("formula", "CuP3=>CuD5");
    model.component("c").physics("plas").feature("rad3").set("kf", "2000000.0[1/s]*beta3");
    model.component("c").physics("plas").create("lineup3", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("lineup3").set("formula", "e+CuD5=>e+CuP3");
    model.component("c").physics("plas").feature("lineup3")
         .set("kf", "10^lr_lineup3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("lineup3").set("type", "Excitation");
    model.component("c").physics("plas").feature("lineup3").set("de", "2.427744");
    model.component("c").physics("plas").create("linedn3", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("linedn3").set("formula", "e+CuP3=>e+CuD5");
    model.component("c").physics("plas").feature("linedn3")
         .set("kf", "10^lr_linedn3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("linedn3").set("type", "Excitation");
    model.component("c").physics("plas").feature("linedn3").set("de", "-2.427744");
    model.component("c").physics("plas").create("rad4", "Reaction", 2);
    model.component("c").physics("plas").feature("rad4").set("formula", "CuH3=>CuP1");
    model.component("c").physics("plas").feature("rad4").set("kf", "60000000.0[1/s]*beta4");
    model.component("c").physics("plas").create("lineup4", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("lineup4").set("formula", "e+CuP1=>e+CuH3");
    model.component("c").physics("plas").feature("lineup4")
         .set("kf", "10^lr_lineup4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("lineup4").set("type", "Excitation");
    model.component("c").physics("plas").feature("lineup4").set("de", "2.4052773999999997");
    model.component("c").physics("plas").create("linedn4", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("linedn4").set("formula", "e+CuH3=>e+CuP1");
    model.component("c").physics("plas").feature("linedn4")
         .set("kf", "10^lr_linedn4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("linedn4").set("type", "Excitation");
    model.component("c").physics("plas").feature("linedn4").set("de", "-2.4052773999999997");
    model.component("c").physics("plas").create("rad5", "Reaction", 2);
    model.component("c").physics("plas").feature("rad5").set("formula", "CuH5=>CuP3");
    model.component("c").physics("plas").feature("rad5").set("kf", "75000000.0[1/s]*beta5");
    model.component("c").physics("plas").create("lineup5", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("lineup5").set("formula", "e+CuP3=>e+CuH5");
    model.component("c").physics("plas").feature("lineup5")
         .set("kf", "10^lr_lineup5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("lineup5").set("type", "Excitation");
    model.component("c").physics("plas").feature("lineup5").set("de", "2.3753331999999996");
    model.component("c").physics("plas").create("linedn5", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("linedn5").set("formula", "e+CuH5=>e+CuP3");
    model.component("c").physics("plas").feature("linedn5")
         .set("kf", "10^lr_linedn5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("linedn5").set("type", "Excitation");
    model.component("c").physics("plas").feature("linedn5").set("de", "-2.3753331999999996");
    model.component("c").physics("plas").create("rad6", "Reaction", 2);
    model.component("c").physics("plas").feature("rad6").set("formula", "CuH3=>CuP3");
    model.component("c").physics("plas").feature("rad6").set("kf", "15000000.0[1/s]*beta6");
    model.component("c").physics("plas").create("lineup6", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("lineup6").set("formula", "e+CuP3=>e+CuH3");
    model.component("c").physics("plas").feature("lineup6")
         .set("kf", "10^lr_lineup6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("lineup6").set("type", "Excitation");
    model.component("c").physics("plas").feature("lineup6").set("de", "2.3744830999999995");
    model.component("c").physics("plas").create("linedn6", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("linedn6").set("formula", "e+CuH3=>e+CuP3");
    model.component("c").physics("plas").feature("linedn6")
         .set("kf", "10^lr_linedn6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("linedn6").set("type", "Excitation");
    model.component("c").physics("plas").feature("linedn6").set("de", "-2.3744830999999995");
    model.component("c").physics("plas").create("rad7", "Reaction", 2);
    model.component("c").physics("plas").feature("rad7").set("formula", "CuP3=>CuD3");
    model.component("c").physics("plas").feature("rad7").set("kf", "240000.0[1/s]*beta7");
    model.component("c").physics("plas").create("lineup7", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("lineup7").set("formula", "e+CuD3=>e+CuP3");
    model.component("c").physics("plas").feature("lineup7")
         .set("kf", "10^lr_lineup7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("lineup7").set("type", "Excitation");
    model.component("c").physics("plas").feature("lineup7").set("de", "2.1744664");
    model.component("c").physics("plas").create("linedn7", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("linedn7").set("formula", "e+CuP3=>e+CuD3");
    model.component("c").physics("plas").feature("linedn7")
         .set("kf", "10^lr_linedn7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("linedn7").set("type", "Excitation");
    model.component("c").physics("plas").feature("linedn7").set("de", "-2.1744664");
    model.component("c").physics("plas").create("rad8", "Reaction", 2);
    model.component("c").physics("plas").feature("rad8").set("formula", "CuP1=>CuD3");
    model.component("c").physics("plas").feature("rad8").set("kf", "1650000.0[1/s]*beta8");
    model.component("c").physics("plas").create("lineup8", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("lineup8").set("formula", "e+CuD3=>e+CuP1");
    model.component("c").physics("plas").feature("lineup8")
         .set("kf", "10^lr_lineup8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("lineup8").set("type", "Excitation");
    model.component("c").physics("plas").feature("lineup8").set("de", "2.1436721");
    model.component("c").physics("plas").create("linedn8", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("linedn8").set("formula", "e+CuP1=>e+CuD3");
    model.component("c").physics("plas").feature("linedn8")
         .set("kf", "10^lr_linedn8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("linedn8").set("type", "Excitation");
    model.component("c").physics("plas").feature("linedn8").set("de", "-2.1436721");
    model.component("c").physics("plas").create("Cuion", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("Cuion").set("formula", "e+Cu=>2e+Cu+");
    model.component("c").physics("plas").feature("Cuion")
         .set("kf", "10^lr_Cu_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("Cuion").set("type", "Ionization");
    model.component("c").physics("plas").feature("Cuion").set("de", "7.72638");
    model.component("c").physics("plas").create("Cu3rec", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("Cu3rec").set("formula", "2e+Cu+=>e+Cu");
    model.component("c").physics("plas").feature("Cu3rec")
         .set("kf", "10^lr_Cu_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("c").physics("plas").feature("Cu3rec").set("type", "Excitation");
    model.component("c").physics("plas").feature("Cu3rec").set("de", "-7.72638");
    model.component("c").physics("plas").create("Curr", "Reaction", 2);
    model.component("c").physics("plas").feature("Curr").set("formula", "e+Cu+=>Cu");
    model.component("c").physics("plas").feature("Curr")
         .set("kf", "10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("Nion", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("Nion").set("formula", "e+N=>2e+N+");
    model.component("c").physics("plas").feature("Nion")
         .set("kf", "10^lr_N_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("Nion").set("type", "Ionization");
    model.component("c").physics("plas").feature("Nion").set("de", "14.534");
    model.component("c").physics("plas").create("N3rec", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("N3rec").set("formula", "2e+N+=>e+N");
    model.component("c").physics("plas").feature("N3rec")
         .set("kf", "10^lr_N_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("c").physics("plas").feature("N3rec").set("type", "Excitation");
    model.component("c").physics("plas").feature("N3rec").set("de", "-14.534");
    model.component("c").physics("plas").create("Nrr", "Reaction", 2);
    model.component("c").physics("plas").feature("Nrr").set("formula", "e+N+=>N");
    model.component("c").physics("plas").feature("Nrr").set("kf", "10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("Oion", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("Oion").set("formula", "e+O=>2e+O+");
    model.component("c").physics("plas").feature("Oion")
         .set("kf", "10^lr_O_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("Oion").set("type", "Ionization");
    model.component("c").physics("plas").feature("Oion").set("de", "13.618");
    model.component("c").physics("plas").create("O3rec", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("O3rec").set("formula", "2e+O+=>e+O");
    model.component("c").physics("plas").feature("O3rec")
         .set("kf", "10^lr_O_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("c").physics("plas").feature("O3rec").set("type", "Excitation");
    model.component("c").physics("plas").feature("O3rec").set("de", "-13.618");
    model.component("c").physics("plas").create("Orr", "Reaction", 2);
    model.component("c").physics("plas").feature("Orr").set("formula", "e+O+=>O");
    model.component("c").physics("plas").feature("Orr").set("kf", "10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("CuD5ion", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuD5ion").set("formula", "e+CuD5=>2e+Cu+");
    model.component("c").physics("plas").feature("CuD5ion")
         .set("kf", "10^lr_CuD5_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuD5ion").set("type", "Ionization");
    model.component("c").physics("plas").feature("CuD5ion").set("de", "6.337432");
    model.component("c").physics("plas").create("CuD53rec", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuD53rec").set("formula", "2e+Cu+=>e+CuD5");
    model.component("c").physics("plas").feature("CuD53rec")
         .set("kf", "10^lr_CuD5_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("c").physics("plas").feature("CuD53rec").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuD53rec").set("de", "-6.337432");
    model.component("c").physics("plas").create("CuD5rr", "Reaction", 2);
    model.component("c").physics("plas").feature("CuD5rr").set("formula", "e+Cu+=>CuD5");
    model.component("c").physics("plas").feature("CuD5rr")
         .set("kf", "10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("CuD3ion", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuD3ion").set("formula", "e+CuD3=>2e+Cu+");
    model.component("c").physics("plas").feature("CuD3ion")
         .set("kf", "10^lr_CuD3_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuD3ion").set("type", "Ionization");
    model.component("c").physics("plas").feature("CuD3ion").set("de", "6.0841544");
    model.component("c").physics("plas").create("CuD33rec", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuD33rec").set("formula", "2e+Cu+=>e+CuD3");
    model.component("c").physics("plas").feature("CuD33rec")
         .set("kf", "10^lr_CuD3_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("c").physics("plas").feature("CuD33rec").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuD33rec").set("de", "-6.0841544");
    model.component("c").physics("plas").create("CuD3rr", "Reaction", 2);
    model.component("c").physics("plas").feature("CuD3rr").set("formula", "e+Cu+=>CuD3");
    model.component("c").physics("plas").feature("CuD3rr")
         .set("kf", "10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("CuP1ion", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuP1ion").set("formula", "e+CuP1=>2e+Cu+");
    model.component("c").physics("plas").feature("CuP1ion")
         .set("kf", "10^lr_CuP1_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuP1ion").set("type", "Ionization");
    model.component("c").physics("plas").feature("CuP1ion").set("de", "3.9404822999999998");
    model.component("c").physics("plas").create("CuP13rec", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuP13rec").set("formula", "2e+Cu+=>e+CuP1");
    model.component("c").physics("plas").feature("CuP13rec")
         .set("kf", "10^lr_CuP1_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("c").physics("plas").feature("CuP13rec").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuP13rec").set("de", "-3.9404822999999998");
    model.component("c").physics("plas").create("CuP1rr", "Reaction", 2);
    model.component("c").physics("plas").feature("CuP1rr").set("formula", "e+Cu+=>CuP1");
    model.component("c").physics("plas").feature("CuP1rr")
         .set("kf", "10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("CuP3ion", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuP3ion").set("formula", "e+CuP3=>2e+Cu+");
    model.component("c").physics("plas").feature("CuP3ion")
         .set("kf", "10^lr_CuP3_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuP3ion").set("type", "Ionization");
    model.component("c").physics("plas").feature("CuP3ion").set("de", "3.9096879999999996");
    model.component("c").physics("plas").create("CuP33rec", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuP33rec").set("formula", "2e+Cu+=>e+CuP3");
    model.component("c").physics("plas").feature("CuP33rec")
         .set("kf", "10^lr_CuP3_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("c").physics("plas").feature("CuP33rec").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuP33rec").set("de", "-3.9096879999999996");
    model.component("c").physics("plas").create("CuP3rr", "Reaction", 2);
    model.component("c").physics("plas").feature("CuP3rr").set("formula", "e+Cu+=>CuP3");
    model.component("c").physics("plas").feature("CuP3rr")
         .set("kf", "10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("CuH3ion", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuH3ion").set("formula", "e+CuH3=>2e+Cu+");
    model.component("c").physics("plas").feature("CuH3ion")
         .set("kf", "10^lr_CuH3_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuH3ion").set("type", "Ionization");
    model.component("c").physics("plas").feature("CuH3ion").set("de", "1.5352049");
    model.component("c").physics("plas").create("CuH33rec", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuH33rec").set("formula", "2e+Cu+=>e+CuH3");
    model.component("c").physics("plas").feature("CuH33rec")
         .set("kf", "10^lr_CuH3_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("c").physics("plas").feature("CuH33rec").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuH33rec").set("de", "-1.5352049");
    model.component("c").physics("plas").create("CuH3rr", "Reaction", 2);
    model.component("c").physics("plas").feature("CuH3rr").set("formula", "e+Cu+=>CuH3");
    model.component("c").physics("plas").feature("CuH3rr")
         .set("kf", "10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("CuH5ion", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuH5ion").set("formula", "e+CuH5=>2e+Cu+");
    model.component("c").physics("plas").feature("CuH5ion")
         .set("kf", "10^lr_CuH5_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").feature("CuH5ion").set("type", "Ionization");
    model.component("c").physics("plas").feature("CuH5ion").set("de", "1.5343548");
    model.component("c").physics("plas").create("CuH53rec", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("CuH53rec").set("formula", "2e+Cu+=>e+CuH5");
    model.component("c").physics("plas").feature("CuH53rec")
         .set("kf", "10^lr_CuH5_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("c").physics("plas").feature("CuH53rec").set("type", "Excitation");
    model.component("c").physics("plas").feature("CuH53rec").set("de", "-1.5343548");
    model.component("c").physics("plas").create("CuH5rr", "Reaction", 2);
    model.component("c").physics("plas").feature("CuH5rr").set("formula", "e+Cu+=>CuH5");
    model.component("c").physics("plas").feature("CuH5rr")
         .set("kf", "10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("recN2", "Reaction", 2);
    model.component("c").physics("plas").feature("recN2").set("formula", "e+N2+=>2N");
    model.component("c").physics("plas").feature("recN2").set("kf", "2.8e-13[m^3/s]*(300[K]/TeK)^0.5*N_A_const");
    model.component("c").physics("plas").create("recO2", "Reaction", 2);
    model.component("c").physics("plas").feature("recO2").set("formula", "e+O2+=>2O");
    model.component("c").physics("plas").feature("recO2").set("kf", "2e-13[m^3/s]*(300[K]/TeK)^0.7*N_A_const");
    model.component("c").physics("plas").create("att3", "Reaction", 2);
    model.component("c").physics("plas").feature("att3").set("formula", "e+O2+O2=>O2-+O2");
    model.component("c").physics("plas").feature("att3")
         .set("kf", "1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg))*N_A_const^2");
    model.component("c").physics("plas").create("detach", "Reaction", 2);
    model.component("c").physics("plas").feature("detach").set("formula", "O-+O=>O2+e");
    model.component("c").physics("plas").feature("detach").set("kf", "3e-16[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("detach2", "Reaction", 2);
    model.component("c").physics("plas").feature("detach2").set("formula", "O2-+O=>O2+O+e");
    model.component("c").physics("plas").feature("detach2").set("kf", "3.3e-16[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("neutral103", "Reaction", 2);
    model.component("c").physics("plas").feature("neutral103").set("formula", "Cu++O-=>Cu+O");
    model.component("c").physics("plas").feature("neutral103").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("neutral104", "Reaction", 2);
    model.component("c").physics("plas").feature("neutral104").set("formula", "Cu++O2-=>Cu+O2");
    model.component("c").physics("plas").feature("neutral104").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("neutral105", "Reaction", 2);
    model.component("c").physics("plas").feature("neutral105").set("formula", "N2++O-=>N2+O");
    model.component("c").physics("plas").feature("neutral105").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("neutral106", "Reaction", 2);
    model.component("c").physics("plas").feature("neutral106").set("formula", "N2++O2-=>N2+O2");
    model.component("c").physics("plas").feature("neutral106").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("neutral107", "Reaction", 2);
    model.component("c").physics("plas").feature("neutral107").set("formula", "O2++O-=>O2+O");
    model.component("c").physics("plas").feature("neutral107").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("neutral108", "Reaction", 2);
    model.component("c").physics("plas").feature("neutral108").set("formula", "O2++O2-=>O2+O2");
    model.component("c").physics("plas").feature("neutral108").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("neutral109", "Reaction", 2);
    model.component("c").physics("plas").feature("neutral109").set("formula", "N++O-=>N+O");
    model.component("c").physics("plas").feature("neutral109").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("neutral110", "Reaction", 2);
    model.component("c").physics("plas").feature("neutral110").set("formula", "N++O2-=>N+O2");
    model.component("c").physics("plas").feature("neutral110").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("neutral111", "Reaction", 2);
    model.component("c").physics("plas").feature("neutral111").set("formula", "O++O-=>O+O");
    model.component("c").physics("plas").feature("neutral111").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("neutral112", "Reaction", 2);
    model.component("c").physics("plas").feature("neutral112").set("formula", "O++O2-=>O+O2");
    model.component("c").physics("plas").feature("neutral112").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("c").physics("plas").create("N2dN2", "Reaction", 2);
    model.component("c").physics("plas").feature("N2dN2").set("formula", "N2+N2=>2N+N2");
    model.component("c").physics("plas").feature("N2dN2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp(-113200[K]/Tg)/N_A_const*N_A_const");
    model.component("c").physics("plas").create("N2rN2", "Reaction", 2);
    model.component("c").physics("plas").feature("N2rN2").set("formula", "2N+N2=>N2+N2");
    model.component("c").physics("plas").feature("N2rN2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp((-113200[K]+9.76[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.162933627609196e-26[kg]*k_B_const*Tg/h_const^2)^1.5*16/1/(Tg/(2*2.88[K]))*(1-exp(-3390[K]/Tg)))*N_A_const^2");
    model.component("c").physics("plas").create("N2dO2", "Reaction", 2);
    model.component("c").physics("plas").feature("N2dO2").set("formula", "N2+O2=>2N+O2");
    model.component("c").physics("plas").feature("N2dO2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp(-113200[K]/Tg)/N_A_const*N_A_const");
    model.component("c").physics("plas").create("N2rO2", "Reaction", 2);
    model.component("c").physics("plas").feature("N2rO2").set("formula", "2N+O2=>N2+O2");
    model.component("c").physics("plas").feature("N2rO2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp((-113200[K]+9.76[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.162933627609196e-26[kg]*k_B_const*Tg/h_const^2)^1.5*16/1/(Tg/(2*2.88[K]))*(1-exp(-3390[K]/Tg)))*N_A_const^2");
    model.component("c").physics("plas").create("N2dCu", "Reaction", 2);
    model.component("c").physics("plas").feature("N2dCu").set("formula", "N2+Cu=>2N+Cu");
    model.component("c").physics("plas").feature("N2dCu")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp(-113200[K]/Tg)/N_A_const*N_A_const");
    model.component("c").physics("plas").create("N2rCu", "Reaction", 2);
    model.component("c").physics("plas").feature("N2rCu").set("formula", "2N+Cu=>N2+Cu");
    model.component("c").physics("plas").feature("N2rCu")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp((-113200[K]+9.76[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.162933627609196e-26[kg]*k_B_const*Tg/h_const^2)^1.5*16/1/(Tg/(2*2.88[K]))*(1-exp(-3390[K]/Tg)))*N_A_const^2");
    model.component("c").physics("plas").create("O2dN2", "Reaction", 2);
    model.component("c").physics("plas").feature("O2dN2").set("formula", "O2+N2=>2O+N2");
    model.component("c").physics("plas").feature("O2dN2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp(-59500[K]/Tg)/N_A_const*N_A_const");
    model.component("c").physics("plas").create("O2rN2", "Reaction", 2);
    model.component("c").physics("plas").feature("O2rN2").set("formula", "2O+N2=>O2+N2");
    model.component("c").physics("plas").feature("O2rN2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp((-59500[K]+5.16[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.3283814375670623e-26[kg]*k_B_const*Tg/h_const^2)^1.5*81/3/(Tg/(2*2.07[K]))*(1-exp(-2270[K]/Tg)))*N_A_const^2");
    model.component("c").physics("plas").create("O2dO2", "Reaction", 2);
    model.component("c").physics("plas").feature("O2dO2").set("formula", "O2+O2=>2O+O2");
    model.component("c").physics("plas").feature("O2dO2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp(-59500[K]/Tg)/N_A_const*N_A_const");
    model.component("c").physics("plas").create("O2rO2", "Reaction", 2);
    model.component("c").physics("plas").feature("O2rO2").set("formula", "2O+O2=>O2+O2");
    model.component("c").physics("plas").feature("O2rO2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp((-59500[K]+5.16[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.3283814375670623e-26[kg]*k_B_const*Tg/h_const^2)^1.5*81/3/(Tg/(2*2.07[K]))*(1-exp(-2270[K]/Tg)))*N_A_const^2");
    model.component("c").physics("plas").create("O2dCu", "Reaction", 2);
    model.component("c").physics("plas").feature("O2dCu").set("formula", "O2+Cu=>2O+Cu");
    model.component("c").physics("plas").feature("O2dCu")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp(-59500[K]/Tg)/N_A_const*N_A_const");
    model.component("c").physics("plas").create("O2rCu", "Reaction", 2);
    model.component("c").physics("plas").feature("O2rCu").set("formula", "2O+Cu=>O2+Cu");
    model.component("c").physics("plas").feature("O2rCu")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp((-59500[K]+5.16[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.3283814375670623e-26[kg]*k_B_const*Tg/h_const^2)^1.5*81/3/(Tg/(2*2.07[K]))*(1-exp(-2270[K]/Tg)))*N_A_const^2");
    model.component("c").physics("plas").feature("N2").set("PresetSpeciesData", "N2");
    model.component("c").physics("plas").feature("N2").set("M", "0.0280134[kg/mol]");
    model.component("c").physics("plas").feature("N2").set("x0", ".79");
    model.component("c").physics("plas").feature("N2").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2").set("PresetSpeciesData", "O2");
    model.component("c").physics("plas").feature("O2").set("M", "0.0319988[kg/mol]");
    model.component("c").physics("plas").feature("O2").set("x0", ".21");
    model.component("c").physics("plas").feature("O2").set("Dfs", "Dgas");

    return model;
  }

  public static Model run3(Model model) {
    model.component("c").physics("plas").feature("N").set("PresetSpeciesData", "N");
    model.component("c").physics("plas").feature("N").set("M", "0.0140067[kg/mol]");
    model.component("c").physics("plas").feature("N").set("x0", "1e-24");
    model.component("c").physics("plas").feature("N").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O").set("PresetSpeciesData", "O");
    model.component("c").physics("plas").feature("O").set("M", "0.0159994[kg/mol]");
    model.component("c").physics("plas").feature("O").set("x0", "1e-24");
    model.component("c").physics("plas").feature("O").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N2_1p").set("PresetSpeciesData", "N2");
    model.component("c").physics("plas").feature("N2_1p").set("M", "0.0280134[kg/mol]");
    model.component("c").physics("plas").feature("N2_1p").set("x0", "1e-24");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("c").physics("plas").feature("N2_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("c").physics("plas").feature("O2_1p").set("PresetSpeciesData", "O2");
    model.component("c").physics("plas").feature("O2_1p").set("M", "0.0319988[kg/mol]");
    model.component("c").physics("plas").feature("O2_1p").set("x0", "1e-24");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("c").physics("plas").feature("O2_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("c").physics("plas").feature("N_1p").set("PresetSpeciesData", "N");
    model.component("c").physics("plas").feature("N_1p").set("M", "0.0140067[kg/mol]");
    model.component("c").physics("plas").feature("N_1p").set("x0", "1e-24");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("c").physics("plas").feature("N_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("c").physics("plas").feature("O_1p").set("PresetSpeciesData", "O");
    model.component("c").physics("plas").feature("O_1p").set("M", "0.0159994[kg/mol]");
    model.component("c").physics("plas").feature("O_1p").set("x0", "1e-24");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("c").physics("plas").feature("O_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("c").physics("plas").feature("O_1m").set("PresetSpeciesData", "O");
    model.component("c").physics("plas").feature("O_1m").set("M", "0.0159994[kg/mol]");
    model.component("c").physics("plas").feature("O_1m").set("x0", "1e-24");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("c").physics("plas").feature("O_1m").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("c").physics("plas").feature("O2_1m").set("PresetSpeciesData", "O2");
    model.component("c").physics("plas").feature("O2_1m").set("M", "0.0319988[kg/mol]");
    model.component("c").physics("plas").feature("O2_1m").set("x0", "1e-24");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("c").physics("plas").feature("O2_1m").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("c").physics("plas").feature("Cu").set("PresetSpeciesData", "UserDefined");
    model.component("c").physics("plas").feature("Cu").set("M", "0.063546[kg/mol]");
    model.component("c").physics("plas").feature("Cu").set("x0", "1e-24");
    model.component("c").physics("plas").feature("Cu").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu").set("sigma", "3[angstrom]");
    model.component("c").physics("plas").feature("Cu").set("epsilonkb", "100[K]");
    model.component("c").physics("plas").feature("CuD5").set("PresetSpeciesData", "UserDefined");
    model.component("c").physics("plas").feature("CuD5").set("M", "0.063546[kg/mol]");
    model.component("c").physics("plas").feature("CuD5").set("x0", "1e-24");
    model.component("c").physics("plas").feature("CuD5").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("CuD5").set("sigma", "3[angstrom]");
    model.component("c").physics("plas").feature("CuD5").set("epsilonkb", "100[K]");
    model.component("c").physics("plas").feature("CuD3").set("PresetSpeciesData", "UserDefined");
    model.component("c").physics("plas").feature("CuD3").set("M", "0.063546[kg/mol]");
    model.component("c").physics("plas").feature("CuD3").set("x0", "1e-24");
    model.component("c").physics("plas").feature("CuD3").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("CuD3").set("sigma", "3[angstrom]");
    model.component("c").physics("plas").feature("CuD3").set("epsilonkb", "100[K]");
    model.component("c").physics("plas").feature("CuP1").set("PresetSpeciesData", "UserDefined");
    model.component("c").physics("plas").feature("CuP1").set("M", "0.063546[kg/mol]");
    model.component("c").physics("plas").feature("CuP1").set("x0", "1e-24");
    model.component("c").physics("plas").feature("CuP1").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("CuP1").set("sigma", "3[angstrom]");
    model.component("c").physics("plas").feature("CuP1").set("epsilonkb", "100[K]");
    model.component("c").physics("plas").feature("CuP3").set("PresetSpeciesData", "UserDefined");
    model.component("c").physics("plas").feature("CuP3").set("M", "0.063546[kg/mol]");
    model.component("c").physics("plas").feature("CuP3").set("x0", "1e-24");
    model.component("c").physics("plas").feature("CuP3").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("CuP3").set("sigma", "3[angstrom]");
    model.component("c").physics("plas").feature("CuP3").set("epsilonkb", "100[K]");
    model.component("c").physics("plas").feature("CuH3").set("PresetSpeciesData", "UserDefined");
    model.component("c").physics("plas").feature("CuH3").set("M", "0.063546[kg/mol]");
    model.component("c").physics("plas").feature("CuH3").set("x0", "1e-24");
    model.component("c").physics("plas").feature("CuH3").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("CuH3").set("sigma", "3[angstrom]");
    model.component("c").physics("plas").feature("CuH3").set("epsilonkb", "100[K]");
    model.component("c").physics("plas").feature("CuH5").set("PresetSpeciesData", "UserDefined");
    model.component("c").physics("plas").feature("CuH5").set("M", "0.063546[kg/mol]");
    model.component("c").physics("plas").feature("CuH5").set("x0", "1e-24");
    model.component("c").physics("plas").feature("CuH5").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("CuH5").set("sigma", "3[angstrom]");
    model.component("c").physics("plas").feature("CuH5").set("epsilonkb", "100[K]");
    model.component("c").physics("plas").feature("Cu_1p").set("PresetSpeciesData", "UserDefined");
    model.component("c").physics("plas").feature("Cu_1p").set("M", "0.063546[kg/mol]");
    model.component("c").physics("plas").feature("Cu_1p").set("x0", "1e-24");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("c").physics("plas").feature("Cu_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("c").physics("plas").feature("Cu_1p").set("sigma", "3[angstrom]");
    model.component("c").physics("plas").feature("Cu_1p").set("epsilonkb", "100[K]");
    model.component("c").physics("plas").feature("N2").set("FromMassConstraint", true);
    model.component("c").physics("plas").feature("N2_1p").set("InitIon", false);
    model.component("c").physics("plas").feature("N2_1p").set("x0", "seedNe*k_B_const*Tamb/pAmb");
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.Nn0", new String[]{"pAmb/(k_B_const*Tamb)"});
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe");
    model.component("c").physics("plas").feature("O2_1p").set("n0", "1e-24*pAmb/(k_B_const*Tamb)");
    model.component("c").physics("plas").feature("N_1p").set("n0", "1e-24*pAmb/(k_B_const*Tamb)");
    model.component("c").physics("plas").feature("O_1p").set("n0", "1e-24*pAmb/(k_B_const*Tamb)");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e-24*pAmb/(k_B_const*Tamb)");
    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e-24*pAmb/(k_B_const*Tamb)");
    model.component("c").physics("plas").feature("Cu_1p").set("n0", "1e-24*pAmb/(k_B_const*Tamb)");
    model.component("c").physics("plas").prop("DiffusionModel").set("DiffusionModel", "FicksLaw");
    model.component("c").physics("plas").prop("TransportSettings").set("Convection", true);
    model.component("c").physics("plas").prop("ElectronProperties")
         .set("MeanElectronEnergyModel", "LocalEnergyApproximationE");
    model.component("c").physics("plas").prop("Stabilization").set("SourceStabilization", false);
    model.component("c").physics("plas").prop("Stabilization").set("ReactionSourceStabilization", false);
    model.component("c").physics("plas").feature("pes1").set("DensityType", "DensityType");
    model.component("c").physics("plas").feature("pes1").set("rho", "rhoP");
    model.component("c").physics("plas").feature("pes1").set("T", "Tg");
    model.component("c").physics("plas").feature("pes1").set("pA", "pHeavy");
    model.component("c").physics("plas").feature("pes1").set("u", new String[]{"urP", "0", "uzP"});
    model.component("c").physics("plas").feature("pes1").set("SpecifyElectronDensityAndEnergy", "SpecifyMueOnly");
    model.component("c").physics("plas").feature("pes1").set("mue", "e_const/(me_const*nuCollision)");
    model.component("c").physics("plas").feature("init1").set("neinit", "seedNe");
    model.component("c").physics("plas").feature("init1").set("ebarinit", "1.5*k_B_const*Tamb/e_const");
    model.component("c").physics("plas").create("bulkconv", "ElectronProductionRate", 2);
    model.component("c").physics("plas").feature("bulkconv").selection().named("g_gas_dom");
    model.component("c").physics("plas").feature("bulkconv")
         .set("Re", "-urP*d(plas.ne,r)-uzP*d(plas.ne,z)-plas.ne*divU");
    model.component("c").physics("plas").create("power", "GeneralPowerDeposition", 2);
    model.component("c").physics("plas").feature("power").selection().named("g_gas_dom");
    model.component("c").physics("plas").feature("power")
         .set("Qgen", "Qlaser-QeRR-QeAttach-urP*d(Ue,r)-uzP*d(Ue,z)-(Ue+pe)*divU-Qei");
    model.component("c").physics("plas").create("wall", "WallDriftDiffusion", 1);
    model.component("c").physics("plas").feature("wall").selection().named("surface");
    model.component("c").physics("plas").feature("wall").set("ThermalEmissionModel", "RichardsonLaw");
    model.component("c").physics("plas").feature("wall").set("lambda_R", "1");
    model.component("c").physics("plas").feature("wall").set("Phi", "workCu/1[V]");
    model.component("c").physics("plas").feature("wall").set("T_s", "T");
    model.component("c").physics("plas").feature("wall").set("th_ebar", "2*k_B_const*T/e_const");
    model.component("c").physics("plas").feature("wall").set("incmig", true);
    model.component("c").physics("plas").create("float", "FloatingPotential", 1);
    model.component("c").physics("plas").feature("float").selection().named("surface");
    model.component("c").physics("plas").feature("float").set("Q0", "qmetal*1[pC]");
    model.component("c").physics().create("charge", "GlobalEquations", "g");
    model.component("c").physics("charge").feature("ge1").set("name", new String[]{"qmetal"});
    model.component("c").physics("charge").feature("ge1")
         .set("equation", new String[]{"qmetalt-intSurf(Iwall)/1[pC]"});
    model.component("c").physics("charge").feature("ge1").set("initialValueU", new String[]{"0"});
    model.component("c").physics("charge").feature("ge1").set("valueType", "real");
    model.component("c").physics("plas").create("ground", "Ground", 1);
    model.component("c").physics("plas").feature("ground").selection().named("top");
    model.component("c").physics("plas").create("out", "Outflow", 1);
    model.component("c").physics("plas").feature("out").selection().named("far");
    model.component("c").physics("plas").feature("out").set("IncludeIons", true);
    model.component("c").physics("plas").create("eout", "ElectronOutlet", 1);
    model.component("c").physics("plas").feature("eout").selection().named("far");
    model.component("c").physics("plas").create("feed", "HeavySpeciesFlux", 1);
    model.component("c").physics("plas").feature("feed").selection().named("surface");
    model.component("c").physics("plas").feature("feed").set("IncludeConvection", true);
    model.component("c").physics().create("melt", "LaminarFlow", "g");
    model.component("c").physics("melt").selection().named("g_cu_dom");
    model.component("c").physics("melt").prop("PhysicalModelProperty").set("Compressibility", "Incompressible");
    model.component("c").physics("melt").field("velocity").field("umelt");
    model.component("c").physics("melt").field("velocity").component(new String[]{"urCu", "uphiCu", "uzCu"});
    model.component("c").physics().create("ht", "HeatTransfer", "g");
    model.component("c").physics("ht").selection().named("g_cu_dom");
    model.component("c").physics("ht").feature("init1").set("Tinit", "Tamb");

    model.component("c").variable("solidv").set("fL", "0.5*(1+(T-Tm)/sqrt((T-Tm)^2+100[K^2]))");
    model.component("c").variable("solidv")
         .set("cpCu", "385[J/(kg*K)]*(1-fL)+572[J/(kg*K)]*fL+Lm*50[K^2]/((T-Tm)^2+100[K^2])^1.5");
    model.component("c").variable("solidv")
         .set("hCond", "385[J/(kg*K)]*(T-Tamb)+187[J/(kg*K)]*0.5*(T-Tamb+sqrt((T-Tm)^2+100[K^2])-sqrt((Tamb-Tm)^2+100[K^2]))+Lm*(fL-0.5*(1+(Tamb-Tm)/sqrt((Tamb-Tm)^2+100[K^2])))");
    model.component("c").variable("solidv").set("kCu", "385[W/(m*K)]*(1-fL)+165[W/(m*K)]*fL");
    model.component("c").variable("solidv").set("darcy", "1e13[kg/(m^3*s)]*(1-fL)^2/(fL^3+1e-3)");

    model.component("c").material().create("copper", "Common");
    model.component("c").material("copper").selection().named("g_cu_dom");
    model.component("c").material("copper").propertyGroup("def").set("density", "rhoCu");
    model.component("c").material("copper").propertyGroup("def").set("dynamicviscosity", "0.005[Pa*s]");
    model.component("c").material("copper").propertyGroup("def").set("heatcapacity", "cpCu");
    model.component("c").material("copper").propertyGroup("def").set("thermalconductivity", "kCu");

    model.component("c").physics("melt").create("drag", "VolumeForce", 2);
    model.component("c").physics("melt").feature("drag").selection().named("g_cu_dom");
    model.component("c").physics("melt").feature("drag").set("F", new String[]{"-darcy*urCu", "0", "-darcy*uzCu"});
    model.component("c").physics("melt").create("surf", "BoundaryStress", 1);
    model.component("c").physics("melt").feature("surf").selection().named("surface");
    model.component("c").physics("melt").feature("surf")
         .set("Fbnd", new String[]{"-(pPl-pAmb+recoil)*ncr", "0", "-(pPl-pAmb+recoil)*ncz"});
    model.component("c").physics("melt").create("capillary", "WeakContribution", 1);
    model.component("c").physics("melt").feature("capillary").selection().named("surface");
    model.component("c").physics("melt").feature("capillary")
         .set("weakExpression", "-sigmaCu*(dtang(test(urCu),r)+dtang(test(uzCu),z)+test(urCu)/max(r,1e-12[m]))");
    model.component("c").physics("ht").create("advection", "HeatSource", 2);
    model.component("c").physics("ht").feature("advection").selection().named("g_cu_dom");
    model.component("c").physics("ht").feature("advection").set("Q0", "-rhoCu*cpCu*(urCu*d(T,r)+uzCu*d(T,z))");
    model.component("c").physics("ht").create("laser", "HeatFluxBoundary", 1);
    model.component("c").physics("ht").feature("laser").selection().named("surface");
    model.component("c").physics("ht").feature("laser").set("q0", "Qsurface-Qmass+mdot*hCond+Qexchange-Qthermion");
    model.component("c").physics("ht").create("base", "TemperatureBoundary", 1);
    model.component("c").physics("ht").feature("base").selection().named("bottom");
    model.component("c").physics("ht").feature("base").set("T0", "Tamb");

    model.component("c").common().create("deform", "DeformingDomain");
    model.component("c").common("deform").selection().all();
    model.component("c").common("deform").set("smoothingType", "yeoh");
    model.component("c").common().create("meshSurf", "PrescribedNormalMeshVelocity");
    model.component("c").common("meshSurf").selection().named("surface");
    model.component("c").common("meshSurf")
         .set("prescribedNormalVelocity", "meltOn*(urCu*nr+uzCu*nz-mdot/rhoCu*(ncr*nr+ncz*nz))");
    model.component("c").common().create("meshFixed", "FixedBoundary");
    model.component("c").common("meshFixed").selection().named("fixed");
    model.component("c").common().create("meshAxis", "Symmetry");
    model.component("c").common("meshAxis").selection().named("axis");

    model.component("c").cpl().create("intSurf", "Integration");
    model.component("c").cpl("intSurf").selection().named("surface");
    model.component("c").cpl().create("intGas", "Integration");
    model.component("c").cpl("intGas").selection().named("g_gas_dom");
    model.component("c").cpl().create("intCu", "Integration");
    model.component("c").cpl("intCu").selection().named("g_cu_dom");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2)");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urCurP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*urCuzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");

    return model;
  }

  public static Model run4(Model model) {
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});
    model.component("c").physics().create("ld", "GeneralFormPDE", "g", new String[]{"optD"});
    model.component("c").physics("ld").selection().named("g_gas_dom");
    model.component("c").physics("ld").feature("gfeq1").set("da", "0");
    model.component("c").physics("ld").feature("gfeq1").set("Ga", new String[]{"0", "-optD-0.02*h*d(optD,z)"});
    model.component("c").physics("ld").feature("gfeq1").set("f", "-alphaLaser*optD");
    model.component("c").physics("ld").create("input", "DirichletBoundary", 1);
    model.component("c").physics("ld").feature("input").selection().named("top");
    model.component("c").physics("ld").feature("input").set("r", "pulse");
    model.component("c").physics().create("lu", "GeneralFormPDE", "g", new String[]{"optU"});
    model.component("c").physics("lu").selection().named("g_gas_dom");
    model.component("c").physics("lu").feature("gfeq1").set("da", "0");
    model.component("c").physics("lu").feature("gfeq1").set("Ga", new String[]{"0", "optU-0.02*h*d(optU,z)"});
    model.component("c").physics("lu").feature("gfeq1").set("f", "-alphaLaser*optU");
    model.component("c").physics("lu").create("input", "DirichletBoundary", 1);
    model.component("c").physics("lu").feature("input").selection().named("surface");
    model.component("c").physics("lu").feature("input").set("r", "(1-Acu)*optD");

    model.component("c").variable("gasv").set("pulse", "exp(-4*log(2)*((t-tp)/tau)^2)");
    model.component("c").variable("gasv").set("Ipeak", "2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");
    model.component("c").variable("gasv").set("Ibeam", "Ipeak*exp(-2*(r/w0)^2)");
    model.component("c").variable("gasv").set("Qlaser", "alphaLaser*Ibeam*(optD+optU)");

    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"rhoP*urP/rho0", "rhoP*uzP/rho0"}, {"(rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0)", "(rhoP*urP*uzP-tauRZ)/(rho0*u0)"}, {"(rhoP*urP*uzP-tauRZ)/(rho0*u0)", "(rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0)"}, {"(urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0)", "(uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0)"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"0", "(pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0)", "plas.scharge*plas.Etz/(rho0*u0)", "(Qlaser-Qrad+Qfield)/(rho0*e0)"});
    model.component("c").physics("flow").feature("init1").set("qR", "rhoAmb/rho0");
    model.component("c").physics("flow").feature("init1").set("qE", "2.5*pAmb/(rho0*e0)");
    model.component("c").physics("flow").create("feed", "FluxBoundary", 1);
    model.component("c").physics("flow").feature("feed").selection().named("surface");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"(mdot+rhoP*vnSurf)/rho0", "((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0)", "((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0)", "(Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0)"});
    model.component("c").physics("flow").create("axisbc", "DirichletBoundary", 1);
    model.component("c").physics("flow").feature("axisbc").selection().named("axis");
    model.component("c").physics("flow").feature("axisbc").set("r", new String[]{"0", "0", "0", "0"});
    model.component("c").physics("flow").feature("axisbc").set("useDirichletCondition", new int[]{0, 1, 0, 0});
    model.component("c").physics("flow").create("farbc", "DirichletBoundary", 1);
    model.component("c").physics("flow").feature("farbc").selection().named("far");
    model.component("c").physics("flow").feature("farbc")
         .set("r", new String[]{"rhoAmb/rho0", "0", "0", "2.5*pAmb/(rho0*e0)"});
    model.component("c").physics("temp").feature("gfeq1").set("da", "0");
    model.component("c").physics("temp").feature("gfeq1").set("Ga", new String[]{"0", "0"});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", "(rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0)");
    model.component("c").physics("temp").feature("init1").set("Tg", "Tamb");

    model.component("c").mesh().create("mesh");
    model.component("c").mesh("mesh").feature("size").set("custom", true);
    model.component("c").mesh("mesh").feature("size").set("hmax", "80[um]*meshFactor");
    model.component("c").mesh("mesh").feature("size").set("hmin", "0.15[um]*meshFactor");
    model.component("c").mesh("mesh").feature("size").set("hgrad", 1.25);
    model.component("c").mesh("mesh").create("sz0", "Size");
    model.component("c").mesh("mesh").feature("sz0").selection().named("g_mid_dom");
    model.component("c").mesh("mesh").feature("sz0").set("custom", true);
    model.component("c").mesh("mesh").feature("sz0").set("hmaxactive", true);
    model.component("c").mesh("mesh").feature("sz0").set("hmax", "8[um]*meshFactor");
    model.component("c").mesh("mesh").create("sz1", "Size");
    model.component("c").mesh("mesh").feature("sz1").selection().named("g_core_dom");
    model.component("c").mesh("mesh").feature("sz1").set("custom", true);
    model.component("c").mesh("mesh").feature("sz1").set("hmaxactive", true);
    model.component("c").mesh("mesh").feature("sz1").set("hmax", "2[um]*meshFactor");
    model.component("c").mesh("mesh").create("sz2", "Size");
    model.component("c").mesh("mesh").feature("sz2").selection().named("g_meltcore_dom");
    model.component("c").mesh("mesh").feature("sz2").set("custom", true);
    model.component("c").mesh("mesh").feature("sz2").set("hmaxactive", true);
    model.component("c").mesh("mesh").feature("sz2").set("hmax", "0.6[um]*meshFactor");
    model.component("c").mesh("mesh").create("edge", "Edge");
    model.component("c").mesh("mesh").feature("edge").selection().named("beam");
    model.component("c").mesh("mesh").feature("edge").create("size", "Size");
    model.component("c").mesh("mesh").feature("edge").feature("size").set("custom", true);
    model.component("c").mesh("mesh").feature("edge").feature("size").set("hmax", "0.5[um]*meshFactor");
    model.component("c").mesh("mesh").feature("edge").feature("size").set("hmin", "0.1[um]*meshFactor");
    model.component("c").mesh("mesh").create("tri", "FreeTri");
    model.component("c").mesh("mesh").run();

    model.study().create("std");
    model.study("std").create("time", "Transient");
    model.study("std").feature("time").set("tlist", "range(0,2[ns],tEnd)");
    model.study("std").feature("time").set("rtol", 0.003);
    model.study("std").setGenPlots(false);
    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");

    model.label("P03_ready.mph");

    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").create("wallConv", "WeakContribution", 1);
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2)");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urCurP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");

    return model;
  }

  public static Model run5(Model model) {
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*urCuzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");

    model.label("P04_ready.mph");

    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2)");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");

    return model;
  }

  public static Model run6(Model model) {
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");

    model.label("P05_ready.mph");

    model.component("c").physics("plas").feature().remove("float");
    model.component("c").physics("plas").create("metal", "MetalContact", 1);
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");

    return model;
  }

  public static Model run7(Model model) {
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");

    model.label("P06_ready.mph");

    model.component("c").variable("gasv").set("axisWeight", "r/Rdom");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("flow").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)", "axisWeight*(rhoP*uzP/rho0)"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("temp").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("temp").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("temp").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(0)"}});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", new String[]{"axisWeight*((rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0))"});
    model.component("c").physics("ld").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("ld").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("ld").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-optD-0.02*h*d(optD,z))"}});
    model.component("c").physics("ld").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optD)"});
    model.component("c").physics("lu").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("lu").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("lu").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(optU-0.02*h*d(optU,z))"}});
    model.component("c").physics("lu").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optU)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"axisWeight*((mdot+rhoP*vnSurf)/rho0)", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("ld").create("exit", "FluxBoundary", 1);
    model.component("c").physics("ld").feature("exit").selection().named("surface");
    model.component("c").physics("ld").feature("exit").set("g", "-axisWeight*optD*ncz");
    model.component("c").physics("ld").feature("init1").set("optD", "pulse");
    model.component("c").physics("lu").create("exit", "FluxBoundary", 1);
    model.component("c").physics("lu").feature("exit").selection().named("top");
    model.component("c").physics("lu").feature("exit").set("g", "-axisWeight*optU");
    model.component("c").physics("lu").feature("init1").set("optU", "(1-Acu)*pulse");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");

    return model;
  }

  public static Model run8(Model model) {
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");
    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("P08_ready.mph");

    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe+2e7[1/m^3]");

    model.component("c").variable("gasv").set("axisWeight", "r/Rdom");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("flow").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)", "axisWeight*(rhoP*uzP/rho0)"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("temp").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("temp").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("temp").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(0)"}});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", new String[]{"axisWeight*((rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0))"});
    model.component("c").physics("ld").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("ld").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("ld").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-optD-0.02*h*d(optD,z))"}});
    model.component("c").physics("ld").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optD)"});
    model.component("c").physics("lu").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("lu").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("lu").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(optU-0.02*h*d(optU,z))"}});
    model.component("c").physics("lu").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optU)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"axisWeight*((mdot+rhoP*vnSurf)/rho0)", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("ld").feature("exit").selection().named("surface");
    model.component("c").physics("ld").feature("exit").set("g", "-axisWeight*optD*ncz");
    model.component("c").physics("ld").feature("init1").set("optD", "pulse");
    model.component("c").physics("lu").feature("exit").selection().named("top");
    model.component("c").physics("lu").feature("exit").set("g", "-axisWeight*optU");
    model.component("c").physics("lu").feature("init1").set("optU", "(1-Acu)*pulse");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");

    return model;
  }

  public static Model run9(Model model) {
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");
    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("P10_ready.mph");

    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe+2e7[1/m^3]");

    model.component("c").variable("gasv").set("axisWeight", "r/Rdom");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("flow").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)", "axisWeight*(rhoP*uzP/rho0)"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("temp").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("temp").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("temp").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(0)"}});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", new String[]{"axisWeight*((rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0))"});
    model.component("c").physics("ld").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("ld").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("ld").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-optD-0.02*h*d(optD,z))"}});
    model.component("c").physics("ld").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optD)"});
    model.component("c").physics("lu").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("lu").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("lu").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(optU-0.02*h*d(optU,z))"}});
    model.component("c").physics("lu").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optU)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"axisWeight*((mdot+rhoP*vnSurf)/rho0)", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.metal.nJt", new String[]{"Iwall-plas.wall.nJe"});

    model.component("c").variable("boundv")
         .set("mueWallDotE", "-e_const/(me_const*nuCollision)*(plas.Etr*ncr+plas.Etz*ncz)");

    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.nemig_bnd", new String[]{"plas.ne*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.enmig_bnd", new String[]{"(5/3)*plas.en*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("ld").feature("exit").selection().named("surface");
    model.component("c").physics("ld").feature("exit").set("g", "-axisWeight*optD*ncz");
    model.component("c").physics("ld").feature("init1").set("optD", "pulse");
    model.component("c").physics("lu").feature("exit").selection().named("top");
    model.component("c").physics("lu").feature("exit").set("g", "-axisWeight*optU");
    model.component("c").physics("lu").feature("init1").set("optU", "(1-Acu)*pulse");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");

    return model;
  }

  public static Model run10(Model model) {
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");
    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("P11_ready.mph");

    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe+2e7[1/m^3]");

    model.component("c").variable("gasv").set("axisWeight", "r/Rdom");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("flow").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)", "axisWeight*(rhoP*uzP/rho0)"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("temp").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("temp").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("temp").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(0)"}});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", new String[]{"axisWeight*((rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0))"});
    model.component("c").physics("ld").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("ld").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("ld").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-optD-0.02*h*d(optD,z))"}});
    model.component("c").physics("ld").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optD)"});
    model.component("c").physics("lu").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("lu").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("lu").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(optU-0.02*h*d(optU,z))"}});
    model.component("c").physics("lu").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optU)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"axisWeight*((mdot+rhoP*vnSurf)/rho0)", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.metal.nJt", new String[]{"Iwall-plas.wall.nJe"});

    model.component("c").variable("boundv")
         .set("mueWallDotE", "-e_const/(me_const*nuCollision)*(plas.Etr*ncr+plas.Etz*ncz)");

    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.nemig_bnd", new String[]{"plas.ne*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.enmig_bnd", new String[]{"(5/3)*plas.en*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("ld").feature("exit").selection().named("surface");
    model.component("c").physics("ld").feature("exit").set("g", "-axisWeight*optD*ncz");
    model.component("c").physics("ld").feature("init1").set("optD", "pulse");
    model.component("c").physics("lu").feature("exit").selection().named("top");
    model.component("c").physics("lu").feature("exit").set("g", "-axisWeight*optU");
    model.component("c").physics("lu").feature("init1").set("optU", "(1-Acu)*pulse");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");

    return model;
  }

  public static Model run11(Model model) {
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});

    model.param().set("meshFactor", "2");

    model.component("c").mesh("mesh").run();

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");
    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("P12_ready.mph");

    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe+2e7[1/m^3]");

    model.component("c").variable("gasv").set("axisWeight", "r/Rdom");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);

    return model;
  }

  public static Model run12(Model model) {
    model.component("c").physics("flow").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)", "axisWeight*(rhoP*uzP/rho0)"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("temp").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("temp").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("temp").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(0)"}});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", new String[]{"axisWeight*((rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0))"});
    model.component("c").physics("ld").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("ld").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("ld").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-optD-0.02*h*d(optD,z))"}});
    model.component("c").physics("ld").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optD)"});
    model.component("c").physics("lu").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("lu").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("lu").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(optU-0.02*h*d(optU,z))"}});
    model.component("c").physics("lu").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optU)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"axisWeight*((mdot+rhoP*vnSurf)/rho0)", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.metal.nJt", new String[]{"Iwall-plas.wall.nJe"});

    model.component("c").variable("boundv")
         .set("mueWallDotE", "-e_const/(me_const*nuCollision)*(plas.Etr*ncr+plas.Etz*ncz)");

    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.nemig_bnd", new String[]{"plas.ne*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.enmig_bnd", new String[]{"(5/3)*plas.en*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("ld").feature("exit").selection().named("surface");
    model.component("c").physics("ld").feature("exit").set("g", "-axisWeight*optD*ncz");
    model.component("c").physics("ld").feature("init1").set("optD", "pulse");
    model.component("c").physics("lu").feature("exit").selection().named("top");
    model.component("c").physics("lu").feature("exit").set("g", "-axisWeight*optU");
    model.component("c").physics("lu").feature("init1").set("optU", "(1-Acu)*pulse");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});
    model.component("c").physics("plas").feature("N").set("x0", "1e-12");
    model.component("c").physics("plas").feature("O").set("x0", "1e-12");
    model.component("c").physics("plas").feature("N2_1p").set("x0", "1e-12");
    model.component("c").physics("plas").feature("O2_1p").set("x0", "1e-12");
    model.component("c").physics("plas").feature("N_1p").set("x0", "1e-12");
    model.component("c").physics("plas").feature("O_1p").set("x0", "1e-12");
    model.component("c").physics("plas").feature("O_1m").set("x0", "1e-12");
    model.component("c").physics("plas").feature("O2_1m").set("x0", "1e-12");
    model.component("c").physics("plas").feature("Cu").set("x0", "1e-12");
    model.component("c").physics("plas").feature("CuD5").set("x0", "1e-12");
    model.component("c").physics("plas").feature("CuD3").set("x0", "1e-12");
    model.component("c").physics("plas").feature("CuP1").set("x0", "1e-12");
    model.component("c").physics("plas").feature("CuP3").set("x0", "1e-12");
    model.component("c").physics("plas").feature("CuH3").set("x0", "1e-12");
    model.component("c").physics("plas").feature("CuH5").set("x0", "1e-12");

    return model;
  }

  public static Model run13(Model model) {
    model.component("c").physics("plas").feature("Cu_1p").set("x0", "1e-12");
    model.component("c").physics("plas").feature("O2_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("Cu_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe-2e7[1/m^3]");

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");
    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("P13trace_ready.mph");

    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe+2e7[1/m^3]");

    model.component("c").variable("gasv").set("axisWeight", "r/Rdom");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("flow").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)", "axisWeight*(rhoP*uzP/rho0)"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("temp").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("temp").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("temp").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(0)"}});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", new String[]{"axisWeight*((rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0))"});
    model.component("c").physics("ld").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("ld").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("ld").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-optD-0.02*h*d(optD,z))"}});
    model.component("c").physics("ld").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optD)"});
    model.component("c").physics("lu").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("lu").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("lu").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(optU-0.02*h*d(optU,z))"}});
    model.component("c").physics("lu").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optU)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"axisWeight*((mdot+rhoP*vnSurf)/rho0)", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.metal.nJt", new String[]{"Iwall-plas.wall.nJe"});

    model.component("c").variable("boundv")
         .set("mueWallDotE", "-e_const/(me_const*nuCollision)*(plas.Etr*ncr+plas.Etz*ncz)");

    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.nemig_bnd", new String[]{"plas.ne*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.enmig_bnd", new String[]{"(5/3)*plas.en*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("ld").feature("exit").selection().named("surface");
    model.component("c").physics("ld").feature("exit").set("g", "-axisWeight*optD*ncz");
    model.component("c").physics("ld").feature("init1").set("optD", "pulse");
    model.component("c").physics("lu").feature("exit").selection().named("top");
    model.component("c").physics("lu").feature("exit").set("g", "-axisWeight*optU");
    model.component("c").physics("lu").feature("init1").set("optU", "(1-Acu)*pulse");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");

    return model;
  }

  public static Model run14(Model model) {
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});

    model.component("c").common().remove("meshSurf");
    model.component("c").common().create("meshSurf", "PrescribedMeshVelocity");
    model.component("c").common("meshSurf").selection().named("surface");
    model.component("c").common("meshSurf")
         .set("prescribedMeshVelocity", new String[]{"0", "0", "meltOn*(uzCu+urCu*ncr/ncz-mdot/(rhoCu*ncz))"});
    model.component("c").common("meshSurf").set("constraintType", "Nitsche");
    model.component("c").common("deform").set("smoothingType", "laplace");

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");
    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("P15_ready.mph");

    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe+2e7[1/m^3]");

    model.component("c").variable("gasv").set("axisWeight", "r/Rdom");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("flow").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)", "axisWeight*(rhoP*uzP/rho0)"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("temp").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("temp").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("temp").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(0)"}});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", new String[]{"axisWeight*((rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0))"});
    model.component("c").physics("ld").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("ld").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("ld").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-optD-0.02*h*d(optD,z))"}});
    model.component("c").physics("ld").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optD)"});
    model.component("c").physics("lu").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("lu").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("lu").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(optU-0.02*h*d(optU,z))"}});
    model.component("c").physics("lu").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optU)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"axisWeight*((mdot+rhoP*vnSurf)/rho0)", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.metal.nJt", new String[]{"Iwall-plas.wall.nJe"});

    model.component("c").variable("boundv")
         .set("mueWallDotE", "-e_const/(me_const*nuCollision)*(plas.Etr*ncr+plas.Etz*ncz)");

    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.nemig_bnd", new String[]{"plas.ne*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.enmig_bnd", new String[]{"(5/3)*plas.en*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("ld").feature("exit").selection().named("surface");
    model.component("c").physics("ld").feature("exit").set("g", "-axisWeight*optD*ncz");
    model.component("c").physics("ld").feature("init1").set("optD", "pulse");
    model.component("c").physics("lu").feature("exit").selection().named("top");
    model.component("c").physics("lu").feature("exit").set("g", "-axisWeight*optU");
    model.component("c").physics("lu").feature("init1").set("optU", "(1-Acu)*pulse");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");

    return model;
  }

  public static Model run15(Model model) {
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});
    model.component("c").physics("plas").feature("N2_1")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_2")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_3")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_4")
         .set("de", "0.59*(1-exp(0.59[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.59[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_5")
         .set("de", "0.88*(1-exp(0.88[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.88[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_6")
         .set("de", "1.17*(1-exp(1.17[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.17[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_7")
         .set("de", "1.47*(1-exp(1.47[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.47[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_8")
         .set("de", "1.76*(1-exp(1.76[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.76[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_3")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_4")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_5")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_6")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_7")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_8")
         .set("de", "0.57*(1-exp(0.57[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.57[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_9")
         .set("de", "0.75*(1-exp(0.75[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.75[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_10")
         .set("de", "0.977*(1-exp(0.977[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.977[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_11")
         .set("de", "1.627*(1-exp(1.627[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.627[V]*e_const/(k_B_const*Tgas)))");

    model.component("c").common().remove("meshSurf");
    model.component("c").common().create("meshSurf", "PrescribedMeshVelocity");
    model.component("c").common("meshSurf").selection().named("surface");
    model.component("c").common("meshSurf")
         .set("prescribedMeshVelocity", new String[]{"0", "0", "meltOn*(uzCu+urCu*ncr/ncz-mdot/(rhoCu*ncz))"});
    model.component("c").common("meshSurf").set("constraintType", "Nitsche");
    model.component("c").common("deform").set("smoothingType", "laplace");

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");
    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("P16_ready.mph");

    model.component("c").physics("ht").feature("advection")
         .set("Q0", "-rhoCu*cpCu*((urCu-d(r,TIME))*d(T,r)+(uzCu-d(z,TIME))*d(T,z))");
    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe+2e7[1/m^3]");

    model.component("c").variable("gasv").set("axisWeight", "r/Rdom");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("flow").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)", "axisWeight*(rhoP*uzP/rho0)"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("temp").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("temp").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("temp").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(0)"}});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", new String[]{"axisWeight*((rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0))"});
    model.component("c").physics("ld").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("ld").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("ld").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-optD-0.02*h*d(optD,z))"}});
    model.component("c").physics("ld").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optD)"});
    model.component("c").physics("lu").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("lu").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("lu").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(optU-0.02*h*d(optU,z))"}});
    model.component("c").physics("lu").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optU)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"axisWeight*((mdot+rhoP*vnSurf)/rho0)", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.metal.nJt", new String[]{"Iwall-plas.wall.nJe"});

    model.component("c").variable("boundv")
         .set("mueWallDotE", "-e_const/(me_const*nuCollision)*(plas.Etr*ncr+plas.Etz*ncz)");

    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.nemig_bnd", new String[]{"plas.ne*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.enmig_bnd", new String[]{"(5/3)*plas.en*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("ld").feature("exit").selection().named("surface");
    model.component("c").physics("ld").feature("exit").set("g", "-axisWeight*optD*ncz");
    model.component("c").physics("ld").feature("init1").set("optD", "pulse");
    model.component("c").physics("lu").feature("exit").selection().named("top");
    model.component("c").physics("lu").feature("exit").set("g", "-axisWeight*optU");
    model.component("c").physics("lu").feature("init1").set("optU", "(1-Acu)*pulse");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "TeV*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "plas.ne*e_const*TeV");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");

    return model;
  }

  public static Model run16(Model model) {
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+r_Curr*e_const*(7.72638[V]+1.5*TeV)+r_Nrr*e_const*(14.534[V]+1.5*TeV)+r_Orr*e_const*(13.618[V]+1.5*TeV)+r_CuD5rr*e_const*(6.337432[V]+1.5*TeV)+r_CuD3rr*e_const*(6.0841544[V]+1.5*TeV)+r_CuP1rr*e_const*(3.9404822999999998[V]+1.5*TeV)+r_CuP3rr*e_const*(3.9096879999999996[V]+1.5*TeV)+r_CuH3rr*e_const*(1.5352049[V]+1.5*TeV)+r_CuH5rr*e_const*(1.5343548[V]+1.5*TeV)+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*1.5*e_const*TeV+r_Nrr*1.5*e_const*TeV+r_Orr*1.5*e_const*TeV+r_CuD5rr*1.5*e_const*TeV+r_CuD3rr*1.5*e_const*TeV+r_CuP1rr*1.5*e_const*TeV+r_CuP3rr*1.5*e_const*TeV+r_CuH3rr*1.5*e_const*TeV+r_CuH5rr*1.5*e_const*TeV");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});
    model.component("c").physics("plas").feature("N2_1")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_2")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_3")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_4")
         .set("de", "0.59*(1-exp(0.59[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.59[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_5")
         .set("de", "0.88*(1-exp(0.88[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.88[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_6")
         .set("de", "1.17*(1-exp(1.17[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.17[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_7")
         .set("de", "1.47*(1-exp(1.47[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.47[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_8")
         .set("de", "1.76*(1-exp(1.76[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.76[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_3")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_4")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_5")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_6")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_7")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_8")
         .set("de", "0.57*(1-exp(0.57[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.57[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_9")
         .set("de", "0.75*(1-exp(0.75[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.75[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_10")
         .set("de", "0.977*(1-exp(0.977[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.977[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_11")
         .set("de", "1.627*(1-exp(1.627[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.627[V]*e_const/(k_B_const*Tgas)))");

    model.component("c").common().remove("meshSurf");
    model.component("c").common().create("meshSurf", "PrescribedMeshVelocity");
    model.component("c").common("meshSurf").selection().named("surface");
    model.component("c").common("meshSurf")
         .set("prescribedMeshVelocity", new String[]{"0", "0", "meltOn*(uzCu+urCu*ncr/ncz-mdot/(rhoCu*ncz))"});
    model.component("c").common("meshSurf").set("constraintType", "Nitsche");
    model.component("c").common("deform").set("smoothingType", "laplace");

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");
    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");

    return model;
  }

  public static Model run17(Model model) {
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("P17_ready.mph");

    model.func().create("rrEnergy", "Interpolation");
    model.func("rrEnergy").set("source", "file");
    model.func("rrEnergy").set("filename", "D:/junlin/PlasmaPlume/route1/data/rr_mean_energy.txt");
    model.func("rrEnergy").set("nargs", 1);
    model.func("rrEnergy")
         .set("funcs", new String[][]{{"rrMean_Curr", "1"}, 
         {"rrMean_Nrr", "2"}, 
         {"rrMean_Orr", "3"}, 
         {"rrMean_CuD5rr", "4"}, 
         {"rrMean_CuD3rr", "5"}, 
         {"rrMean_CuP1rr", "6"}, 
         {"rrMean_CuP3rr", "7"}, 
         {"rrMean_CuH3rr", "8"}, 
         {"rrMean_CuH5rr", "9"}});
    model.func("rrEnergy").set("interp", "piecewisecubic");
    model.func("rrEnergy").set("extrap", "const");
    model.func("rrEnergy").importData();

    model.component("c").cpl("intSurf").set("axisym", true);
    model.component("c").cpl("intGas").set("axisym", true);
    model.component("c").cpl("intCu").set("axisym", true);

    model.component("c").physics("ht").feature("advection")
         .set("Q0", "-rhoCu*cpCu*((urCu-d(r,TIME))*d(T,r)+(uzCu-d(z,TIME))*d(T,z))");
    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O2_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("Cu_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe-2e7[1/m^3]");

    model.component("c").variable("gasv").set("axisWeight", "r/Rdom");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("flow").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)", "axisWeight*(rhoP*uzP/rho0)"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("temp").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("temp").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("temp").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(0)"}});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", new String[]{"axisWeight*((rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0))"});
    model.component("c").physics("ld").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("ld").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("ld").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-optD-0.02*h*d(optD,z))"}});
    model.component("c").physics("ld").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optD)"});
    model.component("c").physics("lu").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("lu").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("lu").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(optU-0.02*h*d(optU,z))"}});
    model.component("c").physics("lu").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optU)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"axisWeight*((mdot+rhoP*vnSurf)/rho0)", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.metal.nJt", new String[]{"Iwall-plas.wall.nJe"});

    model.component("c").variable("boundv")
         .set("mueWallDotE", "-e_const/(me_const*nuCollision)*(plas.Etr*ncr+plas.Etz*ncz)");

    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.nemig_bnd", new String[]{"plas.ne*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.enmig_bnd", new String[]{"(5/3)*plas.en*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("ld").feature("exit").selection().named("surface");
    model.component("c").physics("ld").feature("exit").set("g", "-axisWeight*optD*ncz");
    model.component("c").physics("ld").feature("init1").set("optD", "pulse");
    model.component("c").physics("lu").feature("exit").selection().named("top");
    model.component("c").physics("lu").feature("exit").set("g", "-axisWeight*optU");
    model.component("c").physics("lu").feature("init1").set("optU", "(1-Acu)*pulse");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "plas.Te*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "(2/3)*Ue");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_Curr", "10^rrMean_Curr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_Cu", "r_Curr*e_const*Eph_Curr");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("Eph_Nrr", "10^rrMean_Nrr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_N", "r_Nrr*e_const*Eph_Nrr");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv").set("Eph_Orr", "10^rrMean_Orr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_O", "r_Orr*e_const*Eph_Orr");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuD5rr", "10^rrMean_CuD5rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuD5", "r_CuD5rr*e_const*Eph_CuD5rr");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuD3rr", "10^rrMean_CuD3rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuD3", "r_CuD3rr*e_const*Eph_CuD3rr");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuP1rr", "10^rrMean_CuP1rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuP1", "r_CuP1rr*e_const*Eph_CuP1rr");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuP3rr", "10^rrMean_CuP3rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuP3", "r_CuP3rr*e_const*Eph_CuP3rr");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuH3rr", "10^rrMean_CuH3rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuH3", "r_CuH3rr*e_const*Eph_CuH3rr");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuH5rr", "10^rrMean_CuH5rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuH5", "r_CuH5rr*e_const*Eph_CuH5rr");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+Qfb_Cu+Qfb_N+Qfb_O+Qfb_CuD5+Qfb_CuD3+Qfb_CuP1+Qfb_CuP3+Qfb_CuH3+Qfb_CuH5+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jfbVisible", "Qfb_CuH3*(exp(-max(h_const*c_const/(525[nm]*e_const)-1.5352049[V],0[V])/TeV)-exp(-max(h_const*c_const/(475[nm]*e_const)-1.5352049[V],0[V])/TeV))/(4*pi)+Qfb_CuH5*(exp(-max(h_const*c_const/(525[nm]*e_const)-1.5343548[V],0[V])/TeV)-exp(-max(h_const*c_const/(475[nm]*e_const)-1.5343548[V],0[V])/TeV))/(4*pi)");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)+jfbVisible");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*e_const*(Eph_Curr-7.72638[V])+r_Nrr*e_const*(Eph_Nrr-14.534[V])+r_Orr*e_const*(Eph_Orr-13.618[V])+r_CuD5rr*e_const*(Eph_CuD5rr-6.337432[V])+r_CuD3rr*e_const*(Eph_CuD3rr-6.0841544[V])+r_CuP1rr*e_const*(Eph_CuP1rr-3.9404822999999998[V])+r_CuP3rr*e_const*(Eph_CuP3rr-3.9096879999999996[V])+r_CuH3rr*e_const*(Eph_CuH3rr-1.5352049[V])+r_CuH5rr*e_const*(Eph_CuH5rr-1.5343548[V])");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");

    return model;
  }

  public static Model run18(Model model) {
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});
    model.component("c").physics("plas").feature("N2_1")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_2")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_3")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_4")
         .set("de", "0.59*(1-exp(0.59[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.59[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_5")
         .set("de", "0.88*(1-exp(0.88[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.88[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_6")
         .set("de", "1.17*(1-exp(1.17[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.17[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_7")
         .set("de", "1.47*(1-exp(1.47[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.47[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_8")
         .set("de", "1.76*(1-exp(1.76[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.76[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_3")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_4")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_5")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_6")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_7")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_8")
         .set("de", "0.57*(1-exp(0.57[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.57[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_9")
         .set("de", "0.75*(1-exp(0.75[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.75[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_10")
         .set("de", "0.977*(1-exp(0.977[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.977[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_11")
         .set("de", "1.627*(1-exp(1.627[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.627[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("melt").prop("ShapeProperty").set("order_fluid", 2);
    model.component("c").physics("melt").prop("ConsistentStabilization").set("StreamlineDiffusion", false);
    model.component("c").physics("melt").prop("ConsistentStabilization").set("CrosswindDiffusion", false);

    model.component("c").common().remove("meshSurf");
    model.component("c").common().create("meshSurf", "PrescribedMeshVelocity");
    model.component("c").common("meshSurf").selection().named("surface");
    model.component("c").common("meshSurf")
         .set("prescribedMeshVelocity", new String[]{"0", "0", "meltOn*(uzCu+urCu*ncr/ncz-mdot/(rhoCu*ncz))"});
    model.component("c").common("meshSurf").set("constraintType", "Nitsche");
    model.component("c").common("deform").set("smoothingType", "laplace");

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");
    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("P18_ready.mph");

    model.func().remove("rrEnergy");
    model.func().create("rrEnergy", "Interpolation");
    model.func("rrEnergy").set("source", "file");
    model.func("rrEnergy").set("filename", "D:/junlin/PlasmaPlume/route1/data/rr_mean_energy.txt");
    model.func("rrEnergy").set("nargs", 1);
    model.func("rrEnergy")
         .set("funcs", new String[][]{{"rrMean_Curr", "1"}, 
         {"rrMean_Nrr", "2"}, 
         {"rrMean_Orr", "3"}, 
         {"rrMean_CuD5rr", "4"}, 
         {"rrMean_CuD3rr", "5"}, 
         {"rrMean_CuP1rr", "6"}, 
         {"rrMean_CuP3rr", "7"}, 
         {"rrMean_CuH3rr", "8"}, 
         {"rrMean_CuH5rr", "9"}});
    model.func("rrEnergy").set("interp", "piecewisecubic");
    model.func("rrEnergy").set("extrap", "const");
    model.func("rrEnergy").importData();

    model.component("c").cpl("intSurf").set("axisym", true);
    model.component("c").cpl("intGas").set("axisym", true);
    model.component("c").cpl("intCu").set("axisym", true);

    model.component("c").physics("ht").feature("advection")
         .set("Q0", "-rhoCu*cpCu*((urCu-d(r,TIME))*d(T,r)+(uzCu-d(z,TIME))*d(T,z))");
    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O2_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("O_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("Cu_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe-2e7[1/m^3]");

    model.component("c").variable("gasv").set("axisWeight", "r/Rdom");

    model.component("c").physics("flow").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("flow").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)", "axisWeight*(rhoP*uzP/rho0)"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(0)", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("temp").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("temp").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("temp").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(0)"}});
    model.component("c").physics("temp").feature("gfeq1")
         .set("f", new String[]{"axisWeight*((rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0))"});
    model.component("c").physics("ld").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("ld").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("ld").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(-optD-0.02*h*d(optD,z))"}});
    model.component("c").physics("ld").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optD)"});
    model.component("c").physics("lu").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("lu").prop("ShapeProperty").set("frame", "spatial");
    model.component("c").physics("lu").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(0)", "axisWeight*(optU-0.02*h*d(optU,z))"}});
    model.component("c").physics("lu").feature("gfeq1").set("f", new String[]{"axisWeight*(-alphaLaser*optU)"});
    model.component("c").physics("flow").feature("gfeq1").set("da", "axisWeight");
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"axisWeight*((mdot+rhoP*vnSurf)/rho0)", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("plas").feature("metal").selection().named("surface");
    model.component("c").physics("plas").feature("metal").set("TerminalType", "Current");
    model.component("c").physics("plas").feature("metal").set("I0", "0[A]");
    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.metal.nJt", new String[]{"Iwall-plas.wall.nJe"});

    model.component("c").variable("boundv")
         .set("mueWallDotE", "-e_const/(me_const*nuCollision)*(plas.Etr*ncr+plas.Etz*ncz)");

    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.nemig_bnd", new String[]{"plas.ne*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("wall").featureInfo("info")
         .set("plas.enmig_bnd", new String[]{"(5/3)*plas.en*min(mueWallDotE,0[m/s])"});
    model.component("c").physics("plas").feature("N2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("N_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("O2_1m")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("Cu_1p")
         .set("MobilityDiffusivitySpecification", "ComputeMobilityAndDiffusivity");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas");
    model.component("c").physics("plas").feature("wallConv").selection().named("surface");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-urP*ncr-uzP*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("ld").feature("exit").selection().named("surface");
    model.component("c").physics("ld").feature("exit").set("g", "-axisWeight*optD*ncz");
    model.component("c").physics("ld").feature("init1").set("optD", "pulse");
    model.component("c").physics("lu").feature("exit").selection().named("top");
    model.component("c").physics("lu").feature("exit").set("g", "-axisWeight*optU");
    model.component("c").physics("lu").feature("init1").set("optU", "(1-Acu)*pulse");

    model.component("c").variable("gasv").set("rhoP", "rho0*qR");
    model.component("c").variable("gasv").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("c").variable("gasv").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("c").variable("gasv").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("c").variable("gasv").set("Tgas", "max(Tg,150[K])");
    model.component("c").variable("gasv").set("TeV", "max(plas.Te,0.015[V])");
    model.component("c").variable("gasv").set("TeK", "plas.Te*e_const/k_B_const");
    model.component("c").variable("gasv").set("Ue", "e_const*plas.en");
    model.component("c").variable("gasv").set("pe", "(2/3)*Ue");
    model.component("c").variable("gasv").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("c").variable("gasv").set("nN2", "plas.n_wN2");
    model.component("c").variable("gasv").set("nO2", "plas.n_wO2");
    model.component("c").variable("gasv").set("nN", "plas.n_wN");
    model.component("c").variable("gasv").set("nO", "plas.n_wO");
    model.component("c").variable("gasv").set("nN2_1p", "plas.n_wN2_1p");
    model.component("c").variable("gasv").set("nO2_1p", "plas.n_wO2_1p");
    model.component("c").variable("gasv").set("nN_1p", "plas.n_wN_1p");
    model.component("c").variable("gasv").set("nO_1p", "plas.n_wO_1p");
    model.component("c").variable("gasv").set("nO_1m", "plas.n_wO_1m");
    model.component("c").variable("gasv").set("nO2_1m", "plas.n_wO2_1m");
    model.component("c").variable("gasv").set("nCu", "plas.n_wCu");
    model.component("c").variable("gasv").set("nCuD5", "plas.n_wCuD5");
    model.component("c").variable("gasv").set("nCuD3", "plas.n_wCuD3");
    model.component("c").variable("gasv").set("nCuP1", "plas.n_wCuP1");
    model.component("c").variable("gasv").set("nCuP3", "plas.n_wCuP3");
    model.component("c").variable("gasv").set("nCuH3", "plas.n_wCuH3");
    model.component("c").variable("gasv").set("nCuH5", "plas.n_wCuH5");
    model.component("c").variable("gasv").set("nCu_1p", "plas.n_wCu_1p");
    model.component("c").variable("gasv")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("c").variable("gasv").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("c").variable("gasv").set("Ycu", "rhoCopper/rhoSafe");
    model.component("c").variable("gasv")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("c").variable("gasv")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("c").variable("gasv").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("c").variable("gasv").set("pPl", "pHeavy+pe");
    model.component("c").variable("gasv").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("c").variable("gasv").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("c").variable("gasv").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("c").variable("gasv").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("c").variable("gasv").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("c").variable("gasv")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("c").variable("gasv").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("c").variable("gasv").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("c").variable("gasv").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("c").variable("gasv").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("c").variable("gasv").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("c").variable("gasv").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("c").variable("gasv")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("c").variable("gasv").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("c").variable("gasv").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("c").variable("gasv")
         .set("alphaLaser", "plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("c").variable("gasv").set("Qei", "3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("c").variable("gasv")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("c").variable("gasv")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("c").variable("gasv")
         .set("Hdiffr", "h_N2*(plas.tfluxr_wN2-plas.cfluxr_wN2)+h_O2*(plas.tfluxr_wO2-plas.cfluxr_wO2)+h_N*(plas.tfluxr_wN-plas.cfluxr_wN)+h_O*(plas.tfluxr_wO-plas.cfluxr_wO)+h_N2_1p*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+h_O2_1p*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+h_N_1p*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+h_O_1p*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+h_O_1m*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+h_O2_1m*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+h_Cu*(plas.tfluxr_wCu-plas.cfluxr_wCu)+h_CuD5*(plas.tfluxr_wCuD5-plas.cfluxr_wCuD5)+h_CuD3*(plas.tfluxr_wCuD3-plas.cfluxr_wCuD3)+h_CuP1*(plas.tfluxr_wCuP1-plas.cfluxr_wCuP1)+h_CuP3*(plas.tfluxr_wCuP3-plas.cfluxr_wCuP3)+h_CuH3*(plas.tfluxr_wCuH3-plas.cfluxr_wCuH3)+h_CuH5*(plas.tfluxr_wCuH5-plas.cfluxr_wCuH5)+h_Cu_1p*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)");
    model.component("c").variable("gasv")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas.tfluxr_wN2_1p-plas.cfluxr_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1p-plas.cfluxr_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxr_wN_1p-plas.cfluxr_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1p-plas.cfluxr_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxr_wO_1m-plas.cfluxr_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxr_wO2_1m-plas.cfluxr_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxr_wCu_1p-plas.cfluxr_wCu_1p)-e_const*plas.gflux_ner+plas.scharge*urP");
    model.component("c").variable("gasv")
         .set("Hdiffz", "h_N2*(plas.tfluxz_wN2-plas.cfluxz_wN2)+h_O2*(plas.tfluxz_wO2-plas.cfluxz_wO2)+h_N*(plas.tfluxz_wN-plas.cfluxz_wN)+h_O*(plas.tfluxz_wO-plas.cfluxz_wO)+h_N2_1p*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+h_O2_1p*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+h_N_1p*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+h_O_1p*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+h_O_1m*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+h_O2_1m*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+h_Cu*(plas.tfluxz_wCu-plas.cfluxz_wCu)+h_CuD5*(plas.tfluxz_wCuD5-plas.cfluxz_wCuD5)+h_CuD3*(plas.tfluxz_wCuD3-plas.cfluxz_wCuD3)+h_CuP1*(plas.tfluxz_wCuP1-plas.cfluxz_wCuP1)+h_CuP3*(plas.tfluxz_wCuP3-plas.cfluxz_wCuP3)+h_CuH3*(plas.tfluxz_wCuH3-plas.cfluxz_wCuH3)+h_CuH5*(plas.tfluxz_wCuH5-plas.cfluxz_wCuH5)+h_Cu_1p*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)");

    return model;
  }

  public static Model run19(Model model) {
    model.component("c").variable("gasv")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas.tfluxz_wN2_1p-plas.cfluxz_wN2_1p)+1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1p-plas.cfluxz_wO2_1p)+1*F_const/0.0140067[kg/mol]*(plas.tfluxz_wN_1p-plas.cfluxz_wN_1p)+1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1p-plas.cfluxz_wO_1p)+-1*F_const/0.0159994[kg/mol]*(plas.tfluxz_wO_1m-plas.cfluxz_wO_1m)+-1*F_const/0.0319988[kg/mol]*(plas.tfluxz_wO2_1m-plas.cfluxz_wO2_1m)+1*F_const/0.063546[kg/mol]*(plas.tfluxz_wCu_1p-plas.cfluxz_wCu_1p)-e_const*plas.gflux_nez+plas.scharge*uzP");
    model.component("c").variable("gasv").set("Qfield", "Jtotalr*plas.Etr+Jtotalz*plas.Etz");
    model.component("c").variable("gasv").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("c").variable("gasv").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("c").variable("gasv").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("c").variable("gasv").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("c").variable("gasv")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("c").variable("gasv")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("c").variable("gasv")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("c").variable("gasv").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("c").variable("gasv")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("c").variable("gasv").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("c").variable("gasv").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_Curr", "10^rrMean_Curr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_Cu", "r_Curr*e_const*Eph_Curr");
    model.component("c").variable("gasv").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nN_1p");
    model.component("c").variable("gasv").set("Eph_Nrr", "10^rrMean_Nrr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_N", "r_Nrr*e_const*Eph_Nrr");
    model.component("c").variable("gasv").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO_1p");
    model.component("c").variable("gasv").set("Eph_Orr", "10^rrMean_Orr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_O", "r_Orr*e_const*Eph_Orr");
    model.component("c").variable("gasv")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuD5rr", "10^rrMean_CuD5rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuD5", "r_CuD5rr*e_const*Eph_CuD5rr");
    model.component("c").variable("gasv")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuD3rr", "10^rrMean_CuD3rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuD3", "r_CuD3rr*e_const*Eph_CuD3rr");
    model.component("c").variable("gasv")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuP1rr", "10^rrMean_CuP1rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuP1", "r_CuP1rr*e_const*Eph_CuP1rr");
    model.component("c").variable("gasv")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuP3rr", "10^rrMean_CuP3rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuP3", "r_CuP3rr*e_const*Eph_CuP3rr");
    model.component("c").variable("gasv")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuH3rr", "10^rrMean_CuH3rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuH3", "r_CuH3rr*e_const*Eph_CuH3rr");
    model.component("c").variable("gasv")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nCu_1p");
    model.component("c").variable("gasv").set("Eph_CuH5rr", "10^rrMean_CuH5rr(log10(TeV/1[V]))*1[V]");
    model.component("c").variable("gasv").set("Qfb_CuH5", "r_CuH5rr*e_const*Eph_CuH5rr");
    model.component("c").variable("gasv").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2");
    model.component("c").variable("gasv")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+Qfb_Cu+Qfb_N+Qfb_O+Qfb_CuD5+Qfb_CuD3+Qfb_CuP1+Qfb_CuP3+Qfb_CuH3+Qfb_CuH5+Qff");
    model.component("c").variable("gasv")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("c").variable("gasv")
         .set("jfbVisible", "Qfb_CuH3*(exp(-max(h_const*c_const/(525[nm]*e_const)-1.5352049[V],0[V])/TeV)-exp(-max(h_const*c_const/(475[nm]*e_const)-1.5352049[V],0[V])/TeV))/(4*pi)+Qfb_CuH5*(exp(-max(h_const*c_const/(525[nm]*e_const)-1.5343548[V],0[V])/TeV)-exp(-max(h_const*c_const/(475[nm]*e_const)-1.5343548[V],0[V])/TeV))/(4*pi)");
    model.component("c").variable("gasv")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)+jfbVisible");
    model.component("c").variable("gasv")
         .set("QeRR", "r_Curr*e_const*(Eph_Curr-7.72638[V])+r_Nrr*e_const*(Eph_Nrr-14.534[V])+r_Orr*e_const*(Eph_Orr-13.618[V])+r_CuD5rr*e_const*(Eph_CuD5rr-6.337432[V])+r_CuD3rr*e_const*(Eph_CuD3rr-6.0841544[V])+r_CuP1rr*e_const*(Eph_CuP1rr-3.9404822999999998[V])+r_CuP3rr*e_const*(Eph_CuP3rr-3.9096879999999996[V])+r_CuH3rr*e_const*(Eph_CuH3rr-1.5352049[V])+r_CuH5rr*e_const*(Eph_CuH5rr-1.5343548[V])");
    model.component("c").variable("gasv")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas.ne*nO2^2)");
    model.component("c").variable("boundv").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("c").variable("boundv").set("ncz", "abs(nz)");
    model.component("c").variable("boundv").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("c").variable("boundv")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component("c").variable("boundv")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component("c").variable("boundv")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component("c").variable("boundv").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("c").variable("boundv")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])");
    model.component("c").variable("boundv")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("c").variable("boundv")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("c").variable("boundv").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component("c").variable("boundv").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("c").variable("boundv").set("Gtherm", "plas.thflux");
    model.component("c").variable("boundv").set("GeIn", "-plas.ne_bnd-plas.nemig_bnd");
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf"});
    model.component("c").physics("plas").feature("N2_1")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_2")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_3")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_4")
         .set("de", "0.59*(1-exp(0.59[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.59[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_5")
         .set("de", "0.88*(1-exp(0.88[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.88[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_6")
         .set("de", "1.17*(1-exp(1.17[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.17[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_7")
         .set("de", "1.47*(1-exp(1.47[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.47[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("N2_8")
         .set("de", "1.76*(1-exp(1.76[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.76[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_3")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_4")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_5")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_6")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_7")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_8")
         .set("de", "0.57*(1-exp(0.57[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.57[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_9")
         .set("de", "0.75*(1-exp(0.75[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.75[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_10")
         .set("de", "0.977*(1-exp(0.977[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.977[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("plas").feature("O2_11")
         .set("de", "1.627*(1-exp(1.627[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.627[V]*e_const/(k_B_const*Tgas)))");
    model.component("c").physics("melt").prop("ShapeProperty").set("order_fluid", 2);
    model.component("c").physics("melt").prop("ConsistentStabilization").set("StreamlineDiffusion", false);
    model.component("c").physics("melt").prop("ConsistentStabilization").set("CrosswindDiffusion", false);

    model.component("c").common().remove("meshSurf");

    model.component("c").physics().create("surfaceODE", "BoundaryODE", "g", new String[]{"etaSurface"});
    model.component("c").physics("surfaceODE").selection().named("surface");
    model.component("c").physics("surfaceODE").prop("ShapeProperty").set("frame", "material");
    model.component("c").physics("surfaceODE").prop("ShapeProperty").set("shapeFunctionType", "shlag");
    model.component("c").physics("surfaceODE").prop("ShapeProperty").set("valueType", "real");
    model.component("c").physics("surfaceODE").prop("ShapeProperty").set("order", 1);
    model.component("c").physics("surfaceODE").feature("dode1").set("da", "r/Rdom");
    model.component("c").physics("surfaceODE").feature("dode1")
         .set("f", "r/Rdom*meltOn*(uzCu+urCu*ncr/ncz-mdot/(rhoCu*ncz))/1[um]");

    model.component("c").common().create("meshSurf", "PrescribedMeshDisplacement");
    model.component("c").common("meshSurf").selection().named("surface");
    model.component("c").common("meshSurf")
         .set("prescribedMeshDisplacement", new String[]{"0", "0", "etaSurface*1[um]"});
    model.component("c").common("meshSurf").set("constraintType", "pointwise");
    model.component("c").common("deform").set("smoothingType", "laplace");

    model.param().set("tEnd", "1000[ns]");

    model.sol().remove("sol1");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "1000");
    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("P19_ready.mph");

    model.component("c").physics("ht").create("fluid1", "FluidHeatTransferModel", 2);
    model.component("c").physics("ht").feature("fluid1").selection().named("g_cu_dom");
    model.component("c").physics("ht").feature("fluid1").set("u", new String[]{"urCu", "0", "uzCu"});
    model.component("c").physics("ht").feature("fluid1").set("fluidType", "gasLiquid");
    model.component("c").physics("ht").feature("advection").set("Q0", "0[W/m^3]");

    model.sol("sol1").feature("t1").create("remesh", "AutoRemesh");
    model.sol("sol1").feature("t1").feature("remesh").set("autoremeshgeom", "g");
    model.sol("sol1").feature("t1").feature("remesh").set("stopcondtype", "quality");
    model.sol("sol1").feature("t1").feature("remesh").set("stopexpr", "c.spatial.minqual");
    model.sol("sol1").feature("t1").feature("remesh").set("stopval", "0.25");
    model.sol("sol1").feature("t1").feature("remesh").set("initialstepremeshactive", true);
    model.sol("sol1").feature("t1").feature("remesh").set("initialstepremesh", "0.001[ns]");

    model.label("P20_ready.mph");

    model.component("c").physics("plas").prop("ConsistentStabilization")
         .set("StreamlineDiffusionElectronsLog", true);
    model.component("c").physics("plas").prop("ConsistentStabilization").set("StreamlineDiffusionIonsLog", true);
    model.component("c").physics("plas").prop("ConsistentStabilization").set("CrosswindDiffusionElectronsLog", true);
    model.component("c").physics("plas").prop("ConsistentStabilization").set("CrosswindDiffusionIonsLog", true);

    model.label("P21_ready.mph");
    model.label("P21.mph");

    model.sol("sol2").getPVals();
    model.sol("sol2").copySolution("restartPhysical");

    model.component("c").physics("ht").feature("fluid1").selection().named("g_cu_dom");
    model.component("c").physics("ht").feature("fluid1").set("u", new String[]{"urCu", "0", "uzCu"});
    model.component("c").physics("ht").feature("fluid1").set("fluidType", "gasLiquid");
    model.component("c").physics("ht").feature("advection").set("Q0", "0[W/m^3]");
    model.component("c").physics("plas").prop("ConsistentStabilization")
         .set("StreamlineDiffusionElectronsLog", true);
    model.component("c").physics("plas").prop("ConsistentStabilization").set("StreamlineDiffusionIonsLog", true);
    model.component("c").physics("plas").prop("ConsistentStabilization").set("CrosswindDiffusionElectronsLog", true);
    model.component("c").physics("plas").prop("ConsistentStabilization").set("CrosswindDiffusionIonsLog", true);
    model.component("c").physics("plas").prop("Stabilization").set("ReactionSourceStabilization", false);

    model.param().set("traceIota", "2");
    model.param().set("traceCuIota", "2");

    model.component("c").physics("plas").prop("Stabilization").set("iota", "traceIota");

    model.component("c").variable("gasv").set("traceCuAvailable", "plas.n_wCu/(plas.n_wCu+1e10[1/m^3])");
    model.component("c").variable("gasv")
         .set("SrawN2", "exp(-traceIota*log(max(plas.n_wN2,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumN2", "exp(-traceIota*log(max(plas.n_wN2,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SrawO2", "exp(-traceIota*log(max(plas.n_wO2,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumO2", "exp(-traceIota*log(max(plas.n_wO2,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    return model;
  }

  public static Model run20(Model model) {
    model.component("c").variable("gasv")
         .set("SrawN", "exp(-traceIota*log(max(plas.n_wN,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumN", "exp(-traceIota*log(max(plas.n_wN,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("N").featureInfo("info")
         .set("plas.R_wN", new String[]{"(-plas.r_75+plas.r_76+plas.r_77+2*plas.r_99+plas.r_110+plas.r_111+2*plas.r_114-2*plas.r_115+2*plas.r_116-2*plas.r_117+2*plas.r_118-2*plas.r_119)-(-SnumN)"});

    model.component("c").variable("gasv")
         .set("SrawO", "exp(-traceIota*log(max(plas.n_wO,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumO", "exp(-traceIota*log(max(plas.n_wO,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("O").featureInfo("info")
         .set("plas.R_wO", new String[]{"(plas.r_27+plas.r_28+2*plas.r_40+2*plas.r_41+2*plas.r_42-plas.r_78+plas.r_79+plas.r_80+2*plas.r_100-plas.r_102+plas.r_104+plas.r_106+plas.r_108+plas.r_110+2*plas.r_112+plas.r_113+2*plas.r_120-2*plas.r_121+2*plas.r_122-2*plas.r_123+2*plas.r_124-2*plas.r_125)-(-SnumO)"});

    model.component("c").variable("gasv")
         .set("SrawN2_1p", "exp(-traceIota*log(max(plas.n_wN2_1p,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumN2_1p", "exp(-traceIota*log(max(plas.n_wN2_1p,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("N2_1p").featureInfo("info")
         .set("plas.R_wN2_1p", new String[]{"(plas.r_25+plas.r_26-plas.r_99-plas.r_106-plas.r_107)-(-SnumN2_1p)"});

    model.component("c").variable("gasv")
         .set("SrawO2_1p", "exp(-traceIota*log(max(plas.n_wO2_1p,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumO2_1p", "exp(-traceIota*log(max(plas.n_wO2_1p,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("O2_1p").featureInfo("info")
         .set("plas.R_wO2_1p", new String[]{"(plas.r_43-plas.r_100-plas.r_108-plas.r_109)-(-SnumO2_1p)"});

    model.component("c").variable("gasv")
         .set("SrawN_1p", "exp(-traceIota*log(max(plas.n_wN_1p,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumN_1p", "exp(-traceIota*log(max(plas.n_wN_1p,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("N_1p").featureInfo("info")
         .set("plas.R_wN_1p", new String[]{"(plas.r_75-plas.r_76-plas.r_77-plas.r_110-plas.r_111)-(-SnumN_1p)"});

    model.component("c").variable("gasv")
         .set("SrawO_1p", "exp(-traceIota*log(max(plas.n_wO_1p,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumO_1p", "exp(-traceIota*log(max(plas.n_wO_1p,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("O_1p").featureInfo("info")
         .set("plas.R_wO_1p", new String[]{"(plas.r_78-plas.r_79-plas.r_80-plas.r_112-plas.r_113)-(-SnumO_1p)"});

    model.component("c").variable("gasv")
         .set("SrawO_1m", "exp(-traceIota*log(max(plas.n_wO_1m,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumO_1m", "exp(-traceIota*log(max(plas.n_wO_1m,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("O_1m").featureInfo("info")
         .set("plas.R_wO_1m", new String[]{"(plas.r_27+plas.r_28-plas.r_102-plas.r_104-plas.r_106-plas.r_108-plas.r_110-plas.r_112)-(-SnumO_1m)"});

    model.component("c").variable("gasv")
         .set("SrawO2_1m", "exp(-traceIota*log(max(plas.n_wO2_1m,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumO2_1m", "exp(-traceIota*log(max(plas.n_wO2_1m,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("O2_1m").featureInfo("info")
         .set("plas.R_wO2_1m", new String[]{"(plas.r_101-plas.r_103-plas.r_105-plas.r_107-plas.r_109-plas.r_111-plas.r_113)-(-SnumO2_1m)"});

    model.component("c").variable("gasv")
         .set("SrawCu", "exp(-traceIota*log(max(plas.n_wCu,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv").set("SnumCu", "0[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SrawCuD5", "exp(-traceIota*log(max(plas.n_wCuD5,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumCuD5", "traceCuAvailable*exp(-traceCuIota*log(max(plas.n_wCuD5,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("CuD5").featureInfo("info")
         .set("plas.R_wCuD5", new String[]{"(plas.r_44-plas.r_45-plas.r_55+plas.r_56-plas.r_81+plas.r_82+plas.r_54+plas.r_83)-(-SnumCuD5)"});

    model.component("c").variable("gasv")
         .set("SrawCuD3", "exp(-traceIota*log(max(plas.n_wCuD3,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumCuD3", "traceCuAvailable*exp(-traceCuIota*log(max(plas.n_wCuD3,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("CuD3").featureInfo("info")
         .set("plas.R_wCuD3", new String[]{"(plas.r_46-plas.r_47-plas.r_67+plas.r_68-plas.r_70+plas.r_71-plas.r_84+plas.r_85+plas.r_66+plas.r_69+plas.r_86)-(-SnumCuD3)"});

    model.component("c").variable("gasv")
         .set("SrawCuP1", "exp(-traceIota*log(max(plas.n_wCuP1,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumCuP1", "traceCuAvailable*exp(-traceCuIota*log(max(plas.n_wCuP1,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("CuP1").featureInfo("info")
         .set("plas.R_wCuP1", new String[]{"(plas.r_48-plas.r_49-plas.r_58+plas.r_59+plas.r_70-plas.r_71-plas.r_87+plas.r_88-plas.r_53+plas.r_57-plas.r_69+plas.r_89)-(-SnumCuP1)"});

    model.component("c").variable("gasv")
         .set("SrawCuP3", "exp(-traceIota*log(max(plas.n_wCuP3,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumCuP3", "traceCuAvailable*exp(-traceCuIota*log(max(plas.n_wCuP3,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("CuP3").featureInfo("info")
         .set("plas.R_wCuP3", new String[]{"(plas.r_50-plas.r_51+plas.r_55-plas.r_56-plas.r_61+plas.r_62-plas.r_64+plas.r_65+plas.r_67-plas.r_68-plas.r_90+plas.r_91-plas.r_52-plas.r_54+plas.r_60+plas.r_63-plas.r_66+plas.r_92)-(-SnumCuP3)"});

    model.component("c").variable("gasv")
         .set("SrawCuH3", "exp(-traceIota*log(max(plas.n_wCuH3,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumCuH3", "traceCuAvailable*exp(-traceCuIota*log(max(plas.n_wCuH3,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("CuH3").featureInfo("info")
         .set("plas.R_wCuH3", new String[]{"(plas.r_58-plas.r_59+plas.r_64-plas.r_65-plas.r_93+plas.r_94-plas.r_57-plas.r_63+plas.r_95)-(-SnumCuH3)"});

    model.component("c").variable("gasv")
         .set("SrawCuH5", "exp(-traceIota*log(max(plas.n_wCuH5,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumCuH5", "traceCuAvailable*exp(-traceCuIota*log(max(plas.n_wCuH5,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("CuH5").featureInfo("info")
         .set("plas.R_wCuH5", new String[]{"(plas.r_61-plas.r_62-plas.r_96+plas.r_97-plas.r_60+plas.r_98)-(-SnumCuH5)"});

    model.component("c").variable("gasv")
         .set("SrawCu_1p", "exp(-traceIota*log(max(plas.n_wCu_1p,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");
    model.component("c").variable("gasv")
         .set("SnumCu_1p", "traceCuAvailable*exp(-traceCuIota*log(max(plas.n_wCu_1p,1[1/m^3])/1[1/m^3]))*1[mol/(m^3*s)]");

    model.component("c").physics("plas").feature("Cu_1p").featureInfo("info")
         .set("plas.R_wCu_1p", new String[]{"(plas.r_72-plas.r_73+plas.r_81-plas.r_82+plas.r_84-plas.r_85+plas.r_87-plas.r_88+plas.r_90-plas.r_91+plas.r_93-plas.r_94+plas.r_96-plas.r_97-plas.r_74-plas.r_83-plas.r_86-plas.r_89-plas.r_92-plas.r_95-plas.r_98-plas.r_104-plas.r_105)-(-SnumCu_1p)"});
    model.component("c").physics("plas").feature("Cu").featureInfo("info")
         .set("plas.R_wCu", new String[]{"(-plas.r_44+plas.r_45-plas.r_46+plas.r_47-plas.r_48+plas.r_49-plas.r_50+plas.r_51-plas.r_72+plas.r_73+plas.r_52+plas.r_53+plas.r_74+plas.r_104+plas.r_105)-(SnumCu+SnumCuD5+SnumCuD3+SnumCuP1+SnumCuP3+SnumCuH3+SnumCuH5+SnumCu_1p)"});
    model.component("c").physics("plas").feature("O2").featureInfo("info")
         .set("plas.R_wO2", new String[]{"(-plas.r_27-plas.r_28-plas.r_40-plas.r_41-plas.r_42-plas.r_43-plas.r_101+plas.r_102+plas.r_103+plas.r_105+plas.r_107+plas.r_108+2*plas.r_109+plas.r_111+plas.r_113-plas.r_120+plas.r_121-plas.r_122+plas.r_123-plas.r_124+plas.r_125)-(0.5*(SnumO+SnumO_1p+SnumO_1m)+SnumO2_1p+SnumO2_1m)"});

    model.component("c").variable("gasv")
         .set("SneNumerical", "N_A_const*(SnumN2_1p+SnumN_1p+SnumO2_1p+SnumO_1p+SnumCu_1p-SnumO_1m-SnumO2_1m)");

    model.component("c").physics("plas").feature("bulkconv")
         .set("Re", "-urP*d(plas.ne,r)-uzP*d(plas.ne,z)-plas.ne*divU+SneNumerical");
    model.component("c").physics("plas").feature("power")
         .set("Qgen", "Qlaser-QeRR-QeAttach-Qff-urP*d(Ue,r)-uzP*d(Ue,z)-(Ue+pe)*divU-Qei+1.5*e_const*TeV*SneNumerical");

    model.component("c").variable("gasv")
         .set("QnumCuExc", "F_const*(1.388948[V]*SnumCuD5+1.6422256[V]*SnumCuD3+3.7858977[V]*SnumCuP1+3.816692[V]*SnumCuP3+6.1911751[V]*SnumCuH3+6.1920252[V]*SnumCuH5+7.72638[V]*SnumCu_1p)");

    model.param().set("traceIota", "0.5");

    model.component("c").physics("plas").prop("TransportSettings").set("MixtureDiffusionCorrection", true);

    model.component("c").common("meshSurf").set("constraintMethod", "nodal");
    model.component("c").common("meshFixed").set("constraintMethod", "nodal");
    model.component("c").common("meshAxis").set("constraintMethod", "nodal");

    model.param().set("tEnd", "1000[ns]");

    model.study("std").feature("time").set("tlist", "range(80[ns],2[ns],tEnd)");

    model.sol("sol1").feature("t1").set("tlist", "range(80[ns],2[ns],tEnd)");
    model.sol("sol1").feature("v1").set("control", "user");
    model.sol("sol1").feature("v1").set("initmethod", "sol");
    model.sol("sol1").feature("v1").set("initsol", "restartPhysical");
    model.sol("sol1").feature("v1").set("solnum", "interp");
    model.sol("sol1").feature("v1").set("t", "80[ns]");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "100");
    model.sol("sol1").feature("t1").set("consistent", "bweuler");
    model.sol("sol1").feature("t1").set("bwinitstepfrac", "0.1");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").feature("remesh").set("consistentremesh", true);

    model.label("P38_ready.mph");

    model.component("c").variable("gasv")
         .set("compositionDen", "1+exp(WO2)+exp(WN)+exp(WO)+exp(WN2_1p)+exp(WO2_1p)+exp(WN_1p)+exp(WO_1p)+exp(WO_1m)+exp(WO2_1m)+exp(WCu)+exp(WCuD5)+exp(WCuD3)+exp(WCuP1)+exp(WCuP3)+exp(WCuH3)+exp(WCuH5)+exp(WCu_1p)");
    model.component("c").variable("gasv").set("compositionLogDen", "log(compositionDen)");

    model.param().set("initialN2MassFraction", "0.79*0.0280134/(0.79*0.0280134+0.21*0.0319988)");

    model.component("c").physics("plas").feature("N2").featureInfo("info")
         .set("plas.wN2", new String[]{"1/compositionDen"});
    model.component("c").physics("plas").feature("O2").featureInfo("info")
         .set("plas.wO2", new String[]{"exp(WO2)/compositionDen"});
    model.component("c").physics("plas").feature("O2").featureInfo("info")
         .set("plas.w0_wO2", new String[]{"(plas.x0_wO2*plas.M_wO2/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("N").featureInfo("info")
         .set("plas.wN", new String[]{"exp(WN)/compositionDen"});
    model.component("c").physics("plas").feature("N").featureInfo("info")
         .set("plas.w0_wN", new String[]{"(plas.x0_wN*plas.M_wN/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("O").featureInfo("info")
         .set("plas.wO", new String[]{"exp(WO)/compositionDen"});
    model.component("c").physics("plas").feature("O").featureInfo("info")
         .set("plas.w0_wO", new String[]{"(plas.x0_wO*plas.M_wO/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("N2_1p").featureInfo("info")
         .set("plas.wN2_1p", new String[]{"exp(WN2_1p)/compositionDen"});
    model.component("c").physics("plas").feature("N2_1p").featureInfo("info")
         .set("plas.w0_wN2_1p", new String[]{"(plas.x0_wN2_1p*plas.M_wN2_1p/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("O2_1p").featureInfo("info")
         .set("plas.wO2_1p", new String[]{"exp(WO2_1p)/compositionDen"});
    model.component("c").physics("plas").feature("O2_1p").featureInfo("info")
         .set("plas.w0_wO2_1p", new String[]{"(plas.x0_wO2_1p*plas.M_wO2_1p/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("N_1p").featureInfo("info")
         .set("plas.wN_1p", new String[]{"exp(WN_1p)/compositionDen"});
    model.component("c").physics("plas").feature("N_1p").featureInfo("info")
         .set("plas.w0_wN_1p", new String[]{"(plas.x0_wN_1p*plas.M_wN_1p/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("O_1p").featureInfo("info")
         .set("plas.wO_1p", new String[]{"exp(WO_1p)/compositionDen"});
    model.component("c").physics("plas").feature("O_1p").featureInfo("info")
         .set("plas.w0_wO_1p", new String[]{"(plas.x0_wO_1p*plas.M_wO_1p/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("O_1m").featureInfo("info")
         .set("plas.wO_1m", new String[]{"exp(WO_1m)/compositionDen"});
    model.component("c").physics("plas").feature("O_1m").featureInfo("info")
         .set("plas.w0_wO_1m", new String[]{"(plas.x0_wO_1m*plas.M_wO_1m/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("O2_1m").featureInfo("info")
         .set("plas.wO2_1m", new String[]{"exp(WO2_1m)/compositionDen"});
    model.component("c").physics("plas").feature("O2_1m").featureInfo("info")
         .set("plas.w0_wO2_1m", new String[]{"(plas.x0_wO2_1m*plas.M_wO2_1m/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("Cu").featureInfo("info")
         .set("plas.wCu", new String[]{"exp(WCu)/compositionDen"});
    model.component("c").physics("plas").feature("Cu").featureInfo("info")
         .set("plas.w0_wCu", new String[]{"(plas.x0_wCu*plas.M_wCu/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("CuD5").featureInfo("info")
         .set("plas.wCuD5", new String[]{"exp(WCuD5)/compositionDen"});
    model.component("c").physics("plas").feature("CuD5").featureInfo("info")
         .set("plas.w0_wCuD5", new String[]{"(plas.x0_wCuD5*plas.M_wCuD5/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("CuD3").featureInfo("info")
         .set("plas.wCuD3", new String[]{"exp(WCuD3)/compositionDen"});
    model.component("c").physics("plas").feature("CuD3").featureInfo("info")
         .set("plas.w0_wCuD3", new String[]{"(plas.x0_wCuD3*plas.M_wCuD3/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("CuP1").featureInfo("info")
         .set("plas.wCuP1", new String[]{"exp(WCuP1)/compositionDen"});
    model.component("c").physics("plas").feature("CuP1").featureInfo("info")
         .set("plas.w0_wCuP1", new String[]{"(plas.x0_wCuP1*plas.M_wCuP1/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("CuP3").featureInfo("info")
         .set("plas.wCuP3", new String[]{"exp(WCuP3)/compositionDen"});
    model.component("c").physics("plas").feature("CuP3").featureInfo("info")
         .set("plas.w0_wCuP3", new String[]{"(plas.x0_wCuP3*plas.M_wCuP3/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("CuH3").featureInfo("info")
         .set("plas.wCuH3", new String[]{"exp(WCuH3)/compositionDen"});
    model.component("c").physics("plas").feature("CuH3").featureInfo("info")
         .set("plas.w0_wCuH3", new String[]{"(plas.x0_wCuH3*plas.M_wCuH3/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("CuH5").featureInfo("info")
         .set("plas.wCuH5", new String[]{"exp(WCuH5)/compositionDen"});
    model.component("c").physics("plas").feature("CuH5").featureInfo("info")
         .set("plas.w0_wCuH5", new String[]{"(plas.x0_wCuH5*plas.M_wCuH5/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").feature("Cu_1p").featureInfo("info")
         .set("plas.wCu_1p", new String[]{"exp(WCu_1p)/compositionDen"});
    model.component("c").physics("plas").feature("Cu_1p").featureInfo("info")
         .set("plas.w0_wCu_1p", new String[]{"(plas.x0_wCu_1p*plas.M_wCu_1p/plas.w0denom)/initialN2MassFraction"});
    model.component("c").physics("plas").create("compositionChainRule", "WeakContribution", 2);
    model.component("c").physics("plas").feature("compositionChainRule").selection().named("g_gas_dom");
    model.component("c").physics("plas").feature("compositionChainRule")
         .set("weakExpression", "(0[kg/m^3]+plas.rho*plas.wO2*test(WO2)+plas.rho*plas.wN*test(WN)+plas.rho*plas.wO*test(WO)+plas.rho*plas.wN2_1p*test(WN2_1p)+plas.rho*plas.wO2_1p*test(WO2_1p)+plas.rho*plas.wN_1p*test(WN_1p)+plas.rho*plas.wO_1p*test(WO_1p)+plas.rho*plas.wO_1m*test(WO_1m)+plas.rho*plas.wO2_1m*test(WO2_1m)+plas.rho*plas.wCu*test(WCu)+plas.rho*plas.wCuD5*test(WCuD5)+plas.rho*plas.wCuD3*test(WCuD3)+plas.rho*plas.wCuP1*test(WCuP1)+plas.rho*plas.wCuP3*test(WCuP3)+plas.rho*plas.wCuH3*test(WCuH3)+plas.rho*plas.wCuH5*test(WCuH5)+plas.rho*plas.wCu_1p*test(WCu_1p))*(d(compositionLogDen,TIME)+(plas.ur-d(r,TIME))*d(compositionLogDen,r)+(plas.uz-d(z,TIME))*d(compositionLogDen,z))");
    model.component("c").physics("plas").prop("ConsistentStabilization").set("StreamlineDiffusionIonsLog", false);
    model.component("c").physics("plas").prop("ConsistentStabilization").set("CrosswindDiffusionIonsLog", false);

    model.param().set("tEnd", "1000[ns]");

    model.study("std").feature("time").set("tlist", "range(0,2[ns],tEnd)");

    model.sol("sol1").feature("t1").set("tlist", "range(0,2[ns],tEnd)");
    model.sol("sol1").feature("v1").set("initmethod", "init");
    model.sol("sol1").feature("t1").set("consistent", "bweuler");

    model.label("P39_ready.mph");

    model.component("c").variable("gasv").set("JmassR", "-Dnum*d(rhoP,r)");
    model.component("c").variable("gasv").set("JmassZ", "-Dnum*d(rhoP,z)");
    model.component("c").variable("gasv").set("uMassR", "urP+JmassR/rhoSafe");
    model.component("c").variable("gasv").set("uMassZ", "uzP+JmassZ/rhoSafe");
    model.component("c").variable("gasv").set("divUMass", "d(uMassR,r)+uMassR/max(r,1e-12[m])+d(uMassZ,z)");

    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"axisWeight*(rhoP*urP/rho0)+axisWeight*JmassR/rho0", "axisWeight*(rhoP*uzP/rho0)+axisWeight*JmassZ/rho0"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))+axisWeight*urP*JmassR/(rho0*u0)", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))+axisWeight*urP*JmassZ/(rho0*u0)"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))+axisWeight*uzP*JmassR/(rho0*u0)", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))+axisWeight*uzP*JmassZ/(rho0*u0)"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))+axisWeight*(rho0*e0*qE+pPl)*JmassR/(rhoSafe*rho0*e0)", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))+axisWeight*(rho0*e0*qE+pPl)*JmassZ/(rhoSafe*rho0*e0)"}});
    model.component("c").physics("plas").feature("pes1").set("u", new String[]{"uMassR", "0", "uMassZ"});
    model.component("c").physics("plas").feature("N2").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("O2").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("N").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("O").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("N2_1p").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("N2_1p").featureInfo("info")
         .set("plas.um_wN2_1p", new String[]{"e_const*Dgas/(k_B_const*Tgas)"});
    model.component("c").physics("plas").feature("O2_1p").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("O2_1p").featureInfo("info")
         .set("plas.um_wO2_1p", new String[]{"e_const*Dgas/(k_B_const*Tgas)"});
    model.component("c").physics("plas").feature("N_1p").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("N_1p").featureInfo("info")
         .set("plas.um_wN_1p", new String[]{"e_const*Dgas/(k_B_const*Tgas)"});
    model.component("c").physics("plas").feature("O_1p").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("O_1p").featureInfo("info")
         .set("plas.um_wO_1p", new String[]{"e_const*Dgas/(k_B_const*Tgas)"});
    model.component("c").physics("plas").feature("O_1m").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("O_1m").featureInfo("info")
         .set("plas.um_wO_1m", new String[]{"e_const*Dgas/(k_B_const*Tgas)"});
    model.component("c").physics("plas").feature("O2_1m").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("O2_1m").featureInfo("info")
         .set("plas.um_wO2_1m", new String[]{"e_const*Dgas/(k_B_const*Tgas)"});
    model.component("c").physics("plas").feature("Cu").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("CuD5").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("CuD3").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("CuP1").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("CuP3").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("CuH3").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("CuH5").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("Cu_1p").set("Dfs", "Dgas+Dnum");
    model.component("c").physics("plas").feature("Cu_1p").featureInfo("info")
         .set("plas.um_wCu_1p", new String[]{"e_const*Dgas/(k_B_const*Tgas)"});
    model.component("c").physics("plas").prop("InconsistentStabilization").set("IsotropicDiffusionIons", false);
    model.component("c").physics("plas").feature("bulkconv")
         .set("Re", "-uMassR*d(plas.ne,r)-uMassZ*d(plas.ne,z)-plas.ne*divUMass+SneNumerical");
    model.component("c").physics("plas").feature("power")
         .set("Qgen", "Qlaser-QeRR-QeAttach-Qff-uMassR*d(Ue,r)-uMassZ*d(Ue,z)-(Ue+pe)*divUMass-Qei+1.5*e_const*TeV*SneNumerical");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "(vnSurf-uMassR*ncr-uMassZ*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");

    model.label("P40_ready.mph");

    model.component("c").mesh("mesh").create("sheath", "BndLayer");
    model.component("c").mesh("mesh").feature("sheath").selection().named("g_gas_dom");
    model.component("c").mesh("mesh").feature("sheath").create("layers", "BndLayerProp");
    model.component("c").mesh("mesh").feature("sheath").feature("layers").selection().named("beam");
    model.component("c").mesh("mesh").feature("sheath").feature("layers").set("inittype", "blhmin");
    model.component("c").mesh("mesh").feature("sheath").feature("layers").set("blhmin", "10[nm]");
    model.component("c").mesh("mesh").feature("sheath").feature("layers").set("blnlayers", 12);
    model.component("c").mesh("mesh").feature("sheath").feature("layers").set("blstretch", 1.3);
    model.component("c").mesh("mesh").run();

    model.label("P41_ready.mph");

    model.sol("sol1").feature("t1").feature("remesh").set("stopexpr", "c.spatial.relVolMin");
    model.sol("sol1").feature("t1").feature("remesh").set("stopval", "0.35");

    model.label("P42_ready.mph");

    model.component("c").physics("plas").feature("bulkconv").set("Re", "SneNumerical");
    model.component("c").physics("plas").feature("power")
         .set("Qgen", "Qlaser-QeRR-QeAttach-Qff-pe*divUMass-Qei+1.5*e_const*TeV*SneNumerical");
    model.component("c").physics("plas").create("conservativeElectronFlux", "WeakContribution", 2);
    model.component("c").physics("plas").feature("conservativeElectronFlux").selection().named("g_gas_dom");
    model.component("c").physics("plas").feature("conservativeElectronFlux")
         .set("weakExpression", "plas.ne*(uMassR*test(Ner)+uMassZ*test(Nez))+plas.en*(uMassR*test(Enr)+uMassZ*test(Enz))");
    model.component("c").physics("plas").feature("wallConv")
         .set("weakExpression", "vnSurf*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("plas").create("electronFarConv", "WeakContribution", 1);
    model.component("c").physics("plas").feature("electronFarConv").selection().named("far");
    model.component("c").physics("plas").feature("electronFarConv")
         .set("weakExpression", "-(uMassR*nr+uMassZ*nz)*(plas.ne*test(Ne)+plas.en*test(En))");
    model.component("c").physics("plas").prop("ConsistentStabilization")
         .set("StreamlineDiffusionElectronsLog", false);
    model.component("c").physics("plas").prop("ConsistentStabilization")
         .set("CrosswindDiffusionElectronsLog", false);

    model.label("P43_ready.mph");

    model.component("c").physics("plas").create("elasticAtomN", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("elasticAtomN").set("formula", "e+N=>e+N");
    model.component("c").physics("plas").feature("elasticAtomN").set("type", "Elastic");
    model.component("c").physics("plas").feature("elasticAtomN").set("kf", "kmCu*N_A_const");
    model.component("c").physics("plas").feature("elasticAtomN").set("mratio", "me_const*N_A_const/plas.M_wN");
    model.component("c").physics("plas").create("elasticAtomO", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("elasticAtomO").set("formula", "e+O=>e+O");
    model.component("c").physics("plas").feature("elasticAtomO").set("type", "Elastic");
    model.component("c").physics("plas").feature("elasticAtomO").set("kf", "kmCu*N_A_const");
    model.component("c").physics("plas").feature("elasticAtomO").set("mratio", "me_const*N_A_const/plas.M_wO");
    model.component("c").physics("plas").create("elasticAtomCu", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("elasticAtomCu").set("formula", "e+Cu=>e+Cu");
    model.component("c").physics("plas").feature("elasticAtomCu").set("type", "Elastic");
    model.component("c").physics("plas").feature("elasticAtomCu").set("kf", "kmCu*N_A_const");
    model.component("c").physics("plas").feature("elasticAtomCu").set("mratio", "me_const*N_A_const/plas.M_wCu");
    model.component("c").physics("plas").create("elasticAtomCuD5", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("elasticAtomCuD5").set("formula", "e+CuD5=>e+CuD5");
    model.component("c").physics("plas").feature("elasticAtomCuD5").set("type", "Elastic");
    model.component("c").physics("plas").feature("elasticAtomCuD5").set("kf", "kmCu*N_A_const");
    model.component("c").physics("plas").feature("elasticAtomCuD5").set("mratio", "me_const*N_A_const/plas.M_wCuD5");
    model.component("c").physics("plas").create("elasticAtomCuD3", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("elasticAtomCuD3").set("formula", "e+CuD3=>e+CuD3");
    model.component("c").physics("plas").feature("elasticAtomCuD3").set("type", "Elastic");
    model.component("c").physics("plas").feature("elasticAtomCuD3").set("kf", "kmCu*N_A_const");
    model.component("c").physics("plas").feature("elasticAtomCuD3").set("mratio", "me_const*N_A_const/plas.M_wCuD3");
    model.component("c").physics("plas").create("elasticAtomCuP1", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("elasticAtomCuP1").set("formula", "e+CuP1=>e+CuP1");
    model.component("c").physics("plas").feature("elasticAtomCuP1").set("type", "Elastic");
    model.component("c").physics("plas").feature("elasticAtomCuP1").set("kf", "kmCu*N_A_const");
    model.component("c").physics("plas").feature("elasticAtomCuP1").set("mratio", "me_const*N_A_const/plas.M_wCuP1");
    model.component("c").physics("plas").create("elasticAtomCuP3", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("elasticAtomCuP3").set("formula", "e+CuP3=>e+CuP3");
    model.component("c").physics("plas").feature("elasticAtomCuP3").set("type", "Elastic");
    model.component("c").physics("plas").feature("elasticAtomCuP3").set("kf", "kmCu*N_A_const");
    model.component("c").physics("plas").feature("elasticAtomCuP3").set("mratio", "me_const*N_A_const/plas.M_wCuP3");
    model.component("c").physics("plas").create("elasticAtomCuH3", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("elasticAtomCuH3").set("formula", "e+CuH3=>e+CuH3");
    model.component("c").physics("plas").feature("elasticAtomCuH3").set("type", "Elastic");
    model.component("c").physics("plas").feature("elasticAtomCuH3").set("kf", "kmCu*N_A_const");
    model.component("c").physics("plas").feature("elasticAtomCuH3").set("mratio", "me_const*N_A_const/plas.M_wCuH3");
    model.component("c").physics("plas").create("elasticAtomCuH5", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").feature("elasticAtomCuH5").set("formula", "e+CuH5=>e+CuH5");
    model.component("c").physics("plas").feature("elasticAtomCuH5").set("type", "Elastic");
    model.component("c").physics("plas").feature("elasticAtomCuH5").set("kf", "kmCu*N_A_const");
    model.component("c").physics("plas").feature("elasticAtomCuH5").set("mratio", "me_const*N_A_const/plas.M_wCuH5");

    model.component("c").variable("gasv")
         .set("Qei", "3*me_const*N_A_const*plas.ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*(0[mol/(kg*m^3)]+plas.n_wN2_1p/plas.M_wN2_1p+plas.n_wO2_1p/plas.M_wO2_1p+plas.n_wN_1p/plas.M_wN_1p+plas.n_wO_1p/plas.M_wO_1p+plas.n_wO_1m/plas.M_wO_1m+plas.n_wO2_1m/plas.M_wO2_1m+plas.n_wCu_1p/plas.M_wCu_1p)");

    model.label("P44_ready.mph");

    model.component("c").physics("plas").feature("metal").set("constraintOptions", "weakConstraints");

    model.component("c").variable("boundv")
         .set("surfaceAreaRate", "dtang(d(r,TIME),r)+dtang(d(z,TIME),z)+d(r,TIME)/max(r,1e-12[m])");

    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.I_1", new String[]{"-plas.metal.int(2*pi*r*plas.d*(d(V_lm,TIME)+V_lm*surfaceAreaRate+Iwall))"});

    model.sol().remove("sol1");
    model.sol().remove("sol2");
    model.sol().remove("restartPhysical");

    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").feature().remove("se1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");

    return model;
  }

  public static Model run21(Model model) {
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.study("std").feature("time").set("rtol", 1.0E-4);

    model.sol("sol1").feature("t1").set("rtol", 1.0E-4);
    model.sol("sol1").feature("t1").set("consistent", "bweuler");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_spatial_disp").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "100");
    model.sol("sol1").feature("v1").feature("c_V_lm").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V_lm").set("scaleval", "1e-5");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "0.001");
    model.sol("sol1").feature("t1").create("remesh", "AutoRemesh");
    model.sol("sol1").feature("t1").feature("remesh").set("autoremeshgeom", "g");
    model.sol("sol1").feature("t1").feature("remesh").set("stopcondtype", "quality");
    model.sol("sol1").feature("t1").feature("remesh").set("stopexpr", "c.spatial.minqual");
    model.sol("sol1").feature("t1").feature("remesh").set("stopval", "0.25");
    model.sol("sol1").feature("t1").feature("remesh").set("initialstepremeshactive", true);
    model.sol("sol1").feature("t1").feature("remesh").set("initialstepremesh", "0.001[ns]");
    model.sol("sol1").feature("t1").feature("remesh").set("stopexpr", "c.spatial.relVolMin");
    model.sol("sol1").feature("t1").feature("remesh").set("stopval", "0.35");
    model.sol("sol1").feature("t1").feature("remesh").set("consistentremesh", true);

    model.label("P49_ready.mph");

    model.param().set("traceIota", "2");

    model.label("P55_ready.mph");

    model.component("c").variable("boundv").set("lineAreaRate", "dtang(d(r,TIME),r)+dtang(d(z,TIME),z)");

    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.I_1", new String[]{"-plas.metal.int(d(V_lm,TIME)+V_lm*lineAreaRate+2*pi*r*plas.d*Iwall)"});
    model.component("c").physics("plas").create("metalReactionCorrection", "WeakContribution", 1);
    model.component("c").physics("plas").feature("metalReactionCorrection").selection().named("surface");
    model.component("c").physics("plas").feature("metalReactionCorrection")
         .set("weakExpression", "V_lm*test(plas.metal.V0e_1)/(2*pi*max(r,1e-12[m]))");

    model.sol("sol1").feature("v1").feature("c_V_lm").set("scaleval", "1e-10");

    model.param().set("traceIota", "2");

    model.label("P59_ready.mph");
    model.label("P59.mph");

    model.sol("sol2").getPVals();
    model.sol("sol2").getPVals();
    model.sol("sol2").copySolution("continued");

    model.param().set("tEnd", "1000[ns]");

    model.study("std").feature("time").set("tlist", "range(62[ns],0.2[ns],210[ns]) range(212[ns],2[ns],1000[ns])");

    model.sol("sol1").feature("t1").set("tlist", "range(62[ns],0.2[ns],210[ns]) range(212[ns],2[ns],1000[ns])");
    model.sol("sol1").feature("v1").set("control", "user");
    model.sol("sol1").feature("v1").set("initmethod", "sol");
    model.sol("sol1").feature("v1").set("initsol", "continued");
    model.sol("sol1").feature("v1").set("solnum", "interp");
    model.sol("sol1").feature("v1").set("t", "62[ns]");
    model.sol("sol1").feature("t1").set("consistent", "bweuler");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");

    model.label("P60_ready.mph");

    model.component("c").physics("plas").prop("TransportSettings").set("MixtureDiffusionCorrection", true);
    model.component("c").physics("plas").feature("N2").featureInfo("info")
         .set("plas.tfluxr_wN2", new String[]{"plas.cfluxr_wN2+plas.jr_wN2"});
    model.component("c").physics("plas").feature("N2").featureInfo("info")
         .set("plas.tfluxz_wN2", new String[]{"plas.cfluxz_wN2+plas.jz_wN2"});
    model.component("c").physics("plas").feature("O2").featureInfo("info")
         .set("plas.tfluxr_wO2", new String[]{"plas.cfluxr_wO2+plas.jr_wO2"});
    model.component("c").physics("plas").feature("O2").featureInfo("info")
         .set("plas.tfluxz_wO2", new String[]{"plas.cfluxz_wO2+plas.jz_wO2"});
    model.component("c").physics("plas").feature("N").featureInfo("info")
         .set("plas.tfluxr_wN", new String[]{"plas.cfluxr_wN+plas.jr_wN"});
    model.component("c").physics("plas").feature("N").featureInfo("info")
         .set("plas.tfluxz_wN", new String[]{"plas.cfluxz_wN+plas.jz_wN"});
    model.component("c").physics("plas").feature("O").featureInfo("info")
         .set("plas.tfluxr_wO", new String[]{"plas.cfluxr_wO+plas.jr_wO"});
    model.component("c").physics("plas").feature("O").featureInfo("info")
         .set("plas.tfluxz_wO", new String[]{"plas.cfluxz_wO+plas.jz_wO"});
    model.component("c").physics("plas").feature("N2_1p").featureInfo("info")
         .set("plas.tfluxr_wN2_1p", new String[]{"plas.cfluxr_wN2_1p+plas.jr_wN2_1p"});
    model.component("c").physics("plas").feature("N2_1p").featureInfo("info")
         .set("plas.tfluxz_wN2_1p", new String[]{"plas.cfluxz_wN2_1p+plas.jz_wN2_1p"});
    model.component("c").physics("plas").feature("O2_1p").featureInfo("info")
         .set("plas.tfluxr_wO2_1p", new String[]{"plas.cfluxr_wO2_1p+plas.jr_wO2_1p"});
    model.component("c").physics("plas").feature("O2_1p").featureInfo("info")
         .set("plas.tfluxz_wO2_1p", new String[]{"plas.cfluxz_wO2_1p+plas.jz_wO2_1p"});
    model.component("c").physics("plas").feature("N_1p").featureInfo("info")
         .set("plas.tfluxr_wN_1p", new String[]{"plas.cfluxr_wN_1p+plas.jr_wN_1p"});
    model.component("c").physics("plas").feature("N_1p").featureInfo("info")
         .set("plas.tfluxz_wN_1p", new String[]{"plas.cfluxz_wN_1p+plas.jz_wN_1p"});
    model.component("c").physics("plas").feature("O_1p").featureInfo("info")
         .set("plas.tfluxr_wO_1p", new String[]{"plas.cfluxr_wO_1p+plas.jr_wO_1p"});
    model.component("c").physics("plas").feature("O_1p").featureInfo("info")
         .set("plas.tfluxz_wO_1p", new String[]{"plas.cfluxz_wO_1p+plas.jz_wO_1p"});
    model.component("c").physics("plas").feature("O_1m").featureInfo("info")
         .set("plas.tfluxr_wO_1m", new String[]{"plas.cfluxr_wO_1m+plas.jr_wO_1m"});
    model.component("c").physics("plas").feature("O_1m").featureInfo("info")
         .set("plas.tfluxz_wO_1m", new String[]{"plas.cfluxz_wO_1m+plas.jz_wO_1m"});
    model.component("c").physics("plas").feature("O2_1m").featureInfo("info")
         .set("plas.tfluxr_wO2_1m", new String[]{"plas.cfluxr_wO2_1m+plas.jr_wO2_1m"});
    model.component("c").physics("plas").feature("O2_1m").featureInfo("info")
         .set("plas.tfluxz_wO2_1m", new String[]{"plas.cfluxz_wO2_1m+plas.jz_wO2_1m"});
    model.component("c").physics("plas").feature("Cu").featureInfo("info")
         .set("plas.tfluxr_wCu", new String[]{"plas.cfluxr_wCu+plas.jr_wCu"});
    model.component("c").physics("plas").feature("Cu").featureInfo("info")
         .set("plas.tfluxz_wCu", new String[]{"plas.cfluxz_wCu+plas.jz_wCu"});
    model.component("c").physics("plas").feature("CuD5").featureInfo("info")
         .set("plas.tfluxr_wCuD5", new String[]{"plas.cfluxr_wCuD5+plas.jr_wCuD5"});
    model.component("c").physics("plas").feature("CuD5").featureInfo("info")
         .set("plas.tfluxz_wCuD5", new String[]{"plas.cfluxz_wCuD5+plas.jz_wCuD5"});
    model.component("c").physics("plas").feature("CuD3").featureInfo("info")
         .set("plas.tfluxr_wCuD3", new String[]{"plas.cfluxr_wCuD3+plas.jr_wCuD3"});
    model.component("c").physics("plas").feature("CuD3").featureInfo("info")
         .set("plas.tfluxz_wCuD3", new String[]{"plas.cfluxz_wCuD3+plas.jz_wCuD3"});
    model.component("c").physics("plas").feature("CuP1").featureInfo("info")
         .set("plas.tfluxr_wCuP1", new String[]{"plas.cfluxr_wCuP1+plas.jr_wCuP1"});
    model.component("c").physics("plas").feature("CuP1").featureInfo("info")
         .set("plas.tfluxz_wCuP1", new String[]{"plas.cfluxz_wCuP1+plas.jz_wCuP1"});
    model.component("c").physics("plas").feature("CuP3").featureInfo("info")
         .set("plas.tfluxr_wCuP3", new String[]{"plas.cfluxr_wCuP3+plas.jr_wCuP3"});
    model.component("c").physics("plas").feature("CuP3").featureInfo("info")
         .set("plas.tfluxz_wCuP3", new String[]{"plas.cfluxz_wCuP3+plas.jz_wCuP3"});
    model.component("c").physics("plas").feature("CuH3").featureInfo("info")
         .set("plas.tfluxr_wCuH3", new String[]{"plas.cfluxr_wCuH3+plas.jr_wCuH3"});
    model.component("c").physics("plas").feature("CuH3").featureInfo("info")
         .set("plas.tfluxz_wCuH3", new String[]{"plas.cfluxz_wCuH3+plas.jz_wCuH3"});
    model.component("c").physics("plas").feature("CuH5").featureInfo("info")
         .set("plas.tfluxr_wCuH5", new String[]{"plas.cfluxr_wCuH5+plas.jr_wCuH5"});
    model.component("c").physics("plas").feature("CuH5").featureInfo("info")
         .set("plas.tfluxz_wCuH5", new String[]{"plas.cfluxz_wCuH5+plas.jz_wCuH5"});
    model.component("c").physics("plas").feature("Cu_1p").featureInfo("info")
         .set("plas.tfluxr_wCu_1p", new String[]{"plas.cfluxr_wCu_1p+plas.jr_wCu_1p"});
    model.component("c").physics("plas").feature("Cu_1p").featureInfo("info")
         .set("plas.tfluxz_wCu_1p", new String[]{"plas.cfluxz_wCu_1p+plas.jz_wCu_1p"});
    model.component("c").physics("plas").create("wallQuadrature2", "WeakContribution", 1);
    model.component("c").physics("plas").feature("wallQuadrature2").selection().named("surface");
    model.component("c").physics("plas").feature("wallQuadrature2").set("automaticQuadrature", false);
    model.component("c").physics("plas").feature("wallQuadrature2").set("integrationOrder", 2);
    model.component("c").physics("plas").feature("wallQuadrature2")
         .set("weakExpression", "-((plas.ne_bnd+plas.tne_bnd+plas.nemig_bnd)*test(Ne)+(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)*test(En))");
    model.component("c").physics("plas").create("wallQuadrature8", "WeakContribution", 1);
    model.component("c").physics("plas").feature("wallQuadrature8").selection().named("surface");
    model.component("c").physics("plas").feature("wallQuadrature8").set("automaticQuadrature", false);
    model.component("c").physics("plas").feature("wallQuadrature8").set("integrationOrder", 8);
    model.component("c").physics("plas").feature("wallQuadrature8")
         .set("weakExpression", "((plas.ne_bnd+plas.tne_bnd+plas.nemig_bnd)*test(Ne)+(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)*test(En))");

    model.component("c").cpl("intSurf").set("intorder", 8);
    model.component("c").cpl().create("metalLine", "Integration");
    model.component("c").cpl("metalLine").set("opname", "metalLine");
    model.component("c").cpl("metalLine").selection().geom("g", 1);
    model.component("c").cpl("metalLine").selection().named("surface");
    model.component("c").cpl("metalLine").set("axisym", false);
    model.component("c").cpl("metalLine").set("intorder", 8);

    model.component("c").physics("plas").feature("metal").featureInfo("info")
         .set("plas.I_1", new String[]{"-metalLine(d(V_lm,TIME)+V_lm*lineAreaRate)-intSurf(Iwall)"});
    model.component("c").physics("plas").feature("wallConv").set("automaticQuadrature", false);
    model.component("c").physics("plas").feature("wallConv").set("integrationOrder", 8);
    model.component("c").physics("plas").feature("metalReactionCorrection").set("automaticQuadrature", false);
    model.component("c").physics("plas").feature("metalReactionCorrection").set("integrationOrder", 8);

    model.label("P61_ready.mph");

    model.component("c").mesh("mesh").feature("sheath").feature("layers").set("blhmin", "2[nm]");
    model.component("c").mesh("mesh").feature("sheath").feature("layers").set("blnlayers", 18);
    model.component("c").mesh("mesh").run();

    model.label("P63_ready.mph");
    model.label("P63.mph");

    model.sol("sol2").getPVals();
    model.sol("sol2").copySolution("heavyInit");

    model.component("c").physics("plas").create("heavyConservative", "WeakContribution", 2);
    model.component("c").physics("plas").feature("heavyConservative").selection().named("g_gas_dom");
    model.component("c").physics("plas").feature("heavyConservative")
         .set("weakExpression", "0+(-plas.wO2*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wO2,r)+uMassZ*d(plas.wO2,z)))*test(WO2)+rhoP*plas.wO2*(uMassR*test(WO2r)+uMassZ*test(WO2z))+(-plas.wN*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wN,r)+uMassZ*d(plas.wN,z)))*test(WN)+rhoP*plas.wN*(uMassR*test(WNr)+uMassZ*test(WNz))+(-plas.wO*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wO,r)+uMassZ*d(plas.wO,z)))*test(WO)+rhoP*plas.wO*(uMassR*test(WOr)+uMassZ*test(WOz))+(-plas.wN2_1p*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wN2_1p,r)+uMassZ*d(plas.wN2_1p,z)))*test(WN2_1p)+rhoP*plas.wN2_1p*(uMassR*test(WN2_1pr)+uMassZ*test(WN2_1pz))+(-plas.wO2_1p*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wO2_1p,r)+uMassZ*d(plas.wO2_1p,z)))*test(WO2_1p)+rhoP*plas.wO2_1p*(uMassR*test(WO2_1pr)+uMassZ*test(WO2_1pz))+(-plas.wN_1p*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wN_1p,r)+uMassZ*d(plas.wN_1p,z)))*test(WN_1p)+rhoP*plas.wN_1p*(uMassR*test(WN_1pr)+uMassZ*test(WN_1pz))+(-plas.wO_1p*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wO_1p,r)+uMassZ*d(plas.wO_1p,z)))*test(WO_1p)+rhoP*plas.wO_1p*(uMassR*test(WO_1pr)+uMassZ*test(WO_1pz))+(-plas.wO_1m*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wO_1m,r)+uMassZ*d(plas.wO_1m,z)))*test(WO_1m)+rhoP*plas.wO_1m*(uMassR*test(WO_1mr)+uMassZ*test(WO_1mz))+(-plas.wO2_1m*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wO2_1m,r)+uMassZ*d(plas.wO2_1m,z)))*test(WO2_1m)+rhoP*plas.wO2_1m*(uMassR*test(WO2_1mr)+uMassZ*test(WO2_1mz))+(-plas.wCu*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wCu,r)+uMassZ*d(plas.wCu,z)))*test(WCu)+rhoP*plas.wCu*(uMassR*test(WCur)+uMassZ*test(WCuz))+(-plas.wCuD5*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wCuD5,r)+uMassZ*d(plas.wCuD5,z)))*test(WCuD5)+rhoP*plas.wCuD5*(uMassR*test(WCuD5r)+uMassZ*test(WCuD5z))+(-plas.wCuD3*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wCuD3,r)+uMassZ*d(plas.wCuD3,z)))*test(WCuD3)+rhoP*plas.wCuD3*(uMassR*test(WCuD3r)+uMassZ*test(WCuD3z))+(-plas.wCuP1*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wCuP1,r)+uMassZ*d(plas.wCuP1,z)))*test(WCuP1)+rhoP*plas.wCuP1*(uMassR*test(WCuP1r)+uMassZ*test(WCuP1z))+(-plas.wCuP3*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wCuP3,r)+uMassZ*d(plas.wCuP3,z)))*test(WCuP3)+rhoP*plas.wCuP3*(uMassR*test(WCuP3r)+uMassZ*test(WCuP3z))+(-plas.wCuH3*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wCuH3,r)+uMassZ*d(plas.wCuH3,z)))*test(WCuH3)+rhoP*plas.wCuH3*(uMassR*test(WCuH3r)+uMassZ*test(WCuH3z))+(-plas.wCuH5*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wCuH5,r)+uMassZ*d(plas.wCuH5,z)))*test(WCuH5)+rhoP*plas.wCuH5*(uMassR*test(WCuH5r)+uMassZ*test(WCuH5z))+(-plas.wCu_1p*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d(plas.wCu_1p,r)+uMassZ*d(plas.wCu_1p,z)))*test(WCu_1p)+rhoP*plas.wCu_1p*(uMassR*test(WCu_1pr)+uMassZ*test(WCu_1pz))");
    model.component("c").physics("plas").create("heavyFarConvection", "WeakContribution", 1);
    model.component("c").physics("plas").feature("heavyFarConvection").selection().named("far");
    model.component("c").physics("plas").feature("heavyFarConvection")
         .set("weakExpression", "0-rhoP*plas.wO2*(uMassR*nr+uMassZ*nz)*test(WO2)-rhoP*plas.wN*(uMassR*nr+uMassZ*nz)*test(WN)-rhoP*plas.wO*(uMassR*nr+uMassZ*nz)*test(WO)-rhoP*plas.wN2_1p*(uMassR*nr+uMassZ*nz)*test(WN2_1p)-rhoP*plas.wO2_1p*(uMassR*nr+uMassZ*nz)*test(WO2_1p)-rhoP*plas.wN_1p*(uMassR*nr+uMassZ*nz)*test(WN_1p)-rhoP*plas.wO_1p*(uMassR*nr+uMassZ*nz)*test(WO_1p)-rhoP*plas.wO_1m*(uMassR*nr+uMassZ*nz)*test(WO_1m)-rhoP*plas.wO2_1m*(uMassR*nr+uMassZ*nz)*test(WO2_1m)-rhoP*plas.wCu*(uMassR*nr+uMassZ*nz)*test(WCu)-rhoP*plas.wCuD5*(uMassR*nr+uMassZ*nz)*test(WCuD5)-rhoP*plas.wCuD3*(uMassR*nr+uMassZ*nz)*test(WCuD3)-rhoP*plas.wCuP1*(uMassR*nr+uMassZ*nz)*test(WCuP1)-rhoP*plas.wCuP3*(uMassR*nr+uMassZ*nz)*test(WCuP3)-rhoP*plas.wCuH3*(uMassR*nr+uMassZ*nz)*test(WCuH3)-rhoP*plas.wCuH5*(uMassR*nr+uMassZ*nz)*test(WCuH5)-rhoP*plas.wCu_1p*(uMassR*nr+uMassZ*nz)*test(WCu_1p)");
    model.component("c").physics("plas").feature("feed").set("IncludeConvection", false);

    model.study("std").feature("time").set("tlist", "range(90.2[ns],0.2[ns],123[ns])");

    model.sol("sol1").feature("t1").set("tlist", "range(90.2[ns],0.2[ns],123[ns])");
    model.sol("sol1").feature("v1").set("control", "user");
    model.sol("sol1").feature("v1").set("initmethod", "sol");
    model.sol("sol1").feature("v1").set("initsol", "heavyInit");
    model.sol("sol1").feature("v1").set("solnum", "interp");
    model.sol("sol1").feature("v1").set("t", "90.2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "bweuler");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");

    model.label("P73_1.mph");

    model.component("c").variable("boundv").set("vnPhysical", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("c").variable("boundv").set("vnSurf", "d(r,TIME)*ncr+d(z,TIME)*ncz");

    model.label("P73_2.mph");

    model.component("c").common("deform").set("smoothingType", "yeoh");

    model.label("P73_3.mph");

    model.component("c").physics("surfaceODE").prop("ShapeProperty").set("order", 2);

    model.component("c").sorder("quadratic");

    model.label("P73_4.mph");

    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "const");
    model.sol("sol1").feature("t1").feature("coupledAll").set("damp", 1);
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 10);

    model.label("P73_ready.mph");

    model.component("c").cpl().create("surfaceHeight", "GeneralExtrusion");
    model.component("c").cpl("surfaceHeight").selection().geom("g", 1);
    model.component("c").cpl("surfaceHeight").selection().named("surface");
    model.component("c").cpl("surfaceHeight").set("opname", "surfaceHeight");
    model.component("c").cpl("surfaceHeight").set("srcframe", "material");
    model.component("c").cpl("surfaceHeight").set("usesrcmap", true);
    model.component("c").cpl("surfaceHeight").set("srcmap", new String[]{"R", "0"});
    model.component("c").cpl("surfaceHeight").set("dstmap", new String[]{"R", "0"});

    model.component("c").common().remove("deform");
    model.component("c").common().remove("meshFixed");
    model.component("c").common().remove("meshAxis");
    model.component("c").common().remove("meshSurf");
    model.component("c").common().create("columnMesh", "PrescribedDeformation");
    model.component("c").common("columnMesh").selection().all();
    model.component("c").common("columnMesh")
         .set("prescribedDeformation", new String[]{"0", "0", "surfaceHeight(etaSurface)*1[um]*if(Z>=0,1-Z/Zdom,1+Z/Dcu)"});

    model.study("std").feature("time")
         .set("tlist", "range(0,2[ns],90[ns]) range(90.2[ns],0.2[ns],123[ns]) range(123.5[ns],0.5[ns],203[ns])");

    model.sol("sol1").feature("t1")
         .set("tlist", "range(0,2[ns],90[ns]) range(90.2[ns],0.2[ns],123[ns]) range(123.5[ns],0.5[ns],203[ns])");
    model.sol("sol1").feature("v1").set("initmethod", "init");
    model.sol("sol1").feature("t1").feature("remesh").active(false);

    model.label("P84_ready.mph");
    model.label("P84.mph");

    model.sol("sol1").getPVals();
    model.sol("sol2").getPVals();
    model.sol("sol2").getPVals();
    model.sol("sol1").getPVals();
    model.sol("sol1").copySolution("continued");

    model.param().set("tEnd", "1000[ns]");

    model.study("std").feature("time")
         .set("tlist", "range(90.6[ns],0.2[ns],123[ns]) range(123.5[ns],0.5[ns],203[ns])");

    model.sol("sol1").feature("t1").set("tlist", "range(90.6[ns],0.2[ns],123[ns]) range(123.5[ns],0.5[ns],203[ns])");
    model.sol("sol1").feature("v1").set("control", "user");
    model.sol("sol1").feature("v1").set("initmethod", "sol");
    model.sol("sol1").feature("v1").set("initsol", "continued");
    model.sol("sol1").feature("v1").set("solnum", "interp");
    model.sol("sol1").feature("v1").set("t", "90.6[ns]");
    model.sol("sol1").feature("t1").set("consistent", "bweuler");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");

    model.label("P85_ready.mph");

    model.sol().remove("sol1");
    model.sol().remove("sol2");
    model.sol().remove("continued");
    model.sol().remove("heavyInit");

    model.component("c").geom("g").create("wallstrip", "Rectangle");
    model.component("c").geom("g").feature("wallstrip").set("pos", new String[]{"0", "0"});
    model.component("c").geom("g").feature("wallstrip").set("size", new String[]{"60[um]", "2[um]"});
    model.component("c").geom("g").feature("wallstrip").set("selresult", true);
    model.component("c").geom("g").run();

    model.component("c").selection().create("mapTop", "Box");
    model.component("c").selection("mapTop").set("entitydim", 1);
    model.component("c").selection("mapTop").set("xmin", "-1e-12");
    model.component("c").selection("mapTop").set("xmax", "60[um]+1e-12[m]");
    model.component("c").selection("mapTop").set("ymin", "2[um]-1e-12[m]");
    model.component("c").selection("mapTop").set("ymax", "2[um]+1e-12[m]");
    model.component("c").selection("mapTop").set("condition", "inside");
    model.component("c").selection().create("mapLeft", "Box");
    model.component("c").selection("mapLeft").set("entitydim", 1);
    model.component("c").selection("mapLeft").set("xmin", "-1e-12");
    model.component("c").selection("mapLeft").set("xmax", "1e-12");
    model.component("c").selection("mapLeft").set("ymin", "-1e-12");
    model.component("c").selection("mapLeft").set("ymax", "2[um]+1e-12[m]");
    model.component("c").selection("mapLeft").set("condition", "inside");
    model.component("c").selection().create("mapRight", "Box");
    model.component("c").selection("mapRight").set("entitydim", 1);
    model.component("c").selection("mapRight").set("xmin", "59.999[um]");
    model.component("c").selection("mapRight").set("xmax", "60.001[um]");
    model.component("c").selection("mapRight").set("ymin", "-0.001[um]");
    model.component("c").selection("mapRight").set("ymax", "2.001[um]");
    model.component("c").selection("mapRight").set("condition", "inside");
    model.component("c").selection().create("mapRadial", "Union");
    model.component("c").selection("mapRadial").set("entitydim", 1);
    model.component("c").selection("mapRadial").set("input", new String[]{"beam", "mapTop"});
    model.component("c").selection().create("mapVertical", "Union");
    model.component("c").selection("mapVertical").set("entitydim", 1);
    model.component("c").selection("mapVertical").set("input", new String[]{"mapLeft", "mapRight"});

    model.component("c").mesh("mesh").feature().remove("sheath");
    model.component("c").mesh("mesh").feature().remove("edge");
    model.component("c").mesh("mesh").feature().remove("tri");
    model.component("c").mesh("mesh").create("normalLeft", "Edge");
    model.component("c").mesh("mesh").feature("normalLeft").selection().named("mapLeft");
    model.component("c").mesh("mesh").feature("normalLeft").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("normalLeft").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("normalLeft").feature("dist").set("elemcount", 25);
    model.component("c").mesh("mesh").feature("normalLeft").feature("dist").set("elemratio", 210);
    model.component("c").mesh("mesh").feature("normalLeft").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("normalLeft").feature("dist").set("reverse", false);
    model.component("c").mesh("mesh").create("normalRight", "Edge");
    model.component("c").mesh("mesh").feature("normalRight").selection().named("mapRight");
    model.component("c").mesh("mesh").feature("normalRight").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("normalRight").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("normalRight").feature("dist").set("elemcount", 25);
    model.component("c").mesh("mesh").feature("normalRight").feature("dist").set("elemratio", 210);
    model.component("c").mesh("mesh").feature("normalRight").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("normalRight").feature("dist").set("reverse", true);
    model.component("c").mesh("mesh").create("wallMap", "Map");
    model.component("c").mesh("mesh").feature("wallMap").selection().named("g_wallstrip_dom");
    model.component("c").mesh("mesh").feature("wallMap").create("radial", "Distribution");
    model.component("c").mesh("mesh").feature("wallMap").feature("radial").selection().named("mapRadial");
    model.component("c").mesh("mesh").feature("wallMap").feature("radial").set("type", "number");
    model.component("c").mesh("mesh").feature("wallMap").feature("radial").set("numelem", 120);
    model.component("c").mesh("mesh").create("tri", "FreeTri");
    model.component("c").mesh("mesh").run();

    model.component("c").sorder("quadratic");

    return model;
  }

  public static Model run22(Model model) {

    model.component("c").physics("surfaceODE").prop("ShapeProperty").set("shapeFunctionType", "shlag");
    model.component("c").physics("surfaceODE").prop("ShapeProperty").set("order", 2);

    model.param().set("w0", "11[um]");
    model.param().set("meltOn", "1");
    model.param().set("tEnd", "203[ns]");

    model.study("std").feature("time").set("tlist", "range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)");
    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").feature().remove("fc1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 12);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("maxstepbdf", "2[ns]");
    model.sol("sol1").feature("t1").set("consistent", "off");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e6");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "10");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");
    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("rtol", 0.003);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "1[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").set("initmethod", "init");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Tg").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "100");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V_lm").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V_lm").set("scaleval", "1e-10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");

    model.label("Mapped shared-column moving surface: prepared, not yet solved");
    model.label("M01_structure_ready.mph");

    model.component("c").physics("temp").field("dimensionless").field("logTgas");
    model.component("c").physics("temp").field("dimensionless").component(new String[]{"logTgas"});
    model.component("c").physics("temp").feature("init1").set("logTgas", "log(Tamb/1[K])");

    model.component("c").variable("gasv").set("Tg", "1[K]*exp(logTgas)");
    model.component("c").variable("gasv").set("Tgas", "Tg");

    model.component("c").physics("plas").create("heavyFeedQuadrature2", "WeakContribution", 1);
    model.component("c").physics("plas").feature("heavyFeedQuadrature2").selection().named("surface");
    model.component("c").physics("plas").feature("heavyFeedQuadrature2").set("automaticQuadrature", false);
    model.component("c").physics("plas").feature("heavyFeedQuadrature2").set("integrationOrder", 2);
    model.component("c").physics("plas").feature("heavyFeedQuadrature2")
         .set("weakExpression", "-((plas.influx_N2_1p*test(WN2_1p))+(plas.influx_O2*test(WO2))+(plas.influx_O*test(WO))+(plas.influx_O_1m*test(WO_1m))+(plas.influx_O2_1p*test(WO2_1p))+(plas.influx_Cu*test(WCu))+(plas.influx_CuD5*test(WCuD5))+(plas.influx_CuD3*test(WCuD3))+(plas.influx_CuP1*test(WCuP1))+(plas.influx_CuP3*test(WCuP3))+(plas.influx_CuH3*test(WCuH3))+(plas.influx_CuH5*test(WCuH5))+(plas.influx_Cu_1p*test(WCu_1p))+(plas.influx_N*test(WN))+(plas.influx_N_1p*test(WN_1p))+(plas.influx_O_1p*test(WO_1p))+(plas.influx_O2_1m*test(WO2_1m)))");
    model.component("c").physics("plas").create("heavyFeedQuadrature8", "WeakContribution", 1);
    model.component("c").physics("plas").feature("heavyFeedQuadrature8").selection().named("surface");
    model.component("c").physics("plas").feature("heavyFeedQuadrature8").set("automaticQuadrature", false);
    model.component("c").physics("plas").feature("heavyFeedQuadrature8").set("integrationOrder", 8);
    model.component("c").physics("plas").feature("heavyFeedQuadrature8")
         .set("weakExpression", "((plas.influx_N2_1p*test(WN2_1p))+(plas.influx_O2*test(WO2))+(plas.influx_O*test(WO))+(plas.influx_O_1m*test(WO_1m))+(plas.influx_O2_1p*test(WO2_1p))+(plas.influx_Cu*test(WCu))+(plas.influx_CuD5*test(WCuD5))+(plas.influx_CuD3*test(WCuD3))+(plas.influx_CuP1*test(WCuP1))+(plas.influx_CuP3*test(WCuP3))+(plas.influx_CuH3*test(WCuH3))+(plas.influx_CuH5*test(WCuH5))+(plas.influx_Cu_1p*test(WCu_1p))+(plas.influx_N*test(WN))+(plas.influx_N_1p*test(WN_1p))+(plas.influx_O_1p*test(WO_1p))+(plas.influx_O2_1m*test(WO2_1m)))");
    model.component("c").physics("plas").feature("N2").set("FromMassConstraint", false);
    model.component("c").physics("plas").feature("N2").set("x0", "0.79");

    model.component("c").variable("gasv")
         .set("compositionDen", "0+exp(WN2)+exp(WO2)+exp(WN)+exp(WO)+exp(WN2_1p)+exp(WO2_1p)+exp(WN_1p)+exp(WO_1p)+exp(WO_1m)+exp(WO2_1m)+exp(WCu)+exp(WCuD5)+exp(WCuD3)+exp(WCuP1)+exp(WCuP3)+exp(WCuH3)+exp(WCuH5)+exp(WCu_1p)");
    model.component("c").variable("gasv").set("rhoP", "rho0*compositionDen");
    model.component("c").variable("gasv").set("urP", "rho0*u0*qMr/rhoP");
    model.component("c").variable("gasv").set("uzP", "rho0*u0*qMz/rhoP");
    model.component("c").variable("boundv").set("QworkWall", "workCu*Iwall");

    model.component("c").physics("plas").feature("N2").featureInfo("info")
         .set("plas.wN2", new String[]{"exp(WN2)/compositionDen"});
    model.component("c").physics("plas").feature("N2").featureInfo("info")
         .set("plas.w0_wN2", new String[]{"plas.x0_wN2*plas.M_wN2/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("O2").featureInfo("info")
         .set("plas.wO2", new String[]{"exp(WO2)/compositionDen"});
    model.component("c").physics("plas").feature("O2").featureInfo("info")
         .set("plas.w0_wO2", new String[]{"plas.x0_wO2*plas.M_wO2/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("N").featureInfo("info")
         .set("plas.wN", new String[]{"exp(WN)/compositionDen"});
    model.component("c").physics("plas").feature("N").featureInfo("info")
         .set("plas.w0_wN", new String[]{"plas.x0_wN*plas.M_wN/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("O").featureInfo("info")
         .set("plas.wO", new String[]{"exp(WO)/compositionDen"});
    model.component("c").physics("plas").feature("O").featureInfo("info")
         .set("plas.w0_wO", new String[]{"plas.x0_wO*plas.M_wO/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("N2_1p").featureInfo("info")
         .set("plas.wN2_1p", new String[]{"exp(WN2_1p)/compositionDen"});
    model.component("c").physics("plas").feature("N2_1p").featureInfo("info")
         .set("plas.w0_wN2_1p", new String[]{"plas.x0_wN2_1p*plas.M_wN2_1p/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("O2_1p").featureInfo("info")
         .set("plas.wO2_1p", new String[]{"exp(WO2_1p)/compositionDen"});
    model.component("c").physics("plas").feature("O2_1p").featureInfo("info")
         .set("plas.w0_wO2_1p", new String[]{"plas.x0_wO2_1p*plas.M_wO2_1p/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("N_1p").featureInfo("info")
         .set("plas.wN_1p", new String[]{"exp(WN_1p)/compositionDen"});
    model.component("c").physics("plas").feature("N_1p").featureInfo("info")
         .set("plas.w0_wN_1p", new String[]{"plas.x0_wN_1p*plas.M_wN_1p/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("O_1p").featureInfo("info")
         .set("plas.wO_1p", new String[]{"exp(WO_1p)/compositionDen"});
    model.component("c").physics("plas").feature("O_1p").featureInfo("info")
         .set("plas.w0_wO_1p", new String[]{"plas.x0_wO_1p*plas.M_wO_1p/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("O_1m").featureInfo("info")
         .set("plas.wO_1m", new String[]{"exp(WO_1m)/compositionDen"});
    model.component("c").physics("plas").feature("O_1m").featureInfo("info")
         .set("plas.w0_wO_1m", new String[]{"plas.x0_wO_1m*plas.M_wO_1m/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("O2_1m").featureInfo("info")
         .set("plas.wO2_1m", new String[]{"exp(WO2_1m)/compositionDen"});
    model.component("c").physics("plas").feature("O2_1m").featureInfo("info")
         .set("plas.w0_wO2_1m", new String[]{"plas.x0_wO2_1m*plas.M_wO2_1m/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("Cu").featureInfo("info")
         .set("plas.wCu", new String[]{"exp(WCu)/compositionDen"});
    model.component("c").physics("plas").feature("Cu").featureInfo("info")
         .set("plas.w0_wCu", new String[]{"plas.x0_wCu*plas.M_wCu/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("CuD5").featureInfo("info")
         .set("plas.wCuD5", new String[]{"exp(WCuD5)/compositionDen"});
    model.component("c").physics("plas").feature("CuD5").featureInfo("info")
         .set("plas.w0_wCuD5", new String[]{"plas.x0_wCuD5*plas.M_wCuD5/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("CuD3").featureInfo("info")
         .set("plas.wCuD3", new String[]{"exp(WCuD3)/compositionDen"});
    model.component("c").physics("plas").feature("CuD3").featureInfo("info")
         .set("plas.w0_wCuD3", new String[]{"plas.x0_wCuD3*plas.M_wCuD3/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("CuP1").featureInfo("info")
         .set("plas.wCuP1", new String[]{"exp(WCuP1)/compositionDen"});
    model.component("c").physics("plas").feature("CuP1").featureInfo("info")
         .set("plas.w0_wCuP1", new String[]{"plas.x0_wCuP1*plas.M_wCuP1/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("CuP3").featureInfo("info")
         .set("plas.wCuP3", new String[]{"exp(WCuP3)/compositionDen"});
    model.component("c").physics("plas").feature("CuP3").featureInfo("info")
         .set("plas.w0_wCuP3", new String[]{"plas.x0_wCuP3*plas.M_wCuP3/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("CuH3").featureInfo("info")
         .set("plas.wCuH3", new String[]{"exp(WCuH3)/compositionDen"});
    model.component("c").physics("plas").feature("CuH3").featureInfo("info")
         .set("plas.w0_wCuH3", new String[]{"plas.x0_wCuH3*plas.M_wCuH3/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("CuH5").featureInfo("info")
         .set("plas.wCuH5", new String[]{"exp(WCuH5)/compositionDen"});
    model.component("c").physics("plas").feature("CuH5").featureInfo("info")
         .set("plas.w0_wCuH5", new String[]{"plas.x0_wCuH5*plas.M_wCuH5/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("Cu_1p").featureInfo("info")
         .set("plas.wCu_1p", new String[]{"exp(WCu_1p)/compositionDen"});
    model.component("c").physics("plas").feature("Cu_1p").featureInfo("info")
         .set("plas.w0_wCu_1p", new String[]{"plas.x0_wCu_1p*plas.M_wCu_1p/plas.w0denom*rhoAmb/rho0"});
    model.component("c").physics("plas").feature("compositionChainRule").active(false);
    model.component("c").physics("plas").feature("heavyConservative")
         .set("weakExpression", "0+plas.rho_wN2*((uMassR*WN2r+uMassZ*WN2z)*test(WN2)+uMassR*test(WN2r)+uMassZ*test(WN2z))+plas.rho_wO2*((uMassR*WO2r+uMassZ*WO2z)*test(WO2)+uMassR*test(WO2r)+uMassZ*test(WO2z))+plas.rho_wN*((uMassR*WNr+uMassZ*WNz)*test(WN)+uMassR*test(WNr)+uMassZ*test(WNz))+plas.rho_wO*((uMassR*WOr+uMassZ*WOz)*test(WO)+uMassR*test(WOr)+uMassZ*test(WOz))+plas.rho_wN2_1p*((uMassR*WN2_1pr+uMassZ*WN2_1pz)*test(WN2_1p)+uMassR*test(WN2_1pr)+uMassZ*test(WN2_1pz))+plas.rho_wO2_1p*((uMassR*WO2_1pr+uMassZ*WO2_1pz)*test(WO2_1p)+uMassR*test(WO2_1pr)+uMassZ*test(WO2_1pz))+plas.rho_wN_1p*((uMassR*WN_1pr+uMassZ*WN_1pz)*test(WN_1p)+uMassR*test(WN_1pr)+uMassZ*test(WN_1pz))+plas.rho_wO_1p*((uMassR*WO_1pr+uMassZ*WO_1pz)*test(WO_1p)+uMassR*test(WO_1pr)+uMassZ*test(WO_1pz))+plas.rho_wO_1m*((uMassR*WO_1mr+uMassZ*WO_1mz)*test(WO_1m)+uMassR*test(WO_1mr)+uMassZ*test(WO_1mz))+plas.rho_wO2_1m*((uMassR*WO2_1mr+uMassZ*WO2_1mz)*test(WO2_1m)+uMassR*test(WO2_1mr)+uMassZ*test(WO2_1mz))+plas.rho_wCu*((uMassR*WCur+uMassZ*WCuz)*test(WCu)+uMassR*test(WCur)+uMassZ*test(WCuz))+plas.rho_wCuD5*((uMassR*WCuD5r+uMassZ*WCuD5z)*test(WCuD5)+uMassR*test(WCuD5r)+uMassZ*test(WCuD5z))+plas.rho_wCuD3*((uMassR*WCuD3r+uMassZ*WCuD3z)*test(WCuD3)+uMassR*test(WCuD3r)+uMassZ*test(WCuD3z))+plas.rho_wCuP1*((uMassR*WCuP1r+uMassZ*WCuP1z)*test(WCuP1)+uMassR*test(WCuP1r)+uMassZ*test(WCuP1z))+plas.rho_wCuP3*((uMassR*WCuP3r+uMassZ*WCuP3z)*test(WCuP3)+uMassR*test(WCuP3r)+uMassZ*test(WCuP3z))+plas.rho_wCuH3*((uMassR*WCuH3r+uMassZ*WCuH3z)*test(WCuH3)+uMassR*test(WCuH3r)+uMassZ*test(WCuH3z))+plas.rho_wCuH5*((uMassR*WCuH5r+uMassZ*WCuH5z)*test(WCuH5)+uMassR*test(WCuH5r)+uMassZ*test(WCuH5z))+plas.rho_wCu_1p*((uMassR*WCu_1pr+uMassZ*WCu_1pz)*test(WCu_1p)+uMassR*test(WCu_1pr)+uMassZ*test(WCu_1pz))");
    model.component("c").physics("plas").feature("heavyFarConvection")
         .set("weakExpression", "0-plas.rho_wN2*(uMassR*nr+uMassZ*nz)*test(WN2)-plas.rho_wO2*(uMassR*nr+uMassZ*nz)*test(WO2)-plas.rho_wN*(uMassR*nr+uMassZ*nz)*test(WN)-plas.rho_wO*(uMassR*nr+uMassZ*nz)*test(WO)-plas.rho_wN2_1p*(uMassR*nr+uMassZ*nz)*test(WN2_1p)-plas.rho_wO2_1p*(uMassR*nr+uMassZ*nz)*test(WO2_1p)-plas.rho_wN_1p*(uMassR*nr+uMassZ*nz)*test(WN_1p)-plas.rho_wO_1p*(uMassR*nr+uMassZ*nz)*test(WO_1p)-plas.rho_wO_1m*(uMassR*nr+uMassZ*nz)*test(WO_1m)-plas.rho_wO2_1m*(uMassR*nr+uMassZ*nz)*test(WO2_1m)-plas.rho_wCu*(uMassR*nr+uMassZ*nz)*test(WCu)-plas.rho_wCuD5*(uMassR*nr+uMassZ*nz)*test(WCuD5)-plas.rho_wCuD3*(uMassR*nr+uMassZ*nz)*test(WCuD3)-plas.rho_wCuP1*(uMassR*nr+uMassZ*nz)*test(WCuP1)-plas.rho_wCuP3*(uMassR*nr+uMassZ*nz)*test(WCuP3)-plas.rho_wCuH3*(uMassR*nr+uMassZ*nz)*test(WCuH3)-plas.rho_wCuH5*(uMassR*nr+uMassZ*nz)*test(WCuH5)-plas.rho_wCu_1p*(uMassR*nr+uMassZ*nz)*test(WCu_1p)");
    model.component("c").physics("plas").feature("N2").featureInfo("info")
         .set("plas.R_wN2", new String[]{"(-plas.r_25-plas.r_26+plas.r_106+plas.r_107-plas.r_114+plas.r_115-plas.r_116+plas.r_117-plas.r_118+plas.r_119)-SnumN2_1p-0.5*(SnumN+SnumN_1p)"});
    model.component("c").physics("plas").feature("feed")
         .set("specNames", new String[]{"O2", "N", "O", "N2_1p", "O2_1p", "N_1p", "O_1p", "O_1m", "O2_1m", "Cu", 
         "CuD5", "CuD3", "CuP1", "CuP3", "CuH3", "CuH5", "Cu_1p", "N2"});
    model.component("c").physics("plas").feature("feed")
         .set("influx", new String[]{"0[kg/(m^2*s)]+Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2*vnSurf", "0[kg/(m^2*s)]+Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN*vnSurf", "0[kg/(m^2*s)]+Gwall_O_1p*0.0159994[kg/mol]/N_A_const+Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO*vnSurf", "-Gwall_N2_1p*0.0280134[kg/mol]/N_A_const+plas.rho_wN2_1p*vnSurf", "-Gwall_O2_1p*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1p*vnSurf", "-Gwall_N_1p*0.0140067[kg/mol]/N_A_const+plas.rho_wN_1p*vnSurf", "-Gwall_O_1p*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1p*vnSurf", "-Gwall_O_1m*0.0159994[kg/mol]/N_A_const+plas.rho_wO_1m*vnSurf", "-Gwall_O2_1m*0.0319988[kg/mol]/N_A_const+plas.rho_wO2_1m*vnSurf", "mdotEvap-Gwall_Cu*Mcu/N_A_const+plas.rho_wCu*vnSurf", 
         "-Gwall_CuD5*Mcu/N_A_const+plas.rho_wCuD5*vnSurf", "-Gwall_CuD3*Mcu/N_A_const+plas.rho_wCuD3*vnSurf", "-Gwall_CuP1*Mcu/N_A_const+plas.rho_wCuP1*vnSurf", "-Gwall_CuP3*Mcu/N_A_const+plas.rho_wCuP3*vnSurf", "-Gwall_CuH3*Mcu/N_A_const+plas.rho_wCuH3*vnSurf", "-Gwall_CuH5*Mcu/N_A_const+plas.rho_wCuH5*vnSurf", "-Gwall_Cu_1p*Mcu/N_A_const+plas.rho_wCu_1p*vnSurf", "Gwall_N2_1p*plas.M_wN2/N_A_const+plas.rho_wN2*vnSurf"});
    model.component("c").physics("plas").feature().remove("heavyFeedQuadrature2");
    model.component("c").physics("plas").feature().remove("heavyFeedQuadrature8");
    model.component("c").physics("plas").create("heavyFeedQuadrature2", "WeakContribution", 1);
    model.component("c").physics("plas").feature("heavyFeedQuadrature2").selection().named("surface");
    model.component("c").physics("plas").feature("heavyFeedQuadrature2").set("automaticQuadrature", false);
    model.component("c").physics("plas").feature("heavyFeedQuadrature2").set("integrationOrder", 2);
    model.component("c").physics("plas").feature("heavyFeedQuadrature2")
         .set("weakExpression", "-((plas.influx_N2*test(WN2))+(plas.influx_N2_1p*test(WN2_1p))+(plas.influx_O2*test(WO2))+(plas.influx_O*test(WO))+(plas.influx_O_1m*test(WO_1m))+(plas.influx_O2_1p*test(WO2_1p))+(plas.influx_Cu*test(WCu))+(plas.influx_CuD5*test(WCuD5))+(plas.influx_CuD3*test(WCuD3))+(plas.influx_CuP1*test(WCuP1))+(plas.influx_CuP3*test(WCuP3))+(plas.influx_CuH3*test(WCuH3))+(plas.influx_CuH5*test(WCuH5))+(plas.influx_Cu_1p*test(WCu_1p))+(plas.influx_N*test(WN))+(plas.influx_N_1p*test(WN_1p))+(plas.influx_O_1p*test(WO_1p))+(plas.influx_O2_1m*test(WO2_1m)))");
    model.component("c").physics("plas").create("heavyFeedQuadrature8", "WeakContribution", 1);
    model.component("c").physics("plas").feature("heavyFeedQuadrature8").selection().named("surface");
    model.component("c").physics("plas").feature("heavyFeedQuadrature8").set("automaticQuadrature", false);
    model.component("c").physics("plas").feature("heavyFeedQuadrature8").set("integrationOrder", 8);
    model.component("c").physics("plas").feature("heavyFeedQuadrature8")
         .set("weakExpression", "((plas.influx_N2*test(WN2))+(plas.influx_N2_1p*test(WN2_1p))+(plas.influx_O2*test(WO2))+(plas.influx_O*test(WO))+(plas.influx_O_1m*test(WO_1m))+(plas.influx_O2_1p*test(WO2_1p))+(plas.influx_Cu*test(WCu))+(plas.influx_CuD5*test(WCuD5))+(plas.influx_CuD3*test(WCuD3))+(plas.influx_CuP1*test(WCuP1))+(plas.influx_CuP3*test(WCuP3))+(plas.influx_CuH3*test(WCuH3))+(plas.influx_CuH5*test(WCuH5))+(plas.influx_Cu_1p*test(WCu_1p))+(plas.influx_N*test(WN))+(plas.influx_N_1p*test(WN_1p))+(plas.influx_O_1p*test(WO_1p))+(plas.influx_O2_1m*test(WO2_1m)))");
    model.component("c").physics("flow").feature("gfeq1")
         .set("Ga", new String[][]{{"0", "0"}, {"axisWeight*((rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0))+axisWeight*urP*JmassR/(rho0*u0)", "axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))+axisWeight*urP*JmassZ/(rho0*u0)"}, {"axisWeight*((rhoP*urP*uzP-tauRZ)/(rho0*u0))+axisWeight*uzP*JmassR/(rho0*u0)", "axisWeight*((rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0))+axisWeight*uzP*JmassZ/(rho0*u0)"}, {"axisWeight*((urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0))+axisWeight*(rho0*e0*qE+pPl)*JmassR/(rhoSafe*rho0*e0)", "axisWeight*((uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0))+axisWeight*(rho0*e0*qE+pPl)*JmassZ/(rhoSafe*rho0*e0)"}});
    model.component("c").physics("flow").feature("gfeq1")
         .set("f", new String[]{"axisWeight*(compositionDen-qR)/1[ns]", "axisWeight*((pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0))", "axisWeight*(plas.scharge*plas.Etz/(rho0*u0))", "axisWeight*((Qlaser-Qrad+Qfield)/(rho0*e0))"});
    model.component("c").physics("flow").feature("gfeq1")
         .set("da", new String[]{"0", "0", "0", "0", "0", "axisWeight", "0", "0", "0", "0", 
         "axisWeight", "0", "0", "0", "0", "axisWeight"});
    model.component("c").physics("flow").feature("feed")
         .set("g", new String[]{"0", "axisWeight*(((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0))", "axisWeight*(((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0))", "axisWeight*((Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0))"});
    model.component("c").physics("flow").feature("farbc").set("useDirichletCondition", new int[]{0, 1, 1, 1});

    model.component("c").variable("gasv")
         .set("rawMassFluxr_N2", "-rhoP*plas.Dfs_wN2*(d(plas.wN2,r)+plas.wN2*d(plas.Mn,r)/plas.Mn)-plas.DT_wN2*d(Tgas,r)/Tgas+rhoP*plas.wN2*plas.z_wN2*plas.um_wN2*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_O2", "-rhoP*plas.Dfs_wO2*(d(plas.wO2,r)+plas.wO2*d(plas.Mn,r)/plas.Mn)-plas.DT_wO2*d(Tgas,r)/Tgas+rhoP*plas.wO2*plas.z_wO2*plas.um_wO2*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_N", "-rhoP*plas.Dfs_wN*(d(plas.wN,r)+plas.wN*d(plas.Mn,r)/plas.Mn)-plas.DT_wN*d(Tgas,r)/Tgas+rhoP*plas.wN*plas.z_wN*plas.um_wN*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_O", "-rhoP*plas.Dfs_wO*(d(plas.wO,r)+plas.wO*d(plas.Mn,r)/plas.Mn)-plas.DT_wO*d(Tgas,r)/Tgas+rhoP*plas.wO*plas.z_wO*plas.um_wO*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_N2_1p", "-rhoP*plas.Dfs_wN2_1p*(d(plas.wN2_1p,r)+plas.wN2_1p*d(plas.Mn,r)/plas.Mn)-plas.DT_wN2_1p*d(Tgas,r)/Tgas+rhoP*plas.wN2_1p*plas.z_wN2_1p*plas.um_wN2_1p*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_O2_1p", "-rhoP*plas.Dfs_wO2_1p*(d(plas.wO2_1p,r)+plas.wO2_1p*d(plas.Mn,r)/plas.Mn)-plas.DT_wO2_1p*d(Tgas,r)/Tgas+rhoP*plas.wO2_1p*plas.z_wO2_1p*plas.um_wO2_1p*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_N_1p", "-rhoP*plas.Dfs_wN_1p*(d(plas.wN_1p,r)+plas.wN_1p*d(plas.Mn,r)/plas.Mn)-plas.DT_wN_1p*d(Tgas,r)/Tgas+rhoP*plas.wN_1p*plas.z_wN_1p*plas.um_wN_1p*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_O_1p", "-rhoP*plas.Dfs_wO_1p*(d(plas.wO_1p,r)+plas.wO_1p*d(plas.Mn,r)/plas.Mn)-plas.DT_wO_1p*d(Tgas,r)/Tgas+rhoP*plas.wO_1p*plas.z_wO_1p*plas.um_wO_1p*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_O_1m", "-rhoP*plas.Dfs_wO_1m*(d(plas.wO_1m,r)+plas.wO_1m*d(plas.Mn,r)/plas.Mn)-plas.DT_wO_1m*d(Tgas,r)/Tgas+rhoP*plas.wO_1m*plas.z_wO_1m*plas.um_wO_1m*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_O2_1m", "-rhoP*plas.Dfs_wO2_1m*(d(plas.wO2_1m,r)+plas.wO2_1m*d(plas.Mn,r)/plas.Mn)-plas.DT_wO2_1m*d(Tgas,r)/Tgas+rhoP*plas.wO2_1m*plas.z_wO2_1m*plas.um_wO2_1m*plas.Etr");

    return model;
  }

  public static Model run23(Model model) {
    model.component("c").variable("gasv")
         .set("rawMassFluxr_Cu", "-rhoP*plas.Dfs_wCu*(d(plas.wCu,r)+plas.wCu*d(plas.Mn,r)/plas.Mn)-plas.DT_wCu*d(Tgas,r)/Tgas+rhoP*plas.wCu*plas.z_wCu*plas.um_wCu*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_CuD5", "-rhoP*plas.Dfs_wCuD5*(d(plas.wCuD5,r)+plas.wCuD5*d(plas.Mn,r)/plas.Mn)-plas.DT_wCuD5*d(Tgas,r)/Tgas+rhoP*plas.wCuD5*plas.z_wCuD5*plas.um_wCuD5*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_CuD3", "-rhoP*plas.Dfs_wCuD3*(d(plas.wCuD3,r)+plas.wCuD3*d(plas.Mn,r)/plas.Mn)-plas.DT_wCuD3*d(Tgas,r)/Tgas+rhoP*plas.wCuD3*plas.z_wCuD3*plas.um_wCuD3*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_CuP1", "-rhoP*plas.Dfs_wCuP1*(d(plas.wCuP1,r)+plas.wCuP1*d(plas.Mn,r)/plas.Mn)-plas.DT_wCuP1*d(Tgas,r)/Tgas+rhoP*plas.wCuP1*plas.z_wCuP1*plas.um_wCuP1*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_CuP3", "-rhoP*plas.Dfs_wCuP3*(d(plas.wCuP3,r)+plas.wCuP3*d(plas.Mn,r)/plas.Mn)-plas.DT_wCuP3*d(Tgas,r)/Tgas+rhoP*plas.wCuP3*plas.z_wCuP3*plas.um_wCuP3*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_CuH3", "-rhoP*plas.Dfs_wCuH3*(d(plas.wCuH3,r)+plas.wCuH3*d(plas.Mn,r)/plas.Mn)-plas.DT_wCuH3*d(Tgas,r)/Tgas+rhoP*plas.wCuH3*plas.z_wCuH3*plas.um_wCuH3*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_CuH5", "-rhoP*plas.Dfs_wCuH5*(d(plas.wCuH5,r)+plas.wCuH5*d(plas.Mn,r)/plas.Mn)-plas.DT_wCuH5*d(Tgas,r)/Tgas+rhoP*plas.wCuH5*plas.z_wCuH5*plas.um_wCuH5*plas.Etr");
    model.component("c").variable("gasv")
         .set("rawMassFluxr_Cu_1p", "-rhoP*plas.Dfs_wCu_1p*(d(plas.wCu_1p,r)+plas.wCu_1p*d(plas.Mn,r)/plas.Mn)-plas.DT_wCu_1p*d(Tgas,r)/Tgas+rhoP*plas.wCu_1p*plas.z_wCu_1p*plas.um_wCu_1p*plas.Etr");
    model.component("c").variable("gasv")
         .set("sumRawMassFluxr", "0[kg/(m^2*s)]+rawMassFluxr_N2+rawMassFluxr_O2+rawMassFluxr_N+rawMassFluxr_O+rawMassFluxr_N2_1p+rawMassFluxr_O2_1p+rawMassFluxr_N_1p+rawMassFluxr_O_1p+rawMassFluxr_O_1m+rawMassFluxr_O2_1m+rawMassFluxr_Cu+rawMassFluxr_CuD5+rawMassFluxr_CuD3+rawMassFluxr_CuP1+rawMassFluxr_CuP3+rawMassFluxr_CuH3+rawMassFluxr_CuH5+rawMassFluxr_Cu_1p");

    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wN2", new String[]{"(-plas.rho*plas.Dfs_wN2*d(plas.wN2,r)-plas.rho*plas.wN2*plas.Dfs_wN2*d(plas.Mn,r)/plas.Mn-plas.DT_wN2*d(plas.T,r)/plas.T)-plas.wN2*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wO2", new String[]{"(-plas.rho*plas.Dfs_wO2*d(plas.wO2,r)-plas.rho*plas.wO2*plas.Dfs_wO2*d(plas.Mn,r)/plas.Mn-plas.DT_wO2*d(plas.T,r)/plas.T)-plas.wO2*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wN", new String[]{"(-plas.rho*plas.Dfs_wN*d(plas.wN,r)-plas.rho*plas.wN*plas.Dfs_wN*d(plas.Mn,r)/plas.Mn-plas.DT_wN*d(plas.T,r)/plas.T)-plas.wN*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wO", new String[]{"(-plas.rho*plas.Dfs_wO*d(plas.wO,r)-plas.rho*plas.wO*plas.Dfs_wO*d(plas.Mn,r)/plas.Mn-plas.DT_wO*d(plas.T,r)/plas.T)-plas.wO*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wN2_1p", new String[]{"(-plas.rho*plas.Dfs_wN2_1p*d(plas.wN2_1p,r)-plas.rho*plas.wN2_1p*plas.Dfs_wN2_1p*d(plas.Mn,r)/plas.Mn-plas.DT_wN2_1p*d(plas.T,r)/plas.T)-plas.wN2_1p*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wO2_1p", new String[]{"(-plas.rho*plas.Dfs_wO2_1p*d(plas.wO2_1p,r)-plas.rho*plas.wO2_1p*plas.Dfs_wO2_1p*d(plas.Mn,r)/plas.Mn-plas.DT_wO2_1p*d(plas.T,r)/plas.T)-plas.wO2_1p*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wN_1p", new String[]{"(-plas.rho*plas.Dfs_wN_1p*d(plas.wN_1p,r)-plas.rho*plas.wN_1p*plas.Dfs_wN_1p*d(plas.Mn,r)/plas.Mn-plas.DT_wN_1p*d(plas.T,r)/plas.T)-plas.wN_1p*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wO_1p", new String[]{"(-plas.rho*plas.Dfs_wO_1p*d(plas.wO_1p,r)-plas.rho*plas.wO_1p*plas.Dfs_wO_1p*d(plas.Mn,r)/plas.Mn-plas.DT_wO_1p*d(plas.T,r)/plas.T)-plas.wO_1p*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wO_1m", new String[]{"(-plas.rho*plas.Dfs_wO_1m*d(plas.wO_1m,r)-plas.rho*plas.wO_1m*plas.Dfs_wO_1m*d(plas.Mn,r)/plas.Mn-plas.DT_wO_1m*d(plas.T,r)/plas.T)-plas.wO_1m*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wO2_1m", new String[]{"(-plas.rho*plas.Dfs_wO2_1m*d(plas.wO2_1m,r)-plas.rho*plas.wO2_1m*plas.Dfs_wO2_1m*d(plas.Mn,r)/plas.Mn-plas.DT_wO2_1m*d(plas.T,r)/plas.T)-plas.wO2_1m*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wCu", new String[]{"(-plas.rho*plas.Dfs_wCu*d(plas.wCu,r)-plas.rho*plas.wCu*plas.Dfs_wCu*d(plas.Mn,r)/plas.Mn-plas.DT_wCu*d(plas.T,r)/plas.T)-plas.wCu*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wCuD5", new String[]{"(-plas.rho*plas.Dfs_wCuD5*d(plas.wCuD5,r)-plas.rho*plas.wCuD5*plas.Dfs_wCuD5*d(plas.Mn,r)/plas.Mn-plas.DT_wCuD5*d(plas.T,r)/plas.T)-plas.wCuD5*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wCuD3", new String[]{"(-plas.rho*plas.Dfs_wCuD3*d(plas.wCuD3,r)-plas.rho*plas.wCuD3*plas.Dfs_wCuD3*d(plas.Mn,r)/plas.Mn-plas.DT_wCuD3*d(plas.T,r)/plas.T)-plas.wCuD3*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wCuP1", new String[]{"(-plas.rho*plas.Dfs_wCuP1*d(plas.wCuP1,r)-plas.rho*plas.wCuP1*plas.Dfs_wCuP1*d(plas.Mn,r)/plas.Mn-plas.DT_wCuP1*d(plas.T,r)/plas.T)-plas.wCuP1*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wCuP3", new String[]{"(-plas.rho*plas.Dfs_wCuP3*d(plas.wCuP3,r)-plas.rho*plas.wCuP3*plas.Dfs_wCuP3*d(plas.Mn,r)/plas.Mn-plas.DT_wCuP3*d(plas.T,r)/plas.T)-plas.wCuP3*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wCuH3", new String[]{"(-plas.rho*plas.Dfs_wCuH3*d(plas.wCuH3,r)-plas.rho*plas.wCuH3*plas.Dfs_wCuH3*d(plas.Mn,r)/plas.Mn-plas.DT_wCuH3*d(plas.T,r)/plas.T)-plas.wCuH3*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wCuH5", new String[]{"(-plas.rho*plas.Dfs_wCuH5*d(plas.wCuH5,r)-plas.rho*plas.wCuH5*plas.Dfs_wCuH5*d(plas.Mn,r)/plas.Mn-plas.DT_wCuH5*d(plas.T,r)/plas.T)-plas.wCuH5*sumRawMassFluxr"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jr_wCu_1p", new String[]{"(-plas.rho*plas.Dfs_wCu_1p*d(plas.wCu_1p,r)-plas.rho*plas.wCu_1p*plas.Dfs_wCu_1p*d(plas.Mn,r)/plas.Mn-plas.DT_wCu_1p*d(plas.T,r)/plas.T)-plas.wCu_1p*sumRawMassFluxr"});

    model.component("c").variable("gasv")
         .set("rawMassFluxz_N2", "-rhoP*plas.Dfs_wN2*(d(plas.wN2,z)+plas.wN2*d(plas.Mn,z)/plas.Mn)-plas.DT_wN2*d(Tgas,z)/Tgas+rhoP*plas.wN2*plas.z_wN2*plas.um_wN2*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_O2", "-rhoP*plas.Dfs_wO2*(d(plas.wO2,z)+plas.wO2*d(plas.Mn,z)/plas.Mn)-plas.DT_wO2*d(Tgas,z)/Tgas+rhoP*plas.wO2*plas.z_wO2*plas.um_wO2*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_N", "-rhoP*plas.Dfs_wN*(d(plas.wN,z)+plas.wN*d(plas.Mn,z)/plas.Mn)-plas.DT_wN*d(Tgas,z)/Tgas+rhoP*plas.wN*plas.z_wN*plas.um_wN*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_O", "-rhoP*plas.Dfs_wO*(d(plas.wO,z)+plas.wO*d(plas.Mn,z)/plas.Mn)-plas.DT_wO*d(Tgas,z)/Tgas+rhoP*plas.wO*plas.z_wO*plas.um_wO*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_N2_1p", "-rhoP*plas.Dfs_wN2_1p*(d(plas.wN2_1p,z)+plas.wN2_1p*d(plas.Mn,z)/plas.Mn)-plas.DT_wN2_1p*d(Tgas,z)/Tgas+rhoP*plas.wN2_1p*plas.z_wN2_1p*plas.um_wN2_1p*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_O2_1p", "-rhoP*plas.Dfs_wO2_1p*(d(plas.wO2_1p,z)+plas.wO2_1p*d(plas.Mn,z)/plas.Mn)-plas.DT_wO2_1p*d(Tgas,z)/Tgas+rhoP*plas.wO2_1p*plas.z_wO2_1p*plas.um_wO2_1p*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_N_1p", "-rhoP*plas.Dfs_wN_1p*(d(plas.wN_1p,z)+plas.wN_1p*d(plas.Mn,z)/plas.Mn)-plas.DT_wN_1p*d(Tgas,z)/Tgas+rhoP*plas.wN_1p*plas.z_wN_1p*plas.um_wN_1p*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_O_1p", "-rhoP*plas.Dfs_wO_1p*(d(plas.wO_1p,z)+plas.wO_1p*d(plas.Mn,z)/plas.Mn)-plas.DT_wO_1p*d(Tgas,z)/Tgas+rhoP*plas.wO_1p*plas.z_wO_1p*plas.um_wO_1p*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_O_1m", "-rhoP*plas.Dfs_wO_1m*(d(plas.wO_1m,z)+plas.wO_1m*d(plas.Mn,z)/plas.Mn)-plas.DT_wO_1m*d(Tgas,z)/Tgas+rhoP*plas.wO_1m*plas.z_wO_1m*plas.um_wO_1m*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_O2_1m", "-rhoP*plas.Dfs_wO2_1m*(d(plas.wO2_1m,z)+plas.wO2_1m*d(plas.Mn,z)/plas.Mn)-plas.DT_wO2_1m*d(Tgas,z)/Tgas+rhoP*plas.wO2_1m*plas.z_wO2_1m*plas.um_wO2_1m*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_Cu", "-rhoP*plas.Dfs_wCu*(d(plas.wCu,z)+plas.wCu*d(plas.Mn,z)/plas.Mn)-plas.DT_wCu*d(Tgas,z)/Tgas+rhoP*plas.wCu*plas.z_wCu*plas.um_wCu*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_CuD5", "-rhoP*plas.Dfs_wCuD5*(d(plas.wCuD5,z)+plas.wCuD5*d(plas.Mn,z)/plas.Mn)-plas.DT_wCuD5*d(Tgas,z)/Tgas+rhoP*plas.wCuD5*plas.z_wCuD5*plas.um_wCuD5*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_CuD3", "-rhoP*plas.Dfs_wCuD3*(d(plas.wCuD3,z)+plas.wCuD3*d(plas.Mn,z)/plas.Mn)-plas.DT_wCuD3*d(Tgas,z)/Tgas+rhoP*plas.wCuD3*plas.z_wCuD3*plas.um_wCuD3*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_CuP1", "-rhoP*plas.Dfs_wCuP1*(d(plas.wCuP1,z)+plas.wCuP1*d(plas.Mn,z)/plas.Mn)-plas.DT_wCuP1*d(Tgas,z)/Tgas+rhoP*plas.wCuP1*plas.z_wCuP1*plas.um_wCuP1*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_CuP3", "-rhoP*plas.Dfs_wCuP3*(d(plas.wCuP3,z)+plas.wCuP3*d(plas.Mn,z)/plas.Mn)-plas.DT_wCuP3*d(Tgas,z)/Tgas+rhoP*plas.wCuP3*plas.z_wCuP3*plas.um_wCuP3*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_CuH3", "-rhoP*plas.Dfs_wCuH3*(d(plas.wCuH3,z)+plas.wCuH3*d(plas.Mn,z)/plas.Mn)-plas.DT_wCuH3*d(Tgas,z)/Tgas+rhoP*plas.wCuH3*plas.z_wCuH3*plas.um_wCuH3*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_CuH5", "-rhoP*plas.Dfs_wCuH5*(d(plas.wCuH5,z)+plas.wCuH5*d(plas.Mn,z)/plas.Mn)-plas.DT_wCuH5*d(Tgas,z)/Tgas+rhoP*plas.wCuH5*plas.z_wCuH5*plas.um_wCuH5*plas.Etz");
    model.component("c").variable("gasv")
         .set("rawMassFluxz_Cu_1p", "-rhoP*plas.Dfs_wCu_1p*(d(plas.wCu_1p,z)+plas.wCu_1p*d(plas.Mn,z)/plas.Mn)-plas.DT_wCu_1p*d(Tgas,z)/Tgas+rhoP*plas.wCu_1p*plas.z_wCu_1p*plas.um_wCu_1p*plas.Etz");
    model.component("c").variable("gasv")
         .set("sumRawMassFluxz", "0[kg/(m^2*s)]+rawMassFluxz_N2+rawMassFluxz_O2+rawMassFluxz_N+rawMassFluxz_O+rawMassFluxz_N2_1p+rawMassFluxz_O2_1p+rawMassFluxz_N_1p+rawMassFluxz_O_1p+rawMassFluxz_O_1m+rawMassFluxz_O2_1m+rawMassFluxz_Cu+rawMassFluxz_CuD5+rawMassFluxz_CuD3+rawMassFluxz_CuP1+rawMassFluxz_CuP3+rawMassFluxz_CuH3+rawMassFluxz_CuH5+rawMassFluxz_Cu_1p");

    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wN2", new String[]{"(-plas.rho*plas.Dfs_wN2*d(plas.wN2,z)-plas.rho*plas.wN2*plas.Dfs_wN2*d(plas.Mn,z)/plas.Mn-plas.DT_wN2*d(plas.T,z)/plas.T)-plas.wN2*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wO2", new String[]{"(-plas.rho*plas.Dfs_wO2*d(plas.wO2,z)-plas.rho*plas.wO2*plas.Dfs_wO2*d(plas.Mn,z)/plas.Mn-plas.DT_wO2*d(plas.T,z)/plas.T)-plas.wO2*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wN", new String[]{"(-plas.rho*plas.Dfs_wN*d(plas.wN,z)-plas.rho*plas.wN*plas.Dfs_wN*d(plas.Mn,z)/plas.Mn-plas.DT_wN*d(plas.T,z)/plas.T)-plas.wN*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wO", new String[]{"(-plas.rho*plas.Dfs_wO*d(plas.wO,z)-plas.rho*plas.wO*plas.Dfs_wO*d(plas.Mn,z)/plas.Mn-plas.DT_wO*d(plas.T,z)/plas.T)-plas.wO*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wN2_1p", new String[]{"(-plas.rho*plas.Dfs_wN2_1p*d(plas.wN2_1p,z)-plas.rho*plas.wN2_1p*plas.Dfs_wN2_1p*d(plas.Mn,z)/plas.Mn-plas.DT_wN2_1p*d(plas.T,z)/plas.T)-plas.wN2_1p*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wO2_1p", new String[]{"(-plas.rho*plas.Dfs_wO2_1p*d(plas.wO2_1p,z)-plas.rho*plas.wO2_1p*plas.Dfs_wO2_1p*d(plas.Mn,z)/plas.Mn-plas.DT_wO2_1p*d(plas.T,z)/plas.T)-plas.wO2_1p*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wN_1p", new String[]{"(-plas.rho*plas.Dfs_wN_1p*d(plas.wN_1p,z)-plas.rho*plas.wN_1p*plas.Dfs_wN_1p*d(plas.Mn,z)/plas.Mn-plas.DT_wN_1p*d(plas.T,z)/plas.T)-plas.wN_1p*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wO_1p", new String[]{"(-plas.rho*plas.Dfs_wO_1p*d(plas.wO_1p,z)-plas.rho*plas.wO_1p*plas.Dfs_wO_1p*d(plas.Mn,z)/plas.Mn-plas.DT_wO_1p*d(plas.T,z)/plas.T)-plas.wO_1p*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wO_1m", new String[]{"(-plas.rho*plas.Dfs_wO_1m*d(plas.wO_1m,z)-plas.rho*plas.wO_1m*plas.Dfs_wO_1m*d(plas.Mn,z)/plas.Mn-plas.DT_wO_1m*d(plas.T,z)/plas.T)-plas.wO_1m*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wO2_1m", new String[]{"(-plas.rho*plas.Dfs_wO2_1m*d(plas.wO2_1m,z)-plas.rho*plas.wO2_1m*plas.Dfs_wO2_1m*d(plas.Mn,z)/plas.Mn-plas.DT_wO2_1m*d(plas.T,z)/plas.T)-plas.wO2_1m*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wCu", new String[]{"(-plas.rho*plas.Dfs_wCu*d(plas.wCu,z)-plas.rho*plas.wCu*plas.Dfs_wCu*d(plas.Mn,z)/plas.Mn-plas.DT_wCu*d(plas.T,z)/plas.T)-plas.wCu*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wCuD5", new String[]{"(-plas.rho*plas.Dfs_wCuD5*d(plas.wCuD5,z)-plas.rho*plas.wCuD5*plas.Dfs_wCuD5*d(plas.Mn,z)/plas.Mn-plas.DT_wCuD5*d(plas.T,z)/plas.T)-plas.wCuD5*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wCuD3", new String[]{"(-plas.rho*plas.Dfs_wCuD3*d(plas.wCuD3,z)-plas.rho*plas.wCuD3*plas.Dfs_wCuD3*d(plas.Mn,z)/plas.Mn-plas.DT_wCuD3*d(plas.T,z)/plas.T)-plas.wCuD3*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wCuP1", new String[]{"(-plas.rho*plas.Dfs_wCuP1*d(plas.wCuP1,z)-plas.rho*plas.wCuP1*plas.Dfs_wCuP1*d(plas.Mn,z)/plas.Mn-plas.DT_wCuP1*d(plas.T,z)/plas.T)-plas.wCuP1*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wCuP3", new String[]{"(-plas.rho*plas.Dfs_wCuP3*d(plas.wCuP3,z)-plas.rho*plas.wCuP3*plas.Dfs_wCuP3*d(plas.Mn,z)/plas.Mn-plas.DT_wCuP3*d(plas.T,z)/plas.T)-plas.wCuP3*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wCuH3", new String[]{"(-plas.rho*plas.Dfs_wCuH3*d(plas.wCuH3,z)-plas.rho*plas.wCuH3*plas.Dfs_wCuH3*d(plas.Mn,z)/plas.Mn-plas.DT_wCuH3*d(plas.T,z)/plas.T)-plas.wCuH3*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wCuH5", new String[]{"(-plas.rho*plas.Dfs_wCuH5*d(plas.wCuH5,z)-plas.rho*plas.wCuH5*plas.Dfs_wCuH5*d(plas.Mn,z)/plas.Mn-plas.DT_wCuH5*d(plas.T,z)/plas.T)-plas.wCuH5*sumRawMassFluxz"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.jz_wCu_1p", new String[]{"(-plas.rho*plas.Dfs_wCu_1p*d(plas.wCu_1p,z)-plas.rho*plas.wCu_1p*plas.Dfs_wCu_1p*d(plas.Mn,z)/plas.Mn-plas.DT_wCu_1p*d(plas.T,z)/plas.T)-plas.wCu_1p*sumRawMassFluxz"});

    model.param().set("meltOn", "1");
    model.param().set("w0", "11[um]");
    model.param().set("tEnd", "123[ns]");

    model.sol().remove("sol1");

    model.study("std").feature("time").set("tlist", "range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)");
    model.study("std").feature("time").set("rtol", 0.001);
    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").feature().remove("fc1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 15);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("tlist", "range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)");
    model.sol("sol1").feature("t1").set("rtol", 0.001);
    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "1[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("t1").set("consistent", "bweuler");
    model.sol("sol1").feature("v1").set("initmethod", "init");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_logTgas").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_logTgas").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V_lm").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V_lm").set("scaleval", "1e-10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "expr");
    model.sol("sol1").feature("t1").set("maxstepexpressionbdf", "if(t<80[ns],1[ns],0.1[ns])");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "100");

    model.label("Ground-state moving-surface control, matched to F80/F87 physics");
    model.label("M03_ready.mph");

    model.sol().remove("sol1");

    model.component("c").geom("g").create("fullColumn", "Rectangle");
    model.component("c").geom("g").feature("fullColumn").set("pos", new String[]{"0", "-Dcu"});
    model.component("c").geom("g").feature("fullColumn").set("size", new String[]{"60[um]", "Zdom+Dcu"});
    model.component("c").geom("g").feature("fullColumn").set("selresult", true);
    model.component("c").geom("g").run();

    model.component("c").mesh("mesh").feature().remove("tri");
    model.component("c").mesh("mesh").feature().remove("wallMap");
    model.component("c").mesh("mesh").feature().remove("normalLeft");
    model.component("c").mesh("mesh").feature().remove("normalRight");

    model.component("c").selection().create("columnEdges", "Box");
    model.component("c").selection("columnEdges").set("entitydim", 1);
    model.component("c").selection("columnEdges").set("xmin", "-1e-10");
    model.component("c").selection("columnEdges").set("xmax", "60[um]+1e-10[m]");
    model.component("c").selection("columnEdges").set("ymin", "-Dcu-1e-10[m]");
    model.component("c").selection("columnEdges").set("ymax", "Zdom+1e-10[m]");
    model.component("c").selection("columnEdges").set("condition", "inside");
    model.component("c").selection().create("columnLeft", "Box");
    model.component("c").selection("columnLeft").set("entitydim", 1);
    model.component("c").selection("columnLeft").set("xmin", "-1e-10");
    model.component("c").selection("columnLeft").set("xmax", "1e-10");
    model.component("c").selection("columnLeft").set("ymin", "-Dcu-1e-10[m]");
    model.component("c").selection("columnLeft").set("ymax", "Zdom+1e-10[m]");
    model.component("c").selection("columnLeft").set("condition", "inside");
    model.component("c").selection().create("columnRight", "Box");
    model.component("c").selection("columnRight").set("entitydim", 1);
    model.component("c").selection("columnRight").set("xmin", "60[um]-1e-10[m]");
    model.component("c").selection("columnRight").set("xmax", "60[um]+1e-10[m]");
    model.component("c").selection("columnRight").set("ymin", "-Dcu-1e-10[m]");
    model.component("c").selection("columnRight").set("ymax", "Zdom+1e-10[m]");
    model.component("c").selection("columnRight").set("condition", "inside");
    model.component("c").selection().create("columnHorizontal", "Difference");
    model.component("c").selection("columnHorizontal").set("entitydim", 1);
    model.component("c").selection("columnHorizontal").set("add", new String[]{"columnEdges"});
    model.component("c").selection("columnHorizontal").set("subtract", new String[]{"columnLeft", "columnRight"});
    model.component("c").selection().create("columnEdge0_0", "Box");
    model.component("c").selection("columnEdge0_0").set("entitydim", 1);
    model.component("c").selection("columnEdge0_0").set("xmin", "-1e-10");
    model.component("c").selection("columnEdge0_0").set("xmax", "1e-10");
    model.component("c").selection("columnEdge0_0").set("ymin", "-Dcu-1e-10[m]");
    model.component("c").selection("columnEdge0_0").set("ymax", "-15[um]+1e-10[m]");
    model.component("c").selection("columnEdge0_0").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge0_0", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge0_0").selection().named("columnEdge0_0");
    model.component("c").mesh("mesh").feature("columnEdge0_0").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge0_0").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge0_0").feature("dist").set("elemcount", 10);
    model.component("c").mesh("mesh").feature("columnEdge0_0").feature("dist").set("elemratio", 8);
    model.component("c").mesh("mesh").feature("columnEdge0_0").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge0_0").feature("dist").set("reverse", true);

    model.component("c").selection().create("columnEdge0_1", "Box");
    model.component("c").selection("columnEdge0_1").set("entitydim", 1);
    model.component("c").selection("columnEdge0_1").set("xmin", "60[um]-1e-10[m]");
    model.component("c").selection("columnEdge0_1").set("xmax", "60[um]+1e-10[m]");
    model.component("c").selection("columnEdge0_1").set("ymin", "-Dcu-1e-10[m]");
    model.component("c").selection("columnEdge0_1").set("ymax", "-15[um]+1e-10[m]");
    model.component("c").selection("columnEdge0_1").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge0_1", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge0_1").selection().named("columnEdge0_1");
    model.component("c").mesh("mesh").feature("columnEdge0_1").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge0_1").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge0_1").feature("dist").set("elemcount", 10);
    model.component("c").mesh("mesh").feature("columnEdge0_1").feature("dist").set("elemratio", 8);
    model.component("c").mesh("mesh").feature("columnEdge0_1").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge0_1").feature("dist").set("reverse", false);

    model.component("c").selection().create("columnEdge1_0", "Box");
    model.component("c").selection("columnEdge1_0").set("entitydim", 1);
    model.component("c").selection("columnEdge1_0").set("xmin", "-1e-10");
    model.component("c").selection("columnEdge1_0").set("xmax", "1e-10");
    model.component("c").selection("columnEdge1_0").set("ymin", "-15[um]-1e-10[m]");
    model.component("c").selection("columnEdge1_0").set("ymax", "0[um]+1e-10[m]");
    model.component("c").selection("columnEdge1_0").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge1_0", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge1_0").selection().named("columnEdge1_0");
    model.component("c").mesh("mesh").feature("columnEdge1_0").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge1_0").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge1_0").feature("dist").set("elemcount", 40);
    model.component("c").mesh("mesh").feature("columnEdge1_0").feature("dist").set("elemratio", 10);
    model.component("c").mesh("mesh").feature("columnEdge1_0").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge1_0").feature("dist").set("reverse", true);

    model.component("c").selection().create("columnEdge1_1", "Box");
    model.component("c").selection("columnEdge1_1").set("entitydim", 1);
    model.component("c").selection("columnEdge1_1").set("xmin", "60[um]-1e-10[m]");
    model.component("c").selection("columnEdge1_1").set("xmax", "60[um]+1e-10[m]");
    model.component("c").selection("columnEdge1_1").set("ymin", "-15[um]-1e-10[m]");
    model.component("c").selection("columnEdge1_1").set("ymax", "0[um]+1e-10[m]");
    model.component("c").selection("columnEdge1_1").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge1_1", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge1_1").selection().named("columnEdge1_1");
    model.component("c").mesh("mesh").feature("columnEdge1_1").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge1_1").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge1_1").feature("dist").set("elemcount", 40);
    model.component("c").mesh("mesh").feature("columnEdge1_1").feature("dist").set("elemratio", 10);

    return model;
  }

  public static Model run24(Model model) {
    model.component("c").mesh("mesh").feature("columnEdge1_1").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge1_1").feature("dist").set("reverse", false);

    model.component("c").selection().create("columnEdge2_0", "Box");
    model.component("c").selection("columnEdge2_0").set("entitydim", 1);
    model.component("c").selection("columnEdge2_0").set("xmin", "-1e-10");
    model.component("c").selection("columnEdge2_0").set("xmax", "1e-10");
    model.component("c").selection("columnEdge2_0").set("ymin", "0[um]-1e-10[m]");
    model.component("c").selection("columnEdge2_0").set("ymax", "2[um]+1e-10[m]");
    model.component("c").selection("columnEdge2_0").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge2_0", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge2_0").selection().named("columnEdge2_0");
    model.component("c").mesh("mesh").feature("columnEdge2_0").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge2_0").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge2_0").feature("dist").set("elemcount", 25);
    model.component("c").mesh("mesh").feature("columnEdge2_0").feature("dist").set("elemratio", 210);
    model.component("c").mesh("mesh").feature("columnEdge2_0").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge2_0").feature("dist").set("reverse", false);

    model.component("c").selection().create("columnEdge2_1", "Box");
    model.component("c").selection("columnEdge2_1").set("entitydim", 1);
    model.component("c").selection("columnEdge2_1").set("xmin", "60[um]-1e-10[m]");
    model.component("c").selection("columnEdge2_1").set("xmax", "60[um]+1e-10[m]");
    model.component("c").selection("columnEdge2_1").set("ymin", "0[um]-1e-10[m]");
    model.component("c").selection("columnEdge2_1").set("ymax", "2[um]+1e-10[m]");
    model.component("c").selection("columnEdge2_1").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge2_1", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge2_1").selection().named("columnEdge2_1");
    model.component("c").mesh("mesh").feature("columnEdge2_1").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge2_1").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge2_1").feature("dist").set("elemcount", 25);
    model.component("c").mesh("mesh").feature("columnEdge2_1").feature("dist").set("elemratio", 210);
    model.component("c").mesh("mesh").feature("columnEdge2_1").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge2_1").feature("dist").set("reverse", true);

    model.component("c").selection().create("columnEdge3_0", "Box");
    model.component("c").selection("columnEdge3_0").set("entitydim", 1);
    model.component("c").selection("columnEdge3_0").set("xmin", "-1e-10");
    model.component("c").selection("columnEdge3_0").set("xmax", "1e-10");
    model.component("c").selection("columnEdge3_0").set("ymin", "2[um]-1e-10[m]");
    model.component("c").selection("columnEdge3_0").set("ymax", "80[um]+1e-10[m]");
    model.component("c").selection("columnEdge3_0").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge3_0", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge3_0").selection().named("columnEdge3_0");
    model.component("c").mesh("mesh").feature("columnEdge3_0").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge3_0").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge3_0").feature("dist").set("elemcount", 30);
    model.component("c").mesh("mesh").feature("columnEdge3_0").feature("dist").set("elemratio", 20);
    model.component("c").mesh("mesh").feature("columnEdge3_0").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge3_0").feature("dist").set("reverse", false);

    model.component("c").selection().create("columnEdge3_1", "Box");
    model.component("c").selection("columnEdge3_1").set("entitydim", 1);
    model.component("c").selection("columnEdge3_1").set("xmin", "60[um]-1e-10[m]");
    model.component("c").selection("columnEdge3_1").set("xmax", "60[um]+1e-10[m]");
    model.component("c").selection("columnEdge3_1").set("ymin", "2[um]-1e-10[m]");
    model.component("c").selection("columnEdge3_1").set("ymax", "80[um]+1e-10[m]");
    model.component("c").selection("columnEdge3_1").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge3_1", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge3_1").selection().named("columnEdge3_1");
    model.component("c").mesh("mesh").feature("columnEdge3_1").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge3_1").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge3_1").feature("dist").set("elemcount", 30);
    model.component("c").mesh("mesh").feature("columnEdge3_1").feature("dist").set("elemratio", 20);
    model.component("c").mesh("mesh").feature("columnEdge3_1").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge3_1").feature("dist").set("reverse", true);

    model.component("c").selection().create("columnEdge4_0", "Box");
    model.component("c").selection("columnEdge4_0").set("entitydim", 1);
    model.component("c").selection("columnEdge4_0").set("xmin", "-1e-10");
    model.component("c").selection("columnEdge4_0").set("xmax", "1e-10");
    model.component("c").selection("columnEdge4_0").set("ymin", "80[um]-1e-10[m]");
    model.component("c").selection("columnEdge4_0").set("ymax", "400[um]+1e-10[m]");
    model.component("c").selection("columnEdge4_0").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge4_0", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge4_0").selection().named("columnEdge4_0");
    model.component("c").mesh("mesh").feature("columnEdge4_0").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge4_0").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge4_0").feature("dist").set("elemcount", 12);
    model.component("c").mesh("mesh").feature("columnEdge4_0").feature("dist").set("elemratio", 3);
    model.component("c").mesh("mesh").feature("columnEdge4_0").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge4_0").feature("dist").set("reverse", false);

    model.component("c").selection().create("columnEdge4_1", "Box");
    model.component("c").selection("columnEdge4_1").set("entitydim", 1);
    model.component("c").selection("columnEdge4_1").set("xmin", "60[um]-1e-10[m]");
    model.component("c").selection("columnEdge4_1").set("xmax", "60[um]+1e-10[m]");
    model.component("c").selection("columnEdge4_1").set("ymin", "80[um]-1e-10[m]");
    model.component("c").selection("columnEdge4_1").set("ymax", "400[um]+1e-10[m]");
    model.component("c").selection("columnEdge4_1").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge4_1", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge4_1").selection().named("columnEdge4_1");
    model.component("c").mesh("mesh").feature("columnEdge4_1").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge4_1").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge4_1").feature("dist").set("elemcount", 12);
    model.component("c").mesh("mesh").feature("columnEdge4_1").feature("dist").set("elemratio", 3);
    model.component("c").mesh("mesh").feature("columnEdge4_1").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge4_1").feature("dist").set("reverse", true);

    model.component("c").selection().create("columnEdge5_0", "Box");
    model.component("c").selection("columnEdge5_0").set("entitydim", 1);
    model.component("c").selection("columnEdge5_0").set("xmin", "-1e-10");
    model.component("c").selection("columnEdge5_0").set("xmax", "1e-10");
    model.component("c").selection("columnEdge5_0").set("ymin", "400[um]-1e-10[m]");
    model.component("c").selection("columnEdge5_0").set("ymax", "Zdom+1e-10[m]");
    model.component("c").selection("columnEdge5_0").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge5_0", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge5_0").selection().named("columnEdge5_0");
    model.component("c").mesh("mesh").feature("columnEdge5_0").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge5_0").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge5_0").feature("dist").set("elemcount", 15);
    model.component("c").mesh("mesh").feature("columnEdge5_0").feature("dist").set("elemratio", 1);
    model.component("c").mesh("mesh").feature("columnEdge5_0").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge5_0").feature("dist").set("reverse", false);

    model.component("c").selection().create("columnEdge5_1", "Box");
    model.component("c").selection("columnEdge5_1").set("entitydim", 1);
    model.component("c").selection("columnEdge5_1").set("xmin", "60[um]-1e-10[m]");
    model.component("c").selection("columnEdge5_1").set("xmax", "60[um]+1e-10[m]");
    model.component("c").selection("columnEdge5_1").set("ymin", "400[um]-1e-10[m]");
    model.component("c").selection("columnEdge5_1").set("ymax", "Zdom+1e-10[m]");
    model.component("c").selection("columnEdge5_1").set("condition", "inside");

    model.component("c").mesh("mesh").create("columnEdge5_1", "Edge");
    model.component("c").mesh("mesh").feature("columnEdge5_1").selection().named("columnEdge5_1");
    model.component("c").mesh("mesh").feature("columnEdge5_1").create("dist", "Distribution");
    model.component("c").mesh("mesh").feature("columnEdge5_1").feature("dist").set("type", "predefined");
    model.component("c").mesh("mesh").feature("columnEdge5_1").feature("dist").set("elemcount", 15);
    model.component("c").mesh("mesh").feature("columnEdge5_1").feature("dist").set("elemratio", 1);
    model.component("c").mesh("mesh").feature("columnEdge5_1").feature("dist").set("growthrate", "exponential");
    model.component("c").mesh("mesh").feature("columnEdge5_1").feature("dist").set("reverse", true);
    model.component("c").mesh("mesh").create("fullMap", "Map");
    model.component("c").mesh("mesh").feature("fullMap").selection().named("g_fullColumn_dom");
    model.component("c").mesh("mesh").feature("fullMap").create("radial", "Distribution");
    model.component("c").mesh("mesh").feature("fullMap").feature("radial").selection().named("columnHorizontal");
    model.component("c").mesh("mesh").feature("fullMap").feature("radial").set("type", "number");
    model.component("c").mesh("mesh").feature("fullMap").feature("radial").set("numelem", 120);
    model.component("c").mesh("mesh").create("tri", "FreeTri");
    model.component("c").mesh("mesh").run();

    model.param().set("tEnd", "123[ns]");

    model.study("std").feature("time").set("tlist", "range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)");
    model.study("std").feature("time").set("rtol", 0.001);
    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").feature().remove("fc1");
    model.sol("sol1").feature("t1").create("directAll", "Direct");
    model.sol("sol1").feature("t1").feature("directAll").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledAll", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledAll").set("linsolver", "directAll");
    model.sol("sol1").feature("t1").feature("coupledAll").set("dtech", "auto");
    model.sol("sol1").feature("t1").feature("coupledAll").set("maxiter", 15);
    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "onevery");
    model.sol("sol1").feature("t1").set("tlist", "range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)");
    model.sol("sol1").feature("t1").set("rtol", 0.001);
    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "1[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("t1").set("consistent", "bweuler");
    model.sol("sol1").feature("v1").set("initmethod", "init");
    model.sol("sol1").feature("v1").feature("c_En").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_En").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_logTgas").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_logTgas").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_Ne").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optD").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optD").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_optU").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_optU").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_Cu_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuD5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuH5_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP1_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_CuP3_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_N_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O2_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1m_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_1p_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_O_W").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qE").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qE").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMr").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qMz").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_qR").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_qR").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_V").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_V_lm").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_V_lm").set("scaleval", "1e-10");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_plas_metal_V0e").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_ODE1").set("scaleval", "1");
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "expr");
    model.sol("sol1").feature("t1").set("maxstepexpressionbdf", "if(t<80[ns],1[ns],0.2[ns])");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "100");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scaleval", "1");

    model.label("Fresh ground-state moving model with shared columns across entire copper and gas height");
    model.label("M120_ready.mph");

    model.sol("sol1").feature("t1").feature("coupledAll").set("jtech", "minimal");

    model.label("M120_ready.mph");

    model.sol().remove("sol1");

    model.component("c").physics().remove("flow");
    model.component("c").physics().remove("temp");
    model.component("c").physics().remove("plas");
    model.component("c").physics().remove("charge");
    model.component("c").physics().remove("ld");
    model.component("c").physics().remove("lu");

    model.param().set("tEnd", "203[ns]");
    model.param().set("tp", "120[ns]");
    model.param().set("cameraZero", "90[ns]");
    model.param().set("w0", "11[um]");
    model.param().set("meltOn", "1");

    model.component("c").variable("boundv").set("feedbackTransmission", "1");
    model.component("c").variable("boundv").set("feedbackPressure", "pAmb");
    model.component("c").variable("boundv").set("feedbackHeat", "0[W/m^2]");
    model.component("c").variable("boundv").set("feedbackReturn", "0[kg/(m^2*s)]");
    model.component("c").variable("boundv").set("pPl", "feedbackPressure");
    model.component("c").variable("boundv").set("urP", "0[m/s]");
    model.component("c").variable("boundv").set("uzP", "0[m/s]");
    model.component("c").variable("boundv").set("mdotReturn", "feedbackReturn");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("recoil", "mdotEvap*vKn");
    model.component("c").variable("boundv")
         .set("Qsurface", "Acu*2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau*exp(-4*log(2)*((t-tp)/tau)^2)*exp(-2*(r/w0)^2)*feedbackTransmission*ncz");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))");
    model.component("c").variable("boundv").set("Qexchange", "feedbackHeat");
    model.component("c").variable("boundv").set("Qthermion", "0[W/m^2]");
    model.component("c").variable("boundv").set("QelectronWall", "0[W/m^2]");
    model.component("c").variable("boundv").set("QworkWall", "0[W/m^2]");

    model.component("c").physics("melt").feature("surf")
         .set("Fbnd", new String[]{"-(feedbackPressure-pAmb+recoil)*ncr", "0", "-(feedbackPressure-pAmb+recoil)*ncz"});
    model.component("c").physics("ht").feature("laser").set("q0", "Qsurface-Qmass+mdot*hCond+Qexchange");
    model.component("c").physics("ht").feature("fluid1").set("u", new String[]{"urCu", "0", "uzCu"});
    model.component("c").physics().create("couplingLedger", "GlobalEquations", "g");
    model.component("c").physics("couplingLedger").feature("ge1")
         .set("name", new String[]{"removedCopperNg", "absorbedLaseruJ", "emittedEnthalpyuJ", "recoilWorkuJ"});
    model.component("c").physics("couplingLedger").feature("ge1")
         .set("equation", new String[]{"removedCopperNgt-intSurf(mdot)/1e-12[kg]", "absorbedLaseruJt-intSurf(Qsurface)/1e-6[J]", "emittedEnthalpyuJt-intSurf(Qmass)/1e-6[J]", "recoilWorkuJt-intSurf(recoil*(urCu*ncr+uzCu*ncz))/1e-6[J]"});
    model.component("c").physics("couplingLedger").feature("ge1")
         .set("initialValueU", new String[]{"0", "0", "0", "0"});

    model.study("std").feature("time").set("tlist", "range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)");
    model.study("std").feature("time").set("rtol", 0.001);
    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").feature().remove("fc1");
    model.sol("sol1").feature("t1").create("directCu", "Direct");
    model.sol("sol1").feature("t1").feature("directCu").set("linsolver", "pardiso");
    model.sol("sol1").feature("t1").create("coupledCu", "FullyCoupled");
    model.sol("sol1").feature("t1").feature("coupledCu").set("linsolver", "directCu");
    model.sol("sol1").feature("t1").feature("coupledCu").set("maxiter", 20);
    model.sol("sol1").feature("t1").feature("coupledCu").set("jtech", "minimal");
    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_etaSurface").set("scaleval", "1");
    model.sol("sol1").feature("v1").feature("c_p").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_p").set("scaleval", "1e7");
    model.sol("sol1").feature("v1").feature("c_T").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_T").set("scaleval", "1000");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scalemethod", "manual");
    model.sol("sol1").feature("v1").feature("c_umelt").set("scaleval", "100");

    model
         .label("Partitioned copper predictor: actual melt/ALE; ambient first iterate; inherited Cu closure, high-temperature applicability NOT validated");

    return model;
  }

  public static void main(String[] args) {
    Model model = run();
    model = run2(model);
    model = run3(model);
    model = run4(model);
    model = run5(model);
    model = run6(model);
    model = run7(model);
    model = run8(model);
    model = run9(model);
    model = run10(model);
    model = run11(model);
    model = run12(model);
    model = run13(model);
    model = run14(model);
    model = run15(model);
    model = run16(model);
    model = run17(model);
    model = run18(model);
    model = run19(model);
    model = run20(model);
    model = run21(model);
    model = run22(model);
    model = run23(model);
    run24(model);
  }

}
