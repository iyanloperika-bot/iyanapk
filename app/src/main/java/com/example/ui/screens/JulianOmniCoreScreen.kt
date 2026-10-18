package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.catalog.JulianTool
import com.example.data.catalog.JulianToolCatalog
import com.example.data.catalog.ToolCategory
import com.example.ui.components.JulianActivityLogDialog
import com.example.ui.components.JulianApiSettingsDialog
import com.example.ui.components.JulianMobileDownloadDialog
import com.example.ui.components.JulianOmniChatModal
import com.example.ui.components.JulianStudioModal
import com.example.ui.viewmodel.JulianViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.DirectDownloadHelper

@Composable
fun JulianOmniCoreScreen(
    viewModel: JulianViewModel,
    onSignOut: () -> Unit
) {
    val bookmarksState by viewModel.bookmarksState.collectAsStateWithLifecycle()
    val apiConfig by viewModel.apiConfigState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showBookmarksOnly by viewModel.showBookmarksOnly.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isTableView by viewModel.isTableView.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val filteredTools by viewModel.filteredTools.collectAsStateWithLifecycle()
    val activeTool by viewModel.activeTool.collectAsStateWithLifecycle()
    val toolInputValues by viewModel.toolInputValues.collectAsStateWithLifecycle()
    val studioModeOverride by viewModel.studioModeOverride.collectAsStateWithLifecycle()
    val executionState by viewModel.executionState.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatStreaming by viewModel.isChatStreaming.collectAsStateWithLifecycle()
    val chatUseArgonHigh by viewModel.chatUseArgonHigh.collectAsStateWithLifecycle()

    val bookmarkedIds = remember(bookmarksState) {
        (bookmarksState as? UiState.Success)?.data?.map { it.toolId }?.toSet() ?: emptySet()
    }
    val categoryCounts = remember { JulianToolCatalog.getCategoryCounts() }

    var showApiSettingsDialog by remember { mutableStateOf(false) }
    var showActivityLogDialog by remember { mutableStateOf(false) }
    var showOmniChatModal by remember { mutableStateOf(false) }
    var showMobileDownloadDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val primaryGold = MaterialTheme.colorScheme.primary
    val secondaryCyan = MaterialTheme.colorScheme.secondary

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (activeTool == null && !showOmniChatModal) {
                ExtendedFloatingActionButton(
                    onClick = { showOmniChatModal = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("open_omni_chat_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Open Omni-Core AI Chat"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OMNI-CORE AI",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Subtle Quantum Grid & Specular Glow Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridStep = 72.dp.toPx()
                var gx = 0f
                while (gx < size.width) {
                    drawLine(
                        color = primaryGold.copy(alpha = 0.04f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, size.height),
                        strokeWidth = 1f
                    )
                    gx += gridStep
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(secondaryCyan.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.12f),
                        radius = size.minDimension * 0.5f
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.12f),
                    radius = size.minDimension * 0.5f
                )
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val columns = when {
                    maxWidth >= 900.dp -> 3
                    maxWidth >= 600.dp -> 2
                    else -> 1
                }

                if (!isTableView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                            bottom = 96.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("tools_grid_list")
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            JulianCommandHeaderSection(
                                totalCount = JulianToolCatalog.allTools.size,
                                filteredCount = filteredTools.size,
                                bookmarkedCount = bookmarkedIds.size,
                                recentLogsCount = recentLogs.size,
                                activeMode = apiConfig.executionMode,
                                baseUrl = apiConfig.baseUrl,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                showBookmarksOnly = showBookmarksOnly,
                                categoryCounts = categoryCounts,
                                isTableView = isTableView,
                                isDarkTheme = isDarkTheme,
                                onSearchChange = viewModel::updateSearchQuery,
                                onSelectCategory = viewModel::selectCategory,
                                onToggleBookmarksOnly = viewModel::toggleBookmarksFilter,
                                onToggleViewMode = viewModel::toggleViewMode,
                                onToggleTheme = viewModel::toggleTheme,
                                onOpenApiSettings = { showApiSettingsDialog = true },
                                onOpenActivityLog = { showActivityLogDialog = true },
                                onOpenMobileDownloadHub = { showMobileDownloadDialog = true },
                                onQuickLaunchTool = { toolId ->
                                    JulianToolCatalog.allTools.find { it.id == toolId }?.let {
                                        viewModel.openToolStudio(it)
                                    }
                                },
                                onSignOut = onSignOut
                            )
                        }

                        items(filteredTools, key = { it.id }) { tool ->
                            JulianToolGridCard(
                                tool = tool,
                                isBookmarked = bookmarkedIds.contains(tool.id),
                                onOpenStudio = { viewModel.openToolStudio(tool) },
                                onToggleBookmark = { viewModel.toggleBookmark(tool) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("tools_table_list")
                    ) {
                        item {
                            JulianCommandHeaderSection(
                                totalCount = JulianToolCatalog.allTools.size,
                                filteredCount = filteredTools.size,
                                bookmarkedCount = bookmarkedIds.size,
                                recentLogsCount = recentLogs.size,
                                activeMode = apiConfig.executionMode,
                                baseUrl = apiConfig.baseUrl,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                showBookmarksOnly = showBookmarksOnly,
                                categoryCounts = categoryCounts,
                                isTableView = isTableView,
                                isDarkTheme = isDarkTheme,
                                onSearchChange = viewModel::updateSearchQuery,
                                onSelectCategory = viewModel::selectCategory,
                                onToggleBookmarksOnly = viewModel::toggleBookmarksFilter,
                                onToggleViewMode = viewModel::toggleViewMode,
                                onToggleTheme = viewModel::toggleTheme,
                                onOpenApiSettings = { showApiSettingsDialog = true },
                                onOpenActivityLog = { showActivityLogDialog = true },
                                onOpenMobileDownloadHub = { showMobileDownloadDialog = true },
                                onQuickLaunchTool = { toolId ->
                                    JulianToolCatalog.allTools.find { it.id == toolId }?.let {
                                        viewModel.openToolStudio(it)
                                    }
                                },
                                onSignOut = onSignOut
                            )
                        }

                        items(filteredTools, key = { it.id }) { tool ->
                            JulianToolTableRow(
                                tool = tool,
                                isBookmarked = bookmarkedIds.contains(tool.id),
                                onOpenStudio = { viewModel.openToolStudio(tool) },
                                onToggleBookmark = { viewModel.toggleBookmark(tool) }
                            )
                        }
                    }
                }
            }

            // Full-Screen Interactive Tool Studio Overlay
            val currentActiveTool = activeTool
            if (currentActiveTool != null) {
                JulianStudioModal(
                    tool = currentActiveTool,
                    inputValues = toolInputValues,
                    effectiveMode = studioModeOverride ?: apiConfig.executionMode,
                    isBookmarked = bookmarkedIds.contains(currentActiveTool.id),
                    executionState = executionState,
                    onUpdateInput = viewModel::updateToolInput,
                    onSelectModeOverride = viewModel::setStudioModeOverride,
                    onToggleBookmark = { viewModel.toggleBookmark(currentActiveTool) },
                    onExecute = viewModel::executeActiveTool,
                    onCopyToast = { msg ->
                        viewModel.postSnackbarMessage(msg)
                    },
                    onClose = viewModel::closeToolStudio
                )
            }

            // Multi-Turn Omni-Core Gemini Chatbot Overlay
            if (showOmniChatModal) {
                JulianOmniChatModal(
                    messages = chatMessages,
                    isStreaming = isChatStreaming,
                    useArgonHigh = chatUseArgonHigh,
                    onToggleArgonHigh = viewModel::setChatUseArgonHigh,
                    onSendMessage = viewModel::sendOmniChatMessage,
                    onClose = { showOmniChatModal = false }
                )
            }

            if (showApiSettingsDialog) {
                JulianApiSettingsDialog(
                    currentConfig = apiConfig,
                    onDismiss = { showApiSettingsDialog = false },
                    onSave = viewModel::saveApiSettings
                )
            }

            if (showActivityLogDialog) {
                JulianActivityLogDialog(
                    logs = recentLogs,
                    onDismiss = { showActivityLogDialog = false }
                )
            }

            if (showMobileDownloadDialog) {
                JulianMobileDownloadDialog(
                    baseUrl = apiConfig.baseUrl,
                    onExportAppZip = {
                        val res = DirectDownloadHelper.exportAppZipBundleToPhoneDownloads(
                            context = context,
                            baseUrl = apiConfig.baseUrl
                        )
                        res.fold(
                            onSuccess = { path ->
                                viewModel.postSnackbarMessage("File .ZIP berhasil diunduh ke HP: $path")
                            },
                            onFailure = { err ->
                                viewModel.postSnackbarMessage("Gagal mengunduh .ZIP: ${err.localizedMessage}")
                            }
                        )
                        showMobileDownloadDialog = false
                    },
                    onExportAllToolsJson = {
                        val res = DirectDownloadHelper.exportFullCatalogToPhoneDownloads(
                            context = context,
                            baseUrl = apiConfig.baseUrl
                        )
                        res.fold(
                            onSuccess = { path ->
                                viewModel.postSnackbarMessage("Bundle 85 API berhasil diunduh ke HP: $path")
                            },
                            onFailure = { err ->
                                viewModel.postSnackbarMessage("Gagal mengunduh bundle: ${err.localizedMessage}")
                            }
                        )
                        showMobileDownloadDialog = false
                    },
                    onDismiss = { showMobileDownloadDialog = false }
                )
            }
        }
    }
}

@Composable
private fun JulianCommandHeaderSection(
    totalCount: Int,
    filteredCount: Int,
    bookmarkedCount: Int,
    recentLogsCount: Int,
    activeMode: String,
    baseUrl: String,
    searchQuery: String,
    selectedCategory: ToolCategory?,
    showBookmarksOnly: Boolean,
    categoryCounts: Map<ToolCategory, Int>,
    isTableView: Boolean,
    isDarkTheme: Boolean,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (ToolCategory?) -> Unit,
    onToggleBookmarksOnly: () -> Unit,
    onToggleViewMode: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenApiSettings: () -> Unit,
    onOpenActivityLog: () -> Unit,
    onOpenMobileDownloadHub: () -> Unit,
    onQuickLaunchTool: (String) -> Unit,
    onSignOut: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Top Telemetry Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp)),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "J9",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "JULIAN // OMNI-CORE v9.0",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "MODE: $activeMode  •  $filteredCount/$totalCount ENDPOINTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenMobileDownloadHub,
                            modifier = Modifier.testTag("open_mobile_download_hub_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Unduh APK & Data Langsung di HP",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        IconButton(
                            onClick = onOpenApiSettings,
                            modifier = Modifier.testTag("open_api_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsEthernet,
                                contentDescription = "Configure REST Base URL",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onOpenActivityLog,
                            modifier = Modifier.testTag("open_activity_log_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Recent Telemetry Log ($recentLogsCount)",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.testTag("toggle_theme_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direct Mobile Download Banner (No Computer Needed)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { onOpenMobileDownloadHub() }
                        .testTag("direct_mobile_download_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Unduh APK di HP",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "UNDUH APK & HASIL LANGSUNG DI HP (TANPA PC)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "BUKA >",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Launch Hero Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "maker_brat" to "⚡ maker brat",
                        "maker_struk" to "🧾 maker struk",
                        "tools_nikcheck" to "🆔 tools nikcheck",
                        "maker_qrcode" to "🔳 maker qrcode",
                        "ai_argon_reasoner" to "🧠 ai argon",
                        "ai_bioscience_lab" to "🧬 ai bioscience",
                        "search_google_grounded" to "🌐 search google",
                        "search_maps_grounded" to "📍 search maps"
                    ).forEach { (id, label) ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .clickable { onQuickLaunchTool(id) }
                                .testTag("quick_launch_$id")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Command Palette Search + Grid/Table Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search 85 tools, /api/ paths, or commands...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Tools",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("global_search_input")
            )

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(54.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    .clickable { onToggleViewMode() }
                    .testTag("view_mode_toggle")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isTableView) Icons.Default.GridView else Icons.Default.TableRows,
                            contentDescription = "Switch View Mode",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTableView) "GRID" else "TABLE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // 9-Category Filter Rail with Count Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null && !showBookmarksOnly,
                onClick = { onSelectCategory(null) },
                label = { Text("ALL ($totalCount)", style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("category_chip_ALL")
            )

            FilterChip(
                selected = showBookmarksOnly,
                onClick = onToggleBookmarksOnly,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Bookmarks Filter",
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = { Text("SAVED ($bookmarkedCount)", style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("category_chip_BOOKMARKS")
            )

            ToolCategory.entries.forEach { category ->
                val count = categoryCounts[category] ?: 0
                FilterChip(
                    selected = selectedCategory == category && !showBookmarksOnly,
                    onClick = { onSelectCategory(category) },
                    label = {
                        Text("${category.label} ($count)", style = MaterialTheme.typography.labelMedium)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("category_chip_${category.name}")
                )
            }
        }
    }
}

@Composable
private fun JulianToolGridCard(
    tool: JulianTool,
    isBookmarked: Boolean,
    onOpenStudio: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isBookmarked) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onOpenStudio() }
            .testTag("tool_card_${tool.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = tool.category.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tool.httpMethod,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bookmark_toggle_${tool.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove Bookmark" else "Add Bookmark",
                        tint = if (isBookmarked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tool.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tool.endpointPath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tool.commandName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LAUNCH STUDIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Launch Studio",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun JulianToolTableRow(
    tool: JulianTool,
    isBookmarked: Boolean,
    onOpenStudio: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable { onOpenStudio() }
            .testTag("tool_row_${tool.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = tool.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${tool.commandName} — ${tool.title}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${tool.httpMethod} ${tool.endpointPath}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
