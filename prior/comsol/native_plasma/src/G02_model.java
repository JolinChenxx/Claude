/*
 * G02_model.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

/** Model exported on Nov 8 2026, 22:29 by COMSOL 6.4.0.429. */
public class G02_model {

  public static Model run() {
    Model model = ModelUtil.create("Model");

    model.modelPath("D:\\junlin\\PlasmaPlume\\route1\\models");

    model.param().set("w0", "11[um]");
    model.param().set("Ein", "400[uJ]");
    model.param().set("tau", "60[ns]");
    model.param().set("tp", "120[ns]");
    model.param().set("lambdaL", "1064[nm]");
    model.param().set("Acu", "0.1");
    model.param().set("Rdom", "650[um]");
    model.param().set("Dcu", "100[um]");
    model.param().set("Tamb", "293.15[K]");
    model.param().set("pAmb", "101325[Pa]");
    model.param().set("rhoCu", "8960[kg/m^3]");
    model.param().set("Mcu", "0.063546[kg/mol]");
    model.param().set("rho0", "1[kg/m^3]");
    model.param().set("u0", "1000[m/s]");
    model.param().set("e0", "1e6[J/kg]");
    model.param().set("Bcu", "5815069.632191036[J/kg]");
    model.param().set("Tm", "1358[K]");
    model.param().set("Lm", "204000[J/kg]");
    model.param().set("Lv", "5069000[J/kg]");
    model.param().set("Tv", "2836[K]");
    model.param().set("workCu", "4.65[V]");
    model.param().set("accom", "1");
    model.param().set("seedNe", "1e9[1/m^3]");
    model.param().set("Rinit", "11[um]");
    model.param().set("Hinit", "11[um]");
    model.param().set("tEnd", "1000[ns]");
    model.param().set("mScale", "1e-12[kg]");
    model.param().set("momentumScale", "1e-9[kg*m/s]");
    model.param().set("energyScale", "1[uJ]");
    model.param().set("Mair", "0.79*0.0280134[kg/mol]+0.21*0.0319988[kg/mol]");
    model.param().set("rhoAmb", "pAmb*Mair/(R_const*Tamb)");
    model.param().set("Vinit", "2*pi/3*Rinit^2*Hinit");
    model.param().set("eAir", "2.5*R_const*Tamb/Mair");

    model.component().create("c", true);

    model.component("c").geom().create("g", 2);
    model.component("c").geom("g").axisymmetric(true);
    model.component("c").geom("g").create("cu", "Rectangle");
    model.component("c").geom("g").feature("cu").set("pos", new String[]{"0", "-Dcu"});
    model.component("c").geom("g").feature("cu").set("size", new String[]{"Rdom", "Dcu"});
    model.component("c").geom("g").feature("cu").set("selresult", true);
    model.component("c").geom("g").create("gas", "Rectangle");
    model.component("c").geom("g").feature("gas").set("pos", new String[]{"0", "0"});
    model.component("c").geom("g").feature("gas").set("size", new String[]{"Rdom", "1[um]"});
    model.component("c").geom("g").feature("gas").set("selresult", true);
    model.component("c").geom("g").create("hot", "Rectangle");
    model.component("c").geom("g").feature("hot").set("pos", new String[]{"0", "-15[um]"});
    model.component("c").geom("g").feature("hot").set("size", new String[]{"40[um]", "15[um]"});
    model.component("c").geom("g").feature("hot").set("selresult", true);
    model.component("c").geom("g").run();

    model.component("c").selection().create("surface", "Box");
    model.component("c").selection("surface").set("entitydim", 1);
    model.component("c").selection("surface").set("xmin", "-1e-12");
    model.component("c").selection("surface").set("xmax", "Rdom+1e-12[m]");
    model.component("c").selection("surface").set("ymin", "-1e-12");
    model.component("c").selection("surface").set("ymax", "1e-12");
    model.component("c").selection("surface").set("condition", "inside");
    model.component("c").selection().create("beam", "Box");
    model.component("c").selection("beam").set("entitydim", 1);
    model.component("c").selection("beam").set("xmin", "-1e-12");
    model.component("c").selection("beam").set("xmax", "40[um]+1e-12[m]");
    model.component("c").selection("beam").set("ymin", "-1e-12");
    model.component("c").selection("beam").set("ymax", "1e-12");
    model.component("c").selection("beam").set("condition", "inside");
    model.component("c").selection().create("bottom", "Box");
    model.component("c").selection("bottom").set("entitydim", 1);
    model.component("c").selection("bottom").set("xmin", "-1e-12");
    model.component("c").selection("bottom").set("xmax", "Rdom+1e-12[m]");
    model.component("c").selection("bottom").set("ymin", "-Dcu-1e-12[m]");
    model.component("c").selection("bottom").set("ymax", "-Dcu+1e-12[m]");
    model.component("c").selection("bottom").set("condition", "inside");

    model.component("c").variable().create("gasv");
    model.component("c").variable().create("boundv");
    model.component("c").variable().create("solidv");
    model.component("c").variable("boundv").selection().geom("g", 1);
    model.component("c").variable("boundv").selection().named("surface");
    model.component("c").variable("solidv").selection().named("g_cu_dom");

    model.component("c").cpl().create("intSurf", "Integration");
    model.component("c").cpl("intSurf").selection().geom("g", 1);
    model.component("c").cpl("intSurf").selection().named("surface");
    model.component("c").cpl("intSurf").set("axisym", true);
    model.component("c").cpl("intSurf").set("intorder", 8);
    model.component("c").cpl().create("intCu", "Integration");
    model.component("c").cpl("intCu").selection().named("g_cu_dom");
    model.component("c").cpl("intCu").set("axisym", true);
    model.component("c").cpl("intCu").set("intorder", 8);

    model.component("c").physics().create("plas", "ColdPlasma", "g");
    model.component("c").physics("plas").selection().named("g_gas_dom");
    model.component("c").physics("plas").prop("DiffusionModel").set("DiffusionModel", "GlobalModel");
    model.component("c").physics("plas").prop("Reactor").set("ReactorType", "ClosedReactor");
    model.component("c").physics("plas").prop("Stabilization").set("SourceStabilization", false);
    model.component("c").physics("plas").prop("Stabilization").set("ReactionSourceStabilization", false);

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

    return model;
  }

