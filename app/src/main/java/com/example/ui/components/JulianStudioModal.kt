package com.example.ui.components

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.catalog.JulianTool
import com.example.data.catalog.ToolEngineType
import com.example.domain.engine.NikParseResult
import com.example.domain.engine.ToolExecutionOutput
import com.example.ui.viewmodel.ExecutionUiState
import com.example.util.DirectDownloadHelper
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JulianStudioModal(
    tool: JulianTool,
    inputValues: Map<String, String>,
    effectiveMode: String,
    isBookmarked: Boolean,
    executionState: ExecutionUiState,
    onUpdateInput: (String, String) -> Unit,
    onSelectModeOverride: (String?) -> Unit,
    onToggleBookmark: () -> Unit,
    onExecute: () -> Unit,
    onCopyToast: (String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showRawJsonTab by remember { mutableStateOf(false) }

    // Native Android TextToSpeech for TTS tools
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(tool.engineType) {
        if (tool.engineType == ToolEngineType.NATIVE_TTS) {
            var instance: TextToSpeech? = null
            instance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    instance?.language = Locale.US
                    ttsReady = true
                }
            }
            ttsEngine = instance
        }
        onDispose {
            ttsEngine?.stop()
            ttsEngine?.shutdown()
            ttsEngine = null
        }
    }

    // Trigger Android TTS speech when execution completes for NATIVE_TTS
    LaunchedEffect(executionState) {
        val success = executionState as? ExecutionUiState.Success
        if (success != null && success.output.isTtsTriggered && ttsReady) {
            val script = inputValues["text"].orEmpty()
            val pitch = inputValues["pitch"]?.toFloatOrNull() ?: 1.0f
            val rate = inputValues["rate"]?.toFloatOrNull() ?: 1.0f
            ttsEngine?.setPitch(pitch)
            ttsEngine?.setSpeechRate(rate)
            ttsEngine?.speak(script, TextToSpeech.QUEUE_FLUSH, null, "julian_tts_utterance")
        }
    }

    // Zero-permission Android Photo Picker for UPLOAD category tools
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val lastSeg = uri.lastPathSegment ?: "picked_media.jpg"
            onUpdateInput("fileName", "content_uri_${lastSeg.takeLast(24)}")
            onCopyToast("Selected media asset: $lastSeg")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.96f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Top Studio Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Catalog",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = tool.category.badgeCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${tool.httpMethod} ${tool.endpointPath}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tool.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("studio_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove Bookmark" else "Bookmark Tool",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Execution Engine Mode Selector
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "EXECUTION ENGINE ROUTING // ${tool.engineType.title.uppercase(Locale.ROOT)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "HYBRID" to "Hybrid Auto (Studio + AI)",
                            "NATIVE_AI" to "On-Device / Gemini Studio",
                            "LIVE_REST" to "Live Custom REST Server"
                        ).forEach { (code, label) ->
                            val selected = effectiveMode == code
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectModeOverride(code) },
                                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Input Parameters Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "REQUEST PARAMETERS // ${tool.commandName.uppercase(Locale.ROOT)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (tool.engineType == ToolEngineType.NATIVE_UPLOAD_INSPECTOR) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_media_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = "Pick Visual Media"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Image/Video via Android Photo Picker")
                        }
                    }

                    tool.inputs.forEach { field ->
                        val currentValue = inputValues[field.key] ?: field.defaultValue
                        Column {
                            Text(
                                text = field.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (field.options.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    field.options.forEach { option ->
                                        FilterChip(
                                            selected = currentValue == option,
                                            onClick = { onUpdateInput(field.key, option) },
                                            label = {
                                                Text(option, style = MaterialTheme.typography.bodySmall)
                                            }
                                        )
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { onUpdateInput(field.key, it) },
                                    placeholder = { Text(field.placeholder) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("tool_input_${field.key}"),
                                    minLines = if (field.isMultiline) 3 else 1,
                                    maxLines = if (field.isMultiline) 6 else 2,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onExecute,
                        enabled = executionState !is ExecutionUiState.Running,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("execute_tool_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (executionState is ExecutionUiState.Running) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("EXECUTING MODULE...", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Icon(
                                imageVector = if (tool.engineType == ToolEngineType.NATIVE_TTS) {
                                    Icons.Default.RecordVoiceOver
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = "Execute Tool"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXECUTE // ${tool.commandName.uppercase(Locale.ROOT)}",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Execution Output Workspace
            when (executionState) {
                is ExecutionUiState.Idle -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "READY FOR EXECUTION",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Configure parameters above and tap EXECUTE to run ${tool.title}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is ExecutionUiState.Running -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Synthesizing output via ${tool.engineType.title}...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                is ExecutionUiState.Error -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Execution Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "EXECUTION FAULT",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = executionState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                is ExecutionUiState.Success -> {
                    StudioSuccessOutputSection(
                        output = executionState.output,
                        showRawJsonTab = showRawJsonTab,
                        onToggleTab = { showRawJsonTab = it },
                        onCopyJson = {
                            clipboardManager.setText(AnnotatedString(executionState.output.rawJson))
                            onCopyToast("JSON disalin ke clipboard")
                        },
                        onDownloadImage = {
                            val bmp = executionState.output.generatedBitmap
                            if (bmp != null) {
                                val res = DirectDownloadHelper.saveBitmapToGallery(
                                    context = context,
                                    bitmap = bmp,
                                    commandName = tool.commandName
                                )
                                res.fold(
                                    onSuccess = { path -> onCopyToast("Berhasil diunduh ke Galeri HP: $path") },
                                    onFailure = { err -> onCopyToast("Gagal menyimpan gambar: ${err.localizedMessage}") }
                                )
                            }
                        },
                        onDownloadJson = {
                            val res = DirectDownloadHelper.saveTextToDownloads(
                                context = context,
                                content = executionState.output.rawJson,
                                fileNamePrefix = tool.commandName,
                                extension = "json",
                                mimeType = "application/json"
                            )
                            res.fold(
                                onSuccess = { path -> onCopyToast("Berhasil diunduh ke HP: $path") },
                                onFailure = { err -> onCopyToast("Gagal mengunduh file: ${err.localizedMessage}") }
                            )
                        },
                        onShareResult = {
                            DirectDownloadHelper.shareTextDirectly(
                                context = context,
                                title = tool.title,
                                content = "${executionState.output.summaryTitle}\n\n${executionState.output.formattedReport}"
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun StudioSuccessOutputSection(
    output: ToolExecutionOutput,
    showRawJsonTab: Boolean,
    onToggleTab: (Boolean) -> Unit,
    onCopyJson: () -> Unit,
    onDownloadImage: () -> Unit,
    onDownloadJson: () -> Unit,
    onShareResult: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Telemetry Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HTTP ${output.statusCode} OK • ${output.latencyMs}ms",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                IconButton(
                    onClick = onCopyJson,
                    modifier = Modifier.testTag("copy_json_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy JSON Payload",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "SOURCE: ${output.executionSource}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Output Mode Switcher: Visual Studio vs. Raw JSON Telemetry
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showRawJsonTab,
                    onClick = { onToggleTab(false) },
                    label = { Text("Visual Studio Output") }
                )
                FilterChip(
                    selected = showRawJsonTab,
                    onClick = { onToggleTab(true) },
                    label = { Text("Raw JSON Inspector") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1-Tap Direct Mobile Download Action Row (Save PNG to Gallery / Save JSON to Downloads / Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (output.generatedBitmap != null) {
                    Button(
                        onClick = onDownloadImage,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("download_image_gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Unduh Gambar ke Galeri",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("UNDUH GAMBAR KE GALERI (PNG)", style = MaterialTheme.typography.labelMedium)
                    }
                }

                OutlinedButton(
                    onClick = onDownloadJson,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("download_json_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Unduh File JSON ke HP",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("UNDUH KE HP (.JSON)", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onShareResult,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("share_output_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan Hasil",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BAGIKAN", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!showRawJsonTab) {
                // 1. Rendered Canvas Bitmap Preview (for MAKER tools)
                if (output.generatedBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = output.generatedBitmap.asImageBitmap(),
                            contentDescription = output.summaryTitle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 2. Structured 16-Digit NIK Demographic Card
                if (output.nikResult != null) {
                    NikDemographicCard(nik = output.nikResult)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 3. Formatted Output Report
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = output.summaryTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = output.formattedReport,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                // Monospace Raw JSON Inspector
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = output.rawJson,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NikDemographicCard(nik: NikParseResult) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "NIK DEMOGRAPHIC TELEMETRY // ${nik.nik}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            NikRow("Validation Status", if (nik.isValid) "VALID (16-Digit Verified)" else "INVALID")
            NikRow("Province", "[${nik.provinceCode}] ${nik.provinceName}")
            NikRow("Regency / District", "Kab/Kota ${nik.regencyCode} • Kec ${nik.districtCode}")
            NikRow("Birth Date & Age", "${nik.birthDateFormatted} (${nik.ageYears} years)")
            NikRow("Gender", nik.gender)
            NikRow("Zodiac & Cohort", "${nik.zodiac} • ${nik.generationCohort}")
            NikRow("Registration Sequence", "#${nik.serialNumber}")
        }
    }
}

@Composable
private fun NikRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}
