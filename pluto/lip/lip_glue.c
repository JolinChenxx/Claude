/* lip_glue.c -- table loading, EOS helpers, the operator-split source step LipSource() and budgets.
 *
 * LipSource (called once per full step from the local copy of split_source.c), per radial column:
 *   1. T(ρ,p,Y) for every cell; α1064(ρ,T,Y) from opt.bin.
 *   2. Laser: collimated column marching from the top boundary down to the target face and back up
 *      after reflection of the unabsorbed part (1 − A(t,r)), A from the target source table.
 *   3. Radiation: Q = 4π·NEC (thin or trap per NEC_LIMIT), limited so that the cell does not cool below
 *      T_FLOOR within one step (the withheld energy is booked as "rad_limited").
 *   4. Evaporation (j = first interior row above the target): mass ṁ, Cu mass ṁ, z-momentum ṁ u_v and
 *      total enthalpy ṁ h_v enter as conservative sources.
 * Requires each MPI rank to hold complete z columns (run with -dec N 1).
 * Budgets (J, kg) are accumulated per rank and written by lip_analysis() to budget.csv; the surface
 * transmission I_surf/I_top(t, r) is appended to surftrans_<rank>.txt for the Picard iteration. */
#include "lip_glue.h"
#include <stdio.h>
#include <string.h>

LipEOS      lip_eos;
LipTable    lip_opt;
LipSrcTable lip_src;
static int  lip_ready = 0, i_alpha = -1, i_nec = -1, i_kappa = -1, i_D = -1, i_dh = -1;
static int  s_mdot, s_Tv, s_rhov, s_uv, s_A, s_Ts;
static double **q_las = NULL, **q_rad = NULL, **T_cell = NULL;
static double *trans = NULL;
static double B[9];   /* budget: inc, gas_abs, targ_abs, esc, rad, rad_limited, mass_src, energy_src, wall (gas->target) */

void lip_init(void)
{
  if (lip_ready) return;
  if (lip_eos_init(&lip_eos, "lip_tables/eos.bin")) { printLog("! lip: cannot read lip_tables/eos.bin\n"); QUIT_PLUTO(1); }
  if (lip_table_read("lip_tables/opt.bin", &lip_opt)) { printLog("! lip: cannot read lip_tables/opt.bin\n"); QUIT_PLUTO(1); }
  i_alpha = lip_table_field(&lip_opt, "alpha1064");
  i_nec = lip_table_field(&lip_opt, g_inputParam[NEC_LIMIT] > 0.5 ? "nec_trap" : "nec_thin");
  i_kappa = lip_table_field(&lip_opt, "kappa");
  i_D = lip_table_field(&lip_opt, "D_cuair"); i_dh = lip_table_field(&lip_opt, "dh_dY");
  if (lip_src_read("lip_tables/target_source.bin", &lip_src)) { printLog("! lip: cannot read target_source.bin\n"); QUIT_PLUTO(1); }
  s_mdot = lip_src_field(&lip_src, "mdot"); s_Tv = lip_src_field(&lip_src, "Tv");
  s_rhov = lip_src_field(&lip_src, "rhov"); s_uv = lip_src_field(&lip_src, "uv"); s_A = lip_src_field(&lip_src, "A"); s_Ts = lip_src_field(&lip_src, "Ts");
  if (s_mdot < 0 || s_Tv < 0 || s_rhov < 0 || s_uv < 0 || s_A < 0) { printLog("! lip: source table fields missing\n"); QUIT_PLUTO(1); }
#if THERMAL_CONDUCTION != NO
  if (i_kappa < 0 || s_Ts < 0 || i_D < 0 || i_dh < 0) { printLog("! lip: conduction build needs opt.bin fields kappa, D_cuair, dh_dY and source field Ts (v1 step-2 tables)\n"); QUIT_PLUTO(1); }
#endif
  memset(B, 0, sizeof(B));
  lip_ready = 1;
}

