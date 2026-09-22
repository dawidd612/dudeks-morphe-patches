.section .text.entry,"ax"
.global repair_entry
repair_entry:
    b repair_stars
.section .text.original,"ax"
.global game_add_stars
.hidden game_add_stars
game_add_stars:
    sub sp, sp, #48
    b game_add_resume
