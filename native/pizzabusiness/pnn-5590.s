// Success-only per-NewsLayer completion counter. Saved statistics stay intact.
ldr w9, [x19, #0x520]
add w9, w9, #1
str w9, [x19, #0x520]
add w2, w0, #1
b #{count_return}
nop
nop
nop
// Repeat PNN, then preserve the original PNN2 final reward/close flow.
ldr w9, [x19, #0x520]
cmp w9, #{last_pnn}
adrp x1, #{pnn_page}
add x1, x1, #{pnn_low}
b.lt pnn_selected
adrp x1, #{pnn2_page}
add x1, x1, #{pnn2_low}
pnn_selected:
b #{select_return}
// Match the reward animation's {gem type, quantity} to the real payout.
mov x8, #1
movk x8, #{gems}, lsl #32
b #{animation_return}
nop
