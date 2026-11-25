# Laser and image timing: source passages
Chen et al., DOI 10.1002/lpor.202400388, main article and Supporting Information. Physical PDF page and one-based PyMuPDF extraction line, including headers. Table values are quoted as printed; TTL widths are not optical pulse widths. The PDFs match the hashes recorded in fig2_provenance.json.

## Processing pulse
main_pdf, p. 2, lines 67-69:

> 67: The copper surface was irradiated using a single-pulse nanosec-
> 68: ond laser beam with a wavelength of 1064 nm and pulse dura-
> 69: tion of 60 ns. Laser-induced plume images were captured us-

## Plume imaging
main_pdf, p. 9, lines 77-85:

> 77: shown in Supplementary S3, Supporting Information). To achieve ultra-
> 78: short exposure of the phenomenon, an image intensiﬁer (C10880-03C,
> 79: Hamamatsu Photonics Co., Ltd., Japan) was used, capable of selectively
> 80: amplifying the phenomenon for a duration as brief as 10 ns. The image in-
> 81: tensiﬁer was connected to a cooled CCD camera using a relay lens (A4539,
> 82: Hamamatsu Photonics Co., Ltd., Japan). The transistor–transistor–logic
> 83: (TTL) signals of the nanosecond laser, image intensiﬁer, and cooled CCD
> 84: camera were controlled using two delay generators (DGs) (see Supple-
> 85: mentary S2, Supporting Information for details).

## Pump and probe
main_pdf, p. 9, lines 87-99:

> 87: The shock wave, metal vapor, spat-
> 88: ter, and solid/liquid metals generated during laser irradiation were ob-
> 89: served using the pump–probe method. As in the ultrashort exposure ob-
> 90: servation, a nanosecond laser was used as the pump laser to irradiate the
> 91: copper surface, and a 515 nm femtosecond laser was used as a probe
> 92: laser for pump–probe imaging. An optical ﬁber cable (M124, Thorlabs)
> 93: was used with a core diameter of 400 μm to reduce the inﬂuence of in-
> 94: terference fringes on imaging (Figure S9 in Supplementary S5, Support-
> 95: ing Information). The femtosecond laser beam passed through the opti-
> 96: cal ﬁber cable, and it was irradiated onto the copper sample to capture
> 97: the evolution of laser irradiation. The delay time was controlled using
> 98: two DGs (refer to Supplementary S5, Supporting Information for further
> 99: details).

## Table 1: processing beam; beam-width wording retained
si_pdf, p. 1, lines 39-61:

> 39: Table 1. Irradiation conditions for the low- and high-fluence nanosecond lasers
> 40: 18
> 42: Low-fluence laser
> 43: High-fluence laser
> 44: Laser wavelength
> 45: 1064 nm
> 46: 1064 nm
> 47: Pulse duration
> 48: 60 ns
> 49: 60 ns
> 50: Laser energy
> 51: 0.32 mJ
> 52: 0.4 mJ
> 53: Beam waist radius (e-2)
> 54: 31 μm
> 55: 22 μm
> 56: Fluence
> 57: 42 J/cm2
> 58: 105 J/cm2
> 59: Shot number
> 60: 1
> 61: 1

## Laser identification and objective
si_pdf, p. 1, lines 70-78:

> 70: direction was perpendicular to the propagation direction of the nanosecond laser. A single-
> 72: pulsed nanosecond laser beam emitted from the laser device (YLPN-1-30×240-100-R, IPG
> 74: Photonics) under the conditions listed in Table 1 was focused on the polished copper surface
> 76: using a bright-field near-infrared correction objective lens (MY5X-822, M Plan Apo NIR 5×,
> 78: NA = 0.14). A white-light camera was utilized and positioned coaxially with the nanosecond

## Table 2: electrical timing
si_pdf, p. 3, lines 3-28:

> 3: Table 2. TTL signals set for the delay generator in the ultrashort exposure observations
> 4: 49
> 5: Signals in Figure S1
> 6: Delay time (s)
> 7: Pulse duration (ns)
> 8: Waveform direction
> 9: (a)
> 10: 0
> 11: 100
> 12: Positive
> 13: (b)
> 14: 0
> 15: 200
> 16: Positive
> 17: (c)
> 18: 0.000003
> 19: 100
> 20: Positive
> 21: (d)
> 22: 19.5
> 23: 1960000
> 24: Positive
> 25: (e)
> 26: 19.4995
> 27: 1000000
> 28: Negative

