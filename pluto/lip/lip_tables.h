/* lip_tables.h -- LIPTAB01 table reader and interpolation (PLUTO-independent, pure C99).
 * Format (written by python -m lipsim.tables.build): "LIPTAB01" | int32 n_axes |
 *   per axis: int32 len, char[16] name, float64[len] | int32 n_fields | per field: char[16] name,
 *   float64[prod(len)] in C order of the axes. All tables here have axes (log10_rho, log10_T, Y_cu). */
#ifndef LIP_TABLES_H
#define LIP_TABLES_H

#define LIP_MAX_FIELDS 32

typedef struct {
  int    n[3];            /* lengths of axes: rho, T, Y */
  double *ax[3];          /* axis values (log10 rho, log10 T, Y) */
  int    nf;
  char   name[LIP_MAX_FIELDS][17];
  double *f[LIP_MAX_FIELDS];   /* raw field values */
  double *lf[LIP_MAX_FIELDS];  /* log10 of fields (for positive fields), NULL otherwise */
} LipTable;

int    lip_table_read(const char *path, LipTable *t);   /* 0 on success */
void   lip_table_free(LipTable *t);
int    lip_table_field(const LipTable *t, const char *name); /* index or -1 */
/* trilinear interpolation of field k at (log10 rho, log10 T, Y); log=1 interpolates log10(field) */
double lip_interp(const LipTable *t, int k, double lrho, double lT, double Y, int log);

/* ---- EOS on top of eos.bin (fields e [J/kg], p [Pa]) : SI units ---- */
typedef struct { LipTable tab; int ie, ip, ine; } LipEOS;
int    lip_eos_init(LipEOS *eos, const char *path);
double lip_eos_e(const LipEOS *eos, double rho, double T, double Y);          /* J/kg */
double lip_eos_p(const LipEOS *eos, double rho, double T, double Y);          /* Pa */
/* invert e(rho,T,Y) = e for T; returns 0 on success, 1 if e below table min (T clamped at T_min) */
int    lip_eos_T_from_e(const LipEOS *eos, double rho, double e, double Y, double *T);
int    lip_eos_T_from_p(const LipEOS *eos, double rho, double p, double Y, double *T);
/* squared equilibrium sound speed from table derivatives, c^2 = (dp/drho)_s */
double lip_eos_cs2(const LipEOS *eos, double rho, double T, double Y);
#endif
