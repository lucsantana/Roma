# Roma network interface

The main **ROMA** section is a unified inbox. It aggregates existing private conversations and shows a network label on every row. Network labels describe the messaging ecosystem, not the app installed by the other person or a verified Roma identity.

## Navigation

- **ROMA:** conversation list, latest-message previews, unread indicators, name/message search and an unread filter. New conversation opens the nearby-person picker.
- **Bitchat:** the same existing Bitchat conversations, plus access to native channels, settings, verification and messaging features through the original workspace.
- **Meshtastic:** a separate, explicitly unavailable radio area. It does not pretend to connect to hardware or deliver LoRa messages.

Currently all integrated conversations belong to Bitchat, so Roma and Bitchat inbox contents overlap. New network adapters must supply an explicit source label before their conversations can be aggregated. Do not infer network, identity verification or Roma membership from a nickname.

The shell uses system typography, generous touch targets, rounded search and filter controls, muted surfaces and a green accent. Both light and dark system appearances are supported, with Portuguese and English text. Existing iOS appearance preferences are retained; the default for new preferences uses the upstream system-font theme.

## Integration boundaries

Android reads the existing conversation `StateFlow` and opens the native private-chat sheet. iOS projects the existing `ConversationStore`, coalesces stable/ephemeral aliases by the available fingerprint and opens conversations through `PrivateConversationModel`. Store changes refresh the inbox; reading the inbox itself does not mark a conversation read.

Runtime and mesh ownership remain with the existing activity and `AppRuntime`. Opening a network section does not create a second transport session. The native Bitchat workspace is shown when its features or a conversation are requested. This change does not modify BLE discovery, connection recovery, cryptography, message routing or persistent account identity.

## UI acceptance checklist

- Check empty and populated inboxes, long names, unread changes and offline retained conversations.
- Search by name and preview; clear search; combine search with unread filtering.
- Open a nearby contact, send and receive individual text, close the conversation and return to the inbox.
- Verify an incoming notification can open the intended conversation.
- Switch between all three network sections and return to a conversation.
- Open native Bitchat channels/settings and confirm their functions remain reachable.
- Check Portuguese/English, light/dark, larger text, keyboard and safe-area layout.
- Verify Meshtastic never reports an active radio or delivery.
- Repeat foreground Android–iPhone BLE messaging without internet after installing the UI.

Record actual results separately from successful compilation. Keep real contact names, messages, location data, device identifiers and screenshots containing them out of public artifacts; use an empty simulator or synthetic fixtures for visual review.
