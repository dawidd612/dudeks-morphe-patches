"""Execute the reviewed sponsor hooks in an actual ELF; no Android reward loops."""
import argparse
import json
import struct
from pathlib import Path
from elftools.elf.elffile import ELFFile
from unicorn import Uc, UC_ARCH_ARM64, UC_MODE_ARM
from unicorn.arm64_const import UC_ARM64_REG_X0, UC_ARM64_REG_X1, UC_ARM64_REG_X2, UC_ARM64_REG_X8, UC_ARM64_REG_X19, UC_ARM64_REG_X20, UC_ARM64_REG_SP, UC_ARM64_REG_LR

ROOT = Path(__file__).resolve().parents[1]


def verify(library, layout_path=ROOT/'native/pizzabusiness/pnn-5590.json'):
    layout = json.loads(layout_path.read_text(encoding='utf-8'))
    addr = lambda name: int(layout[name], 0)
    machine = Uc(UC_ARCH_ARM64, UC_MODE_ARM)
    machine.mem_map(0, 0x2500000)
    with library.open('rb') as source:
        elf = ELFFile(source)
        for segment in elf.iter_segments():
            if segment['p_type']=='PT_LOAD':machine.mem_write(segment['p_vaddr'], segment.data())
    obj = 0x4000000
    machine.mem_map(obj, 0x2000)
    machine.mem_write(obj, b'\xa5'*0x528)
    machine.mem_write(obj+addr('counter_offset'), struct.pack('<I', 0))
    machine.reg_write(UC_ARM64_REG_X19, obj)
    machine.reg_write(UC_ARM64_REG_X20, obj+0x4c0)
    machine.reg_write(UC_ARM64_REG_SP, obj+0x1800)
    machine.reg_write(UC_ARM64_REG_LR, 0x12345678)
    guards = bytes(machine.mem_read(obj, 0x520))
    count = lambda: struct.unpack('<I', machine.mem_read(obj+addr('counter_offset'),4))[0]

    def select():
        machine.emu_start(addr('select_hook'), addr('select_return'), count=32)
        pointer = machine.reg_read(UC_ARM64_REG_X1)
        assert pointer in (addr('pnn'),addr('pnn2')), 'Unknown sponsor placement'
        return bytes(machine.mem_read(pointer, 5)).split(b'\0')[0]

    for completed in range(1,1000):
        machine.reg_write(UC_ARM64_REG_X0, completed+100)
        machine.emu_start(addr('count_hook'), addr('count_return'), count=32)
        assert count()==completed, 'Successful PNN completion did not increment its own counter'
        assert machine.reg_read(UC_ARM64_REG_X2)==completed+101, 'Saved PNN statistic changed'
        assert select()==(b'PNN' if completed<999 else b'PNN2'), 'Incorrect 1000-offer boundary'
        for _ in range(2):
            select()
            assert count()==completed, 'Selection without completion consumed an offer'
    machine.emu_start(addr('reward_amount'),addr('reward_amount')+4,count=16)
    assert machine.reg_read(UC_ARM64_REG_X1)==10, 'Sponsor payout must be 10 gems'
    machine.emu_start(addr('animation_hook'),addr('animation_return'),count=16)
    assert machine.reg_read(UC_ARM64_REG_X8)==(10<<32)|1, 'Reward animation must show 10 gems'
    assert bytes(machine.mem_read(obj,0x520))==guards, 'Original NewsLayer members changed'
    for reg,value in [(UC_ARM64_REG_X19,obj),(UC_ARM64_REG_X20,obj+0x4c0),(UC_ARM64_REG_SP,obj+0x1800),(UC_ARM64_REG_LR,0x12345678)]:
        assert machine.reg_read(reg)==value, 'Callee-saved register changed'
    machine.mem_write(obj+0x520,struct.pack('<I',0))
    assert select()==b'PNN', 'New layer must start a new sequence'
    print('PASS: actual ARM64 hooks: 999 PNN successes then PNN2; 10-gem payout/animation; no counter use without success; original state/registers preserved')


if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--library',required=True,type=Path)
    p.add_argument('--layout',type=Path,default=ROOT/'native/pizzabusiness/pnn-5590.json')
    args=p.parse_args()
    verify(args.library,args.layout)
