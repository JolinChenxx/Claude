# T027 high G0 it2: authorized early stop

Stopped at 2026-11-25 01:00 UTC+8 under the T027-stop-high-it2 handoff. Only the verified T027 production wrapper (PID 28444) and its process tree, including eight v1p MPI ranks, were stopped. No raw data were removed. T023, T028 and the postprocessing queue remained running.

The last complete budget row is at 162.4732 ns of the scheduled 800 ns. This is a partial run, not a successful production completion. The matched high it0 aspect-ratio changes were below the preregistered 5% threshold.

Raw budget SHA256: a95190f1200549ff965c64d34a45b0634f595208e662ddea00d81466af1d3301

```csv
t_ns,E_internal,E_kinetic,M_Cu,M_total,E_inc,E_gas_abs,E_targ_abs,E_esc,E_rad,E_rad_limited,M_src,E_src,E_wall,M_bp_suppressed,E_bd_transfer,E_bd_store,M_drop_condensed,P_vap_mom
1.624732e+002,1.7982495723e-003,1.7221946546e-005,3.1764750610e-012,8.1727446620e-009,3.9636638075e-004,1.6209084608e-004,3.1332627047e-005,2.0294290762e-004,1.9196680286e-005,5.0489439453e-073,3.1764750610e-012,-1.0542542152e-009,4.0491882779e-005,8.8732195687e-013,0.0000000000e+000,0.0000000000e+000,2.5036200790e-013,6.3785680456e-010
```
