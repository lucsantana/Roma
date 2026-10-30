# Product requirements

Decision baseline: October 27, 2026. Original Roma code remains GPL-3.0-or-later, as established by this repository. Third-party code keeps its own terms.

## Confirmed scope

- Serve rural communities, hikers, field teams and people without cellular connectivity, including disaster scenarios.
- Support Android and iPhone from the beginning.
- Reuse Bitchat for nearby Bluetooth communication and Meshtastic for LoRa radios; avoid replacing their existing mesh algorithms.
- Communicate with Roma, original Bitchat and original Meshtastic users. Clearly separate those experiences and highlight Roma users only when Roma capability is actually established.
- Provide a familiar messaging interface. Initial Roma-specific content is individual text messaging; existing upstream channels and features must remain available.
- Create account identity locally and work offline on first use. Identity represents an account, not proof of a unique human or hardware serial number.
- Offer nearby discovery and QR contacts, with an option to hide from unknown Roma users. Default discovery follows the selected Bitchat version. This does not promise radio invisibility.
- Keep Roma history on the device until deleted. Account transfer, Roma broadcast and disappearing messages are future work.
- Choose the route automatically during normal use. Preserve queued messages and distinguish transport acceptance, radio acknowledgement and destination-app receipt.
- Keep the project open source; a possible future foundation donation is an intention, not an established affiliation.

## Validation order

Validate BLE first, then LoRa independently, then the BLE–LoRa bridge. This order does not remove the bridge or upstream feature preservation from the intended product.

The physical test matrix covers Android-to-iPhone and iPhone-to-iPhone across multiple hardware generations. Record exact OS/build and app revisions during testing; keep personal device inventories and identifiers out of public reports.

Two identical SX1276 915 MHz radios are planned but **not purchased**. Exact board revision, firmware and antenna compatibility remain unverified. A public/community network is **pending** and is not a prerequisite for controlled tests.

## First implementation boundary

The initial BLE development apps retain the upstream Bitchat messaging experience and wire protocol. They are an integration baseline, not completion of the new Roma identity, contact discovery, persistent history, user interface or LoRa requirements. A renamed installation alone does not identify another peer as a Roma user.

Do not claim end-to-end cross-network encryption, delivery, background reliability or full feature parity until the corresponding implementation and physical tests exist.

