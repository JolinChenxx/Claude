/* lip_glue.h -- connection between PLUTO and the PLUTO-independent lip kernels. */
#ifndef LIP_GLUE_H
#define LIP_GLUE_H
#include "pluto.h"
#include "lip_tables.h"
#include "lip_physics.h"

/* code <-> SI conversion (definitions.h: 1 kg/m^3, 1 um, 1 km/s, 1 ns, 1 MPa) */
#define LIP_RHO_SI    (UNIT_DENSITY * 1.0e3)
#define LIP_LEN_SI    (UNIT_LENGTH * 1.0e-2)
#define LIP_VEL_SI    (UNIT_VELOCITY * 1.0e-2)
#define LIP_TIME_SI   (LIP_LEN_SI / LIP_VEL_SI)
#define LIP_PRS_SI    (LIP_RHO_SI * LIP_VEL_SI * LIP_VEL_SI)

extern LipEOS      lip_eos;
extern LipTable    lip_opt;
extern LipSrcTable lip_src;

void   lip_init(void);                                  /* loads lip_tables/{eos,opt}.bin, target_source.bin */
double lip_T_from_prim(double *v);                      /* K, from (rho, prs, Y) */
double lip_Y(const double *v);
void   LipSource(Data *d, double dt, timeStep *Dts, Grid *grid);
void   lip_analysis(const Data *d, Grid *grid);
double lip_uservar(const char *name, int k, int j, int i);
double lip_kappa_SI(double *v);                         /* W/m/K from opt.bin "kappa" */
double lip_cv_code(double *v);
void   lip_diff_code(double *v, double *D, double *dh); /* Cu–air diffusion (opt.bin D_cuair, dh_dY) */                          /* d e / d T [code energy per mass per K] */
#endif
