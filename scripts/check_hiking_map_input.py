#!/usr/bin/env python3
"""Check the supported input and, optionally, Morphe's patched output.

Requires androguard 4.1.3. No APKs or account data are uploaded.
"""
import argparse
import hashlib
import io
from pathlib import Path
from zipfile import ZipFile

from loguru import logger
logger.remove()
from androguard.core.apk import APK
from androguard.core.dex import DEX


def read_apk(path):
    data = Path(path).read_bytes()
    with ZipFile(io.BytesIO(data)) as archive:
        if 'AndroidManifest.xml' not in archive.namelist():
            data = archive.read('pl.mapa_turystyczna.app.apk')
    return data


def methods(data):
    result = {}
    with ZipFile(io.BytesIO(data)) as archive:
        for name in archive.namelist():
            if name.endswith('.dex'):
                for cls in DEX(archive.read(name)).get_classes():
                    for method in cls.get_methods():
                        key = (cls.get_name(), method.get_name(), method.get_descriptor())
                        result[key] = method
    return result


def instructions(method):
    return [(i.get_name(), i.get_output()) for i in method.get_instructions()]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('input', help='Original XAPK or base APK')
    parser.add_argument('--patched', help='APK produced by Morphe, using FULL bytecode mode')
    args = parser.parse_args()
    data = read_apk(args.input)
    apk = APK(data, raw=True)
    assert apk.get_package() == 'pl.mapa_turystyczna.app', 'Wrong package'
    assert apk.get_androidversion_name() == '1.16.6', 'Wrong version'
    assert apk.get_androidversion_code() == '153', 'Wrong version code'
    original = methods(data)
    access_key = ('Lzj4;', 'b', '()Z')
    flow_key = ('Ljq;', 'j', '(Ljava/lang/Object; Lft0;)Ljava/lang/Object;')
    info = instructions(original[('Lzh4;', 'toString', '()Ljava/lang/String;')])
    assert any('PremiumInfo(currentOrder=' in value for _, value in info)
    assert any(', isTrialUsed=' in value for _, value in info)
    access = original[access_key]
    assert access.get_access_flags_string() == 'public final'
    assert access.get_code().get_registers_size() >= 2
    assert sum('Lzh4;->a()Z' in value for _, value in instructions(access)) == 1
    flow = instructions(original[flow_key])
    calls = [i for i, (_, value) in enumerate(flow) if 'Lzh4;->a()Z' in value]
    assert len(calls) == 1, 'Ambiguous flow check'
    index = calls[0]
    assert flow[index + 1][0] == 'move-result'
    assert 'Ljava/lang/Boolean;->valueOf(Z)' in flow[index + 2][1]
    assert not any(k[0].startswith('Lcom/pairip/') for k in original)
    print('Input OK: 1.16.6 (153); both feature checks matched; no PairIP classes')
    print('Base APK SHA-256:', hashlib.sha256(data).hexdigest())

    if args.patched:
        patched = methods(read_apk(args.patched))
        assert instructions(patched[access_key]) == [('const/4', 'v0, 1'), ('return', 'v0')]
        output_flow = instructions(patched[flow_key])
        output_calls = [i for i, (_, v) in enumerate(output_flow) if 'Lzh4;->a()Z' in v]
        assert len(output_calls) == 1
        replacement = output_flow[output_calls[0] + 1]
        assert replacement == ('const/16', flow[index + 1][1] + ', 1'), replacement
        for key, method in original.items():
            if key[0] in ('Lzh4;', 'Lni4;', 'Lci4;', 'Lri4;', 'Lxj4;',
                          'Lpl/mapa_turystyczna/shared/premium/data/network/PremiumOrderResource;'):
                assert instructions(method) == instructions(patched[key]), f'Account/order code changed: {key}'
        print('Output OK: both local checks patched; account/order model and repository unchanged')


if __name__ == '__main__':
    main()