## Plume timing and optical gate
si_pdf, p. 3, lines 29-57:

> 29: The transistor–transistor–logic (TTL) signals of all devices were controlled by two delay
> 31: generators (DGs) under the conditions listed in Table 2. An oscilloscope was used to measure
> 33: the delay time between the nanosecond laser and image intensifier. DG1 was set up to generate
> 35: a 2.5 V signal 1,000,000 times every 19.6 µs, that is, approximately 20 s of signal generation,
> 37: which is sufficient for stable oscillation of the nanosecond laser beam for processing. The
> 39: repetition frequency was approximately 51 kHz, which was considerably higher than the
> 41: minimum oscillation value of 50 kHz for the nanosecond laser beam used for processing. DG2
> 43: uses a function that outputs only one signal after waiting for a set time after responding to the
> 45: external input. Nanosecond lasers use the function of extracting only the next pulse after
> 47: receiving an external signal (d) from the connected software. The amplification value of the
> 49: image intensifier was set to 960, with the gating time set to a minimum value of 10 ns. The
> 51: cooled CCD camera used a function that exposed only when an external signal (e) was inputted.
> 53: While the minimum exposure time of the cooled CCD camera was 1 ms, the actual exposure
> 55: time for observing the plume was 10 ns because the image intensifier selectively amplified only
> 57: within this timeframe.

## Shock and spatter (continues on p. 9)
si_pdf, p. 8, lines 19-22:

> 19: The shock wave and spatter induced by the nanosecond laser beam were observed using
> 20: pump–probe imaging, which is similar to the ultrashort exposure observation method described
> 21: in Supplementary 2.
> 22: Figure S8 shows a schematic of the pump–probe imaging method. A nanosecond laser

## Pump/probe timing measurement
si_pdf, p. 9, lines 1-10:

> 1: operating under the conditions listed in Table 1 was used as the pump laser to irradiate the
> 2: copper surface. Simultaneously, a femtosecond laser operating under the conditions specified
> 3: in Table 3 was used as the probe laser to capture the irradiation images. The delay time between
> 4: the pump and probe was controlled using two DGs, and the TTL signals were measured using
> 5: an oscilloscope. DG1 outputted TTL signals 1,000,000 times at 19.6 µs intervals and inputted
> 6: them to two lasers. Meanwhile, DG2 was set to output a signal, triggering the internal shutter
> 7: of the nanosecond laser for processing. It also managed the exposure of the cooled CCD camera
> 8: after a specified duration from the moment it received the output from DG1. Furthermore, the
> 9: signals acquired by the photodetector from the two lasers were inputted into the oscilloscope
> 10: to measure the delay time of the imaging results

## Tables 3 and 4: probe and electrical signals
si_pdf, p. 10, lines 1-36:

> 1: Table 3. Probe laser conditions in the pump–probe method
> 2: Wavelength
> 3: 1030 nm
> 4: Pulse duration
> 5: 180 fs
> 6: Pulse energy
> 7: 20 µJ
> 8: Frequency
> 9: 51 kHz
> 11: Table 4. TTL signals set for the delay generator in pump–probe observations
> 12: Signals in
> 13: Figure S8
> 14: Delay time (s)
> 15: Pulse duration (ns)
> 16: Waveform direction
> 17: (a)
> 18: 0.000003
> 19: 100
> 20: Positive
> 21: (b)
> 22: 0
> 23: 100
> 24: Positive
> 25: (c)
> 26: 0
> 27: 200
> 28: Positive
> 29: (d)
> 30: 19.5
> 31: 1960000
> 32: Positive
> 33: (e)
> 34: 19.4995
> 35: 1000000
> 36: Negative

## Not specified in the inspected methods passages
No explicit optical edge, fractional-height crossing or peak defining time zero; no numerical trigger jitter, processing-pulse FWHM convention, measured optical temporal trace or M? identified in these passages. The SI labels the beam dimension as a radius; no conversion to a diameter has been made here.
