Enigma M4 simulator implementation for hardware library (kotlin virtual asm) with reflection schema from Russian Fialka M125 (magic-circuit).
![Снимок экрана 2024-03-03 в 21 16 51](https://github.com/Scalagrammer/enigma-hardware/assets/39617069/4898a052-626f-453c-a546-29c2b6a65944)
```
.define
req ra, rb, rc, re, rf, rg, rh, ex, sx, lx, mx, rx, gx
reg px
usr px, reset
usr px, play
usr px, show
arr rs [ra, rb, rc, rd, re, rf, rg, rh]
arr gs [0x4, 0xD, 0x8, 0x6, 0xC, 0x0, 0x5, 0x5, 0x5, 0x5]
.code
mov ra, 0x1
mov rb, 0x2
mov rc, 0x3
mov rd, 0x4
mov re, 0x5
mov rf, 0x6
mov rg, 0x7
mov rh, 0x8
seu
mov ex, 0x0
cal greeting
@main
cal await_key_pressed
cal cipher
mov px
jmp main
@reset
cal await_key_released
rst lx
ret
@play
mov mx
ret
@show
mov lx
ret
@cipher
cal shift_alphabet_space
mov sx, 0x1
@swap
mov rx, rs
cal translate
jxt rs, swap
psh sx
jeq 0x0, done
rvr rs
cal reflect
mov sx, 0x0
jmp swap
@done
rvr rs
ret
@greeting
mov gx, gs
jxt gs, greeting

ret
```
