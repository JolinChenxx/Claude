import com.comsol.model.*;
public class AddAxialComponent {
public static void apply(Model model,String cell,String suffix){String plasma="plasma"+suffix;
part0(model,cell,suffix,plasma);
part1(model,cell,suffix,plasma);
part2(model,cell,suffix,plasma);
part3(model,cell,suffix,plasma);
part4(model,cell,suffix,plasma);
part5(model,cell,suffix,plasma);
part6(model,cell,suffix,plasma);
part7(model,cell,suffix,plasma);
part8(model,cell,suffix,plasma);
part9(model,cell,suffix,plasma);
part10(model,cell,suffix,plasma);
part11(model,cell,suffix,plasma);
part12(model,cell,suffix,plasma);
}
static void part0(Model model,String cell,String suffix,String plasma){
    model.component().create(cell, true);
    model.component(cell).geom().create("g"+suffix+"", 2);
    model.component(cell).geom("g"+suffix+"").axisymmetric(true);
    model.component(cell).geom("g"+suffix+"").create("cu", "Rectangle");
    model.component(cell).geom("g"+suffix+"").feature("cu").set("pos", new String[]{"0", "-Dcu"});
    model.component(cell).geom("g"+suffix+"").feature("cu").set("size", new String[]{"Rdom", "Dcu"});
    model.component(cell).geom("g"+suffix+"").feature("cu").set("selresult", true);
    model.component(cell).geom("g"+suffix+"").create("gas", "Rectangle");
    model.component(cell).geom("g"+suffix+"").feature("gas").set("pos", new String[]{"0", "0"});
    model.component(cell).geom("g"+suffix+"").feature("gas").set("size", new String[]{"Rdom", "1[um]"});
    model.component(cell).geom("g"+suffix+"").feature("gas").set("selresult", true);
    model.component(cell).geom("g"+suffix+"").create("hot", "Rectangle");
    model.component(cell).geom("g"+suffix+"").feature("hot").set("pos", new String[]{"0", "-15[um]"});
    model.component(cell).geom("g"+suffix+"").feature("hot").set("size", new String[]{"40[um]", "15[um]"});
    model.component(cell).geom("g"+suffix+"").feature("hot").set("selresult", true);
    model.component(cell).geom("g"+suffix+"").run();
    model.component(cell).selection().create("surface"+suffix+"", "Box");
    model.component(cell).selection("surface"+suffix+"").set("entitydim", 1);
    model.component(cell).selection("surface"+suffix+"").set("xmin", "-1e-12");
    model.component(cell).selection("surface"+suffix+"").set("xmax", "Rdom+1e-12[m]");
    model.component(cell).selection("surface"+suffix+"").set("ymin", "-1e-12");
    model.component(cell).selection("surface"+suffix+"").set("ymax", "1e-12");
    model.component(cell).selection("surface"+suffix+"").set("condition", "inside");
    model.component(cell).selection().create("beam"+suffix+"", "Box");
    model.component(cell).selection("beam"+suffix+"").set("entitydim", 1);
    model.component(cell).selection("beam"+suffix+"").set("xmin", "-1e-12");
    model.component(cell).selection("beam"+suffix+"").set("xmax", "40[um]+1e-12[m]");
    model.component(cell).selection("beam"+suffix+"").set("ymin", "-1e-12");
    model.component(cell).selection("beam"+suffix+"").set("ymax", "1e-12");
    model.component(cell).selection("beam"+suffix+"").set("condition", "inside");
    model.component(cell).selection().create("bottom"+suffix+"", "Box");
    model.component(cell).selection("bottom"+suffix+"").set("entitydim", 1);
    model.component(cell).selection("bottom"+suffix+"").set("xmin", "-1e-12");
    model.component(cell).selection("bottom"+suffix+"").set("xmax", "Rdom+1e-12[m]");
    model.component(cell).selection("bottom"+suffix+"").set("ymin", "-Dcu-1e-12[m]");
    model.component(cell).selection("bottom"+suffix+"").set("ymax", "-Dcu+1e-12[m]");
    model.component(cell).selection("bottom"+suffix+"").set("condition", "inside");
    model.component(cell).variable().create("gasv"+suffix+"");
    model.component(cell).variable().create("boundv"+suffix+"");
    model.component(cell).variable().create("solidv"+suffix+"");
    model.component(cell).variable("boundv"+suffix+"").selection().geom("g"+suffix+"", 1);
    model.component(cell).variable("boundv"+suffix+"").selection().named("surface"+suffix+"");
    model.component(cell).variable("solidv"+suffix+"").selection().named("g"+suffix+"_cu_dom");
    model.component(cell).cpl().create("intSurf"+suffix+"", "Integration");
    model.component(cell).cpl("intSurf"+suffix+"").selection().geom("g"+suffix+"", 1);
    model.component(cell).cpl("intSurf"+suffix+"").selection().named("surface"+suffix+"");
    model.component(cell).cpl("intSurf"+suffix+"").set("axisym", true);
    model.component(cell).cpl("intSurf"+suffix+"").set("intorder", 8);
    model.component(cell).cpl().create("intCu"+suffix+"", "Integration");
    model.component(cell).cpl("intCu"+suffix+"").selection().named("g"+suffix+"_cu_dom");
    model.component(cell).cpl("intCu"+suffix+"").set("axisym", true);
    model.component(cell).cpl("intCu"+suffix+"").set("intorder", 8);
    model.component(cell).physics().create(plasma, "ColdPlasma", "g"+suffix+"");
    model.component(cell).physics(plasma).identifier(plasma);
    model.component(cell).physics(plasma).field("logelectronenergydensity").field("energy"+suffix);
    model.component(cell).physics(plasma).selection().named("g"+suffix+"_gas_dom");
    model.component(cell).physics(plasma).prop("DiffusionModel").set("DiffusionModel", "GlobalModel");
    model.component(cell).physics(plasma).prop("Reactor").set("ReactorType", "ClosedReactor");
    model.component(cell).physics(plasma).prop("Stabilization").set("SourceStabilization", false);
    model.component(cell).physics(plasma).prop("Stabilization").set("ReactionSourceStabilization", false);
    model.component(cell).physics(plasma).create("N2_0", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_0").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_0").set("kf", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_0").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("N2_0").set("mratio", "1.95e-05");
    model.component(cell).physics(plasma).create("N2_1", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_1").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_1").set("kf", "10^lr_N2_1(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_1").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_1").set("de", "0.02");
    model.component(cell).physics(plasma).create("N2_2", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_2").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_2").set("kf", "10^lr_N2_2(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_2").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_2").set("de", "0.29");
    model.component(cell).physics(plasma).create("N2_3", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_3").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_3").set("kf", "10^lr_N2_3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_3").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_3").set("de", "0.29");
    model.component(cell).physics(plasma).create("N2_4", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_4").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_4").set("kf", "10^lr_N2_4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_4").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_4").set("de", "0.59");
    model.component(cell).physics(plasma).create("N2_5", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_5").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_5").set("kf", "10^lr_N2_5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_5").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_5").set("de", "0.88");
    model.component(cell).physics(plasma).create("N2_6", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_6").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_6").set("kf", "10^lr_N2_6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_6").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_6").set("de", "1.17");
    model.component(cell).physics(plasma).create("N2_7", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_7").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_7").set("kf", "10^lr_N2_7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_7").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_7").set("de", "1.47");
    model.component(cell).physics(plasma).create("N2_8", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_8").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_8").set("kf", "10^lr_N2_8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_8").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_8").set("de", "1.76");
    model.component(cell).physics(plasma).create("N2_9", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_9").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_9").set("kf", "10^lr_N2_9(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_9").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_9").set("de", "2.06");
    model.component(cell).physics(plasma).create("N2_10", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_10").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_10")
         .set("kf", "10^lr_N2_10(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_10").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_10").set("de", "2.35");
    model.component(cell).physics(plasma).create("N2_11", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_11").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_11")
         .set("kf", "10^lr_N2_11(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_11").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_11").set("de", "6.17");
    model.component(cell).physics(plasma).create("N2_12", "ElectronImpactReaction", 2);
}
static void part1(Model model,String cell,String suffix,String plasma){

    model.component(cell).physics(plasma).feature("N2_12").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_12")
         .set("kf", "10^lr_N2_12(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_12").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_12").set("de", "7.0");
    model.component(cell).physics(plasma).create("N2_13", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_13").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_13")
         .set("kf", "10^lr_N2_13(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_13").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_13").set("de", "7.35");
    model.component(cell).physics(plasma).create("N2_14", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_14").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_14")
         .set("kf", "10^lr_N2_14(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_14").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_14").set("de", "7.36");
    model.component(cell).physics(plasma).create("N2_15", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_15").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_15")
         .set("kf", "10^lr_N2_15(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_15").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_15").set("de", "7.8");
    model.component(cell).physics(plasma).create("N2_16", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_16").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_16")
         .set("kf", "10^lr_N2_16(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_16").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_16").set("de", "8.16");
    model.component(cell).physics(plasma).create("N2_17", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_17").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_17")
         .set("kf", "10^lr_N2_17(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_17").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_17").set("de", "8.4");
    model.component(cell).physics(plasma).create("N2_18", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_18").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_18")
         .set("kf", "10^lr_N2_18(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_18").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_18").set("de", "8.55");
    model.component(cell).physics(plasma).create("N2_19", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_19").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_19")
         .set("kf", "10^lr_N2_19(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_19").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_19").set("de", "8.89");
    model.component(cell).physics(plasma).create("N2_20", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_20").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_20")
         .set("kf", "10^lr_N2_20(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_20").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_20").set("de", "11.03");
    model.component(cell).physics(plasma).create("N2_21", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_21").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_21")
         .set("kf", "10^lr_N2_21(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_21").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_21").set("de", "11.87");
    model.component(cell).physics(plasma).create("N2_22", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_22").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_22")
         .set("kf", "10^lr_N2_22(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_22").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_22").set("de", "12.25");
    model.component(cell).physics(plasma).create("N2_23", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_23").set("formula", "e+N2"+suffix+"=>e+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2_23")
         .set("kf", "10^lr_N2_23(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_23").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N2_23").set("de", "13.0");
    model.component(cell).physics(plasma).create("N2_24", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_24").set("formula", "e+N2"+suffix+"=>2e+N2"+suffix+"+");
    model.component(cell).physics(plasma).feature("N2_24")
         .set("kf", "10^lr_N2_24(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_24").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("N2_24").set("de", "15.6");
    model.component(cell).physics(plasma).create("N2_25", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N2_25").set("formula", "e+N2"+suffix+"=>2e+N2"+suffix+"+");
    model.component(cell).physics(plasma).feature("N2_25")
         .set("kf", "10^lr_N2_25(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("N2_25").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("N2_25").set("de", "18.8");
    model.component(cell).physics(plasma).create("O2_0", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_0").set("formula", "e+O2"+suffix+"=>O"+suffix+"+O"+suffix+"-");
    model.component(cell).physics(plasma).feature("O2_0").set("kf", "10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_0").set("type", "Attachment");
    model.component(cell).physics(plasma).feature("O2_0").set("de", "0");
    model.component(cell).physics(plasma).create("O2_1", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_1").set("formula", "e+O2"+suffix+"=>O"+suffix+"+O"+suffix+"-");
    model.component(cell).physics(plasma).feature("O2_1").set("kf", "10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_1").set("type", "Attachment");
    model.component(cell).physics(plasma).feature("O2_1").set("de", "0");
    model.component(cell).physics(plasma).create("O2_2", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_2").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_2").set("kf", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_2").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("O2_2").set("mratio", "1.71e-05");
    model.component(cell).physics(plasma).create("O2_3", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_3").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_3").set("kf", "10^lr_O2_3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_3").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_3").set("de", "0.02");
    model.component(cell).physics(plasma).create("O2_4", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_4").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_4").set("kf", "10^lr_O2_4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_4").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_4").set("de", "0.19");
    model.component(cell).physics(plasma).create("O2_5", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_5").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_5").set("kf", "10^lr_O2_5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_5").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_5").set("de", "0.19");
    model.component(cell).physics(plasma).create("O2_6", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_6").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_6").set("kf", "10^lr_O2_6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_6").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_6").set("de", "0.38");
    model.component(cell).physics(plasma).create("O2_7", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_7").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_7").set("kf", "10^lr_O2_7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_7").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_7").set("de", "0.38");
    model.component(cell).physics(plasma).create("O2_8", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_8").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_8").set("kf", "10^lr_O2_8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_8").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_8").set("de", "0.57");
    model.component(cell).physics(plasma).create("O2_9", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_9").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_9").set("kf", "10^lr_O2_9(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_9").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_9").set("de", "0.75");
    model.component(cell).physics(plasma).create("O2_10", "ElectronImpactReaction", 2);
}
static void part2(Model model,String cell,String suffix,String plasma){

    model.component(cell).physics(plasma).feature("O2_10").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_10")
         .set("kf", "10^lr_O2_10(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_10").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_10").set("de", "0.977");
    model.component(cell).physics(plasma).create("O2_11", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_11").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_11")
         .set("kf", "10^lr_O2_11(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_11").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_11").set("de", "1.627");
    model.component(cell).physics(plasma).create("O2_12", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_12").set("formula", "e+O2"+suffix+"=>e+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_12")
         .set("kf", "10^lr_O2_12(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_12").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_12").set("de", "4.5");
    model.component(cell).physics(plasma).create("O2_13", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_13").set("formula", "e+O2"+suffix+"=>e+2O"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_13")
         .set("kf", "10^lr_O2_13(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_13").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_13").set("de", "6.0");
    model.component(cell).physics(plasma).create("O2_14", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_14").set("formula", "e+O2"+suffix+"=>e+2O"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_14")
         .set("kf", "10^lr_O2_14(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_14").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_14").set("de", "8.4");
    model.component(cell).physics(plasma).create("O2_15", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_15").set("formula", "e+O2"+suffix+"=>e+2O"+suffix+"");
    model.component(cell).physics(plasma).feature("O2_15")
         .set("kf", "10^lr_O2_15(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_15").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O2_15").set("de", "9.97");
    model.component(cell).physics(plasma).create("O2_16", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O2_16").set("formula", "e+O2"+suffix+"=>2e+O2"+suffix+"+");
    model.component(cell).physics(plasma).feature("O2_16")
         .set("kf", "10^lr_O2_16(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("O2_16").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("O2_16").set("de", "12.06");
    model.component(cell).physics(plasma).create("CuD5up", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuD5up").set("formula", "e+Cu"+suffix+"=>e+CuD5"+suffix+"");
    model.component(cell).physics(plasma).feature("CuD5up")
         .set("kf", "10^lr_CuD5_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuD5up").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuD5up").set("de", "1.388948");
    model.component(cell).physics(plasma).create("CuD5down", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuD5down").set("formula", "e+CuD5"+suffix+"=>e+Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("CuD5down")
         .set("kf", "10^lr_CuD5_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuD5down").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuD5down").set("de", "-1.388948");
    model.component(cell).physics(plasma).create("CuD3up", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuD3up").set("formula", "e+Cu"+suffix+"=>e+CuD3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuD3up")
         .set("kf", "10^lr_CuD3_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuD3up").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuD3up").set("de", "1.6422256");
    model.component(cell).physics(plasma).create("CuD3down", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuD3down").set("formula", "e+CuD3"+suffix+"=>e+Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("CuD3down")
         .set("kf", "10^lr_CuD3_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuD3down").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuD3down").set("de", "-1.6422256");
    model.component(cell).physics(plasma).create("CuP1up", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuP1up").set("formula", "e+Cu"+suffix+"=>e+CuP1"+suffix+"");
    model.component(cell).physics(plasma).feature("CuP1up")
         .set("kf", "10^lr_CuP1_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuP1up").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuP1up").set("de", "3.7858977");
    model.component(cell).physics(plasma).create("CuP1down", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuP1down").set("formula", "e+CuP1"+suffix+"=>e+Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("CuP1down")
         .set("kf", "10^lr_CuP1_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuP1down").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuP1down").set("de", "-3.7858977");
    model.component(cell).physics(plasma).create("CuP3up", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuP3up").set("formula", "e+Cu"+suffix+"=>e+CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuP3up")
         .set("kf", "10^lr_CuP3_up(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuP3up").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuP3up").set("de", "3.816692");
    model.component(cell).physics(plasma).create("CuP3down", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuP3down").set("formula", "e+CuP3"+suffix+"=>e+Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("CuP3down")
         .set("kf", "10^lr_CuP3_down(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuP3down").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuP3down").set("de", "-3.816692");
    model.component(cell).physics(plasma).create("rad1", "Reaction", 2);
    model.component(cell).physics(plasma).feature("rad1").set("formula", "CuP3"+suffix+"=>Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("rad1").set("kf", "139500000.0[1/s]*beta1");
    model.component(cell).physics(plasma).create("rad2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("rad2").set("formula", "CuP1"+suffix+"=>Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("rad2").set("kf", "137600000.0[1/s]*beta2");
    model.component(cell).physics(plasma).create("rad3", "Reaction", 2);
    model.component(cell).physics(plasma).feature("rad3").set("formula", "CuP3"+suffix+"=>CuD5"+suffix+"");
    model.component(cell).physics(plasma).feature("rad3").set("kf", "2000000.0[1/s]*beta3");
    model.component(cell).physics(plasma).create("lineup3", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("lineup3").set("formula", "e+CuD5"+suffix+"=>e+CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("lineup3")
         .set("kf", "10^lr_lineup3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("lineup3").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("lineup3").set("de", "2.427744");
    model.component(cell).physics(plasma).create("linedn3", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("linedn3").set("formula", "e+CuP3"+suffix+"=>e+CuD5"+suffix+"");
    model.component(cell).physics(plasma).feature("linedn3")
         .set("kf", "10^lr_linedn3(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("linedn3").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("linedn3").set("de", "-2.427744");
    model.component(cell).physics(plasma).create("rad4", "Reaction", 2);
    model.component(cell).physics(plasma).feature("rad4").set("formula", "CuH3"+suffix+"=>CuP1"+suffix+"");
    model.component(cell).physics(plasma).feature("rad4").set("kf", "60000000.0[1/s]*beta4");
    model.component(cell).physics(plasma).create("lineup4", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("lineup4").set("formula", "e+CuP1"+suffix+"=>e+CuH3"+suffix+"");
    model.component(cell).physics(plasma).feature("lineup4")
         .set("kf", "10^lr_lineup4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("lineup4").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("lineup4").set("de", "2.4052773999999997");
    model.component(cell).physics(plasma).create("linedn4", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("linedn4").set("formula", "e+CuH3"+suffix+"=>e+CuP1"+suffix+"");
    model.component(cell).physics(plasma).feature("linedn4")
         .set("kf", "10^lr_linedn4(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("linedn4").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("linedn4").set("de", "-2.4052773999999997");
    model.component(cell).physics(plasma).create("rad5", "Reaction", 2);
    model.component(cell).physics(plasma).feature("rad5").set("formula", "CuH5"+suffix+"=>CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("rad5").set("kf", "75000000.0[1/s]*beta5");
    model.component(cell).physics(plasma).create("lineup5", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("lineup5").set("formula", "e+CuP3"+suffix+"=>e+CuH5"+suffix+"");
    model.component(cell).physics(plasma).feature("lineup5")
         .set("kf", "10^lr_lineup5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("lineup5").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("lineup5").set("de", "2.3753331999999996");
    model.component(cell).physics(plasma).create("linedn5", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("linedn5").set("formula", "e+CuH5"+suffix+"=>e+CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("linedn5")
         .set("kf", "10^lr_linedn5(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("linedn5").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("linedn5").set("de", "-2.3753331999999996");
    model.component(cell).physics(plasma).create("rad6", "Reaction", 2);
}
static void part3(Model model,String cell,String suffix,String plasma){

    model.component(cell).physics(plasma).feature("rad6").set("formula", "CuH3"+suffix+"=>CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("rad6").set("kf", "15000000.0[1/s]*beta6");
    model.component(cell).physics(plasma).create("lineup6", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("lineup6").set("formula", "e+CuP3"+suffix+"=>e+CuH3"+suffix+"");
    model.component(cell).physics(plasma).feature("lineup6")
         .set("kf", "10^lr_lineup6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("lineup6").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("lineup6").set("de", "2.3744830999999995");
    model.component(cell).physics(plasma).create("linedn6", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("linedn6").set("formula", "e+CuH3"+suffix+"=>e+CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("linedn6")
         .set("kf", "10^lr_linedn6(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("linedn6").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("linedn6").set("de", "-2.3744830999999995");
    model.component(cell).physics(plasma).create("rad7", "Reaction", 2);
    model.component(cell).physics(plasma).feature("rad7").set("formula", "CuP3"+suffix+"=>CuD3"+suffix+"");
    model.component(cell).physics(plasma).feature("rad7").set("kf", "240000.0[1/s]*beta7");
    model.component(cell).physics(plasma).create("lineup7", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("lineup7").set("formula", "e+CuD3"+suffix+"=>e+CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("lineup7")
         .set("kf", "10^lr_lineup7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("lineup7").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("lineup7").set("de", "2.1744664");
    model.component(cell).physics(plasma).create("linedn7", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("linedn7").set("formula", "e+CuP3"+suffix+"=>e+CuD3"+suffix+"");
    model.component(cell).physics(plasma).feature("linedn7")
         .set("kf", "10^lr_linedn7(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("linedn7").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("linedn7").set("de", "-2.1744664");
    model.component(cell).physics(plasma).create("rad8", "Reaction", 2);
    model.component(cell).physics(plasma).feature("rad8").set("formula", "CuP1"+suffix+"=>CuD3"+suffix+"");
    model.component(cell).physics(plasma).feature("rad8").set("kf", "1650000.0[1/s]*beta8");
    model.component(cell).physics(plasma).create("lineup8", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("lineup8").set("formula", "e+CuD3"+suffix+"=>e+CuP1"+suffix+"");
    model.component(cell).physics(plasma).feature("lineup8")
         .set("kf", "10^lr_lineup8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("lineup8").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("lineup8").set("de", "2.1436721");
    model.component(cell).physics(plasma).create("linedn8", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("linedn8").set("formula", "e+CuP1"+suffix+"=>e+CuD3"+suffix+"");
    model.component(cell).physics(plasma).feature("linedn8")
         .set("kf", "10^lr_linedn8(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("linedn8").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("linedn8").set("de", "-2.1436721");
    model.component(cell).physics(plasma).create("Cuion", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("Cuion").set("formula", "e+Cu"+suffix+"=>2e+Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("Cuion")
         .set("kf", "10^lr_Cu_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("Cuion").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("Cuion").set("de", "7.72638");
    model.component(cell).physics(plasma).create("Cu3rec", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("Cu3rec").set("formula", "2e+Cu"+suffix+"+=>e+Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("Cu3rec")
         .set("kf", "10^lr_Cu_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component(cell).physics(plasma).feature("Cu3rec").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("Cu3rec").set("de", "-7.72638");
    model.component(cell).physics(plasma).create("Curr", "Reaction", 2);
    model.component(cell).physics(plasma).feature("Curr").set("formula", "e+Cu"+suffix+"+=>Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("Curr")
         .set("kf", "10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("Nion", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("Nion").set("formula", "e+N"+suffix+"=>2e+N"+suffix+"+");
    model.component(cell).physics(plasma).feature("Nion")
         .set("kf", "10^lr_N_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("Nion").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("Nion").set("de", "14.534");
    model.component(cell).physics(plasma).create("N3rec", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("N3rec").set("formula", "2e+N"+suffix+"+=>e+N"+suffix+"");
    model.component(cell).physics(plasma).feature("N3rec")
         .set("kf", "10^lr_N_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component(cell).physics(plasma).feature("N3rec").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("N3rec").set("de", "-14.534");
    model.component(cell).physics(plasma).create("Nrr", "Reaction", 2);
    model.component(cell).physics(plasma).feature("Nrr").set("formula", "e+N"+suffix+"+=>N"+suffix+"");
    model.component(cell).physics(plasma).feature("Nrr").set("kf", "10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("Oion", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("Oion").set("formula", "e+O"+suffix+"=>2e+O"+suffix+"+");
    model.component(cell).physics(plasma).feature("Oion")
         .set("kf", "10^lr_O_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("Oion").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("Oion").set("de", "13.618");
    model.component(cell).physics(plasma).create("O3rec", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("O3rec").set("formula", "2e+O"+suffix+"+=>e+O"+suffix+"");
    model.component(cell).physics(plasma).feature("O3rec")
         .set("kf", "10^lr_O_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component(cell).physics(plasma).feature("O3rec").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("O3rec").set("de", "-13.618");
    model.component(cell).physics(plasma).create("Orr", "Reaction", 2);
    model.component(cell).physics(plasma).feature("Orr").set("formula", "e+O"+suffix+"+=>O"+suffix+"");
    model.component(cell).physics(plasma).feature("Orr").set("kf", "10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("CuD5ion", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuD5ion").set("formula", "e+CuD5"+suffix+"=>2e+Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("CuD5ion")
         .set("kf", "10^lr_CuD5_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuD5ion").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("CuD5ion").set("de", "6.337432");
    model.component(cell).physics(plasma).create("CuD53rec", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuD53rec").set("formula", "2e+Cu"+suffix+"+=>e+CuD5"+suffix+"");
    model.component(cell).physics(plasma).feature("CuD53rec")
         .set("kf", "10^lr_CuD5_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component(cell).physics(plasma).feature("CuD53rec").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuD53rec").set("de", "-6.337432");
    model.component(cell).physics(plasma).create("CuD5rr", "Reaction", 2);
    model.component(cell).physics(plasma).feature("CuD5rr").set("formula", "e+Cu"+suffix+"+=>CuD5"+suffix+"");
    model.component(cell).physics(plasma).feature("CuD5rr")
         .set("kf", "10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("CuD3ion", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuD3ion").set("formula", "e+CuD3"+suffix+"=>2e+Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("CuD3ion")
         .set("kf", "10^lr_CuD3_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuD3ion").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("CuD3ion").set("de", "6.0841544");
    model.component(cell).physics(plasma).create("CuD33rec", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuD33rec").set("formula", "2e+Cu"+suffix+"+=>e+CuD3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuD33rec")
         .set("kf", "10^lr_CuD3_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component(cell).physics(plasma).feature("CuD33rec").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuD33rec").set("de", "-6.0841544");
    model.component(cell).physics(plasma).create("CuD3rr", "Reaction", 2);
    model.component(cell).physics(plasma).feature("CuD3rr").set("formula", "e+Cu"+suffix+"+=>CuD3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuD3rr")
         .set("kf", "10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("CuP1ion", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuP1ion").set("formula", "e+CuP1"+suffix+"=>2e+Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("CuP1ion")
         .set("kf", "10^lr_CuP1_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuP1ion").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("CuP1ion").set("de", "3.9404822999999998");
    model.component(cell).physics(plasma).create("CuP13rec", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuP13rec").set("formula", "2e+Cu"+suffix+"+=>e+CuP1"+suffix+"");
    model.component(cell).physics(plasma).feature("CuP13rec")
         .set("kf", "10^lr_CuP1_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component(cell).physics(plasma).feature("CuP13rec").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuP13rec").set("de", "-3.9404822999999998");
    model.component(cell).physics(plasma).create("CuP1rr", "Reaction", 2);
    model.component(cell).physics(plasma).feature("CuP1rr").set("formula", "e+Cu"+suffix+"+=>CuP1"+suffix+"");
    model.component(cell).physics(plasma).feature("CuP1rr")
         .set("kf", "10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("CuP3ion", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuP3ion").set("formula", "e+CuP3"+suffix+"=>2e+Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("CuP3ion")
         .set("kf", "10^lr_CuP3_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuP3ion").set("type", "Ionization");
}
static void part4(Model model,String cell,String suffix,String plasma){

    model.component(cell).physics(plasma).feature("CuP3ion").set("de", "3.9096879999999996");
    model.component(cell).physics(plasma).create("CuP33rec", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuP33rec").set("formula", "2e+Cu"+suffix+"+=>e+CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuP33rec")
         .set("kf", "10^lr_CuP3_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component(cell).physics(plasma).feature("CuP33rec").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuP33rec").set("de", "-3.9096879999999996");
    model.component(cell).physics(plasma).create("CuP3rr", "Reaction", 2);
    model.component(cell).physics(plasma).feature("CuP3rr").set("formula", "e+Cu"+suffix+"+=>CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuP3rr")
         .set("kf", "10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("CuH3ion", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuH3ion").set("formula", "e+CuH3"+suffix+"=>2e+Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("CuH3ion")
         .set("kf", "10^lr_CuH3_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuH3ion").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("CuH3ion").set("de", "1.5352049");
    model.component(cell).physics(plasma).create("CuH33rec", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuH33rec").set("formula", "2e+Cu"+suffix+"+=>e+CuH3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuH33rec")
         .set("kf", "10^lr_CuH3_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component(cell).physics(plasma).feature("CuH33rec").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuH33rec").set("de", "-1.5352049");
    model.component(cell).physics(plasma).create("CuH3rr", "Reaction", 2);
    model.component(cell).physics(plasma).feature("CuH3rr").set("formula", "e+Cu"+suffix+"+=>CuH3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuH3rr")
         .set("kf", "10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("CuH5ion", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuH5ion").set("formula", "e+CuH5"+suffix+"=>2e+Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("CuH5ion")
         .set("kf", "10^lr_CuH5_ion(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).feature("CuH5ion").set("type", "Ionization");
    model.component(cell).physics(plasma).feature("CuH5ion").set("de", "1.5343548");
    model.component(cell).physics(plasma).create("CuH53rec", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("CuH53rec").set("formula", "2e+Cu"+suffix+"+=>e+CuH5"+suffix+"");
    model.component(cell).physics(plasma).feature("CuH53rec")
         .set("kf", "10^lr_CuH5_3rec(log10(TeV/1[V]))*1[m^6/s]*N_A_const^2");
    model.component(cell).physics(plasma).feature("CuH53rec").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("CuH53rec").set("de", "-1.5343548");
    model.component(cell).physics(plasma).create("CuH5rr", "Reaction", 2);
    model.component(cell).physics(plasma).feature("CuH5rr").set("formula", "e+Cu"+suffix+"+=>CuH5"+suffix+"");
    model.component(cell).physics(plasma).feature("CuH5rr")
         .set("kf", "10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("recN2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("recN2").set("formula", "e+N2"+suffix+"+=>2N"+suffix+"");
    model.component(cell).physics(plasma).feature("recN2").set("kf", "2.8e-13[m^3/s]*(300[K]/TeK)^0.5*N_A_const");
    model.component(cell).physics(plasma).create("recO2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("recO2").set("formula", "e+O2"+suffix+"+=>2O"+suffix+"");
    model.component(cell).physics(plasma).feature("recO2").set("kf", "2e-13[m^3/s]*(300[K]/TeK)^0.7*N_A_const");
    model.component(cell).physics(plasma).create("att3", "Reaction", 2);
    model.component(cell).physics(plasma).feature("att3").set("formula", "e+O2"+suffix+"+O2"+suffix+"=>O2"+suffix+"-+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("att3")
         .set("kf", "1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg))*N_A_const^2");
    model.component(cell).physics(plasma).create("detach", "Reaction", 2);
    model.component(cell).physics(plasma).feature("detach").set("formula", "O"+suffix+"-+O"+suffix+"=>O2"+suffix+"+e");
    model.component(cell).physics(plasma).feature("detach").set("kf", "3e-16[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("detach2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("detach2").set("formula", "O2"+suffix+"-+O"+suffix+"=>O2"+suffix+"+O"+suffix+"+e");
    model.component(cell).physics(plasma).feature("detach2").set("kf", "3.3e-16[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("neutral103", "Reaction", 2);
    model.component(cell).physics(plasma).feature("neutral103").set("formula", "Cu"+suffix+"++O"+suffix+"-=>Cu"+suffix+"+O"+suffix+"");
    model.component(cell).physics(plasma).feature("neutral103").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("neutral104", "Reaction", 2);
    model.component(cell).physics(plasma).feature("neutral104").set("formula", "Cu"+suffix+"++O2"+suffix+"-=>Cu"+suffix+"+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("neutral104").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("neutral105", "Reaction", 2);
    model.component(cell).physics(plasma).feature("neutral105").set("formula", "N2"+suffix+"++O"+suffix+"-=>N2"+suffix+"+O"+suffix+"");
    model.component(cell).physics(plasma).feature("neutral105").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("neutral106", "Reaction", 2);
    model.component(cell).physics(plasma).feature("neutral106").set("formula", "N2"+suffix+"++O2"+suffix+"-=>N2"+suffix+"+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("neutral106").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("neutral107", "Reaction", 2);
    model.component(cell).physics(plasma).feature("neutral107").set("formula", "O2"+suffix+"++O"+suffix+"-=>O2"+suffix+"+O"+suffix+"");
    model.component(cell).physics(plasma).feature("neutral107").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("neutral108", "Reaction", 2);
    model.component(cell).physics(plasma).feature("neutral108").set("formula", "O2"+suffix+"++O2"+suffix+"-=>O2"+suffix+"+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("neutral108").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("neutral109", "Reaction", 2);
    model.component(cell).physics(plasma).feature("neutral109").set("formula", "N"+suffix+"++O"+suffix+"-=>N"+suffix+"+O"+suffix+"");
    model.component(cell).physics(plasma).feature("neutral109").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("neutral110", "Reaction", 2);
    model.component(cell).physics(plasma).feature("neutral110").set("formula", "N"+suffix+"++O2"+suffix+"-=>N"+suffix+"+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("neutral110").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("neutral111", "Reaction", 2);
    model.component(cell).physics(plasma).feature("neutral111").set("formula", "O"+suffix+"++O"+suffix+"-=>O"+suffix+"+O"+suffix+"");
    model.component(cell).physics(plasma).feature("neutral111").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("neutral112", "Reaction", 2);
    model.component(cell).physics(plasma).feature("neutral112").set("formula", "O"+suffix+"++O2"+suffix+"-=>O"+suffix+"+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("neutral112").set("kf", "1.5e-13[m^3/s]*N_A_const");
    model.component(cell).physics(plasma).create("N2dN2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("N2dN2").set("formula", "N2"+suffix+"+N2"+suffix+"=>2N"+suffix+"+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2dN2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp(-113200[K]/Tg)/N_A_const*N_A_const");
    model.component(cell).physics(plasma).create("N2rN2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("N2rN2").set("formula", "2N"+suffix+"+N2"+suffix+"=>N2"+suffix+"+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2rN2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp((-113200[K]+9.76[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.162933627609196e-26[kg]*k_B_const*Tg/h_const^2)^1.5*16/1/(Tg/(2*2.88[K]))*(1-exp(-3390[K]/Tg)))*N_A_const^2");
    model.component(cell).physics(plasma).create("N2dO2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("N2dO2").set("formula", "N2"+suffix+"+O2"+suffix+"=>2N"+suffix+"+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2dO2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp(-113200[K]/Tg)/N_A_const*N_A_const");
    model.component(cell).physics(plasma).create("N2rO2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("N2rO2").set("formula", "2N"+suffix+"+O2"+suffix+"=>N2"+suffix+"+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("N2rO2")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp((-113200[K]+9.76[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.162933627609196e-26[kg]*k_B_const*Tg/h_const^2)^1.5*16/1/(Tg/(2*2.88[K]))*(1-exp(-3390[K]/Tg)))*N_A_const^2");
    model.component(cell).physics(plasma).create("N2dCu", "Reaction", 2);
    model.component(cell).physics(plasma).feature("N2dCu").set("formula", "N2"+suffix+"+Cu"+suffix+"=>2N"+suffix+"+Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("N2dCu")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp(-113200[K]/Tg)/N_A_const*N_A_const");
    model.component(cell).physics(plasma).create("N2rCu", "Reaction", 2);
    model.component(cell).physics(plasma).feature("N2rCu").set("formula", "2N"+suffix+"+Cu"+suffix+"=>N2"+suffix+"+Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("N2rCu")
         .set("kf", "7000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.6)*exp((-113200[K]+9.76[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.162933627609196e-26[kg]*k_B_const*Tg/h_const^2)^1.5*16/1/(Tg/(2*2.88[K]))*(1-exp(-3390[K]/Tg)))*N_A_const^2");
    model.component(cell).physics(plasma).create("O2dN2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("O2dN2").set("formula", "O2"+suffix+"+N2"+suffix+"=>2O"+suffix+"+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2dN2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp(-59500[K]/Tg)/N_A_const*N_A_const");
    model.component(cell).physics(plasma).create("O2rN2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("O2rN2").set("formula", "2O"+suffix+"+N2"+suffix+"=>O2"+suffix+"+N2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2rN2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp((-59500[K]+5.16[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.3283814375670623e-26[kg]*k_B_const*Tg/h_const^2)^1.5*81/3/(Tg/(2*2.07[K]))*(1-exp(-2270[K]/Tg)))*N_A_const^2");
    model.component(cell).physics(plasma).create("O2dO2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("O2dO2").set("formula", "O2"+suffix+"+O2"+suffix+"=>2O"+suffix+"+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2dO2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp(-59500[K]/Tg)/N_A_const*N_A_const");
    model.component(cell).physics(plasma).create("O2rO2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("O2rO2").set("formula", "2O"+suffix+"+O2"+suffix+"=>O2"+suffix+"+O2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2rO2")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp((-59500[K]+5.16[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.3283814375670623e-26[kg]*k_B_const*Tg/h_const^2)^1.5*81/3/(Tg/(2*2.07[K]))*(1-exp(-2270[K]/Tg)))*N_A_const^2");
    model.component(cell).physics(plasma).create("O2dCu", "Reaction", 2);
    model.component(cell).physics(plasma).feature("O2dCu").set("formula", "O2"+suffix+"+Cu"+suffix+"=>2O"+suffix+"+Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("O2dCu")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp(-59500[K]/Tg)/N_A_const*N_A_const");
    model.component(cell).physics(plasma).create("O2rCu", "Reaction", 2);
    model.component(cell).physics(plasma).feature("O2rCu").set("formula", "2O"+suffix+"+Cu"+suffix+"=>O2"+suffix+"+Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("O2rCu")
         .set("kf", "2000000000000000.0[m^3/(mol*s)]*(Tg/1[K])^(-1.5)*exp((-59500[K]+5.16[eV]/k_B_const)/Tg)/N_A_const/((2*pi*1.3283814375670623e-26[kg]*k_B_const*Tg/h_const^2)^1.5*81/3/(Tg/(2*2.07[K]))*(1-exp(-2270[K]/Tg)))*N_A_const^2");
    model.component(cell).physics(plasma).feature("N2"+suffix+"").set("PresetSpeciesData", "N2");
    model.component(cell).physics(plasma).feature("N2"+suffix+"").set("M", "0.0280134[kg/mol]");
    model.component(cell).physics(plasma).feature("N2"+suffix+"").set("x0", ".79");
    model.component(cell).physics(plasma).feature("N2"+suffix+"").set("Dfs", "Dgas");
}
static void part5(Model model,String cell,String suffix,String plasma){

    model.component(cell).physics(plasma).feature("O2"+suffix+"").set("PresetSpeciesData", "O2");
    model.component(cell).physics(plasma).feature("O2"+suffix+"").set("M", "0.0319988[kg/mol]");
    model.component(cell).physics(plasma).feature("O2"+suffix+"").set("x0", ".21");
    model.component(cell).physics(plasma).feature("O2"+suffix+"").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("N"+suffix+"").set("PresetSpeciesData", "N");
    model.component(cell).physics(plasma).feature("N"+suffix+"").set("M", "0.0140067[kg/mol]");
    model.component(cell).physics(plasma).feature("N"+suffix+"").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("N"+suffix+"").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("O"+suffix+"").set("PresetSpeciesData", "O");
    model.component(cell).physics(plasma).feature("O"+suffix+"").set("M", "0.0159994[kg/mol]");
    model.component(cell).physics(plasma).feature("O"+suffix+"").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("O"+suffix+"").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").set("PresetSpeciesData", "N2");
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").set("M", "0.0280134[kg/mol]");
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p").set("PresetSpeciesData", "O2");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p").set("M", "0.0319988[kg/mol]");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p").set("PresetSpeciesData", "N");
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p").set("M", "0.0140067[kg/mol]");
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p").set("PresetSpeciesData", "O");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p").set("M", "0.0159994[kg/mol]");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m").set("PresetSpeciesData", "O");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m").set("M", "0.0159994[kg/mol]");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m").set("PresetSpeciesData", "O2");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m").set("M", "0.0319988[kg/mol]");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").set("PresetSpeciesData", "UserDefined");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").set("M", "0.063546[kg/mol]");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").set("sigma", "3[angstrom]");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").set("epsilonkb", "100[K]");
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").set("PresetSpeciesData", "UserDefined");
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").set("M", "0.063546[kg/mol]");
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").set("sigma", "3[angstrom]");
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").set("epsilonkb", "100[K]");
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").set("PresetSpeciesData", "UserDefined");
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").set("M", "0.063546[kg/mol]");
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").set("sigma", "3[angstrom]");
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").set("epsilonkb", "100[K]");
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").set("PresetSpeciesData", "UserDefined");
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").set("M", "0.063546[kg/mol]");
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").set("sigma", "3[angstrom]");
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").set("epsilonkb", "100[K]");
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").set("PresetSpeciesData", "UserDefined");
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").set("M", "0.063546[kg/mol]");
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").set("sigma", "3[angstrom]");
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").set("epsilonkb", "100[K]");
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").set("PresetSpeciesData", "UserDefined");
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").set("M", "0.063546[kg/mol]");
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").set("sigma", "3[angstrom]");
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").set("epsilonkb", "100[K]");
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").set("PresetSpeciesData", "UserDefined");
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").set("M", "0.063546[kg/mol]");
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").set("sigma", "3[angstrom]");
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").set("epsilonkb", "100[K]");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").set("PresetSpeciesData", "UserDefined");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").set("M", "0.063546[kg/mol]");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").set("x0", "1e-24");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").set("Dfs", "Dgas");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p")
         .set("MobilityDiffusivitySpecification", "SpecifyMobilityComputeDiffusivity");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").set("MobilitySpecification", "SpecifyConstantMobility");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").set("um", "e_const*Dgas/(k_B_const*Tg)");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").set("sigma", "3[angstrom]");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").set("epsilonkb", "100[K]");
    model.component(cell).physics(plasma).feature("N2"+suffix+"").set("FromMassConstraint", true);
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").set("InitIon", true);
    model.component(cell).variable("gasv"+suffix+"").set("rhoP", "rho0*qR");
    model.component(cell).variable("gasv"+suffix+"").set("rhoSafe", "max(rhoP,1e-8[kg/m^3])");
    model.component(cell).variable("gasv"+suffix+"").set("urP", "u0*qMr/max(qR,1e-8)");
    model.component(cell).variable("gasv"+suffix+"").set("uzP", "u0*qMz/max(qR,1e-8)");
    model.component(cell).variable("gasv"+suffix+"").set("Tgas", "max(Tg,150[K])");
    model.component(cell).variable("gasv"+suffix+"").set("TeV", "max("+plasma+".Te,0.015[V])");
    model.component(cell).variable("gasv"+suffix+"").set("TeK", ""+plasma+".Te*e_const/k_B_const");
    model.component(cell).variable("gasv"+suffix+"").set("Ue", "e_const*"+plasma+".en");
    model.component(cell).variable("gasv"+suffix+"").set("pe", "(2/3)*Ue");
    model.component(cell).variable("gasv"+suffix+"").set("divU", "d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)");
    model.component(cell).variable("gasv"+suffix+"").set("nN2", ""+plasma+".n_wN2"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nO2", ""+plasma+".n_wO2"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nN", ""+plasma+".n_wN"+suffix+"");
}
static void part6(Model model,String cell,String suffix,String plasma){

    model.component(cell).variable("gasv"+suffix+"").set("nO", ""+plasma+".n_wO"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nN2_1p", ""+plasma+".n_wN2"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix+"").set("nO2_1p", ""+plasma+".n_wO2"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix+"").set("nN_1p", ""+plasma+".n_wN"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix+"").set("nO_1p", ""+plasma+".n_wO"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix+"").set("nO_1m", ""+plasma+".n_wO"+suffix+"_1m");
    model.component(cell).variable("gasv"+suffix+"").set("nO2_1m", ""+plasma+".n_wO2"+suffix+"_1m");
    model.component(cell).variable("gasv"+suffix+"").set("nCu", ""+plasma+".n_wCu"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nCuD5", ""+plasma+".n_wCuD5"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nCuD3", ""+plasma+".n_wCuD3"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nCuP1", ""+plasma+".n_wCuP1"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nCuP3", ""+plasma+".n_wCuP3"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nCuH3", ""+plasma+".n_wCuH3"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nCuH5", ""+plasma+".n_wCuH5"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nCu_1p", ""+plasma+".n_wCu"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix+"")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component(cell).variable("gasv"+suffix+"").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p");
    model.component(cell).variable("gasv"+suffix+"").set("rhoCopper", "nCuTotal*Mcu/N_A_const");
    model.component(cell).variable("gasv"+suffix+"").set("Ycu", "rhoCopper/rhoSafe");
    model.component(cell).variable("gasv"+suffix+"")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)");
    model.component(cell).variable("gasv"+suffix+"").set("pHeavy", "nHeavy*k_B_const*Tgas");
    model.component(cell).variable("gasv"+suffix+"").set("pPl", "pHeavy+pe");
    model.component(cell).variable("gasv"+suffix+"").set("cs", "sqrt(1.667*pPl/rhoSafe)");
    model.component(cell).variable("gasv"+suffix+"").set("speedP", "sqrt(urP^2+uzP^2+1e-6[m^2/s^2])");
    model.component(cell).variable("gasv"+suffix+"").set("Dnum", "nuFactor*h*(speedP+cs)");
    model.component(cell).variable("gasv"+suffix+"").set("Dgas", "1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe");
    model.component(cell).variable("gasv"+suffix+"").set("muGas", "1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum");
    model.component(cell).variable("gasv"+suffix+"")
         .set("kGas", "0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7");
    model.component(cell).variable("gasv"+suffix+"").set("tauRR", "2*muGas*d(urP,r)-2/3*muGas*divU");
    model.component(cell).variable("gasv"+suffix+"").set("tauZZ", "2*muGas*d(uzP,z)-2/3*muGas*divU");
    model.component(cell).variable("gasv"+suffix+"").set("tauRZ", "muGas*(d(urP,z)+d(uzP,r))");
    model.component(cell).variable("gasv"+suffix+"").set("tauPP", "2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU");
    model.component(cell).variable("gasv"+suffix+"").set("kmN2", "10^lr_N2_0(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("kmO2", "10^lr_O2_2(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("kmCu", "2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))");
    model.component(cell).variable("gasv"+suffix+"").set("nuEN", "nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu");
    model.component(cell).variable("gasv"+suffix+"").set("niZ2", "nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p");
    model.component(cell).variable("gasv"+suffix+"")
         .set("coulombLog", "max(2,min(15,23-log(sqrt(max("+plasma+".ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))");
    model.component(cell).variable("gasv"+suffix+"").set("nuEI", "2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5");
    model.component(cell).variable("gasv"+suffix+"").set("nuCollision", "max(nuEN+nuEI,1e5[1/s])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("alphaLaser", ""+plasma+".ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component(cell).variable("gasv"+suffix+"").set("Qei", "3*me_const/(Mcu/N_A_const)*"+plasma+".ne*k_B_const*(TeK-Tgas)*nuEI");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_N2", "(3.5*R_const*Tgas/0.0280134[kg/mol]+0[V]*F_const/0.0280134[kg/mol])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_O2", "(3.5*R_const*Tgas/0.0319988[kg/mol]+0[V]*F_const/0.0319988[kg/mol])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_N", "(2.5*R_const*Tgas/0.0140067[kg/mol]+4.88[V]*F_const/0.0140067[kg/mol])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_O", "(2.5*R_const*Tgas/0.0159994[kg/mol]+2.58[V]*F_const/0.0159994[kg/mol])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_N2_1p", "(3.5*R_const*Tgas/0.0280134[kg/mol]+15.58[V]*F_const/0.0280134[kg/mol])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_O2_1p", "(3.5*R_const*Tgas/0.0319988[kg/mol]+12.06[V]*F_const/0.0319988[kg/mol])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_N_1p", "(2.5*R_const*Tgas/0.0140067[kg/mol]+19.414[V]*F_const/0.0140067[kg/mol])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_O_1p", "(2.5*R_const*Tgas/0.0159994[kg/mol]+16.198[V]*F_const/0.0159994[kg/mol])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_O_1m", "(2.5*R_const*Tgas/0.0159994[kg/mol]+1.119[V]*F_const/0.0159994[kg/mol])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_O2_1m", "(3.5*R_const*Tgas/0.0319988[kg/mol]+-0.45[V]*F_const/0.0319988[kg/mol])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_Cu", "(2.5*R_const*Tgas/0.063546[kg/mol]+0[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_CuD5", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.388948[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_CuD3", "(2.5*R_const*Tgas/0.063546[kg/mol]+1.6422256[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_CuP1", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.7858977[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_CuP3", "(2.5*R_const*Tgas/0.063546[kg/mol]+3.816692[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_CuH3", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1911751[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_CuH5", "(2.5*R_const*Tgas/0.063546[kg/mol]+6.1920252[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("h_Cu_1p", "(2.5*R_const*Tgas/0.063546[kg/mol]+7.72638[V]*F_const/0.063546[kg/mol]+Bcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Hdiffr", "h_N2*("+plasma+".tfluxr_wN2"+suffix+"-"+plasma+".cfluxr_wN2"+suffix+")+h_O2*("+plasma+".tfluxr_wO2"+suffix+"-"+plasma+".cfluxr_wO2"+suffix+")+h_N*("+plasma+".tfluxr_wN"+suffix+"-"+plasma+".cfluxr_wN"+suffix+")+h_O*("+plasma+".tfluxr_wO"+suffix+"-"+plasma+".cfluxr_wO"+suffix+")+h_N2_1p*("+plasma+".tfluxr_wN2"+suffix+"_1p-"+plasma+".cfluxr_wN2"+suffix+"_1p)+h_O2_1p*("+plasma+".tfluxr_wO2"+suffix+"_1p-"+plasma+".cfluxr_wO2"+suffix+"_1p)+h_N_1p*("+plasma+".tfluxr_wN"+suffix+"_1p-"+plasma+".cfluxr_wN"+suffix+"_1p)+h_O_1p*("+plasma+".tfluxr_wO"+suffix+"_1p-"+plasma+".cfluxr_wO"+suffix+"_1p)+h_O_1m*("+plasma+".tfluxr_wO"+suffix+"_1m-"+plasma+".cfluxr_wO"+suffix+"_1m)+h_O2_1m*("+plasma+".tfluxr_wO2"+suffix+"_1m-"+plasma+".cfluxr_wO2"+suffix+"_1m)+h_Cu*("+plasma+".tfluxr_wCu"+suffix+"-"+plasma+".cfluxr_wCu"+suffix+")+h_CuD5*("+plasma+".tfluxr_wCuD5"+suffix+"-"+plasma+".cfluxr_wCuD5"+suffix+")+h_CuD3*("+plasma+".tfluxr_wCuD3"+suffix+"-"+plasma+".cfluxr_wCuD3"+suffix+")+h_CuP1*("+plasma+".tfluxr_wCuP1"+suffix+"-"+plasma+".cfluxr_wCuP1"+suffix+")+h_CuP3*("+plasma+".tfluxr_wCuP3"+suffix+"-"+plasma+".cfluxr_wCuP3"+suffix+")+h_CuH3*("+plasma+".tfluxr_wCuH3"+suffix+"-"+plasma+".cfluxr_wCuH3"+suffix+")+h_CuH5*("+plasma+".tfluxr_wCuH5"+suffix+"-"+plasma+".cfluxr_wCuH5"+suffix+")+h_Cu_1p*("+plasma+".tfluxr_wCu"+suffix+"_1p-"+plasma+".cfluxr_wCu"+suffix+"_1p)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Jtotalr", "1*F_const/0.0280134[kg/mol]*("+plasma+".tfluxr_wN2"+suffix+"_1p-"+plasma+".cfluxr_wN2"+suffix+"_1p)+1*F_const/0.0319988[kg/mol]*("+plasma+".tfluxr_wO2"+suffix+"_1p-"+plasma+".cfluxr_wO2"+suffix+"_1p)+1*F_const/0.0140067[kg/mol]*("+plasma+".tfluxr_wN"+suffix+"_1p-"+plasma+".cfluxr_wN"+suffix+"_1p)+1*F_const/0.0159994[kg/mol]*("+plasma+".tfluxr_wO"+suffix+"_1p-"+plasma+".cfluxr_wO"+suffix+"_1p)+-1*F_const/0.0159994[kg/mol]*("+plasma+".tfluxr_wO"+suffix+"_1m-"+plasma+".cfluxr_wO"+suffix+"_1m)+-1*F_const/0.0319988[kg/mol]*("+plasma+".tfluxr_wO2"+suffix+"_1m-"+plasma+".cfluxr_wO2"+suffix+"_1m)+1*F_const/0.063546[kg/mol]*("+plasma+".tfluxr_wCu"+suffix+"_1p-"+plasma+".cfluxr_wCu"+suffix+"_1p)-e_const*"+plasma+".gflux_ner+"+plasma+".scharge*urP");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Hdiffz", "h_N2*("+plasma+".tfluxz_wN2"+suffix+"-"+plasma+".cfluxz_wN2"+suffix+")+h_O2*("+plasma+".tfluxz_wO2"+suffix+"-"+plasma+".cfluxz_wO2"+suffix+")+h_N*("+plasma+".tfluxz_wN"+suffix+"-"+plasma+".cfluxz_wN"+suffix+")+h_O*("+plasma+".tfluxz_wO"+suffix+"-"+plasma+".cfluxz_wO"+suffix+")+h_N2_1p*("+plasma+".tfluxz_wN2"+suffix+"_1p-"+plasma+".cfluxz_wN2"+suffix+"_1p)+h_O2_1p*("+plasma+".tfluxz_wO2"+suffix+"_1p-"+plasma+".cfluxz_wO2"+suffix+"_1p)+h_N_1p*("+plasma+".tfluxz_wN"+suffix+"_1p-"+plasma+".cfluxz_wN"+suffix+"_1p)+h_O_1p*("+plasma+".tfluxz_wO"+suffix+"_1p-"+plasma+".cfluxz_wO"+suffix+"_1p)+h_O_1m*("+plasma+".tfluxz_wO"+suffix+"_1m-"+plasma+".cfluxz_wO"+suffix+"_1m)+h_O2_1m*("+plasma+".tfluxz_wO2"+suffix+"_1m-"+plasma+".cfluxz_wO2"+suffix+"_1m)+h_Cu*("+plasma+".tfluxz_wCu"+suffix+"-"+plasma+".cfluxz_wCu"+suffix+")+h_CuD5*("+plasma+".tfluxz_wCuD5"+suffix+"-"+plasma+".cfluxz_wCuD5"+suffix+")+h_CuD3*("+plasma+".tfluxz_wCuD3"+suffix+"-"+plasma+".cfluxz_wCuD3"+suffix+")+h_CuP1*("+plasma+".tfluxz_wCuP1"+suffix+"-"+plasma+".cfluxz_wCuP1"+suffix+")+h_CuP3*("+plasma+".tfluxz_wCuP3"+suffix+"-"+plasma+".cfluxz_wCuP3"+suffix+")+h_CuH3*("+plasma+".tfluxz_wCuH3"+suffix+"-"+plasma+".cfluxz_wCuH3"+suffix+")+h_CuH5*("+plasma+".tfluxz_wCuH5"+suffix+"-"+plasma+".cfluxz_wCuH5"+suffix+")+h_Cu_1p*("+plasma+".tfluxz_wCu"+suffix+"_1p-"+plasma+".cfluxz_wCu"+suffix+"_1p)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Jtotalz", "1*F_const/0.0280134[kg/mol]*("+plasma+".tfluxz_wN2"+suffix+"_1p-"+plasma+".cfluxz_wN2"+suffix+"_1p)+1*F_const/0.0319988[kg/mol]*("+plasma+".tfluxz_wO2"+suffix+"_1p-"+plasma+".cfluxz_wO2"+suffix+"_1p)+1*F_const/0.0140067[kg/mol]*("+plasma+".tfluxz_wN"+suffix+"_1p-"+plasma+".cfluxz_wN"+suffix+"_1p)+1*F_const/0.0159994[kg/mol]*("+plasma+".tfluxz_wO"+suffix+"_1p-"+plasma+".cfluxz_wO"+suffix+"_1p)+-1*F_const/0.0159994[kg/mol]*("+plasma+".tfluxz_wO"+suffix+"_1m-"+plasma+".cfluxz_wO"+suffix+"_1m)+-1*F_const/0.0319988[kg/mol]*("+plasma+".tfluxz_wO2"+suffix+"_1m-"+plasma+".cfluxz_wO2"+suffix+"_1m)+1*F_const/0.063546[kg/mol]*("+plasma+".tfluxz_wCu"+suffix+"_1p-"+plasma+".cfluxz_wCu"+suffix+"_1p)-e_const*"+plasma+".gflux_nez+"+plasma+".scharge*uzP");
    model.component(cell).variable("gasv"+suffix+"").set("Qfield", "Jtotalr*"+plasma+".Etr+Jtotalz*"+plasma+".Etz");
    model.component(cell).variable("gasv"+suffix+"").set("dnu1", "1/(324.7536[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("sigline1", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.44113338749349373/(sqrt(pi)*dnu1)");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*w0");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta1", "if(tauLine1<1e-3,1-tauLine1/2+tauLine1^2/6,(1-exp(-tauLine1))/tauLine1)");
    model.component(cell).variable("gasv"+suffix+"").set("jline1", "139500000.0[1/s]*nCuP3*3.816692[V]*e_const/(4*pi)");
    model.component(cell).variable("gasv"+suffix+"").set("dnu2", "1/(327.3952[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("sigline2", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.2211163348852216/(sqrt(pi)*dnu2)");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*w0");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta2", "if(tauLine2<1e-3,1-tauLine2/2+tauLine2^2/6,(1-exp(-tauLine2))/tauLine2)");
    model.component(cell).variable("gasv"+suffix+"").set("jline2", "137600000.0[1/s]*nCuP1*3.7858977[V]*e_const/(4*pi)");
    model.component(cell).variable("gasv"+suffix+"").set("dnu3", "1/(510.5548[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("sigline3", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0052105220881473865/(sqrt(pi)*dnu3)");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*w0");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta3", "if(tauLine3<1e-3,1-tauLine3/2+tauLine3^2/6,(1-exp(-tauLine3))/tauLine3)");
    model.component(cell).variable("gasv"+suffix+"").set("jline3", "2000000.0[1/s]*nCuP3*2.427744[V]*e_const/(4*pi)");
    model.component(cell).variable("gasv"+suffix+"").set("dnu4", "1/(515.3238[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("sigline4", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4777486018933752/(sqrt(pi)*dnu4)");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*w0");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta4", "if(tauLine4<1e-3,1-tauLine4/2+tauLine4^2/6,(1-exp(-tauLine4))/tauLine4)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("jline4", "60000000.0[1/s]*nCuH3*2.4052773999999997[V]*e_const/(4*pi)");
    model.component(cell).variable("gasv"+suffix+"").set("dnu5", "1/(521.8202[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("sigline5", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.4592530759477124/(sqrt(pi)*dnu5)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*w0");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta5", "if(tauLine5<1e-3,1-tauLine5/2+tauLine5^2/6,(1-exp(-tauLine5))/tauLine5)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("jline5", "75000000.0[1/s]*nCuH5*2.3753331999999996[V]*e_const/(4*pi)");
    model.component(cell).variable("gasv"+suffix+"").set("dnu6", "1/(522.007[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("sigline6", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.06127759193714263/(sqrt(pi)*dnu6)");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*w0");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta6", "if(tauLine6<1e-3,1-tauLine6/2+tauLine6^2/6,(1-exp(-tauLine6))/tauLine6)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("jline6", "15000000.0[1/s]*nCuH3*2.3744830999999995[V]*e_const/(4*pi)");
    model.component(cell).variable("gasv"+suffix+"").set("dnu7", "1/(570.024[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("sigline7", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0011691098026221686/(sqrt(pi)*dnu7)");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*w0");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta7", "if(tauLine7<1e-3,1-tauLine7/2+tauLine7^2/6,(1-exp(-tauLine7))/tauLine7)");
    model.component(cell).variable("gasv"+suffix+"").set("jline7", "240000.0[1/s]*nCuP3*2.1744664[V]*e_const/(4*pi)");
    model.component(cell).variable("gasv"+suffix+"").set("dnu8", "1/(578.2126[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("sigline8", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.0041351077309135596/(sqrt(pi)*dnu8)");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*w0");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta8", "if(tauLine8<1e-3,1-tauLine8/2+tauLine8^2/6,(1-exp(-tauLine8))/tauLine8)");
    model.component(cell).variable("gasv"+suffix+"").set("jline8", "1650000.0[1/s]*nCuP1*2.1436721[V]*e_const/(4*pi)");
    model.component(cell).variable("gasv"+suffix+"").set("r_Curr", "(10^lr_Cu_rr(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nCu_1p");
    model.component(cell).variable("gasv"+suffix+"").set("Eph_Curr", "10^rrMean_Curr(log10(TeV/1[V]))*1[V]");
    model.component(cell).variable("gasv"+suffix+"").set("Qfb_Cu", "r_Curr*e_const*Eph_Curr");
    model.component(cell).variable("gasv"+suffix+"").set("r_Nrr", "(10^lr_N_rr(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nN_1p");
    model.component(cell).variable("gasv"+suffix+"").set("Eph_Nrr", "10^rrMean_Nrr(log10(TeV/1[V]))*1[V]");
    model.component(cell).variable("gasv"+suffix+"").set("Qfb_N", "r_Nrr*e_const*Eph_Nrr");
    model.component(cell).variable("gasv"+suffix+"").set("r_Orr", "(10^lr_O_rr(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nO_1p");
    model.component(cell).variable("gasv"+suffix+"").set("Eph_Orr", "10^rrMean_Orr(log10(TeV/1[V]))*1[V]");
    model.component(cell).variable("gasv"+suffix+"").set("Qfb_O", "r_Orr*e_const*Eph_Orr");
    model.component(cell).variable("gasv"+suffix+"")
         .set("r_CuD5rr", "(10^lr_CuD5_rr(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nCu_1p");
    model.component(cell).variable("gasv"+suffix+"").set("Eph_CuD5rr", "10^rrMean_CuD5rr(log10(TeV/1[V]))*1[V]");
    model.component(cell).variable("gasv"+suffix+"").set("Qfb_CuD5", "r_CuD5rr*e_const*Eph_CuD5rr");
    model.component(cell).variable("gasv"+suffix+"")
         .set("r_CuD3rr", "(10^lr_CuD3_rr(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nCu_1p");
    model.component(cell).variable("gasv"+suffix+"").set("Eph_CuD3rr", "10^rrMean_CuD3rr(log10(TeV/1[V]))*1[V]");
}
static void part7(Model model,String cell,String suffix,String plasma){

    model.component(cell).variable("gasv"+suffix+"").set("Qfb_CuD3", "r_CuD3rr*e_const*Eph_CuD3rr");
    model.component(cell).variable("gasv"+suffix+"")
         .set("r_CuP1rr", "(10^lr_CuP1_rr(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nCu_1p");
    model.component(cell).variable("gasv"+suffix+"").set("Eph_CuP1rr", "10^rrMean_CuP1rr(log10(TeV/1[V]))*1[V]");
    model.component(cell).variable("gasv"+suffix+"").set("Qfb_CuP1", "r_CuP1rr*e_const*Eph_CuP1rr");
    model.component(cell).variable("gasv"+suffix+"")
         .set("r_CuP3rr", "(10^lr_CuP3_rr(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nCu_1p");
    model.component(cell).variable("gasv"+suffix+"").set("Eph_CuP3rr", "10^rrMean_CuP3rr(log10(TeV/1[V]))*1[V]");
    model.component(cell).variable("gasv"+suffix+"").set("Qfb_CuP3", "r_CuP3rr*e_const*Eph_CuP3rr");
    model.component(cell).variable("gasv"+suffix+"")
         .set("r_CuH3rr", "(10^lr_CuH3_rr(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nCu_1p");
    model.component(cell).variable("gasv"+suffix+"").set("Eph_CuH3rr", "10^rrMean_CuH3rr(log10(TeV/1[V]))*1[V]");
    model.component(cell).variable("gasv"+suffix+"").set("Qfb_CuH3", "r_CuH3rr*e_const*Eph_CuH3rr");
    model.component(cell).variable("gasv"+suffix+"")
         .set("r_CuH5rr", "(10^lr_CuH5_rr(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nCu_1p");
    model.component(cell).variable("gasv"+suffix+"").set("Eph_CuH5rr", "10^rrMean_CuH5rr(log10(TeV/1[V]))*1[V]");
    model.component(cell).variable("gasv"+suffix+"").set("Qfb_CuH5", "r_CuH5rr*e_const*Eph_CuH5rr");
    model.component(cell).variable("gasv"+suffix+"").set("Qff", "1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*"+plasma+".ne*niZ2");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+Qfb_Cu+Qfb_N+Qfb_O+Qfb_CuD5+Qfb_CuD3+Qfb_CuP1+Qfb_CuP3+Qfb_CuH3+Qfb_CuH5+Qff");
    model.component(cell).variable("gasv"+suffix+"")
         .set("ffVisibleFraction", "exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("jfbVisible", "Qfb_CuH3*(exp(-max(h_const*c_const/(525[nm]*e_const)-1.5352049[V],0[V])/TeV)-exp(-max(h_const*c_const/(475[nm]*e_const)-1.5352049[V],0[V])/TeV))/(4*pi)+Qfb_CuH5*(exp(-max(h_const*c_const/(525[nm]*e_const)-1.5343548[V],0[V])/TeV)-exp(-max(h_const*c_const/(475[nm]*e_const)-1.5343548[V],0[V])/TeV))/(4*pi)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("jVisible", "jline3+jline4+jline5+jline6+Qff*ffVisibleFraction/(4*pi)+jfbVisible");
    model.component(cell).variable("gasv"+suffix+"")
         .set("QeRR", "r_Curr*e_const*(Eph_Curr-7.72638[V])+r_Nrr*e_const*(Eph_Nrr-14.534[V])+r_Orr*e_const*(Eph_Orr-13.618[V])+r_CuD5rr*e_const*(Eph_CuD5rr-6.337432[V])+r_CuD3rr*e_const*(Eph_CuD3rr-6.0841544[V])+r_CuP1rr*e_const*(Eph_CuP1rr-3.9404822999999998[V])+r_CuP3rr*e_const*(Eph_CuP3rr-3.9096879999999996[V])+r_CuH3rr*e_const*(Eph_CuH3rr-1.5352049[V])+r_CuH5rr*e_const*(Eph_CuH5rr-1.5343548[V])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("QeAttach", "1.5*e_const*TeV*((10^lr_O2_0(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nO2+(10^lr_O2_1(log10(TeV/1[V]))*1[m^3/s])*"+plasma+".ne*nO2+(2.8e-13[m^3/s]*(300[K]/TeK)^0.5)*"+plasma+".ne*nN2_1p+(2e-13[m^3/s]*(300[K]/TeK)^0.7)*"+plasma+".ne*nO2_1p+(1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg)))*"+plasma+".ne*nO2^2)");
    model.component(cell).variable("boundv"+suffix+"").set("ncr", "if(nz>=0,nr,-nr)");
    model.component(cell).variable("boundv"+suffix+"").set("ncz", "abs(nz)");
    model.component(cell).variable("boundv"+suffix+"").set("kappa", "dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("sigmaCu", "max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("psatCu", "pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("mdotEvap", "accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))");
    model.component(cell).variable("boundv"+suffix+"").set("vKn", "sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_N2_1p", "nN2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0280134[kg/mol]/N_A_const))+nN2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*("+plasma+".Etr*ncr+"+plasma+".Etz*ncz),0[m/s])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_O2_1p", "nO2_1p*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*("+plasma+".Etr*ncr+"+plasma+".Etz*ncz),0[m/s])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_N_1p", "nN_1p*sqrt(k_B_const*Tgas/(2*pi*0.0140067[kg/mol]/N_A_const))+nN_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*("+plasma+".Etr*ncr+"+plasma+".Etz*ncz),0[m/s])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_O_1p", "nO_1p*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*("+plasma+".Etr*ncr+"+plasma+".Etz*ncz),0[m/s])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_O_1m", "nO_1m*sqrt(k_B_const*Tgas/(2*pi*0.0159994[kg/mol]/N_A_const))+nO_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*("+plasma+".Etr*ncr+"+plasma+".Etz*ncz),0[m/s])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_O2_1m", "nO2_1m*sqrt(k_B_const*Tgas/(2*pi*0.0319988[kg/mol]/N_A_const))+nO2_1m*max(--1*e_const*Dgas/(k_B_const*Tgas)*("+plasma+".Etr*ncr+"+plasma+".Etz*ncz),0[m/s])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_Cu", "nCu*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_CuD5", "nCuD5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_CuD3", "nCuD3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_CuP1", "nCuP1*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_CuP3", "nCuP3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_CuH3", "nCuH3*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_CuH5", "nCuH5*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Gwall_Cu_1p", "nCu_1p*sqrt(k_B_const*Tgas/(2*pi*0.063546[kg/mol]/N_A_const))+nCu_1p*max(-1*e_const*Dgas/(k_B_const*Tgas)*("+plasma+".Etr*ncr+"+plasma+".Etz*ncz),0[m/s])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("mdotReturn", "Gwall_Cu*Mcu/N_A_const+Gwall_CuD5*Mcu/N_A_const+Gwall_CuD3*Mcu/N_A_const+Gwall_CuP1*Mcu/N_A_const+Gwall_CuP3*Mcu/N_A_const+Gwall_CuH3*Mcu/N_A_const+Gwall_CuH5*Mcu/N_A_const+Gwall_Cu_1p*Mcu/N_A_const");
    model.component(cell).variable("boundv"+suffix+"").set("mdot", "mdotEvap-mdotReturn");
    model.component(cell).variable("boundv"+suffix+"").set("recoil", "mdotEvap*vKn");
    model.component(cell).variable("boundv"+suffix+"").set("vnSurf", "meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Qmass", "mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-(Gwall_Cu*Mcu/N_A_const*(h_Cu+0.5*(urP^2+uzP^2))+Gwall_CuD5*Mcu/N_A_const*(h_CuD5+0.5*(urP^2+uzP^2))+Gwall_CuD3*Mcu/N_A_const*(h_CuD3+0.5*(urP^2+uzP^2))+Gwall_CuP1*Mcu/N_A_const*(h_CuP1+0.5*(urP^2+uzP^2))+Gwall_CuP3*Mcu/N_A_const*(h_CuP3+0.5*(urP^2+uzP^2))+Gwall_CuH3*Mcu/N_A_const*(h_CuH3+0.5*(urP^2+uzP^2))+Gwall_CuH5*Mcu/N_A_const*(h_CuH5+0.5*(urP^2+uzP^2))+Gwall_Cu_1p*Mcu/N_A_const*(h_Cu_1p+0.5*(urP^2+uzP^2)))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("hAir", "k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))");
    model.component(cell).variable("boundv"+suffix+"").set("Qexchange", "accom*hAir*(Tg-T)");
    model.component(cell).variable("boundv"+suffix+"").set("Qsurface", "Acu*Ibeam*optD*ncz");
    model.component(cell).variable("boundv"+suffix+"").set("Gtherm", ""+plasma+".thflux");
    model.component(cell).variable("boundv"+suffix+"").set("GeIn", "-"+plasma+".ne_bnd-"+plasma+".nemig"+suffix+"_bnd");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Iwall", "e_const*(1*Gwall_N2_1p+1*Gwall_O2_1p+1*Gwall_N_1p+1*Gwall_O_1p+-1*Gwall_O_1m+-1*Gwall_O2_1m+1*Gwall_Cu_1p-GeIn+Gtherm)");
    model.component(cell).variable("boundv"+suffix+"")
         .set("QelectronWall", "e_const*("+plasma+".en_bnd+"+plasma+".ten_bnd+"+plasma+".enmig"+suffix+"_bnd)");
    model.component(cell).variable("boundv"+suffix+"").set("QworkWall", "e_const*workCu*(Gtherm-GeIn)");
    model.component(cell).variable("boundv"+suffix+"").set("Qthermion", "QelectronWall+QworkWall");
    model.component(cell).physics(plasma).feature("N2_1")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("N2_2")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("N2_3")
         .set("de", "0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("N2_4")
         .set("de", "0.59*(1-exp(0.59[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.59[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("N2_5")
         .set("de", "0.88*(1-exp(0.88[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.88[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("N2_6")
         .set("de", "1.17*(1-exp(1.17[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.17[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("N2_7")
         .set("de", "1.47*(1-exp(1.47[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.47[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("N2_8")
         .set("de", "1.76*(1-exp(1.76[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.76[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("O2_3")
         .set("de", "0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("O2_4")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("O2_5")
         .set("de", "0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("O2_6")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("O2_7")
         .set("de", "0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("O2_8")
         .set("de", "0.57*(1-exp(0.57[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.57[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("O2_9")
         .set("de", "0.75*(1-exp(0.75[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.75[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("O2_10")
         .set("de", "0.977*(1-exp(0.977[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.977[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).physics(plasma).feature("O2_11")
         .set("de", "1.627*(1-exp(1.627[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.627[V]*e_const/(k_B_const*Tgas)))");
    model.component(cell).variable("gasv"+suffix+"").set("nOtherNeutral", "nN+nO+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5");
    model.component(cell).variable("gasv"+suffix+"").set("nuEN", "nN2*kmN2+nO2*kmO2+nOtherNeutral*kmCu");
    model.component(cell).physics(plasma).feature("N2"+suffix+"").set("n0", "1[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wO2"+suffix+"", "wO2"+suffix+"");
    model.component(cell).physics(plasma).feature("O2"+suffix+"").set("n0", "0.21*pAmb/(k_B_const*Tamb)");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wN"+suffix+"", "wN"+suffix+"");
    model.component(cell).physics(plasma).feature("N"+suffix+"").set("n0", "1[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wO"+suffix+"", "wO"+suffix+"");
    model.component(cell).physics(plasma).feature("O"+suffix+"").set("n0", "1[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wN2"+suffix+"_1p", "wN2"+suffix+"_1p");
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").set("n0", "1e7[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wO2"+suffix+"_1p", "wO2"+suffix+"_1p");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p").set("n0", "1e7[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wN"+suffix+"_1p", "wN"+suffix+"_1p");
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p").set("n0", "1e7[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wO"+suffix+"_1p", "wO"+suffix+"_1p");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p").set("n0", "1e7[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wO"+suffix+"_1m", "wO"+suffix+"_1m");
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m").set("n0", "1e7[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wO2"+suffix+"_1m", "wO2"+suffix+"_1m");
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m").set("n0", "1e7[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCu"+suffix+"", "wCu"+suffix+"");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").set("n0", "1[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCuD5"+suffix+"", "wCuD5"+suffix+"");
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").set("n0", "1[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCuD3"+suffix+"", "wCuD3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").set("n0", "1[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCuP1"+suffix+"", "wCuP1"+suffix+"");
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").set("n0", "1[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCuP3"+suffix+"", "wCuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").set("n0", "1[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCuH3"+suffix+"", "wCuH3"+suffix+"");
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").set("n0", "1[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCuH5"+suffix+"", "wCuH5"+suffix+"");
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").set("n0", "1[1/m^3]");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCu"+suffix+"_1p", "wCu"+suffix+"_1p");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").set("n0", "1e7[1/m^3]");
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").set("n0", "seedNe-2e7[1/m^3]");
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").set("InitIon", false);
    model.component(cell).physics(plasma).feature("pes1").set("Tgm", "Tg");
    model.component(cell).physics(plasma).feature("pes1").set("Tgm0", "Tamb");
    model.component(cell).physics(plasma).feature("pes1").set("p0", "pAmb");
    model.component(cell).physics(plasma).feature("pes1").set("ebarinitgm", "1.5*k_B_const*Tamb/e_const");
    model.component(cell).variable("gasv"+suffix+"").set("Tg", "exp(logGasT)*1[K]");
    model.component(cell).variable("gasv"+suffix+"").set("Tgas", "Tg");
    model.component(cell).variable("gasv"+suffix+"").set("rhoP", ""+plasma+".rho");
    model.component(cell).variable("gasv"+suffix+"").set("urP", "0[m/s]");
    model.component(cell).variable("gasv"+suffix+"").set("uzP", "0[m/s]");
}
static void part8(Model model,String cell,String suffix,String plasma){

    model.component(cell).variable("gasv"+suffix+"").set("Dgas", "1e-5[m^2/s]");
    model.component(cell).variable("gasv"+suffix+"").set("Rp", "Rinit*exp(logRadius)");
    model.component(cell).variable("gasv"+suffix+"").set("Hp", "Hinit*exp(logHeight)");
    model.component(cell).variable("gasv"+suffix+"").set("Vp", "2*pi/3*Rp^2*Hp");
    model.component(cell).variable("gasv"+suffix+"").set("Mgas", "rhoP*Vp");
    model.component(cell).variable("gasv"+suffix+"").set("vRadius", "momentumScale*pRadius/Mgas");
    model.component(cell).variable("gasv"+suffix+"").set("vHeight", "momentumScale*pHeight/Mgas");
    model.component(cell).variable("gasv"+suffix+"").set("volumeRate", "2*vRadius/Rp+vHeight/Hp");
    model.component(cell).variable("gasv"+suffix+"").set("Vdot", "Vp*volumeRate");
    model.component(cell).variable("gasv"+suffix+"").set("Msweep", "rhoAmb*max(Vdot,0[m^3/s])");
    model.component(cell).variable("gasv"+suffix+"").set("Kinetic", "Mgas*(2*vRadius^2+vHeight^2)/10");
    model.component(cell).variable("gasv"+suffix+"").set("pPl", ""+plasma+".pA+pe");
    model.component(cell).variable("gasv"+suffix+"").set("Req", "(3*Vp/(4*pi))^(1/3)");
    model.component(cell).variable("gasv"+suffix+"").set("pulse", "exp(-4*log(2)*((t-tp)/tau)^2)");
    model.component(cell).variable("gasv"+suffix+"").set("Pincident", "Ein*sqrt(4*log(2)/pi)/tau*pulse");
    model.component(cell).variable("gasv"+suffix+"").set("Ipeak", "2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");
    model.component(cell).variable("gasv"+suffix+"").set("beamCoverage", "1-exp(-2*(Rp/w0)^2)");
    model.component(cell).variable("gasv"+suffix+"").set("laserTau", "alphaLaser*Hp");
    model.component(cell).variable("gasv"+suffix+"").set("beamTransmission", "exp(-laserTau)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beamAbsorptance", "if(laserTau<1e-4,laserTau*(1-laserTau/2+laserTau^2/6-laserTau^3/24),1-exp(-laserTau))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Plaser", "Pincident*beamCoverage*beamAbsorptance*(1+(1-Acu)*beamTransmission)");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine1", "sigline1*max(nCu-0.5*nCuP3,0[1/m^3])*Req");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta1", "if(tauLine1<0.01,1-3*tauLine1/4+2*tauLine1^2/5,3/(4*tauLine1)*(1-1/(2*tauLine1^2)+(1/tauLine1+1/(2*tauLine1^2))*exp(-2*tauLine1)))");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine2", "sigline2*max(nCu-1.0*nCuP1,0[1/m^3])*Req");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta2", "if(tauLine2<0.01,1-3*tauLine2/4+2*tauLine2^2/5,3/(4*tauLine2)*(1-1/(2*tauLine2^2)+(1/tauLine2+1/(2*tauLine2^2))*exp(-2*tauLine2)))");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine3", "sigline3*max(nCuD5-1.5*nCuP3,0[1/m^3])*Req");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta3", "if(tauLine3<0.01,1-3*tauLine3/4+2*tauLine3^2/5,3/(4*tauLine3)*(1-1/(2*tauLine3^2)+(1/tauLine3+1/(2*tauLine3^2))*exp(-2*tauLine3)))");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine4", "sigline4*max(nCuP1-0.5*nCuH3,0[1/m^3])*Req");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta4", "if(tauLine4<0.01,1-3*tauLine4/4+2*tauLine4^2/5,3/(4*tauLine4)*(1-1/(2*tauLine4^2)+(1/tauLine4+1/(2*tauLine4^2))*exp(-2*tauLine4)))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("tauLine5", "sigline5*max(nCuP3-0.6666666666666666*nCuH5,0[1/m^3])*Req");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta5", "if(tauLine5<0.01,1-3*tauLine5/4+2*tauLine5^2/5,3/(4*tauLine5)*(1-1/(2*tauLine5^2)+(1/tauLine5+1/(2*tauLine5^2))*exp(-2*tauLine5)))");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine6", "sigline6*max(nCuP3-1.0*nCuH3,0[1/m^3])*Req");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta6", "if(tauLine6<0.01,1-3*tauLine6/4+2*tauLine6^2/5,3/(4*tauLine6)*(1-1/(2*tauLine6^2)+(1/tauLine6+1/(2*tauLine6^2))*exp(-2*tauLine6)))");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine7", "sigline7*max(nCuD3-1.0*nCuP3,0[1/m^3])*Req");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta7", "if(tauLine7<0.01,1-3*tauLine7/4+2*tauLine7^2/5,3/(4*tauLine7)*(1-1/(2*tauLine7^2)+(1/tauLine7+1/(2*tauLine7^2))*exp(-2*tauLine7)))");
    model.component(cell).variable("gasv"+suffix+"").set("tauLine8", "sigline8*max(nCuD3-2.0*nCuP1,0[1/m^3])*Req");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta8", "if(tauLine8<0.01,1-3*tauLine8/4+2*tauLine8^2/5,3/(4*tauLine8)*(1-1/(2*tauLine8^2)+(1/tauLine8+1/(2*tauLine8^2))*exp(-2*tauLine8)))");
    model.component(cell).physics(plasma).create("elasticAtomN", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("elasticAtomN").set("formula", "e+N"+suffix+"=>e+N"+suffix+"");
    model.component(cell).physics(plasma).feature("elasticAtomN").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("elasticAtomN").set("kf", "kmCu*N_A_const");
    model.component(cell).physics(plasma).feature("elasticAtomN").set("mratio", "me_const*N_A_const/"+plasma+".M_wN"+suffix+"");
    model.component(cell).physics(plasma).create("elasticAtomO", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("elasticAtomO").set("formula", "e+O"+suffix+"=>e+O"+suffix+"");
    model.component(cell).physics(plasma).feature("elasticAtomO").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("elasticAtomO").set("kf", "kmCu*N_A_const");
    model.component(cell).physics(plasma).feature("elasticAtomO").set("mratio", "me_const*N_A_const/"+plasma+".M_wO"+suffix+"");
    model.component(cell).physics(plasma).create("elasticAtomCu", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("elasticAtomCu").set("formula", "e+Cu"+suffix+"=>e+Cu"+suffix+"");
    model.component(cell).physics(plasma).feature("elasticAtomCu").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("elasticAtomCu").set("kf", "kmCu*N_A_const");
    model.component(cell).physics(plasma).feature("elasticAtomCu").set("mratio", "me_const*N_A_const/"+plasma+".M_wCu"+suffix+"");
    model.component(cell).physics(plasma).create("elasticAtomCuD5", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("elasticAtomCuD5").set("formula", "e+CuD5"+suffix+"=>e+CuD5"+suffix+"");
    model.component(cell).physics(plasma).feature("elasticAtomCuD5").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("elasticAtomCuD5").set("kf", "kmCu*N_A_const");
    model.component(cell).physics(plasma).feature("elasticAtomCuD5").set("mratio", "me_const*N_A_const/"+plasma+".M_wCuD5"+suffix+"");
    model.component(cell).physics(plasma).create("elasticAtomCuD3", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("elasticAtomCuD3").set("formula", "e+CuD3"+suffix+"=>e+CuD3"+suffix+"");
    model.component(cell).physics(plasma).feature("elasticAtomCuD3").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("elasticAtomCuD3").set("kf", "kmCu*N_A_const");
    model.component(cell).physics(plasma).feature("elasticAtomCuD3").set("mratio", "me_const*N_A_const/"+plasma+".M_wCuD3"+suffix+"");
    model.component(cell).physics(plasma).create("elasticAtomCuP1", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("elasticAtomCuP1").set("formula", "e+CuP1"+suffix+"=>e+CuP1"+suffix+"");
    model.component(cell).physics(plasma).feature("elasticAtomCuP1").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("elasticAtomCuP1").set("kf", "kmCu*N_A_const");
    model.component(cell).physics(plasma).feature("elasticAtomCuP1").set("mratio", "me_const*N_A_const/"+plasma+".M_wCuP1"+suffix+"");
    model.component(cell).physics(plasma).create("elasticAtomCuP3", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("elasticAtomCuP3").set("formula", "e+CuP3"+suffix+"=>e+CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("elasticAtomCuP3").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("elasticAtomCuP3").set("kf", "kmCu*N_A_const");
    model.component(cell).physics(plasma).feature("elasticAtomCuP3").set("mratio", "me_const*N_A_const/"+plasma+".M_wCuP3"+suffix+"");
    model.component(cell).physics(plasma).create("elasticAtomCuH3", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("elasticAtomCuH3").set("formula", "e+CuH3"+suffix+"=>e+CuH3"+suffix+"");
    model.component(cell).physics(plasma).feature("elasticAtomCuH3").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("elasticAtomCuH3").set("kf", "kmCu*N_A_const");
    model.component(cell).physics(plasma).feature("elasticAtomCuH3").set("mratio", "me_const*N_A_const/"+plasma+".M_wCuH3"+suffix+"");
    model.component(cell).physics(plasma).create("elasticAtomCuH5", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("elasticAtomCuH5").set("formula", "e+CuH5"+suffix+"=>e+CuH5"+suffix+"");
    model.component(cell).physics(plasma).feature("elasticAtomCuH5").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("elasticAtomCuH5").set("kf", "kmCu*N_A_const");
    model.component(cell).physics(plasma).feature("elasticAtomCuH5").set("mratio", "me_const*N_A_const/"+plasma+".M_wCuH5"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Qei", "3*me_const*N_A_const*"+plasma+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*(0[mol/(kg*m^3)]+"+plasma+".n_wN2"+suffix+"_1p/"+plasma+".M_wN2"+suffix+"_1p+"+plasma+".n_wO2"+suffix+"_1p/"+plasma+".M_wO2"+suffix+"_1p+"+plasma+".n_wN"+suffix+"_1p/"+plasma+".M_wN"+suffix+"_1p+"+plasma+".n_wO"+suffix+"_1p/"+plasma+".M_wO"+suffix+"_1p+"+plasma+".n_wO"+suffix+"_1m/"+plasma+".M_wO"+suffix+"_1m+"+plasma+".n_wO2"+suffix+"_1m/"+plasma+".M_wO2"+suffix+"_1m+"+plasma+".n_wCu"+suffix+"_1p/"+plasma+".M_wCu"+suffix+"_1p)");
    model.component(cell).variable("solidv"+suffix+"").set("fL", "0.5*(1+(T-Tm)/sqrt((T-Tm)^2+100[K^2]))");
    model.component(cell).variable("solidv"+suffix+"")
         .set("cpCu", "385[J/(kg*K)]*(1-fL)+572[J/(kg*K)]*fL+Lm*50[K^2]/((T-Tm)^2+100[K^2])^1.5");
    model.component(cell).variable("solidv"+suffix+"")
         .set("hCond", "385[J/(kg*K)]*(T-Tamb)+187[J/(kg*K)]*0.5*(T-Tamb+sqrt((T-Tm)^2+100[K^2])-sqrt((Tamb-Tm)^2+100[K^2]))+Lm*(fL-0.5*(1+(Tamb-Tm)/sqrt((Tamb-Tm)^2+100[K^2])))");
    model.component(cell).variable("solidv"+suffix+"").set("kCu", "385[W/(m*K)]*(1-fL)+165[W/(m*K)]*fL");
    model.component(cell).material().create("copper"+suffix+"", "Common");
    model.component(cell).material("copper"+suffix+"").selection().named("g"+suffix+"_cu_dom");
    model.component(cell).material("copper"+suffix+"").propertyGroup("def").set("density", "rhoCu");
    model.component(cell).material("copper"+suffix+"").propertyGroup("def").set("heatcapacity", "cpCu");
    model.component(cell).material("copper"+suffix+"").propertyGroup("def").set("thermalconductivity", "kCu");
    model.component(cell).variable("boundv"+suffix+"").set("contact", "flc2hs(Rp-r,1[um])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Qsurface", "Acu*Ipeak*pulse*exp(-2*(r/w0)^2)*(1-contact*(1-beamTransmission))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("mdotReturn", "contact*rhoCopper*sqrt(k_B_const*Tg/(2*pi*Mcu/N_A_const))");
    model.component(cell).variable("boundv"+suffix+"").set("mdot", "mdotEvap-mdotReturn");
    model.component(cell).variable("boundv"+suffix+"").set("emitWeight_Cu", "2*exp(-0.0[V]*e_const/(k_B_const*T))");
    model.component(cell).variable("boundv"+suffix+"").set("emitWeight_CuD5", "6*exp(-1.388948[V]*e_const/(k_B_const*T))");
    model.component(cell).variable("boundv"+suffix+"").set("emitWeight_CuD3", "4*exp(-1.6422256[V]*e_const/(k_B_const*T))");
    model.component(cell).variable("boundv"+suffix+"").set("emitWeight_CuP1", "2*exp(-3.7858977[V]*e_const/(k_B_const*T))");
    model.component(cell).variable("boundv"+suffix+"").set("emitWeight_CuP3", "4*exp(-3.816692[V]*e_const/(k_B_const*T))");
    model.component(cell).variable("boundv"+suffix+"").set("emitWeight_CuH3", "4*exp(-6.1911751[V]*e_const/(k_B_const*T))");
    model.component(cell).variable("boundv"+suffix+"").set("emitWeight_CuH5", "6*exp(-6.1920252[V]*e_const/(k_B_const*T))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("emitWeight_Cu_1p", "1*exp((workCu-7.72638[V])*e_const/(k_B_const*T))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("emitPartition", "0+emitWeight_Cu+emitWeight_CuD5+emitWeight_CuD3+emitWeight_CuP1+emitWeight_CuP3+emitWeight_CuH3+emitWeight_CuH5+emitWeight_Cu_1p");
    model.component(cell).variable("boundv"+suffix+"").set("emitFraction_Cu", "emitWeight_Cu/emitPartition");
    model.component(cell).variable("boundv"+suffix+"")
         .set("source_Cu", "mdotEvap*emitFraction_Cu-mdotReturn*"+plasma+".n_wCu"+suffix+"/max(nCuTotal,1[1/m^3])");
    model.component(cell).variable("boundv"+suffix+"").set("emitFraction_CuD5", "emitWeight_CuD5/emitPartition");
    model.component(cell).variable("boundv"+suffix+"")
         .set("source_CuD5", "mdotEvap*emitFraction_CuD5-mdotReturn*"+plasma+".n_wCuD5"+suffix+"/max(nCuTotal,1[1/m^3])");
    model.component(cell).variable("boundv"+suffix+"").set("emitFraction_CuD3", "emitWeight_CuD3/emitPartition");
    model.component(cell).variable("boundv"+suffix+"")
         .set("source_CuD3", "mdotEvap*emitFraction_CuD3-mdotReturn*"+plasma+".n_wCuD3"+suffix+"/max(nCuTotal,1[1/m^3])");
    model.component(cell).variable("boundv"+suffix+"").set("emitFraction_CuP1", "emitWeight_CuP1/emitPartition");
    model.component(cell).variable("boundv"+suffix+"")
         .set("source_CuP1", "mdotEvap*emitFraction_CuP1-mdotReturn*"+plasma+".n_wCuP1"+suffix+"/max(nCuTotal,1[1/m^3])");
    model.component(cell).variable("boundv"+suffix+"").set("emitFraction_CuP3", "emitWeight_CuP3/emitPartition");
    model.component(cell).variable("boundv"+suffix+"")
         .set("source_CuP3", "mdotEvap*emitFraction_CuP3-mdotReturn*"+plasma+".n_wCuP3"+suffix+"/max(nCuTotal,1[1/m^3])");
    model.component(cell).variable("boundv"+suffix+"").set("emitFraction_CuH3", "emitWeight_CuH3/emitPartition");
    model.component(cell).variable("boundv"+suffix+"")
         .set("source_CuH3", "mdotEvap*emitFraction_CuH3-mdotReturn*"+plasma+".n_wCuH3"+suffix+"/max(nCuTotal,1[1/m^3])");
    model.component(cell).variable("boundv"+suffix+"").set("emitFraction_CuH5", "emitWeight_CuH5/emitPartition");
    model.component(cell).variable("boundv"+suffix+"")
         .set("source_CuH5", "mdotEvap*emitFraction_CuH5-mdotReturn*"+plasma+".n_wCuH5"+suffix+"/max(nCuTotal,1[1/m^3])");
    model.component(cell).variable("boundv"+suffix+"").set("emitFraction_Cu_1p", "emitWeight_Cu_1p/emitPartition");
}
static void part9(Model model,String cell,String suffix,String plasma){

    model.component(cell).variable("boundv"+suffix+"")
         .set("source_Cu_1p", "mdotEvap*emitFraction_Cu_1p-mdotReturn*"+plasma+".n_wCu"+suffix+"_1p/max(nCuTotal,1[1/m^3])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("emitMeanEnergy", "0[V]+emitFraction_Cu*0.0[V]+emitFraction_CuD5*1.388948[V]+emitFraction_CuD3*1.6422256[V]+emitFraction_CuP1*3.7858977[V]+emitFraction_CuP3*3.816692[V]+emitFraction_CuH3*6.1911751[V]+emitFraction_CuH5*6.1920252[V]+emitFraction_Cu_1p*7.72638[V]");
    model.component(cell).variable("gasv"+suffix+"")
         .set("returnMeanEnergy", "0[V]+"+plasma+".n_wCu"+suffix+"/max(nCuTotal,1[1/m^3])*0.0[V]+"+plasma+".n_wCuD5"+suffix+"/max(nCuTotal,1[1/m^3])*1.388948[V]+"+plasma+".n_wCuD3"+suffix+"/max(nCuTotal,1[1/m^3])*1.6422256[V]+"+plasma+".n_wCuP1"+suffix+"/max(nCuTotal,1[1/m^3])*3.7858977[V]+"+plasma+".n_wCuP3"+suffix+"/max(nCuTotal,1[1/m^3])*3.816692[V]+"+plasma+".n_wCuH3"+suffix+"/max(nCuTotal,1[1/m^3])*6.1911751[V]+"+plasma+".n_wCuH5"+suffix+"/max(nCuTotal,1[1/m^3])*6.1920252[V]+"+plasma+".n_wCu"+suffix+"_1p/max(nCuTotal,1[1/m^3])*7.72638[V]");
    model.component(cell).variable("boundv"+suffix+"")
         .set("electronSourceEnergy", "2*k_B_const*T*mdotEvap*N_A_const/Mcu*emitFraction_Cu_1p-2.5*k_B_const*TeK*mdotReturn*N_A_const/Mcu*nCu_1p/max(nCuTotal,1[1/m^3])");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Qmass", "mdotEvap*(Bcu+3*R_const*T/Mcu+F_const*emitMeanEnergy/Mcu)-mdotReturn*(Bcu+2.5*R_const*Tg/Mcu+F_const*returnMeanEnergy/Mcu)+electronSourceEnergy");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Qexchange", "contact*2.5*k_B_const*nHeavy*sqrt(8*k_B_const*Tg/(pi*Mair/N_A_const))/4*(Tg-T)");
    model.component(cell).variable("gasv"+suffix+"").set("Msource", "intSurf"+suffix+"(mdot)+Msweep");
    model.component(cell).variable("gasv"+suffix+"").set("Psource", "intSurf"+suffix+"(Qmass-Qexchange)");
    model.component(cell).variable("gasv"+suffix+"").set("PeSource", "intSurf"+suffix+"(electronSourceEnergy)");
    model.component(cell).physics(plasma).feature("pes1").featureInfo("info").set(""+plasma+".Vol_gm", new String[]{"Vp"});
    model.component(cell).physics(plasma).feature("pes1").featureInfo("info")
         .set(""+plasma+".mfinGm", new String[]{"Msource"});
    model.component(cell).physics(plasma).feature("pes1").featureInfo("info")
         .set(""+plasma+".Nn0", new String[]{"pAmb/(k_B_const*Tamb)"});
    model.component(cell).physics(plasma).feature("O2"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wO2"+suffix+"", new String[]{"(-"+plasma+".r_27-"+plasma+".r_28-"+plasma+".r_40-"+plasma+".r_41-"+plasma+".r_42-"+plasma+".r_43-"+plasma+".r_101+"+plasma+".r_102+"+plasma+".r_103+"+plasma+".r_105+"+plasma+".r_107+"+plasma+".r_108+2*"+plasma+".r_109+"+plasma+".r_111+"+plasma+".r_113-"+plasma+".r_120+"+plasma+".r_121-"+plasma+".r_122+"+plasma+".r_123-"+plasma+".r_124+"+plasma+".r_125)+(Msweep*(0.21*0.0319988[kg/mol]/Mair)/Vp)/"+plasma+".M_wO2"+suffix+""});
    model.component(cell).physics(plasma).feature("N"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wN"+suffix+"", new String[]{"(-"+plasma+".r_75+"+plasma+".r_76+"+plasma+".r_77+2*"+plasma+".r_99+"+plasma+".r_110+"+plasma+".r_111+2*"+plasma+".r_114-2*"+plasma+".r_115+2*"+plasma+".r_116-2*"+plasma+".r_117+2*"+plasma+".r_118-2*"+plasma+".r_119)+(0[kg/(m^3*s)])/"+plasma+".M_wN"+suffix+""});
    model.component(cell).physics(plasma).feature("O"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wO"+suffix+"", new String[]{"("+plasma+".r_27+"+plasma+".r_28+2*"+plasma+".r_40+2*"+plasma+".r_41+2*"+plasma+".r_42-"+plasma+".r_78+"+plasma+".r_79+"+plasma+".r_80+2*"+plasma+".r_100-"+plasma+".r_102+"+plasma+".r_104+"+plasma+".r_106+"+plasma+".r_108+"+plasma+".r_110+2*"+plasma+".r_112+"+plasma+".r_113+2*"+plasma+".r_120-2*"+plasma+".r_121+2*"+plasma+".r_122-2*"+plasma+".r_123+2*"+plasma+".r_124-2*"+plasma+".r_125)+(0[kg/(m^3*s)])/"+plasma+".M_wO"+suffix+""});
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wN2"+suffix+"_1p", new String[]{"("+plasma+".r_25+"+plasma+".r_26-"+plasma+".r_99-"+plasma+".r_106-"+plasma+".r_107)+(0[kg/(m^3*s)])/"+plasma+".M_wN2"+suffix+"_1p"});
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wO2"+suffix+"_1p", new String[]{"("+plasma+".r_43-"+plasma+".r_100-"+plasma+".r_108-"+plasma+".r_109)+(0[kg/(m^3*s)])/"+plasma+".M_wO2"+suffix+"_1p"});
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wN"+suffix+"_1p", new String[]{"("+plasma+".r_75-"+plasma+".r_76-"+plasma+".r_77-"+plasma+".r_110-"+plasma+".r_111)+(0[kg/(m^3*s)])/"+plasma+".M_wN"+suffix+"_1p"});
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wO"+suffix+"_1p", new String[]{"("+plasma+".r_78-"+plasma+".r_79-"+plasma+".r_80-"+plasma+".r_112-"+plasma+".r_113)+(0[kg/(m^3*s)])/"+plasma+".M_wO"+suffix+"_1p"});
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m").featureInfo("info")
         .set(""+plasma+".R_wO"+suffix+"_1m", new String[]{"("+plasma+".r_27+"+plasma+".r_28-"+plasma+".r_102-"+plasma+".r_104-"+plasma+".r_106-"+plasma+".r_108-"+plasma+".r_110-"+plasma+".r_112)+(0[kg/(m^3*s)])/"+plasma+".M_wO"+suffix+"_1m"});
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m").featureInfo("info")
         .set(""+plasma+".R_wO2"+suffix+"_1m", new String[]{"("+plasma+".r_101-"+plasma+".r_103-"+plasma+".r_105-"+plasma+".r_107-"+plasma+".r_109-"+plasma+".r_111-"+plasma+".r_113)+(0[kg/(m^3*s)])/"+plasma+".M_wO2"+suffix+"_1m"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"", new String[]{"(-"+plasma+".r_44+"+plasma+".r_45-"+plasma+".r_46+"+plasma+".r_47-"+plasma+".r_48+"+plasma+".r_49-"+plasma+".r_50+"+plasma+".r_51-"+plasma+".r_72+"+plasma+".r_73+"+plasma+".r_52+"+plasma+".r_53+"+plasma+".r_74+"+plasma+".r_104+"+plasma+".r_105)+(intSurf"+suffix+"(source_Cu)/Vp)/"+plasma+".M_wCu"+suffix+""});
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuD5"+suffix+"", new String[]{"("+plasma+".r_44-"+plasma+".r_45-"+plasma+".r_55+"+plasma+".r_56-"+plasma+".r_81+"+plasma+".r_82+"+plasma+".r_54+"+plasma+".r_83)+(intSurf"+suffix+"(source_CuD5)/Vp)/"+plasma+".M_wCuD5"+suffix+""});
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuD3"+suffix+"", new String[]{"("+plasma+".r_46-"+plasma+".r_47-"+plasma+".r_67+"+plasma+".r_68-"+plasma+".r_70+"+plasma+".r_71-"+plasma+".r_84+"+plasma+".r_85+"+plasma+".r_66+"+plasma+".r_69+"+plasma+".r_86)+(intSurf"+suffix+"(source_CuD3)/Vp)/"+plasma+".M_wCuD3"+suffix+""});
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuP1"+suffix+"", new String[]{"("+plasma+".r_48-"+plasma+".r_49-"+plasma+".r_58+"+plasma+".r_59+"+plasma+".r_70-"+plasma+".r_71-"+plasma+".r_87+"+plasma+".r_88-"+plasma+".r_53+"+plasma+".r_57-"+plasma+".r_69+"+plasma+".r_89)+(intSurf"+suffix+"(source_CuP1)/Vp)/"+plasma+".M_wCuP1"+suffix+""});
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuP3"+suffix+"", new String[]{"("+plasma+".r_50-"+plasma+".r_51+"+plasma+".r_55-"+plasma+".r_56-"+plasma+".r_61+"+plasma+".r_62-"+plasma+".r_64+"+plasma+".r_65+"+plasma+".r_67-"+plasma+".r_68-"+plasma+".r_90+"+plasma+".r_91-"+plasma+".r_52-"+plasma+".r_54+"+plasma+".r_60+"+plasma+".r_63-"+plasma+".r_66+"+plasma+".r_92)+(intSurf"+suffix+"(source_CuP3)/Vp)/"+plasma+".M_wCuP3"+suffix+""});
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuH3"+suffix+"", new String[]{"("+plasma+".r_58-"+plasma+".r_59+"+plasma+".r_64-"+plasma+".r_65-"+plasma+".r_93+"+plasma+".r_94-"+plasma+".r_57-"+plasma+".r_63+"+plasma+".r_95)+(intSurf"+suffix+"(source_CuH3)/Vp)/"+plasma+".M_wCuH3"+suffix+""});
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuH5"+suffix+"", new String[]{"("+plasma+".r_61-"+plasma+".r_62-"+plasma+".r_96+"+plasma+".r_97-"+plasma+".r_60+"+plasma+".r_98)+(intSurf"+suffix+"(source_CuH5)/Vp)/"+plasma+".M_wCuH5"+suffix+""});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_1p", new String[]{"("+plasma+".r_72-"+plasma+".r_73+"+plasma+".r_81-"+plasma+".r_82+"+plasma+".r_84-"+plasma+".r_85+"+plasma+".r_87-"+plasma+".r_88+"+plasma+".r_90-"+plasma+".r_91+"+plasma+".r_93-"+plasma+".r_94+"+plasma+".r_96-"+plasma+".r_97-"+plasma+".r_74-"+plasma+".r_83-"+plasma+".r_86-"+plasma+".r_89-"+plasma+".r_92-"+plasma+".r_95-"+plasma+".r_98-"+plasma+".r_104-"+plasma+".r_105)+(intSurf"+suffix+"(source_Cu_1p)/Vp)/"+plasma+".M_wCu"+suffix+"_1p"});
    model.component(cell).physics(plasma).feature("pes1").featureInfo("info")
         .set("root."+cell+"."+plasma+".pes1.weak$5", new String[]{"(d("+plasma+".rho,t)+"+plasma+".rho*volumeRate)*test("+plasma+".pA)"});
    model.component(cell).physics(plasma).feature("pes1")
         .set("Pgm", "Plaser+PeSource-Vp*(QeRR+QeAttach+Qff+Qei+(Ue+pe)*volumeRate)");
    model.component(cell).physics().create("ht"+suffix+"", "HeatTransfer", "g"+suffix+"");
    model.component(cell).physics("ht"+suffix).field("temperature").field("unusedTemp"+suffix);
    model.component(cell).physics("ht"+suffix+"").selection().named("g"+suffix+"_cu_dom");
    model.component(cell).physics("ht"+suffix+"").feature("init1").set("Tinit", "Tamb");
    model.component(cell).physics("ht"+suffix+"").create("laser", "HeatFluxBoundary", 1);
    model.component(cell).physics("ht"+suffix+"").feature("laser").selection().named("surface"+suffix+"");
    model.component(cell).physics("ht"+suffix+"").feature("laser").set("q0", "Qsurface-Qmass+Qexchange");
    model.component(cell).physics("ht"+suffix+"").create("base", "TemperatureBoundary", 1);
    model.component(cell).physics("ht"+suffix+"").feature("base").selection().named("bottom"+suffix+"");
    model.component(cell).physics("ht"+suffix+"").feature("base").set("T0", "Tamb");
    model.component(cell).physics().create("bulk"+suffix+"", "GlobalEquations", "g"+suffix+"");
    model.component(cell).physics("bulk"+suffix+"").feature("ge1")
         .set("name", new String[]{"logRadius", "logHeight", "pRadius", "pHeight", "gasEnergy", "logGasT", "absorbedEnergy", "radiatedEnergy", "ambientWork", "entrainedEnergy", 
         "removedMass"});
    model.component(cell).physics("bulk"+suffix+"").feature("ge1")
         .set("equation", new String[]{"logRadiust-vRadius/Rp", "logHeightt-vHeight/Hp", "pRadiust-5*(pPl-pAmb)*Vp/(Rp*momentumScale)", "pHeightt-5*(pPl-pAmb)*Vp/(Hp*momentumScale)", "gasEnergyt-(Psource+Plaser-Qrad*Vp-pAmb*Vdot+eAir*Msweep)/energyScale", "(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue)-Kinetic)/energyScale", "absorbedEnergyt-(intSurf"+suffix+"(Qsurface)+Plaser)/energyScale", "radiatedEnergyt-Qrad*Vp/energyScale", "ambientWorkt-pAmb*Vdot/energyScale", "entrainedEnergyt-eAir*Msweep/energyScale", 
         "removedMasst-intSurf"+suffix+"(mdot)/mScale"});
    model.component(cell).physics("bulk"+suffix+"").feature("ge1")
         .set("initialValueU", new String[]{"0", "0", "0", "0", "2.5*pAmb*Vinit/energyScale", "log(Tamb/1[K])", "0", "0", "0", "0", 
         "0"});
    model.component(cell).variable("gasv"+suffix+"").set("tailKu", "10^tailUp(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("tailKd", "10^tailDn(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("tailKi", "10^tailIon(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("tailK3", "10^tailRec(log10(TeV/1[V]))*1[m^6/s]");
    model.component(cell).variable("gasv"+suffix+"").set("tailKrr", "10^tailRR(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("tailMeanRR", "10^tailRRmean(log10(TeV/1[V]))*1[V]");
    model.component(cell).physics(plasma).create("nativeTailUp", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("nativeTailUp").set("formula", "e+CuP3"+suffix+"=>e+CuT5"+suffix+"");
    model.component(cell).physics(plasma).feature("nativeTailUp").set("kf", "tailKu*N_A_const");
    model.component(cell).physics(plasma).feature("nativeTailUp").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("nativeTailUp").set("de", "3.0509536");
    model.component(cell).physics(plasma).create("nativeTailDn", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("nativeTailDn").set("formula", "e+CuT5"+suffix+"=>e+CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("nativeTailDn").set("kf", "tailKd*N_A_const");
    model.component(cell).physics(plasma).feature("nativeTailDn").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("nativeTailDn").set("de", "-3.0509536");
    model.component(cell).physics(plasma).create("nativeTailIon", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("nativeTailIon").set("formula", "e+CuT5"+suffix+"=>2e+Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("nativeTailIon").set("kf", "tailKi*N_A_const");
    model.component(cell).physics(plasma).feature("nativeTailIon").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("nativeTailIon").set("de", "0.8587344");
    model.component(cell).physics(plasma).create("nativeTail3rec", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("nativeTail3rec").set("formula", "2e+Cu"+suffix+"+=>e+CuT5"+suffix+"");
    model.component(cell).physics(plasma).feature("nativeTail3rec").set("kf", "tailK3*N_A_const^2");
    model.component(cell).physics(plasma).feature("nativeTail3rec").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("nativeTail3rec").set("de", "-0.8587344");
    model.component(cell).physics(plasma).create("nativeTailRR", "Reaction", 2);
    model.component(cell).physics(plasma).feature("nativeTailRR").set("formula", "e+Cu"+suffix+"+=>CuT5"+suffix+"");
    model.component(cell).physics(plasma).feature("nativeTailRR").set("kf", "tailKrr*N_A_const");
    model.component(cell).physics(plasma).create("nativeTailRad", "Reaction", 2);
    model.component(cell).physics(plasma).feature("nativeTailRad").set("formula", "CuT5"+suffix+"=>CuP3"+suffix+"");
    model.component(cell).physics(plasma).feature("nativeTailRad").set("kf", "tailA*tailBeta");
    model.component(cell).physics(plasma).create("nativeTailPhoto", "Reaction", 2);
    model.component(cell).physics(plasma).feature("nativeTailPhoto").set("formula", "CuT5"+suffix+"=>e+Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("nativeTailPhoto").set("kf", "tailPhotoRate");
    model.component(cell).physics(plasma).feature("CuT5"+suffix+"").set("M", "Mcu");
    model.component(cell).physics(plasma).feature("CuT5"+suffix+"").set("x0", "1e-24");
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCuT5"+suffix+"", "wCuT5"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nCuT5", ""+plasma+".n_wCuT5"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("nCuTotal", "nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p+nCuT5");
    model.component(cell).variable("gasv"+suffix+"")
         .set("nHeavy", "nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p+nCuT5");
    model.component(cell).variable("gasv"+suffix+"")
         .set("CvVol", "k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)+1.5*k_B_const*nCuT5");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Uchem", "Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)+e_const*tailEnergy*nCuT5");
    model.component(cell).variable("gasv"+suffix+"").set("tailDnu", "1/(406.2638[nm])*sqrt(2*k_B_const*Tg/(Mcu/N_A_const))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("tailTau", "e_const^2/(4*epsilon0_const*me_const*c_const)*0.07794434001494746/(sqrt(pi)*tailDnu)*max(nCuP3-(4/6)*nCuT5,0[1/m^3])*Req");
    model.component(cell).variable("gasv"+suffix+"")
         .set("tailBeta", "if(tailTau<0.01,1-3*tailTau/4+2*tailTau^2/5,3/(4*tailTau)*(1-1/(2*tailTau^2)+(1/tailTau+1/(2*tailTau^2))*exp(-2*tailTau)))");
    model.component(cell).variable("gasv"+suffix+"").set("tailPhoton", "h_const*c_const/lambdaL");
    model.component(cell).variable("gasv"+suffix+"")
         .set("tailSigma", "6.3e-22[m^2]*sqrt(13.605693[V]/tailI)*(tailI*e_const/tailPhoton)^3");
    model.component(cell).variable("gasv"+suffix+"")
         .set("alphaIB", ""+plasma+".ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)");
    model.component(cell).variable("gasv"+suffix+"").set("alphaPhoto", "nCuT5*tailSigma");
    model.component(cell).variable("gasv"+suffix+"").set("alphaLaser", "alphaIB+alphaPhoto");
    model.component(cell).variable("gasv"+suffix+"")
         .set("meanLaserIntensity", "Pincident*beamCoverage*Hp/Vp*if(laserTau<1e-4,1-laserTau/2+laserTau^2/6-laserTau^3/24,(1-exp(-laserTau))/laserTau)*(1+(1-Acu)*beamTransmission)");
    model.component(cell).variable("gasv"+suffix+"").set("tailPhotoRate", "tailSigma*meanLaserIntensity/tailPhoton");
    model.component(cell).variable("gasv"+suffix+"").set("tailPhotoEvents", "nCuT5*tailPhotoRate");
    model.component(cell).variable("gasv"+suffix+"")
         .set("tailNetExcitation", ""+plasma+".ne*nCuP3*tailKu-"+plasma+".ne*nCuT5*tailKd-tailA*tailBeta*nCuT5");
    model.component(cell).variable("gasv"+suffix+"")
         .set("tailNetIonization", ""+plasma+".ne*nCuT5*tailKi-"+plasma+".ne^2*nCu_1p*tailK3-"+plasma+".ne*nCu_1p*tailKrr+tailPhotoEvents");
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuP3"+suffix+"", new String[]{"(("+plasma+".r_50-"+plasma+".r_51+"+plasma+".r_55-"+plasma+".r_56-"+plasma+".r_61+"+plasma+".r_62-"+plasma+".r_64+"+plasma+".r_65+"+plasma+".r_67-"+plasma+".r_68-"+plasma+".r_90+"+plasma+".r_91-"+plasma+".r_52-"+plasma+".r_54+"+plasma+".r_60+"+plasma+".r_63-"+plasma+".r_66+"+plasma+".r_92)+(intSurf"+suffix+"(source_CuP3)/Vp)/"+plasma+".M_wCuP3"+suffix+")-tailNetExcitation/N_A_const"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_1p", new String[]{"(("+plasma+".r_72-"+plasma+".r_73+"+plasma+".r_81-"+plasma+".r_82+"+plasma+".r_84-"+plasma+".r_85+"+plasma+".r_87-"+plasma+".r_88+"+plasma+".r_90-"+plasma+".r_91+"+plasma+".r_93-"+plasma+".r_94+"+plasma+".r_96-"+plasma+".r_97-"+plasma+".r_74-"+plasma+".r_83-"+plasma+".r_86-"+plasma+".r_89-"+plasma+".r_92-"+plasma+".r_95-"+plasma+".r_98-"+plasma+".r_104-"+plasma+".r_105)+(intSurf"+suffix+"(source_Cu_1p)/Vp)/"+plasma+".M_wCu"+suffix+"_1p)+tailNetIonization/N_A_const"});
    model.component(cell).variable("boundv"+suffix+"").set("emitWeight_CuT5", "6*exp(-tailEnergy*e_const/(k_B_const*T))");
    model.component(cell).variable("boundv"+suffix+"")
         .set("emitPartition", "0+emitWeight_Cu+emitWeight_CuD5+emitWeight_CuD3+emitWeight_CuP1+emitWeight_CuP3+emitWeight_CuH3+emitWeight_CuH5+emitWeight_Cu_1p+emitWeight_CuT5");
    model.component(cell).variable("boundv"+suffix+"").set("emitFraction_CuT5", "emitWeight_CuT5/emitPartition");
    model.component(cell).variable("boundv"+suffix+"")
         .set("emitMeanEnergy", "0[V]+emitFraction_Cu*0.0[V]+emitFraction_CuD5*1.388948[V]+emitFraction_CuD3*1.6422256[V]+emitFraction_CuP1*3.7858977[V]+emitFraction_CuP3*3.816692[V]+emitFraction_CuH3*6.1911751[V]+emitFraction_CuH5*6.1920252[V]+emitFraction_Cu_1p*7.72638[V]+emitFraction_CuT5*tailEnergy");
    model.component(cell).variable("gasv"+suffix+"")
         .set("returnMeanEnergy", "0[V]+"+plasma+".n_wCu"+suffix+"/max(nCuTotal,1[1/m^3])*0.0[V]+"+plasma+".n_wCuD5"+suffix+"/max(nCuTotal,1[1/m^3])*1.388948[V]+"+plasma+".n_wCuD3"+suffix+"/max(nCuTotal,1[1/m^3])*1.6422256[V]+"+plasma+".n_wCuP1"+suffix+"/max(nCuTotal,1[1/m^3])*3.7858977[V]+"+plasma+".n_wCuP3"+suffix+"/max(nCuTotal,1[1/m^3])*3.816692[V]+"+plasma+".n_wCuH3"+suffix+"/max(nCuTotal,1[1/m^3])*6.1911751[V]+"+plasma+".n_wCuH5"+suffix+"/max(nCuTotal,1[1/m^3])*6.1920252[V]+"+plasma+".n_wCu"+suffix+"_1p/max(nCuTotal,1[1/m^3])*7.72638[V]+nCuT5/max(nCuTotal,1[1/m^3])*tailEnergy");
    model.component(cell).variable("boundv"+suffix+"")
         .set("source_CuT5", "mdotEvap*emitFraction_CuT5-mdotReturn*nCuT5/max(nCuTotal,1[1/m^3])");
    model.component(cell).physics(plasma).feature("CuT5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuT5"+suffix+"", new String[]{"(tailNetExcitation-tailNetIonization)/N_A_const+intSurf"+suffix+"(source_CuT5)/(Vp*Mcu)"});
    model.component(cell).variable("gasv"+suffix+"").set("tailQfb", ""+plasma+".ne*nCu_1p*tailKrr*tailMeanRR*e_const");
    model.component(cell).variable("gasv"+suffix+"").set("tailQline", "tailA*tailBeta*nCuT5*tailDelta*e_const");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Qrad", "4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+Qfb_Cu+Qfb_N+Qfb_O+Qfb_CuD5+Qfb_CuD3+Qfb_CuP1+Qfb_CuP3+Qfb_CuH3+Qfb_CuH5+Qff+tailQfb+tailQline");
    model.component(cell).variable("gasv"+suffix+"")
         .set("QeRR", "r_Curr*e_const*(Eph_Curr-7.72638[V])+r_Nrr*e_const*(Eph_Nrr-14.534[V])+r_Orr*e_const*(Eph_Orr-13.618[V])+r_CuD5rr*e_const*(Eph_CuD5rr-6.337432[V])+r_CuD3rr*e_const*(Eph_CuD3rr-6.0841544[V])+r_CuP1rr*e_const*(Eph_CuP1rr-3.9404822999999998[V])+r_CuP3rr*e_const*(Eph_CuP3rr-3.9096879999999996[V])+r_CuH3rr*e_const*(Eph_CuH3rr-1.5352049[V])+r_CuH5rr*e_const*(Eph_CuH5rr-1.5343548[V])+"+plasma+".ne*nCu_1p*tailKrr*(tailMeanRR-tailI)*e_const");
    model.component(cell).physics(plasma).feature("pes1")
         .set("Pgm", "Plaser+PeSource-Vp*(QeRR+QeAttach+Qff+Qei+(Ue+pe)*volumeRate)-Vp*tailPhotoEvents*tailI*e_const");
    model.component(cell).physics(plasma).create("elasticTail", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("elasticTail").set("formula", "e+CuT5"+suffix+"=>e+CuT5"+suffix+"");
    model.component(cell).physics(plasma).feature("elasticTail").set("type", "Elastic");
    model.component(cell).physics(plasma).feature("elasticTail").set("kf", "kmCu*N_A_const");
    model.component(cell).physics(plasma).feature("elasticTail").set("mratio", "me_const*N_A_const/Mcu");
}
static void part10(Model model,String cell,String suffix,String plasma){

    model.component(cell).variable("gasv"+suffix+"")
         .set("escapeSpeedRatio", "abs((2*vRadius/Rp+vHeight/Hp)/3)*Req/sqrt(2*k_B_const*Tg/(Mcu/N_A_const))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta1", "10^logGlobalEscape(log10(max(tauLine1,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta2", "10^logGlobalEscape(log10(max(tauLine2,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta3", "10^logGlobalEscape(log10(max(tauLine3,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta4", "10^logGlobalEscape(log10(max(tauLine4,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta5", "10^logGlobalEscape(log10(max(tauLine5,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta6", "10^logGlobalEscape(log10(max(tauLine6,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta7", "10^logGlobalEscape(log10(max(tauLine7,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("beta8", "10^logGlobalEscape(log10(max(tauLine8,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("tailBeta", "10^logGlobalEscape(log10(max(tailTau,1e-7)),log10(max(escapeSpeedRatio,1e-5)))");
    model.component(cell).variable("gasv"+suffix+"").set("Rp", "Rinit");
    model.component(cell).variable("gasv"+suffix+"").set("Hp", "Hinit");
    model.component(cell).variable("gasv"+suffix+"").set("vRadius", "0[m/s]");
    model.component(cell).variable("gasv"+suffix+"").set("vHeight", "0[m/s]");
    model.component(cell).variable("gasv"+suffix+"").set("volumeRate", "0[1/s]");
    model.component(cell).variable("gasv"+suffix+"").set("Kinetic", "0[J]");
    model.component(cell).variable("gasv"+suffix+"").set("Msweep", "0[kg/s]");
    model.component(cell).variable("gasv"+suffix+"").set("nozzleArea", "2*pi*Rinit^2");
    model.component(cell).variable("gasv"+suffix+"").set("gammaNozzle", "1+(nHeavy*k_B_const+(2/3)*Ue/Tg)/(CvVol+Ue/Tg)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("nozzleRatioCritical", "(2/(gammaNozzle+1))^(gammaNozzle/(gammaNozzle-1))");
    model.component(cell).variable("gasv"+suffix+"").set("nozzleRatio", "max(nozzleRatioCritical,min(1,pAmb/pPl))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("nozzleFluxSquared", "max(2*gammaNozzle/(gammaNozzle-1)*rhoP*pPl*(nozzleRatio^(2/gammaNozzle)-nozzleRatio^((gammaNozzle+1)/gammaNozzle)),0[kg^2/(m^4*s^2)])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("outMassRate", "nozzleArea*(sqrt(nozzleFluxSquared+1e-20[kg^2/(m^4*s^2)])-1e-10[kg/(m^2*s)])");
    model.component(cell).variable("gasv"+suffix+"").set("outSpecificEnthalpy", "(CvVol*Tg+Uchem+Ue+pPl)/rhoP");
    model.component(cell).variable("gasv"+suffix+"").set("outPower", "outMassRate*outSpecificEnthalpy");
    model.component(cell).variable("gasv"+suffix+"").set("outElectronPower", "outMassRate*(Ue+pe)/rhoP");
    model.component(cell).variable("gasv"+suffix+"").set("outCuRate", "outMassRate*rhoCopper/rhoP");
    model.component(cell).variable("gasv"+suffix+"").set("Msource", "intSurf"+suffix+"(mdot)-outMassRate");
    model.component(cell).physics(plasma).feature("O2"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wO2"+suffix+"", new String[]{"((-"+plasma+".r_27-"+plasma+".r_28-"+plasma+".r_40-"+plasma+".r_41-"+plasma+".r_42-"+plasma+".r_43-"+plasma+".r_101+"+plasma+".r_102+"+plasma+".r_103+"+plasma+".r_105+"+plasma+".r_107+"+plasma+".r_108+2*"+plasma+".r_109+"+plasma+".r_111+"+plasma+".r_113-"+plasma+".r_120+"+plasma+".r_121-"+plasma+".r_122+"+plasma+".r_123-"+plasma+".r_124+"+plasma+".r_125)+(Msweep*(0.21*0.0319988[kg/mol]/Mair)/Vp)/"+plasma+".M_wO2"+suffix+")-outMassRate*"+plasma+".wO2"+suffix+"/(Vp*"+plasma+".M_wO2"+suffix+")"});
    model.component(cell).physics(plasma).feature("N"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wN"+suffix+"", new String[]{"((-"+plasma+".r_75+"+plasma+".r_76+"+plasma+".r_77+2*"+plasma+".r_99+"+plasma+".r_110+"+plasma+".r_111+2*"+plasma+".r_114-2*"+plasma+".r_115+2*"+plasma+".r_116-2*"+plasma+".r_117+2*"+plasma+".r_118-2*"+plasma+".r_119)+(0[kg/(m^3*s)])/"+plasma+".M_wN"+suffix+")-outMassRate*"+plasma+".wN"+suffix+"/(Vp*"+plasma+".M_wN"+suffix+")"});
    model.component(cell).physics(plasma).feature("O"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wO"+suffix+"", new String[]{"(("+plasma+".r_27+"+plasma+".r_28+2*"+plasma+".r_40+2*"+plasma+".r_41+2*"+plasma+".r_42-"+plasma+".r_78+"+plasma+".r_79+"+plasma+".r_80+2*"+plasma+".r_100-"+plasma+".r_102+"+plasma+".r_104+"+plasma+".r_106+"+plasma+".r_108+"+plasma+".r_110+2*"+plasma+".r_112+"+plasma+".r_113+2*"+plasma+".r_120-2*"+plasma+".r_121+2*"+plasma+".r_122-2*"+plasma+".r_123+2*"+plasma+".r_124-2*"+plasma+".r_125)+(0[kg/(m^3*s)])/"+plasma+".M_wO"+suffix+")-outMassRate*"+plasma+".wO"+suffix+"/(Vp*"+plasma+".M_wO"+suffix+")"});
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wN2"+suffix+"_1p", new String[]{"(("+plasma+".r_25+"+plasma+".r_26-"+plasma+".r_99-"+plasma+".r_106-"+plasma+".r_107)+(0[kg/(m^3*s)])/"+plasma+".M_wN2"+suffix+"_1p)-outMassRate*"+plasma+".wN2"+suffix+"_1p/(Vp*"+plasma+".M_wN2"+suffix+"_1p)"});
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wO2"+suffix+"_1p", new String[]{"(("+plasma+".r_43-"+plasma+".r_100-"+plasma+".r_108-"+plasma+".r_109)+(0[kg/(m^3*s)])/"+plasma+".M_wO2"+suffix+"_1p)-outMassRate*"+plasma+".wO2"+suffix+"_1p/(Vp*"+plasma+".M_wO2"+suffix+"_1p)"});
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wN"+suffix+"_1p", new String[]{"(("+plasma+".r_75-"+plasma+".r_76-"+plasma+".r_77-"+plasma+".r_110-"+plasma+".r_111)+(0[kg/(m^3*s)])/"+plasma+".M_wN"+suffix+"_1p)-outMassRate*"+plasma+".wN"+suffix+"_1p/(Vp*"+plasma+".M_wN"+suffix+"_1p)"});
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wO"+suffix+"_1p", new String[]{"(("+plasma+".r_78-"+plasma+".r_79-"+plasma+".r_80-"+plasma+".r_112-"+plasma+".r_113)+(0[kg/(m^3*s)])/"+plasma+".M_wO"+suffix+"_1p)-outMassRate*"+plasma+".wO"+suffix+"_1p/(Vp*"+plasma+".M_wO"+suffix+"_1p)"});
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m").featureInfo("info")
         .set(""+plasma+".R_wO"+suffix+"_1m", new String[]{"(("+plasma+".r_27+"+plasma+".r_28-"+plasma+".r_102-"+plasma+".r_104-"+plasma+".r_106-"+plasma+".r_108-"+plasma+".r_110-"+plasma+".r_112)+(0[kg/(m^3*s)])/"+plasma+".M_wO"+suffix+"_1m)-outMassRate*"+plasma+".wO"+suffix+"_1m/(Vp*"+plasma+".M_wO"+suffix+"_1m)"});
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m").featureInfo("info")
         .set(""+plasma+".R_wO2"+suffix+"_1m", new String[]{"(("+plasma+".r_101-"+plasma+".r_103-"+plasma+".r_105-"+plasma+".r_107-"+plasma+".r_109-"+plasma+".r_111-"+plasma+".r_113)+(0[kg/(m^3*s)])/"+plasma+".M_wO2"+suffix+"_1m)-outMassRate*"+plasma+".wO2"+suffix+"_1m/(Vp*"+plasma+".M_wO2"+suffix+"_1m)"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"", new String[]{"((-"+plasma+".r_44+"+plasma+".r_45-"+plasma+".r_46+"+plasma+".r_47-"+plasma+".r_48+"+plasma+".r_49-"+plasma+".r_50+"+plasma+".r_51-"+plasma+".r_72+"+plasma+".r_73+"+plasma+".r_52+"+plasma+".r_53+"+plasma+".r_74+"+plasma+".r_104+"+plasma+".r_105)+(intSurf"+suffix+"(source_Cu)/Vp)/"+plasma+".M_wCu"+suffix+")-outMassRate*"+plasma+".wCu"+suffix+"/(Vp*"+plasma+".M_wCu"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuD5"+suffix+"", new String[]{"(("+plasma+".r_44-"+plasma+".r_45-"+plasma+".r_55+"+plasma+".r_56-"+plasma+".r_81+"+plasma+".r_82+"+plasma+".r_54+"+plasma+".r_83)+(intSurf"+suffix+"(source_CuD5)/Vp)/"+plasma+".M_wCuD5"+suffix+")-outMassRate*"+plasma+".wCuD5"+suffix+"/(Vp*"+plasma+".M_wCuD5"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuD3"+suffix+"", new String[]{"(("+plasma+".r_46-"+plasma+".r_47-"+plasma+".r_67+"+plasma+".r_68-"+plasma+".r_70+"+plasma+".r_71-"+plasma+".r_84+"+plasma+".r_85+"+plasma+".r_66+"+plasma+".r_69+"+plasma+".r_86)+(intSurf"+suffix+"(source_CuD3)/Vp)/"+plasma+".M_wCuD3"+suffix+")-outMassRate*"+plasma+".wCuD3"+suffix+"/(Vp*"+plasma+".M_wCuD3"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuP1"+suffix+"", new String[]{"(("+plasma+".r_48-"+plasma+".r_49-"+plasma+".r_58+"+plasma+".r_59+"+plasma+".r_70-"+plasma+".r_71-"+plasma+".r_87+"+plasma+".r_88-"+plasma+".r_53+"+plasma+".r_57-"+plasma+".r_69+"+plasma+".r_89)+(intSurf"+suffix+"(source_CuP1)/Vp)/"+plasma+".M_wCuP1"+suffix+")-outMassRate*"+plasma+".wCuP1"+suffix+"/(Vp*"+plasma+".M_wCuP1"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuP3"+suffix+"", new String[]{"((("+plasma+".r_50-"+plasma+".r_51+"+plasma+".r_55-"+plasma+".r_56-"+plasma+".r_61+"+plasma+".r_62-"+plasma+".r_64+"+plasma+".r_65+"+plasma+".r_67-"+plasma+".r_68-"+plasma+".r_90+"+plasma+".r_91-"+plasma+".r_52-"+plasma+".r_54+"+plasma+".r_60+"+plasma+".r_63-"+plasma+".r_66+"+plasma+".r_92)+(intSurf"+suffix+"(source_CuP3)/Vp)/"+plasma+".M_wCuP3"+suffix+")-tailNetExcitation/N_A_const)-outMassRate*"+plasma+".wCuP3"+suffix+"/(Vp*"+plasma+".M_wCuP3"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuH3"+suffix+"", new String[]{"(("+plasma+".r_58-"+plasma+".r_59+"+plasma+".r_64-"+plasma+".r_65-"+plasma+".r_93+"+plasma+".r_94-"+plasma+".r_57-"+plasma+".r_63+"+plasma+".r_95)+(intSurf"+suffix+"(source_CuH3)/Vp)/"+plasma+".M_wCuH3"+suffix+")-outMassRate*"+plasma+".wCuH3"+suffix+"/(Vp*"+plasma+".M_wCuH3"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuH5"+suffix+"", new String[]{"(("+plasma+".r_61-"+plasma+".r_62-"+plasma+".r_96+"+plasma+".r_97-"+plasma+".r_60+"+plasma+".r_98)+(intSurf"+suffix+"(source_CuH5)/Vp)/"+plasma+".M_wCuH5"+suffix+")-outMassRate*"+plasma+".wCuH5"+suffix+"/(Vp*"+plasma+".M_wCuH5"+suffix+")"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_1p", new String[]{"((("+plasma+".r_72-"+plasma+".r_73+"+plasma+".r_81-"+plasma+".r_82+"+plasma+".r_84-"+plasma+".r_85+"+plasma+".r_87-"+plasma+".r_88+"+plasma+".r_90-"+plasma+".r_91+"+plasma+".r_93-"+plasma+".r_94+"+plasma+".r_96-"+plasma+".r_97-"+plasma+".r_74-"+plasma+".r_83-"+plasma+".r_86-"+plasma+".r_89-"+plasma+".r_92-"+plasma+".r_95-"+plasma+".r_98-"+plasma+".r_104-"+plasma+".r_105)+(intSurf"+suffix+"(source_Cu_1p)/Vp)/"+plasma+".M_wCu"+suffix+"_1p)+tailNetIonization/N_A_const)-outMassRate*"+plasma+".wCu"+suffix+"_1p/(Vp*"+plasma+".M_wCu"+suffix+"_1p)"});
    model.component(cell).physics(plasma).feature("CuT5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuT5"+suffix+"", new String[]{"((tailNetExcitation-tailNetIonization)/N_A_const+intSurf"+suffix+"(source_CuT5)/(Vp*Mcu))-outMassRate*"+plasma+".wCuT5"+suffix+"/(Vp*"+plasma+".M_wCuT5"+suffix+")"});
    model.component(cell).physics(plasma).feature("pes1")
         .set("Pgm", "(Plaser+PeSource-Vp*(QeRR+QeAttach+Qff+Qei+(Ue+pe)*volumeRate)-Vp*tailPhotoEvents*tailI*e_const)-outElectronPower");
    model.component(cell).physics("bulk"+suffix+"").feature("ge1")
         .set("name", new String[]{"gasEnergy", "logGasT", "absorbedEnergy", "radiatedEnergy", "ambientWork", "entrainedEnergy", "removedMass", "outMass", "outEnergy", "outCuMass"});
    model.component(cell).physics("bulk"+suffix+"").feature("ge1")
         .set("equation", new String[]{"gasEnergyt-(Psource+Plaser-Qrad*Vp-outPower)/energyScale", "(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue))/energyScale", "absorbedEnergyt-(intSurf"+suffix+"(Qsurface)+Plaser)/energyScale", "radiatedEnergyt-Qrad*Vp/energyScale", "ambientWorkt", "entrainedEnergyt", "removedMasst-intSurf"+suffix+"(mdot)/mScale", "outMasst-outMassRate/mScale", "outEnergyt-outPower/energyScale", "outCuMasst-outCuRate/mScale"});
    model.component(cell).physics("bulk"+suffix+"").feature("ge1")
         .set("initialValueU", new String[]{"2.5*pAmb*Vinit/energyScale", "log(Tamb/1[K])", "0", "0", "0", "0", "0", "0", "0", "0"});
    model.component(cell).variable("boundv"+suffix+"").set("contactInner", "flc2hs(Rinit-r,1[um])");
    model.component(cell).variable("boundv"+suffix+"").set("contactOuter", "flc2hs(outer.Rp-r,1[um])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("coveredInnerPower", "intSurf"+suffix+"(Ipeak*pulse*exp(-2*(r/w0)^2)*contactInner)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("coveredOuterPower", "intSurf"+suffix+"(Ipeak*pulse*exp(-2*(r/w0)^2)*contactOuter)");
    model.component(cell).variable("boundv"+suffix+"")
         .set("Qsurface", "Acu*Ipeak*pulse*exp(-2*(r/w0)^2)*(1-contactOuter*(1-outer.beamTransmission))*(1-contactInner*(1-beamTransmission))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Plaser", "coveredInnerPower*outer.beamTransmission*beamAbsorptance*(1+(1-Acu)*beamTransmission)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("meanLaserIntensity", "coveredInnerPower*outer.beamTransmission*Hp/Vp*if(laserTau<1e-4,1-laserTau/2+laserTau^2/6-laserTau^3/24,beamAbsorptance/laserTau)*(1+(1-Acu)*beamTransmission)");
    model.component(cell).variable("gasv"+suffix+"").set("nozzleRatio", "max(nozzleRatioCritical,min(1,outer.pPl/pPl))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("forwardMassRate", "nozzleArea*(sqrt(nozzleFluxSquared+1e-20[kg^2/(m^4*s^2)])-1e-10[kg/(m^2*s)])");
    model.component(cell).variable("gasv"+suffix+"")
         .set("reverseMassRate", "nozzleArea*(sqrt(outer.nozzleFluxSquared+1e-20[kg^2/(m^4*s^2)])-1e-10[kg/(m^2*s)])");
    model.component(cell).variable("gasv"+suffix+"").set("outMassRate", "forwardMassRate-reverseMassRate");
    model.component(cell).variable("gasv"+suffix+"")
         .set("outPower", "forwardMassRate*outSpecificEnthalpy-reverseMassRate*outer.specificEnthalpy");
    model.component(cell).variable("gasv"+suffix+"")
         .set("outElectronPower", "forwardMassRate*(Ue+pe)/rhoP-reverseMassRate*(outer.Ue+outer.pe)/outer.rhoP");
    model.component(cell).variable("gasv"+suffix+"")
         .set("outCuRate", "forwardMassRate*rhoCopper/rhoP-reverseMassRate*outer.rhoCopper/outer.rhoP");
    model.component(cell).physics(plasma).feature("O2"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wO2"+suffix+"", new String[]{"(((-"+plasma+".r_27-"+plasma+".r_28-"+plasma+".r_40-"+plasma+".r_41-"+plasma+".r_42-"+plasma+".r_43-"+plasma+".r_101+"+plasma+".r_102+"+plasma+".r_103+"+plasma+".r_105+"+plasma+".r_107+"+plasma+".r_108+2*"+plasma+".r_109+"+plasma+".r_111+"+plasma+".r_113-"+plasma+".r_120+"+plasma+".r_121-"+plasma+".r_122+"+plasma+".r_123-"+plasma+".r_124+"+plasma+".r_125)+(Msweep*(0.21*0.0319988[kg/mol]/Mair)/Vp)/"+plasma+".M_wO2"+suffix+")-outMassRate*"+plasma+".wO2"+suffix+"/(Vp*"+plasma+".M_wO2"+suffix+"))+reverseMassRate*(outer.plas2.wO2"+suffix+"-"+plasma+".wO2"+suffix+")/(Vp*"+plasma+".M_wO2"+suffix+")"});
    model.component(cell).physics(plasma).feature("N"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wN"+suffix+"", new String[]{"(((-"+plasma+".r_75+"+plasma+".r_76+"+plasma+".r_77+2*"+plasma+".r_99+"+plasma+".r_110+"+plasma+".r_111+2*"+plasma+".r_114-2*"+plasma+".r_115+2*"+plasma+".r_116-2*"+plasma+".r_117+2*"+plasma+".r_118-2*"+plasma+".r_119)+(0[kg/(m^3*s)])/"+plasma+".M_wN"+suffix+")-outMassRate*"+plasma+".wN"+suffix+"/(Vp*"+plasma+".M_wN"+suffix+"))+reverseMassRate*(outer.plas2.wN"+suffix+"-"+plasma+".wN"+suffix+")/(Vp*"+plasma+".M_wN"+suffix+")"});
    model.component(cell).physics(plasma).feature("O"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wO"+suffix+"", new String[]{"((("+plasma+".r_27+"+plasma+".r_28+2*"+plasma+".r_40+2*"+plasma+".r_41+2*"+plasma+".r_42-"+plasma+".r_78+"+plasma+".r_79+"+plasma+".r_80+2*"+plasma+".r_100-"+plasma+".r_102+"+plasma+".r_104+"+plasma+".r_106+"+plasma+".r_108+"+plasma+".r_110+2*"+plasma+".r_112+"+plasma+".r_113+2*"+plasma+".r_120-2*"+plasma+".r_121+2*"+plasma+".r_122-2*"+plasma+".r_123+2*"+plasma+".r_124-2*"+plasma+".r_125)+(0[kg/(m^3*s)])/"+plasma+".M_wO"+suffix+")-outMassRate*"+plasma+".wO"+suffix+"/(Vp*"+plasma+".M_wO"+suffix+"))+reverseMassRate*(outer.plas2.wO"+suffix+"-"+plasma+".wO"+suffix+")/(Vp*"+plasma+".M_wO"+suffix+")"});
    model.component(cell).physics(plasma).feature("N2"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wN2"+suffix+"_1p", new String[]{"((("+plasma+".r_25+"+plasma+".r_26-"+plasma+".r_99-"+plasma+".r_106-"+plasma+".r_107)+(0[kg/(m^3*s)])/"+plasma+".M_wN2"+suffix+"_1p)-outMassRate*"+plasma+".wN2"+suffix+"_1p/(Vp*"+plasma+".M_wN2"+suffix+"_1p))+reverseMassRate*(outer.plas2.wN2"+suffix+"_1p-"+plasma+".wN2"+suffix+"_1p)/(Vp*"+plasma+".M_wN2"+suffix+"_1p)"});
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wO2"+suffix+"_1p", new String[]{"((("+plasma+".r_43-"+plasma+".r_100-"+plasma+".r_108-"+plasma+".r_109)+(0[kg/(m^3*s)])/"+plasma+".M_wO2"+suffix+"_1p)-outMassRate*"+plasma+".wO2"+suffix+"_1p/(Vp*"+plasma+".M_wO2"+suffix+"_1p))+reverseMassRate*(outer.plas2.wO2"+suffix+"_1p-"+plasma+".wO2"+suffix+"_1p)/(Vp*"+plasma+".M_wO2"+suffix+"_1p)"});
    model.component(cell).physics(plasma).feature("N"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wN"+suffix+"_1p", new String[]{"((("+plasma+".r_75-"+plasma+".r_76-"+plasma+".r_77-"+plasma+".r_110-"+plasma+".r_111)+(0[kg/(m^3*s)])/"+plasma+".M_wN"+suffix+"_1p)-outMassRate*"+plasma+".wN"+suffix+"_1p/(Vp*"+plasma+".M_wN"+suffix+"_1p))+reverseMassRate*(outer.plas2.wN"+suffix+"_1p-"+plasma+".wN"+suffix+"_1p)/(Vp*"+plasma+".M_wN"+suffix+"_1p)"});
    model.component(cell).physics(plasma).feature("O"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wO"+suffix+"_1p", new String[]{"((("+plasma+".r_78-"+plasma+".r_79-"+plasma+".r_80-"+plasma+".r_112-"+plasma+".r_113)+(0[kg/(m^3*s)])/"+plasma+".M_wO"+suffix+"_1p)-outMassRate*"+plasma+".wO"+suffix+"_1p/(Vp*"+plasma+".M_wO"+suffix+"_1p))+reverseMassRate*(outer.plas2.wO"+suffix+"_1p-"+plasma+".wO"+suffix+"_1p)/(Vp*"+plasma+".M_wO"+suffix+"_1p)"});
    model.component(cell).physics(plasma).feature("O"+suffix+"_1m").featureInfo("info")
         .set(""+plasma+".R_wO"+suffix+"_1m", new String[]{"((("+plasma+".r_27+"+plasma+".r_28-"+plasma+".r_102-"+plasma+".r_104-"+plasma+".r_106-"+plasma+".r_108-"+plasma+".r_110-"+plasma+".r_112)+(0[kg/(m^3*s)])/"+plasma+".M_wO"+suffix+"_1m)-outMassRate*"+plasma+".wO"+suffix+"_1m/(Vp*"+plasma+".M_wO"+suffix+"_1m))+reverseMassRate*(outer.plas2.wO"+suffix+"_1m-"+plasma+".wO"+suffix+"_1m)/(Vp*"+plasma+".M_wO"+suffix+"_1m)"});
    model.component(cell).physics(plasma).feature("O2"+suffix+"_1m").featureInfo("info")
         .set(""+plasma+".R_wO2"+suffix+"_1m", new String[]{"((("+plasma+".r_101-"+plasma+".r_103-"+plasma+".r_105-"+plasma+".r_107-"+plasma+".r_109-"+plasma+".r_111-"+plasma+".r_113)+(0[kg/(m^3*s)])/"+plasma+".M_wO2"+suffix+"_1m)-outMassRate*"+plasma+".wO2"+suffix+"_1m/(Vp*"+plasma+".M_wO2"+suffix+"_1m))+reverseMassRate*(outer.plas2.wO2"+suffix+"_1m-"+plasma+".wO2"+suffix+"_1m)/(Vp*"+plasma+".M_wO2"+suffix+"_1m)"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"", new String[]{"(((-"+plasma+".r_44+"+plasma+".r_45-"+plasma+".r_46+"+plasma+".r_47-"+plasma+".r_48+"+plasma+".r_49-"+plasma+".r_50+"+plasma+".r_51-"+plasma+".r_72+"+plasma+".r_73+"+plasma+".r_52+"+plasma+".r_53+"+plasma+".r_74+"+plasma+".r_104+"+plasma+".r_105)+(intSurf"+suffix+"(source_Cu)/Vp)/"+plasma+".M_wCu"+suffix+")-outMassRate*"+plasma+".wCu"+suffix+"/(Vp*"+plasma+".M_wCu"+suffix+"))+reverseMassRate*(outer.plas2.wCu"+suffix+"-"+plasma+".wCu"+suffix+")/(Vp*"+plasma+".M_wCu"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuD5"+suffix+"", new String[]{"((("+plasma+".r_44-"+plasma+".r_45-"+plasma+".r_55+"+plasma+".r_56-"+plasma+".r_81+"+plasma+".r_82+"+plasma+".r_54+"+plasma+".r_83)+(intSurf"+suffix+"(source_CuD5)/Vp)/"+plasma+".M_wCuD5"+suffix+")-outMassRate*"+plasma+".wCuD5"+suffix+"/(Vp*"+plasma+".M_wCuD5"+suffix+"))+reverseMassRate*(outer.plas2.wCuD5"+suffix+"-"+plasma+".wCuD5"+suffix+")/(Vp*"+plasma+".M_wCuD5"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuD3"+suffix+"", new String[]{"((("+plasma+".r_46-"+plasma+".r_47-"+plasma+".r_67+"+plasma+".r_68-"+plasma+".r_70+"+plasma+".r_71-"+plasma+".r_84+"+plasma+".r_85+"+plasma+".r_66+"+plasma+".r_69+"+plasma+".r_86)+(intSurf"+suffix+"(source_CuD3)/Vp)/"+plasma+".M_wCuD3"+suffix+")-outMassRate*"+plasma+".wCuD3"+suffix+"/(Vp*"+plasma+".M_wCuD3"+suffix+"))+reverseMassRate*(outer.plas2.wCuD3"+suffix+"-"+plasma+".wCuD3"+suffix+")/(Vp*"+plasma+".M_wCuD3"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuP1"+suffix+"", new String[]{"((("+plasma+".r_48-"+plasma+".r_49-"+plasma+".r_58+"+plasma+".r_59+"+plasma+".r_70-"+plasma+".r_71-"+plasma+".r_87+"+plasma+".r_88-"+plasma+".r_53+"+plasma+".r_57-"+plasma+".r_69+"+plasma+".r_89)+(intSurf"+suffix+"(source_CuP1)/Vp)/"+plasma+".M_wCuP1"+suffix+")-outMassRate*"+plasma+".wCuP1"+suffix+"/(Vp*"+plasma+".M_wCuP1"+suffix+"))+reverseMassRate*(outer.plas2.wCuP1"+suffix+"-"+plasma+".wCuP1"+suffix+")/(Vp*"+plasma+".M_wCuP1"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuP3"+suffix+"", new String[]{"(((("+plasma+".r_50-"+plasma+".r_51+"+plasma+".r_55-"+plasma+".r_56-"+plasma+".r_61+"+plasma+".r_62-"+plasma+".r_64+"+plasma+".r_65+"+plasma+".r_67-"+plasma+".r_68-"+plasma+".r_90+"+plasma+".r_91-"+plasma+".r_52-"+plasma+".r_54+"+plasma+".r_60+"+plasma+".r_63-"+plasma+".r_66+"+plasma+".r_92)+(intSurf"+suffix+"(source_CuP3)/Vp)/"+plasma+".M_wCuP3"+suffix+")-tailNetExcitation/N_A_const)-outMassRate*"+plasma+".wCuP3"+suffix+"/(Vp*"+plasma+".M_wCuP3"+suffix+"))+reverseMassRate*(outer.plas2.wCuP3"+suffix+"-"+plasma+".wCuP3"+suffix+")/(Vp*"+plasma+".M_wCuP3"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuH3"+suffix+"", new String[]{"((("+plasma+".r_58-"+plasma+".r_59+"+plasma+".r_64-"+plasma+".r_65-"+plasma+".r_93+"+plasma+".r_94-"+plasma+".r_57-"+plasma+".r_63+"+plasma+".r_95)+(intSurf"+suffix+"(source_CuH3)/Vp)/"+plasma+".M_wCuH3"+suffix+")-outMassRate*"+plasma+".wCuH3"+suffix+"/(Vp*"+plasma+".M_wCuH3"+suffix+"))+reverseMassRate*(outer.plas2.wCuH3"+suffix+"-"+plasma+".wCuH3"+suffix+")/(Vp*"+plasma+".M_wCuH3"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuH5"+suffix+"", new String[]{"((("+plasma+".r_61-"+plasma+".r_62-"+plasma+".r_96+"+plasma+".r_97-"+plasma+".r_60+"+plasma+".r_98)+(intSurf"+suffix+"(source_CuH5)/Vp)/"+plasma+".M_wCuH5"+suffix+")-outMassRate*"+plasma+".wCuH5"+suffix+"/(Vp*"+plasma+".M_wCuH5"+suffix+"))+reverseMassRate*(outer.plas2.wCuH5"+suffix+"-"+plasma+".wCuH5"+suffix+")/(Vp*"+plasma+".M_wCuH5"+suffix+")"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_1p", new String[]{"(((("+plasma+".r_72-"+plasma+".r_73+"+plasma+".r_81-"+plasma+".r_82+"+plasma+".r_84-"+plasma+".r_85+"+plasma+".r_87-"+plasma+".r_88+"+plasma+".r_90-"+plasma+".r_91+"+plasma+".r_93-"+plasma+".r_94+"+plasma+".r_96-"+plasma+".r_97-"+plasma+".r_74-"+plasma+".r_83-"+plasma+".r_86-"+plasma+".r_89-"+plasma+".r_92-"+plasma+".r_95-"+plasma+".r_98-"+plasma+".r_104-"+plasma+".r_105)+(intSurf"+suffix+"(source_Cu_1p)/Vp)/"+plasma+".M_wCu"+suffix+"_1p)+tailNetIonization/N_A_const)-outMassRate*"+plasma+".wCu"+suffix+"_1p/(Vp*"+plasma+".M_wCu"+suffix+"_1p))+reverseMassRate*(outer.plas2.wCu"+suffix+"_1p-"+plasma+".wCu"+suffix+"_1p)/(Vp*"+plasma+".M_wCu"+suffix+"_1p)"});
    model.component(cell).physics(plasma).feature("CuT5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuT5"+suffix+"", new String[]{"(((tailNetExcitation-tailNetIonization)/N_A_const+intSurf"+suffix+"(source_CuT5)/(Vp*Mcu))-outMassRate*"+plasma+".wCuT5"+suffix+"/(Vp*"+plasma+".M_wCuT5"+suffix+"))+reverseMassRate*(outer.plas2.wCuT5"+suffix+"-"+plasma+".wCuT5"+suffix+")/(Vp*"+plasma+".M_wCuT5"+suffix+")"});
    model.component(cell).physics("bulk"+suffix+"").feature("ge1")
         .set("name", new String[]{"gasEnergy", "logGasT", "absorbedEnergy", "radiatedEnergy", "ambientWork", "entrainedEnergy", "removedMass", "outMass", "outEnergy", "outCuMass"});
    model.component(cell).physics("bulk"+suffix+"").feature("ge1")
         .set("equation", new String[]{"gasEnergyt-(Psource+Plaser-Qrad*Vp-outPower)/energyScale", "(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue))/energyScale", "absorbedEnergyt-(intSurf"+suffix+"(Qsurface)+Plaser+outer.Plaser)/energyScale", "radiatedEnergyt-(Qrad*Vp+outer.Qrad*outer.Vp)/energyScale", "ambientWorkt-pAmb*outer.Vdot/energyScale", "entrainedEnergyt-eAir*outer.Msweep/energyScale", "removedMasst-intSurf"+suffix+"(mdot)/mScale", "outMasst-outMassRate/mScale", "outEnergyt-outPower/energyScale", "outCuMasst-outCuRate/mScale"});
    model.component(cell).physics("bulk"+suffix+"").feature("ge1")
         .set("initialValueU", new String[]{"2.5*pAmb*Vinit/energyScale", "log(Tamb/1[K])", "0", "0", "0", "0", "0", "0", "0", "0"});
    model.component(cell).variable("gasv"+suffix+"")
         .set("pulse", "0.5*(erf((t-pulseStart)/pulseEdge)-erf((t-pulseStart-tau)/pulseEdge))");
    model.component(cell).variable("gasv"+suffix+"").set("Pincident", "Ein/tau*pulse");
    model.component(cell).variable("gasv"+suffix+"").set("Ipeak", "2*Ein/(pi*w0^2*tau)");
    model.component(cell).variable("gasv"+suffix+"").set("multCuIon1", "10^cu1_ion(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("multCuRec1", "10^cu1_rec(log10(TeV/1[V]))*1[m^6/s]");
    model.component(cell).variable("gasv"+suffix+"").set("multCuRR1", "10^cu1_rr(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("multCuMean1", "10^cu1_mean(log10(TeV/1[V]))*1[V]");
    model.component(cell).physics(plasma).create("multIon1", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("multIon1").set("formula", "e+Cu"+suffix+"+=>2e+Cu"+suffix+"++");
    model.component(cell).physics(plasma).feature("multIon1").set("kf", "multCuIon1*N_A_const");
    model.component(cell).physics(plasma).feature("multIon1").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("multIon1").set("de", "20.29239");
    model.component(cell).physics(plasma).create("multRec1", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("multRec1").set("formula", "2e+Cu"+suffix+"++=>e+Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("multRec1").set("kf", "multCuRec1*N_A_const^2");
    model.component(cell).physics(plasma).feature("multRec1").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("multRec1").set("de", "-20.29239");
    model.component(cell).physics(plasma).create("multRR1", "Reaction", 2);
    model.component(cell).physics(plasma).feature("multRR1").set("formula", "e+Cu"+suffix+"++=>Cu"+suffix+"+");
    model.component(cell).physics(plasma).feature("multRR1").set("kf", "multCuRR1*N_A_const");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_2p").set("M", "Mcu");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_2p").set("n0", "1e-20[1/m^3]");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_2p").set("x0", "1e-40");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_2p").set("InitIon", false);
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCu"+suffix+"_2p", "wCu"+suffix+"_2p");
    model.component(cell).variable("gasv"+suffix+"").set("nCu2", ""+plasma+".n_wCu"+suffix+"_2p");
    model.component(cell).variable("gasv"+suffix+"")
         .set("multNet1", ""+plasma+".ne*"+plasma+".n_wCu"+suffix+"_1p*multCuIon1-"+plasma+".ne^2*nCu2*multCuRec1-"+plasma+".ne*nCu2*multCuRR1");
    model.component(cell).variable("gasv"+suffix+"")
         .set("nCuTotal", "(nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p+nCuT5)+(nCu2)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("nHeavy", "(nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p+nCuT5)+(nCu2)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("CvVol", "(k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)+1.5*k_B_const*nCuT5)+(1.5*k_B_const*nCu2)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Uchem", "(Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)+e_const*tailEnergy*nCuT5)+(e_const*28.01877[V]*nCu2)");
    model.component(cell).variable("gasv"+suffix+"").set("niZ2", "(nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p)+(4*nCu2)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Qei", "(3*me_const*N_A_const*"+plasma+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*(0[mol/(kg*m^3)]+"+plasma+".n_wN2"+suffix+"_1p/"+plasma+".M_wN2"+suffix+"_1p+"+plasma+".n_wO2"+suffix+"_1p/"+plasma+".M_wO2"+suffix+"_1p+"+plasma+".n_wN"+suffix+"_1p/"+plasma+".M_wN"+suffix+"_1p+"+plasma+".n_wO"+suffix+"_1p/"+plasma+".M_wO"+suffix+"_1p+"+plasma+".n_wO"+suffix+"_1m/"+plasma+".M_wO"+suffix+"_1m+"+plasma+".n_wO2"+suffix+"_1m/"+plasma+".M_wO2"+suffix+"_1m+"+plasma+".n_wCu"+suffix+"_1p/"+plasma+".M_wCu"+suffix+"_1p))+(3*me_const*N_A_const*"+plasma+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*4*nCu2/Mcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Qrad", "(4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+Qfb_Cu+Qfb_N+Qfb_O+Qfb_CuD5+Qfb_CuD3+Qfb_CuP1+Qfb_CuP3+Qfb_CuH3+Qfb_CuH5+Qff+tailQfb+tailQline)+("+plasma+".ne*nCu2*multCuRR1*multCuMean1*e_const)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("QeRR", "(r_Curr*e_const*(Eph_Curr-7.72638[V])+r_Nrr*e_const*(Eph_Nrr-14.534[V])+r_Orr*e_const*(Eph_Orr-13.618[V])+r_CuD5rr*e_const*(Eph_CuD5rr-6.337432[V])+r_CuD3rr*e_const*(Eph_CuD3rr-6.0841544[V])+r_CuP1rr*e_const*(Eph_CuP1rr-3.9404822999999998[V])+r_CuP3rr*e_const*(Eph_CuP3rr-3.9096879999999996[V])+r_CuH3rr*e_const*(Eph_CuH3rr-1.5352049[V])+r_CuH5rr*e_const*(Eph_CuH5rr-1.5343548[V])+"+plasma+".ne*nCu_1p*tailKrr*(tailMeanRR-tailI)*e_const)+("+plasma+".ne*nCu2*multCuRR1*(multCuMean1-20.29239[V])*e_const)");
}
static void part11(Model model,String cell,String suffix,String plasma){

    model.component(cell).variable("gasv"+suffix+"").set("multCuIon2", "10^cu2_ion(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("multCuRec2", "10^cu2_rec(log10(TeV/1[V]))*1[m^6/s]");
    model.component(cell).variable("gasv"+suffix+"").set("multCuRR2", "10^cu2_rr(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("multCuMean2", "10^cu2_mean(log10(TeV/1[V]))*1[V]");
    model.component(cell).physics(plasma).create("multIon2", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("multIon2").set("formula", "e+Cu"+suffix+"++=>2e+Cu"+suffix+"+++");
    model.component(cell).physics(plasma).feature("multIon2").set("kf", "multCuIon2*N_A_const");
    model.component(cell).physics(plasma).feature("multIon2").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("multIon2").set("de", "36.841");
    model.component(cell).physics(plasma).create("multRec2", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("multRec2").set("formula", "2e+Cu"+suffix+"+++=>e+Cu"+suffix+"++");
    model.component(cell).physics(plasma).feature("multRec2").set("kf", "multCuRec2*N_A_const^2");
    model.component(cell).physics(plasma).feature("multRec2").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("multRec2").set("de", "-36.841");
    model.component(cell).physics(plasma).create("multRR2", "Reaction", 2);
    model.component(cell).physics(plasma).feature("multRR2").set("formula", "e+Cu"+suffix+"+++=>Cu"+suffix+"++");
    model.component(cell).physics(plasma).feature("multRR2").set("kf", "multCuRR2*N_A_const");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_3p").set("M", "Mcu");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_3p").set("n0", "1e-20[1/m^3]");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_3p").set("x0", "1e-40");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_3p").set("InitIon", false);
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCu"+suffix+"_3p", "wCu"+suffix+"_3p");
    model.component(cell).variable("gasv"+suffix+"").set("nCu3", ""+plasma+".n_wCu"+suffix+"_3p");
    model.component(cell).variable("gasv"+suffix+"")
         .set("multNet2", ""+plasma+".ne*"+plasma+".n_wCu"+suffix+"_2p*multCuIon2-"+plasma+".ne^2*nCu3*multCuRec2-"+plasma+".ne*nCu3*multCuRR2");
    model.component(cell).variable("gasv"+suffix+"")
         .set("nCuTotal", "((nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p+nCuT5)+(nCu2))+(nCu3)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("nHeavy", "((nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p+nCuT5)+(nCu2))+(nCu3)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("CvVol", "((k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)+1.5*k_B_const*nCuT5)+(1.5*k_B_const*nCu2))+(1.5*k_B_const*nCu3)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Uchem", "((Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)+e_const*tailEnergy*nCuT5)+(e_const*28.01877[V]*nCu2))+(e_const*64.85977[V]*nCu3)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("niZ2", "((nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p)+(4*nCu2))+(9*nCu3)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Qei", "((3*me_const*N_A_const*"+plasma+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*(0[mol/(kg*m^3)]+"+plasma+".n_wN2"+suffix+"_1p/"+plasma+".M_wN2"+suffix+"_1p+"+plasma+".n_wO2"+suffix+"_1p/"+plasma+".M_wO2"+suffix+"_1p+"+plasma+".n_wN"+suffix+"_1p/"+plasma+".M_wN"+suffix+"_1p+"+plasma+".n_wO"+suffix+"_1p/"+plasma+".M_wO"+suffix+"_1p+"+plasma+".n_wO"+suffix+"_1m/"+plasma+".M_wO"+suffix+"_1m+"+plasma+".n_wO2"+suffix+"_1m/"+plasma+".M_wO2"+suffix+"_1m+"+plasma+".n_wCu"+suffix+"_1p/"+plasma+".M_wCu"+suffix+"_1p))+(3*me_const*N_A_const*"+plasma+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*4*nCu2/Mcu))+(3*me_const*N_A_const*"+plasma+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*9*nCu3/Mcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Qrad", "((4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+Qfb_Cu+Qfb_N+Qfb_O+Qfb_CuD5+Qfb_CuD3+Qfb_CuP1+Qfb_CuP3+Qfb_CuH3+Qfb_CuH5+Qff+tailQfb+tailQline)+("+plasma+".ne*nCu2*multCuRR1*multCuMean1*e_const))+("+plasma+".ne*nCu3*multCuRR2*multCuMean2*e_const)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("QeRR", "((r_Curr*e_const*(Eph_Curr-7.72638[V])+r_Nrr*e_const*(Eph_Nrr-14.534[V])+r_Orr*e_const*(Eph_Orr-13.618[V])+r_CuD5rr*e_const*(Eph_CuD5rr-6.337432[V])+r_CuD3rr*e_const*(Eph_CuD3rr-6.0841544[V])+r_CuP1rr*e_const*(Eph_CuP1rr-3.9404822999999998[V])+r_CuP3rr*e_const*(Eph_CuP3rr-3.9096879999999996[V])+r_CuH3rr*e_const*(Eph_CuH3rr-1.5352049[V])+r_CuH5rr*e_const*(Eph_CuH5rr-1.5343548[V])+"+plasma+".ne*nCu_1p*tailKrr*(tailMeanRR-tailI)*e_const)+("+plasma+".ne*nCu2*multCuRR1*(multCuMean1-20.29239[V])*e_const))+("+plasma+".ne*nCu3*multCuRR2*(multCuMean2-36.841[V])*e_const)");
    model.component(cell).variable("gasv"+suffix+"").set("multCuIon3", "10^cu3_ion(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("multCuRec3", "10^cu3_rec(log10(TeV/1[V]))*1[m^6/s]");
    model.component(cell).variable("gasv"+suffix+"").set("multCuRR3", "10^cu3_rr(log10(TeV/1[V]))*1[m^3/s]");
    model.component(cell).variable("gasv"+suffix+"").set("multCuMean3", "10^cu3_mean(log10(TeV/1[V]))*1[V]");
    model.component(cell).physics(plasma).create("multIon3", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("multIon3").set("formula", "e+Cu"+suffix+"+++=>2e+Cu"+suffix+"++++");
    model.component(cell).physics(plasma).feature("multIon3").set("kf", "multCuIon3*N_A_const");
    model.component(cell).physics(plasma).feature("multIon3").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("multIon3").set("de", "57.38");
    model.component(cell).physics(plasma).create("multRec3", "ElectronImpactReaction", 2);
    model.component(cell).physics(plasma).feature("multRec3").set("formula", "2e+Cu"+suffix+"++++=>e+Cu"+suffix+"+++");
    model.component(cell).physics(plasma).feature("multRec3").set("kf", "multCuRec3*N_A_const^2");
    model.component(cell).physics(plasma).feature("multRec3").set("type", "Excitation");
    model.component(cell).physics(plasma).feature("multRec3").set("de", "-57.38");
    model.component(cell).physics(plasma).create("multRR3", "Reaction", 2);
    model.component(cell).physics(plasma).feature("multRR3").set("formula", "e+Cu"+suffix+"++++=>Cu"+suffix+"+++");
    model.component(cell).physics(plasma).feature("multRR3").set("kf", "multCuRR3*N_A_const");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_4p").set("M", "Mcu");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_4p").set("n0", "1e-20[1/m^3]");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_4p").set("x0", "1e-40");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_4p").set("InitIon", false);
    model.component(cell).variable("gasv"+suffix+"").set(""+plasma+".wCu"+suffix+"_4p", "wCu"+suffix+"_4p");
    model.component(cell).variable("gasv"+suffix+"").set("nCu4", ""+plasma+".n_wCu"+suffix+"_4p");
    model.component(cell).variable("gasv"+suffix+"")
         .set("multNet3", ""+plasma+".ne*"+plasma+".n_wCu"+suffix+"_3p*multCuIon3-"+plasma+".ne^2*nCu4*multCuRec3-"+plasma+".ne*nCu4*multCuRR3");
    model.component(cell).variable("gasv"+suffix+"")
         .set("nCuTotal", "(((nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p+nCuT5)+(nCu2))+(nCu3))+(nCu4)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("nHeavy", "(((nN2+nO2+nN+nO+nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu+nCuD5+nCuD3+nCuP1+nCuP3+nCuH3+nCuH5+nCu_1p+nCuT5)+(nCu2))+(nCu3))+(nCu4)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("CvVol", "(((k_B_const*(2.5*nN2+2.5*nO2+1.5*nN+1.5*nO+2.5*nN2_1p+2.5*nO2_1p+1.5*nN_1p+1.5*nO_1p+1.5*nO_1m+2.5*nO2_1m+1.5*nCu+1.5*nCuD5+1.5*nCuD3+1.5*nCuP1+1.5*nCuP3+1.5*nCuH3+1.5*nCuH5+1.5*nCu_1p)+1.5*k_B_const*nCuT5)+(1.5*k_B_const*nCu2))+(1.5*k_B_const*nCu3))+(1.5*k_B_const*nCu4)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Uchem", "(((Bcu*rhoCopper+e_const*(4.88[V]*nN+2.58[V]*nO+15.58[V]*nN2_1p+12.06[V]*nO2_1p+19.414[V]*nN_1p+16.198[V]*nO_1p+1.119[V]*nO_1m+-0.45[V]*nO2_1m+1.388948[V]*nCuD5+1.6422256[V]*nCuD3+3.7858977[V]*nCuP1+3.816692[V]*nCuP3+6.1911751[V]*nCuH3+6.1920252[V]*nCuH5+7.72638[V]*nCu_1p)+e_const*tailEnergy*nCuT5)+(e_const*28.01877[V]*nCu2))+(e_const*64.85977[V]*nCu3))+(e_const*122.23977[V]*nCu4)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("niZ2", "(((nN2_1p+nO2_1p+nN_1p+nO_1p+nO_1m+nO2_1m+nCu_1p)+(4*nCu2))+(9*nCu3))+(16*nCu4)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Qei", "(((3*me_const*N_A_const*"+plasma+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*(0[mol/(kg*m^3)]+"+plasma+".n_wN2"+suffix+"_1p/"+plasma+".M_wN2"+suffix+"_1p+"+plasma+".n_wO2"+suffix+"_1p/"+plasma+".M_wO2"+suffix+"_1p+"+plasma+".n_wN"+suffix+"_1p/"+plasma+".M_wN"+suffix+"_1p+"+plasma+".n_wO"+suffix+"_1p/"+plasma+".M_wO"+suffix+"_1p+"+plasma+".n_wO"+suffix+"_1m/"+plasma+".M_wO"+suffix+"_1m+"+plasma+".n_wO2"+suffix+"_1m/"+plasma+".M_wO2"+suffix+"_1m+"+plasma+".n_wCu"+suffix+"_1p/"+plasma+".M_wCu"+suffix+"_1p))+(3*me_const*N_A_const*"+plasma+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*4*nCu2/Mcu))+(3*me_const*N_A_const*"+plasma+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*9*nCu3/Mcu))+(3*me_const*N_A_const*"+plasma+".ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*16*nCu4/Mcu)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Qrad", "(((4*pi*jline1*beta1+4*pi*jline2*beta2+4*pi*jline3*beta3+4*pi*jline4*beta4+4*pi*jline5*beta5+4*pi*jline6*beta6+4*pi*jline7*beta7+4*pi*jline8*beta8+Qfb_Cu+Qfb_N+Qfb_O+Qfb_CuD5+Qfb_CuD3+Qfb_CuP1+Qfb_CuP3+Qfb_CuH3+Qfb_CuH5+Qff+tailQfb+tailQline)+("+plasma+".ne*nCu2*multCuRR1*multCuMean1*e_const))+("+plasma+".ne*nCu3*multCuRR2*multCuMean2*e_const))+("+plasma+".ne*nCu4*multCuRR3*multCuMean3*e_const)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("QeRR", "(((r_Curr*e_const*(Eph_Curr-7.72638[V])+r_Nrr*e_const*(Eph_Nrr-14.534[V])+r_Orr*e_const*(Eph_Orr-13.618[V])+r_CuD5rr*e_const*(Eph_CuD5rr-6.337432[V])+r_CuD3rr*e_const*(Eph_CuD3rr-6.0841544[V])+r_CuP1rr*e_const*(Eph_CuP1rr-3.9404822999999998[V])+r_CuP3rr*e_const*(Eph_CuP3rr-3.9096879999999996[V])+r_CuH3rr*e_const*(Eph_CuH3rr-1.5352049[V])+r_CuH5rr*e_const*(Eph_CuH5rr-1.5343548[V])+"+plasma+".ne*nCu_1p*tailKrr*(tailMeanRR-tailI)*e_const)+("+plasma+".ne*nCu2*multCuRR1*(multCuMean1-20.29239[V])*e_const))+("+plasma+".ne*nCu3*multCuRR2*(multCuMean2-36.841[V])*e_const))+("+plasma+".ne*nCu4*multCuRR3*(multCuMean3-57.38[V])*e_const)");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_1p", new String[]{"((((("+plasma+".r_72-"+plasma+".r_73+"+plasma+".r_81-"+plasma+".r_82+"+plasma+".r_84-"+plasma+".r_85+"+plasma+".r_87-"+plasma+".r_88+"+plasma+".r_90-"+plasma+".r_91+"+plasma+".r_93-"+plasma+".r_94+"+plasma+".r_96-"+plasma+".r_97-"+plasma+".r_74-"+plasma+".r_83-"+plasma+".r_86-"+plasma+".r_89-"+plasma+".r_92-"+plasma+".r_95-"+plasma+".r_98-"+plasma+".r_104-"+plasma+".r_105)+(intSurf"+suffix+"(source_Cu_1p)/Vp)/"+plasma+".M_wCu"+suffix+"_1p)+tailNetIonization/N_A_const)-outMassRate*"+plasma+".wCu"+suffix+"_1p/(Vp*"+plasma+".M_wCu"+suffix+"_1p))+reverseMassRate*(outer.plas2.wCu"+suffix+"_1p-"+plasma+".wCu"+suffix+"_1p)/(Vp*"+plasma+".M_wCu"+suffix+"_1p))-multNet1/N_A_const"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_2p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_2p", new String[]{"(multNet1-multNet2)/N_A_const+(reverseMassRate*outer.plas2.wCuX_2p-forwardMassRate*"+plasma+".wCu"+suffix+"_2p)/(Vp*Mcu)-intSurf"+suffix+"(mdotReturn*nCu2/nCuTotal)/(Vp*Mcu)"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_3p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_3p", new String[]{"(multNet2-multNet3)/N_A_const+(reverseMassRate*outer.plas2.wCuX_3p-forwardMassRate*"+plasma+".wCu"+suffix+"_3p)/(Vp*Mcu)-intSurf"+suffix+"(mdotReturn*nCu3/nCuTotal)/(Vp*Mcu)"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_4p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_4p", new String[]{"(multNet3)/N_A_const+(reverseMassRate*outer.plas2.wCuX_4p-forwardMassRate*"+plasma+".wCu"+suffix+"_4p)/(Vp*Mcu)-intSurf"+suffix+"(mdotReturn*nCu4/nCuTotal)/(Vp*Mcu)"});
    model.component(cell).variable("gasv"+suffix+"")
         .set("returnMeanEnergy", "(((0[V]+"+plasma+".n_wCu"+suffix+"/max(nCuTotal,1[1/m^3])*0.0[V]+"+plasma+".n_wCuD5"+suffix+"/max(nCuTotal,1[1/m^3])*1.388948[V]+"+plasma+".n_wCuD3"+suffix+"/max(nCuTotal,1[1/m^3])*1.6422256[V]+"+plasma+".n_wCuP1"+suffix+"/max(nCuTotal,1[1/m^3])*3.7858977[V]+"+plasma+".n_wCuP3"+suffix+"/max(nCuTotal,1[1/m^3])*3.816692[V]+"+plasma+".n_wCuH3"+suffix+"/max(nCuTotal,1[1/m^3])*6.1911751[V]+"+plasma+".n_wCuH5"+suffix+"/max(nCuTotal,1[1/m^3])*6.1920252[V]+"+plasma+".n_wCu"+suffix+"_1p/max(nCuTotal,1[1/m^3])*7.72638[V]+nCuT5/max(nCuTotal,1[1/m^3])*tailEnergy)+(28.01877[V]*nCu2/nCuTotal))+(64.85977[V]*nCu3/nCuTotal))+(122.23977[V]*nCu4/nCuTotal)");
    model.component(cell).variable("boundv"+suffix+"")
         .set("electronSourceEnergy", "(2*k_B_const*T*mdotEvap*N_A_const/Mcu*emitFraction_Cu_1p-2.5*k_B_const*TeK*mdotReturn*N_A_const/Mcu*nCu_1p/max(nCuTotal,1[1/m^3]))-2.5*k_B_const*TeK*mdotReturn*N_A_const/Mcu*(2*nCu2+3*nCu3+4*nCu4)/nCuTotal");
    model.component(cell).physics().remove("ht"+suffix+"");
    model.component(cell).material().remove("copper"+suffix+"");
    model.component(cell).variable("gasv"+suffix+"").set("thermalSourceMode", "1");
    model.component(cell).variable("gasv"+suffix+"").set("T", "Tamb");
    model.component(cell).variable("gasv"+suffix+"").set("Tr", "0[K/m]");
    model.component(cell).variable("gasv"+suffix+"").set("Tz", "0[K/m]");
    model.component(cell).variable("gasv"+suffix+"").set("prescribedTargetK", "thermalTargetK(t/1[s])*1[K]");
    model.component(cell).variable("gasv"+suffix+"").set("prescribedTargetAbsorption", "thermalTargetAbsorption(t/1[s])*1[W]");
    model.component(cell).variable("gasv"+suffix+"").set("Msource", "thermalMass(t/1[s])*1[kg/s]-outMassRate");
    model.component(cell).variable("gasv"+suffix+"").set("Psource", "thermalPower(t/1[s])*1[W]");
    model.component(cell).variable("gasv"+suffix+"").set("PeSource", "thermalElectron(t/1[s])*1[W]");
    model.component(cell).physics(plasma).feature("Cu"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"", new String[]{"(((-"+plasma+".r_44+"+plasma+".r_45-"+plasma+".r_46+"+plasma+".r_47-"+plasma+".r_48+"+plasma+".r_49-"+plasma+".r_50+"+plasma+".r_51-"+plasma+".r_72+"+plasma+".r_73+"+plasma+".r_52+"+plasma+".r_53+"+plasma+".r_74+"+plasma+".r_104+"+plasma+".r_105)+(thermalFluxCu(t/1[s])*1[kg/s]/Vp)/"+plasma+".M_wCu"+suffix+")-outMassRate*"+plasma+".wCu"+suffix+"/(Vp*"+plasma+".M_wCu"+suffix+"))+reverseMassRate*(outer.plas2.wCu"+suffix+"-"+plasma+".wCu"+suffix+")/(Vp*"+plasma+".M_wCu"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuD5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuD5"+suffix+"", new String[]{"((("+plasma+".r_44-"+plasma+".r_45-"+plasma+".r_55+"+plasma+".r_56-"+plasma+".r_81+"+plasma+".r_82+"+plasma+".r_54+"+plasma+".r_83)+(thermalFluxCuD5(t/1[s])*1[kg/s]/Vp)/"+plasma+".M_wCuD5"+suffix+")-outMassRate*"+plasma+".wCuD5"+suffix+"/(Vp*"+plasma+".M_wCuD5"+suffix+"))+reverseMassRate*(outer.plas2.wCuD5"+suffix+"-"+plasma+".wCuD5"+suffix+")/(Vp*"+plasma+".M_wCuD5"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuD3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuD3"+suffix+"", new String[]{"((("+plasma+".r_46-"+plasma+".r_47-"+plasma+".r_67+"+plasma+".r_68-"+plasma+".r_70+"+plasma+".r_71-"+plasma+".r_84+"+plasma+".r_85+"+plasma+".r_66+"+plasma+".r_69+"+plasma+".r_86)+(thermalFluxCuD3(t/1[s])*1[kg/s]/Vp)/"+plasma+".M_wCuD3"+suffix+")-outMassRate*"+plasma+".wCuD3"+suffix+"/(Vp*"+plasma+".M_wCuD3"+suffix+"))+reverseMassRate*(outer.plas2.wCuD3"+suffix+"-"+plasma+".wCuD3"+suffix+")/(Vp*"+plasma+".M_wCuD3"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuP1"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuP1"+suffix+"", new String[]{"((("+plasma+".r_48-"+plasma+".r_49-"+plasma+".r_58+"+plasma+".r_59+"+plasma+".r_70-"+plasma+".r_71-"+plasma+".r_87+"+plasma+".r_88-"+plasma+".r_53+"+plasma+".r_57-"+plasma+".r_69+"+plasma+".r_89)+(thermalFluxCuP1(t/1[s])*1[kg/s]/Vp)/"+plasma+".M_wCuP1"+suffix+")-outMassRate*"+plasma+".wCuP1"+suffix+"/(Vp*"+plasma+".M_wCuP1"+suffix+"))+reverseMassRate*(outer.plas2.wCuP1"+suffix+"-"+plasma+".wCuP1"+suffix+")/(Vp*"+plasma+".M_wCuP1"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuP3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuP3"+suffix+"", new String[]{"(((("+plasma+".r_50-"+plasma+".r_51+"+plasma+".r_55-"+plasma+".r_56-"+plasma+".r_61+"+plasma+".r_62-"+plasma+".r_64+"+plasma+".r_65+"+plasma+".r_67-"+plasma+".r_68-"+plasma+".r_90+"+plasma+".r_91-"+plasma+".r_52-"+plasma+".r_54+"+plasma+".r_60+"+plasma+".r_63-"+plasma+".r_66+"+plasma+".r_92)+(thermalFluxCuP3(t/1[s])*1[kg/s]/Vp)/"+plasma+".M_wCuP3"+suffix+")-tailNetExcitation/N_A_const)-outMassRate*"+plasma+".wCuP3"+suffix+"/(Vp*"+plasma+".M_wCuP3"+suffix+"))+reverseMassRate*(outer.plas2.wCuP3"+suffix+"-"+plasma+".wCuP3"+suffix+")/(Vp*"+plasma+".M_wCuP3"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuH3"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuH3"+suffix+"", new String[]{"((("+plasma+".r_58-"+plasma+".r_59+"+plasma+".r_64-"+plasma+".r_65-"+plasma+".r_93+"+plasma+".r_94-"+plasma+".r_57-"+plasma+".r_63+"+plasma+".r_95)+(thermalFluxCuH3(t/1[s])*1[kg/s]/Vp)/"+plasma+".M_wCuH3"+suffix+")-outMassRate*"+plasma+".wCuH3"+suffix+"/(Vp*"+plasma+".M_wCuH3"+suffix+"))+reverseMassRate*(outer.plas2.wCuH3"+suffix+"-"+plasma+".wCuH3"+suffix+")/(Vp*"+plasma+".M_wCuH3"+suffix+")"});
    model.component(cell).physics(plasma).feature("CuH5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuH5"+suffix+"", new String[]{"((("+plasma+".r_61-"+plasma+".r_62-"+plasma+".r_96+"+plasma+".r_97-"+plasma+".r_60+"+plasma+".r_98)+(thermalFluxCuH5(t/1[s])*1[kg/s]/Vp)/"+plasma+".M_wCuH5"+suffix+")-outMassRate*"+plasma+".wCuH5"+suffix+"/(Vp*"+plasma+".M_wCuH5"+suffix+"))+reverseMassRate*(outer.plas2.wCuH5"+suffix+"-"+plasma+".wCuH5"+suffix+")/(Vp*"+plasma+".M_wCuH5"+suffix+")"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_1p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_1p", new String[]{"((((("+plasma+".r_72-"+plasma+".r_73+"+plasma+".r_81-"+plasma+".r_82+"+plasma+".r_84-"+plasma+".r_85+"+plasma+".r_87-"+plasma+".r_88+"+plasma+".r_90-"+plasma+".r_91+"+plasma+".r_93-"+plasma+".r_94+"+plasma+".r_96-"+plasma+".r_97-"+plasma+".r_74-"+plasma+".r_83-"+plasma+".r_86-"+plasma+".r_89-"+plasma+".r_92-"+plasma+".r_95-"+plasma+".r_98-"+plasma+".r_104-"+plasma+".r_105)+(thermalFluxCu_1p(t/1[s])*1[kg/s]/Vp)/"+plasma+".M_wCu"+suffix+"_1p)+tailNetIonization/N_A_const)-outMassRate*"+plasma+".wCu"+suffix+"_1p/(Vp*"+plasma+".M_wCu"+suffix+"_1p))+reverseMassRate*(outer.plas2.wCu"+suffix+"_1p-"+plasma+".wCu"+suffix+"_1p)/(Vp*"+plasma+".M_wCu"+suffix+"_1p))-multNet1/N_A_const"});
    model.component(cell).physics(plasma).feature("CuT5"+suffix+"").featureInfo("info")
         .set(""+plasma+".R_wCuT5"+suffix+"", new String[]{"(((tailNetExcitation-tailNetIonization)/N_A_const+thermalFluxCuT5(t/1[s])*1[kg/s]/(Vp*Mcu))-outMassRate*"+plasma+".wCuT5"+suffix+"/(Vp*"+plasma+".M_wCuT5"+suffix+"))+reverseMassRate*(outer.plas2.wCuT5"+suffix+"-"+plasma+".wCuT5"+suffix+")/(Vp*"+plasma+".M_wCuT5"+suffix+")"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_2p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_2p", new String[]{"(multNet1-multNet2)/N_A_const+(reverseMassRate*outer.plas2.wCuX_2p-forwardMassRate*"+plasma+".wCu"+suffix+"_2p)/(Vp*Mcu)-0[kg/s]/(Vp*Mcu)"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_3p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_3p", new String[]{"(multNet2-multNet3)/N_A_const+(reverseMassRate*outer.plas2.wCuX_3p-forwardMassRate*"+plasma+".wCu"+suffix+"_3p)/(Vp*Mcu)-0[kg/s]/(Vp*Mcu)"});
    model.component(cell).physics(plasma).feature("Cu"+suffix+"_4p").featureInfo("info")
         .set(""+plasma+".R_wCu"+suffix+"_4p", new String[]{"(multNet3)/N_A_const+(reverseMassRate*outer.plas2.wCuX_4p-forwardMassRate*"+plasma+".wCu"+suffix+"_4p)/(Vp*Mcu)-0[kg/s]/(Vp*Mcu)"});
    model.component(cell).variable("gasv"+suffix+"").set("coveredInnerPower", "(1-Acu)*Pincident*(1-exp(-2*(Rinit/w0)^2))");
    model.component(cell).variable("gasv"+suffix+"").set("coveredOuterPower", "(1-Acu)*Pincident*(1-exp(-2*(outer.Rp/w0)^2))");
    model.component(cell).variable("gasv"+suffix+"")
         .set("Plaser", "coveredInnerPower*outer.beamTransmission*beamAbsorptance*(1+1*beamTransmission)");
    model.component(cell).variable("gasv"+suffix+"")
         .set("meanLaserIntensity", "coveredInnerPower*outer.beamTransmission*Hp/Vp*if(laserTau<1e-4,1-laserTau/2+laserTau^2/6-laserTau^3/24,beamAbsorptance/laserTau)*(1+1*beamTransmission)");
    model.component(cell).physics("bulk"+suffix+"").feature("ge1")
         .set("equation", new String[]{"gasEnergyt-(Psource+Plaser-Qrad*Vp-outPower)/energyScale", "(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue))/energyScale", "absorbedEnergyt-(Psource+Plaser+outer.Plaser)/energyScale", "radiatedEnergyt-(Qrad*Vp+outer.Qrad*outer.Vp)/energyScale", "ambientWorkt-pAmb*outer.Vdot/energyScale", "entrainedEnergyt-eAir*outer.Msweep/energyScale", "removedMasst-thermalMass(t/1[s])*1[kg/s]/mScale", "outMasst-outMassRate/mScale", "outEnergyt-outPower/energyScale", "outCuMasst-outCuRate/mScale"});
    model.component(cell).variable("gasv"+suffix).set(plasma+".wN2",plasma+".wN2"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wN2",plasma+".n_wN2"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wN2",plasma+".M_wN2"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wO2",plasma+".wO2"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wO2",plasma+".n_wO2"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wO2",plasma+".M_wO2"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wN",plasma+".wN"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wN",plasma+".n_wN"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wN",plasma+".M_wN"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wO",plasma+".wO"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wO",plasma+".n_wO"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wO",plasma+".M_wO"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wN2_1p",plasma+".wN2"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wN2_1p",plasma+".n_wN2"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wN2_1p",plasma+".M_wN2"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wO2_1p",plasma+".wO2"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wO2_1p",plasma+".n_wO2"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wO2_1p",plasma+".M_wO2"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wN_1p",plasma+".wN"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wN_1p",plasma+".n_wN"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wN_1p",plasma+".M_wN"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wO_1p",plasma+".wO"+suffix+"_1p");
}
static void part12(Model model,String cell,String suffix,String plasma){

    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wO_1p",plasma+".n_wO"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wO_1p",plasma+".M_wO"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wO_1m",plasma+".wO"+suffix+"_1m");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wO_1m",plasma+".n_wO"+suffix+"_1m");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wO_1m",plasma+".M_wO"+suffix+"_1m");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wO2_1m",plasma+".wO2"+suffix+"_1m");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wO2_1m",plasma+".n_wO2"+suffix+"_1m");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wO2_1m",plasma+".M_wO2"+suffix+"_1m");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCu",plasma+".wCu"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCu",plasma+".n_wCu"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCu",plasma+".M_wCu"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCuD5",plasma+".wCuD5"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCuD5",plasma+".n_wCuD5"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCuD5",plasma+".M_wCuD5"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCuD3",plasma+".wCuD3"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCuD3",plasma+".n_wCuD3"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCuD3",plasma+".M_wCuD3"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCuP1",plasma+".wCuP1"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCuP1",plasma+".n_wCuP1"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCuP1",plasma+".M_wCuP1"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCuP3",plasma+".wCuP3"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCuP3",plasma+".n_wCuP3"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCuP3",plasma+".M_wCuP3"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCuH3",plasma+".wCuH3"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCuH3",plasma+".n_wCuH3"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCuH3",plasma+".M_wCuH3"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCuH5",plasma+".wCuH5"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCuH5",plasma+".n_wCuH5"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCuH5",plasma+".M_wCuH5"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCu_1p",plasma+".wCu"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCu_1p",plasma+".n_wCu"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCu_1p",plasma+".M_wCu"+suffix+"_1p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCuT5",plasma+".wCuT5"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCuT5",plasma+".n_wCuT5"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCuT5",plasma+".M_wCuT5"+suffix+"");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCu_2p",plasma+".wCu"+suffix+"_2p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCu_2p",plasma+".n_wCu"+suffix+"_2p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCu_2p",plasma+".M_wCu"+suffix+"_2p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCu_3p",plasma+".wCu"+suffix+"_3p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCu_3p",plasma+".n_wCu"+suffix+"_3p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCu_3p",plasma+".M_wCu"+suffix+"_3p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".wCu_4p",plasma+".wCu"+suffix+"_4p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".n_wCu_4p",plasma+".n_wCu"+suffix+"_4p");
    model.component(cell).variable("gasv"+suffix).set(plasma+".M_wCu_4p",plasma+".M_wCu"+suffix+"_4p");
}
}
