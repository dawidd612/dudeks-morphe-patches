#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <limits.h>
#include <stdlib.h>
void repair_stars(void *, int);
static unsigned char player[1200];
static int marker, failure, calls, granted, uid=10123;
static char saved_path[128];
static void reset(int e, int s) { memset(player,0,sizeof(player)); memcpy(player+912,&e,4); memcpy(player+1016,&s,4); marker=failure=calls=granted=0;uid=10123; }
int game_get_int(void *p) { int v;memcpy(&v,p,4);return v; }
void game_add_stars(void *p, int amount) { calls++;granted=amount;int v=game_get_int((char*)p+912)+amount;memcpy((char*)p+912,&v,4); }
long repair_syscall(long n,long a,long b,long c,long d) {
    switch(n) {
    case 174:return uid;
    case 56: strcpy(saved_path,(char*)b);assert(d==0600);return failure==1?-13:7;
    case 32:return failure==2?-11:0;
    case 63:if(failure==3)return -5;if(marker){*(char*)b='D';return 1;}return 0;
    case 64:assert(*(char*)b=='D');if(failure==4)return -5;marker=1;return 1;
    case 57:case 82:return 0;
    default:abort();
    }
}
static int balance(void){return game_get_int(player+912)-game_get_int(player+1016);}
int main(void) {
    reset(1271,2672);repair_stars(player,1);assert(balance()==2 && marker && calls==1 && granted==1403);
    repair_stars(player,1);assert(balance()==3 && granted==1); /* no repeated grant */
    int s=9999;memcpy(player+1016,&s,4);repair_stars(player,1);assert(granted==1); /* persistent marker */
    reset(0,1402);repair_stars(player,1);assert(balance()==2 && marker);
    reset(-1401,0);repair_stars(player,1);assert(balance()==2 && marker);
    reset(0,1);repair_stars(player,1);assert(balance()==2 && marker);
    reset(12,10);repair_stars(player,1);assert(balance()==3 && !marker);
    reset(10,10);repair_stars(player,1);assert(balance()==1 && !marker);
    reset(0,1401);repair_stars(player,0);assert(balance()==-1401 && !marker);
    reset(0,1401);repair_stars(player,-1);assert(balance()==-1402 && !marker);
    reset(0,1401);failure=1;repair_stars(player,1);assert(balance()==-1400 && !marker);
    reset(0,1401);failure=2;repair_stars(player,1);assert(balance()==-1400 && !marker);
    reset(0,1401);failure=3;repair_stars(player,1);assert(balance()==-1400 && !marker);
    reset(0,1401);failure=4;repair_stars(player,1);assert(balance()==2 && !marker);repair_stars(player,1);assert(balance()==3);
    reset(0,1401);uid=1010123;repair_stars(player,1);assert(strstr(saved_path,"/data/user/10/")==saved_path && balance()==2);
    reset(0,1401);uid=-1;repair_stars(player,1);assert(balance()==-1400 && !marker);
    reset(0,INT_MAX);repair_stars(player,1);assert(!marker && granted==1);
    reset(INT_MIN,0);repair_stars(player,1);assert(!marker && granted==1);
    reset(0,1401);marker=1;repair_stars(player,1);assert(balance()==-1400 && granted==1);
    puts("PASS: 19 production repair scenarios, including persistence, overflow and I/O failures");
}
