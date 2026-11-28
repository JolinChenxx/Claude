#include "pluto.h"
void ComputeUserVar(const Data *d,Grid *grid){
  int i,j,k,n;double v[NVAR],T;double ***temp=GetUserVar("temperature");
  DOM_LOOP(k,j,i){for(n=0;n<NVAR;n++)v[n]=d->Vc[n][k][j][i];
    GetPV_Temperature(v,&T);temp[k][j][i]=T;}
}
void ChangeOutputVar(void){}

