# Roma

Roma is a community-driven, open-source protocol and application project designed to interconnect Bitchat's Bluetooth mesh network with LoRa-based Meshtastic networks, enabling communication without cellular service or internet access.

## Vision

Build an open interoperability layer and a unified application that allow messages to move between these networks through compatible bridge devices.

The planned architecture connects:

- **Bitchat networks:** local communication over Bluetooth mesh.
- **Meshtastic networks:** communication over LoRa radios connected to compatible devices.
- **Roma bridges:** a planned protocol translation and message forwarding layer between the two networks.

LoRa is the radio technology; Meshtastic is the initial target networking ecosystem for Roma's LoRa integration. Support for other LoRa-based protocols may be explored separately.

## Project status

Roma has an initial native BLE development integration for Android and iOS. It builds pinned Bitchat sources with a Roma shell and separate installation identifiers, preserving the upstream messaging engine. Source generation is tested; consult the CI results for native build status.

The [BLE reference baseline](docs/ble-baseline.md), tagged `v0.1.0-ble-baseline`, records manually confirmed foreground discovery and bidirectional individual text messaging without internet, for Roma Android ↔ Roma iOS and for each Roma platform ↔ original Bitchat iOS. Simultaneous Bluetooth power-cycle recovery remains a known limitation; full physical acceptance is still open.

This is not yet the complete Roma messenger. Roma-specific identity/history, the full interface, Meshtastic support and the BLE–LoRa bridge are not implemented. Cross-network delivery and encryption must not be assumed.

## Get started

```sh
git clone https://github.com/lucsantana/Roma.git
cd Roma
python3 scripts/prepare.py all
python3 -m unittest discover -s tests -v
```

Use Python 3.12+. See [development setup](docs/development.md) for Android and iPhone build/signing requirements. Network access is required to fetch the pinned sources and build dependencies.

## Repository layout

| Path | Purpose |
| --- | --- |
| `apps/android/overlay/` | Roma Kotlin/Compose additions |
| `apps/ios/overlay/` | Roma SwiftUI additions |
| `scripts/prepare.py` | Reproducible upstream source preparation and explicit integration changes |
| `upstream.lock.json` | Exact upstream revisions |
| `tests/` | Source integration and preparation safety checks |
| `docs/` | Requirements, architecture, setup, roadmap and physical acceptance tests |
| `.github/workflows/` | Android, iOS and integration checks |

Generated `.build/` projects and cached `.upstream/` sources are local outputs, not the source of truth for Roma changes.

Read the [requirements](docs/requirements.md), [architecture](docs/architecture.md), [roadmap and tracked tasks](docs/roadmap.md), [physical test plan](docs/testing.md), [contribution guide](CONTRIBUTING.md) and [third-party notices](THIRD_PARTY.md).

## Development language

**English is the default language for development.** Use English for source code identifiers, code comments, documentation, issues, pull requests, and commit messages. User-facing translations are welcome as the project evolves.

## Community

Roma is intended to remain fully open source and to be developed and maintained by the open-source community. Technical proposals, architectural decisions, and contributions should be discussed publicly in this repository.

## License

Roma's original code is free software licensed under the GNU General Public License, version 3 or, at your option, any later version (`GPL-3.0-or-later`). See [LICENSE](LICENSE) for the full license text.

Third-party components retain their respective licenses and copyright notices.
