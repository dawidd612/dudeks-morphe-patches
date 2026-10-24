"""Build a reviewed PNN native delta using the existing PZB2 profile format."""
import argparse
import hashlib
import json
from pathlib import Path
import struct
from keystone import Ks, KS_ARCH_ARM64, KS_MODE_LITTLE_ENDIAN
from build_pizza_native_delta import encode

ROOT=Path(__file__).resolve().parents[1]


def build(original, layout):
    digest=hashlib.sha256(original).hexdigest()
    name=next((name for name,value in layout['inputs'].items() if value==digest),None)
    if name is None:raise ValueError('PNN requires the exact restored/hidden native input for this profile')
    addr=lambda key:int(layout[key],0)
    entry=addr('entry')
    values={key:addr(key) for key in ('count_return','select_return','animation_return')}
    values.update(last_pnn=layout['offers']-1,gems=layout['gems'],pnn_page=addr('pnn')&~0xfff,pnn_low=addr('pnn')&0xfff,pnn2_page=addr('pnn2')&~0xfff,pnn2_low=addr('pnn2')&0xfff)
    source=(ROOT/'native/pizzabusiness/pnn-5590.s').read_text(encoding='utf-8')
    source='\n'.join(line.split('//',1)[0] for line in source.splitlines()).format(**values)
    assembler=Ks(KS_ARCH_ARM64,KS_MODE_LITTLE_ENDIAN)
    payload=bytes(assembler.asm(source,entry)[0]);assert len(payload)==80
    phoff=struct.unpack_from('<Q',original,32)[0]
    size,count=struct.unpack_from('<HH',original,54)
    assert original[:6]==b'\x7fELF\x02\x01' and struct.unpack_from('<H',original,18)[0]==183
    assert phoff==64 and size==56 and count==14
    extra=phoff+size*count;assert original[extra:extra+size]==bytes(size)
    offset=(len(original)+0x3fff)&~0x3fff
    output=bytearray(original)+bytearray(offset+len(payload)-len(original))
    phdr=False
    for i in range(count):
        at=phoff+i*size;kind=struct.unpack_from('<I',original,at)[0]
        if kind==1:
            va=struct.unpack_from('<Q',original,at+16)[0];mem=struct.unpack_from('<Q',original,at+40)[0]
            assert va+mem<=entry
        elif kind==6:
            phdr=True;struct.pack_into('<QQ',output,at+32,(count+1)*size,(count+1)*size)
    assert phdr
    struct.pack_into('<H',output,56,count+1)
    struct.pack_into('<IIQQQQQQ',output,extra,1,5,offset,entry,entry,len(payload),len(payload),0x4000)
    output[offset:]=payload

    def edit(at,before,after):
        assert original[at:at+len(before)]==before,hex(at)
        output[at:at+len(after)]=after

    for i,at in enumerate(layout['allocation']):
        reg=0 if i==0 else 2 if i==1 else 1
        old=bytes(assembler.asm(f'mov w{reg}, #0x520')[0]);new=bytes(assembler.asm(f'mov w{reg}, #0x528')[0])
        edit(int(at,0),old,new)
    for key,destination in [('count_hook',entry),('select_hook',entry+32),('animation_hook',entry+64)]:
        at=addr(key)
        expected={'count_hook':bytes.fromhex('02040011'),'select_hook':bytes.fromhex('01a0fff0'),'animation_hook':bytes.fromhex('e80300b2')}[key]
        edit(at,expected,bytes(assembler.asm(f'b #{destination}',at)[0]))
    edit(addr('reward_amount'),bytes(assembler.asm('mov w1, #1')[0]),bytes(assembler.asm(f'mov w1, #{layout["gems"]}')[0]))
    return bytes(output),name


if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('input',type=Path);p.add_argument('output',type=Path)
    p.add_argument('--delta-dir',type=Path,required=True)
    args=p.parse_args();source=args.input.read_bytes()
    layout=json.loads((ROOT/'native/pizzabusiness/pnn-5590.json').read_text(encoding='utf-8'))
    output,name=build(source,layout)
    args.output.write_bytes(output);args.delta_dir.mkdir(parents=True,exist_ok=True)
    delta=args.delta_dir/name;delta.write_bytes(encode(source,output))
    print(json.dumps({'input_sha256':hashlib.sha256(source).hexdigest(),'output_sha256':hashlib.sha256(output).hexdigest(),'delta':str(delta),'delta_bytes':delta.stat().st_size,'output_bytes':len(output)}))
