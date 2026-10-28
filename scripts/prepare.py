#!/usr/bin/env python3
"""Materialize pinned upstream sources and apply reviewable Roma integration changes."""
# SPDX-License-Identifier: GPL-3.0-or-later
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import plistlib
import re
import shutil
import subprocess
import tarfile
import tempfile

ROOT = Path(__file__).resolve().parents[1]
APP_ID = "org.roma.messenger.dev"


def git(*args: str, cwd: Path | None = None) -> str:
    return subprocess.check_output(["git", *args], cwd=cwd, text=True).strip()


def replace_exact(path: Path, old: str, new: str, count: int = 1) -> None:
    text = path.read_text()
    actual = text.count(old)
    if actual != count:
        raise ValueError(f"Upstream drift in {path.name}: expected {count} matches, got {actual}")
    path.write_text(text.replace(old, new))


def configure_android_scan_recovery(tree: Path) -> None:
    client = tree / "app/src/main/java/com/bitchat/android/mesh/BluetoothGattClientManager.kt"
    replace_exact(client, '    private val bleScanner: BluetoothLeScanner?',
                  '    private var bleScanner: BluetoothLeScanner?')
    replace_exact(client, '    private var isActive = false\n', '''    private var isActive = false

    private val adapterRecovery = org.roma.messenger.BluetoothScanRecovery(
        context.applicationContext,
        suspendScan = {
            scanDutyCycleJob?.cancel()
            scanDutyCycleJob = null
            stopScanWatchdog()
            onScanStateChanged(false)
            // Power loss invalidates the OS scan even when stopScan cannot run.
            isCurrentlyScanning = false
            scanCallback = null
            bleScanner = null
        },
        resumeScan = {
            if (isActive && bluetoothAdapter?.isEnabled == true && permissionManager.hasBluetoothPermissions()) {
                bleScanner = bluetoothAdapter.bluetoothLeScanner
                scanRetryCount = 0
                lastScanResultTime = 0L
                // Retain the upstream five-second start limit and power profile.
                restartScanning()
            }
        }
    )
''')
    replace_exact(client, '        if (bleScanner == null) {\n',
                  '        bleScanner = bluetoothAdapter?.bluetoothLeScanner\n        if (bleScanner == null) {\n')
    replace_exact(client, '        isActive = true\n',
                  '        isActive = true\n        adapterRecovery.start()\n')
    replace_exact(client, '    fun stop() {\n',
                  '    fun stop() {\n        adapterRecovery.stop()\n')
    replace_exact(client,
                  '        if (!permissionManager.hasBluetoothPermissions() || bleScanner == null || !isActive || !enabled) return',
                  '        val scanner = bleScanner ?: return\n'
                  '        if (!permissionManager.hasBluetoothPermissions() || !isActive || !enabled ||\n'
                  '            !scanningDesired || bluetoothAdapter?.isEnabled != true) return')
    replace_exact(client, '            bleScanner.startScan(', '            scanner.startScan(')
    replace_exact(client, '        if (!permissionManager.hasBluetoothPermissions() || bleScanner == null) return',
                  '        val scanner = bleScanner ?: return\n        if (!permissionManager.hasBluetoothPermissions()) return')
    replace_exact(client, '                    bleScanner.stopScan(it)', '                    scanner.stopScan(it)')


