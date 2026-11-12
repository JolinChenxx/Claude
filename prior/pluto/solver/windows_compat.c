#include <stdint.h>
static uint64_t state48=0x1234abcd330eULL;
void srand48(long seed){state48=((uint64_t)(uint32_t)seed<<16)|0x330eULL;}
double drand48(void){state48=(state48*0x5deece66dULL+0xbULL)&0xffffffffffffULL;return (double)state48/281474976710656.;}
