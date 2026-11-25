# Cu, 1064 nm: reflectance source check (in progress)

Mattern, Kukreja & Ostendorf, DOI 10.1007/s11665-023-08961-8, Fig. 3(a?d) and accompanying Results paragraphs. Entries are measured **reflectance (%)**, not absorptance; temperatures are measured, not corrected.

| Surface | Initial R | Solid near transition / last measured | Liquid |
|---|---:|---|---|
| Untreated rolled | 94.74?94.94 | 96.76?96.91 around 1050 K; not a value at melting | 93.66 at 1349 K, one sample |
| Mechanically polished | 96.57?97.70 | 96.60?96.95 at melting | 93.40?93.94 |
| Oxide reduced | 98.90?98.96 | Final observations: 97.13 at 1077 K; 96.94 at 1189 K; 97.36 at 1130 K | Not measured for these samples |
| Resolidified | 97.41?98.70 | 96.60?96.97 at melting | Approximately 93.4?93.8 |

No specimen-paired melting-step magnitude is transcribed in the table. No jump is calculated by subtracting unpaired extrema.

### Second-paper correction versus the measured melting step
DOI 10.1007/s40194-023-01672-7, section 4.2, Fig. 10(a): the process-oriented correction is zero at room temperature and a reflectance shift of -0.4% at melting temperature for the reduced curve; the authors remove the dip around 900 K before correcting the instrumental drift. This is a correction to a curve, not a measured jump on melting. In the first paper, section 3.1, Fig. 4(b), the mean measured liquid reflectance across successfully melted samples is 93.68%. No synthetic paired jump is reported here.

### Verified primary-PDF locations (01:30 UTC+8)
Physical PDF pages and 1-based PyMuPDF page.get_text().splitlines() line numbers. Local source PDFs under D:/sim_tools/T036-sources. SHA-256: Mattern1.pdf `8dc26dd929a2288186a29d3315dbe7a5914ea66604301ec85500dbc1dfc02c42`; Mattern2.pdf `96ad84c18ddde1d25595a8a919e1053c59b97f7d21502a9fb7557159b6b5af14`.

| First paper, Fig. 3 surface | Source lines |
|---|---|
| Untreated | p. 5, ll. 119-136 |
| Mechanically polished | p. 7, ll. 5-13 |
| Oxide reduced | p. 7, ll. 39-61 |
| Resolidified | p. 7, ll. 70-77, 89-99 |

Pooled liquid reflectance 93.68%: p. 8, ll. 22-31. The printed solid and liquid ranges in Fig. 3(b,d) are not paired specimen values; neither their spread nor the drift correction is an individual measured melting step. Second paper, section 4.2: zero correction at room temperature, p. 13, ll. 23-25; optimal -0.4% correction at melting, p. 13, ll. 46-50 (also p. 14, ll. 4-8).
