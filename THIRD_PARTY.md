# Third-party sources

Roma's original code remains GPL-3.0-or-later. This does not replace third-party licenses or copyright notices.

| Component | Pinned revision | License evidence |
| --- | --- | --- |
| [Bitchat Apple](https://github.com/permissionlesstech/bitchat) | `04995f68e470a18d1fc99f86fa326c2a87bab4f2` | Repository `LICENSE`: Unlicense; retain notices and package licenses. |
| [Bitchat Android](https://github.com/permissionlesstech/bitchat-android) | `01294db3f097922f4ba148c1a7186d6776391bbd` | Repository `LICENSE.md`: GPL version 3. README also claims public domain; do not rely on that conflicting summary to remove GPL terms. Individual source/dependency notices remain intact. |

The preparation script retains all source license files, original headers, dependency declarations and lockfiles. Generated applications include third-party components beyond these two repositories; this table is not a complete dependency-license audit.

Roma changes currently cover the installation identity, root presentation, Android private shutdown permission/action and Android download endpoints. BLE services, wire formats and cryptographic implementations remain unchanged by the overlays. Build manifests identify source revisions and overlay hashes.

Meshtastic sources are not incorporated in this first BLE integration. Record their exact revisions, protobuf compatibility and notices when adding that adapter. Complete dependency and distribution review before publishing production binaries or store releases; no production signing/release pipeline is enabled here.
