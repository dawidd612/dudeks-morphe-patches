"""Execute the compiled ARM64 hook, trampoline and syscalls in Unicorn.
Only engine property access / its original transaction are stubbed, not our hook.
"""
import base64, pathlib, struct
from unicorn import Uc, UC_ARCH_ARM64, UC_MODE_ARM, UC_HOOK_CODE, UC_HOOK_INTR
from unicorn.arm64_const import *
root=pathlib.Path(__file__).resolve().parents[1]
payload=base64.b64decode((root/'patches/src/main/resources/gardenscapes/repair-arm64.b64').read_text())
checks=0
for bias in [0,0x7100000000]:
    u=Uc(UC_ARCH_ARM64,UC_MODE_ARM)
    for a,n in [(bias+0x38b4000,0x10000),(bias+0x79a0000,0x4000),(0x10000000,0x1000),(0x11000000,0x10000),(0x12000000,0x1000)]:u.mem_map(a,n)
    u.mem_write(bias+0x79a1000,payload)
    branch=0x14000000|(((0x79a1000-0x38bd6e4)//4)&0x3ffffff)
    u.mem_write(bias+0x38bd6e4,struct.pack('<I',branch))
    marker=False; granted=[]; paths=[]; deny=False
    def integer(a):return struct.unpack('<i',bytes(u.mem_read(a,4)))[0]
    def code(uc,addr,size,data):
        if addr==bias+0x38b6d08:
            uc.reg_write(UC_ARM64_REG_X0,integer(uc.reg_read(UC_ARM64_REG_X0))&0xffffffff)
            uc.reg_write(UC_ARM64_REG_PC,uc.reg_read(UC_ARM64_REG_LR))
        elif addr==bias+0x38bd6e8:
            p=uc.reg_read(UC_ARM64_REG_X0);a=uc.reg_read(UC_ARM64_REG_W1)
            if a&0x80000000:a-=0x100000000
            granted.append(a)
            uc.mem_write(p+912,struct.pack('<i',integer(p+912)+a))
            uc.reg_write(UC_ARM64_REG_SP,uc.reg_read(UC_ARM64_REG_SP)+48)
            uc.reg_write(UC_ARM64_REG_PC,uc.reg_read(UC_ARM64_REG_LR))
    def syscall(uc,n,data):
        global marker
        call=uc.reg_read(UC_ARM64_REG_X8);a,b,c,d=[uc.reg_read(r) for r in [UC_ARM64_REG_X0,UC_ARM64_REG_X1,UC_ARM64_REG_X2,UC_ARM64_REG_X3]]
        if call==174:ret=1010123
        elif call==56:
            path=bytes(uc.mem_read(b,128)).split(b'\0')[0].decode();paths.append(path)
            assert path=='/data/user/10/com.playrix.gardenscapes/files/.dudek-stars-repaired-v1'
            assert c==2|64|0x20000|0x80000 and d==0o600
            ret=-13 if deny else 7
        elif call==32:assert a==7 and b==6;ret=0
        elif call==63:
            ret=int(marker)
            if marker:uc.mem_write(b,b'D')
        elif call==64:assert bytes(uc.mem_read(b,1))==b'D';marker=True;ret=1
        elif call in [57,82]:ret=0
        else:raise AssertionError(('Unexpected syscall',call))
        uc.reg_write(UC_ARM64_REG_X0,ret&0xffffffffffffffff)
    u.hook_add(UC_HOOK_CODE,code);u.hook_add(UC_HOOK_INTR,syscall)
    def run(e,s,a):
        u.mem_write(0x10000000+912,struct.pack('<i',e));u.mem_write(0x10000000+1016,struct.pack('<i',s))
        for r in range(UC_ARM64_REG_X19,UC_ARM64_REG_X28+1):u.reg_write(r,r*37)
        u.reg_write(UC_ARM64_REG_FP,0x12345678)
        u.reg_write(UC_ARM64_REG_X0,0x10000000);u.reg_write(UC_ARM64_REG_W1,a&0xffffffff)
        u.reg_write(UC_ARM64_REG_SP,0x1100fff0);u.reg_write(UC_ARM64_REG_LR,0x12000000)
        u.emu_start(bias+0x38bd6e4,0x12000000,count=100000)
        assert u.reg_read(UC_ARM64_REG_PC)==0x12000000
        assert u.reg_read(UC_ARM64_REG_SP)==0x1100fff0
        assert u.reg_read(UC_ARM64_REG_FP)==0x12345678
        for r in range(UC_ARM64_REG_X19,UC_ARM64_REG_X28+1):assert u.reg_read(r)==r*37
        return integer(0x10000000+912)-s
    assert run(1271,2672,1)==2 and marker and granted[-1]==1403;checks+=1
    assert run(2674,2672,1)==3 and granted[-1]==1;checks+=1
    assert run(1271,2672,1)==-1400 and granted[-1]==1;checks+=1
    marker=False;deny=True
    assert run(0,1401,1)==-1400 and not marker;checks+=1
    deny=False
    assert run(0,1401,0)==-1401 and not marker;checks+=1
    assert run(0,1401,-1)==-1402 and not marker;checks+=1
    assert run(0,0,1)==1 and not marker;checks+=1
    assert run(-1401,0,1)==2 and marker;checks+=1
print(f'PASS: {checks} compiled ARM64/ASLR scenarios, stack and callee-saved registers preserved')