def configure_android(tree: Path) -> None:
    configure_android_scan_recovery(tree)
    # The upstream watch target shares the BLE client from the phone sources.
    replace_exact(tree / "wear/build.gradle.kts", 'val sharedSourceIncludes = listOf(\n',
                  'val sharedSourceIncludes = listOf(\n    "org/roma/messenger/BluetoothScanRecovery.kt",\n')
    replace_exact(tree / "app/build.gradle.kts", 'applicationId = "com.bitchat.droid"', f'applicationId = "{APP_ID}"')
    replace_exact(tree / "app/build.gradle.kts", 'versionName = "2.0.3"', 'versionName = "0.1.0-dev"')
    main = tree / "app/src/main"
    replace_exact(main / "java/com/bitchat/android/MainActivity.kt",
                  "ChatScreen(viewModel = chatViewModel)",
                  "org.roma.messenger.RomaRoot { ChatScreen(viewModel = chatViewModel) }")
    for path in (main / "res").glob("values*/strings.xml"):
        text = path.read_text()
        text = re.sub(r'(<string name="app_name"[^>]*>).*?(</string>)', r'\g<1>ROMA Dev\2', text)
        path.write_text(text)
    permission = "com.bitchat.android.permission.FORCE_FINISH"
    replace_exact(main / "AndroidManifest.xml", permission, f"{APP_ID}.permission.FORCE_FINISH", 2)
    constants = main / "java/com/bitchat/android/util/AppConstants.kt"
    replace_exact(constants, permission, f"{APP_ID}.permission.FORCE_FINISH")
    replace_exact(constants, "com.bitchat.android.ACTION_FORCE_FINISH", f"{APP_ID}.ACTION_FORCE_FINISH")
    # Download/update endpoints must never offer the original application's APK as Roma.
    for filename in ("GitHubReleaseClient.kt", "ApkDownloadSource.kt"):
        path = main / "java/com/bitchat/android/util" / filename
        text = path.read_text()
        if "permissionlesstech/bitchat-android" not in text:
            raise ValueError(f"Upstream download configuration changed: {filename}")
        path.write_text(text.replace("permissionlesstech/bitchat-android", "lucsantana/Roma")
                        .replace("bitchat-android-universal.apk", "roma-android-universal.apk"))
    # Preserve the upstream download-contract assertions with Roma's intentional endpoint.
    download_test = tree / "app/src/test/kotlin/com/bitchat/android/util/ApkDownloadSourceTest.kt"
    replace_exact(download_test, "permissionlesstech/bitchat-android", "lucsantana/Roma", 2)
    replace_exact(download_test, "bitchat-android-universal.apk", "roma-android-universal.apk")


def configure_ios_ble_discovery(tree: Path) -> None:
    # Android uses the public Bitchat service in both Debug and Release.
    # Upstream iOS Debug selects an isolated testnet and cannot discover it.
    replace_exact(tree / "bitchat/Services/BLE/BLEService.swift",
                  '    #if DEBUG\n'
                  '    static let serviceUUID = CBUUID(string: "F47B5E2D-4A9E-4C5A-9B3F-8E1D2C3A4B5A") // testnet\n'
                  '    #else\n'
                  '    static let serviceUUID = CBUUID(string: "F47B5E2D-4A9E-4C5A-9B3F-8E1D2C3A4B5C") // mainnet\n'
                  '    #endif\n',
                  '    static let serviceUUID = CBUUID(string: "F47B5E2D-4A9E-4C5A-9B3F-8E1D2C3A4B5C") // mainnet\n')


def configure_ios(tree: Path) -> None:
    configure_ios_ble_discovery(tree)
    replace_exact(tree / "bitchat/BitchatApp.swift", "            ContentView()",
                  "            RomaRootView { ContentView() }")
    config = tree / "Configs/Release.xcconfig"
    replace_exact(config, "PRODUCT_BUNDLE_IDENTIFIER = chat.bitchat", f"PRODUCT_BUNDLE_IDENTIFIER = {APP_ID}")
    replace_exact(config, "APP_GROUP_ID = group.chat.bitchat", f"APP_GROUP_ID = group.{APP_ID}")
    replace_exact(config, "DEVELOPMENT_TEAM = L3N5LHJD5Y", "DEVELOPMENT_TEAM =")
    replace_exact(config, "MARKETING_VERSION = 1.8.0", "MARKETING_VERSION = 0.1.0")
    for path in (tree / "bitchat/Info.plist", tree / "bitchatShareExtension/Info.plist"):
        data = plistlib.loads(path.read_bytes())
        data["CFBundleDisplayName"] = "ROMA Dev"
        for key, value in data.items():
            if key.startswith("NS") and key.endswith("UsageDescription") and isinstance(value, str):
                data[key] = value.replace("bitchat uses", "ROMA uses").replace("bitchat lets", "ROMA lets")
        path.write_bytes(plistlib.dumps(data, sort_keys=False))
    project = tree / "bitchat.xcodeproj/project.pbxproj"
    replace_exact(project, "INFOPLIST_KEY_CFBundleDisplayName = bitchat;",
                  'INFOPLIST_KEY_CFBundleDisplayName = "ROMA Dev";', 6)
    (tree / "Configs/Local.xcconfig.example").write_text(
        "// Copy to Local.xcconfig; never commit signing credentials.\n"
        "DEVELOPMENT_TEAM = YOUR_TEAM_ID\n"
        f"PRODUCT_BUNDLE_IDENTIFIER = {APP_ID}.$(DEVELOPMENT_TEAM)\n"
        f"APP_GROUP_ID = group.{APP_ID}.$(DEVELOPMENT_TEAM)\n")


