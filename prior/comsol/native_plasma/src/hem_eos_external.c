/* Common-pressure additive-volume Cu-air equilibrium flash for COMSOL.
 * SI values; nuclear Cu EOS only. init reads the audited binary EOS table.
 * No equilibrium electronic energy and no fitted plume source.
 */
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define API __declspec(dllexport)
static int nt=0,nr=0;
static double *lt=NULL,*lr=NULL,*tab=NULL;
static _Thread_local const char *error=NULL;
static double mx(double a,double b){return a>b?a:b;}
static double mn(double a,double b){return a<b?a:b;}
static int lower(const double *a,int n,double x){int l=0,h=n-1;while(h-l>1){int m=(h+l)/2;if(a[m]<=x)l=m;else h=m;}return l;}
static double at(int i,int j,int k){return tab[((size_t)i*nr+j)*7+k];}
API int init(const char *path){
 error=NULL;FILE *f=fopen(path,"rb");if(!f){error="Cannot open audited nuclear copper EOS binary";return 0;}
 int dims[2];if(fread(dims,sizeof(int),2,f)!=2){fclose(f);return 0;}
 if(lt){free(lt);free(lr);free(tab);}nt=dims[0];nr=dims[1];
 lt=malloc(nt*sizeof(double));lr=malloc(nr*sizeof(double));tab=malloc((size_t)nt*nr*7*sizeof(double));
 int ok=lt&&lr&&tab&&fread(lt,sizeof(double),nt,f)==nt&&fread(lr,sizeof(double),nr,f)==nr&&fread(tab,sizeof(double),(size_t)nt*nr*7,f)==(size_t)nt*nr*7;
 fclose(f);if(!ok)error="EOS binary allocation or read failed";return ok;
}
API const char *getLastError(void){return error;}
static int state(double logT,double logRc,double logRa,double out[6]){
 if(!isfinite(logT)||!isfinite(logRc)||!isfinite(logRa))return 0;
 if(logT<lt[0]||logT>lt[nt-1]||logRc>lr[nr-1])return 0;
 double T=pow(10.,logT),rc=pow(10.,logRc),ra=pow(10.,logRa),rho=rc+ra;
 if(!(rho>0)||!isfinite(rho))return 0;
 if(rc<1e-100){out[0]=717.5*T;out[1]=ra*287*T;out[2]=out[3]=out[5]=1.;out[4]=sqrt(1.4*287*T);out[5]=0.;return 1;}
 int it=lower(lt,nt,logT);double a=(logT-lt[it])/(lt[it+1]-lt[it]);
 double low=mx(logRc,lr[0]),high=lr[nr-1],ri=low;
 if(ra>0){
  int jl=lower(lr,nr,low),jh=nr-1;
  while(jh-jl>1){int j=(jl+jh)/2;double pj=(1-a)*at(it,j,0)+a*at(it+1,j,0);double f=pj*(1-rc/pow(10.,lr[j]))-ra*287*T;if(f<0)jl=j;else jh=j;}
  low=mx(low,lr[jl]);high=lr[jh];
  double pl=(1-a)*at(it,jl,0)+a*at(it+1,jl,0),ph=(1-a)*at(it,jh,0)+a*at(it+1,jh,0);
  double slope=(ph-pl)/(lr[jh]-lr[jl]);ri=(low+high)/2;
  for(int k=0;k<20;k++){
   double pc=pl+slope*(ri-lr[jl]),ac=rc/pow(10.,ri),f=pc*(1-ac)-ra*287*T;
   if(f>0)high=ri;else low=ri;
   if(fabs(f)<1e-10*mx(mx(pc,ra*287*T),1e-12))break;
   double df=slope*(1-ac)+pc*ac*log(10.),trial=ri-f/mx(df,1e-100);
   if(trial<=low||trial>=high)trial=(low+high)/2;ri=trial;
  }
 }
 int j=lower(lr,nr,ri);double b=(ri-lr[j])/(lr[j+1]-lr[j]),q[7];
 for(int k=0;k<7;k++)q[k]=(1-a)*((1-b)*at(it,j,k)+b*at(it,j+1,k))+a*((1-b)*at(it+1,j,k)+b*at(it+1,j+1,k));
 double ac=rc/pow(10.,ri);
 out[0]=(rc*q[1]+ra*717.5*T)/rho;out[1]=q[0];out[2]=q[4];out[3]=1-ac+ac*q[5];out[4]=mx(q[3],sqrt(1.4*287*T));out[5]=ac;
 for(int k=0;k<6;k++)if(!isfinite(out[k]))return 0;return 1;
}
static int state_energy(double energy,double logRc,double logRa,double out[6]){
 double ql[6],qh[6],q[6];int l=0,h=nt-1;
 if(!isfinite(energy))return 0;
 if(!state(lt[0],logRc,logRa,ql)||!state(lt[nt-1],logRc,logRa,qh))return 0;
 if(energy<ql[0]||energy>qh[0])return 0;
 while(h-l>1){int mid=(l+h)/2;if(!state(lt[mid],logRc,logRa,q))return 0;if(q[0]<energy)l=mid;else h=mid;}
 double lo=lt[l],hi=lt[h];if(!state(lo,logRc,logRa,ql)||!state(hi,logRc,logRa,qh))return 0;
 double e0=ql[0],e1=qh[0],logT=lo+(hi-lo)*(energy-e0)/mx(e1-e0,1e-100);
 for(int k=0;k<20;k++){
  if(!state(logT,logRc,logRa,q))return 0;double f=q[0]-energy;
  if(fabs(f)<1e-9*mx(fabs(energy),1e4))break;
  if(f>0){hi=logT;e1=q[0];}else{lo=logT;e0=q[0];}
  logT=lo+(hi-lo)*(energy-e0)/mx(e1-e0,1e-100);
 }
 out[0]=pow(10.,logT);for(int k=1;k<6;k++)out[k]=q[k];return 1;
}
API int eval(const char *func,int nArgs,const double **inReal,const double **inImag,int n,double *outReal,double *outImag){
 error=NULL;if(nArgs!=3||inImag){error="HEM expects three real logarithmic arguments";return 0;}
 const char *namesT[]={"hemE","hemP","hemXv","hemAg","hemCs","hemAc"};const char *namesE[]={"eeT","eeP","eeXv","eeAg","eeCs","eeAc"};int energyMode=!strncmp(func,"ee",2);const char **names=energyMode?namesE:namesT;int index=-1,der=-1;
 for(int k=0;k<6;k++){size_t sz=strlen(names[k]);if(!strncmp(func,names[k],sz)){if(func[sz]=='\0'){index=k;break;}if(func[sz]=='_'&&func[sz+1]=='d'&&func[sz+2]>='0'&&func[sz+2]<='2'){index=k;der=func[sz+2]-'0';break;}}}
 if(index<0){error="Unknown HEM function";return 0;}
 for(int i=0;i<n;i++){
  double x[3]={inReal[0][i],inReal[1][i],inReal[2][i]},q[6];
  if(der<0){if(!(energyMode?state_energy(x[0],x[1],x[2],q):state(x[0],x[1],x[2],q))){error="State outside nuclear copper EOS range";return 0;}outReal[i]=q[index];}
  else{
   double step=energyMode&&der==0?mx(1e-2,fabs(x[0])*1e-7):1e-8,qp[6],qm[6];x[der]+=step;int ok=energyMode?state_energy(x[0],x[1],x[2],qp):state(x[0],x[1],x[2],qp);x[der]-=2*step;ok=ok&&(energyMode?state_energy(x[0],x[1],x[2],qm):state(x[0],x[1],x[2],qm));
   if(!ok){error="HEM derivative outside EOS range";return 0;}outReal[i]=(qp[index]-qm[index])/(2*step);
  }
 }
 return 1;
}
