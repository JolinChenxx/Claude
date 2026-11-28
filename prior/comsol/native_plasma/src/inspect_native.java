/*
 * inspect_native.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

/** Model exported on Nov 7 2026, 18:22 by COMSOL 6.4.0.429. */
public class inspect_native {

  public static Model run() {
    Model model = ModelUtil.create("Model");

    model.modelPath("D:\\junlin\\PlasmaPlume\\route1\\src");

    model.component().create("c", true);

    model.component("c").geom().create("g", 2);
    model.component("c").geom("g").axisymmetric(true);
    model.component("c").geom("g").create("r", "Rectangle");
    model.component("c").geom("g").run();

    model.component("c").physics().create("plas", "ColdPlasma", "g");
    model.component("c").physics("plas").create("rx1", "Reaction", 2);
    model.component("c").physics("plas").feature("rx1").set("formula", "e+Cu=>2e+Cu+");
    model.component("c").physics("plas").create("ei1", "ElectronImpactReaction", 2);
    model.component("c").physics("plas").create("probe0", "GeneralPowerDeposition", 2);
    model.component("c").physics("plas").create("probe2", "ElectronProductionRate", 2);
    model.component("c").physics("plas").create("probe3", "HeavySpeciesFlux", 1);
    model.component("c").physics("plas").create("probe4", "Outflow", 1);
    model.component("c").physics("plas").create("probe5", "ElectronOutlet", 1);
    model.component("c").physics("plas").create("probe6", "WallDriftDiffusion", 1);
    model.component("c").physics("plas").create("probe7", "FloatingPotential", 1);
    model.component("c").physics().create("liq", "LaminarFlow", "g");
    model.component("c").physics("liq").create("FreeSurface", "FreeSurface", 1);
    model.component("c").physics("liq").create("BoundaryStress", "BoundaryStress", 1);
    model.component("c").physics("liq").create("VolumeForce", "VolumeForce", 2);

    model.component("c").common().create("PrescribedNormalMeshVelocity", "PrescribedNormalMeshVelocity");
    model.component("c").common().create("PrescribedMeshVelocity", "PrescribedMeshVelocity");
    model.component("c").common().create("FixedBoundary", "FixedBoundary");
    model.component("c").common().create("DeformingDomain", "DeformingDomain");

    model.component("c").physics("plas").prop("TransportSettings").set("Convection", true);
    model.component("c").physics("plas").feature("pes1").set("u", new String[]{"urTest", "0", "uzTest"});

    return model;
  }

  public static void main(String[] args) {
    run();
  }

}
