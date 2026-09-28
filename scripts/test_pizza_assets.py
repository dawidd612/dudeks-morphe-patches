"""Black-box regression checks for split/native/asset preservation."""
import io
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
import zipfile
from build_pizza_native_delta import encode

LIBRARY = "lib/arm64-v8a/libcocos2dcpp.so"


def archive(entries):
    data = io.BytesIO()
    with zipfile.ZipFile(data, "w") as output:
        for name, value in entries.items():
            output.writestr(name, value)
    return data.getvalue()


class AssetPreservationTests(unittest.TestCase):
    def verify(self, patched_entries, extra_split=None, delta=None):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            splits = {"base.apk": archive({"assets/base": b"base"}),
                      "split_native.apk": archive({LIBRARY: b"native"}),
                      "split_assets.apk": archive({"assets/save-template": b"initial"})}
            if extra_split:
                splits["split_conflict.apk"] = archive(extra_split)
            (root / "original.apkm").write_bytes(archive(splits))
            (root / "patched.apk").write_bytes(archive(patched_entries))
            arguments = [sys.executable, str(Path(__file__).with_name("verify_pizza_assets.py")),
                         str(root / "original.apkm"), str(root / "patched.apk")]
            if delta is not None:
                (root / "bootstrap.gz").write_bytes(delta)
                arguments += ["--bootstrap-delta", str(root / "bootstrap.gz")]
            return subprocess.run(arguments,
                                  capture_output=True, text=True)

    def originals(self):
        return {"assets/base": b"base", LIBRARY: b"native",
                "assets/save-template": b"initial"}

    def test_preserved_all_splits(self):
        result = self.verify(self.originals())
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('"verified_assets": 2', result.stdout)

    def test_changed_native_library_fails(self):
        entries = self.originals()
        entries[LIBRARY] = b"changed"
        self.assertNotEqual(self.verify(entries).returncode, 0)

    def test_missing_asset_split_fails(self):
        entries = self.originals()
        del entries["assets/save-template"]
        self.assertNotEqual(self.verify(entries).returncode, 0)

    def test_conflicting_input_splits_fail(self):
        self.assertNotEqual(self.verify(self.originals(), {"assets/base": b"conflict"}).returncode, 0)

    def test_exact_bootstrap_reconstruction_passes(self):
        entries = self.originals()
        entries[LIBRARY] = b"restored"
        result = self.verify(entries, delta=encode(b"native", b"restored"))
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_bootstrap_does_not_allow_arbitrary_native_changes(self):
        entries = self.originals()
        entries[LIBRARY] = b"wrong"
        self.assertNotEqual(self.verify(entries, delta=encode(b"native", b"restored")).returncode, 0)

    def test_bootstrap_does_not_allow_asset_changes(self):
        entries = self.originals()
        entries[LIBRARY] = b"restored"
        entries["assets/save-template"] = b"wrong"
        self.assertNotEqual(self.verify(entries, delta=encode(b"native", b"restored")).returncode, 0)

    def test_bootstrap_for_different_input_fails(self):
        self.assertNotEqual(self.verify(self.originals(), delta=encode(b"other", b"native")).returncode, 0)


if __name__ == "__main__":
    unittest.main()
