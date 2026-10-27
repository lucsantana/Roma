# SPDX-License-Identifier: GPL-3.0-or-later
import importlib.util
import json
from pathlib import Path
import plistlib
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location("prepare", ROOT / "scripts/prepare.py")
prepare = importlib.util.module_from_spec(spec)
spec.loader.exec_module(prepare)


class PreparationSafetyTests(unittest.TestCase):
    def test_changed_upstream_fails_without_rewriting_file(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "source"
            path.write_text("changed implementation")
            with self.assertRaises(ValueError):
                prepare.replace_exact(path, "expected upstream", "replacement")
            self.assertEqual(path.read_text(), "changed implementation")

    def test_ambiguous_match_fails_without_partial_edit(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "source"
            path.write_text("needle needle")
            with self.assertRaises(ValueError):
                prepare.replace_exact(path, "needle", "replacement")
            self.assertEqual(path.read_text(), "needle needle")

    def test_existing_output_is_never_overwritten(self):
        original_root = prepare.ROOT
        with tempfile.TemporaryDirectory() as directory:
            prepare.ROOT = Path(directory)
            try:
                (prepare.ROOT / "upstream.lock.json").write_bytes((ROOT / "upstream.lock.json").read_bytes())
                output = prepare.ROOT / ".build/android"
                output.mkdir(parents=True)
                sentinel = output / "local-work"
                sentinel.write_text("preserve this work")
                with self.assertRaises(FileExistsError):
                    prepare.prepare("android", offline=True)
                self.assertEqual(sentinel.read_text(), "preserve this work")
            finally:
                prepare.ROOT = original_root


class GeneratedSourceTests(unittest.TestCase):
    """Run after preparation. Checks actual upstream files, not fabricated network success."""

    def test_generated_identity_and_entry_points(self):
        android = ROOT / ".build/android"
        ios = ROOT / ".build/ios"
        self.assertTrue(android.exists(), "Prepare both platforms before running tests")
        self.assertTrue(ios.exists(), "Prepare both platforms before running tests")
        self.assertIn(f'applicationId = "{prepare.APP_ID}"', (android / "app/build.gradle.kts").read_text())
        manifest = (android / "app/src/main/AndroidManifest.xml").read_text()
        self.assertNotIn("com.bitchat.android.permission.FORCE_FINISH", manifest)
        self.assertIn(f"{prepare.APP_ID}.permission.FORCE_FINISH", manifest)
        self.assertIn("org.roma.messenger.RomaRoot", (android / "app/src/main/java/com/bitchat/android/MainActivity.kt").read_text())
        self.assertIn("RomaRootView { ContentView() }", (ios / "bitchat/BitchatApp.swift").read_text())
        config = (ios / "Configs/Release.xcconfig").read_text()
        self.assertIn(f"PRODUCT_BUNDLE_IDENTIFIER = {prepare.APP_ID}\n", config)
        self.assertIn("DEVELOPMENT_TEAM =\n", config)
        plist = plistlib.loads((ios / "bitchat/Info.plist").read_bytes())
        self.assertEqual(plist["CFBundleDisplayName"], "ROMA Dev")
        self.assertIn("bluetooth-central", plist["UIBackgroundModes"])
        self.assertIn("bluetooth-peripheral", plist["UIBackgroundModes"])

    def test_upstream_network_and_license_bytes_are_unchanged(self):
        lock = json.loads((ROOT / "upstream.lock.json").read_text())
        prefixes = {
            "android": ("app/src/main/java/com/bitchat/android/mesh/", "app/src/main/java/com/bitchat/android/protocol/", "app/src/main/java/com/bitchat/android/noise/"),
            "ios": ("bitchat/Services/BLE/", "bitchat/Noise/", "bitchat/Protocols/"),
        }
        for platform, entry in lock.items():
            if platform == "schema":
                continue
            cache = ROOT / ".upstream" / entry["cache"]
            tree = ROOT / ".build" / platform
            names = prepare.git("ls-tree", "-r", "--name-only", entry["revision"], cwd=cache).splitlines()
            checked = 0
            for name in names:
                if name == entry["license_file"] or name.startswith(prefixes[platform]):
                    original = subprocess.check_output(["git", "show", f'{entry["revision"]}:{name}'], cwd=cache)
                    self.assertEqual((tree / name).read_bytes(), original, f"Unexpected protocol/license change: {platform}/{name}")
                    checked += 1
            self.assertGreater(checked, 10, "A source layout change requires reviewing the protected paths")

    def test_android_downloads_target_roma(self):
        base = ROOT / ".build/android/app/src/main/java/com/bitchat/android/util"
        for name in ("GitHubReleaseClient.kt", "ApkDownloadSource.kt"):
            content = (base / name).read_text()
            self.assertIn("lucsantana/Roma", content)
            self.assertNotIn("permissionlesstech/bitchat-android/releases", content)


if __name__ == "__main__":
    unittest.main()