double lip_Y(const double *v)
{
  double Y = v[TRC];
  return Y < 0.0 ? 0.0 : (Y > 1.0 ? 1.0 : Y);
}

double lip_T_from_prim(double *v)
{
  double T;
  lip_init();
  lip_eos_T_from_p(&lip_eos, v[RHO] * LIP_RHO_SI, v[PRS] * LIP_PRS_SI, lip_Y(v), &T);
  return T;
}

double lip_kappa_SI(double *v)
{
  lip_init();
  if (i_kappa < 0) return 0.0;
  double T = lip_T_from_prim(v);
  return lip_interp(&lip_opt, i_kappa, log10(v[RHO] * LIP_RHO_SI), log10(T), lip_Y(v), 1);
}

double lip_cv_code(double *v)
{
  double T = lip_T_from_prim(v), rho = v[RHO] * LIP_RHO_SI, Y = lip_Y(v);
  double h = 1.0e-3;
  double cv = (lip_eos_e(&lip_eos, rho, T * (1 + h), Y) - lip_eos_e(&lip_eos, rho, T * (1 - h), Y)) / (2 * h * T);
  return cv / (LIP_VEL_SI * LIP_VEL_SI);   /* J/kg/K -> code energy per mass per K */
}

void lip_diff_code(double *v, double *D, double *dh)
{
  /* Cu–air binary diffusion coefficient [code length^2/time] and h_Cu - h_air [code energy/mass] */
  lip_init();
  double T = lip_T_from_prim(v), lr = log10(v[RHO] * LIP_RHO_SI), Y = lip_Y(v);
  *D  = lip_interp(&lip_opt, i_D,  lr, log10(T), Y, 1) / (LIP_LEN_SI * LIP_VEL_SI);
  *dh = lip_interp(&lip_opt, i_dh, lr, log10(T), Y, 0) / (LIP_VEL_SI * LIP_VEL_SI);
}

double lip_cu_vth_code(double *v)
{
  /* mean thermal speed of Cu atoms sqrt(8 k T / (pi m_Cu)) in code velocity units */
  const double kB = 1.380649e-23, mCu = 63.546 * 1.66053906660e-27;   /* registry k_B, A_Cu, amu */
  double T = lip_T_from_prim(v);
  return sqrt(8.0 * kB * T / (CONST_PI * mCu)) / LIP_VEL_SI;
}

static double laser_I(double r, double t)
{
  double w = 0.5 * g_inputParam[D_BEAM];
  double s = g_inputParam[FWHM] / (2.0 * sqrt(2.0 * log(2.0)));
  double P = g_inputParam[E_PULSE] / (s * sqrt(2.0 * CONST_PI)) * exp(-0.5 * pow((t - g_inputParam[T_PEAK]) / s, 2));
  return P * 2.0 / (CONST_PI * w * w) * exp(-2.0 * r * r / (w * w));
}

