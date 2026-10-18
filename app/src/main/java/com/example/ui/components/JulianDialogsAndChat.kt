package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserApiConfig
import com.example.domain.engine.ChatMessage
import com.example.ui.viewmodel.ActivityLogEntry

@Composable
fun JulianApiSettingsDialog(
    currentConfig: UserApiConfig,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean) -> Unit
) {
    var baseUrl by remember(currentConfig) { mutableStateOf(currentConfig.baseUrl) }
    var apiKeyHeader by remember(currentConfig) { mutableStateOf(currentConfig.apiKeyHeader) }
    var executionMode by remember(currentConfig) { mutableStateOf(currentConfig.executionMode) }
    var enableHighThinking by remember(currentConfig) { mutableStateOf(currentConfig.enableHighThinking) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SettingsEthernet,
                    contentDescription = "API Endpoint Configurator",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "REST ENDPOINT & ENGINE CONFIG",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Synced to your account via Cloud Firestore. Configure your custom Julian REST server Base URL and default execution engine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("REST API Base URL") },
                    placeholder = { Text("https://api.julian-omnicore.dev/v9") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_base_url_input")
                )

                OutlinedTextField(
                    value = apiKeyHeader,
                    onValueChange = { apiKeyHeader = it },
                    label = { Text("Optional X-Julian-Api-Key / Bearer Token") },
                    placeholder = { Text("jln_live_...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_header_input")
                )

                Text(
                    text = "Default Execution Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "HYBRID" to "Hybrid Auto",
                        "NATIVE_AI" to "Studio + Gemini",
                        "LIVE_REST" to "Live REST Only"
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = executionMode == code,
                            onClick = { executionMode = code },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gemini 4 Argon High Thinking",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Use gemini-4-argon for complex AI & Bioscience queries",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enableHighThinking,
                        onCheckedChange = { enableHighThinking = it },
                        modifier = Modifier.testTag("high_thinking_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(baseUrl, apiKeyHeader, executionMode, enableHighThinking)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_api_settings_button")
            ) {
                Text("Sync to Cloud")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun JulianActivityLogDialog(
    logs: List<ActivityLogEntry>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Activity Telemetry Log",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RECENT TELEMETRY LOG (${logs.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            if (logs.isEmpty()) {
                Text(
                    text = "No modules executed in this session yet. Tap any of the 85 tools to run.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { entry ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.commandName.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "HTTP ${entry.statusCode} • ${entry.latencyMs}ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Text(
                                    text = "${entry.endpointPath}  |  ${entry.timeLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = entry.source,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun JulianOmniChatModal(
    messages: List<ChatMessage>,
    isStreaming: Boolean,
    useArgonHigh: Boolean,
    onToggleArgonHigh: (Boolean) -> Unit,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var inputPrompt by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.97f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Omni-Core AI Chat",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OMNI-CORE NEURAL CHATBOT",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (useArgonHigh) {
                                "Active Model: gemini-4-argon (High Thinking)"
                            } else {
                                "Active Model: gemini-3.8-flash (Fast Grounded)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_chat_modal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Chat",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Model Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = useArgonHigh,
                    onClick = { onToggleArgonHigh(true) },
                    label = { Text("Gemini 4 Argon (High Thinking)") }
                )
                FilterChip(
                    selected = !useArgonHigh,
                    onClick = { onToggleArgonHigh(false) },
                    label = { Text("Gemini 3.8 Flash (Speed)") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Prompt Starters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Design a REST API gateway for Julian v9.0",
                    "Calculate PCR annealing temperature for a 20bp primer",
                    "Explain Indonesian 16-digit NIK structure"
                ).forEach { suggestion ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        onClick = { inputPrompt = suggestion }
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Conversation Thread
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            modifier = Modifier
                                .widthIn(max = 560.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (isUser) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            color = if (isUser) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = msg.modelUsed,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isUser) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.secondary
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = msg.timestampLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = msg.text.ifEmpty { if (msg.isStreaming) "Thinking..." else "" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Composer Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = { Text("Ask Julian Omni-Core AI or Bioscience assistant...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("omni_chat_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val toSend = inputPrompt
                        inputPrompt = ""
                        onSendMessage(toSend)
                    },
                    enabled = inputPrompt.isNotBlank() && !isStreaming,
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("omni_chat_send_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isStreaming) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Chat Message"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JulianMobileDownloadDialog(
    baseUrl: String,
    onExportAppZip: () -> Unit,
    onExportAllToolsJson: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "UNDUH FILE .ZIP & APK DI HP (TANPA PC)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Pilih cara paling mudah buat kamu untuk mengunduh file .ZIP atau langsung .APK di HP:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CARA 1 (Menu Atas Google AI Studio):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Ketuk ikon ⚙️ (Settings / Export) di pojok kanan atas halaman AI Studio ini.\n• Pilih \"Download ZIP\" (Export Project ZIP) atau \"Download APK\".\n• Di dalam file ZIP proyek juga sudah disertakan folder julian_standalone_webapp (index.html + manifest.json) siap konversi APK!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "CARA 2 (Tombol Unduh .ZIP Instan di Bawah):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Ketuk tombol \"UNDUH FILE .ZIP SIAP JADI APK\" di bawah untuk menyimpan paket Julian_OmniCore_v9_App.zip ke folder Download HP kamu (bisa langsung diubah jadi APK lewat WebIntoApp.com atau HopWeb).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Button(
                    onClick = onExportAppZip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_app_zip_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📦 UNDUH FILE .ZIP SIAP JADI APK")
                }

                TextButton(
                    onClick = onExportAllToolsJson,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_all_tools_json_button")
                ) {
                    Text("Unduh Spesifikasi 85 API (.JSON)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

