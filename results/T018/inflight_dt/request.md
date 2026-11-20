# T018 low members: what limits dt? (light request; change no run)

Low T018 dt ≈ 1e-3 ns at 197 ns. T016e low G0 (no phase explosion) had 4e-2 ns at the same time. At the current ~34 ns/h, each low iteration to 1300 ns would take ~32 h. The 190 ns extract (r < 37 µm, z 60–110 µm) has no fast cells: max |v| + c_s ≈ 1.8 km/s. So the limit is somewhere else.

For low_thin_G0/it0 and low_trap_G0/it0, use `Select-String` on the **rank logs of every rank** (pluto.N.log) for the patterns `lip dt-limit` and `lip vmax`. Push the last 6 matches of each per rank, with the step numbers, into `results/T018/inflight_dt/`. Also push the latest `dt =` lines (the last 5 step lines from rank 0). Then write a short handoff. Do not stop or change anything.
