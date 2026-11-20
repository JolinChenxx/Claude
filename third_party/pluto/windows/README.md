# Exact T005 Windows build adapter copies

- Src/HD/makefile: VPATH separators for native mingw32-make.
- Config/Windows.serial.gcc.defs: serial GCC build flags.
- Config/Windows.mpi8.gcc.defs: MPI GCC flags; mpi8 names the T005 build, not a fixed process count.
- windows_compat.c: POSIX random-number compatibility.
- windows_compat.h: Windows/SAL compatibility declarations.

Compiler: C:\Program Files\ANSYS Inc\v261\SCADE Rapid Prototyper\contrib\Msys64\mingw64\bin\gcc.exe (MinGW GCC 5.1.0).

MS-MPI launcher: C:\Program Files\Microsoft MPI\Bin\mpiexec.exe.

MS-MPI headers/library: D:/sim_tools/pluto_ref/msmpi/include, include/x64, lib/msmpi.lib. SDK and compiler binaries are not included.

Actual MPI configuration:

```make
CC = gcc
CFLAGS = -c -O3 -std=c11 -Wno-implicit-function-declaration -DMSMPI_NO_SAL -include windows_compat.h -ID:/sim_tools/pluto_ref/msmpi/include -ID:/sim_tools/pluto_ref/msmpi/include/x64
LDFLAGS = -lm D:/sim_tools/pluto_ref/msmpi/lib/msmpi.lib
PARALLEL = TRUE
USE_HDF5 = FALSE
USE_PNG = FALSE
```

Serial has the same optimization/compatibility flags without MPI paths. Local makefile generation follows scripts/win/build_pluto_reference.py. No fluid-physics changes.
