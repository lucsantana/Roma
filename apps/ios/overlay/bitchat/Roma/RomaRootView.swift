// SPDX-License-Identifier: GPL-3.0-or-later
import BitFoundation
import Combine
import SwiftUI

private func romaText(_ key: String) -> String {
    NSLocalizedString(key, tableName: "Roma", bundle: .main, comment: "")
}

private enum RomaNetwork: String, CaseIterable {
    case roma = "ROMA", bitchat = "Bitchat", meshtastic = "Meshtastic"
    var icon: String {
        switch self { case .roma: return "bubble.left.and.bubble.right"; case .bitchat: return "antenna.radiowaves.left.and.right"; case .meshtastic: return "radio" }
    }
    var subtitle: String {
        switch self { case .roma: return "roma_all_networks"; case .bitchat: return "roma_bluetooth_network"; case .meshtastic: return "roma_lora_network" }
    }
}

private struct RomaInboxRow: Identifiable {
    let id: String
    let peerID: PeerID
    let name: String
    let preview: String
    let date: Date
    let unread: Bool
    let online: Bool
}

/// Read-only projection of the existing store: no second history or transport session.
@MainActor
private final class RomaInboxModel: ObservableObject {
    @Published private(set) var rows: [RomaInboxRow] = []
    private let runtime: AppRuntime
    private var subscriptions = Set<AnyCancellable>()

    init(runtime: AppRuntime) {
        self.runtime = runtime
        runtime.conversations.changes.receive(on: DispatchQueue.main)
            .sink { [weak self] _ in self?.refresh() }.store(in: &subscriptions)
        runtime.peerListModel.$meshRows.receive(on: DispatchQueue.main)
            .sink { [weak self] _ in self?.refresh() }.store(in: &subscriptions)
        runtime.peerListModel.$groupRows.receive(on: DispatchQueue.main)
            .sink { [weak self] _ in self?.refresh() }.store(in: &subscriptions)
        refresh()
    }

    private func refresh() {
        let model = runtime.chatViewModel
        var byIdentity: [String: RomaInboxRow] = [:]
        for (id, conversation) in runtime.conversations.conversationsByID {
            guard case .direct(let handle) = id, let last = conversation.messages.last else { continue }
            let peer = handle.routingPeerID
            guard !model.isPeerBlocked(peer) else { continue }
            let identity = model.getFingerprint(for: peer) ?? peer.id
            let roster = runtime.peerListModel.meshRows.first { row in
                row.peerID == peer || model.getFingerprint(for: row.peerID) == identity
            }
            let name: String
            if peer.isGroup {
                name = runtime.peerListModel.groupRows.first { $0.peerID == peer }?.name ?? romaText("roma_group")
            } else if peer.isGeoDM {
                name = model.geohashDisplayName(for: peer)
            } else {
                name = roster?.displayName ?? model.resolveNickname(for: peer)
            }
            let preview = last.content.contains("file://") ? romaText("roma_attachment") :
                String(last.content.replacingOccurrences(of: "\n", with: " ").prefix(160))
            let unread = model.hasUnreadMessages(for: peer) || (byIdentity[identity]?.unread ?? false)
            if let existing = byIdentity[identity], existing.date > last.timestamp {
                byIdentity[identity] = RomaInboxRow(id: identity, peerID: existing.peerID, name: existing.name,
                    preview: existing.preview, date: existing.date, unread: unread, online: existing.online)
            } else {
                byIdentity[identity] = RomaInboxRow(id: identity, peerID: peer, name: name, preview: preview,
                    date: last.timestamp, unread: unread, online: roster?.isConnected == true || roster?.isReachable == true)
            }
        }
        rows = byIdentity.values.sorted { $0.date == $1.date ? $0.id < $1.id : $0.date > $1.date }
    }
}

@MainActor
struct RomaRootView<Content: View>: View {
    @StateObject private var inbox: RomaInboxModel
    @EnvironmentObject private var peers: PeerListModel
    @EnvironmentObject private var conversation: PrivateConversationModel
    @Environment(\.colorScheme) private var colorScheme
    @State private var network = RomaNetwork.roma
    @State private var workspace = false
    @State private var returnToInbox = false
    @State private var query = ""
    @State private var unreadOnly = false
    @State private var showPeople = false
    @State private var pendingPeer: PeerID?
    private let content: Content

    init(runtime: AppRuntime, @ViewBuilder content: () -> Content) {
        _inbox = StateObject(wrappedValue: RomaInboxModel(runtime: runtime))
        self.content = content()
    }

