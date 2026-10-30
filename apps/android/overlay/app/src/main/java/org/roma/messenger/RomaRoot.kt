// SPDX-License-Identifier: GPL-3.0-or-later
package org.roma.messenger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bitchat.android.R

/** Keeps the upstream chat mounted while presenting Roma's network information. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomaRoot(content: @Composable () -> Unit) {
    var showNetworks by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("ROMA", style = MaterialTheme.typography.titleMedium)
            Text("Bitchat", style = MaterialTheme.typography.labelLarge)
            TextButton(onClick = { showNetworks = true }) {
                Text(stringResource(R.string.roma_networks))
            }
        }
        HorizontalDivider()
        Box(Modifier.weight(1f).fillMaxWidth()) { content() }
    }
    if (showNetworks) {
        ModalBottomSheet(onDismissRequest = { showNetworks = false }) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.roma_networks), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.roma_bitchat_available))
                Text(stringResource(R.string.roma_meshtastic_pending))
                Text(stringResource(R.string.roma_preview_notice), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