void LipSource(Data *d, double dt, timeStep *Dts, Grid *grid)
{
  int i, j, k = 0;
  double ***V = (double ***)d->Vc[0];
  double t_SI = g_time * LIP_TIME_SI, dt_SI = dt * LIP_TIME_SI;
  double inv_src = 0.0;   /* source-term time-step constraint (1/s), N-grade stability limits */
  /* diagnostic (T016e high_trap it3 stall): which cell and which source term sets inv_src */
  int lim_kind = 0, lim_i = -1, lim_j = -1; double lim_rho = 0, lim_T = 0, lim_rhoe = 0, lim_q = 0;
  double vmax = 0.0, vmax_rho = 0, vmax_T = 0, vmax_vr = 0, vmax_vz = 0; int vmax_i = -1, vmax_j = -1;
#define LIP_LIM(kind, val, q_) do { double _v = (val); if (_v > inv_src) { inv_src = _v; lim_kind = (kind); lim_i = i; lim_j = j; \
      lim_rho = rho; lim_T = T; lim_rhoe = rhoe; lim_q = (q_); } } while (0)
  const double F_E = 0.2, F_M = 0.5;
  (void)V;
  lip_init();
  if (grid->nproc[JDIR] != 1) { printLog("! LipSource: run with -dec N 1 (full z columns per rank)\n"); QUIT_PLUTO(1); }
  int nz = grid->np_int[JDIR];
  if (q_las == NULL) {
    q_las = ARRAY_2D(NX2_TOT, NX1_TOT, double);
    q_rad = ARRAY_2D(NX2_TOT, NX1_TOT, double);
    T_cell = ARRAY_2D(NX2_TOT, NX1_TOT, double);
    trans = ARRAY_1D(NX1_TOT, double);
  }
  double *dz = ARRAY_1D(nz, double), *al = ARRAY_1D(nz, double), *q = ARRAY_1D(nz, double);
  /* v1j droplet release: mass for this step (global, identical on all ranks) and the global weight sum */
  static double drop_done = 0.0;
  double drop_dm = 0.0, drop_W = 0.0;
  if (g_inputParam[DROP_M] > 0.0 && t_SI + dt_SI > g_inputParam[DROP_T0] && drop_done < g_inputParam[DROP_M]) {
    IDOM_LOOP(i) {
      double rr = grid->x[IDIR][i] * LIP_LEN_SI, dr = grid->dx[IDIR][i] * LIP_LEN_SI;
      JDOM_LOOP(j) {
        double Yc = d->Vc[TRC][k][j][i];
        if (Yc > 0.5) drop_W += d->Vc[RHO][k][j][i] * LIP_RHO_SI * Yc * 2.0 * CONST_PI * rr * dr * grid->dx[JDIR][j] * LIP_LEN_SI;
      }
    }
#ifdef PARALLEL
    { double wl = drop_W; MPI_Allreduce(&wl, &drop_W, 1, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD); }
#endif
    if (drop_W > 0.0) {
      double frac = (t_SI + dt_SI - g_inputParam[DROP_T0]) / (g_inputParam[DROP_T1] - g_inputParam[DROP_T0]);
      double target = g_inputParam[DROP_M] * (frac < 1.0 ? frac : 1.0);
      drop_dm = target - drop_done;
      if (drop_dm < 0.0) drop_dm = 0.0;
      drop_done += drop_dm;
    }
  }
  IDOM_LOOP(i) {
    double r = grid->x[IDIR][i] * LIP_LEN_SI;
    double dr = grid->dx[IDIR][i] * LIP_LEN_SI;
    double ring = 2.0 * CONST_PI * r * dr;
    /* 1. temperatures and absorption */
    for (j = JBEG; j <= JEND; j++) {
      double v[NVAR];
      int nv;
      NVAR_LOOP(nv) v[nv] = d->Vc[nv][k][j][i];
      double T = lip_T_from_prim(v);
      T_cell[j][i] = T;
      double rho = v[RHO] * LIP_RHO_SI, Y = lip_Y(v);
      dz[j - JBEG] = grid->dx[JDIR][j] * LIP_LEN_SI;
      al[j - JBEG] = lip_interp(&lip_opt, i_alpha, log10(rho), log10(T), Y, 1);
    }
    /* 2. laser */
    double I_top = laser_I(r, t_SI);
    double A_s = lip_src_get(&lip_src, s_A, t_SI, r);
    double I_s, I_e;
    double absd = lip_laser_column(nz, dz, al, I_top, A_s, q, &I_s, &I_e);
    trans[i] = I_top > 0.0 ? I_s / I_top : 1.0;
    B[0] += I_top * ring * dt_SI; B[1] += absd * ring * dt_SI;
    B[2] += A_s * I_s * ring * dt_SI; B[3] += I_e * ring * dt_SI;
    /* 3./4. update cells */
    for (j = JBEG; j <= JEND; j++) {
      double v[NVAR];
      int nv;
      NVAR_LOOP(nv) v[nv] = d->Vc[nv][k][j][i];
      double rho = v[RHO] * LIP_RHO_SI, Y = lip_Y(v), T = T_cell[j][i];
      double vr = v[VX1] * LIP_VEL_SI, vz = v[VX2] * LIP_VEL_SI;
      double rhoe = rho * lip_eos_e(&lip_eos, rho, T, Y);
      { double sp = sqrt(vr * vr + vz * vz);
        if (sp > vmax) { vmax = sp; vmax_i = i; vmax_j = j; vmax_rho = rho; vmax_T = T; vmax_vr = vr; vmax_vz = vz; } }
      double dV = ring * dz[j - JBEG];
      double ql = q[j - JBEG];
      double Q = 4.0 * CONST_PI * lip_interp(&lip_opt, i_nec, log10(rho), log10(T), Y, 1);
      double rhoe_floor = rho * lip_eos_e(&lip_eos, rho, g_inputParam[T_FLOOR], Y);
      double dE_rad = Q * dt_SI;
      if (rhoe + ql * dt_SI - dE_rad < rhoe_floor) {
        double lim = rhoe + ql * dt_SI - rhoe_floor;
        if (lim < 0.0) lim = 0.0;
        B[5] += (dE_rad - lim) * dV;
        dE_rad = lim;
      }
      B[4] += dE_rad * dV;
      q_las[j][i] = ql; q_rad[j][i] = dE_rad / dt_SI;
      double m = rho, mY = rho * Y, pz = rho * vz;
      double E = rhoe + 0.5 * rho * (vr * vr + vz * vz) + (ql * dt_SI - dE_rad);
#if THERMAL_CONDUCTION != NO
      /* v1: conductive exchange between the first gas cell and the target surface at T_s(t,r) (one-way):
         q = kappa (T_s - T_1) / (dz/2); energy enters/leaves the gas, booked as B[8] (positive = gas -> target).
         v1i: backward Euler in T with kappa frozen. Solve rho [e(T_n) - e(T)] = a (T_s - T_n), with
         a = kappa dt / (dz/2 · dz), by bisection on [min(T,T_s), max(T,T_s)]. The left side rises with T_n and the
         right side falls, so the root is unique and T_n stays between T and T_s: no overshoot, no explicit
         stability limit. As a·dt → 0 it reduces to the explicit flux. Same physics as v1h, but the rarefied cold
         cell next to the wall (rho ~ 1e-3 kg/m^3 after the plume leaves) no longer caps dt at ~1e-12 s. */
      if (j == JBEG && grid->lbound[JDIR] != 0 && s_Ts >= 0) {
        double Ts = lip_src_get(&lip_src, s_Ts, t_SI, r);
        if (Ts < g_inputParam[T_AMB]) Ts = g_inputParam[T_AMB];
        double kap = lip_interp(&lip_opt, i_kappa, log10(rho), log10(T), Y, 1);
        double a = kap * dt_SI / (0.5 * dz[0] * dz[0]);       /* J/m^3/K */
        double lo = T < Ts ? T : Ts, hi = T < Ts ? Ts : T, Tn = T;
        double e0 = lip_eos_e(&lip_eos, rho, T, Y);
        if (a > 0.0 && hi > lo) {
          int it;
          for (it = 0; it < 60 && hi - lo > 1e-9 * hi; it++) {
            Tn = 0.5 * (lo + hi);
            double f = rho * (lip_eos_e(&lip_eos, rho, Tn, Y) - e0) - a * (Ts - Tn);
            if (f > 0.0) hi = Tn; else lo = Tn;
          }
          Tn = 0.5 * (lo + hi);
        }
        double dEw = a * (Ts - Tn);                           /* J/m^3 (= flux at T_n times dt / dz) */
        if (rhoe + dEw < rhoe_floor) dEw = rhoe_floor - rhoe; /* cannot cool below T_floor */
        E += dEw;
        B[8] -= dEw * dV;
      }
#endif
      if (j == JBEG && g_inputParam[SRC_ON] > 0.5 && grid->lbound[JDIR] != 0) {
        double mdot = lip_src_get(&lip_src, s_mdot, t_SI, r);
        if (mdot > 0.0) {
          double Tv = lip_src_get(&lip_src, s_Tv, t_SI, r), rv = lip_src_get(&lip_src, s_rhov, t_SI, r);
          double uv = lip_src_get(&lip_src, s_uv, t_SI, r);
          double dm = mdot / dz[0] * dt_SI;
          double hv = lip_vapour_enthalpy(&lip_eos, rv, uv, Tv);
          /* v1 correction (pre-declared, PREREG_v0 §5 / STATE 11-17): inject mass and the vapour's stagnation
             enthalpy only; no direct momentum next to the reflecting wall (the ṁu_v momentum source drove
             E − ½ρv² negative in the first cells -> ConsToPrim repairs). The pressure accelerates the vapour. */
          m += dm; mY += dm; E += dm * hv;
          B[6] += dm * dV; B[7] += dm * hv * dV;
        }
      }
      /* v1j droplet bound (PREREG 2026-11-24): condensed ejecta evaporate inside the Cu core (Y > 0.5) during
         [DROP_T0, DROP_T1] (end of pulse to t_ref). Each cell receives the step's droplet mass in proportion to its rho·Y. The droplets arrive
         at rest (momentum conserved) with the Cu-vapour internal energy at DROP_TCAP minus the latent heat, so the gas
         pays L_v. Booked as source mass/energy (B[6], B[7]). Off when DROP_M = 0 (bit-identical to v1i). */
      double m_pre_drop = m;
      if (drop_dm > 0.0 && Y > 0.5) {
        double dmd = drop_dm * rho * Y / drop_W;                          /* kg/m^3 this step */
        double einj = lip_eos_e(&lip_eos, rho, g_inputParam[DROP_TCAP], 1.0) - g_inputParam[DROP_LV];
        m += dmd; mY += dmd; E += dmd * einj;
        B[6] += dmd * dV; B[7] += dmd * einj * dV;
      }
      /* stability limits for the next step: |Δ(rhoe)| ≤ F_E·rhoe, injected mass ≤ F_M·ρ, vapour signal speed */
      if (rhoe > 0.0) LIP_LIM(2, fabs(ql - dE_rad / dt_SI) / (F_E * rhoe), ql - dE_rad / dt_SI);
      if (m > rho) {
        double Sm = (m - rho) / dt_SI;
        LIP_LIM(3, Sm / (F_M * rho), Sm);
        if (j == JBEG) {
          double cv = sqrt(lip_eos_cs2(&lip_eos, m, T, mY / m));
          LIP_LIM(4, (fabs(vz) + cv) / dz[0], cv);
        }
      }
      double vz_new = pz / m;
      double vr_new = (m_pre_drop != m) ? vr * m_pre_drop / m : vr;        /* = vr exactly unless droplets were added */
      double rhoe_new = E - 0.5 * m * (vr_new * vr_new + vz_new * vz_new);
      double Y_new = mY / m, T_new;
      lip_eos_T_from_e(&lip_eos, m, rhoe_new / m, Y_new, &T_new);
      double p_new = lip_eos_p(&lip_eos, m, T_new, Y_new);
      d->Vc[RHO][k][j][i] = m / LIP_RHO_SI;
      d->Vc[VX2][k][j][i] = vz_new / LIP_VEL_SI;
      if (m_pre_drop != m) d->Vc[VX1][k][j][i] = vr_new / LIP_VEL_SI;
      d->Vc[PRS][k][j][i] = p_new / LIP_PRS_SI;
      d->Vc[TRC][k][j][i] = Y_new;
      T_cell[j][i] = T_new;
    }
  }
  FreeArray1D(dz); FreeArray1D(al); FreeArray1D(q);
  /* PLUTO computes dt_next = cfl / invDt_hyp (code time units); fold the source constraint in */
  if (g_stepNumber % 1000 == 0)
    printLog("! lip vmax: step %ld [i,j=%d,%d] |v|=%.3e m/s vr=%.3e vz=%.3e rho=%.3e T=%.3e\n",
             g_stepNumber, vmax_i, vmax_j, vmax, vmax_vr, vmax_vz, vmax_rho, vmax_T);
  if (g_stepNumber % 1000 == 0 && inv_src * LIP_TIME_SI / RuntimeGet()->cfl > Dts->invDt_hyp)
    printLog("! lip dt-limit: step %ld kind %d (1 wall 2 heat/rad 3 mass 4 vapour) [i,j=%d,%d] rho=%.3e T=%.3e rhoe=%.3e q=%.3e dt_src=%.3e s\n",
             g_stepNumber, lim_kind, lim_i, lim_j, lim_rho, lim_T, lim_rhoe, lim_q, 1.0 / inv_src);
  Dts->invDt_hyp = MAX(Dts->invDt_hyp, inv_src * LIP_TIME_SI / RuntimeGet()->cfl);
}

