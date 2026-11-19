# T018b low thin: 200ns checkpoint

Captured 2026-11-19T16:37:40+08:00; run continues, no parameter changes. Latest complete budget t=239.4524ns. Average throughput since MPI start=1394.44ns/h over618.2s; since batch launch=1319.66ns/h over653.2s. These include the early transient and do not predict remaining runtime.

## Time-step evidence

Logging is every1000 steps. Exact100/150ns dt/Nrkl pairs were not recorded; all available step lines are reproduced below. Nearest step to both100 and150ns is129.45ns. Snapshot-index dt values near each requested time are separate evidence and do not carry Nrkl. No Nrkl interpolation.

```text
step:0; t = 0.0000e+000; dt = 1.0000e-005; 0.0 %
[Mach = 0.000000, Nrkl = 2]
step:1000; t = 6.6847e+001; dt = 5.7414e-002; 5.1 %
[Mach = 11.666763, Nrkl = 2]
step:2000; t = 1.2945e+002; dt = 7.3271e-002; 10.0 %
[Mach = 10.330936, Nrkl = 2]
step:3000; t = 2.0040e+002; dt = 7.5452e-002; 15.4 %
[Mach = 8.590506, Nrkl = 10]
```

| Requested ns | Snapshot ns | Snapshot step | Snapshot dt ns |
|---:|---:|---:|---:|
| 100 | 99.974140 | 1559 | 0.05849946 |
| 150 | 149.937700 | 2291 | 0.06724266 |
| 200 | 199.963100 | 2994 | 0.07543264 |

Latest recorded rank0 dt-limit (offset-verified, dt_src is seconds and differs from actual step dt):

```text
! lip dt-limit: step 3000 kind 3 (1 wall 2 heat/rad 3 mass 4 vapour) [i,j=3,3] rho=4.525e-001 T=3.096e+003 rhoe=2.951e+005 q=2.968e+008 dt_src=7.622e-010 s
```

## Budget comparison

Linear interpolation at exactly100/150/200ns; signed percent =100*(v1i-v1h)/max(abs(v1i),abs(v1h),1e-30). Values in raw budget units. No acceptance tolerance imposed. Tiny near-zero diagnostic values remain explicit; relative percentages alone are not an accuracy verdict.

| t ns | Quantity | v1h | v1i | v1i-v1h | Signed % of maxabs |
|---:|---|---:|---:|---:|---:|
| 100 | E_internal | 1.71408494e-03 | 1.71408461e-03 | -3.3480e-10 | -0.00002 |
| 100 | E_kinetic | 2.43118160e-06 | 2.43061463e-06 | -5.6698e-10 | -0.02332 |
| 100 | M_Cu | 1.53928451e-12 | 1.53900106e-12 | -2.8346e-16 | -0.01841 |
| 100 | M_total | 8.17110747e-09 | 8.17110719e-09 | -2.8344e-16 | -0.00000 |
| 100 | E_inc | 3.03669629e-04 | 3.03354133e-04 | -3.1550e-07 | -0.10389 |
| 100 | E_gas_abs | 1.73225781e-08 | 1.70742746e-08 | -2.4830e-10 | -1.43341 |
| 100 | E_targ_abs | 2.71979588e-05 | 2.71848333e-05 | -1.3126e-08 | -0.04826 |
| 100 | E_esc | 2.76454347e-04 | 2.76152226e-04 | -3.0212e-07 | -0.10928 |
| 100 | E_rad | 9.69581968e-10 | 9.64543410e-10 | -5.0386e-12 | -0.51966 |
| 100 | E_rad_limited | 1.23477556e-72 | 1.18633183e-72 | -4.8444e-74 | -0.00000 |
| 100 | M_src | 1.53928451e-12 | 1.53900106e-12 | -2.8346e-16 | -0.01841 |
| 100 | E_src | 3.43572430e-06 | 3.43506938e-06 | -6.5492e-10 | -0.01906 |
| 100 | E_wall | 4.20955206e-10 | 4.14082343e-10 | -6.8729e-12 | -1.63268 |
| 150 | E_internal | 1.71530655e-03 | 1.71530600e-03 | -5.4473e-10 | -0.00003 |
| 150 | E_kinetic | 1.38791841e-06 | 1.38759826e-06 | -3.2015e-10 | -0.02307 |
| 150 | M_Cu | 1.64944638e-12 | 1.64922335e-12 | -2.2303e-16 | -0.01352 |
| 150 | M_total | 8.17121763e-09 | 8.17121741e-09 | -2.2308e-16 | -0.00000 |
| 150 | E_inc | 3.15765313e-04 | 3.15457180e-04 | -3.0813e-07 | -0.09758 |
| 150 | E_gas_abs | 1.73442498e-08 | 1.70959113e-08 | -2.4834e-10 | -1.43182 |
| 150 | E_targ_abs | 2.84595598e-05 | 2.84471971e-05 | -1.2363e-08 | -0.04344 |
| 150 | E_esc | 2.87288409e-04 | 2.86992887e-04 | -2.9552e-07 | -0.10287 |
| 150 | E_rad | 1.01604643e-09 | 1.01089529e-09 | -5.1511e-12 | -0.50698 |
| 150 | E_rad_limited | 2.08685792e-72 | 1.88510075e-72 | -2.0176e-73 | -0.00000 |
| 150 | M_src | 1.64944638e-12 | 1.64922335e-12 | -2.2303e-16 | -0.01352 |
| 150 | E_src | 3.61911796e-06 | 3.61856585e-06 | -5.5211e-10 | -0.01526 |
| 150 | E_wall | 2.30746062e-08 | 2.31302850e-08 | 5.5679e-11 | 0.24072 |
| 200 | E_internal | 1.71567959e-03 | 1.71567899e-03 | -5.9688e-10 | -0.00003 |
| 200 | E_kinetic | 9.93661918e-07 | 9.93363540e-07 | -2.9838e-10 | -0.03003 |
| 200 | M_Cu | 1.65426354e-12 | 1.65404816e-12 | -2.1538e-16 | -0.01302 |
| 200 | M_total | 8.17122245e-09 | 8.17122224e-09 | -2.1530e-16 | -0.00000 |
| 200 | E_inc | 3.15794773e-04 | 3.15486795e-04 | -3.0798e-07 | -0.09752 |
| 200 | E_gas_abs | 1.73442499e-08 | 1.70959114e-08 | -2.4834e-10 | -1.43182 |
| 200 | E_targ_abs | 2.84619922e-05 | 2.84496426e-05 | -1.2350e-08 | -0.04339 |
| 200 | E_esc | 2.87315436e-04 | 2.87020056e-04 | -2.9538e-07 | -0.10281 |
| 200 | E_rad | 1.40990300e-09 | 1.40512829e-09 | -4.7747e-12 | -0.33866 |
| 200 | E_rad_limited | 2.14204401e-72 | 1.91620432e-72 | -2.2584e-73 | -0.00000 |
| 200 | M_src | 1.65426354e-12 | 1.65404816e-12 | -2.1538e-16 | -0.01302 |
| 200 | E_src | 3.62416784e-06 | 3.62362463e-06 | -5.4321e-10 | -0.01499 |
| 200 | E_wall | 5.07936533e-08 | 5.08965833e-08 | 1.0293e-10 | 0.20223 |

Original v1h run intentionally stopped per T018b step2; complete captured budgets retained with SHA256 in checkpoint.json. No fitting, tuning or pass/fail threshold was introduced.
