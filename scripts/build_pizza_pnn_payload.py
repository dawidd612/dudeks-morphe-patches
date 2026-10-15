"""Rebuild the exact 5.58.0 ARM64 PNN counter/selector (keystone-engine 0.9.2)."""
import base64
from pathlib import Path
from keystone import Ks, KS_ARCH_ARM64, KS_MODE_LITTLE_ENDIAN

ROOT = Path(__file__).resolve().parents[1]
ADDRESS = 0x23E0000

def build():
    source = (ROOT / 'native/pizzabusiness/pnn-5580.s').read_text()
    source = '\n'.join(line.split('//', 1)[0] for line in source.splitlines())
    code, _ = Ks(KS_ARCH_ARM64, KS_MODE_LITTLE_ENDIAN).asm(source, ADDRESS)
    assert len(code) == 64
    return bytes(code)

if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--update', action='store_true')
    args = parser.parse_args()
    path = ROOT / 'patches/src/main/resources/pizzabusiness/5.58.0/pnn-1000-arm64.b64'
    encoded = base64.b64encode(build()).decode() + '\n'
    if args.update:
        path.write_text(encoded)
    else:
        assert path.read_text() == encoded, 'PNN payload differs from assembly'
    print('PASS: reproducible 64-byte PNN ARM64 payload')
