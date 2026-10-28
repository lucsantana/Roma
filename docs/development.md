# Development setup

## Generate the native projects

Requirements: Git and Python 3.12 or newer. The script fetches exact upstream revisions, exports tracked source, applies Roma's integration changes and copies the overlays. It preserves upstream license files and writes a source manifest.

```sh
python3 scripts/prepare.py all
python3 -m unittest discover -s tests -v
```

Use `--offline` after the pinned repositories have been fetched. Generated projects live in `.build/android` and `.build/ios`; upstream Git objects live in `.upstream/`. Neither belongs in commits. Generation refuses to overwrite an existing output directory. Preserve any local changes before removing generated output and regenerating.

Edit `apps/*/overlay/` for Roma source additions and `scripts/prepare.py` for explicit upstream integration points. An unexpected upstream layout causes generation to fail rather than silently apply incomplete changes. `upstream.lock.json` is the source version authority.

## Android

Use JDK 21, Android platform API 37 (`platforms;android-37.0` in SDK Manager) and build tools 37.0.0, matching the pinned upstream catalog. Open `.build/android` in Android Studio or configure an SDK using `ANDROID_HOME`/local properties, then:

```sh
cd .build/android
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

The application ID is `org.roma.messenger.dev`, separate from the original Bitchat installation. Internal upstream package names remain unchanged for maintainability. APK download endpoints point to Roma; no signed Roma release channel is established by this development build. Debug APKs are not production releases.

## iPhone

Open Xcode and complete its license/first-launch setup. Select the complete Xcode developer directory for command-line builds, or set `DEVELOPER_DIR` for individual commands. Open `.build/ios/bitchat.xcodeproj`, select the `bitchat (iOS)` scheme and build.

For a simulator build without signing:

```sh
xcodebuild -project .build/ios/bitchat.xcodeproj \
  -scheme 'bitchat (iOS)' -configuration Debug \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath .build/ios-derived CODE_SIGNING_ALLOWED=NO build
```

For a physical iPhone, copy the generated `Configs/Local.xcconfig.example` to `Configs/Local.xcconfig` and configure your own signing team and unique app/group identifiers. Keep this file local. The upstream developer team is deliberately removed. App-group provisioning, signing and device trust still need validation; a simulator build does not establish installability on a phone or BLE operation.

## Verification and scope

Roma uses the public Bitchat BLE service UUID in both Debug and Release on both platforms. Pinned upstream iOS selects a separate testnet UUID in Debug, whereas Android always uses the public UUID. Preparation removes only that iOS selector so development builds can discover Android and public Bitchat peers. Debug diagnostics remain enabled; packet handling and cryptography are unchanged. This does not establish message-level interoperability, which still requires physical tests.

CI checks generation, matching BLE service/characteristic identifiers, protected upstream networking/license files (with only the documented iOS discovery selector exception), Android compilation/unit tests and iOS simulator compilation. Consult the actual run results; the presence of a workflow is not a passing build.

For physical validation use [testing.md](testing.md). The first integration retains Bitchat behavior under a Roma shell; the full [requirements](requirements.md) remain the product target.
