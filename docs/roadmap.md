# Roadmap and implementation status

The initial source integration adds native Roma development shells around pinned Bitchat applications. The six preparation checks pass locally and in CI. Native build results and physical validation are separate gates; consult the pull request checks.

| Workstream | Tracking | Completion condition |
| --- | --- | --- |
| BLE development apps | [#1](https://github.com/lucsantana/Roma/issues/1) | Successful native builds and physical cross-platform/original-client messaging tests |
| Roma account, contacts, history and interface | [#2](https://github.com/lucsantana/Roma/issues/2) | Offline identity, verified associations, persistent individual conversations and separated network areas |
| Meshtastic | [#3](https://github.com/lucsantana/Roma/issues/3) | Native radio integration and physical LoRa interoperability |
| BLE–LoRa bridge | [#4](https://github.com/lucsantana/Roma/issues/4) | Authenticated routing, bounded queue/retries, deduplication and three-phone/two-radio proof |
| Licensing and distribution | [#5](https://github.com/lucsantana/Roma/issues/5) | Dependency notices, upstream inconsistency review and production distribution readiness |
| Future account transfer | [#6](https://github.com/lucsantana/Roma/issues/6) | Safe transfer with defined old-device behavior |
| Future Roma broadcast | [#7](https://github.com/lucsantana/Roma/issues/7) | Product/protocol evaluation without removing native channels |
| Future disappearing messages | [#8](https://github.com/lucsantana/Roma/issues/8) | Optional expiration semantics after persistent history |

Work on BLE and LoRa independently before the bridge. Preserve existing upstream functions throughout; the individual-text limit applies to new Roma features, not to native channel removal.

No task is marked complete merely because source exists, CI is configured or a device is available. Public-network testing and radio purchase remain pending.
