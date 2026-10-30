// SPDX-License-Identifier: GPL-3.0-or-later
import SwiftUI

/// Preserves the upstream view and its runtime rather than creating a second mesh session.
struct RomaRootView<Content: View>: View {
    @State private var showNetworks = false
    private let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text("ROMA").font(.headline)
                Spacer()
                Text("Bitchat").font(.subheadline)
                Spacer()
                Button("Networks") { showNetworks = true }
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
            Divider()
            content
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .sheet(isPresented: $showNetworks) {
            VStack(alignment: .leading, spacing: 20) {
                HStack {
                    Text("Networks").font(.title2.bold())
                    Spacer()
                    Button("Done") { showNetworks = false }
                }
                Text("Bitchat: nearby messaging and existing channels are available here.")
                Text("Meshtastic: LoRa messaging is not available in this preview.")
                Text("Development preview. Messages use Bitchat identities and settings. Roma contacts and automatic switching between networks are coming later.")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                Spacer()
            }
            .padding(24)
            .frame(minWidth: 280, minHeight: 300)
        }
    }
}
