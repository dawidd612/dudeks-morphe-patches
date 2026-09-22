/* Gardenscapes 9.9.0 arm64 only. Calls the original earned-star transaction. */
typedef long i64;
__attribute__((visibility("hidden"))) extern int game_get_int(void *);
__attribute__((visibility("hidden"))) extern void game_add_stars(void *, int);
#ifdef REPAIR_TEST
extern i64 repair_syscall(i64, i64, i64, i64, i64);
#else
static i64 repair_syscall(i64 n, i64 a, i64 b, i64 c, i64 d) {
    register i64 x8 __asm__("x8") = n;
    register i64 x0 __asm__("x0") = a;
    register i64 x1 __asm__("x1") = b;
    register i64 x2 __asm__("x2") = c;
    register i64 x3 __asm__("x3") = d;
    __asm__ volatile("svc #0" : "+r"(x0) : "r"(x8), "r"(x1), "r"(x2), "r"(x3) : "memory", "cc");
    return x0;
}
#endif
static int earned(void *p) { return game_get_int((char *)p + 912); }
static int spent(void *p) { return game_get_int((char *)p + 1016); }

__attribute__((visibility("hidden"))) void repair_stars(void *player, int amount) {
    if (amount <= 0) { game_add_stars(player, amount); return; }
    i64 e = earned(player), s = spent(player);
    i64 correction = s + 2 - e;
    if (e >= s || s < 0 || s > 2147483645L || correction > 2147483647L) {
        game_add_stars(player, amount); return;
    }
    i64 uid = repair_syscall(174, 0, 0, 0, 0); /* getuid */
    if (uid < 10000 || uid > 2147483647L) { game_add_stars(player, amount); return; }
    unsigned user = (unsigned)uid / 100000;
    char path[128], digits[12];
    const char *prefix = "/data/user/", *suffix = "/com.playrix.gardenscapes/files/.dudek-stars-repaired-v1";
    int pos = 0, count = 0;
    while (*prefix) path[pos++] = *prefix++;
    do { digits[count++] = '0' + user % 10; user /= 10; } while (user);
    while (count) path[pos++] = digits[--count];
    while (*suffix) path[pos++] = *suffix++;
    path[pos] = 0;
    /* O_RDWR | O_CREAT | O_NOFOLLOW | O_CLOEXEC; app-private, owner only. */
    i64 fd = repair_syscall(56, -100, (i64)path, 2 | 64 | 0x20000 | 0x80000, 0600);
    if (fd < 0) { game_add_stars(player, amount); return; }
    if (repair_syscall(32, fd, 6, 0, 0) != 0) { /* flock LOCK_EX | LOCK_NB */
        repair_syscall(57, fd, 0, 0, 0);
        game_add_stars(player, amount); return;
    }
    char done = 0;
    i64 size = repair_syscall(63, fd, (i64)&done, 1, 0);
    if (size != 0) { /* Completed marker or unreadable state: fail closed. */
        repair_syscall(57, fd, 0, 0, 0);
        game_add_stars(player, amount); return;
    }
    game_add_stars(player, (int)correction);
    if ((i64)earned(player) - spent(player) == 2) {
        done = 'D';
        if (repair_syscall(64, fd, (i64)&done, 1, 0) == 1)
            repair_syscall(82, fd, 0, 0, 0); /* fsync */
    }
    repair_syscall(57, fd, 0, 0, 0);
}