def prepare(platform: str, offline: bool = False) -> Path:
    lock_bytes = (ROOT / "upstream.lock.json").read_bytes()
    lock = json.loads(lock_bytes)
    entry = lock[platform]
    revision = entry["revision"]
    if not re.fullmatch(r"[a-f0-9]{40}", revision):
        raise ValueError("An exact upstream commit is required")
    cache = ROOT / ".upstream" / entry["cache"]
    output_root = ROOT / ".build"
    destination = output_root / platform
    if destination.exists() or destination.is_symlink():
        raise FileExistsError(f"{destination.relative_to(ROOT)} already exists; preserve any work before removing it")
    if not cache.exists():
        if offline:
            raise FileNotFoundError(f"Missing cached upstream: {platform}")
        cache.mkdir(parents=True)
        git("init", cwd=cache)
        git("remote", "add", "origin", entry["repository"], cwd=cache)
    if git("remote", "get-url", "origin", cwd=cache) != entry["repository"]:
        raise ValueError("Upstream cache origin does not match the lockfile")
    if not offline:
        git("fetch", "--depth=1", "origin", revision, cwd=cache)
    if git("rev-parse", f"{revision}^{{commit}}", cwd=cache) != revision:
        raise ValueError("Upstream revision verification failed")
    output_root.mkdir(exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="roma-prepare-", dir=output_root) as temp:
        stage = Path(temp) / "source"
        stage.mkdir()
        archive = Path(temp) / "source.tar"
        subprocess.run(["git", "archive", "--format=tar", f"--output={archive}", revision], cwd=cache, check=True)
        with tarfile.open(archive) as source:
            source.extractall(stage, filter="data")
        license_hash = hashlib.sha256((stage / entry["license_file"]).read_bytes()).hexdigest()
        (configure_android if platform == "android" else configure_ios)(stage)
        overlay = ROOT / "apps" / platform / "overlay"
        shutil.copytree(overlay, stage, dirs_exist_ok=True)
        if hashlib.sha256((stage / entry["license_file"]).read_bytes()).hexdigest() != license_hash:
            raise ValueError("Upstream license was modified")
        manifest = {
            "schema": 1, "platform": platform, "upstream": entry,
            "lock_sha256": hashlib.sha256(lock_bytes).hexdigest(),
            "prepare_sha256": hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
            "overlay_sha256": {
                str(path.relative_to(overlay)): hashlib.sha256(path.read_bytes()).hexdigest()
                for path in sorted(overlay.rglob("*")) if path.is_file()
            },
        }
        (stage / "roma-build.json").write_text(json.dumps(manifest, indent=2) + "\n")
        os.replace(stage, destination)
    return destination


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("platform", choices=("android", "ios", "all"))
    parser.add_argument("--offline", action="store_true", help="Use existing cached Git objects only")
    args = parser.parse_args()
    for platform in (("android", "ios") if args.platform == "all" else (args.platform,)):
        print(f"Prepared {prepare(platform, args.offline).relative_to(ROOT)}")