  public static Model run2(Model model) {
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

    return model;
  }

  public static Model run3(Model model) {
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
    model.component("c").physics("plas").feature("N2_1p").set("InitIon", true);

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

    return model;
  }

  public static Model run4(Model model) {
    model.component("c").variable("boundv")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("c").variable("boundv")
         .set("QelectronWall", "e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)");
    model.component("c").variable("boundv").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("c").variable("boundv").set("Qthermion", "QelectronWall+QworkWall");

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

    model.component("c").variable("gasv").set("nOtherNeutral", "nN+nO+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5");
    model.component("c").variable("gasv").set("nuEN", "nN2*kmN2+nO2*kmO2+nOtherNeutral*kmCu");

    model.component("c").physics("plas").feature("N2").set("n0", "1[1/m^3]");

    model.component("c").variable("gasv").set("plas.wO2", "wO2");

    model.component("c").physics("plas").feature("O2").set("n0", "0.21*pAmb/(k_B_const*Tamb)");

    model.component("c").variable("gasv").set("plas.wN", "wN");

    model.component("c").physics("plas").feature("N").set("n0", "1[1/m^3]");

    model.component("c").variable("gasv").set("plas.wO", "wO");

    model.component("c").physics("plas").feature("O").set("n0", "1[1/m^3]");

    model.component("c").variable("gasv").set("plas.wN2_1p", "wN2_1p");

    model.component("c").physics("plas").feature("N2_1p").set("n0", "1e7[1/m^3]");

    model.component("c").variable("gasv").set("plas.wO2_1p", "wO2_1p");

    model.component("c").physics("plas").feature("O2_1p").set("n0", "1e7[1/m^3]");

    model.component("c").variable("gasv").set("plas.wN_1p", "wN_1p");

    model.component("c").physics("plas").feature("N_1p").set("n0", "1e7[1/m^3]");

    model.component("c").variable("gasv").set("plas.wO_1p", "wO_1p");

    model.component("c").physics("plas").feature("O_1p").set("n0", "1e7[1/m^3]");

    model.component("c").variable("gasv").set("plas.wO_1m", "wO_1m");

    model.component("c").physics("plas").feature("O_1m").set("n0", "1e7[1/m^3]");

    model.component("c").variable("gasv").set("plas.wO2_1m", "wO2_1m");

    model.component("c").physics("plas").feature("O2_1m").set("n0", "1e7[1/m^3]");

    model.component("c").variable("gasv").set("plas.wCu", "wCu");

    model.component("c").physics("plas").feature("Cu").set("n0", "1[1/m^3]");

    model.component("c").variable("gasv").set("plas.wCuD5", "wCuD5");

    model.component("c").physics("plas").feature("CuD5").set("n0", "1[1/m^3]");

    model.component("c").variable("gasv").set("plas.wCuD3", "wCuD3");

    model.component("c").physics("plas").feature("CuD3").set("n0", "1[1/m^3]");

    model.component("c").variable("gasv").set("plas.wCuP1", "wCuP1");

    model.component("c").physics("plas").feature("CuP1").set("n0", "1[1/m^3]");

    model.component("c").variable("gasv").set("plas.wCuP3", "wCuP3");

    model.component("c").physics("plas").feature("CuP3").set("n0", "1[1/m^3]");

    model.component("c").variable("gasv").set("plas.wCuH3", "wCuH3");

    model.component("c").physics("plas").feature("CuH3").set("n0", "1[1/m^3]");

    model.component("c").variable("gasv").set("plas.wCuH5", "wCuH5");

    model.component("c").physics("plas").feature("CuH5").set("n0", "1[1/m^3]");

    model.component("c").variable("gasv").set("plas.wCu_1p", "wCu_1p");

    model.component("c").physics("plas").feature("Cu_1p").set("n0", "1e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("n0", "seedNe-2e7[1/m^3]");
    model.component("c").physics("plas").feature("N2_1p").set("InitIon", false);
    model.component("c").physics("plas").feature("pes1").set("Tgm", "Tg");
    model.component("c").physics("plas").feature("pes1").set("Tgm0", "Tamb");
    model.component("c").physics("plas").feature("pes1").set("p0", "pAmb");
    model.component("c").physics("plas").feature("pes1").set("ebarinitgm", "1.5*k_B_const*Tamb/e_const");

    model.component("c").variable("gasv").set("Tg", "exp(logGasT)*1[K]");
    model.component("c").variable("gasv").set("Tgas", "Tg");
    model.component("c").variable("gasv").set("rhoP", "plas.rho");
    model.component("c").variable("gasv").set("urP", "0[m/s]");
    model.component("c").variable("gasv").set("uzP", "0[m/s]");
    model.component("c").variable("gasv").set("Dgas", "1e-5[m^2/s]");
    model.component("c").variable("gasv").set("Rp", "Rinit*exp(logRadius)");
    model.component("c").variable("gasv").set("Hp", "Hinit*exp(logHeight)");
    model.component("c").variable("gasv").set("Vp", "2*pi/3*Rp^2*Hp");
    model.component("c").variable("gasv").set("Mgas", "rhoP*Vp");
    model.component("c").variable("gasv").set("vRadius", "momentumScale*pRadius/Mgas");
    model.component("c").variable("gasv").set("vHeight", "momentumScale*pHeight/Mgas");
    model.component("c").variable("gasv").set("volumeRate", "2*vRadius/Rp+vHeight/Hp");
    model.component("c").variable("gasv").set("Vdot", "Vp*volumeRate");
    model.component("c").variable("gasv").set("Msweep", "rhoAmb*max(Vdot,0[m^3/s])");
    model.component("c").variable("gasv").set("Kinetic", "Mgas*(2*vRadius^2+vHeight^2)/10");
    model.component("c").variable("gasv").set("pPl", "plas.pA+pe");
    model.component("c").variable("gasv").set("Req", "(3*Vp/(4*pi))^(1/3)");
    model.component("c").variable("gasv").set("pulse", "exp(-4*log(2)*((t-tp)/tau)^2)");
    model.component("c").variable("gasv").set("Pincident", "Ein*sqrt(4*log(2)/pi)/tau*pulse");
    model.component("c").variable("gasv").set("Ipeak", "2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");
    model.component("c").variable("gasv").set("beamCoverage", "1-exp(-2*(Rp/w0)^2)");
    model.component("c").variable("gasv").set("beamTransmission", "exp(-alphaLaser*Hp)");
    model.component("c").variable("gasv")
         .set("Plaser", "Pincident*beamCoverage*(1-beamTransmission)*(1+(1-Acu)*beamTransmission)");
    model.component("c").variable("gasv").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*Req");
    model.component("c").variable("gasv")
         .set("beta1", "if(tauLine1<0.01,1-3*tauLine1/4+2*tauLine1^2/5,3/(4*tauLine1)*(1-1/(2*tauLine1^2)+(1/tauLine1+1/(2*tauLine1^2))*exp(-2*tauLine1)))");
    model.component("c").variable("gasv").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*Req");
    model.component("c").variable("gasv")
         .set("beta2", "if(tauLine2<0.01,1-3*tauLine2/4+2*tauLine2^2/5,3/(4*tauLine2)*(1-1/(2*tauLine2^2)+(1/tauLine2+1/(2*tauLine2^2))*exp(-2*tauLine2)))");
    model.component("c").variable("gasv").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*Req");
    model.component("c").variable("gasv")
         .set("beta3", "if(tauLine3<0.01,1-3*tauLine3/4+2*tauLine3^2/5,3/(4*tauLine3)*(1-1/(2*tauLine3^2)+(1/tauLine3+1/(2*tauLine3^2))*exp(-2*tauLine3)))");
    model.component("c").variable("gasv").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*Req");
    model.component("c").variable("gasv")
         .set("beta4", "if(tauLine4<0.01,1-3*tauLine4/4+2*tauLine4^2/5,3/(4*tauLine4)*(1-1/(2*tauLine4^2)+(1/tauLine4+1/(2*tauLine4^2))*exp(-2*tauLine4)))");
    model.component("c").variable("gasv")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*Req");
    model.component("c").variable("gasv")
         .set("beta5", "if(tauLine5<0.01,1-3*tauLine5/4+2*tauLine5^2/5,3/(4*tauLine5)*(1-1/(2*tauLine5^2)+(1/tauLine5+1/(2*tauLine5^2))*exp(-2*tauLine5)))");
    model.component("c").variable("gasv").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*Req");
    model.component("c").variable("gasv")
         .set("beta6", "if(tauLine6<0.01,1-3*tauLine6/4+2*tauLine6^2/5,3/(4*tauLine6)*(1-1/(2*tauLine6^2)+(1/tauLine6+1/(2*tauLine6^2))*exp(-2*tauLine6)))");
    model.component("c").variable("gasv").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*Req");
    model.component("c").variable("gasv")
         .set("beta7", "if(tauLine7<0.01,1-3*tauLine7/4+2*tauLine7^2/5,3/(4*tauLine7)*(1-1/(2*tauLine7^2)+(1/tauLine7+1/(2*tauLine7^2))*exp(-2*tauLine7)))");
    model.component("c").variable("gasv").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*Req");
    model.component("c").variable("gasv")
         .set("beta8", "if(tauLine8<0.01,1-3*tauLine8/4+2*tauLine8^2/5,3/(4*tauLine8)*(1-1/(2*tauLine8^2)+(1/tauLine8+1/(2*tauLine8^2))*exp(-2*tauLine8)))");

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
    model.component("c").variable("solidv").set("fL", "0.5*(1+(T-Tm)/sqrt((T-Tm)^2+100[K^2]))");
    model.component("c").variable("solidv")
         .set("cpCu", "385[J/(kg*K)]*(1-fL)+572[J/(kg*K)]*fL+Lm*50[K^2]/((T-Tm)^2+100[K^2])^1.5");
    model.component("c").variable("solidv")
         .set("hCond", "385[J/(kg*K)]*(T-Tamb)+187[J/(kg*K)]*0.5*(T-Tamb+sqrt((T-Tm)^2+100[K^2])-sqrt((Tamb-Tm)^2+100[K^2]))+Lm*(fL-0.5*(1+(Tamb-Tm)/sqrt((Tamb-Tm)^2+100[K^2])))");
    model.component("c").variable("solidv").set("kCu", "385[W/(m*K)]*(1-fL)+165[W/(m*K)]*fL");

    model.component("c").material().create("copper", "Common");
    model.component("c").material("copper").selection().named("g_cu_dom");
    model.component("c").material("copper").propertyGroup("def").set("density", "rhoCu");
    model.component("c").material("copper").propertyGroup("def").set("heatcapacity", "cpCu");
    model.component("c").material("copper").propertyGroup("def").set("thermalconductivity", "kCu");

    model.component("c").variable("boundv").set("contact", "flc2hs(Rp-r,1[um])");
    model.component("c").variable("boundv")
         .set("Qsurface", "Acu*Ipeak*pulse*exp(-2*(r/w0)^2)*(1-contact*(1-beamTransmission))");
    model.component("c").variable("boundv")
         .set("mdotReturn", "contact*rhoCopper*sqrt(k_B_const*Tg/(2*pi*Mcu/N_A_const))");
    model.component("c").variable("boundv").set("mdot", "mdotEvap-mdotReturn");
    model.component("c").variable("boundv").set("emitWeight_Cu", "2*exp(-0.0[V]*e_const/(k_B_const*T))");
    model.component("c").variable("boundv").set("emitWeight_CuD5", "6*exp(-1.388948[V]*e_const/(k_B_const*T))");
    model.component("c").variable("boundv").set("emitWeight_CuD3", "4*exp(-1.6422256[V]*e_const/(k_B_const*T))");
    model.component("c").variable("boundv").set("emitWeight_CuP1", "2*exp(-3.7858977[V]*e_const/(k_B_const*T))");
    model.component("c").variable("boundv").set("emitWeight_CuP3", "4*exp(-3.816692[V]*e_const/(k_B_const*T))");
    model.component("c").variable("boundv").set("emitWeight_CuH3", "4*exp(-6.1911751[V]*e_const/(k_B_const*T))");
    model.component("c").variable("boundv").set("emitWeight_CuH5", "6*exp(-6.1920252[V]*e_const/(k_B_const*T))");
    model.component("c").variable("boundv")
         .set("emitWeight_Cu_1p", "1*exp((workCu-7.72638[V])*e_const/(k_B_const*T))");
    model.component("c").variable("boundv")
         .set("emitPartition", "0+emitWeight_Cu+emitWeight_CuD5+emitWeight_CuD3+emitWeight_CuP1+emitWeight_CuP3+emitWeight_CuH3+emitWeight_CuH5+emitWeight_Cu_1p");
    model.component("c").variable("boundv").set("emitFraction_Cu", "emitWeight_Cu/emitPartition");
    model.component("c").variable("boundv")
         .set("source_Cu", "mdotEvap*emitFraction_Cu-mdotReturn*plas.n_wCu/max(nCuTotal,1[1/m^3])");
    model.component("c").variable("boundv").set("emitFraction_CuD5", "emitWeight_CuD5/emitPartition");
    model.component("c").variable("boundv")
         .set("source_CuD5", "mdotEvap*emitFraction_CuD5-mdotReturn*plas.n_wCuD5/max(nCuTotal,1[1/m^3])");
    model.component("c").variable("boundv").set("emitFraction_CuD3", "emitWeight_CuD3/emitPartition");
    model.component("c").variable("boundv")
         .set("source_CuD3", "mdotEvap*emitFraction_CuD3-mdotReturn*plas.n_wCuD3/max(nCuTotal,1[1/m^3])");
    model.component("c").variable("boundv").set("emitFraction_CuP1", "emitWeight_CuP1/emitPartition");
    model.component("c").variable("boundv")
         .set("source_CuP1", "mdotEvap*emitFraction_CuP1-mdotReturn*plas.n_wCuP1/max(nCuTotal,1[1/m^3])");
    model.component("c").variable("boundv").set("emitFraction_CuP3", "emitWeight_CuP3/emitPartition");
    model.component("c").variable("boundv")
         .set("source_CuP3", "mdotEvap*emitFraction_CuP3-mdotReturn*plas.n_wCuP3/max(nCuTotal,1[1/m^3])");
    model.component("c").variable("boundv").set("emitFraction_CuH3", "emitWeight_CuH3/emitPartition");
    model.component("c").variable("boundv")
         .set("source_CuH3", "mdotEvap*emitFraction_CuH3-mdotReturn*plas.n_wCuH3/max(nCuTotal,1[1/m^3])");
    model.component("c").variable("boundv").set("emitFraction_CuH5", "emitWeight_CuH5/emitPartition");
    model.component("c").variable("boundv")
         .set("source_CuH5", "mdotEvap*emitFraction_CuH5-mdotReturn*plas.n_wCuH5/max(nCuTotal,1[1/m^3])");
    model.component("c").variable("boundv").set("emitFraction_Cu_1p", "emitWeight_Cu_1p/emitPartition");
    model.component("c").variable("boundv")
         .set("source_Cu_1p", "mdotEvap*emitFraction_Cu_1p-mdotReturn*plas.n_wCu_1p/max(nCuTotal,1[1/m^3])");
    model.component("c").variable("boundv")
         .set("emitMeanEnergy", "0[V]+emitFraction_Cu*0.0[V]+emitFraction_CuD5*1.388948[V]+emitFraction_CuD3*1.6422256[V]+emitFraction_CuP1*3.7858977[V]+emitFraction_CuP3*3.816692[V]+emitFraction_CuH3*6.1911751[V]+emitFraction_CuH5*6.1920252[V]+emitFraction_Cu_1p*7.72638[V]");
    model.component("c").variable("gasv")
         .set("returnMeanEnergy", "0[V]+plas.n_wCu/max(nCuTotal,1[1/m^3])*0.0[V]+plas.n_wCuD5/max(nCuTotal,1[1/m^3])*1.388948[V]+plas.n_wCuD3/max(nCuTotal,1[1/m^3])*1.6422256[V]+plas.n_wCuP1/max(nCuTotal,1[1/m^3])*3.7858977[V]+plas.n_wCuP3/max(nCuTotal,1[1/m^3])*3.816692[V]+plas.n_wCuH3/max(nCuTotal,1[1/m^3])*6.1911751[V]+plas.n_wCuH5/max(nCuTotal,1[1/m^3])*6.1920252[V]+plas.n_wCu_1p/max(nCuTotal,1[1/m^3])*7.72638[V]");
    model.component("c").variable("boundv")
         .set("electronSourceEnergy", "2*k_B_const*T*mdotEvap*N_A_const/Mcu*emitFraction_Cu_1p-2.5*k_B_const*TeK*mdotReturn*N_A_const/Mcu*nCu_1p/max(nCuTotal,1[1/m^3])");
    model.component("c").variable("boundv")
         .set("Qmass", "mdotEvap*(Bcu+3*R_const*T/Mcu+F_const*emitMeanEnergy/Mcu)-mdotReturn*(Bcu+2.5*R_const*Tg/Mcu+F_const*returnMeanEnergy/Mcu)+electronSourceEnergy");
    model.component("c").variable("boundv")
         .set("Qexchange", "contact*2.5*k_B_const*nHeavy*sqrt(8*k_B_const*Tg/(pi*Mair/N_A_const))/4*(Tg-T)");
    model.component("c").variable("gasv").set("Msource", "intSurf(mdot)+Msweep");
    model.component("c").variable("gasv").set("Psource", "intSurf(Qmass-Qexchange)");
    model.component("c").variable("gasv").set("PeSource", "intSurf(electronSourceEnergy)");

    model.component("c").physics("plas").feature("pes1").featureInfo("info").set("plas.Vol_gm", new String[]{"Vp"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("plas.mfinGm", new String[]{"Msource"});
    model.component("c").physics("plas").feature("O2").featureInfo("info")
         .set("plas.R_wO2", new String[]{"(-plas.r_27-plas.r_28-plas.r_40-plas.r_41-plas.r_42-plas.r_43-plas.r_101+plas.r_102+plas.r_103+plas.r_105+plas.r_107+plas.r_108+2*plas.r_109+plas.r_111+plas.r_113-plas.r_120+plas.r_121-plas.r_122+plas.r_123-plas.r_124+plas.r_125)+(Msweep*(0.21*0.0319988[kg/mol]/Mair)/Vp)/plas.M_wO2"});
    model.component("c").physics("plas").feature("N").featureInfo("info")
         .set("plas.R_wN", new String[]{"(-plas.r_75+plas.r_76+plas.r_77+2*plas.r_99+plas.r_110+plas.r_111+2*plas.r_114-2*plas.r_115+2*plas.r_116-2*plas.r_117+2*plas.r_118-2*plas.r_119)+(0[kg/(m^3*s)])/plas.M_wN"});
    model.component("c").physics("plas").feature("O").featureInfo("info")
         .set("plas.R_wO", new String[]{"(plas.r_27+plas.r_28+2*plas.r_40+2*plas.r_41+2*plas.r_42-plas.r_78+plas.r_79+plas.r_80+2*plas.r_100-plas.r_102+plas.r_104+plas.r_106+plas.r_108+plas.r_110+2*plas.r_112+plas.r_113+2*plas.r_120-2*plas.r_121+2*plas.r_122-2*plas.r_123+2*plas.r_124-2*plas.r_125)+(0[kg/(m^3*s)])/plas.M_wO"});
    model.component("c").physics("plas").feature("N2_1p").featureInfo("info")
         .set("plas.R_wN2_1p", new String[]{"(plas.r_25+plas.r_26-plas.r_99-plas.r_106-plas.r_107)+(0[kg/(m^3*s)])/plas.M_wN2_1p"});
    model.component("c").physics("plas").feature("O2_1p").featureInfo("info")
         .set("plas.R_wO2_1p", new String[]{"(plas.r_43-plas.r_100-plas.r_108-plas.r_109)+(0[kg/(m^3*s)])/plas.M_wO2_1p"});
    model.component("c").physics("plas").feature("N_1p").featureInfo("info")
         .set("plas.R_wN_1p", new String[]{"(plas.r_75-plas.r_76-plas.r_77-plas.r_110-plas.r_111)+(0[kg/(m^3*s)])/plas.M_wN_1p"});
    model.component("c").physics("plas").feature("O_1p").featureInfo("info")
         .set("plas.R_wO_1p", new String[]{"(plas.r_78-plas.r_79-plas.r_80-plas.r_112-plas.r_113)+(0[kg/(m^3*s)])/plas.M_wO_1p"});
    model.component("c").physics("plas").feature("O_1m").featureInfo("info")
         .set("plas.R_wO_1m", new String[]{"(plas.r_27+plas.r_28-plas.r_102-plas.r_104-plas.r_106-plas.r_108-plas.r_110-plas.r_112)+(0[kg/(m^3*s)])/plas.M_wO_1m"});
    model.component("c").physics("plas").feature("O2_1m").featureInfo("info")
         .set("plas.R_wO2_1m", new String[]{"(plas.r_101-plas.r_103-plas.r_105-plas.r_107-plas.r_109-plas.r_111-plas.r_113)+(0[kg/(m^3*s)])/plas.M_wO2_1m"});
    model.component("c").physics("plas").feature("Cu").featureInfo("info")
         .set("plas.R_wCu", new String[]{"(-plas.r_44+plas.r_45-plas.r_46+plas.r_47-plas.r_48+plas.r_49-plas.r_50+plas.r_51-plas.r_72+plas.r_73+plas.r_52+plas.r_53+plas.r_74+plas.r_104+plas.r_105)+(intSurf(source_Cu)/Vp)/plas.M_wCu"});
    model.component("c").physics("plas").feature("CuD5").featureInfo("info")
         .set("plas.R_wCuD5", new String[]{"(plas.r_44-plas.r_45-plas.r_55+plas.r_56-plas.r_81+plas.r_82+plas.r_54+plas.r_83)+(intSurf(source_CuD5)/Vp)/plas.M_wCuD5"});
    model.component("c").physics("plas").feature("CuD3").featureInfo("info")
         .set("plas.R_wCuD3", new String[]{"(plas.r_46-plas.r_47-plas.r_67+plas.r_68-plas.r_70+plas.r_71-plas.r_84+plas.r_85+plas.r_66+plas.r_69+plas.r_86)+(intSurf(source_CuD3)/Vp)/plas.M_wCuD3"});
    model.component("c").physics("plas").feature("CuP1").featureInfo("info")
         .set("plas.R_wCuP1", new String[]{"(plas.r_48-plas.r_49-plas.r_58+plas.r_59+plas.r_70-plas.r_71-plas.r_87+plas.r_88-plas.r_53+plas.r_57-plas.r_69+plas.r_89)+(intSurf(source_CuP1)/Vp)/plas.M_wCuP1"});
    model.component("c").physics("plas").feature("CuP3").featureInfo("info")
         .set("plas.R_wCuP3", new String[]{"(plas.r_50-plas.r_51+plas.r_55-plas.r_56-plas.r_61+plas.r_62-plas.r_64+plas.r_65+plas.r_67-plas.r_68-plas.r_90+plas.r_91-plas.r_52-plas.r_54+plas.r_60+plas.r_63-plas.r_66+plas.r_92)+(intSurf(source_CuP3)/Vp)/plas.M_wCuP3"});
    model.component("c").physics("plas").feature("CuH3").featureInfo("info")
         .set("plas.R_wCuH3", new String[]{"(plas.r_58-plas.r_59+plas.r_64-plas.r_65-plas.r_93+plas.r_94-plas.r_57-plas.r_63+plas.r_95)+(intSurf(source_CuH3)/Vp)/plas.M_wCuH3"});
    model.component("c").physics("plas").feature("CuH5").featureInfo("info")
         .set("plas.R_wCuH5", new String[]{"(plas.r_61-plas.r_62-plas.r_96+plas.r_97-plas.r_60+plas.r_98)+(intSurf(source_CuH5)/Vp)/plas.M_wCuH5"});
    model.component("c").physics("plas").feature("Cu_1p").featureInfo("info")
         .set("plas.R_wCu_1p", new String[]{"(plas.r_72-plas.r_73+plas.r_81-plas.r_82+plas.r_84-plas.r_85+plas.r_87-plas.r_88+plas.r_90-plas.r_91+plas.r_93-plas.r_94+plas.r_96-plas.r_97-plas.r_74-plas.r_83-plas.r_86-plas.r_89-plas.r_92-plas.r_95-plas.r_98-plas.r_104-plas.r_105)+(intSurf(source_Cu_1p)/Vp)/plas.M_wCu_1p"});
    model.component("c").physics("plas").feature("pes1").featureInfo("info")
         .set("root.c.plas.pes1.weak$5", new String[]{"(d(plas.rho,t)+plas.rho*volumeRate)*test(plas.pA)"});
    model.component("c").physics("plas").feature("pes1")
         .set("Pgm", "Plaser+PeSource-Vp*(QeRR+QeAttach+Qff+Qei+(Ue+pe)*volumeRate)");
    model.component("c").physics().create("ht", "HeatTransfer", "g");
    model.component("c").physics("ht").selection().named("g_cu_dom");
    model.component("c").physics("ht").feature("init1").set("Tinit", "Tamb");
    model.component("c").physics("ht").create("laser", "HeatFluxBoundary", 1);
    model.component("c").physics("ht").feature("laser").selection().named("surface");
    model.component("c").physics("ht").feature("laser").set("q0", "Qsurface-Qmass+Qexchange");
    model.component("c").physics("ht").create("base", "TemperatureBoundary", 1);
    model.component("c").physics("ht").feature("base").selection().named("bottom");
    model.component("c").physics("ht").feature("base").set("T0", "Tamb");
    model.component("c").physics().create("bulk", "GlobalEquations", "g");
    model.component("c").physics("bulk").feature("ge1")
         .set("name", new String[]{"logRadius", "logHeight", "pRadius", "pHeight", "gasEnergy", "logGasT", "absorbedEnergy", "radiatedEnergy", "ambientWork", "entrainedEnergy", 
         "removedMass"});
    model.component("c").physics("bulk").feature("ge1")
         .set("equation", new String[]{"logRadiust-vRadius/Rp", "logHeightt-vHeight/Hp", "pRadiust-5*(pPl-pAmb)*Vp/(Rp*momentumScale)", "pHeightt-5*(pPl-pAmb)*Vp/(Hp*momentumScale)", "gasEnergyt-(Psource+Plaser-Qrad*Vp-pAmb*Vdot+eAir*Msweep)/energyScale", "(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue)-Kinetic)/energyScale", "absorbedEnergyt-(intSurf(Qsurface)+Plaser)/energyScale", "radiatedEnergyt-Qrad*Vp/energyScale", "ambientWorkt-pAmb*Vdot/energyScale", "entrainedEnergyt-eAir*Msweep/energyScale", 
         "removedMasst-intSurf(mdot)/mScale"});
    model.component("c").physics("bulk").feature("ge1")
         .set("initialValueU", new String[]{"0", "0", "0", "0", "2.5*pAmb*Vinit/energyScale", "log(Tamb/1[K])", "0", "0", "0", "0", 
         "0"});

    model.component("c").mesh().create("mesh");
    model.component("c").mesh("mesh").feature("size").set("custom", true);
    model.component("c").mesh("mesh").feature("size").set("hmax", "15[um]");
    model.component("c").mesh("mesh").feature("size").set("hmin", "0.3[um]");
    model.component("c").mesh("mesh").feature("size").set("hgrad", 1.3);
    model.component("c").mesh("mesh").create("hotSize", "Size");
    model.component("c").mesh("mesh").feature("hotSize").selection().named("g_hot_dom");
    model.component("c").mesh("mesh").feature("hotSize").set("custom", true);
    model.component("c").mesh("mesh").feature("hotSize").set("hmaxactive", true);
    model.component("c").mesh("mesh").feature("hotSize").set("hmax", "0.5[um]");
    model.component("c").mesh("mesh").create("tri", "FreeTri");
    model.component("c").mesh("mesh").run();

    model.study().create("std");
    model.study("std").create("time", "Transient");
    model.study("std").feature("time").set("tlist", "range(0,1[ns],tEnd)");
    model.study("std").feature("time").set("rtol", 1.0E-4);
    model.study("std").createAutoSequences("sol");

    model.sol("sol1").feature("t1").set("maxorder", 2);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", "0.5[ns]");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("initialstepbdf", "0.001[ns]");

    model.label("Reduced native global Plasma, expanding ellipsoid and target heat; not spatial/sheath validation");

    return model;
  }

  public static void main(String[] args) {
    Model model = run();
    model = run2(model);
    model = run3(model);
    run4(model);
  }

}
