/* lip_tables.c -- see lip_tables.h. Pure C99, no PLUTO dependency (unit-tested in pluto/tests). */
#include "lip_tables.h"
#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int rd(void *p, size_t sz, size_t n, FILE *fp) { return fread(p, sz, n, fp) == n ? 0 : 1; }

int lip_table_read(const char *path, LipTable *t)
{
  FILE *fp = fopen(path, "rb");
  char magic[8];
  int32_t na, nf, len;
  memset(t, 0, sizeof(*t));
  if (!fp) return 1;
  if (rd(magic, 1, 8, fp) || memcmp(magic, "LIPTAB01", 8)) { fclose(fp); return 2; }
  if (rd(&na, 4, 1, fp) || na != 3) { fclose(fp); return 3; }
  for (int a = 0; a < 3; a++) {
    char nm[16];
    if (rd(&len, 4, 1, fp) || rd(nm, 1, 16, fp)) { fclose(fp); return 4; }
    t->n[a] = len;
    t->ax[a] = (double *)malloc(sizeof(double) * len);
    if (rd(t->ax[a], 8, len, fp)) { fclose(fp); return 5; }
  }
  if (rd(&nf, 4, 1, fp) || nf > LIP_MAX_FIELDS) { fclose(fp); return 6; }
  t->nf = nf;
  size_t size = (size_t)t->n[0] * t->n[1] * t->n[2];
  for (int k = 0; k < nf; k++) {
    if (rd(t->name[k], 1, 16, fp)) { fclose(fp); return 7; }
    t->name[k][16] = '\0';
    t->f[k] = (double *)malloc(sizeof(double) * size);
    if (rd(t->f[k], 8, size, fp)) { fclose(fp); return 8; }
    int pos = 1;
    for (size_t i = 0; i < size; i++) if (!(t->f[k][i] > 0.0)) { pos = 0; break; }
    if (pos) {
      t->lf[k] = (double *)malloc(sizeof(double) * size);
      for (size_t i = 0; i < size; i++) t->lf[k][i] = log10(t->f[k][i]);
    }
  }
  fclose(fp);
  return 0;
}

void lip_table_free(LipTable *t)
{
  for (int a = 0; a < 3; a++) free(t->ax[a]);
  for (int k = 0; k < t->nf; k++) { free(t->f[k]); free(t->lf[k]); }
  memset(t, 0, sizeof(*t));
}

int lip_table_field(const LipTable *t, const char *name)
{
  for (int k = 0; k < t->nf; k++) if (strcmp(t->name[k], name) == 0) return k;
  return -1;
}

/* index of the cell containing x on a monotone axis, with the fractional weight; x clamped */
static int locate(const double *ax, int n, double x, double *w)
{
  if (x <= ax[0]) { *w = 0.0; return 0; }
  if (x >= ax[n - 1]) { *w = 1.0; return n - 2; }
  int lo = 0, hi = n - 1;
  while (hi - lo > 1) { int m = (lo + hi) / 2; if (ax[m] <= x) lo = m; else hi = m; }
  *w = (x - ax[lo]) / (ax[lo + 1] - ax[lo]);
  return lo;
}

#define IDX(t, i, j, k) (((size_t)(i) * (t)->n[1] + (j)) * (t)->n[2] + (k))

double lip_interp(const LipTable *t, int f, double lrho, double lT, double Y, int log)
{
  double wa, wb, wc;
  int i = locate(t->ax[0], t->n[0], lrho, &wa);
  int j = locate(t->ax[1], t->n[1], lT, &wb);
  int k = locate(t->ax[2], t->n[2], Y, &wc);
  const double *v = (log && t->lf[f]) ? t->lf[f] : t->f[f];
  double r = 0.0;
  for (int di = 0; di < 2; di++)
    for (int dj = 0; dj < 2; dj++)
      for (int dk = 0; dk < 2; dk++) {
        double w = (di ? wa : 1 - wa) * (dj ? wb : 1 - wb) * (dk ? wc : 1 - wc);
        r += w * v[IDX(t, i + di, j + dj, k + dk)];
      }
  return (log && t->lf[f]) ? pow(10.0, r) : r;
}

