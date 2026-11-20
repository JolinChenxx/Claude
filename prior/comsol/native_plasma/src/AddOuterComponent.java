import com.comsol.model.*;
/** Generated recorded API component with separate native field names. */
public class AddOuterComponent {
public static void apply(Model model){
part0(model);
part1(model);
part2(model);
part3(model);
part4(model);
part5(model);
part6(model);
part7(model);
part8(model);
}
static void part0(Model model){
    model.component().create("outer", true);
    model.component("outer").geom().create("og", 2);
    model.component("outer").geom("og").axisymmetric(true);
    model.component("outer").geom("og").create("cu", "Rectangle");
    model.component("outer").geom("og").feature("cu").set("pos", new String[]{"0", "-Dcu"});
    model.component("outer").geom("og").feature("cu").set("size", new String[]{"Rdom", "Dcu"});
    model.component("outer").geom("og").feature("cu").set("selresult", true);
    model.component("outer").geom("og").create("gas", "Rectangle");
    model.component("outer").geom("og").feature("gas").set("pos", new String[]{"0", "0"});
    model.component("outer").geom("og").feature("gas").set("size", new String[]{"Rdom", "1[um]"});
    model.component("outer").geom("og").feature("gas").set("selresult", true);
    model.component("outer").geom("og").create("hot", "Rectangle");
    model.component("outer").geom("og").feature("hot").set("pos", new String[]{"0", "-15[um]"});
    model.component("outer").geom("og").feature("hot").set("size", new String[]{"40[um]", "15[um]"});
    model.component("outer").geom("og").feature("hot").set("selresult", true);
    model.component("outer").geom("og").run();
    model.component("outer").selection().create("outerSurface", "Box");
    model.component("outer").selection("outerSurface").set("entitydim", 1);
    model.component("outer").selection("outerSurface").set("xmin", "-1e-12");
    model.component("outer").selection("outerSurface").set("xmax", "Rdom+1e-12[m]");
    model.component("outer").selection("outerSurface").set("ymin", "-1e-12");
    model.component("outer").selection("outerSurface").set("ymax", "1e-12");
    model.component("outer").selection("outerSurface").set("condition", "inside");
    model.component("outer").selection().create("outerBeam", "Box");
    model.component("outer").selection("outerBeam").set("entitydim", 1);
    model.component("outer").selection("outerBeam").set("xmin", "-1e-12");
    model.component("outer").selection("outerBeam").set("xmax", "40[um]+1e-12[m]");
    model.component("outer").selection("outerBeam").set("ymin", "-1e-12");
    model.component("outer").selection("outerBeam").set("ymax", "1e-12");
    model.component("outer").selection("outerBeam").set("condition", "inside");
    model.component("outer").selection().create("outerBottom", "Box");
    model.component("outer").selection("outerBottom").set("entitydim", 1);
    model.component("outer").selection("outerBottom").set("xmin", "-1e-12");
    model.component("outer").selection("outerBottom").set("xmax", "Rdom+1e-12[m]");
    model.component("outer").selection("outerBottom").set("ymin", "-Dcu-1e-12[m]");
    model.component("outer").selection("outerBottom").set("ymax", "-Dcu+1e-12[m]");
    model.component("outer").selection("outerBottom").set("condition", "inside");
    model.component("outer").variable().create("outerGas");
    model.component("outer").variable().create("outerBoundary");
    model.component("outer").variable().create("outerSolid");
    model.component("outer").variable("outerBoundary").selection().geom("og", 1);
    model.component("outer").variable("outerBoundary").selection().named("outerSurface");
    model.component("outer").variable("outerSolid").selection().named("og_cu_dom");
    model.component("outer").cpl().create("outerIntSurf", "Integration");
    model.component("outer").cpl("outerIntSurf").selection().geom("og", 1);
    model.component("outer").cpl("outerIntSurf").selection().named("outerSurface");
    model.component("outer").cpl("outerIntSurf").set("axisym", true);
    model.component("outer").cpl("outerIntSurf").set("intorder", 8);
    model.component("outer").cpl().create("outerIntCu", "Integration");
    model.component("outer").cpl("outerIntCu").selection().named("og_cu_dom");
    model.component("outer").cpl("outerIntCu").set("axisym", true);
    model.component("outer").cpl("outerIntCu").set("intorder", 8);
    model.component("outer").physics().create("plas2", "ColdPlasma", "og");
    model.component("outer").physics("plas2").field("logelectronenergydensity").field("outerEn");
    model.component("outer").physics("plas2").selection().named("og_gas_dom");
    model.component("outer").physics("plas2").prop("DiffusionModel").set("DiffusionModel", "GlobalModel");
    model.component("outer").physics("plas2").prop("Reactor").set("ReactorType", "ClosedReactor");
    model.component("outer").physics("plas2").prop("Stabilization").set("SourceStabilization", false);
    model.component("outer").physics("plas2").prop("Stabilization").set("ReactionSourceStabilization", false);
    model.component("outer").physics("plas2").create("N2_0", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_0").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_0").set("kf", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_0").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("N2_0").set("mratio", "1.95e-05");
    model.component("outer").physics("plas2").create("N2_1", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_1").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_1").set("kf", "10^lr_N2_1(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_1").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_1").set("de", "0.02");
    model.component("outer").physics("plas2").create("N2_2", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_2").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_2").set("kf", "10^lr_N2_2(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_2").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_2").set("de", "0.29");
    model.component("outer").physics("plas2").create("N2_3", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_3").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_3").set("kf", "10^lr_N2_3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_3").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_3").set("de", "0.29");
    model.component("outer").physics("plas2").create("N2_4", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_4").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_4").set("kf", "10^lr_N2_4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_4").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_4").set("de", "0.59");
    model.component("outer").physics("plas2").create("N2_5", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_5").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_5").set("kf", "10^lr_N2_5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_5").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_5").set("de", "0.88");
    model.component("outer").physics("plas2").create("N2_6", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_6").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_6").set("kf", "10^lr_N2_6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_6").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_6").set("de", "1.17");
    model.component("outer").physics("plas2").create("N2_7", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_7").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_7").set("kf", "10^lr_N2_7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_7").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_7").set("de", "1.47");
    model.component("outer").physics("plas2").create("N2_8", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_8").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_8").set("kf", "10^lr_N2_8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_8").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_8").set("de", "1.76");
    model.component("outer").physics("plas2").create("N2_9", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_9").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_9").set("kf", "10^lr_N2_9(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_9").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_9").set("de", "2.06");
    model.component("outer").physics("plas2").create("N2_10", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_10").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_10")
         .set("kf", "10^lr_N2_10(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_10").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_10").set("de", "2.35");
    model.component("outer").physics("plas2").create("N2_11", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_11").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_11")
         .set("kf", "10^lr_N2_11(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_11").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_11").set("de", "6.17");
    model.component("outer").physics("plas2").create("N2_12", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_12").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_12")
         .set("kf", "10^lr_N2_12(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_12").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_12").set("de", "7.0");
    model.component("outer").physics("plas2").create("N2_13", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_13").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_13")
         .set("kf", "10^lr_N2_13(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_13").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_13").set("de", "7.35");
    model.component("outer").physics("plas2").create("N2_14", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_14").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_14")
         .set("kf", "10^lr_N2_14(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_14").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_14").set("de", "7.36");
    model.component("outer").physics("plas2").create("N2_15", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_15").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_15")
         .set("kf", "10^lr_N2_15(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_15").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_15").set("de", "7.8");
    model.component("outer").physics("plas2").create("N2_16", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_16").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_16")
         .set("kf", "10^lr_N2_16(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_16").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_16").set("de", "8.16");
    model.component("outer").physics("plas2").create("N2_17", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_17").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_17")
         .set("kf", "10^lr_N2_17(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_17").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_17").set("de", "8.4");
    model.component("outer").physics("plas2").create("N2_18", "ElectronImpactReaction", 2);
}
static void part1(Model model){

    model.component("outer").physics("plas2").feature("N2_18").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_18")
         .set("kf", "10^lr_N2_18(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_18").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_18").set("de", "8.55");
    model.component("outer").physics("plas2").create("N2_19", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_19").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_19")
         .set("kf", "10^lr_N2_19(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_19").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_19").set("de", "8.89");
    model.component("outer").physics("plas2").create("N2_20", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_20").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_20")
         .set("kf", "10^lr_N2_20(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_20").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_20").set("de", "11.03");
    model.component("outer").physics("plas2").create("N2_21", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_21").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_21")
         .set("kf", "10^lr_N2_21(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_21").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_21").set("de", "11.87");
    model.component("outer").physics("plas2").create("N2_22", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_22").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_22")
         .set("kf", "10^lr_N2_22(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_22").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_22").set("de", "12.25");
    model.component("outer").physics("plas2").create("N2_23", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_23").set("formula", "e+N2X=>e+N2X");
    model.component("outer").physics("plas2").feature("N2_23")
         .set("kf", "10^lr_N2_23(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_23").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N2_23").set("de", "13.0");
    model.component("outer").physics("plas2").create("N2_24", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_24").set("formula", "e+N2X=>2e+N2X+");
    model.component("outer").physics("plas2").feature("N2_24")
         .set("kf", "10^lr_N2_24(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_24").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("N2_24").set("de", "15.6");
    model.component("outer").physics("plas2").create("N2_25", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N2_25").set("formula", "e+N2X=>2e+N2X+");
    model.component("outer").physics("plas2").feature("N2_25")
         .set("kf", "10^lr_N2_25(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("N2_25").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("N2_25").set("de", "18.8");
    model.component("outer").physics("plas2").create("O2_0", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_0").set("formula", "e+O2X=>OX+OX-");
    model.component("outer").physics("plas2").feature("O2_0").set("kf", "10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_0").set("type", "Attachment");
    model.component("outer").physics("plas2").feature("O2_0").set("de", "0");
    model.component("outer").physics("plas2").create("O2_1", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_1").set("formula", "e+O2X=>OX+OX-");
    model.component("outer").physics("plas2").feature("O2_1").set("kf", "10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_1").set("type", "Attachment");
    model.component("outer").physics("plas2").feature("O2_1").set("de", "0");
    model.component("outer").physics("plas2").create("O2_2", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_2").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_2").set("kf", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_2").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("O2_2").set("mratio", "1.71e-05");
    model.component("outer").physics("plas2").create("O2_3", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_3").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_3").set("kf", "10^lr_O2_3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_3").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_3").set("de", "0.02");
    model.component("outer").physics("plas2").create("O2_4", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_4").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_4").set("kf", "10^lr_O2_4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_4").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_4").set("de", "0.19");
    model.component("outer").physics("plas2").create("O2_5", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_5").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_5").set("kf", "10^lr_O2_5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_5").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_5").set("de", "0.19");
    model.component("outer").physics("plas2").create("O2_6", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_6").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_6").set("kf", "10^lr_O2_6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_6").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_6").set("de", "0.38");
    model.component("outer").physics("plas2").create("O2_7", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_7").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_7").set("kf", "10^lr_O2_7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_7").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_7").set("de", "0.38");
    model.component("outer").physics("plas2").create("O2_8", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_8").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_8").set("kf", "10^lr_O2_8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_8").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_8").set("de", "0.57");
    model.component("outer").physics("plas2").create("O2_9", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_9").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_9").set("kf", "10^lr_O2_9(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_9").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_9").set("de", "0.75");
    model.component("outer").physics("plas2").create("O2_10", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_10").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_10")
         .set("kf", "10^lr_O2_10(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_10").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_10").set("de", "0.977");
    model.component("outer").physics("plas2").create("O2_11", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_11").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_11")
         .set("kf", "10^lr_O2_11(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_11").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_11").set("de", "1.627");
    model.component("outer").physics("plas2").create("O2_12", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_12").set("formula", "e+O2X=>e+O2X");
    model.component("outer").physics("plas2").feature("O2_12")
         .set("kf", "10^lr_O2_12(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_12").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_12").set("de", "4.5");
    model.component("outer").physics("plas2").create("O2_13", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_13").set("formula", "e+O2X=>e+2OX");
    model.component("outer").physics("plas2").feature("O2_13")
         .set("kf", "10^lr_O2_13(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_13").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_13").set("de", "6.0");
    model.component("outer").physics("plas2").create("O2_14", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_14").set("formula", "e+O2X=>e+2OX");
    model.component("outer").physics("plas2").feature("O2_14")
         .set("kf", "10^lr_O2_14(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_14").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_14").set("de", "8.4");
    model.component("outer").physics("plas2").create("O2_15", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_15").set("formula", "e+O2X=>e+2OX");
    model.component("outer").physics("plas2").feature("O2_15")
         .set("kf", "10^lr_O2_15(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_15").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O2_15").set("de", "9.97");
    model.component("outer").physics("plas2").create("O2_16", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O2_16").set("formula", "e+O2X=>2e+O2X+");
    model.component("outer").physics("plas2").feature("O2_16")
         .set("kf", "10^lr_O2_16(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("O2_16").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("O2_16").set("de", "12.06");
    model.component("outer").physics("plas2").create("CuD5up", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuD5up").set("formula", "e+CuX=>e+CuD5X");
    model.component("outer").physics("plas2").feature("CuD5up")
         .set("kf", "10^lr_CuD5_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuD5up").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuD5up").set("de", "1.388948");
    model.component("outer").physics("plas2").create("CuD5down", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuD5down").set("formula", "e+CuD5X=>e+CuX");
    model.component("outer").physics("plas2").feature("CuD5down")
         .set("kf", "10^lr_CuD5_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuD5down").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuD5down").set("de", "-1.388948");
    model.component("outer").physics("plas2").create("CuD3up", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuD3up").set("formula", "e+CuX=>e+CuD3X");
    model.component("outer").physics("plas2").feature("CuD3up")
         .set("kf", "10^lr_CuD3_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuD3up").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuD3up").set("de", "1.6422256");
    model.component("outer").physics("plas2").create("CuD3down", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuD3down").set("formula", "e+CuD3X=>e+CuX");
    model.component("outer").physics("plas2").feature("CuD3down")
         .set("kf", "10^lr_CuD3_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuD3down").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuD3down").set("de", "-1.6422256");
    model.component("outer").physics("plas2").create("CuP1up", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuP1up").set("formula", "e+CuX=>e+CuP1X");
    model.component("outer").physics("plas2").feature("CuP1up")
         .set("kf", "10^lr_CuP1_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuP1up").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuP1up").set("de", "3.7858977");
    model.component("outer").physics("plas2").create("CuP1down", "ElectronImpactReaction", 2);
}
static void part2(Model model){

    model.component("outer").physics("plas2").feature("CuP1down").set("formula", "e+CuP1X=>e+CuX");
    model.component("outer").physics("plas2").feature("CuP1down")
         .set("kf", "10^lr_CuP1_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuP1down").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuP1down").set("de", "-3.7858977");
    model.component("outer").physics("plas2").create("CuP3up", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuP3up").set("formula", "e+CuX=>e+CuP3X");
    model.component("outer").physics("plas2").feature("CuP3up")
         .set("kf", "10^lr_CuP3_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuP3up").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuP3up").set("de", "3.816692");
    model.component("outer").physics("plas2").create("CuP3down", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuP3down").set("formula", "e+CuP3X=>e+CuX");
    model.component("outer").physics("plas2").feature("CuP3down")
         .set("kf", "10^lr_CuP3_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuP3down").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuP3down").set("de", "-3.816692");
    model.component("outer").physics("plas2").create("rad1", "Reaction", 2);
    model.component("outer").physics("plas2").feature("rad1").set("formula", "CuP3X=>CuX");
    model.component("outer").physics("plas2").feature("rad1").set("kf", "139500000.0[1/s]*beta1");
    model.component("outer").physics("plas2").create("rad2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("rad2").set("formula", "CuP1X=>CuX");
    model.component("outer").physics("plas2").feature("rad2").set("kf", "137600000.0[1/s]*beta2");
    model.component("outer").physics("plas2").create("rad3", "Reaction", 2);
    model.component("outer").physics("plas2").feature("rad3").set("formula", "CuP3X=>CuD5X");
    model.component("outer").physics("plas2").feature("rad3").set("kf", "2000000.0[1/s]*beta3");
    model.component("outer").physics("plas2").create("lineup3", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("lineup3").set("formula", "e+CuD5X=>e+CuP3X");
    model.component("outer").physics("plas2").feature("lineup3")
         .set("kf", "10^lr_lineup3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("lineup3").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("lineup3").set("de", "2.427744");
    model.component("outer").physics("plas2").create("linedn3", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("linedn3").set("formula", "e+CuP3X=>e+CuD5X");
    model.component("outer").physics("plas2").feature("linedn3")
         .set("kf", "10^lr_linedn3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("linedn3").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("linedn3").set("de", "-2.427744");
    model.component("outer").physics("plas2").create("rad4", "Reaction", 2);
    model.component("outer").physics("plas2").feature("rad4").set("formula", "CuH3X=>CuP1X");
    model.component("outer").physics("plas2").feature("rad4").set("kf", "60000000.0[1/s]*beta4");
    model.component("outer").physics("plas2").create("lineup4", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("lineup4").set("formula", "e+CuP1X=>e+CuH3X");
    model.component("outer").physics("plas2").feature("lineup4")
         .set("kf", "10^lr_lineup4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("lineup4").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("lineup4").set("de", "2.4052773999999997");
    model.component("outer").physics("plas2").create("linedn4", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("linedn4").set("formula", "e+CuH3X=>e+CuP1X");
    model.component("outer").physics("plas2").feature("linedn4")
         .set("kf", "10^lr_linedn4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("linedn4").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("linedn4").set("de", "-2.4052773999999997");
    model.component("outer").physics("plas2").create("rad5", "Reaction", 2);
    model.component("outer").physics("plas2").feature("rad5").set("formula", "CuH5X=>CuP3X");
    model.component("outer").physics("plas2").feature("rad5").set("kf", "75000000.0[1/s]*beta5");
    model.component("outer").physics("plas2").create("lineup5", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("lineup5").set("formula", "e+CuP3X=>e+CuH5X");
    model.component("outer").physics("plas2").feature("lineup5")
         .set("kf", "10^lr_lineup5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("lineup5").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("lineup5").set("de", "2.3753331999999996");
    model.component("outer").physics("plas2").create("linedn5", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("linedn5").set("formula", "e+CuH5X=>e+CuP3X");
    model.component("outer").physics("plas2").feature("linedn5")
         .set("kf", "10^lr_linedn5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("linedn5").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("linedn5").set("de", "-2.3753331999999996");
    model.component("outer").physics("plas2").create("rad6", "Reaction", 2);
    model.component("outer").physics("plas2").feature("rad6").set("formula", "CuH3X=>CuP3X");
    model.component("outer").physics("plas2").feature("rad6").set("kf", "15000000.0[1/s]*beta6");
    model.component("outer").physics("plas2").create("lineup6", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("lineup6").set("formula", "e+CuP3X=>e+CuH3X");
    model.component("outer").physics("plas2").feature("lineup6")
         .set("kf", "10^lr_lineup6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("lineup6").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("lineup6").set("de", "2.3744830999999995");
    model.component("outer").physics("plas2").create("linedn6", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("linedn6").set("formula", "e+CuH3X=>e+CuP3X");
    model.component("outer").physics("plas2").feature("linedn6")
         .set("kf", "10^lr_linedn6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("linedn6").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("linedn6").set("de", "-2.3744830999999995");
    model.component("outer").physics("plas2").create("rad7", "Reaction", 2);
    model.component("outer").physics("plas2").feature("rad7").set("formula", "CuP3X=>CuD3X");
    model.component("outer").physics("plas2").feature("rad7").set("kf", "240000.0[1/s]*beta7");
    model.component("outer").physics("plas2").create("lineup7", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("lineup7").set("formula", "e+CuD3X=>e+CuP3X");
    model.component("outer").physics("plas2").feature("lineup7")
         .set("kf", "10^lr_lineup7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("lineup7").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("lineup7").set("de", "2.1744664");
    model.component("outer").physics("plas2").create("linedn7", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("linedn7").set("formula", "e+CuP3X=>e+CuD3X");
    model.component("outer").physics("plas2").feature("linedn7")
         .set("kf", "10^lr_linedn7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("linedn7").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("linedn7").set("de", "-2.1744664");
    model.component("outer").physics("plas2").create("rad8", "Reaction", 2);
    model.component("outer").physics("plas2").feature("rad8").set("formula", "CuP1X=>CuD3X");
    model.component("outer").physics("plas2").feature("rad8").set("kf", "1650000.0[1/s]*beta8");
    model.component("outer").physics("plas2").create("lineup8", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("lineup8").set("formula", "e+CuD3X=>e+CuP1X");
    model.component("outer").physics("plas2").feature("lineup8")
         .set("kf", "10^lr_lineup8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("lineup8").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("lineup8").set("de", "2.1436721");
    model.component("outer").physics("plas2").create("linedn8", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("linedn8").set("formula", "e+CuP1X=>e+CuD3X");
    model.component("outer").physics("plas2").feature("linedn8")
         .set("kf", "10^lr_linedn8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("linedn8").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("linedn8").set("de", "-2.1436721");
    model.component("outer").physics("plas2").create("Cuion", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("Cuion").set("formula", "e+CuX=>2e+CuX+");
    model.component("outer").physics("plas2").feature("Cuion")
         .set("kf", "10^lr_Cu_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("Cuion").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("Cuion").set("de", "7.72638");
    model.component("outer").physics("plas2").create("Cu3rec", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("Cu3rec").set("formula", "2e+CuX+=>e+CuX");
    model.component("outer").physics("plas2").feature("Cu3rec")
         .set("kf", "10^lr_Cu_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("outer").physics("plas2").feature("Cu3rec").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("Cu3rec").set("de", "-7.72638");
    model.component("outer").physics("plas2").create("Curr", "Reaction", 2);
    model.component("outer").physics("plas2").feature("Curr").set("formula", "e+CuX+=>CuX");
    model.component("outer").physics("plas2").feature("Curr")
         .set("kf", "10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("Nion", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("Nion").set("formula", "e+NX=>2e+NX+");
    model.component("outer").physics("plas2").feature("Nion")
         .set("kf", "10^lr_N_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("Nion").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("Nion").set("de", "14.534");
    model.component("outer").physics("plas2").create("N3rec", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("N3rec").set("formula", "2e+NX+=>e+NX");
    model.component("outer").physics("plas2").feature("N3rec")
         .set("kf", "10^lr_N_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("outer").physics("plas2").feature("N3rec").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("N3rec").set("de", "-14.534");
    model.component("outer").physics("plas2").create("Nrr", "Reaction", 2);
    model.component("outer").physics("plas2").feature("Nrr").set("formula", "e+NX+=>NX");
    model.component("outer").physics("plas2").feature("Nrr").set("kf", "10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("Oion", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("Oion").set("formula", "e+OX=>2e+OX+");
    model.component("outer").physics("plas2").feature("Oion")
         .set("kf", "10^lr_O_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("Oion").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("Oion").set("de", "13.618");
    model.component("outer").physics("plas2").create("O3rec", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("O3rec").set("formula", "2e+OX+=>e+OX");
    model.component("outer").physics("plas2").feature("O3rec")
         .set("kf", "10^lr_O_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("outer").physics("plas2").feature("O3rec").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("O3rec").set("de", "-13.618");
    model.component("outer").physics("plas2").create("Orr", "Reaction", 2);
    model.component("outer").physics("plas2").feature("Orr").set("formula", "e+OX+=>OX");
    model.component("outer").physics("plas2").feature("Orr").set("kf", "10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("CuD5ion", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuD5ion").set("formula", "e+CuD5X=>2e+CuX+");
    model.component("outer").physics("plas2").feature("CuD5ion")
         .set("kf", "10^lr_CuD5_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuD5ion").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("CuD5ion").set("de", "6.337432");
    model.component("outer").physics("plas2").create("CuD53rec", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuD53rec").set("formula", "2e+CuX+=>e+CuD5X");
    model.component("outer").physics("plas2").feature("CuD53rec")
         .set("kf", "10^lr_CuD5_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("outer").physics("plas2").feature("CuD53rec").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuD53rec").set("de", "-6.337432");
    model.component("outer").physics("plas2").create("CuD5rr", "Reaction", 2);
    model.component("outer").physics("plas2").feature("CuD5rr").set("formula", "e+CuX+=>CuD5X");
    model.component("outer").physics("plas2").feature("CuD5rr")
         .set("kf", "10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
}
static void part3(Model model){

    model.component("outer").physics("plas2").create("CuD3ion", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuD3ion").set("formula", "e+CuD3X=>2e+CuX+");
    model.component("outer").physics("plas2").feature("CuD3ion")
         .set("kf", "10^lr_CuD3_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuD3ion").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("CuD3ion").set("de", "6.0841544");
    model.component("outer").physics("plas2").create("CuD33rec", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuD33rec").set("formula", "2e+CuX+=>e+CuD3X");
    model.component("outer").physics("plas2").feature("CuD33rec")
         .set("kf", "10^lr_CuD3_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("outer").physics("plas2").feature("CuD33rec").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuD33rec").set("de", "-6.0841544");
    model.component("outer").physics("plas2").create("CuD3rr", "Reaction", 2);
    model.component("outer").physics("plas2").feature("CuD3rr").set("formula", "e+CuX+=>CuD3X");
    model.component("outer").physics("plas2").feature("CuD3rr")
         .set("kf", "10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("CuP1ion", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuP1ion").set("formula", "e+CuP1X=>2e+CuX+");
    model.component("outer").physics("plas2").feature("CuP1ion")
         .set("kf", "10^lr_CuP1_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuP1ion").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("CuP1ion").set("de", "3.9404822999999998");
    model.component("outer").physics("plas2").create("CuP13rec", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuP13rec").set("formula", "2e+CuX+=>e+CuP1X");
    model.component("outer").physics("plas2").feature("CuP13rec")
         .set("kf", "10^lr_CuP1_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("outer").physics("plas2").feature("CuP13rec").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuP13rec").set("de", "-3.9404822999999998");
    model.component("outer").physics("plas2").create("CuP1rr", "Reaction", 2);
    model.component("outer").physics("plas2").feature("CuP1rr").set("formula", "e+CuX+=>CuP1X");
    model.component("outer").physics("plas2").feature("CuP1rr")
         .set("kf", "10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("CuP3ion", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuP3ion").set("formula", "e+CuP3X=>2e+CuX+");
    model.component("outer").physics("plas2").feature("CuP3ion")
         .set("kf", "10^lr_CuP3_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuP3ion").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("CuP3ion").set("de", "3.9096879999999996");
    model.component("outer").physics("plas2").create("CuP33rec", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuP33rec").set("formula", "2e+CuX+=>e+CuP3X");
    model.component("outer").physics("plas2").feature("CuP33rec")
         .set("kf", "10^lr_CuP3_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("outer").physics("plas2").feature("CuP33rec").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuP33rec").set("de", "-3.9096879999999996");
    model.component("outer").physics("plas2").create("CuP3rr", "Reaction", 2);
    model.component("outer").physics("plas2").feature("CuP3rr").set("formula", "e+CuX+=>CuP3X");
    model.component("outer").physics("plas2").feature("CuP3rr")
         .set("kf", "10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("CuH3ion", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuH3ion").set("formula", "e+CuH3X=>2e+CuX+");
    model.component("outer").physics("plas2").feature("CuH3ion")
         .set("kf", "10^lr_CuH3_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuH3ion").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("CuH3ion").set("de", "1.5352049");
    model.component("outer").physics("plas2").create("CuH33rec", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuH33rec").set("formula", "2e+CuX+=>e+CuH3X");
    model.component("outer").physics("plas2").feature("CuH33rec")
         .set("kf", "10^lr_CuH3_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("outer").physics("plas2").feature("CuH33rec").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuH33rec").set("de", "-1.5352049");
    model.component("outer").physics("plas2").create("CuH3rr", "Reaction", 2);
    model.component("outer").physics("plas2").feature("CuH3rr").set("formula", "e+CuX+=>CuH3X");
    model.component("outer").physics("plas2").feature("CuH3rr")
         .set("kf", "10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("CuH5ion", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuH5ion").set("formula", "e+CuH5X=>2e+CuX+");
    model.component("outer").physics("plas2").feature("CuH5ion")
         .set("kf", "10^lr_CuH5_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").feature("CuH5ion").set("type", "Ionization");
    model.component("outer").physics("plas2").feature("CuH5ion").set("de", "1.5343548");
    model.component("outer").physics("plas2").create("CuH53rec", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("CuH53rec").set("formula", "2e+CuX+=>e+CuH5X");
    model.component("outer").physics("plas2").feature("CuH53rec")
         .set("kf", "10^lr_CuH5_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component("outer").physics("plas2").feature("CuH53rec").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("CuH53rec").set("de", "-1.5343548");
    model.component("outer").physics("plas2").create("CuH5rr", "Reaction", 2);
    model.component("outer").physics("plas2").feature("CuH5rr").set("formula", "e+CuX+=>CuH5X");
    model.component("outer").physics("plas2").feature("CuH5rr")
         .set("kf", "10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("recN2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("recN2").set("formula", "e+N2X+=>2NX");
    model.component("outer").physics("plas2").feature("recN2").set("kf", "2.8e-13[m^3/s]*(300[K]/TeK)^0.5*N_A_const");
    model.component("outer").physics("plas2").create("recO2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("recO2").set("formula", "e+O2X+=>2OX");
    model.component("outer").physics("plas2").feature("recO2").set("kf", "2e-13[m^3/s]*(300[K]/TeK)^0.7*N_A_const");
    model.component("outer").physics("plas2").create("att3", "Reaction", 2);
    model.component("outer").physics("plas2").feature("att3").set("formula", "e+O2X+O2X=>O2X-+O2X");
    model.component("outer").physics("plas2").feature("att3")
         .set("kf", "1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg))*N_A_const^2");
    model.component("outer").physics("plas2").create("detach", "Reaction", 2);
    model.component("outer").physics("plas2").feature("detach").set("formula", "OX-+OX=>O2X+e");
    model.component("outer").physics("plas2").feature("detach").set("kf", "3e-16[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("detach2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("detach2").set("formula", "O2X-+OX=>O2X+OX+e");
    model.component("outer").physics("plas2").feature("detach2").set("kf", "3.3e-16[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("neutral103", "Reaction", 2);
    model.component("outer").physics("plas2").feature("neutral103").set("formula", "CuX++OX-=>CuX+OX");
    model.component("outer").physics("plas2").feature("neutral103").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("neutral104", "Reaction", 2);
    model.component("outer").physics("plas2").feature("neutral104").set("formula", "CuX++O2X-=>CuX+O2X");
    model.component("outer").physics("plas2").feature("neutral104").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("neutral105", "Reaction", 2);
    model.component("outer").physics("plas2").feature("neutral105").set("formula", "N2X++OX-=>N2X+OX");
    model.component("outer").physics("plas2").feature("neutral105").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("neutral106", "Reaction", 2);
    model.component("outer").physics("plas2").feature("neutral106").set("formula", "N2X++O2X-=>N2X+O2X");
    model.component("outer").physics("plas2").feature("neutral106").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("neutral107", "Reaction", 2);
    model.component("outer").physics("plas2").feature("neutral107").set("formula", "O2X++OX-=>O2X+OX");
    model.component("outer").physics("plas2").feature("neutral107").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("neutral108", "Reaction", 2);
    model.component("outer").physics("plas2").feature("neutral108").set("formula", "O2X++O2X-=>O2X+O2X");
    model.component("outer").physics("plas2").feature("neutral108").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("neutral109", "Reaction", 2);
    model.component("outer").physics("plas2").feature("neutral109").set("formula", "NX++OX-=>NX+OX");
    model.component("outer").physics("plas2").feature("neutral109").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("neutral110", "Reaction", 2);
    model.component("outer").physics("plas2").feature("neutral110").set("formula", "NX++O2X-=>NX+O2X");
    model.component("outer").physics("plas2").feature("neutral110").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("neutral111", "Reaction", 2);
    model.component("outer").physics("plas2").feature("neutral111").set("formula", "OX++OX-=>OX+OX");
    model.component("outer").physics("plas2").feature("neutral111").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("neutral112", "Reaction", 2);
    model.component("outer").physics("plas2").feature("neutral112").set("formula", "OX++O2X-=>OX+O2X");
    model.component("outer").physics("plas2").feature("neutral112").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component("outer").physics("plas2").create("N2dN2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("N2dN2").set("formula", "N2X+N2X=>2NX+N2X");
    model.component("outer").physics("plas2").feature("N2dN2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp(-113200[K]/Tg)/N_A_const*N_A_const");
    model.component("outer").physics("plas2").create("N2rN2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("N2rN2").set("formula", "2NX+N2X=>N2X+N2X");
    model.component("outer").physics("plas2").feature("N2rN2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp((-113200[K]+9.76[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.162933627609196e-26[kg]*k_B_const*Tg/h_const^2)^1.5*16/1/(Tg/(2*2.88[K]))*(1-exp(-3390[K]/Tg)))*N_A_const^2");
    model.component("outer").physics("plas2").create("N2dO2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("N2dO2").set("formula", "N2X+O2X=>2NX+O2X");
    model.component("outer").physics("plas2").feature("N2dO2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp(-113200[K]/Tg)/N_A_const*N_A_const");
    model.component("outer").physics("plas2").create("N2rO2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("N2rO2").set("formula", "2NX+O2X=>N2X+O2X");
    model.component("outer").physics("plas2").feature("N2rO2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp((-113200[K]+9.76[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.162933627609196e-26[kg]*k_B_const*Tg/h_const^2)^1.5*16/1/(Tg/(2*2.88[K]))*(1-exp(-3390[K]/Tg)))*N_A_const^2");
    model.component("outer").physics("plas2").create("N2dCu", "Reaction", 2);
    model.component("outer").physics("plas2").feature("N2dCu").set("formula", "N2X+CuX=>2NX+CuX");
    model.component("outer").physics("plas2").feature("N2dCu")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp(-113200[K]/Tg)/N_A_const*N_A_const");
    model.component("outer").physics("plas2").create("N2rCu", "Reaction", 2);
    model.component("outer").physics("plas2").feature("N2rCu").set("formula", "2NX+CuX=>N2X+CuX");
    model.component("outer").physics("plas2").feature("N2rCu")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp((-113200[K]+9.76[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.162933627609196e-26[kg]*k_B_const*Tg/h_const^2)^1.5*16/1/(Tg/(2*2.88[K]))*(1-exp(-3390[K]/Tg)))*N_A_const^2");
    model.component("outer").physics("plas2").create("O2dN2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("O2dN2").set("formula", "O2X+N2X=>2OX+N2X");
    model.component("outer").physics("plas2").feature("O2dN2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp(-59500[K]/Tg)/N_A_const*N_A_const");
    model.component("outer").physics("plas2").create("O2rN2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("O2rN2").set("formula", "2OX+N2X=>O2X+N2X");
    model.component("outer").physics("plas2").feature("O2rN2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp((-59500[K]+5.16[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.3283814375670623e-26[kg]*k_B_const*Tg/h_const^2)^1.5*81/3/(Tg/(2*2.07[K]))*(1-exp(-2270[K]/Tg)))*N_A_const^2");
    model.component("outer").physics("plas2").create("O2dO2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("O2dO2").set("formula", "O2X+O2X=>2OX+O2X");
    model.component("outer").physics("plas2").feature("O2dO2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp(-59500[K]/Tg)/N_A_const*N_A_const");
    model.component("outer").physics("plas2").create("O2rO2", "Reaction", 2);
    model.component("outer").physics("plas2").feature("O2rO2").set("formula", "2OX+O2X=>O2X+O2X");
    model.component("outer").physics("plas2").feature("O2rO2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp((-59500[K]+5.16[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.3283814375670623e-26[kg]*k_B_const*Tg/h_const^2)^1.5*81/3/(Tg/(2*2.07[K]))*(1-exp(-2270[K]/Tg)))*N_A_const^2");
    model.component("outer").physics("plas2").create("O2dCu", "Reaction", 2);
    model.component("outer").physics("plas2").feature("O2dCu").set("formula", "O2X+CuX=>2OX+CuX");
    model.component("outer").physics("plas2").feature("O2dCu")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp(-59500[K]/Tg)/N_A_const*N_A_const");
    model.component("outer").physics("plas2").create("O2rCu", "Reaction", 2);
    model.component("outer").physics("plas2").feature("O2rCu").set("formula", "2OX+CuX=>O2X+CuX");
    model.component("outer").physics("plas2").feature("O2rCu")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp((-59500[K]+5.16[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.3283814375670623e-26[kg]*k_B_const*Tg/h_const^2)^1.5*81/3/(Tg/(2*2.07[K]))*(1-exp(-2270[K]/Tg)))*N_A_const^2");
    model.component("outer").physics("plas2").feature("N2X").set("PresetSpeciesData", "N2");
    model.component("outer").physics("plas2").feature("N2X").set("M", "0.0280134[kg/mol]");
    model.component("outer").physics("plas2").feature("N2X").set("x0", ".79");
    model.component("outer").physics("plas2").feature("N2X").set("Dfs", "Dgas");
}
static void part4(Model model){

    model.component("outer").physics("plas2").feature("O2X").set("PresetSpeciesData", "O2");
    model.component("outer").physics("plas2").feature("O2X").set("M", "0.0319988[kg/mol]");
    model.component("outer").physics("plas2").feature("O2X").set("x0", ".21");
    model.component("outer").physics("plas2").feature("O2X").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("NX").set("PresetSpeciesData", "N");
    model.component("outer").physics("plas2").feature("NX").set("M", "0.0140067[kg/mol]");
    model.component("outer").physics("plas2").feature("NX").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("NX").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("OX").set("PresetSpeciesData", "O");
    model.component("outer").physics("plas2").feature("OX").set("M", "0.0159994[kg/mol]");
    model.component("outer").physics("plas2").feature("OX").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("OX").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("N2X_1p").set("PresetSpeciesData", "N2");
    model.component("outer").physics("plas2").feature("N2X_1p").set("M", "0.0280134[kg/mol]");
    model.component("outer").physics("plas2").feature("N2X_1p").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("N2X_1p").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("N2X_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("outer").physics("plas2").feature("N2X_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("outer").physics("plas2").feature("N2X_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("outer").physics("plas2").feature("O2X_1p").set("PresetSpeciesData", "O2");
    model.component("outer").physics("plas2").feature("O2X_1p").set("M", "0.0319988[kg/mol]");
    model.component("outer").physics("plas2").feature("O2X_1p").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("O2X_1p").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("O2X_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("outer").physics("plas2").feature("O2X_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("outer").physics("plas2").feature("O2X_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("outer").physics("plas2").feature("NX_1p").set("PresetSpeciesData", "N");
    model.component("outer").physics("plas2").feature("NX_1p").set("M", "0.0140067[kg/mol]");
    model.component("outer").physics("plas2").feature("NX_1p").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("NX_1p").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("NX_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("outer").physics("plas2").feature("NX_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("outer").physics("plas2").feature("NX_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("outer").physics("plas2").feature("OX_1p").set("PresetSpeciesData", "O");
    model.component("outer").physics("plas2").feature("OX_1p").set("M", "0.0159994[kg/mol]");
    model.component("outer").physics("plas2").feature("OX_1p").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("OX_1p").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("OX_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("outer").physics("plas2").feature("OX_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("outer").physics("plas2").feature("OX_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("outer").physics("plas2").feature("OX_1m").set("PresetSpeciesData", "O");
    model.component("outer").physics("plas2").feature("OX_1m").set("M", "0.0159994[kg/mol]");
    model.component("outer").physics("plas2").feature("OX_1m").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("OX_1m").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("OX_1m")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("outer").physics("plas2").feature("OX_1m").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("outer").physics("plas2").feature("OX_1m").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("outer").physics("plas2").feature("O2X_1m").set("PresetSpeciesData", "O2");
    model.component("outer").physics("plas2").feature("O2X_1m").set("M", "0.0319988[kg/mol]");
    model.component("outer").physics("plas2").feature("O2X_1m").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("O2X_1m").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("O2X_1m")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("outer").physics("plas2").feature("O2X_1m").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("outer").physics("plas2").feature("O2X_1m").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("outer").physics("plas2").feature("CuX").set("PresetSpeciesData", "UserDefined");
    model.component("outer").physics("plas2").feature("CuX").set("M", "0.063546[kg/mol]");
    model.component("outer").physics("plas2").feature("CuX").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("CuX").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("CuX").set("sigma", "3[angstrom]");
    model.component("outer").physics("plas2").feature("CuX").set("epsilonkb", "100[K]");
    model.component("outer").physics("plas2").feature("CuD5X").set("PresetSpeciesData", "UserDefined");
    model.component("outer").physics("plas2").feature("CuD5X").set("M", "0.063546[kg/mol]");
    model.component("outer").physics("plas2").feature("CuD5X").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("CuD5X").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("CuD5X").set("sigma", "3[angstrom]");
    model.component("outer").physics("plas2").feature("CuD5X").set("epsilonkb", "100[K]");
    model.component("outer").physics("plas2").feature("CuD3X").set("PresetSpeciesData", "UserDefined");
    model.component("outer").physics("plas2").feature("CuD3X").set("M", "0.063546[kg/mol]");
    model.component("outer").physics("plas2").feature("CuD3X").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("CuD3X").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("CuD3X").set("sigma", "3[angstrom]");
    model.component("outer").physics("plas2").feature("CuD3X").set("epsilonkb", "100[K]");
    model.component("outer").physics("plas2").feature("CuP1X").set("PresetSpeciesData", "UserDefined");
    model.component("outer").physics("plas2").feature("CuP1X").set("M", "0.063546[kg/mol]");
    model.component("outer").physics("plas2").feature("CuP1X").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("CuP1X").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("CuP1X").set("sigma", "3[angstrom]");
    model.component("outer").physics("plas2").feature("CuP1X").set("epsilonkb", "100[K]");
    model.component("outer").physics("plas2").feature("CuP3X").set("PresetSpeciesData", "UserDefined");
    model.component("outer").physics("plas2").feature("CuP3X").set("M", "0.063546[kg/mol]");
    model.component("outer").physics("plas2").feature("CuP3X").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("CuP3X").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("CuP3X").set("sigma", "3[angstrom]");
    model.component("outer").physics("plas2").feature("CuP3X").set("epsilonkb", "100[K]");
    model.component("outer").physics("plas2").feature("CuH3X").set("PresetSpeciesData", "UserDefined");
    model.component("outer").physics("plas2").feature("CuH3X").set("M", "0.063546[kg/mol]");
    model.component("outer").physics("plas2").feature("CuH3X").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("CuH3X").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("CuH3X").set("sigma", "3[angstrom]");
    model.component("outer").physics("plas2").feature("CuH3X").set("epsilonkb", "100[K]");
    model.component("outer").physics("plas2").feature("CuH5X").set("PresetSpeciesData", "UserDefined");
    model.component("outer").physics("plas2").feature("CuH5X").set("M", "0.063546[kg/mol]");
    model.component("outer").physics("plas2").feature("CuH5X").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("CuH5X").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("CuH5X").set("sigma", "3[angstrom]");
    model.component("outer").physics("plas2").feature("CuH5X").set("epsilonkb", "100[K]");
    model.component("outer").physics("plas2").feature("CuX_1p").set("PresetSpeciesData", "UserDefined");
    model.component("outer").physics("plas2").feature("CuX_1p").set("M", "0.063546[kg/mol]");
    model.component("outer").physics("plas2").feature("CuX_1p").set("x0", "1e-24");
    model.component("outer").physics("plas2").feature("CuX_1p").set("Dfs", "Dgas");
    model.component("outer").physics("plas2").feature("CuX_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component("outer").physics("plas2").feature("CuX_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component("outer").physics("plas2").feature("CuX_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component("outer").physics("plas2").feature("CuX_1p").set("sigma", "3[angstrom]");
    model.component("outer").physics("plas2").feature("CuX_1p").set("epsilonkb", "100[K]");
    model.component("outer").physics("plas2").feature("N2X").set("FromMassConstraint", true);
    model.component("outer").physics("plas2").feature("N2X_1p").set("InitIon", true);
    model.component("outer").variable("outerGas").set("rhoP", "rho0*qR");
    model.component("outer").variable("outerGas").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component("outer").variable("outerGas").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component("outer").variable("outerGas").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component("outer").variable("outerGas").set("Tgas", "max(Tg,150[K])");
    model.component("outer").variable("outerGas").set("TeV", "max(plas2.Te,0.015[V])");
    model.component("outer").variable("outerGas").set("TeK", "plas2.Te*e_const/k_B_const");
    model.component("outer").variable("outerGas").set("Ue", "e_const*plas2.en");
    model.component("outer").variable("outerGas").set("pe", "(2/3)*Ue");
    model.component("outer").variable("outerGas").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component("outer").variable("outerGas").set("nN2", "plas2.n_wN2X");
    model.component("outer").variable("outerGas").set("nO2", "plas2.n_wO2X");
    model.component("outer").variable("outerGas").set("nN", "plas2.n_wNX");
    model.component("outer").variable("outerGas").set("nO", "plas2.n_wOX");
    model.component("outer").variable("outerGas").set("nN2_1p", "plas2.n_wN2X_1p");
    model.component("outer").variable("outerGas").set("nO2_1p", "plas2.n_wO2X_1p");
    model.component("outer").variable("outerGas").set("nN_1p", "plas2.n_wNX_1p");
    model.component("outer").variable("outerGas").set("nO_1p", "plas2.n_wOX_1p");
    model.component("outer").variable("outerGas").set("nO_1m", "plas2.n_wOX_1m");
    model.component("outer").variable("outerGas").set("nO2_1m", "plas2.n_wO2X_1m");
    model.component("outer").variable("outerGas").set("nCu", "plas2.n_wCuX");
    model.component("outer").variable("outerGas").set("nCuD5", "plas2.n_wCuD5X");
    model.component("outer").variable("outerGas").set("nCuD3", "plas2.n_wCuD3X");
    model.component("outer").variable("outerGas").set("nCuP1", "plas2.n_wCuP1X");
    model.component("outer").variable("outerGas").set("nCuP3", "plas2.n_wCuP3X");
    model.component("outer").variable("outerGas").set("nCuH3", "plas2.n_wCuH3X");
    model.component("outer").variable("outerGas").set("nCuH5", "plas2.n_wCuH5X");
    model.component("outer").variable("outerGas").set("nCu_1p", "plas2.n_wCuX_1p");
    model.component("outer").variable("outerGas")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("outer").variable("outerGas").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component("outer").variable("outerGas").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component("outer").variable("outerGas").set("Ycu", "rhoCopper/rhoSafe");
    model.component("outer").variable("outerGas")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component("outer").variable("outerGas")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component("outer").variable("outerGas").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component("outer").variable("outerGas").set("pPl", "pHeavy+pe");
    model.component("outer").variable("outerGas").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component("outer").variable("outerGas").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component("outer").variable("outerGas").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component("outer").variable("outerGas").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component("outer").variable("outerGas").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component("outer").variable("outerGas")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component("outer").variable("outerGas").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
}
static void part5(Model model){

    model.component("outer").variable("outerGas").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component("outer").variable("outerGas").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component("outer").variable("outerGas").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component("outer").variable("outerGas").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component("outer").variable("outerGas").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component("outer").variable("outerGas").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component("outer").variable("outerGas").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component("outer").variable("outerGas").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component("outer").variable("outerGas")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max(plas2.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component("outer").variable("outerGas").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component("outer").variable("outerGas").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component("outer").variable("outerGas")
         .set("alphaLaser", "plas2.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("outer").variable("outerGas").set("Qei", "3*me_const/(Mcu/N_A_const)*plas2.ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component("outer").variable("outerGas")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component("outer").variable("outerGas")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component("outer").variable("outerGas")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component("outer").variable("outerGas")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component("outer").variable("outerGas")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component("outer").variable("outerGas")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component("outer").variable("outerGas")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component("outer").variable("outerGas")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component("outer").variable("outerGas")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component("outer").variable("outerGas")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component("outer").variable("outerGas")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("outer").variable("outerGas")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("outer").variable("outerGas")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("outer").variable("outerGas")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("outer").variable("outerGas")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("outer").variable("outerGas")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("outer").variable("outerGas")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("outer").variable("outerGas")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component("outer").variable("outerGas")
         .set("Hdiffr", "h_N2*(plas2.tfluxr_wN2X-plas2.cfluxr_wN2X)+h_O2*(plas2.tfluxr_wO2X-plas2.cfluxr_wO2X)+h_N*(plas2.tfluxr_wNX-plas2.cfluxr_wNX)+h_O*(plas2.tfluxr_wOX-plas2.cfluxr_wOX)+h_N2_1p*(plas2.tfluxr_wN2X_1p-plas2.cfluxr_wN2X_1p)+h_O2_1p*(plas2.tfluxr_wO2X_1p-plas2.cfluxr_wO2X_1p)+h_N_1p*(plas2.tfluxr_wNX_1p-plas2.cfluxr_wNX_1p)+h_O_1p*(plas2.tfluxr_wOX_1p-plas2.cfluxr_wOX_1p)+h_O_1m*(plas2.tfluxr_wOX_1m-plas2.cfluxr_wOX_1m)+h_O2_1m*(plas2.tfluxr_wO2X_1m-plas2.cfluxr_wO2X_1m)+h_Cu*(plas2.tfluxr_wCuX-plas2.cfluxr_wCuX)+h_CuD5*(plas2.tfluxr_wCuD5X-plas2.cfluxr_wCuD5X)+h_CuD3*(plas2.tfluxr_wCuD3X-plas2.cfluxr_wCuD3X)+h_CuP1*(plas2.tfluxr_wCuP1X-plas2.cfluxr_wCuP1X)+h_CuP3*(plas2.tfluxr_wCuP3X-plas2.cfluxr_wCuP3X)+h_CuH3*(plas2.tfluxr_wCuH3X-plas2.cfluxr_wCuH3X)+h_CuH5*(plas2.tfluxr_wCuH5X-plas2.cfluxr_wCuH5X)+h_Cu_1p*(plas2.tfluxr_wCuX_1p-plas2.cfluxr_wCuX_1p)");
    model.component("outer").variable("outerGas")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*(plas2.tfluxr_wN2X_1p-plas2.cfluxr_wN2X_1p)+1*F_const/0.0319988[kg/mol]*(plas2.tfluxr_wO2X_1p-plas2.cfluxr_wO2X_1p)+1*F_const/0.0140067[kg/mol]*(plas2.tfluxr_wNX_1p-plas2.cfluxr_wNX_1p)+1*F_const/0.0159994[kg/mol]*(plas2.tfluxr_wOX_1p-plas2.cfluxr_wOX_1p)+-1*F_const/0.0159994[kg/mol]*(plas2.tfluxr_wOX_1m-plas2.cfluxr_wOX_1m)+-1*F_const/0.0319988[kg/mol]*(plas2.tfluxr_wO2X_1m-plas2.cfluxr_wO2X_1m)+1*F_const/0.063546[kg/mol]*(plas2.tfluxr_wCuX_1p-plas2.cfluxr_wCuX_1p)-e_const*plas2.gflux_ner+plas2.scharge*urP");
    model.component("outer").variable("outerGas")
         .set("Hdiffz", "h_N2*(plas2.tfluxz_wN2X-plas2.cfluxz_wN2X)+h_O2*(plas2.tfluxz_wO2X-plas2.cfluxz_wO2X)+h_N*(plas2.tfluxz_wNX-plas2.cfluxz_wNX)+h_O*(plas2.tfluxz_wOX-plas2.cfluxz_wOX)+h_N2_1p*(plas2.tfluxz_wN2X_1p-plas2.cfluxz_wN2X_1p)+h_O2_1p*(plas2.tfluxz_wO2X_1p-plas2.cfluxz_wO2X_1p)+h_N_1p*(plas2.tfluxz_wNX_1p-plas2.cfluxz_wNX_1p)+h_O_1p*(plas2.tfluxz_wOX_1p-plas2.cfluxz_wOX_1p)+h_O_1m*(plas2.tfluxz_wOX_1m-plas2.cfluxz_wOX_1m)+h_O2_1m*(plas2.tfluxz_wO2X_1m-plas2.cfluxz_wO2X_1m)+h_Cu*(plas2.tfluxz_wCuX-plas2.cfluxz_wCuX)+h_CuD5*(plas2.tfluxz_wCuD5X-plas2.cfluxz_wCuD5X)+h_CuD3*(plas2.tfluxz_wCuD3X-plas2.cfluxz_wCuD3X)+h_CuP1*(plas2.tfluxz_wCuP1X-plas2.cfluxz_wCuP1X)+h_CuP3*(plas2.tfluxz_wCuP3X-plas2.cfluxz_wCuP3X)+h_CuH3*(plas2.tfluxz_wCuH3X-plas2.cfluxz_wCuH3X)+h_CuH5*(plas2.tfluxz_wCuH5X-plas2.cfluxz_wCuH5X)+h_Cu_1p*(plas2.tfluxz_wCuX_1p-plas2.cfluxz_wCuX_1p)");
    model.component("outer").variable("outerGas")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*(plas2.tfluxz_wN2X_1p-plas2.cfluxz_wN2X_1p)+1*F_const/0.0319988[kg/mol]*(plas2.tfluxz_wO2X_1p-plas2.cfluxz_wO2X_1p)+1*F_const/0.0140067[kg/mol]*(plas2.tfluxz_wNX_1p-plas2.cfluxz_wNX_1p)+1*F_const/0.0159994[kg/mol]*(plas2.tfluxz_wOX_1p-plas2.cfluxz_wOX_1p)+-1*F_const/0.0159994[kg/mol]*(plas2.tfluxz_wOX_1m-plas2.cfluxz_wOX_1m)+-1*F_const/0.0319988[kg/mol]*(plas2.tfluxz_wO2X_1m-plas2.cfluxz_wO2X_1m)+1*F_const/0.063546[kg/mol]*(plas2.tfluxz_wCuX_1p-plas2.cfluxz_wCuX_1p)-e_const*plas2.gflux_nez+plas2.scharge*uzP");
    model.component("outer").variable("outerGas").set("Qfield", "Jtotalr*plas2.Etr+Jtotalz*plas2.Etz");
    model.component("outer").variable("outerGas").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("outer").variable("outerGas")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component("outer").variable("outerGas").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component("outer").variable("outerGas")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component("outer").variable("outerGas").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component("outer").variable("outerGas").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("outer").variable("outerGas")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component("outer").variable("outerGas").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component("outer").variable("outerGas")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component("outer").variable("outerGas").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component("outer").variable("outerGas").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("outer").variable("outerGas")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component("outer").variable("outerGas").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component("outer").variable("outerGas")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component("outer").variable("outerGas").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component("outer").variable("outerGas").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("outer").variable("outerGas")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component("outer").variable("outerGas").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component("outer").variable("outerGas")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component("outer").variable("outerGas")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component("outer").variable("outerGas").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("outer").variable("outerGas")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component("outer").variable("outerGas")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component("outer").variable("outerGas")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component("outer").variable("outerGas")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component("outer").variable("outerGas").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("outer").variable("outerGas")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component("outer").variable("outerGas").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component("outer").variable("outerGas")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component("outer").variable("outerGas")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component("outer").variable("outerGas").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("outer").variable("outerGas")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component("outer").variable("outerGas").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component("outer").variable("outerGas")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component("outer").variable("outerGas").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component("outer").variable("outerGas").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component("outer").variable("outerGas")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component("outer").variable("outerGas").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component("outer").variable("outerGas")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component("outer").variable("outerGas").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component("outer").variable("outerGas").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nCu_1p");
    model.component("outer").variable("outerGas").set("Eph_Curr", "10^rrMean_Curr(log10(TeV/1[V]))*1[V]");
    model.component("outer").variable("outerGas").set("Qfb_Cu", "r_Curr*e_const*Eph_Curr");
    model.component("outer").variable("outerGas").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nN_1p");
    model.component("outer").variable("outerGas").set("Eph_Nrr", "10^rrMean_Nrr(log10(TeV/1[V]))*1[V]");
    model.component("outer").variable("outerGas").set("Qfb_N", "r_Nrr*e_const*Eph_Nrr");
    model.component("outer").variable("outerGas").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nO_1p");
    model.component("outer").variable("outerGas").set("Eph_Orr", "10^rrMean_Orr(log10(TeV/1[V]))*1[V]");
    model.component("outer").variable("outerGas").set("Qfb_O", "r_Orr*e_const*Eph_Orr");
    model.component("outer").variable("outerGas")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nCu_1p");
    model.component("outer").variable("outerGas").set("Eph_CuD5rr", "10^rrMean_CuD5rr(log10(TeV/1[V]))*1[V]");
    model.component("outer").variable("outerGas").set("Qfb_CuD5", "r_CuD5rr*e_const*Eph_CuD5rr");
    model.component("outer").variable("outerGas")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nCu_1p");
    model.component("outer").variable("outerGas").set("Eph_CuD3rr", "10^rrMean_CuD3rr(log10(TeV/1[V]))*1[V]");
    model.component("outer").variable("outerGas").set("Qfb_CuD3", "r_CuD3rr*e_const*Eph_CuD3rr");
    model.component("outer").variable("outerGas")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nCu_1p");
    model.component("outer").variable("outerGas").set("Eph_CuP1rr", "10^rrMean_CuP1rr(log10(TeV/1[V]))*1[V]");
    model.component("outer").variable("outerGas").set("Qfb_CuP1", "r_CuP1rr*e_const*Eph_CuP1rr");
    model.component("outer").variable("outerGas")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nCu_1p");
    model.component("outer").variable("outerGas").set("Eph_CuP3rr", "10^rrMean_CuP3rr(log10(TeV/1[V]))*1[V]");
    model.component("outer").variable("outerGas").set("Qfb_CuP3", "r_CuP3rr*e_const*Eph_CuP3rr");
    model.component("outer").variable("outerGas")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nCu_1p");
    model.component("outer").variable("outerGas").set("Eph_CuH3rr", "10^rrMean_CuH3rr(log10(TeV/1[V]))*1[V]");
    model.component("outer").variable("outerGas").set("Qfb_CuH3", "r_CuH3rr*e_const*Eph_CuH3rr");
    model.component("outer").variable("outerGas")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nCu_1p");
    model.component("outer").variable("outerGas").set("Eph_CuH5rr", "10^rrMean_CuH5rr(log10(TeV/1[V]))*1[V]");
    model.component("outer").variable("outerGas").set("Qfb_CuH5", "r_CuH5rr*e_const*Eph_CuH5rr");
    model.component("outer").variable("outerGas").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas2.ne*niZ2");
    model.component("outer").variable("outerGas")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+Qfb_Cu+Qfb_N+Qfb_O+Qfb_CuD5+Qfb_CuD3+Qfb_CuP1+Qfb_CuP3+Qfb_CuH3+Qfb_CuH5+Qff");
    model.component("outer").variable("outerGas")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component("outer").variable("outerGas")
         .set("jfbVisible", "Qfb_CuH3*(exp(-max(h_const*c_const/(525[nm]*e_const)-1.5352049[V],0[V])/TeV)-exp(-max(h_const*c_const/(475[nm]*e_const)-1.5352049[V],0[V])/TeV))/(4*pi)+Qfb_CuH5*(exp(-max(h_const*c_const/(525[nm]*e_const)-1.5343548[V],0[V])/TeV)-exp(-max(h_const*c_const/(475[nm]*e_const)-1.5343548[V],0[V])/TeV))/(4*pi)");
    model.component("outer").variable("outerGas")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)+jfbVisible");
    model.component("outer").variable("outerGas")
         .set("QeRR", "r_Curr*e_const*(Eph_Curr-7.72638[V])+r_Nrr*e_const*(Eph_Nrr-14.534[V])+r_Orr*e_const*(Eph_Orr-13.618[V])+r_CuD5rr*e_const*(Eph_CuD5rr-6.337432[V])+r_CuD3rr*e_const*(Eph_CuD3rr-6.0841544[V])+r_CuP1rr*e_const*(Eph_CuP1rr-3.9404822999999998[V])+r_CuP3rr*e_const*(Eph_CuP3rr-3.9096879999999996[V])+r_CuH3rr*e_const*(Eph_CuH3rr-1.5352049[V])+r_CuH5rr*e_const*(Eph_CuH5rr-1.5343548[V])");
    model.component("outer").variable("outerGas")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*plas2.ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*plas2.ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*plas2.ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*plas2.ne*nO2^2)");
    model.component("outer").variable("outerBoundary").set("ncr", "if(nz>=0,nr,-nr)");
    model.component("outer").variable("outerBoundary").set("ncz", "abs(nz)");
    model.component("outer").variable("outerBoundary").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component("outer").variable("outerBoundary")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(outerT-1356[K]))");
    model.component("outer").variable("outerBoundary")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(outerT,150[K])))");
    model.component("outer").variable("outerBoundary")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(outerT,150[K])))");
    model.component("outer").variable("outerBoundary").set("vKn", "sqrt(k_B_const*max(outerT,150[K])/(Mcu/N_A_const))");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas2.Etr*ncr+plas2.Etz*ncz),0[m/s])");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas2.Etr*ncr+plas2.Etz*ncz),0[m/s])");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas2.Etr*ncr+plas2.Etz*ncz),0[m/s])");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas2.Etr*ncr+plas2.Etz*ncz),0[m/s])");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas2.Etr*ncr+plas2.Etz*ncz),0[m/s])");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*(plas2.Etr*ncr+plas2.Etz*ncz),0[m/s])");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component("outer").variable("outerBoundary")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*(plas2.Etr*ncr+plas2.Etz*ncz),0[m/s])");
    model.component("outer").variable("outerBoundary")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component("outer").variable("outerBoundary").set("mdot", "mdotEvap-mdotReturn");
    model.component("outer").variable("outerBoundary").set("recoil", "mdotEvap*vKn");
    model.component("outer").variable("outerBoundary").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component("outer").variable("outerBoundary")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*outerT/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component("outer").variable("outerBoundary")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component("outer").variable("outerBoundary").set("Qexchange", "accom*hAir*(Tg-outerT)");
    model.component("outer").variable("outerBoundary").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component("outer").variable("outerBoundary").set("Gtherm", "plas2.thflux");
    model.component("outer").variable("outerBoundary").set("GeIn", "-plas2.ne_bnd-plas2.nemiog_bnd");
    model.component("outer").variable("outerBoundary")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component("outer").variable("outerBoundary")
         .set("QelectronWall", "e_const*(plas2.en_bnd+plas2.ten_bnd+plas2.enmiog_bnd)");
    model.component("outer").variable("outerBoundary").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component("outer").variable("outerBoundary").set("Qthermion", "QelectronWall+QworkWall");
    model.component("outer").physics("plas2").feature("N2_1")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("N2_2")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("N2_3")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("N2_4")
         .set("de", "0.59*(1-exp(0.59[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.59[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("N2_5")
         .set("de", "0.88*(1-exp(0.88[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.88[V]*e_const/(k_B_const*Tgas)))");
}
static void part6(Model model){

    model.component("outer").physics("plas2").feature("N2_6")
         .set("de", "1.17*(1-exp(1.17[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.17[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("N2_7")
         .set("de", "1.47*(1-exp(1.47[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.47[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("N2_8")
         .set("de", "1.76*(1-exp(1.76[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.76[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("O2_3")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("O2_4")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("O2_5")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("O2_6")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("O2_7")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("O2_8")
         .set("de", "0.57*(1-exp(0.57[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.57[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("O2_9")
         .set("de", "0.75*(1-exp(0.75[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.75[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("O2_10")
         .set("de", "0.977*(1-exp(0.977[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.977[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").physics("plas2").feature("O2_11")
         .set("de", "1.627*(1-exp(1.627[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.627[V]*e_const/(k_B_const*Tgas)))");
    model.component("outer").variable("outerGas").set("nOtherNeutral", "nN+nO+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5");
    model.component("outer").variable("outerGas").set("nuEN", "nN2*kmN2+nO2*kmO2+nOtherNeutral*kmCu");
    model.component("outer").physics("plas2").feature("N2X").set("n0", "1[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wO2X", "wO2X");
    model.component("outer").physics("plas2").feature("O2X").set("n0", "0.21*pAmb/(k_B_const*Tamb)");
    model.component("outer").variable("outerGas").set("plas2.wNX", "wNX");
    model.component("outer").physics("plas2").feature("NX").set("n0", "1[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wOX", "wOX");
    model.component("outer").physics("plas2").feature("OX").set("n0", "1[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wN2X_1p", "wN2X_1p");
    model.component("outer").physics("plas2").feature("N2X_1p").set("n0", "1e7[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wO2X_1p", "wO2X_1p");
    model.component("outer").physics("plas2").feature("O2X_1p").set("n0", "1e7[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wNX_1p", "wNX_1p");
    model.component("outer").physics("plas2").feature("NX_1p").set("n0", "1e7[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wOX_1p", "wOX_1p");
    model.component("outer").physics("plas2").feature("OX_1p").set("n0", "1e7[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wOX_1m", "wOX_1m");
    model.component("outer").physics("plas2").feature("OX_1m").set("n0", "1e7[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wO2X_1m", "wO2X_1m");
    model.component("outer").physics("plas2").feature("O2X_1m").set("n0", "1e7[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wCuX", "wCuX");
    model.component("outer").physics("plas2").feature("CuX").set("n0", "1[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wCuD5X", "wCuD5X");
    model.component("outer").physics("plas2").feature("CuD5X").set("n0", "1[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wCuD3X", "wCuD3X");
    model.component("outer").physics("plas2").feature("CuD3X").set("n0", "1[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wCuP1X", "wCuP1X");
    model.component("outer").physics("plas2").feature("CuP1X").set("n0", "1[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wCuP3X", "wCuP3X");
    model.component("outer").physics("plas2").feature("CuP3X").set("n0", "1[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wCuH3X", "wCuH3X");
    model.component("outer").physics("plas2").feature("CuH3X").set("n0", "1[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wCuH5X", "wCuH5X");
    model.component("outer").physics("plas2").feature("CuH5X").set("n0", "1[1/m^3]");
    model.component("outer").variable("outerGas").set("plas2.wCuX_1p", "wCuX_1p");
    model.component("outer").physics("plas2").feature("CuX_1p").set("n0", "1e7[1/m^3]");
    model.component("outer").physics("plas2").feature("N2X_1p").set("n0", "seedNe-2e7[1/m^3]");
    model.component("outer").physics("plas2").feature("N2X_1p").set("InitIon", false);
    model.component("outer").physics("plas2").feature("pes1").set("Tgm", "Tg");
    model.component("outer").physics("plas2").feature("pes1").set("Tgm0", "Tamb");
    model.component("outer").physics("plas2").feature("pes1").set("p0", "pAmb");
    model.component("outer").physics("plas2").feature("pes1").set("ebarinitgm", "1.5*k_B_const*Tamb/e_const");
    model.component("outer").variable("outerGas").set("Tg", "exp(logGasT)*1[K]");
    model.component("outer").variable("outerGas").set("Tgas", "Tg");
    model.component("outer").variable("outerGas").set("rhoP", "plas2.rho");
    model.component("outer").variable("outerGas").set("urP", "0[m/s]");
    model.component("outer").variable("outerGas").set("uzP", "0[m/s]");
    model.component("outer").variable("outerGas").set("Dgas", "1e-5[m^2/s]");
    model.component("outer").variable("outerGas").set("Rp", "Rinit*exp(logRadius)");
    model.component("outer").variable("outerGas").set("Hp", "Hinit*exp(logHeight)");
    model.component("outer").variable("outerGas").set("Vp", "2*pi/3*Rp^2*Hp");
    model.component("outer").variable("outerGas").set("Mgas", "rhoP*Vp");
    model.component("outer").variable("outerGas").set("vRadius", "momentumScale*pRadius/Mgas");
    model.component("outer").variable("outerGas").set("vHeight", "momentumScale*pHeight/Mgas");
    model.component("outer").variable("outerGas").set("volumeRate", "2*vRadius/Rp+vHeight/Hp");
    model.component("outer").variable("outerGas").set("Vdot", "Vp*volumeRate");
    model.component("outer").variable("outerGas").set("Msweep", "rhoAmb*max(Vdot,0[m^3/s])");
    model.component("outer").variable("outerGas").set("Kinetic", "Mgas*(2*vRadius^2+vHeight^2)/10");
    model.component("outer").variable("outerGas").set("pPl", "plas2.pA+pe");
    model.component("outer").variable("outerGas").set("Req", "(3*Vp/(4*pi))^(1/3)");
    model.component("outer").variable("outerGas").set("pulse", "exp(-4*log(2)*((t-tp)/tau)^2)");
    model.component("outer").variable("outerGas").set("Pincident", "Ein*sqrt(4*log(2)/pi)/tau*pulse");
    model.component("outer").variable("outerGas").set("Ipeak", "2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");
    model.component("outer").variable("outerGas").set("beamCoverage", "1-exp(-2*(Rp/w0)^2)");
    model.component("outer").variable("outerGas").set("laserTau", "alphaLaser*Hp");
    model.component("outer").variable("outerGas").set("beamTransmission", "exp(-laserTau)");
    model.component("outer").variable("outerGas")
         .set("beamAbsorptance", "if(laserTau<1e-4,laserTau*(1-laserTau/2+laserTau^2/6-laserTau^3/24),1-exp(-laserTau))");
    model.component("outer").variable("outerGas")
         .set("Plaser", "Pincident*beamCoverage*beamAbsorptance*(1+(1-Acu)*beamTransmission)");
    model.component("outer").variable("outerGas").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*Req");
    model.component("outer").variable("outerGas")
         .set("beta1", "if(tauLine1<0.01,1-3*tauLine1/4+2*tauLine1^2/5,3/(4*tauLine1)*(1-1/(2*tauLine1^2)+(1/tauLine1+1/(2*tauLine1^2))*exp(-2*tauLine1)))");
    model.component("outer").variable("outerGas").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*Req");
    model.component("outer").variable("outerGas")
         .set("beta2", "if(tauLine2<0.01,1-3*tauLine2/4+2*tauLine2^2/5,3/(4*tauLine2)*(1-1/(2*tauLine2^2)+(1/tauLine2+1/(2*tauLine2^2))*exp(-2*tauLine2)))");
    model.component("outer").variable("outerGas").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*Req");
    model.component("outer").variable("outerGas")
         .set("beta3", "if(tauLine3<0.01,1-3*tauLine3/4+2*tauLine3^2/5,3/(4*tauLine3)*(1-1/(2*tauLine3^2)+(1/tauLine3+1/(2*tauLine3^2))*exp(-2*tauLine3)))");
    model.component("outer").variable("outerGas").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*Req");
    model.component("outer").variable("outerGas")
         .set("beta4", "if(tauLine4<0.01,1-3*tauLine4/4+2*tauLine4^2/5,3/(4*tauLine4)*(1-1/(2*tauLine4^2)+(1/tauLine4+1/(2*tauLine4^2))*exp(-2*tauLine4)))");
    model.component("outer").variable("outerGas")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*Req");
    model.component("outer").variable("outerGas")
         .set("beta5", "if(tauLine5<0.01,1-3*tauLine5/4+2*tauLine5^2/5,3/(4*tauLine5)*(1-1/(2*tauLine5^2)+(1/tauLine5+1/(2*tauLine5^2))*exp(-2*tauLine5)))");
    model.component("outer").variable("outerGas").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*Req");
    model.component("outer").variable("outerGas")
         .set("beta6", "if(tauLine6<0.01,1-3*tauLine6/4+2*tauLine6^2/5,3/(4*tauLine6)*(1-1/(2*tauLine6^2)+(1/tauLine6+1/(2*tauLine6^2))*exp(-2*tauLine6)))");
    model.component("outer").variable("outerGas").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*Req");
    model.component("outer").variable("outerGas")
         .set("beta7", "if(tauLine7<0.01,1-3*tauLine7/4+2*tauLine7^2/5,3/(4*tauLine7)*(1-1/(2*tauLine7^2)+(1/tauLine7+1/(2*tauLine7^2))*exp(-2*tauLine7)))");
    model.component("outer").variable("outerGas").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*Req");
    model.component("outer").variable("outerGas")
         .set("beta8", "if(tauLine8<0.01,1-3*tauLine8/4+2*tauLine8^2/5,3/(4*tauLine8)*(1-1/(2*tauLine8^2)+(1/tauLine8+1/(2*tauLine8^2))*exp(-2*tauLine8)))");
    model.component("outer").physics("plas2").create("elasticAtomN", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("elasticAtomN").set("formula", "e+NX=>e+NX");
    model.component("outer").physics("plas2").feature("elasticAtomN").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("elasticAtomN").set("kf", "kmCu*N_A_const");
    model.component("outer").physics("plas2").feature("elasticAtomN").set("mratio", "me_const*N_A_const/plas2.M_wNX");
    model.component("outer").physics("plas2").create("elasticAtomO", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("elasticAtomO").set("formula", "e+OX=>e+OX");
    model.component("outer").physics("plas2").feature("elasticAtomO").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("elasticAtomO").set("kf", "kmCu*N_A_const");
    model.component("outer").physics("plas2").feature("elasticAtomO").set("mratio", "me_const*N_A_const/plas2.M_wOX");
    model.component("outer").physics("plas2").create("elasticAtomCu", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("elasticAtomCu").set("formula", "e+CuX=>e+CuX");
    model.component("outer").physics("plas2").feature("elasticAtomCu").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("elasticAtomCu").set("kf", "kmCu*N_A_const");
    model.component("outer").physics("plas2").feature("elasticAtomCu").set("mratio", "me_const*N_A_const/plas2.M_wCuX");
    model.component("outer").physics("plas2").create("elasticAtomCuD5", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("elasticAtomCuD5").set("formula", "e+CuD5X=>e+CuD5X");
    model.component("outer").physics("plas2").feature("elasticAtomCuD5").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("elasticAtomCuD5").set("kf", "kmCu*N_A_const");
    model.component("outer").physics("plas2").feature("elasticAtomCuD5").set("mratio", "me_const*N_A_const/plas2.M_wCuD5X");
    model.component("outer").physics("plas2").create("elasticAtomCuD3", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("elasticAtomCuD3").set("formula", "e+CuD3X=>e+CuD3X");
    model.component("outer").physics("plas2").feature("elasticAtomCuD3").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("elasticAtomCuD3").set("kf", "kmCu*N_A_const");
    model.component("outer").physics("plas2").feature("elasticAtomCuD3").set("mratio", "me_const*N_A_const/plas2.M_wCuD3X");
    model.component("outer").physics("plas2").create("elasticAtomCuP1", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("elasticAtomCuP1").set("formula", "e+CuP1X=>e+CuP1X");
    model.component("outer").physics("plas2").feature("elasticAtomCuP1").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("elasticAtomCuP1").set("kf", "kmCu*N_A_const");
    model.component("outer").physics("plas2").feature("elasticAtomCuP1").set("mratio", "me_const*N_A_const/plas2.M_wCuP1X");
    model.component("outer").physics("plas2").create("elasticAtomCuP3", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("elasticAtomCuP3").set("formula", "e+CuP3X=>e+CuP3X");
    model.component("outer").physics("plas2").feature("elasticAtomCuP3").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("elasticAtomCuP3").set("kf", "kmCu*N_A_const");
    model.component("outer").physics("plas2").feature("elasticAtomCuP3").set("mratio", "me_const*N_A_const/plas2.M_wCuP3X");
    model.component("outer").physics("plas2").create("elasticAtomCuH3", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("elasticAtomCuH3").set("formula", "e+CuH3X=>e+CuH3X");
    model.component("outer").physics("plas2").feature("elasticAtomCuH3").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("elasticAtomCuH3").set("kf", "kmCu*N_A_const");
    model.component("outer").physics("plas2").feature("elasticAtomCuH3").set("mratio", "me_const*N_A_const/plas2.M_wCuH3X");
    model.component("outer").physics("plas2").create("elasticAtomCuH5", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("elasticAtomCuH5").set("formula", "e+CuH5X=>e+CuH5X");
    model.component("outer").physics("plas2").feature("elasticAtomCuH5").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("elasticAtomCuH5").set("kf", "kmCu*N_A_const");
    model.component("outer").physics("plas2").feature("elasticAtomCuH5").set("mratio", "me_const*N_A_const/plas2.M_wCuH5X");
    model.component("outer").variable("outerGas")
         .set("Qei", "3*me_const*N_A_const*plas2.ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*(0[mol/(kg*m^3)]+plas2.n_wN2X_1p/plas2.M_wN2X_1p+plas2.n_wO2X_1p/plas2.M_wO2X_1p+plas2.n_wNX_1p/plas2.M_wNX_1p+plas2.n_wOX_1p/plas2.M_wOX_1p+plas2.n_wOX_1m/plas2.M_wOX_1m+plas2.n_wO2X_1m/plas2.M_wO2X_1m+plas2.n_wCuX_1p/plas2.M_wCuX_1p)");
    model.component("outer").variable("outerSolid").set("fL", "0.5*(1+(outerT-Tm)/sqrt((outerT-Tm)^2+100[K^2]))");
    model.component("outer").variable("outerSolid")
         .set("cpCu", "385[J/(kg*K)]*(1-fL)+572[J/(kg*K)]*fL+Lm*50[K^2]/((outerT-Tm)^2+100[K^2])^1.5");
    model.component("outer").variable("outerSolid")
         .set("hCond", "385[J/(kg*K)]*(outerT-Tamb)+187[J/(kg*K)]*0.5*(outerT-Tamb+sqrt((outerT-Tm)^2+100[K^2])-sqrt((Tamb-Tm)^2+100[K^2]))+Lm*(fL-0.5*(1+(Tamb-Tm)/sqrt((Tamb-Tm)^2+100[K^2])))");
    model.component("outer").variable("outerSolid").set("kCu", "385[W/(m*K)]*(1-fL)+165[W/(m*K)]*fL");
    model.component("outer").material().create("outerCopper", "Common");
    model.component("outer").material("outerCopper").selection().named("og_cu_dom");
    model.component("outer").material("outerCopper").propertyGroup("def").set("density", "rhoCu");
}
static void part7(Model model){

    model.component("outer").material("outerCopper").propertyGroup("def").set("heatcapacity", "cpCu");
    model.component("outer").material("outerCopper").propertyGroup("def").set("thermalconductivity", "kCu");
    model.component("outer").variable("outerBoundary").set("contact", "flc2hs(Rp-r,1[um])");
    model.component("outer").variable("outerBoundary")
         .set("Qsurface", "Acu*Ipeak*pulse*exp(-2*(r/w0)^2)*(1-contact*(1-beamTransmission))");
    model.component("outer").variable("outerBoundary")
         .set("mdotReturn", "contact*rhoCopper*sqrt(k_B_const*Tg/(2*pi*Mcu/N_A_const))");
    model.component("outer").variable("outerBoundary").set("mdot", "mdotEvap-mdotReturn");
    model.component("outer").variable("outerBoundary").set("emitWeight_Cu", "2*exp(-0.0[V]*e_const/(k_B_const*outerT))");
    model.component("outer").variable("outerBoundary").set("emitWeight_CuD5", "6*exp(-1.388948[V]*e_const/(k_B_const*outerT))");
    model.component("outer").variable("outerBoundary").set("emitWeight_CuD3", "4*exp(-1.6422256[V]*e_const/(k_B_const*outerT))");
    model.component("outer").variable("outerBoundary").set("emitWeight_CuP1", "2*exp(-3.7858977[V]*e_const/(k_B_const*outerT))");
    model.component("outer").variable("outerBoundary").set("emitWeight_CuP3", "4*exp(-3.816692[V]*e_const/(k_B_const*outerT))");
    model.component("outer").variable("outerBoundary").set("emitWeight_CuH3", "4*exp(-6.1911751[V]*e_const/(k_B_const*outerT))");
    model.component("outer").variable("outerBoundary").set("emitWeight_CuH5", "6*exp(-6.1920252[V]*e_const/(k_B_const*outerT))");
    model.component("outer").variable("outerBoundary")
         .set("emitWeight_Cu_1p", "1*exp((workCu-7.72638[V])*e_const/(k_B_const*outerT))");
    model.component("outer").variable("outerBoundary")
         .set("emitPartition", "0+emitWeight_Cu+emitWeight_CuD5+emitWeight_CuD3+emitWeight_CuP1+emitWeight_CuP3+emitWeight_CuH3+emitWeight_CuH5+emitWeight_Cu_1p");
    model.component("outer").variable("outerBoundary").set("emitFraction_Cu", "emitWeight_Cu/emitPartition");
    model.component("outer").variable("outerBoundary")
         .set("source_Cu", "mdotEvap*emitFraction_Cu-mdotReturn*plas2.n_wCuX/max(nCuTotal,1[1/m^3])");
    model.component("outer").variable("outerBoundary").set("emitFraction_CuD5", "emitWeight_CuD5/emitPartition");
    model.component("outer").variable("outerBoundary")
         .set("source_CuD5", "mdotEvap*emitFraction_CuD5-mdotReturn*plas2.n_wCuD5X/max(nCuTotal,1[1/m^3])");
    model.component("outer").variable("outerBoundary").set("emitFraction_CuD3", "emitWeight_CuD3/emitPartition");
    model.component("outer").variable("outerBoundary")
         .set("source_CuD3", "mdotEvap*emitFraction_CuD3-mdotReturn*plas2.n_wCuD3X/max(nCuTotal,1[1/m^3])");
    model.component("outer").variable("outerBoundary").set("emitFraction_CuP1", "emitWeight_CuP1/emitPartition");
    model.component("outer").variable("outerBoundary")
         .set("source_CuP1", "mdotEvap*emitFraction_CuP1-mdotReturn*plas2.n_wCuP1X/max(nCuTotal,1[1/m^3])");
    model.component("outer").variable("outerBoundary").set("emitFraction_CuP3", "emitWeight_CuP3/emitPartition");
    model.component("outer").variable("outerBoundary")
         .set("source_CuP3", "mdotEvap*emitFraction_CuP3-mdotReturn*plas2.n_wCuP3X/max(nCuTotal,1[1/m^3])");
    model.component("outer").variable("outerBoundary").set("emitFraction_CuH3", "emitWeight_CuH3/emitPartition");
    model.component("outer").variable("outerBoundary")
         .set("source_CuH3", "mdotEvap*emitFraction_CuH3-mdotReturn*plas2.n_wCuH3X/max(nCuTotal,1[1/m^3])");
    model.component("outer").variable("outerBoundary").set("emitFraction_CuH5", "emitWeight_CuH5/emitPartition");
    model.component("outer").variable("outerBoundary")
         .set("source_CuH5", "mdotEvap*emitFraction_CuH5-mdotReturn*plas2.n_wCuH5X/max(nCuTotal,1[1/m^3])");
    model.component("outer").variable("outerBoundary").set("emitFraction_Cu_1p", "emitWeight_Cu_1p/emitPartition");
    model.component("outer").variable("outerBoundary")
         .set("source_Cu_1p", "mdotEvap*emitFraction_Cu_1p-mdotReturn*plas2.n_wCuX_1p/max(nCuTotal,1[1/m^3])");
    model.component("outer").variable("outerBoundary")
         .set("emitMeanEnergy", "0[V]+emitFraction_Cu*0.0[V]+emitFraction_CuD5*1.388948[V]+emitFraction_CuD3*1.6422256[V]+emitFraction_CuP1*3.7858977[V]+emitFraction_CuP3*3.816692[V]+emitFraction_CuH3*6.1911751[V]+emitFraction_CuH5*6.1920252[V]+emitFraction_Cu_1p*7.72638[V]");
    model.component("outer").variable("outerGas")
         .set("returnMeanEnergy", "0[V]+plas2.n_wCuX/max(nCuTotal,1[1/m^3])*0.0[V]+plas2.n_wCuD5X/max(nCuTotal,1[1/m^3])*1.388948[V]+plas2.n_wCuD3X/max(nCuTotal,1[1/m^3])*1.6422256[V]+plas2.n_wCuP1X/max(nCuTotal,1[1/m^3])*3.7858977[V]+plas2.n_wCuP3X/max(nCuTotal,1[1/m^3])*3.816692[V]+plas2.n_wCuH3X/max(nCuTotal,1[1/m^3])*6.1911751[V]+plas2.n_wCuH5X/max(nCuTotal,1[1/m^3])*6.1920252[V]+plas2.n_wCuX_1p/max(nCuTotal,1[1/m^3])*7.72638[V]");
    model.component("outer").variable("outerBoundary")
         .set("electronSourceEnergy", "2*k_B_const*outerT*mdotEvap*N_A_const/Mcu*emitFraction_Cu_1p-2.5*k_B_const*TeK*mdotReturn*N_A_const/Mcu*nCu_1p/max(nCuTotal,1[1/m^3])");
    model.component("outer").variable("outerBoundary")
         .set("Qmass", "mdotEvap*(Bcu+3*R_const*outerT/Mcu+F_const*emitMeanEnergy/Mcu)-mdotReturn*(Bcu+2.5*R_const*Tg/Mcu+F_const*returnMeanEnergy/Mcu)+electronSourceEnergy");
    model.component("outer").variable("outerBoundary")
         .set("Qexchange", "contact*2.5*k_B_const*nHeavy*sqrt(8*k_B_const*Tg/(pi*Mair/N_A_const))/4*(Tg-outerT)");
    model.component("outer").variable("outerGas").set("Msource", "outerIntSurf(mdot)+Msweep");
    model.component("outer").variable("outerGas").set("Psource", "outerIntSurf(Qmass-Qexchange)");
    model.component("outer").variable("outerGas").set("PeSource", "outerIntSurf(electronSourceEnergy)");
    model.component("outer").physics("plas2").feature("pes1").featureInfo("info").set("plas2.Vol_gm", new String[]{"Vp"});
    model.component("outer").physics("plas2").feature("pes1").featureInfo("info")
         .set("plas2.mfinGm", new String[]{"Msource"});
    model.component("outer").physics("plas2").feature("pes1").featureInfo("info")
         .set("plas2.Nn0", new String[]{"pAmb/(k_B_const*Tamb)"});
    model.component("outer").physics("plas2").feature("O2X").featureInfo("info")
         .set("plas2.R_wO2X", new String[]{"(-plas2.r_27-plas2.r_28-plas2.r_40-plas2.r_41-plas2.r_42-plas2.r_43-plas2.r_101+plas2.r_102+plas2.r_103+plas2.r_105+plas2.r_107+plas2.r_108+2*plas2.r_109+plas2.r_111+plas2.r_113-plas2.r_120+plas2.r_121-plas2.r_122+plas2.r_123-plas2.r_124+plas2.r_125)+(Msweep*(0.21*0.0319988[kg/mol]/Mair)/Vp)/plas2.M_wO2X"});
    model.component("outer").physics("plas2").feature("NX").featureInfo("info")
         .set("plas2.R_wNX", new String[]{"(-plas2.r_75+plas2.r_76+plas2.r_77+2*plas2.r_99+plas2.r_110+plas2.r_111+2*plas2.r_114-2*plas2.r_115+2*plas2.r_116-2*plas2.r_117+2*plas2.r_118-2*plas2.r_119)+(0[kg/(m^3*s)])/plas2.M_wNX"});
    model.component("outer").physics("plas2").feature("OX").featureInfo("info")
         .set("plas2.R_wOX", new String[]{"(plas2.r_27+plas2.r_28+2*plas2.r_40+2*plas2.r_41+2*plas2.r_42-plas2.r_78+plas2.r_79+plas2.r_80+2*plas2.r_100-plas2.r_102+plas2.r_104+plas2.r_106+plas2.r_108+plas2.r_110+2*plas2.r_112+plas2.r_113+2*plas2.r_120-2*plas2.r_121+2*plas2.r_122-2*plas2.r_123+2*plas2.r_124-2*plas2.r_125)+(0[kg/(m^3*s)])/plas2.M_wOX"});
    model.component("outer").physics("plas2").feature("N2X_1p").featureInfo("info")
         .set("plas2.R_wN2X_1p", new String[]{"(plas2.r_25+plas2.r_26-plas2.r_99-plas2.r_106-plas2.r_107)+(0[kg/(m^3*s)])/plas2.M_wN2X_1p"});
    model.component("outer").physics("plas2").feature("O2X_1p").featureInfo("info")
         .set("plas2.R_wO2X_1p", new String[]{"(plas2.r_43-plas2.r_100-plas2.r_108-plas2.r_109)+(0[kg/(m^3*s)])/plas2.M_wO2X_1p"});
    model.component("outer").physics("plas2").feature("NX_1p").featureInfo("info")
         .set("plas2.R_wNX_1p", new String[]{"(plas2.r_75-plas2.r_76-plas2.r_77-plas2.r_110-plas2.r_111)+(0[kg/(m^3*s)])/plas2.M_wNX_1p"});
    model.component("outer").physics("plas2").feature("OX_1p").featureInfo("info")
         .set("plas2.R_wOX_1p", new String[]{"(plas2.r_78-plas2.r_79-plas2.r_80-plas2.r_112-plas2.r_113)+(0[kg/(m^3*s)])/plas2.M_wOX_1p"});
    model.component("outer").physics("plas2").feature("OX_1m").featureInfo("info")
         .set("plas2.R_wOX_1m", new String[]{"(plas2.r_27+plas2.r_28-plas2.r_102-plas2.r_104-plas2.r_106-plas2.r_108-plas2.r_110-plas2.r_112)+(0[kg/(m^3*s)])/plas2.M_wOX_1m"});
    model.component("outer").physics("plas2").feature("O2X_1m").featureInfo("info")
         .set("plas2.R_wO2X_1m", new String[]{"(plas2.r_101-plas2.r_103-plas2.r_105-plas2.r_107-plas2.r_109-plas2.r_111-plas2.r_113)+(0[kg/(m^3*s)])/plas2.M_wO2X_1m"});
    model.component("outer").physics("plas2").feature("CuX").featureInfo("info")
         .set("plas2.R_wCuX", new String[]{"(-plas2.r_44+plas2.r_45-plas2.r_46+plas2.r_47-plas2.r_48+plas2.r_49-plas2.r_50+plas2.r_51-plas2.r_72+plas2.r_73+plas2.r_52+plas2.r_53+plas2.r_74+plas2.r_104+plas2.r_105)+(outerIntSurf(source_Cu)/Vp)/plas2.M_wCuX"});
    model.component("outer").physics("plas2").feature("CuD5X").featureInfo("info")
         .set("plas2.R_wCuD5X", new String[]{"(plas2.r_44-plas2.r_45-plas2.r_55+plas2.r_56-plas2.r_81+plas2.r_82+plas2.r_54+plas2.r_83)+(outerIntSurf(source_CuD5)/Vp)/plas2.M_wCuD5X"});
    model.component("outer").physics("plas2").feature("CuD3X").featureInfo("info")
         .set("plas2.R_wCuD3X", new String[]{"(plas2.r_46-plas2.r_47-plas2.r_67+plas2.r_68-plas2.r_70+plas2.r_71-plas2.r_84+plas2.r_85+plas2.r_66+plas2.r_69+plas2.r_86)+(outerIntSurf(source_CuD3)/Vp)/plas2.M_wCuD3X"});
    model.component("outer").physics("plas2").feature("CuP1X").featureInfo("info")
         .set("plas2.R_wCuP1X", new String[]{"(plas2.r_48-plas2.r_49-plas2.r_58+plas2.r_59+plas2.r_70-plas2.r_71-plas2.r_87+plas2.r_88-plas2.r_53+plas2.r_57-plas2.r_69+plas2.r_89)+(outerIntSurf(source_CuP1)/Vp)/plas2.M_wCuP1X"});
    model.component("outer").physics("plas2").feature("CuP3X").featureInfo("info")
         .set("plas2.R_wCuP3X", new String[]{"(plas2.r_50-plas2.r_51+plas2.r_55-plas2.r_56-plas2.r_61+plas2.r_62-plas2.r_64+plas2.r_65+plas2.r_67-plas2.r_68-plas2.r_90+plas2.r_91-plas2.r_52-plas2.r_54+plas2.r_60+plas2.r_63-plas2.r_66+plas2.r_92)+(outerIntSurf(source_CuP3)/Vp)/plas2.M_wCuP3X"});
    model.component("outer").physics("plas2").feature("CuH3X").featureInfo("info")
         .set("plas2.R_wCuH3X", new String[]{"(plas2.r_58-plas2.r_59+plas2.r_64-plas2.r_65-plas2.r_93+plas2.r_94-plas2.r_57-plas2.r_63+plas2.r_95)+(outerIntSurf(source_CuH3)/Vp)/plas2.M_wCuH3X"});
    model.component("outer").physics("plas2").feature("CuH5X").featureInfo("info")
         .set("plas2.R_wCuH5X", new String[]{"(plas2.r_61-plas2.r_62-plas2.r_96+plas2.r_97-plas2.r_60+plas2.r_98)+(outerIntSurf(source_CuH5)/Vp)/plas2.M_wCuH5X"});
    model.component("outer").physics("plas2").feature("CuX_1p").featureInfo("info")
         .set("plas2.R_wCuX_1p", new String[]{"(plas2.r_72-plas2.r_73+plas2.r_81-plas2.r_82+plas2.r_84-plas2.r_85+plas2.r_87-plas2.r_88+plas2.r_90-plas2.r_91+plas2.r_93-plas2.r_94+plas2.r_96-plas2.r_97-plas2.r_74-plas2.r_83-plas2.r_86-plas2.r_89-plas2.r_92-plas2.r_95-plas2.r_98-plas2.r_104-plas2.r_105)+(outerIntSurf(source_Cu_1p)/Vp)/plas2.M_wCuX_1p"});
    model.component("outer").physics("plas2").feature("pes1").featureInfo("info")
         .set("root.outer.plas2.pes1.weak$5", new String[]{"(d(plas2.rho,t)+plas2.rho*volumeRate)*test(plas2.pA)"});
    model.component("outer").physics("plas2").feature("pes1")
         .set("Pgm", "Plaser+PeSource-Vp*(QeRR+QeAttach+Qff+Qei+(Ue+pe)*volumeRate)");
    model.component("outer").physics().create("outerHeat", "HeatTransfer", "og");
    model.component("outer").physics("outerHeat").field("temperature").field("outerT");
    model.component("outer").physics("outerHeat").selection().named("og_cu_dom");
    model.component("outer").physics("outerHeat").feature("init1").set("Tinit", "Tamb");
    model.component("outer").physics("outerHeat").create("laser", "HeatFluxBoundary", 1);
    model.component("outer").physics("outerHeat").feature("laser").selection().named("outerSurface");
    model.component("outer").physics("outerHeat").feature("laser").set("q0", "Qsurface-Qmass+Qexchange");
    model.component("outer").physics("outerHeat").create("base", "TemperatureBoundary", 1);
    model.component("outer").physics("outerHeat").feature("base").selection().named("outerBottom");
    model.component("outer").physics("outerHeat").feature("base").set("T0", "Tamb");
    model.component("outer").physics().create("outerBulk", "GlobalEquations", "og");
    model.component("outer").physics("outerBulk").feature("ge1")
         .set("name", new String[]{"logRadius", "logHeight", "pRadius", "pHeight", "gasEnergy", "logGasT", "absorbedEnergy", "radiatedEnergy", "ambientWork", "entrainedEnergy", 
         "removedMass"});
    model.component("outer").physics("outerBulk").feature("ge1")
         .set("equation", new String[]{"logRadiust-vRadius/Rp", "logHeightt-vHeight/Hp", "pRadiust-5*(pPl-pAmb)*Vp/(Rp*momentumScale)", "pHeightt-5*(pPl-pAmb)*Vp/(Hp*momentumScale)", "gasEnergyt-(Psource+Plaser-Qrad*Vp-pAmb*Vdot+eAir*Msweep)/energyScale", "(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue)-Kinetic)/energyScale", "absorbedEnergyt-(outerIntSurf(Qsurface)+Plaser)/energyScale", "radiatedEnergyt-Qrad*Vp/energyScale", "ambientWorkt-pAmb*Vdot/energyScale", "entrainedEnergyt-eAir*Msweep/energyScale", 
         "removedMasst-outerIntSurf(mdot)/mScale"});
    model.component("outer").physics("outerBulk").feature("ge1")
         .set("initialValueU", new String[]{"0", "0", "0", "0", "2.5*pAmb*Vinit/energyScale", "log(Tamb/1[K])", "0", "0", "0", "0", 
         "0"});
    model.component("outer").mesh().create("omesh");
    model.component("outer").mesh("omesh").feature("size").set("custom", true);
    model.component("outer").mesh("omesh").feature("size").set("hmax", "15[um]");
    model.component("outer").mesh("omesh").feature("size").set("hmin", "0.3[um]");
    model.component("outer").mesh("omesh").feature("size").set("hgrad", 1.3);
    model.component("outer").mesh("omesh").create("hotSize", "Size");
    model.component("outer").mesh("omesh").feature("hotSize").selection().named("og_hot_dom");
    model.component("outer").mesh("omesh").feature("hotSize").set("custom", true);
    model.component("outer").mesh("omesh").feature("hotSize").set("hmaxactive", true);
    model.component("outer").mesh("omesh").feature("hotSize").set("hmax", "0.5[um]");
    model.component("outer").mesh("omesh").create("tri", "FreeTri");
    model.component("outer").mesh("omesh").run();
    model.component("outer").variable("outerGas").set("tailKu", "10^tailUp(log10(TeV/1[V]))*1[m^3/s]");
    model.component("outer").variable("outerGas").set("tailKd", "10^tailDn(log10(TeV/1[V]))*1[m^3/s]");
    model.component("outer").variable("outerGas").set("tailKi", "10^tailIon(log10(TeV/1[V]))*1[m^3/s]");
    model.component("outer").variable("outerGas").set("tailK3", "10^tailRec(log10(TeV/1[V]))*1[m^6/s]");
    model.component("outer").variable("outerGas").set("tailKrr", "10^tailRR(log10(TeV/1[V]))*1[m^3/s]");
    model.component("outer").variable("outerGas").set("tailMeanRR", "10^tailRRmean(log10(TeV/1[V]))*1[V]");
    model.component("outer").physics("plas2").create("nativeTailUp", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("nativeTailUp").set("formula", "e+CuP3X=>e+CuT5X");
    model.component("outer").physics("plas2").feature("nativeTailUp").set("kf", "tailKu*N_A_const");
    model.component("outer").physics("plas2").feature("nativeTailUp").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("nativeTailUp").set("de", "3.0509536");
    model.component("outer").physics("plas2").create("nativeTailDn", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("nativeTailDn").set("formula", "e+CuT5X=>e+CuP3X");
    model.component("outer").physics("plas2").feature("nativeTailDn").set("kf", "tailKd*N_A_const");
    model.component("outer").physics("plas2").feature("nativeTailDn").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("nativeTailDn").set("de", "-3.0509536");
    model.component("outer").physics("plas2").create("nativeTailIon", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("nativeTailIon").set("formula", "e+CuT5X=>2e+CuX+");
    model.component("outer").physics("plas2").feature("nativeTailIon").set("kf", "tailKi*N_A_const");
    model.component("outer").physics("plas2").feature("nativeTailIon").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("nativeTailIon").set("de", "0.8587344");
    model.component("outer").physics("plas2").create("nativeTail3rec", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("nativeTail3rec").set("formula", "2e+CuX+=>e+CuT5X");
    model.component("outer").physics("plas2").feature("nativeTail3rec").set("kf", "tailK3*N_A_const^2");
    model.component("outer").physics("plas2").feature("nativeTail3rec").set("type", "Excitation");
    model.component("outer").physics("plas2").feature("nativeTail3rec").set("de", "-0.8587344");
    model.component("outer").physics("plas2").create("nativeTailRR", "Reaction", 2);
    model.component("outer").physics("plas2").feature("nativeTailRR").set("formula", "e+CuX+=>CuT5X");
    model.component("outer").physics("plas2").feature("nativeTailRR").set("kf", "tailKrr*N_A_const");
    model.component("outer").physics("plas2").create("nativeTailRad", "Reaction", 2);
    model.component("outer").physics("plas2").feature("nativeTailRad").set("formula", "CuT5X=>CuP3X");
    model.component("outer").physics("plas2").feature("nativeTailRad").set("kf", "tailA*tailBeta");
    model.component("outer").physics("plas2").create("nativeTailPhoto", "Reaction", 2);
    model.component("outer").physics("plas2").feature("nativeTailPhoto").set("formula", "CuT5X=>e+CuX+");
    model.component("outer").physics("plas2").feature("nativeTailPhoto").set("kf", "tailPhotoRate");
    model.component("outer").physics("plas2").feature("CuT5X").set("M", "Mcu");
    model.component("outer").physics("plas2").feature("CuT5X").set("x0", "1e-24");
    model.component("outer").variable("outerGas").set("plas2.wCuT5X", "wCuT5X");
    model.component("outer").variable("outerGas").set("nCuT5", "plas2.n_wCuT5X");
    model.component("outer").variable("outerGas").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p+nCuT5");
    model.component("outer").variable("outerGas")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p+nCuT5");
    model.component("outer").variable("outerGas")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)+1.5*k_B_const*nCuT5");
    model.component("outer").variable("outerGas")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)+e_const*tailEnergy*nCuT5");
    model.component("outer").variable("outerGas").set("tailDnu", "1/(406.2638[nm])*sqrt(2*k_B_const*Tg/(Mcu/N_A_const))");
    model.component("outer").variable("outerGas")
         .set("tailTau", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.07794434001494746/(sqrt(pi)*tailDnu)*max(nCuP3-(4/6)*nCuT5,0[1/m^3])*Req");
    model.component("outer").variable("outerGas")
         .set("tailBeta", "if(tailTau<0.01,1-3*tailTau/4+2*tailTau^2/5,3/(4*tailTau)*(1-1/(2*tailTau^2)+(1/tailTau+1/(2*tailTau^2))*exp(-2*tailTau)))");
    model.component("outer").variable("outerGas").set("tailPhoton", "h_const*c_const/lambdaL");
    model.component("outer").variable("outerGas")
         .set("tailSigma", "6.3e-22[m^2]*sqrt(13.605693[V]/tailI)*(tailI*e_const/tailPhoton)^3");
    model.component("outer").variable("outerGas")
         .set("alphaIB", "plas2.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component("outer").variable("outerGas").set("alphaPhoto", "nCuT5*tailSigma");
    model.component("outer").variable("outerGas").set("alphaLaser", "alphaIB+alphaPhoto");
    model.component("outer").variable("outerGas")
         .set("meanLaserIntensity", "Pincident*beamCoverage*Hp/Vp*if(laserTau<1e-4,1-laserTau/2+laserTau^2/6-laserTau^3/24,(1-exp(-laserTau))/laserTau)*(1+(1-Acu)*beamTransmission)");
    model.component("outer").variable("outerGas").set("tailPhotoRate", "tailSigma*meanLaserIntensity/tailPhoton");
    model.component("outer").variable("outerGas").set("tailPhotoEvents", "nCuT5*tailPhotoRate");
    model.component("outer").variable("outerGas")
         .set("tailNetExcitation", "plas2.ne*nCuP3*tailKu-plas2.ne*nCuT5*tailKd-tailA*tailBeta*nCuT5");
    model.component("outer").variable("outerGas")
         .set("tailNetIonization", "plas2.ne*nCuT5*tailKi-plas2.ne^2*nCu_1p*tailK3-plas2.ne*nCu_1p*tailKrr+tailPhotoEvents");
    model.component("outer").physics("plas2").feature("CuP3X").featureInfo("info")
         .set("plas2.R_wCuP3X", new String[]{"((plas2.r_50-plas2.r_51+plas2.r_55-plas2.r_56-plas2.r_61+plas2.r_62-plas2.r_64+plas2.r_65+plas2.r_67-plas2.r_68-plas2.r_90+plas2.r_91-plas2.r_52-plas2.r_54+plas2.r_60+plas2.r_63-plas2.r_66+plas2.r_92)+(outerIntSurf(source_CuP3)/Vp)/plas2.M_wCuP3X)-tailNetExcitation/N_A_const"});
    model.component("outer").physics("plas2").feature("CuX_1p").featureInfo("info")
         .set("plas2.R_wCuX_1p", new String[]{"((plas2.r_72-plas2.r_73+plas2.r_81-plas2.r_82+plas2.r_84-plas2.r_85+plas2.r_87-plas2.r_88+plas2.r_90-plas2.r_91+plas2.r_93-plas2.r_94+plas2.r_96-plas2.r_97-plas2.r_74-plas2.r_83-plas2.r_86-plas2.r_89-plas2.r_92-plas2.r_95-plas2.r_98-plas2.r_104-plas2.r_105)+(outerIntSurf(source_Cu_1p)/Vp)/plas2.M_wCuX_1p)+tailNetIonization/N_A_const"});
    model.component("outer").variable("outerBoundary").set("emitWeight_CuT5", "6*exp(-tailEnergy*e_const/(k_B_const*outerT))");
    model.component("outer").variable("outerBoundary")
         .set("emitPartition", "0+emitWeight_Cu+emitWeight_CuD5+emitWeight_CuD3+emitWeight_CuP1+emitWeight_CuP3+emitWeight_CuH3+emitWeight_CuH5+emitWeight_Cu_1p+emitWeight_CuT5");
    model.component("outer").variable("outerBoundary").set("emitFraction_CuT5", "emitWeight_CuT5/emitPartition");
    model.component("outer").variable("outerBoundary")
         .set("emitMeanEnergy", "0[V]+emitFraction_Cu*0.0[V]+emitFraction_CuD5*1.388948[V]+emitFraction_CuD3*1.6422256[V]+emitFraction_CuP1*3.7858977[V]+emitFraction_CuP3*3.816692[V]+emitFraction_CuH3*6.1911751[V]+emitFraction_CuH5*6.1920252[V]+emitFraction_Cu_1p*7.72638[V]+emitFraction_CuT5*tailEnergy");
    model.component("outer").variable("outerGas")
         .set("returnMeanEnergy", "0[V]+plas2.n_wCuX/max(nCuTotal,1[1/m^3])*0.0[V]+plas2.n_wCuD5X/max(nCuTotal,1[1/m^3])*1.388948[V]+plas2.n_wCuD3X/max(nCuTotal,1[1/m^3])*1.6422256[V]+plas2.n_wCuP1X/max(nCuTotal,1[1/m^3])*3.7858977[V]+plas2.n_wCuP3X/max(nCuTotal,1[1/m^3])*3.816692[V]+plas2.n_wCuH3X/max(nCuTotal,1[1/m^3])*6.1911751[V]+plas2.n_wCuH5X/max(nCuTotal,1[1/m^3])*6.1920252[V]+plas2.n_wCuX_1p/max(nCuTotal,1[1/m^3])*7.72638[V]+nCuT5/max(nCuTotal,1[1/m^3])*tailEnergy");
}
static void part8(Model model){

    model.component("outer").variable("outerBoundary")
         .set("source_CuT5", "mdotEvap*emitFraction_CuT5-mdotReturn*nCuT5/max(nCuTotal,1[1/m^3])");
    model.component("outer").physics("plas2").feature("CuT5X").featureInfo("info")
         .set("plas2.R_wCuT5X", new String[]{"(tailNetExcitation-tailNetIonization)/N_A_const+outerIntSurf(source_CuT5)/(Vp*Mcu)"});
    model.component("outer").variable("outerGas").set("tailQfb", "plas2.ne*nCu_1p*tailKrr*tailMeanRR*e_const");
    model.component("outer").variable("outerGas").set("tailQline", "tailA*tailBeta*nCuT5*tailDelta*e_const");
    model.component("outer").variable("outerGas")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+Qfb_Cu+Qfb_N+Qfb_O+Qfb_CuD5+Qfb_CuD3+Qfb_CuP1+Qfb_CuP3+Qfb_CuH3+Qfb_CuH5+Qff+tailQfb+tailQline");
    model.component("outer").variable("outerGas")
         .set("QeRR", "r_Curr*e_const*(Eph_Curr-7.72638[V])+r_Nrr*e_const*(Eph_Nrr-14.534[V])+r_Orr*e_const*(Eph_Orr-13.618[V])+r_CuD5rr*e_const*(Eph_CuD5rr-6.337432[V])+r_CuD3rr*e_const*(Eph_CuD3rr-6.0841544[V])+r_CuP1rr*e_const*(Eph_CuP1rr-3.9404822999999998[V])+r_CuP3rr*e_const*(Eph_CuP3rr-3.9096879999999996[V])+r_CuH3rr*e_const*(Eph_CuH3rr-1.5352049[V])+r_CuH5rr*e_const*(Eph_CuH5rr-1.5343548[V])+plas2.ne*nCu_1p*tailKrr*(tailMeanRR-tailI)*e_const");
    model.component("outer").physics("plas2").feature("pes1")
         .set("Pgm", "Plaser+PeSource-Vp*(QeRR+QeAttach+Qff+Qei+(Ue+pe)*volumeRate)-Vp*tailPhotoEvents*tailI*e_const");
    model.component("outer").physics("plas2").create("elasticTail", "ElectronImpactReaction", 2);
    model.component("outer").physics("plas2").feature("elasticTail").set("formula", "e+CuT5X=>e+CuT5X");
    model.component("outer").physics("plas2").feature("elasticTail").set("type", "Elastic");
    model.component("outer").physics("plas2").feature("elasticTail").set("kf", "kmCu*N_A_const");
    model.component("outer").physics("plas2").feature("elasticTail").set("mratio", "me_const*N_A_const/Mcu");
    model.component("outer").variable("outerGas")
         .set("escapeSpeedRatio", "abs((2*vRadius/Rp+vHeight/Hp)/3)*Req/sqrt(2*k_B_const*Tg/(Mcu/N_A_const))");
    model.component("outer").variable("outerGas")
         .set("beta1", "10^logGlobalEscape(log10(max(tauLine1,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component("outer").variable("outerGas")
         .set("beta2", "10^logGlobalEscape(log10(max(tauLine2,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component("outer").variable("outerGas")
         .set("beta3", "10^logGlobalEscape(log10(max(tauLine3,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component("outer").variable("outerGas")
         .set("beta4", "10^logGlobalEscape(log10(max(tauLine4,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component("outer").variable("outerGas")
         .set("beta5", "10^logGlobalEscape(log10(max(tauLine5,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component("outer").variable("outerGas")
         .set("beta6", "10^logGlobalEscape(log10(max(tauLine6,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component("outer").variable("outerGas")
         .set("beta7", "10^logGlobalEscape(log10(max(tauLine7,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component("outer").variable("outerGas")
         .set("beta8", "10^logGlobalEscape(log10(max(tauLine8,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component("outer").variable("outerGas")
         .set("tailBeta", "10^logGlobalEscape(log10(max(tailTau,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component("outer").variable("outerGas").set("plas2.wN2", "plas2.wN2X");
    model.component("outer").variable("outerGas").set("plas2.n_wN2", "plas2.n_wN2X");
    model.component("outer").variable("outerGas").set("plas2.M_wN2", "plas2.M_wN2X");
    model.component("outer").variable("outerGas").set("plas2.wO2", "plas2.wO2X");
    model.component("outer").variable("outerGas").set("plas2.n_wO2", "plas2.n_wO2X");
    model.component("outer").variable("outerGas").set("plas2.M_wO2", "plas2.M_wO2X");
    model.component("outer").variable("outerGas").set("plas2.wN", "plas2.wNX");
    model.component("outer").variable("outerGas").set("plas2.n_wN", "plas2.n_wNX");
    model.component("outer").variable("outerGas").set("plas2.M_wN", "plas2.M_wNX");
    model.component("outer").variable("outerGas").set("plas2.wO", "plas2.wOX");
    model.component("outer").variable("outerGas").set("plas2.n_wO", "plas2.n_wOX");
    model.component("outer").variable("outerGas").set("plas2.M_wO", "plas2.M_wOX");
    model.component("outer").variable("outerGas").set("plas2.wN2_1p", "plas2.wN2X_1p");
    model.component("outer").variable("outerGas").set("plas2.n_wN2_1p", "plas2.n_wN2X_1p");
    model.component("outer").variable("outerGas").set("plas2.M_wN2_1p", "plas2.M_wN2X_1p");
    model.component("outer").variable("outerGas").set("plas2.wO2_1p", "plas2.wO2X_1p");
    model.component("outer").variable("outerGas").set("plas2.n_wO2_1p", "plas2.n_wO2X_1p");
    model.component("outer").variable("outerGas").set("plas2.M_wO2_1p", "plas2.M_wO2X_1p");
    model.component("outer").variable("outerGas").set("plas2.wN_1p", "plas2.wNX_1p");
    model.component("outer").variable("outerGas").set("plas2.n_wN_1p", "plas2.n_wNX_1p");
    model.component("outer").variable("outerGas").set("plas2.M_wN_1p", "plas2.M_wNX_1p");
    model.component("outer").variable("outerGas").set("plas2.wO_1p", "plas2.wOX_1p");
    model.component("outer").variable("outerGas").set("plas2.n_wO_1p", "plas2.n_wOX_1p");
    model.component("outer").variable("outerGas").set("plas2.M_wO_1p", "plas2.M_wOX_1p");
    model.component("outer").variable("outerGas").set("plas2.wO_1m", "plas2.wOX_1m");
    model.component("outer").variable("outerGas").set("plas2.n_wO_1m", "plas2.n_wOX_1m");
    model.component("outer").variable("outerGas").set("plas2.M_wO_1m", "plas2.M_wOX_1m");
    model.component("outer").variable("outerGas").set("plas2.wO2_1m", "plas2.wO2X_1m");
    model.component("outer").variable("outerGas").set("plas2.n_wO2_1m", "plas2.n_wO2X_1m");
    model.component("outer").variable("outerGas").set("plas2.M_wO2_1m", "plas2.M_wO2X_1m");
    model.component("outer").variable("outerGas").set("plas2.wCu", "plas2.wCuX");
    model.component("outer").variable("outerGas").set("plas2.n_wCu", "plas2.n_wCuX");
    model.component("outer").variable("outerGas").set("plas2.M_wCu", "plas2.M_wCuX");
    model.component("outer").variable("outerGas").set("plas2.wCuD5", "plas2.wCuD5X");
    model.component("outer").variable("outerGas").set("plas2.n_wCuD5", "plas2.n_wCuD5X");
    model.component("outer").variable("outerGas").set("plas2.M_wCuD5", "plas2.M_wCuD5X");
    model.component("outer").variable("outerGas").set("plas2.wCuD3", "plas2.wCuD3X");
    model.component("outer").variable("outerGas").set("plas2.n_wCuD3", "plas2.n_wCuD3X");
    model.component("outer").variable("outerGas").set("plas2.M_wCuD3", "plas2.M_wCuD3X");
    model.component("outer").variable("outerGas").set("plas2.wCuP1", "plas2.wCuP1X");
    model.component("outer").variable("outerGas").set("plas2.n_wCuP1", "plas2.n_wCuP1X");
    model.component("outer").variable("outerGas").set("plas2.M_wCuP1", "plas2.M_wCuP1X");
    model.component("outer").variable("outerGas").set("plas2.wCuP3", "plas2.wCuP3X");
    model.component("outer").variable("outerGas").set("plas2.n_wCuP3", "plas2.n_wCuP3X");
    model.component("outer").variable("outerGas").set("plas2.M_wCuP3", "plas2.M_wCuP3X");
    model.component("outer").variable("outerGas").set("plas2.wCuH3", "plas2.wCuH3X");
    model.component("outer").variable("outerGas").set("plas2.n_wCuH3", "plas2.n_wCuH3X");
    model.component("outer").variable("outerGas").set("plas2.M_wCuH3", "plas2.M_wCuH3X");
    model.component("outer").variable("outerGas").set("plas2.wCuH5", "plas2.wCuH5X");
    model.component("outer").variable("outerGas").set("plas2.n_wCuH5", "plas2.n_wCuH5X");
    model.component("outer").variable("outerGas").set("plas2.M_wCuH5", "plas2.M_wCuH5X");
    model.component("outer").variable("outerGas").set("plas2.wCu_1p", "plas2.wCuX_1p");
    model.component("outer").variable("outerGas").set("plas2.n_wCu_1p", "plas2.n_wCuX_1p");
    model.component("outer").variable("outerGas").set("plas2.M_wCu_1p", "plas2.M_wCuX_1p");
    model.component("outer").variable("outerGas").set("plas2.wCuT5", "plas2.wCuT5X");
    model.component("outer").variable("outerGas").set("plas2.n_wCuT5", "plas2.n_wCuT5X");
    model.component("outer").variable("outerGas").set("plas2.M_wCuT5", "plas2.M_wCuT5X");
}
}
