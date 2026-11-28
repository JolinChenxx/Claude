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

/* Knight (AIAA J. 17, 519, 1979) Knudsen-layer jump for a monatomic vapour (gamma = 5/3) at exit Mach M. */
static void knight_M(double M, double *tr, double *rr)
{
  const double g = 5.0 / 3.0, sp = sqrt(CONST_PI_LIP);
  double m = M * sqrt(0.5 * g), b = (g - 1.0) / (g + 1.0) * 0.5 * m;
  double st = sqrt(1.0 + CONST_PI_LIP * b * b) - sp * b;
  double E = exp(m * m) * erfc(m);              /* m <= 0.913: no overflow */
  *tr = st * st;
  *rr = sqrt(1.0 / *tr) * ((m * m + 0.5) * E - m / sp) + 0.5 / *tr * (1.0 - sp * m * E);
}

double lip_knight_bp(double pr, double *M_out, double *tr_out, double *rr_out)
{
  double tr1, rr1, tr, rr, M;
  knight_M(1.0, &tr1, &rr1);
  if (pr <= rr1 * tr1) { M = 1.0; tr = tr1; rr = rr1; }
  else if (pr >= 1.0) { M = 0.0; tr = 1.0; rr = 1.0; }
  else {
    double lo = 0.0, hi = 1.0;                  /* p_v/p_sat falls monotonically with M */
    for (int it = 0; it < 60; it++) {
      M = 0.5 * (lo + hi);
      knight_M(M, &tr, &rr);
      if (rr * tr > pr) lo = M; else hi = M;
    }
    M = 0.5 * (lo + hi);
    knight_M(M, &tr, &rr);
  }
  if (M_out) *M_out = M;
  if (tr_out) *tr_out = tr;
  if (rr_out) *rr_out = rr;
  return rr * M * sqrt(tr) / (rr1 * sqrt(tr1));
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

/* ------------------------------------------------------------- v1m: two-temperature breakdown (Cu) */
#define BD_KB 1.380649e-23
#define BD_E  1.602176634e-19
#define BD_ME 9.1093837015e-31
#define BD_H  6.62607015e-34
#define BD_C  2.99792458e8
#define BD_EPS0 8.8541878128e-12
#define BD_MCU (63.546 * 1.66053906660e-27)
#define BD_CHI 7.72638          /* registry IP_Cu_I, eV */
#define BD_LAMBDA 1.064e-6

double lip_e1(double x)
{
  /* exponential integral E1(x), x > 0: series for x <= 1, continued fraction (Lentz) otherwise */
  if (x <= 1.0) {
    double s = 0.0, term = 1.0;
    for (int k = 1; k < 60; k++) {
      term *= -x / k;
      s += -term / k;
      if (fabs(term / k) < 1e-17 * fabs(s)) break;
    }
    return -0.57721566490153286 - log(x) + s;
  }
  double b = x + 1.0, c = 1e300, d = 1.0 / b, h = d;
  for (int i = 1; i < 300; i++) {
    double an = -(double)i * i;
    b += 2.0;
    d = 1.0 / (an * d + b);
    c = b + an / c;
    double del = c * d;
    h *= del;
    if (fabs(del - 1.0) < 1e-16) break;
  }
  return h * exp(-x);
}

double lip_k_ion_cu(double Te, double lotz_a)
{
  /* Lotz (1967) single-shell (4s, q = 1) Maxwellian rate, m^3/s; lotz_a in m^2 eV^2 */
  double kT = BD_KB * Te / BD_E;
  if (kT <= 0.0) return 0.0;
  double x = BD_CHI / kT;
  if (x > 700.0) return 0.0;
  return lotz_a / BD_CHI * sqrt(8.0 * BD_E / (CONST_PI_LIP * BD_ME * kT)) * lip_e1(x);
}

double lip_saha_cu(double Te)
{
  /* n_e n_II / n_I [m^-3]; Q_I = 2 + 6 exp(-1.389/kT) + 4 exp(-1.642/kT) (3d10 4s 2S, 3d9 4s2 2D), Q_II = 1 */
  double kT = BD_KB * Te / BD_E;
  double QI = 2.0 + 6.0 * exp(-1.389 / kT) + 4.0 * exp(-1.642 / kT);
  double lam = pow(2.0 * CONST_PI_LIP * BD_ME * BD_KB * Te / (BD_H * BD_H), 1.5);
  return 2.0 * (1.0 / QI) * lam * exp(-BD_CHI / kT);
}

double lip_alpha_2t(double ne, double Te, double n0, double sig_m)
{
  /* 1064 nm absorption with electron temperature Te: e-neutral Drude (momentum-transfer frequency) with the
     quantum factor (1 - e^-x)/x, plus e-ion Kramers (Z = 1, n_i = n_e, Gaunt 1) with stimulated emission. */
  double nu = BD_C / BD_LAMBDA, om = 2.0 * CONST_PI_LIP * nu, x = BD_H * nu / (BD_KB * Te);
  double vth = sqrt(8.0 * BD_KB * Te / (CONST_PI_LIP * BD_ME));
  double k_en = ne * BD_E * BD_E * (n0 * sig_m * vth) / (BD_EPS0 * BD_ME * BD_C * om * om) * (-expm1(-x)) / x;
  double e2 = BD_E * BD_E / (4.0 * CONST_PI_LIP * BD_EPS0);
  double K = (4.0 / 3.0) * sqrt(2.0 * CONST_PI_LIP / (3.0 * BD_ME * BD_KB * Te)) * e2 * e2 * e2 / (BD_H * BD_C * BD_ME * nu * nu * nu);
  return k_en + K * ne * ne * expm1(x);
}

double lip_te_qs(double I, double ne, double n0, double Th, double sig_m, double lotz_a)
{
  /* quasi-steady electron temperature: absorbed power per electron = elastic loss to heavies + ionisation power */
  if (ne <= 0.0 || I <= 0.0) return Th;
  double lo = Th, hi = 3.0e5;
  for (int it = 0; it < 80; it++) {
    double Te = 0.5 * (lo + hi);
    double P = lip_alpha_2t(ne, Te, n0, sig_m) * I / ne;
    double vth = sqrt(8.0 * BD_KB * Te / (CONST_PI_LIP * BD_ME));
    double Qel = 3.0 * BD_ME / BD_MCU * (n0 * sig_m * vth) * 1.5 * BD_KB * (Te - Th);
    double Qi = lip_k_ion_cu(Te, lotz_a) * n0 * BD_CHI * BD_E;
    if (P > Qel + Qi) lo = Te; else hi = Te;
    if (hi - lo < 1e-6 * hi) break;
  }
  return 0.5 * (lo + hi);
}

double lip_ne_step(double ne, double nCu, double Te, double dt, double lotz_a)
{
  /* backward Euler for dn/dt = k_i n (nCu - n) - (k_i / S) n^3 (Cu I <-> Cu II, three-body by detailed balance) */
  double ki = lip_k_ion_cu(Te, lotz_a), S = lip_saha_cu(Te);
  if (ki <= 0.0 || nCu <= 0.0) return ne;
  double n = ne;
  for (int it = 0; it < 60; it++) {
    double f = n - ne - dt * (ki * n * (nCu - n) - ki / S * n * n * n);
    double df = 1.0 - dt * (ki * (nCu - 2.0 * n) - 3.0 * ki / S * n * n);
    double nn = n - f / df;
    if (nn <= 0.0) nn = 0.5 * n;
    if (nn > nCu) nn = 0.5 * (n + nCu);
    if (fabs(nn - n) < 1e-12 * n) { n = nn; break; }
    n = nn;
  }
  return n;
}

/* ---- v1r droplet heat transfer (PREREG 2026-11-25) ---- */
double lip_drop_qfm(double rho, double T, double mbar, double deps)
{
  /* effusive heavy-particle mass flux rho*vbar/4 times the energy each kg gives up on accommodating to the droplet
     at T_b: deps = [e + p/(2 rho)](T) - [e + p/(2 rho)](T_b) (2kT translational per particle plus ionisation and
     excitation; electrons follow the ions, ambipolar). Accommodation coefficient 1 (upper bound). */
  const double kB = 1.380649e-23;
  if (rho <= 0.0 || T <= 0.0 || deps <= 0.0) return 0.0;
  return 0.25 * rho * sqrt(8.0 * kB * T / (3.14159265358979323846 * mbar)) * deps;
}

double lip_drop_q(double qfm, double K, double a)
{
  if (qfm <= 0.0 || K <= 0.0) return 0.0;
  return 1.0 / (1.0 / qfm + a / K);
}

double lip_drop_dm(double m, double m0, double a0, double rho_l, double qfm, double K, double Lv, double dt)
{
  if (m <= 0.0 || m0 <= 0.0) return 0.0;
  double a = a0 * cbrt(m / m0);
  double N = m0 / (4.0 / 3.0 * 3.14159265358979323846 * a0 * a0 * a0 * rho_l);
  double dm = dt * N * 4.0 * 3.14159265358979323846 * a * a * lip_drop_q(qfm, K, a) / Lv;
  return dm < m ? dm : m;
}