/* ------------------------------------------------------------------ EOS */
int lip_eos_init(LipEOS *eos, const char *path)
{
  int s = lip_table_read(path, &eos->tab);
  if (s) return s;
  eos->ie = lip_table_field(&eos->tab, "e");
  eos->ip = lip_table_field(&eos->tab, "p");
  eos->ine = lip_table_field(&eos->tab, "ne");
  if (eos->ie < 0 || eos->ip < 0 || !eos->tab.lf[eos->ie] || !eos->tab.lf[eos->ip]) return 9;
  return 0;
}

double lip_eos_e(const LipEOS *eos, double rho, double T, double Y)
{ return lip_interp(&eos->tab, eos->ie, log10(rho), log10(T), Y, 1); }

double lip_eos_p(const LipEOS *eos, double rho, double T, double Y)
{ return lip_interp(&eos->tab, eos->ip, log10(rho), log10(T), Y, 1); }

/* Invert a field monotone in T: the trilinear interpolant at fixed (lrho, Y) is piecewise linear in
 * log10 T with monotone node values, so the inverse is exact and unique. */
static int invert_T(const LipEOS *eos, int f, double rho, double val, double Y, double *T)
{
  const LipTable *t = &eos->tab;
  if (!(val > 0.0) || !(rho > 0.0)) {   /* e or p <= 0 or NaN: return the table floor, flag 3 (was: table max) */
    *T = pow(10.0, eos->tab.ax[1][0]);
    return 3;
  }
  double lrho = log10(rho), lv = log10(val), prev = 0.0;
  int nT = t->n[1];
  /* node values of log10 f along T at fixed (lrho, Y) */
  double wa, wc;
  int i = locate(t->ax[0], t->n[0], lrho, &wa);
  int k = locate(t->ax[2], t->n[2], Y, &wc);
  const double *v = t->lf[f];
  for (int j = 0; j < nT; j++) {
    double y = 0.0;
    for (int di = 0; di < 2; di++)
      for (int dk = 0; dk < 2; dk++)
        y += (di ? wa : 1 - wa) * (dk ? wc : 1 - wc) * v[IDX(t, i + di, j, k + dk)];
    if (j == 0 && lv <= y) { *T = pow(10.0, t->ax[1][0]); return 1; }
    if (j > 0 && lv <= y) {
      double w = (lv - prev) / (y - prev);
      *T = pow(10.0, t->ax[1][j - 1] + w * (t->ax[1][j] - t->ax[1][j - 1]));
      return 0;
    }
    prev = y;
  }
  *T = pow(10.0, t->ax[1][nT - 1]);
  return 2;
}

int lip_eos_T_from_e(const LipEOS *eos, double rho, double e, double Y, double *T)
{ return invert_T(eos, eos->ie, rho, e, Y, T); }

int lip_eos_T_from_p(const LipEOS *eos, double rho, double p, double Y, double *T)
{ return invert_T(eos, eos->ip, rho, p, Y, T); }

double lip_eos_cs2(const LipEOS *eos, double rho, double T, double Y)
{
  const double h = 1.0e-3;  /* relative step for derivatives of the interpolant */
  double dp_drho = (lip_eos_p(eos, rho * (1 + h), T, Y) - lip_eos_p(eos, rho * (1 - h), T, Y)) / (2 * h * rho);
  double dp_dT = (lip_eos_p(eos, rho, T * (1 + h), Y) - lip_eos_p(eos, rho, T * (1 - h), Y)) / (2 * h * T);
  double de_dT = (lip_eos_e(eos, rho, T * (1 + h), Y) - lip_eos_e(eos, rho, T * (1 - h), Y)) / (2 * h * T);
  double c2 = (de_dT > 0.0) ? dp_drho + T * dp_dT * dp_dT / (rho * rho * de_dT) : dp_drho;
  /* Thermodynamic bound (T016d): for an ideal-gas mixture in chemical/ionisation equilibrium the adiabatic index
     Gamma1 = rho c^2 / p lies in [1, 5/3]. Outside the table (T above its maximum, where e(T) is flat and de/dT -> 0)
     the finite-difference c^2 diverges and froze the time step (dt 4e-14 s). Clip Gamma1 to [1, 5/3]. */
  double p = lip_eos_p(eos, rho, T, Y);
  if (p > 0.0) {
    double g1 = c2 * rho / p;
    if (!(g1 >= 1.0)) g1 = 1.0;
    if (g1 > 5.0 / 3.0) g1 = 5.0 / 3.0;
    c2 = g1 * p / rho;
  }
  return c2 > 0 ? c2 : dp_drho;
}
