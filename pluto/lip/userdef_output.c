/* userdef_output.c -- extra output variables: T [K], qlas, qrad [W/m^3] (declare in pluto.ini uservar). */
#include "lip_glue.h"

void ComputeUserVar(const Data *d, Grid *grid)
{
  int i, j, k;
  double ***T = GetUserVar("T"), ***ql = GetUserVar("qlas"), ***qr = GetUserVar("qrad");
  DOM_LOOP(k, j, i) {
    double v[NVAR];
    int nv;
    NVAR_LOOP(nv) v[nv] = d->Vc[nv][k][j][i];
    T[k][j][i] = lip_T_from_prim(v);
    ql[k][j][i] = lip_uservar("qlas", k, j, i);
    qr[k][j][i] = lip_uservar("qrad", k, j, i);
  }
}

void ChangeOutputVar(void) {}
