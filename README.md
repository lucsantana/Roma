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

## Architecture decisions

The initial architecture follows a native, offline-first approach. These decisions define the intended implementation direction; they do not describe completed functionality.

| Area | Decision |
| --- | --- |
| Android application | Native Android application written in **Kotlin**. |
| iOS application | Native iOS application written in **Swift**. |
| Mobile implementation | Separate native applications with direct access to each platform's Bluetooth APIs, permissions, lifecycle, and background execution capabilities. |
| Protocol interoperability | A shared, platform-independent Roma protocol specification with common message formats and compatibility test vectors for both native implementations. |
| Network integration | Separate adapters for Bitchat interoperability and compatible LoRa / Meshtastic devices, keeping transport-specific behavior separate from application logic. |
| Offline operation | Core messaging is designed to operate without cellular service, internet access, or a mandatory cloud backend. Delivery still depends on powered devices and reachable peers or radio links. |
| LoRa hardware | Use compatible external LoRa / Meshtastic radios. The mobile application does not replace the radio hardware. |

Native applications are chosen to give each platform explicit control over device integration and operating-system behavior. Background communication remains subject to Android and iOS restrictions and must be tested on real devices.

Bridge placement, message routing, duplicate suppression, persistence, identity mapping, cross-network encryption, and supported hardware remain open design topics. They will be documented and validated through public architectural proposals before implementation. No end-to-end privacy guarantee across the bridge is claimed at this stage.

## Community

Roma is intended to remain fully open source and to be developed and maintained by the open-source community. Technical proposals, architectural decisions, and contributions should be discussed publicly in this repository.

## License

Roma's original code is free software licensed under the GNU General Public License, version 3 or, at your option, any later version (`GPL-3.0-or-later`). See [LICENSE](LICENSE) for the full license text.

Third-party components retain their respective licenses and copyright notices.