    private var accent: Color { colorScheme == .dark ? Color(red: 0.44, green: 0.85, blue: 0.74) : Color(red: 0.086, green: 0.49, blue: 0.40) }
    private var background: Color { colorScheme == .dark ? Color(red: 0.063, green: 0.106, blue: 0.125) : Color(red: 0.96, green: 0.975, blue: 0.975) }
    private var surface: Color { colorScheme == .dark ? Color(red: 0.086, green: 0.14, blue: 0.165) : .white }
    private var nearby: [MeshPeerRow] { peers.meshRows.filter { !$0.isMe && !$0.isBlocked && ($0.isConnected || $0.isReachable) } }
    private var visibleRows: [RomaInboxRow] {
        let text = query.trimmingCharacters(in: .whitespacesAndNewlines)
        return inbox.rows.filter { row in
            (!unreadOnly || row.unread) && (text.isEmpty || row.name.localizedCaseInsensitiveContains(text) || row.preview.localizedCaseInsensitiveContains(text))
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            if workspace {
                HStack {
                    Button { workspace = false; returnToInbox = false } label: {
                        Label(romaText("roma_back"), systemImage: "chevron.left")
                    }
                    Spacer()
                    Text("Bitchat").font(.headline)
                }
                .padding(.horizontal, 20).padding(.vertical, 12).background(surface)
                content.frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                header
                if network == .meshtastic { radioPlaceholder }
                else { inboxContent }
            }
            Divider()
            HStack(spacing: 0) {
                ForEach(RomaNetwork.allCases, id: \.self) { destination in
                    Button {
                        network = destination; workspace = false; returnToInbox = false
                        query = ""; unreadOnly = false
                    } label: {
                        VStack(spacing: 5) {
                            Image(systemName: destination.icon).font(.system(size: 22, weight: .medium))
                                .frame(width: 64, height: 32)
                                .background(network == destination ? accent.opacity(0.10) : .clear, in: Capsule())
                            Text(destination.rawValue).font(.caption.weight(network == destination ? .semibold : .regular))
                        }.foregroundStyle(network == destination ? accent : .secondary)
                            .frame(maxWidth: .infinity).padding(.vertical, 8)
                            .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(network == destination ? .isSelected : [])
                }
            }.background(surface)
        }
        .background(background).tint(accent)
        .onChange(of: conversation.selectedPeerID) { peer in
            if peer != nil { workspace = true }
            else if returnToInbox { workspace = false; returnToInbox = false }
        }
        .sheet(isPresented: $showPeople, onDismiss: {
            if let peer = pendingPeer { pendingPeer = nil; presentConversation(peer) }
        }) { peopleSheet }
    }

    private var header: some View {
        HStack {
            VStack(alignment: .leading, spacing: 5) {
                Text(network.rawValue).font(.largeTitle.bold())
                Text(romaText(network.subtitle)).font(.subheadline).foregroundStyle(.secondary)
            }
            Spacer(minLength: 12)
            if network != .meshtastic {
                Button { showPeople = true } label: {
                    Image(systemName: "square.and.pencil").font(.title3)
                        .frame(width: 46, height: 46).background(accent.opacity(0.10), in: Circle())
                }.accessibilityLabel(romaText("roma_new_conversation"))
            }
        }.padding(.horizontal, 22).padding(.top, 18).padding(.bottom, 18)
    }

    private var inboxContent: some View {
        VStack(spacing: 0) {
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass").foregroundStyle(.secondary)
                TextField(romaText("roma_search"), text: $query).textFieldStyle(.plain)
                if !query.isEmpty { Button { query = "" } label: { Image(systemName: "xmark.circle.fill") }
                    .accessibilityLabel(romaText("roma_clear_search")) }
            }.padding(15).background(accent.opacity(0.055), in: RoundedRectangle(cornerRadius: 18))
                .padding(.horizontal, 20)
            HStack(spacing: 8) {
                filter(romaText("roma_all"), selected: !unreadOnly) { unreadOnly = false }
                filter(romaText("roma_unread"), selected: unreadOnly) { unreadOnly = true }
                Spacer()
            }.padding(.horizontal, 20).padding(.vertical, 12)
            ScrollView {
                LazyVStack(spacing: 0) {
                    Button { showPeople = true } label: {
                        HStack(spacing: 12) {
                            Image(systemName: "antenna.radiowaves.left.and.right").foregroundStyle(accent)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(romaText("roma_nearby")).font(.subheadline.weight(.semibold))
                                Text(String(format: romaText("roma_nearby_count"), nearby.count)).font(.caption).foregroundStyle(.secondary)
                            }
                            Spacer()
                            Image(systemName: "chevron.right").font(.caption).foregroundStyle(.secondary)
                        }.padding(16).background(surface, in: RoundedRectangle(cornerRadius: 18))
                    }.buttonStyle(.plain).padding(.horizontal, 20)
                    if network == .bitchat {
                        Button { workspace = true; returnToInbox = false } label: {
                            Label(romaText("roma_open_bitchat"), systemImage: "number")
                                .font(.subheadline).frame(maxWidth: .infinity, alignment: .leading).padding(20)
                        }
                    }
                    Text(romaText("roma_conversations")).font(.subheadline.weight(.semibold)).foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity, alignment: .leading).padding(.horizontal, 22).padding(.vertical, 18)
                    if visibleRows.isEmpty { emptyInbox }
                    ForEach(visibleRows) { row in
                        Button { open(row.peerID) } label: {
                            conversationRow(name: row.name, preview: row.preview, unread: row.unread, online: row.online, date: row.date)
                        }.buttonStyle(.plain)
                    }
                }.padding(.bottom, 16)
            }
        }
    }

    private func filter(_ title: String, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title).font(.subheadline.weight(selected ? .semibold : .regular))
                .padding(.horizontal, 15).padding(.vertical, 8)
                .foregroundStyle(selected ? accent : .secondary)
                .background(selected ? accent.opacity(0.12) : surface, in: Capsule())
        }.buttonStyle(.plain).accessibilityAddTraits(selected ? .isSelected : [])
    }

    private var emptyInbox: some View {
        VStack(spacing: 14) {
            Image(systemName: "bubble.left.and.bubble.right").font(.system(size: 40)).foregroundStyle(accent)
            Text(romaText(query.isEmpty && !unreadOnly ? "roma_no_chats" : "roma_no_results")).font(.headline)
            Text(romaText(query.isEmpty && !unreadOnly ? "roma_start_nearby" : "roma_try_search"))
                .foregroundStyle(.secondary).multilineTextAlignment(.center)
            if query.isEmpty && !unreadOnly { Button(romaText("roma_new_conversation")) { showPeople = true } }
        }.padding(32).frame(maxWidth: .infinity)
    }

    private var radioPlaceholder: some View {
        VStack(spacing: 20) {
            Spacer()
            Image(systemName: "radio").font(.system(size: 40)).foregroundStyle(accent)
                .padding(28).background(accent.opacity(0.10), in: Circle())
            Text(romaText("roma_lora_title")).font(.title2.bold())
            Text(romaText("roma_lora_description")).foregroundStyle(.secondary).multilineTextAlignment(.center)
            Text(romaText("roma_coming_soon")).font(.subheadline.weight(.semibold)).foregroundStyle(accent)
            Spacer()
        }.padding(28).frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var peopleSheet: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    Text(romaText("roma_nearby_hint")).foregroundStyle(.secondary).padding(24)
                    if nearby.isEmpty { Text(romaText("roma_no_peers")).foregroundStyle(.secondary).padding(24) }
                    ForEach(nearby) { peer in
                        Button { open(peer.peerID) } label: {
                            conversationRow(name: peer.displayName, preview: romaText("roma_available"), unread: peer.hasUnread, online: true, date: nil)
                        }.buttonStyle(.plain)
                    }
                }
            }.background(background).navigationTitle(romaText("roma_new_conversation"))
                .toolbar { ToolbarItem(placement: .confirmationAction) { Button(romaText("roma_done")) { showPeople = false } } }
        }.tint(accent)
    }

    private func open(_ peer: PeerID) {
        if showPeople { pendingPeer = peer; showPeople = false }
        else { presentConversation(peer) }
    }

    private func presentConversation(_ peer: PeerID) {
        workspace = true
        returnToInbox = true
        conversation.openConversation(for: peer)
    }

    private func conversationRow(name: String, preview: String, unread: Bool, online: Bool, date: Date?) -> some View {
        VStack(spacing: 0) {
            HStack(spacing: 14) {
                Text(String(name.prefix(1)).uppercased()).font(.title3.bold()).foregroundStyle(accent)
                    .frame(width: 52, height: 52).background(accent.opacity(0.12), in: Circle())
                    .overlay(alignment: .bottomTrailing) {
                        if online { Circle().fill(accent).frame(width: 10, height: 10).padding(2).background(surface, in: Circle()) }
                    }
                VStack(alignment: .leading, spacing: 5) {
                    Text(name).font(.body.weight(unread ? .bold : .semibold)).lineLimit(1)
                    Text(preview).font(.subheadline).foregroundStyle(.secondary).lineLimit(1)
                    Text("Bitchat").font(.caption2.weight(.medium)).foregroundStyle(accent)
                        .padding(.horizontal, 6).padding(.vertical, 2).background(accent.opacity(0.08), in: RoundedRectangle(cornerRadius: 5))
                }.frame(maxWidth: .infinity, alignment: .leading)
                VStack(alignment: .trailing, spacing: 12) {
                    if let date { Text(date, style: .time).font(.caption2).foregroundStyle(unread ? accent : .secondary) }
                    if unread { Circle().fill(accent).frame(width: 10, height: 10).accessibilityLabel(romaText("roma_unread")) }
                }
            }.padding(.horizontal, 20).padding(.vertical, 14).contentShape(Rectangle())
            Divider().padding(.leading, 86).padding(.trailing, 20)
        }
    }
}
