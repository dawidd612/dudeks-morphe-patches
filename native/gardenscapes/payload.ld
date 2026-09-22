ENTRY(repair_entry)
game_get_int = 0x38b6d08;
game_add_resume = 0x38bd6e8;
SECTIONS {
    . = 0x79a1000;
    .payload : { KEEP(*(.text.entry)) *(.text*) *(.rodata*) }
    /DISCARD/ : { *(.comment) *(.note*) *(.eh_frame*) }
}
ASSERT(SIZEOF(.data) == 0, "payload must not have writable data")
ASSERT(SIZEOF(.bss) == 0, "payload must not have writable state")