double lip_uservar(const char *name, int k, int j, int i)
{
  (void)k;
  if (q_las == NULL) return 0.0;
  if (!strcmp(name, "T")) return T_cell[j][i];
  if (!strcmp(name, "qlas")) return q_las[j][i];
  if (!strcmp(name, "qrad")) return q_rad[j][i];
  return 0.0;
}

void lip_analysis(const Data *d, Grid *grid)
{
  int i, j, k = 0;
  double tot[4] = {0, 0, 0, 0}, glob[4], Bg[9];
  lip_init();
  DOM_LOOP(k, j, i) {
    double v[NVAR];
    int nv;
    NVAR_LOOP(nv) v[nv] = d->Vc[nv][k][j][i];
    double rho = v[RHO] * LIP_RHO_SI, Y = lip_Y(v), T = lip_T_from_prim(v);
    double dV = 2.0 * CONST_PI * grid->x[IDIR][i] * grid->dx[IDIR][i] * grid->dx[JDIR][j] * pow(LIP_LEN_SI, 3);
    double vr = v[VX1] * LIP_VEL_SI, vz = v[VX2] * LIP_VEL_SI;
    tot[0] += rho * lip_eos_e(&lip_eos, rho, T, Y) * dV;
    tot[1] += 0.5 * rho * (vr * vr + vz * vz) * dV;
    tot[2] += rho * Y * dV;
    tot[3] += rho * dV;
  }
#ifdef PARALLEL
  MPI_Allreduce(tot, glob, 4, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
  MPI_Allreduce(B, Bg, 9, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
#else
  memcpy(glob, tot, sizeof(tot)); memcpy(Bg, B, sizeof(B));
#endif
  if (prank == 0) {
    FILE *fp = fopen("budget.csv", g_stepNumber == 0 ? "w" : "a");
    if (g_stepNumber == 0)
      fprintf(fp, "t_ns,E_internal,E_kinetic,M_Cu,M_total,E_inc,E_gas_abs,E_targ_abs,E_esc,E_rad,E_rad_limited,M_src,E_src,E_wall\n");
    fprintf(fp, "%.6e,%.10e,%.10e,%.10e,%.10e,%.10e,%.10e,%.10e,%.10e,%.10e,%.10e,%.10e,%.10e,%.10e\n", g_time,
            glob[0], glob[1], glob[2], glob[3], Bg[0], Bg[1], Bg[2], Bg[3], Bg[4], Bg[5], Bg[6], Bg[7], Bg[8]);
    fclose(fp);
  }
  if (trans != NULL && grid->lbound[JDIR] != 0) {
    char fn[64];
    sprintf(fn, "surftrans_%03d.txt", prank);
    FILE *fp = fopen(fn, "a");
    IDOM_LOOP(i) fprintf(fp, "%.6e %.6e %.6e\n", g_time * LIP_TIME_SI, grid->x[IDIR][i] * LIP_LEN_SI, trans[i]);
    fclose(fp);
  }
}
