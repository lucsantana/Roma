// SPDX-License-Identifier: GPL-3.0-or-later
package org.roma.messenger

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.R
import com.bitchat.android.ui.ChatViewModel
import java.text.DateFormat
import java.util.Date

private enum class RomaNetwork { ROMA, BITCHAT, MESHTASTIC }

/** Presentation only; the activity retains the existing mesh and view model. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomaRoot(viewModel: ChatViewModel, content: @Composable () -> Unit) {
    var network by rememberSaveable { mutableStateOf(RomaNetwork.ROMA) }
    var workspace by rememberSaveable { mutableStateOf(false) }
    var returnToInbox by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var unreadOnly by rememberSaveable { mutableStateOf(false) }
    var showPeople by rememberSaveable { mutableStateOf(false) }
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val peers by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val names by viewModel.peerNicknames.collectAsStateWithLifecycle()
    val activeSheet by viewModel.privateChatSheetPeer.collectAsStateWithLifecycle()
    val selectedPeer by viewModel.selectedPrivateChatPeer.collectAsStateWithLifecycle()
    val colors = if (isSystemInDarkTheme()) darkColorScheme(
        primary = Color(0xFF71D8BC), background = Color(0xFF101B20), surface = Color(0xFF16242A),
        surfaceVariant = Color(0xFF23343A), onSurface = Color(0xFFE8F0F2), onSurfaceVariant = Color(0xFFA6B9BF),
        outlineVariant = Color(0xFF2C3D43),
    ) else lightColorScheme(
        primary = Color(0xFF167D67), background = Color(0xFFF5F8F8), surface = Color.White,
        surfaceVariant = Color(0xFFEDF3F3), onSurface = Color(0xFF162C36), onSurfaceVariant = Color(0xFF657B85),
        outlineVariant = Color(0xFFE7EEEE),
    )
    fun openConversation(id: String) {
        showPeople = false
        returnToInbox = true
        workspace = true
        viewModel.showPrivateChatSheet(id)
    }
    LaunchedEffect(activeSheet, selectedPeer) {
        if (activeSheet != null || selectedPeer != null) workspace = true
        else if (returnToInbox) { workspace = false; returnToInbox = false }
    }
    BackHandler(workspace && activeSheet == null && selectedPeer == null) { workspace = false }

    MaterialTheme(colorScheme = colors, typography = Typography()) {
        Column(Modifier.fillMaxSize().background(colors.background)) {
            if (workspace) {
                Row(Modifier.fillMaxWidth().background(colors.surface).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { workspace = false; returnToInbox = false }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.roma_back))
                    }
                    Text("Bitchat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text(stringResource(R.string.roma_network_space), style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant, modifier = Modifier.padding(end = 12.dp))
                }
                Box(Modifier.weight(1f).fillMaxWidth()) { content() }
            } else {
                Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 12.dp, top = 18.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(when (network) { RomaNetwork.ROMA -> "ROMA"; RomaNetwork.BITCHAT -> "Bitchat"; RomaNetwork.MESHTASTIC -> "Meshtastic" },
                            style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text(stringResource(when (network) { RomaNetwork.ROMA -> R.string.roma_all_networks; RomaNetwork.BITCHAT -> R.string.roma_bluetooth_network; RomaNetwork.MESHTASTIC -> R.string.roma_lora_network }),
                            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    if (network != RomaNetwork.MESHTASTIC) FilledTonalIconButton(onClick = { showPeople = true }, shape = CircleShape) {
                        Icon(Icons.Outlined.Edit, stringResource(R.string.roma_new_conversation))
                    }
                }
                if (network == RomaNetwork.MESHTASTIC) {
                    Column(Modifier.weight(1f).fillMaxWidth().padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = colors.surfaceVariant) {
                            Icon(Icons.Outlined.CellTower, null, Modifier.padding(24.dp).size(40.dp), tint = colors.primary)
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.roma_lora_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.roma_lora_description), color = colors.onSurfaceVariant)
                        Spacer(Modifier.height(20.dp))
                        Text(stringResource(R.string.roma_coming_soon), color = colors.primary, style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text(stringResource(R.string.roma_search)) },
                        leadingIcon = { Icon(Icons.Outlined.Search, null) },
                        trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, stringResource(R.string.roma_clear_search)) } },
                        singleLine = true, shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, unfocusedContainerColor = colors.surfaceVariant, focusedContainerColor = colors.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
                    Row(Modifier.padding(horizontal = 20.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(!unreadOnly, { unreadOnly = false }, label = { Text(stringResource(R.string.roma_all)) })
                        FilterChip(unreadOnly, { unreadOnly = true }, label = { Text(stringResource(R.string.roma_unread)) })
                    }
                    val visible = conversations.filter {
                        (!unreadOnly || it.unreadCount > 0) && (query.isBlank() || it.displayName.contains(query.trim(), true) || it.latestMessagePreview.contains(query.trim(), true))
                    }
                    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {
                        item {
                            Surface(onClick = { showPeople = true }, shape = RoundedCornerShape(18.dp), color = colors.surface,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp).fillMaxWidth()) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Bluetooth, null, tint = colors.primary)
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                        Text(stringResource(R.string.roma_nearby), fontWeight = FontWeight.SemiBold)
                                        Text(stringResource(R.string.roma_nearby_count, peers.size), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                    }
                                    Icon(Icons.Outlined.ChevronRight, null, tint = colors.onSurfaceVariant)
                                }
                            }
                        }
                        if (network == RomaNetwork.BITCHAT) item {
                            TextButton(onClick = { workspace = true; returnToInbox = false }, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Icon(Icons.Outlined.Forum, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.roma_open_bitchat))
                            }
                        }
                        item { Text(stringResource(R.string.roma_conversations), Modifier.padding(horizontal = 22.dp, vertical = 16.dp), style = MaterialTheme.typography.titleSmall, color = colors.onSurfaceVariant) }
                        if (visible.isEmpty()) item {
                            Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.AutoMirrored.Outlined.Chat, null, Modifier.size(42.dp), tint = colors.primary)
                                Spacer(Modifier.height(16.dp))
                                Text(stringResource(if (query.isNotBlank() || unreadOnly) R.string.roma_no_results else R.string.roma_no_chats), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(if (query.isNotBlank() || unreadOnly) R.string.roma_try_search else R.string.roma_start_nearby), color = colors.onSurfaceVariant)
                                if (query.isBlank() && !unreadOnly) TextButton(onClick = { showPeople = true }) { Text(stringResource(R.string.roma_new_conversation)) }
                            }
                        }
                        items(visible, key = { it.conversationID }) { conversation ->
                            RomaConversationRow(conversation.displayName, conversation.latestMessagePreview, conversation.unreadCount, conversation.isConnected,
                                DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(conversation.latestMessageAt))) { openConversation(conversation.conversationID) }
                        }
                    }
                }
            }
            HorizontalDivider(color = colors.outlineVariant)
            NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
                RomaNetwork.entries.forEach { destination ->
                    NavigationBarItem(selected = network == destination,
                        onClick = { network = destination; workspace = false; returnToInbox = false; query = ""; unreadOnly = false },
                        icon = { Icon(when (destination) { RomaNetwork.ROMA -> Icons.AutoMirrored.Outlined.Chat; RomaNetwork.BITCHAT -> Icons.Outlined.Bluetooth; RomaNetwork.MESHTASTIC -> Icons.Outlined.CellTower }, null) },
                        label = { Text(when (destination) { RomaNetwork.ROMA -> "ROMA"; RomaNetwork.BITCHAT -> "Bitchat"; RomaNetwork.MESHTASTIC -> "Meshtastic" }) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = colors.primary, selectedTextColor = colors.primary, indicatorColor = colors.primary.copy(alpha = .10f)))
                }
            }
        }
        if (showPeople) ModalBottomSheet(onDismissRequest = { showPeople = false }, containerColor = colors.surface) {
            Text(stringResource(R.string.roma_new_conversation), Modifier.padding(horizontal = 24.dp, vertical = 8.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.roma_nearby_hint), Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = colors.onSurfaceVariant)
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                if (peers.isEmpty()) item { Text(stringResource(R.string.roma_no_peers), Modifier.padding(24.dp), color = colors.onSurfaceVariant) }
                items(peers, key = { it }) { id ->
                    RomaConversationRow(names[id] ?: stringResource(R.string.roma_person), stringResource(R.string.roma_available), 0, true, "") { openConversation(id) }
                }
            }
        }
    }
}

@Composable
private fun RomaConversationRow(name: String, preview: String, unread: Int, online: Boolean, time: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            Surface(shape = CircleShape, color = colors.primary.copy(alpha = .12f), modifier = Modifier.fillMaxSize()) {
                Box(contentAlignment = Alignment.Center) { Text(name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = colors.primary, style = MaterialTheme.typography.titleLarge) }
            }
            if (online) Box(Modifier.align(Alignment.BottomEnd).size(12.dp).background(colors.surface, CircleShape).padding(2.dp).background(colors.primary, CircleShape))
        }
        Column(Modifier.weight(1f).padding(start = 14.dp, end = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, fontWeight = if (unread > 0) FontWeight.Bold else FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(preview, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Bitchat", style = MaterialTheme.typography.labelSmall, color = colors.primary,
                modifier = Modifier.background(colors.primary.copy(alpha = .08f), RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(time, style = MaterialTheme.typography.labelSmall, color = if (unread > 0) colors.primary else colors.onSurfaceVariant)
            if (unread > 0) Badge(containerColor = colors.primary, contentColor = colors.surface) { Text(if (unread > 99) "99+" else unread.toString()) }
        }
    }
    HorizontalDivider(Modifier.padding(start = 86.dp, end = 20.dp), color = colors.outlineVariant)
}
