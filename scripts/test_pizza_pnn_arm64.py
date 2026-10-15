"""Execute the shipped PNN counter and selector on ARM64, optionally in a patched ELF."""
import argparse
import base64
import struct
from pathlib import Path
from unicorn import Uc, UC_ARCH_ARM64, UC_MODE_ARM
from unicorn.arm64_const import UC_ARM64_REG_X0, UC_ARM64_REG_X1, UC_ARM64_REG_X2, UC_ARM64_REG_X9, UC_ARM64_REG_X19, UC_ARM64_REG_X20, UC_ARM64_REG_SP, UC_ARM64_REG_LR

ROOT = Path(__file__).resolve().parents[1]
ENTRY = 0x23E0000
COUNT_RETURN = 0x136C4DC
SELECT_RETURN = 0x1369AA8
OBJECT = 0x4000000

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--library', type=Path)
    args = parser.parse_args()
    payload = base64.b64decode((ROOT / 'patches/src/main/resources/pizzabusiness/5.58.0/pnn-1000-arm64.b64').read_text())
    machine = Uc(UC_ARCH_ARM64, UC_MODE_ARM)
    machine.mem_map(0, 0x3000000)
    if args.library:
        from elftools.elf.elffile import ELFFile
        with args.library.open('rb') as source:
            elf = ELFFile(source)
            loads = [s for s in elf.iter_segments() if s['p_type'] == 'PT_LOAD']
            for segment in loads:
                machine.mem_write(segment['p_vaddr'], segment.data())
            added = loads[-1]
            assert added['p_vaddr'] == ENTRY and added['p_flags'] == 5
            assert added.data() == payload
        count_entry, select_entry = 0x136C4D8, 0x1369AA0
    else:
        machine.mem_write(ENTRY, payload)
        count_entry, select_entry = ENTRY, ENTRY + 32
    machine.mem_map(OBJECT, 0x2000)
    machine.mem_write(0x7E2409, b'PNN\0')
    machine.mem_write(0x7A7ACA, b'PNN2\0')
    machine.reg_write(UC_ARM64_REG_X19, OBJECT)
    machine.reg_write(UC_ARM64_REG_X20, OBJECT + 0x4C0)
    machine.reg_write(UC_ARM64_REG_SP, OBJECT + 0x1800)
    machine.reg_write(UC_ARM64_REG_LR, 0x12345678)
    machine.mem_write(OBJECT, b'\xa5' * 0x528)
    machine.mem_write(OBJECT + 0x520, struct.pack('<I', 0))
    guards = bytes(machine.mem_read(OBJECT, 0x520))

    def count():
        return struct.unpack('<I', machine.mem_read(OBJECT + 0x520, 4))[0]

    def select():
        machine.emu_start(select_entry, SELECT_RETURN, count=32)
        pointer = machine.reg_read(UC_ARM64_REG_X1)
        assert pointer in (0x7E2409, 0x7A7ACA)
        return 'PNN' if pointer == 0x7E2409 else 'PNN2'

    # The stock first request uses empty -> PNN, so exercise 999 successful
    # first-placement callbacks and their exact displaced saved-counter update.
    for completed in range(1, 1000):
        machine.reg_write(UC_ARM64_REG_X0, completed + 100)
        machine.emu_start(count_entry, COUNT_RETURN, count=32)
        assert count() == completed
        assert machine.reg_read(UC_ARM64_REG_X2) == completed + 101
        assert select() == ('PNN' if completed < 999 else 'PNN2')
        # Readiness failure/cancellation does not execute a success callback.
        for _ in range(3):
            select()
            assert count() == completed
    assert bytes(machine.mem_read(OBJECT, 0x520)) == guards
    assert machine.reg_read(UC_ARM64_REG_X19) == OBJECT
    assert machine.reg_read(UC_ARM64_REG_X20) == OBJECT + 0x4C0
    assert machine.reg_read(UC_ARM64_REG_SP) == OBJECT + 0x1800
    assert machine.reg_read(UC_ARM64_REG_LR) == 0x12345678
    # A new NewsLayer gets its own zeroed allocation, even on the same real day.
    machine.mem_write(OBJECT + 0x520, struct.pack('<I', 0))
    assert select() == 'PNN'
    print('PASS: 999 successful PNN callbacks -> PNN2 final offer; failures do not consume slots; counter resets with a new layer; original members/callee-saved registers unchanged')

if __name__ == '__main__':
    main()
