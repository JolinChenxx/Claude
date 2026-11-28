# Deposited copper particle sizes: source check for T030

Chen et al., *Mechanism of Laser-Induced Self-Deposition of Nanoparticles Identified by ...*, Laser & Photonics Reviews, DOI 10.1002/lpor.202400388, and its Supporting Information. Source attribution and the Creative Commons Attribution notice are on main physical page 1, lines 39-45. The two local PDF hashes were checked against fig2_provenance.json.

No numerical diameter, radius or particle-size distribution for the deposited particles was identified in the extracted main or SI text. The authors report qualitative changes with distance and fluence. The deposition distances and SEM scale bars are not particle diameters. No image measurements were made. These passages do not validate the 10, 30 or 100 nm model radii or directly measure the radius of an airborne molten droplet.

Physical pages and one-based lines use PyMuPDF page.get_text().splitlines(), as in droplets_notes.md. Text is left in source order; standalone printed SI line numbers are omitted. Ellipses are not used inside quotations.

## Deposits far from the trace

Main p. 7, lines 22-24:

> 22: In addition, NPs could be observed at a distance of 1 mm from
> 23: the laser irradiation trace; however, these NPs were minute and
> 24: did not signiﬁcantly aﬀect the nanostructure of the metal surface.

## Fluence and distance

Main p. 7, lines 25-31:

> 25: The NPs around the laser-irradiated area under low and high
> 26: ﬂuences were compared. The results demonstrated that as the
> 27: laser ﬂuence increased, the number and size of the NPs near
> 28: the laser-irradiated area signiﬁcantly increased. However, at a dis-
> 29: tance farther away from the laser-irradiated area, changes in the
> 30: size and number of NPs were not evident (Figure S15, Support-
> 31: ing Information). The optical microscopy results showed that the

## Formation mechanism, not a size measurement

Main p. 7, lines 88-95:

> 88: lying the surface tension. This reduction in surface tension dom-
> 89: inance enables smaller fragments of the molten material to be
> 90: ejected from the surface. These fragments solidify more rapidly
> 91: owing to their higher surface-to-volume ratio, leading to the for-
> 92: mation of NPs as the material cools and solidiﬁes. Therefore, the
> 93: surface energy signiﬁcantly inﬂuences the size, morphology, and
> 94: distribution of NPs formed during laser ablation processes, par-
> 95: ticularly in the later stages of particle formation.

## Nanometer-scale material in the plume, not a measured deposit size

Main p. 9, lines 52-56:

> 52: atmosphere. The outside of the plume transforms into a metal-
> 53: lic vapor. Furthermore, the plume is cooled by the surrounding
> 54: atmosphere and transformed into nanometer-scale solid and liq-
> 55: uid metal particles. Meanwhile, the copper volume rapidly de-
> 56: creases as it changes from gas to solid/liquid; thus, a pressure

## SEM comparison near and far from the trace

SI p. 16, lines 3-9:

> 3: As shown in Figure S15a, b, the nanostructures resulting from irradiating the polished copper
> 5: surface with a single-pulsed nanosecond laser differ at different locations and for different laser powers.
> 7: Figure S15c, d shows that the nanostructures close to the laser-irradiated area are larger than those
> 9: farther away. Figure S16 shows the nanostructures at different locations in the area irradiated with low-

Main p. 7, lines 3-21 and SI pp. 16-17 (Figs. S15-S17) describe particle counts, observation positions or distances from the irradiation trace. Those numbers were not used as particle-size statistics.
