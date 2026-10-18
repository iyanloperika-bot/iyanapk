package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.catalog.JulianTool
import com.example.data.catalog.JulianToolCatalog
import com.example.data.catalog.ToolCategory
import com.example.data.model.BookmarkedTool
import com.example.data.model.UserApiConfig
import com.example.data.repository.JulianFirestoreRepository
import com.example.domain.engine.ChatMessage
import com.example.domain.engine.HybridExecutionService
import com.example.domain.engine.ToolExecutionOutput
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "JulianViewModel"

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface ExecutionUiState {
    data object Idle : ExecutionUiState
    data object Running : ExecutionUiState
    data class Success(val output: ToolExecutionOutput) : ExecutionUiState
    data class Error(val message: String) : ExecutionUiState
}

data class ActivityLogEntry(
    val id: String,
    val toolId: String,
    val commandName: String,
    val endpointPath: String,
    val statusCode: Int,
    val latencyMs: Long,
    val source: String,
    val timeLabel: String
)

class JulianViewModel(
    private val repository: JulianFirestoreRepository,
    private val executionService: HybridExecutionService,
    val currentUserId: String
) : ViewModel() {

    // Two-tier Flow error handling for Bookmarks
    val bookmarksState: StateFlow<UiState<List<BookmarkedTool>>> = repository
        .observeBookmarks(currentUserId)
        .map<List<BookmarkedTool>, UiState<List<BookmarkedTool>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing bookmarks", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to observe bookmarks"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Two-tier Flow error handling for UserApiConfig
    val apiConfigState: StateFlow<UserApiConfig> = repository
        .observeApiConfig(currentUserId)
        .map { it ?: UserApiConfig(userId = currentUserId) }
        .catch { error ->
            Log.w(TAG, "Error observing apiConfig", error)
            emit(UserApiConfig(userId = currentUserId))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UserApiConfig(userId = currentUserId)
        )

    private val _selectedCategory = MutableStateFlow<ToolCategory?>(null)
    val selectedCategory: StateFlow<ToolCategory?> = _selectedCategory.asStateFlow()

    private val _showBookmarksOnly = MutableStateFlow(false)
    val showBookmarksOnly: StateFlow<Boolean> = _showBookmarksOnly.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isTableView = MutableStateFlow(false)
    val isTableView: StateFlow<Boolean> = _isTableView.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _activeTool = MutableStateFlow<JulianTool?>(null)
    val activeTool: StateFlow<JulianTool?> = _activeTool.asStateFlow()

    private val _toolInputValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val toolInputValues: StateFlow<Map<String, String>> = _toolInputValues.asStateFlow()

    private val _studioModeOverride = MutableStateFlow<String?>(null)
    val studioModeOverride: StateFlow<String?> = _studioModeOverride.asStateFlow()

    private val _executionState = MutableStateFlow<ExecutionUiState>(ExecutionUiState.Idle)
    val executionState: StateFlow<ExecutionUiState> = _executionState.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<ActivityLogEntry>>(emptyList())
    val recentLogs: StateFlow<List<ActivityLogEntry>> = _recentLogs.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Multi-turn Omni-Core Gemini Chatbot state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome_01",
                role = "model",
                text = "JULIAN // OMNI-CORE v9.0 neural assistant online. Ask me to architect REST endpoints, analyze molecular/bioscience sequences, or chain any of the 85 studio modules.",
                modelUsed = "gemini-4-argon (Thinking: HIGH)",
                timestampLabel = "ONLINE"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatStreaming = MutableStateFlow(false)
    val isChatStreaming: StateFlow<Boolean> = _isChatStreaming.asStateFlow()

    private val _chatUseArgonHigh = MutableStateFlow(true)
    val chatUseArgonHigh: StateFlow<Boolean> = _chatUseArgonHigh.asStateFlow()

    val filteredTools: StateFlow<List<JulianTool>> = combine(
        _selectedCategory,
        _showBookmarksOnly,
        _searchQuery,
        bookmarksState
    ) { category, bookmarksOnly, query, bState ->
        val bookmarkedIds = (bState as? UiState.Success)?.data?.map { it.toolId }?.toSet() ?: emptySet()
        val q = query.trim().lowercase(Locale.ROOT)

        JulianToolCatalog.allTools.filter { tool ->
            val matchesCat = category == null || tool.category == category
            val matchesBook = !bookmarksOnly || bookmarkedIds.contains(tool.id)
            val matchesQuery = q.isEmpty() ||
                tool.title.lowercase(Locale.ROOT).contains(q) ||
                tool.commandName.lowercase(Locale.ROOT).contains(q) ||
                tool.endpointPath.lowercase(Locale.ROOT).contains(q) ||
                tool.description.lowercase(Locale.ROOT).contains(q) ||
                tool.category.label.lowercase(Locale.ROOT).contains(q)
            matchesCat && matchesBook && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = JulianToolCatalog.allTools
    )

    fun selectCategory(category: ToolCategory?) {
        _selectedCategory.value = category
        _showBookmarksOnly.value = false
    }

    fun toggleBookmarksFilter() {
        _showBookmarksOnly.value = !_showBookmarksOnly.value
        if (_showBookmarksOnly.value) {
            _selectedCategory.value = null
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleViewMode() {
        _isTableView.value = !_isTableView.value
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun openToolStudio(tool: JulianTool) {
        _activeTool.value = tool
        _toolInputValues.value = tool.inputs.associate { it.key to it.defaultValue }
        _studioModeOverride.value = null
        _executionState.value = ExecutionUiState.Idle
    }

    fun closeToolStudio() {
        _activeTool.value = null
        _executionState.value = ExecutionUiState.Idle
    }

    fun updateToolInput(key: String, value: String) {
        _toolInputValues.value = _toolInputValues.value.toMutableMap().apply {
            put(key, value)
        }
    }

    fun setStudioModeOverride(mode: String?) {
        _studioModeOverride.value = mode
    }

    fun toggleBookmark(tool: JulianTool) {
        viewModelScope.launch {
            val currentList = (bookmarksState.value as? UiState.Success)?.data ?: emptyList()
            val isCurrentlyBookmarked = currentList.any { it.toolId == tool.id }
            if (isCurrentlyBookmarked) {
                val res = repository.removeBookmark(tool.id)
                if (res.isSuccess) {
                    _snackbarMessage.value = "Removed '${tool.commandName}' from cloud bookmarks"
                } else {
                    _snackbarMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to remove bookmark"
                }
            } else {
                val res = repository.saveBookmark(
                    toolId = tool.id,
                    category = tool.category.name,
                    note = tool.endpointPath
                )
                if (res.isSuccess) {
                    _snackbarMessage.value = "Synced '${tool.commandName}' to Cloud Firestore"
                } else {
                    _snackbarMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to save bookmark"
                }
            }
        }
    }

    fun saveApiSettings(
        baseUrl: String,
        apiKeyHeader: String,
        executionMode: String,
        enableHighThinking: Boolean
    ) {
        viewModelScope.launch {
            val result = repository.saveApiConfig(
                baseUrl = baseUrl,
                apiKeyHeader = apiKeyHeader,
                executionMode = executionMode,
                enableHighThinking = enableHighThinking
            )
            if (result.isSuccess) {
                _snackbarMessage.value = "API Endpoint Configuration synced to Cloud Firestore"
            } else {
                _snackbarMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to save API settings"
            }
        }
    }

    fun executeActiveTool() {
        val tool = _activeTool.value ?: return
        val inputs = _toolInputValues.value
        val config = apiConfigState.value
        val effectiveMode = _studioModeOverride.value ?: config.executionMode

        viewModelScope.launch {
            _executionState.value = ExecutionUiState.Running
            val result = executionService.executeTool(
                tool = tool,
                inputs = inputs,
                executionMode = effectiveMode,
                baseUrl = config.baseUrl,
                apiKeyHeader = config.apiKeyHeader,
                enableHighThinking = config.enableHighThinking
            )
            result.fold(
                onSuccess = { output ->
                    _executionState.value = ExecutionUiState.Success(output)
                    val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                    val entry = ActivityLogEntry(
                        id = UUID.randomUUID().toString(),
                        toolId = tool.id,
                        commandName = tool.commandName,
                        endpointPath = tool.endpointPath,
                        statusCode = output.statusCode,
                        latencyMs = output.latencyMs,
                        source = output.executionSource,
                        timeLabel = timeFmt
                    )
                    _recentLogs.value = (listOf(entry) + _recentLogs.value).take(25)
                },
                onFailure = { error ->
                    _executionState.value = ExecutionUiState.Error(
                        error.localizedMessage ?: "Tool execution failed."
                    )
                }
            )
        }
    }

    fun setChatUseArgonHigh(enabled: Boolean) {
        _chatUseArgonHigh.value = enabled
    }

    fun sendOmniChatMessage(userText: String) {
        val clean = userText.trim()
        if (clean.isEmpty() || _isChatStreaming.value) return

        val timeFmt = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "user",
            text = clean,
            modelUsed = "Operative",
            timestampLabel = timeFmt
        )
        val historySnapshot = _chatMessages.value
        val modelId = UUID.randomUUID().toString()
        val modelLabel = if (_chatUseArgonHigh.value) "gemini-4-argon (Thinking: HIGH)" else "gemini-3.8-flash"

        val placeholderModelMsg = ChatMessage(
            id = modelId,
            role = "model",
            text = "",
            modelUsed = modelLabel,
            timestampLabel = timeFmt,
            isStreaming = true
        )

        _chatMessages.value = historySnapshot + userMsg + placeholderModelMsg
        _isChatStreaming.value = true

        viewModelScope.launch {
            try {
                executionService.streamOmniChat(
                    history = historySnapshot,
                    userPrompt = clean,
                    useHighThinkingArgon = _chatUseArgonHigh.value
                ).collect { accumulated ->
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == modelId) msg.copy(text = accumulated, isStreaming = true) else msg
                    }
                }
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == modelId) {
                        val finalText = msg.text.ifBlank { "Completed analysis." }
                        msg.copy(text = finalText, isStreaming = false)
                    } else msg
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "OmniChat streaming failed", e)
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == modelId) {
                        msg.copy(
                            text = "Omni-Core AI Error: ${e.localizedMessage ?: "Request failed"}",
                            isStreaming = false
                        )
                    } else msg
                }
            } finally {
                _isChatStreaming.value = false
            }
        }
    }

    fun postSnackbarMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    companion object {
        fun provideFactory(currentUserId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                JulianViewModel(
                    repository = JulianFirestoreRepository(db),
                    executionService = HybridExecutionService(),
                    currentUserId = currentUserId
                )
            }
        }
    }
}
