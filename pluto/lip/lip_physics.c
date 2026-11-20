/* lip_physics.c -- see lip_physics.h */
#include "lip_physics.h"
#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

double lip_laser_column(int nz, const double *dz, const double *alpha, double I_top, double A_s,
                        double *q, double *I_surf, double *I_esc)
{
  double I = I_top, absorbed = 0.0;
  for (int k = nz - 1; k >= 0; k--) {            /* downward pass */
    double T = exp(-alpha[k] * dz[k]);
    double dI = I * (1.0 - T);
    q[k] = dI / dz[k];
    absorbed += dI;
    I *= T;
  }
  *I_surf = I;
  I *= (1.0 - A_s);                              /* specular reflection of the unabsorbed part */
  for (int k = 0; k < nz; k++) {                 /* upward pass */
    double T = exp(-alpha[k] * dz[k]);
    double dI = I * (1.0 - T);
    q[k] += dI / dz[k];
    absorbed += dI;
    I *= T;
  }
  *I_esc = I;
  return absorbed;
}

double lip_vapour_enthalpy(const LipEOS *eos, double rhov, double uv, double Tv)
{
  double e = lip_eos_e(eos, rhov, Tv, 1.0);
  double p = lip_eos_p(eos, rhov, Tv, 1.0);
  return e + p / rhov + 0.5 * uv * uv;
}

/* ------------------------------------------------------------- source table (t, r) */
int lip_src_read(const char *path, LipSrcTable *s)
{
  FILE *fp = fopen(path, "rb");
  char magic[8], nm[16];
  int32_t na, len, nf;
  memset(s, 0, sizeof(*s));
  if (!fp) return 1;
  if (fread(magic, 1, 8, fp) != 8 || memcmp(magic, "LIPTAB01", 8)) { fclose(fp); return 2; }
  if (fread(&na, 4, 1, fp) != 1 || na != 2) { fclose(fp); return 3; }
  double **ax[2] = {&s->t, &s->r};
  int *n[2] = {&s->nt, &s->nr};
  for (int a = 0; a < 2; a++) {
    if (fread(&len, 4, 1, fp) != 1 || fread(nm, 1, 16, fp) != 16) { fclose(fp); return 4; }
    *n[a] = len;
    *ax[a] = (double *)malloc(sizeof(double) * len);
    if (fread(*ax[a], 8, len, fp) != (size_t)len) { fclose(fp); return 5; }
  }
  if (fread(&nf, 4, 1, fp) != 1 || nf > 16) { fclose(fp); return 6; }
  s->nf = nf;
  size_t size = (size_t)s->nt * s->nr;
  for (int k = 0; k < nf; k++) {
    if (fread(s->name[k], 1, 16, fp) != 16) { fclose(fp); return 7; }
    s->name[k][16] = '\0';
    s->f[k] = (double *)malloc(sizeof(double) * size);
    if (fread(s->f[k], 8, size, fp) != size) { fclose(fp); return 8; }
  }
  fclose(fp);
  return 0;
}

int lip_src_field(const LipSrcTable *s, const char *name)
{
  for (int k = 0; k < s->nf; k++) if (strcmp(s->name[k], name) == 0) return k;
  return -1;
}

static int loc(const double *a, int n, double x, double *w)
{
  if (x <= a[0]) { *w = 0; return 0; }
  if (x >= a[n - 1]) { *w = 1; return n - 2; }
  int lo = 0, hi = n - 1;
  while (hi - lo > 1) { int m = (lo + hi) / 2; if (a[m] <= x) lo = m; else hi = m; }
  *w = (x - a[lo]) / (a[lo + 1] - a[lo]);
  return lo;
}

double lip_src_get(const LipSrcTable *s, int k, double t, double r)
{
  if (r > s->r[s->nr - 1]) return 0.0;        /* outside the irradiated rings: no source */
  if (t > s->t[s->nt - 1]) t = s->t[s->nt - 1];
  double wt, wr;
  int i = loc(s->t, s->nt, t, &wt), j = loc(s->r, s->nr, r, &wr);
  const double *f = s->f[k];
  int nr = s->nr;
  return (1 - wt) * ((1 - wr) * f[i * nr + j] + wr * f[i * nr + j + 1])
       + wt * ((1 - wr) * f[(i + 1) * nr + j] + wr * f[(i + 1) * nr + j + 1]);
}
