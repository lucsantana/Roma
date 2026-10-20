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

Roma is in the planning stage. The interoperability protocol, bridge implementation, supported hardware, and application architecture have not yet been implemented or validated. Cross-network delivery, identity mapping, and encryption behavior will be defined as part of the protocol design.

## Development language

**English is the default language for development.** Use English for source code identifiers, code comments, documentation, issues, pull requests, and commit messages. User-facing translations are welcome as the project evolves.

## Community

Roma is intended to remain fully open source and to be developed and maintained by the open-source community. Technical proposals, architectural decisions, and contributions should be discussed publicly in this repository.

## License

Roma's original code is free software licensed under the GNU General Public License, version 3 or, at your option, any later version (`GPL-3.0-or-later`). See [LICENSE](LICENSE) for the full license text.

Third-party components retain their respective licenses and copyright notices.
