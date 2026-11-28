#define PHYSICS HD
#define DIMENSIONS 2
#define GEOMETRY CYLINDRICAL
#define BODY_FORCE NO
#define COOLING NO
#define RECONSTRUCTION LINEAR
#define TIME_STEPPING RK2
#define NTRACER 0
#define PARTICLES NO
#define USER_DEF_PARAMETERS 2
#define DUST_FLUID NO
#define EOS IDEAL
#define ENTROPY_SWITCH NO
#define INCLUDE_LES NO
#define THERMAL_CONDUCTION NO
#define VISCOSITY NO
#define ROTATING_FRAME NO
#define E_UJ 0
#define R0_UM 1
/* [Beg] user-defined constants (do not change this line) */
#define LIMITER VANLEER_LIM
#define SHOCK_FLATTENING MULTID
#define UNIT_DENSITY 1.e-3
#define UNIT_LENGTH 1.e-4
#define UNIT_VELOCITY 1.e5
/* [End] user-defined constants (do not change this line) */
