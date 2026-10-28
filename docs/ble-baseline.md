# BLE reference baseline

Reference tag: `v0.1.0-ble-baseline`. This is a development checkpoint, not a production release or an emergency-readiness certification.

The application source matches `d56a3db` / `127c826`. Android retains its pinned upstream networking implementation; iOS uses the public Bitchat BLE service UUID in Debug and Release. The experimental Android scan-recovery changes are not included.

## Confirmed scope

These results were manually reported by the tester; they are not instrumented radio measurements. The latest confirmation covers the restored baseline and adding a third phone to test both platform pairings.

| Scenario | Result | Conditions |
| --- | --- | --- |
| Android ↔ iPhone discovery and individual text | Confirmed in both directions | Both apps open; Bluetooth on; Wi-Fi and cellular data off |
| iPhone ↔ iPhone discovery and individual text | Confirmed in both directions | Same foreground, offline conditions |

Device inventories, identifiers, message content and raw logs remain local. Exact per-device OS/build and app-revision records are incomplete, so these results do not establish compatibility with every supported model or OS version. Three phones in the test set do not by themselves prove multi-hop relaying.

## Limitations and remaining tests

- Recovery when both phones turn Bluetooth off and back on remains delayed or unreliable. Do not mark reconnect acceptance complete.
- Single-phone power-cycle recovery was reported working before the latest baseline confirmation; repeat it as part of systematic regression coverage.
- Locked screen, background operation, restart, duplicate handling, Android–Android and controlled multi-hop delivery remain unvalidated.
- Compatibility with original Bitchat clients and the full upstream feature inventory remain to be checked.
- Meshtastic, LoRa delivery and the BLE–LoRa bridge are not implemented or validated by this checkpoint.

Source integration, Android compilation/unit tests and iOS simulator compilation passed for the identical application source in [this workflow](https://github.com/lucsantana/Roma/actions/runs/40611360464). Compilation is separate from the manual checks above.

Continue tracking complete BLE acceptance in [issue #1](https://github.com/lucsantana/Roma/issues/1). This checkpoint does not close that issue or merge the implementation PR. The next product workstream is the Roma conversation/contact interface in [issue #2](https://github.com/lucsantana/Roma/issues/2).
