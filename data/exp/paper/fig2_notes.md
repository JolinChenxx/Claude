# T023: paper Fig. 2 surface-marker check

Source: Chen et al., *Mechanism of Laser-Induced Self-Deposition of Nanoparticles Identified by In Situ Observation*, Laser & Photonics Reviews 2025, 19, 2400388; DOI 10.1002/lpor.202400388. All page numbers below are physical pages of the local source PDFs.

## Qualitative inspection only

- Main-text Fig. 2a (low fluence) has a horizontal yellow dashed line across each displayed frame. In the 280 ns panel, `Air` is printed above the line and `Copper` below it. This is an explicit surface/interface marking inside the panels; the panel bottom edge is below that line. No numeric surface-row coordinate has been extracted.
- The 750 ns panel has a `100 μm` scale bar. This is a length scale, not a labeled height origin. The time/radius axes in Fig. 2b/c are separate plots, not spatial coordinates for the image panels.
- SI p. 2, Fig. S1, also shows a dashed Air/Copper boundary on its example original camera image. The adjacent focus sentence identifies the camera-side front end of the sample, but does not define the pixel origin or crop rectangle.
- SI p. 6, Fig. S4 (high fluence), has time labels and a `400 μm` scale bar. In the inspected figure there is no explicit Air/Copper label, surface line, surface arrow or spatial zero. Its caption does not state a cropping convention.
- Fig. 2 explicitly concerns low fluence; S4 explicitly concerns high fluence. The inspected figure captions and acquisition/focus paragraphs do not establish a shared crop origin or a pixel mapping between them. No mapping, surface-row value, physical distance or change to the preregistered metric is inferred here; Claude performs the measurement/review.

## Native-resolution deliverable and provenance

`fig2.png` is the complete embedded Fig. 2 image object (panels a-c), extracted directly from main-PDF physical page 3: 2050 × 1084 pixels. No screenshot scaling, upsampling, cropping within the image object, color adjustment, added marks or measurements. The caption is separately transcribed below. `fig2_provenance.json` records both local PDF paths and SHA256 values, the image-object identity and output hash. Reproduction: `python results/T023/extract_paper_fig2.py`.

The quotes below preserve the wording, joining PDF line wraps and split words, normalizing ligatures, and omitting SI margin line numbers. Every quote is programmatically checked against its source page and visually reviewed with that page.

## Main PDF p. 3, complete Figure 2 caption

> Figure 2. Evolution of the plume induced by low-fluence laser irradiation of the copper surface, observed via ultrashort exposure observation at different delay times. a) Selected intensity images of the laser-induced plume on the copper surface. b) Average grayscale of the plume shown in (a). c) Evolution of the plume radius shown in (a).

## Main PDF p. 2, section 2.1: observation direction

> The observation direction was perpendicular to the propagation direction of the nanosecond laser beam, and the plume was captured using the ultrashort exposure observation method (details are provided in Supplementary S2, Supporting Information).

## Main PDF p. 9, section 4 / Materials: sample and polishing

> A copper sample (C1100) with a purity of 99.99% and thickness of 0.5 mm was employed, and the required size was obtained using a cutter. Because the copper sheet was too thin, it was embedded in an aluminum plate with a thickness of 1 mm prior to cutting to avoid plastic deformation during cutting. The experimental surface, measuring 0.5 mm × 15 mm, was polished using a grinder (Struers, LaboPol-20, Japan) with a 4000# sandpaper.

## SI PDF p. 2, printed lines 29-34: two focus directions

> To observe the plasma plume during laser irradiation, the focus had to be adjusted in the direction of the incident processing laser beam and in the direction of imaging using a cooled CCD camera (BU-55, Bitran Corporation, Japan). For the first direction, the focus was adjusted using a white-light camera, whereas for the second direction, the focus was adjusted by introducing a femtosecond pulsed laser beam (Pharos, Phototechnica Co., Ltd, Lithuania) for illumination.

## SI PDF p. 2, printed lines 41-44: camera-side edge in focus

> The cooled CCD camera was set to the continuous shooting mode to inspect the images in real-time, and the focus was adjusted by moving the stage with a fixed objective lens such that the front end of the copper sample on the camera side could be observed clearly.

## SI PDF p. 6, complete Figure S4 caption

> Figure S4. Color contours of the luminance distribution of the plume induced by a high-fluence laser.
