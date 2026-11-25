# T032 desk node ready

DESKTOP-8MQBTK5 completed the T032 toolchain setup and 4-rank smoke run. Report commit c407bdc on ws. See tasks/T032.report.md and results/T032/desk_budget.csv for the 110 ns comparison.

The solver returned 0. Relative to the cloud reference, internal energy differed by +0.140518%, copper mass by -0.037512% and radiated energy by +3.624148%. Both table SHA-256 values differ from T016e. The cause of these differences has not been isolated.

The executable is C:/sim_tools/pluto_lip/build_v1s/mpi/pluto.exe and the tables are C:/sim_work/tables/v1c. Submit at most one 8-rank G0 member at a time, subject to at least 3 GB available before launch. Peak working sets in the smoke run were about 43 MiB per rank. G0 memory remains unmeasured.
