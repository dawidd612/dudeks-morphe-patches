// NewsLayer's added counter is zeroed with the entire expanded allocation.
// Count only successful PNN callbacks; failed/cancelled ads do not consume it.
ldr w9, [x19, #0x520]
add w9, w9, #1
str w9, [x19, #0x520]
add w2, w0, #1
b #0x136c4dc
nop
nop
nop
// First request uses the original empty -> PNN path. Repeat PNN until 999
// completions, then retain the stock PNN2 final reward/close path for #1000.
ldr w9, [x19, #0x520]
cmp w9, #999
adrp x1, #0x7e2000
add x1, x1, #0x409
b.lt pnn_selected
adrp x1, #0x7a7000
add x1, x1, #0xaca
pnn_selected:
b #0x1369aa8
