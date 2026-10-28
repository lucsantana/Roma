# Physical acceptance tests

Initial foreground BLE tests have been manually confirmed for Android–iPhone and iPhone–iPhone, including individual text delivery in both directions without internet. See the [BLE reference baseline](ble-baseline.md) for evidence scope and open cases. LoRa has not been tested. Compilation and unit tests cannot establish radio interoperability.

## Record for every run

Record Roma commit, upstream lock revision, generated-source manifest, phone model, OS/build, app variant, permissions, app state, network state, direction, expected result and observed result. Never include private keys or conversation content in public logs.

## BLE baseline

The generated iOS and Android builds must use the same public Bitchat BLE service UUID, including Debug builds. The source integration test checks this contract. An unmodified upstream iOS Debug build uses a separate testnet and is not a valid public-network interoperability endpoint.

1. Install the development build on one Android phone and one iPhone. Keep Bluetooth on; disable cellular data and Wi-Fi.
2. Complete the upstream onboarding and grant needed Bluetooth permissions.
3. Discover each peer and exchange individual text messages in both directions.
4. Repeat with an original Bitchat client as one endpoint. Distinct installation IDs allow coexistence, but stop the other app during each test to avoid competing BLE sessions.
5. Repeat across the available iPhone generations, then the iPhone-to-iPhone pairs.
6. Check disconnect/reconnect, locked screen, background, restart and duplicate behavior. Record actual limitations instead of claiming continuous background delivery.
7. Verify native channels, private messaging and configuration remain reachable. Record each feature's parity status separately.

A second Android device is needed for physical Android-to-Android coverage; it does not block the cross-platform tests.

## LoRa and bridge, after implementation and radio purchase

First establish radio-to-radio communication with the original Meshtastic clients. Record both firmware versions, board revisions and compatible radio settings. Then test Roma-to-Roma and Roma-to-original-Meshtastic in both directions.

For the bridge, use three phones and two radios. Alternate Android and iPhone as the bridge. Control topology and inspect route evidence so a direct BLE path cannot produce a false LoRa success. Test return messages, duplicate suppression, unavailable routes and reconnection.

Public-network testing remains pending.

