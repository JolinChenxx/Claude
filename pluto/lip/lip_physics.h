/* lip_physics.h -- PLUTO-independent physics kernels used by LipSource() (SI units throughout).
 * Laser: collimated beam along -z (incident from the top of the domain), Beer–Lambert column marching
 *   down to the target face (z = 0), specular return of the unabsorbed part (1 - A(T_s)) back up the
 *   same column (PLAN v3 §3.2, v0: collimated, no refraction).
 * Radiation: Q_rad = 4π·NEC(ρ,T,Y) from opt.bin (limit 'thin' or 'trap'), never cooling below T_floor
 *   within one step (energy limiter, booked).
 * Evaporation: Knight-layer vapour state from the target source table enters the first gas cell as
 *   conservative sources of mass, Cu mass, z-momentum and total energy. */
#ifndef LIP_PHYSICS_H
#define LIP_PHYSICS_H
#include "lip_tables.h"

/* One column. z index 0 is the cell adjacent to the target. dz[k] cell heights, alpha[k] absorption
 * coefficient (1/m). I_top incident intensity at the top face (W/m^2), A_s target absorptance.
 * Output: q[k] deposited power density (W/m^3, down + up pass), *I_surf intensity reaching the target,
 * *I_esc intensity leaving through the top after reflection. Returns absorbed column power (W/m^2). */
double lip_laser_column(int nz, const double *dz, const double *alpha, double I_top, double A_s,
                        double *q, double *I_surf, double *I_esc);

/* Evaporation source: vapour state (rhov, uv, Tv) and the EOS (Y = 1 for pure Cu vapour).
 * Returns the specific total enthalpy carried into the gas: e(ρv,Tv,1) + p/ρv + uv^2/2 (J/kg). */
double lip_vapour_enthalpy(const LipEOS *eos, double rhov, double uv, double Tv);

/* v1k: Knight Knudsen layer with back pressure (subsonic evaporation). pr = p_gas / p_sat(T_s).
 * pr <= 0.2062: sonic (M = 1); pr >= 1: no net evaporation (recondensation not modelled: declared bound);
 * otherwise the exit Mach M solves p_v(M)/p_sat = pr. Returns the mass flux relative to the sonic flux at the
 * same T_s, and M, T_v/T_s, rho_v/rho_sat. */
#define CONST_PI_LIP 3.14159265358979323846
double lip_knight_bp(double pr, double *M, double *Tv_over_Ts, double *rhov_over_rhos);

/* Source table on (t, r) axes (LIPTAB01 with axis names t, r): bilinear lookup of field k. */
typedef struct { int nt, nr; double *t, *r; int nf; char name[16][17]; double *f[16]; } LipSrcTable;
int    lip_src_read(const char *path, LipSrcTable *s);
double lip_src_get(const LipSrcTable *s, int k, double t, double r);
int    lip_src_field(const LipSrcTable *s, const char *name);
#endif
