package com.example.domain.engine

import android.util.Log
import com.example.data.catalog.JulianTool
import com.example.data.catalog.ToolEngineType
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private const val TAG = "HybridExecService"

data class ChatMessage(
    val id: String,
    val role: String, // "user" or "model"
    val text: String,
    val modelUsed: String,
    val timestampLabel: String,
    val isStreaming: Boolean = false
)

class HybridExecutionService {

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    // Lazy initialization of supported Gemini models per firebase-ai-logic-android skill
    private val geminiFlashModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    "You are JULIAN // OMNI-CORE v9.0, a hyper-luxury enterprise API & Bioscience intelligence engine. " +
                        "Provide crisp, authoritative, technically accurate responses with structured metrics."
                )
            }
        )
    }

    private val geminiArgonHighModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-4-argon",
            generationConfig = generationConfig {
                temperature = 0.4f
            },
            systemInstruction = content {
                text(
                    "You are JULIAN // OMNI-CORE v9.0 running on Gemini 4 Argon (High Thinking Mode). " +
                        "Perform rigorous step-by-step STEM, molecular bioscience, and systems architecture analysis."
                )
            }
        )
    }

    private val geminiFlashLiteModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            systemInstruction = content {
                text(
                    "You are the low-latency telemetry engine of JULIAN // OMNI-CORE v9.0. " +
                        "Deliver fast, high-signal structured outputs."
                )
            }
        )
    }

    suspend fun executeTool(
        tool: JulianTool,
        inputs: Map<String, String>,
        executionMode: String,
        baseUrl: String,
        apiKeyHeader: String,
        enableHighThinking: Boolean
    ): Result<ToolExecutionOutput> {
        val startMs = System.currentTimeMillis()
        return try {
            // 1. If user explicitly selected LIVE_REST, dispatch directly to the custom REST server
            if (executionMode == "LIVE_REST") {
                return executeLiveRestRequest(tool, inputs, baseUrl, apiKeyHeader)
            }

            // 2. Native Client-Side Visual & Deterministic Engines
            when (tool.engineType) {
                ToolEngineType.NATIVE_CANVAS_BRAT -> {
                    val bmp = withContext(Dispatchers.Default) {
                        NativeMakerCanvasEngine.renderBratCanvas(
                            text = inputs["text"].orEmpty(),
                            theme = inputs["theme"].orEmpty(),
                            blurMode = inputs["blur"].orEmpty()
                        )
                    }
                    val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(12L)
                    val json = JSONObject().apply {
                        put("status", 200)
                        put("endpoint", tool.endpointPath)
                        put("engine", "NATIVE_CANVAS_BRAT")
                        put("resolution", "${bmp.width}x${bmp.height}")
                        put("latencyMs", elapsed)
                        put("inputs", JSONObject(inputs))
                    }
                    Result.success(
                        ToolExecutionOutput(
                            toolId = tool.id,
                            statusCode = 200,
                            latencyMs = elapsed,
                            executionSource = "Native Android Canvas (800x800 ARGB_8888)",
                            summaryTitle = "Brat Canvas Rendered (${bmp.width}×${bmp.height})",
                            formattedReport = "Rendered Brat typography visual asset on native Canvas.\n• Text: \"${inputs["text"]}\"\n• Theme: ${inputs["theme"]}\n• Mode: ${inputs["blur"]}",
                            rawJson = json.toString(2),
                            generatedBitmap = bmp
                        )
                    )
                }

                ToolEngineType.NATIVE_CANVAS_STRUK -> {
                    val bmp = withContext(Dispatchers.Default) {
                        NativeMakerCanvasEngine.renderStrukCanvas(
                            merchant = inputs["merchant"].orEmpty(),
                            itemsRaw = inputs["items"].orEmpty(),
                            cashier = inputs["cashier"].orEmpty()
                        )
                    }
                    val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(15L)
                    val json = JSONObject().apply {
                        put("status", 200)
                        put("endpoint", tool.endpointPath)
                        put("engine", "NATIVE_CANVAS_STRUK")
                        put("resolution", "${bmp.width}x${bmp.height}")
                        put("merchant", inputs["merchant"])
                        put("latencyMs", elapsed)
                    }
                    Result.success(
                        ToolExecutionOutput(
                            toolId = tool.id,
                            statusCode = 200,
                            latencyMs = elapsed,
                            executionSource = "Native Thermal POS Canvas Studio",
                            summaryTitle = "Thermal Receipt Generated (${bmp.width}×${bmp.height})",
                            formattedReport = "Generated high-contrast thermal POS receipt with 11% PPN tax calculation and deterministic 1D verification barcode.",
                            rawJson = json.toString(2),
                            generatedBitmap = bmp
                        )
                    )
                }

                ToolEngineType.NATIVE_CANVAS_KALENDER -> {
                    val bmp = withContext(Dispatchers.Default) {
                        NativeMakerCanvasEngine.renderKalenderCanvas(
                            monthYear = inputs["monthYear"].orEmpty(),
                            highlightDayStr = inputs["highlightDay"].orEmpty(),
                            eventTitle = inputs["eventTitle"].orEmpty()
                        )
                    }
                    val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(14L)
                    val json = JSONObject().apply {
                        put("status", 200)
                        put("endpoint", tool.endpointPath)
                        put("engine", "NATIVE_CANVAS_KALENDER")
                        put("monthYear", inputs["monthYear"])
                        put("highlightDay", inputs["highlightDay"])
                    }
                    Result.success(
                        ToolExecutionOutput(
                            toolId = tool.id,
                            statusCode = 200,
                            latencyMs = elapsed,
                            executionSource = "Native Quantum Ephemeris Canvas",
                            summaryTitle = "Calendar Poster Rendered (${ inputs["monthYear"] })",
                            formattedReport = "Generated monthly calendar card highlighting Day ${inputs["highlightDay"]} (${inputs["eventTitle"]}).",
                            rawJson = json.toString(2),
                            generatedBitmap = bmp
                        )
                    )
                }

                ToolEngineType.NATIVE_CANVAS_QRCODE -> {
                    val bmp = withContext(Dispatchers.Default) {
                        NativeMakerCanvasEngine.renderQrCodeCanvas(
                            payload = inputs["payload"].orEmpty(),
                            accent = inputs["accent"].orEmpty()
                        )
                    }
                    val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(11L)
                    val json = JSONObject().apply {
                        put("status", 200)
                        put("endpoint", tool.endpointPath)
                        put("engine", "NATIVE_CANVAS_QRCODE")
                        put("payload", inputs["payload"])
                        put("matrixSize", "25x25")
                    }
                    Result.success(
                        ToolExecutionOutput(
                            toolId = tool.id,
                            statusCode = 200,
                            latencyMs = elapsed,
                            executionSource = "Native 25×25 QR Matrix Encoder",
                            summaryTitle = "QR Matrix Encoded (25×25 Modules)",
                            formattedReport = "Encoded payload into a 25×25 QR matrix with 3 finder patterns, timing grid, and SHA-256 bit distribution.",
                            rawJson = json.toString(2),
                            generatedBitmap = bmp
                        )
                    )
                }

                ToolEngineType.NATIVE_CANVAS_CARBON -> {
                    val bmp = withContext(Dispatchers.Default) {
                        NativeMakerCanvasEngine.renderCarbonCanvas(
                            title = inputs["title"].orEmpty(),
                            code = inputs["code"].orEmpty()
                        )
                    }
                    val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(13L)
                    val json = JSONObject().apply {
                        put("status", 200)
                        put("endpoint", tool.endpointPath)
                        put("engine", "NATIVE_CANVAS_CARBON")
                        put("windowTitle", inputs["title"])
                    }
                    Result.success(
                        ToolExecutionOutput(
                            toolId = tool.id,
                            statusCode = 200,
                            latencyMs = elapsed,
                            executionSource = "Native Carbon Code Studio",
                            summaryTitle = "Carbon Snippet Rendered (${inputs["title"]})",
                            formattedReport = "Rendered macOS-style dark glass code card with traffic-light window controls and syntax highlighting.",
                            rawJson = json.toString(2),
                            generatedBitmap = bmp
                        )
                    )
                }

                ToolEngineType.NATIVE_CANVAS_BADGE -> {
                    val bmp = withContext(Dispatchers.Default) {
                        NativeMakerCanvasEngine.renderBadgeCanvas(
                            toolId = tool.id,
                            sender = inputs["sender"].orEmpty(),
                            message = inputs["message"].orEmpty(),
                            accent = inputs["accent"].orEmpty()
                        )
                    }
                    val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(12L)
                    val json = JSONObject().apply {
                        put("status", 200)
                        put("endpoint", tool.endpointPath)
                        put("engine", "NATIVE_CANVAS_BADGE")
                        put("sender", inputs["sender"])
                        put("message", inputs["message"])
                    }
                    Result.success(
                        ToolExecutionOutput(
                            toolId = tool.id,
                            statusCode = 200,
                            latencyMs = elapsed,
                            executionSource = "Native Visual Studio Canvas",
                            summaryTitle = "${tool.title} Rendered",
                            formattedReport = "Generated visual studio card for ${tool.commandName}.",
                            rawJson = json.toString(2),
                            generatedBitmap = bmp
                        )
                    )
                }

                ToolEngineType.NATIVE_NIK_PARSER -> {
                    val nikRaw = inputs["nik"].orEmpty()
                    val parsed = NativeUtilityEngine.parseIndonesianNik(nikRaw)
                    val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(6L)
                    val json = JSONObject().apply {
                        put("status", if (parsed.isValid) 200 else 422)
                        put("endpoint", tool.endpointPath)
                        put("nik", parsed.nik)
                        put("valid", parsed.isValid)
                        put("provinceCode", parsed.provinceCode)
                        put("provinceName", parsed.provinceName)
                        put("regencyCode", parsed.regencyCode)
                        put("districtCode", parsed.districtCode)
                        put("birthDate", parsed.birthDateFormatted)
                        put("gender", parsed.gender)
                        put("ageYears", parsed.ageYears)
                        put("zodiac", parsed.zodiac)
                        put("generationCohort", parsed.generationCohort)
                        put("serialSequence", parsed.serialNumber)
                    }
                    val report = buildString {
                        appendLine("INDONESIAN 16-DIGIT NIK DEMOGRAPHIC BREAKDOWN")
                        appendLine("• Status: ${if (parsed.isValid) "VALIDATED (16 Digits)" else "INVALID FORMAT"}")
                        appendLine("• Province: [${parsed.provinceCode}] ${parsed.provinceName}")
                        appendLine("• Regency/City Code: ${parsed.regencyCode}  |  District Code: ${parsed.districtCode}")
                        appendLine("• Birth Date: ${parsed.birthDateFormatted} (Age: ${parsed.ageYears} yrs)")
                        appendLine("• Gender: ${parsed.gender}")
                        appendLine("• Zodiac & Cohort: ${parsed.zodiac} • ${parsed.generationCohort}")
                        appendLine("• Registration Sequence: #${parsed.serialNumber}")
                    }
                    Result.success(
                        ToolExecutionOutput(
                            toolId = tool.id,
                            statusCode = if (parsed.isValid) 200 else 422,
                            latencyMs = elapsed,
                            executionSource = "Native 16-Digit NIK Demographic Parser",
                            summaryTitle = "${parsed.provinceName} • ${parsed.birthDateFormatted}",
                            formattedReport = report.trim(),
                            rawJson = json.toString(2),
                            nikResult = parsed
                        )
                    )
                }

                ToolEngineType.NATIVE_TTS -> {
                    val script = inputs["text"].orEmpty()
                    val pitch = inputs["pitch"] ?: "1.0"
                    val rate = inputs["rate"] ?: "1.0"
                    val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(8L)
                    val json = JSONObject().apply {
                        put("status", 200)
                        put("endpoint", tool.endpointPath)
                        put("engine", "ANDROID_NATIVE_TTS")
                        put("script", script)
                        put("pitch", pitch)
                        put("speechRate", rate)
                    }
                    Result.success(
                        ToolExecutionOutput(
                            toolId = tool.id,
                            statusCode = 200,
                            latencyMs = elapsed,
                            executionSource = "Native Android TextToSpeech Synthesizer",
                            summaryTitle = "Audio Dispatched to Android Speech Engine",
                            formattedReport = "Synthesizing speech via Android TextToSpeech:\n• Script: \"$script\"\n• Pitch: ${pitch}x  |  Rate: ${rate}x",
                            rawJson = json.toString(2),
                            isTtsTriggered = true
                        )
                    )
                }

                ToolEngineType.NATIVE_CURRENCY -> {
                    Result.success(NativeUtilityEngine.executeCurrencyConversion(inputs))
                }

                ToolEngineType.NATIVE_CRYPTO_UTIL,
                ToolEngineType.NATIVE_UPLOAD_INSPECTOR -> {
                    Result.success(NativeUtilityEngine.executeDeterministicUtility(tool, inputs))
                }

                ToolEngineType.GEMINI_ARGON_HIGH,
                ToolEngineType.GEMINI_FLASH,
                ToolEngineType.GEMINI_FLASH_LITE,
                ToolEngineType.GEMINI_SEARCH_GROUNDED,
                ToolEngineType.GEMINI_MAPS_GROUNDED -> {
                    executeGeminiTool(tool, inputs, enableHighThinking, startMs)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Tool execution failed for ${tool.id}", e)
            Result.failure(e)
        }
    }

    private suspend fun executeGeminiTool(
        tool: JulianTool,
        inputs: Map<String, String>,
        enableHighThinking: Boolean,
        startMs: Long
    ): Result<ToolExecutionOutput> {
        val useArgon = tool.engineType == ToolEngineType.GEMINI_ARGON_HIGH ||
            (enableHighThinking && tool.category.name == "AI")
        val useLite = tool.engineType == ToolEngineType.GEMINI_FLASH_LITE && !useArgon

        val selectedModel = when {
            useArgon -> geminiArgonHighModel
            useLite -> geminiFlashLiteModel
            else -> geminiFlashModel
        }
        val modelLabel = when {
            useArgon -> "gemini-4-argon (Thinking: HIGH)"
            useLite -> "gemini-3.5-flash-lite"
            tool.engineType == ToolEngineType.GEMINI_SEARCH_GROUNDED -> "gemini-3.8-flash (Search Grounded)"
            tool.engineType == ToolEngineType.GEMINI_MAPS_GROUNDED -> "gemini-3.8-flash (Maps Grounded)"
            else -> "gemini-3.8-flash"
        }

        val inputSummary = inputs.entries.joinToString("\n") { (k, v) -> "• $k: $v" }
        val prompt = buildString {
            appendLine("Execute JULIAN // OMNI-CORE v9.0 module:")
            appendLine("Command: ${tool.commandName}")
            appendLine("Title: ${tool.title}")
            appendLine("Endpoint: ${tool.endpointPath}")
            appendLine("Parameters:")
            appendLine(inputSummary)
            appendLine()
            when (tool.engineType) {
                ToolEngineType.GEMINI_SEARCH_GROUNDED ->
                    appendLine("Provide up-to-date, search-grounded factual telemetry, key metrics, and verified reference points.")
                ToolEngineType.GEMINI_MAPS_GROUNDED ->
                    appendLine("Provide geospatial coordinates, neighborhood context, operating details, and routing guidance.")
                else ->
                    appendLine("Provide a comprehensive, well-structured technical response with actionable findings.")
            }
        }

        return try {
            val response = withTimeout(60_000L) {
                selectedModel.generateContent(prompt)
            }
            val text = response.text?.trim()
            if (text.isNullOrBlank()) {
                return Result.failure(IllegalStateException("Model returned an empty response."))
            }
            val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
            val json = JSONObject().apply {
                put("status", 200)
                put("toolId", tool.id)
                put("endpoint", tool.endpointPath)
                put("model", modelLabel)
                put("latencyMs", elapsed)
                put("parameters", JSONObject(inputs))
                put("result", text)
            }
            Result.success(
                ToolExecutionOutput(
                    toolId = tool.id,
                    statusCode = 200,
                    latencyMs = elapsed,
                    executionSource = modelLabel,
                    summaryTitle = "${tool.title} // Completed in ${elapsed}ms",
                    formattedReport = text,
                    rawJson = json.toString(2)
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini generation timed out after 60s", e)
            Result.failure(IllegalStateException("Generation timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation failed", e)
            Result.failure(e)
        }
    }

    suspend fun executeLiveRestRequest(
        tool: JulianTool,
        inputs: Map<String, String>,
        baseUrl: String,
        apiKeyHeader: String
    ): Result<ToolExecutionOutput> = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        val cleanBase = baseUrl.trim().trimEnd('/')
        val fullUrl = "$cleanBase${tool.endpointPath}"

        try {
            val requestBuilder = Request.Builder()
            if (apiKeyHeader.isNotBlank()) {
                requestBuilder.addHeader("X-Julian-Api-Key", apiKeyHeader.trim())
                requestBuilder.addHeader("Authorization", "Bearer ${apiKeyHeader.trim()}")
            }
            requestBuilder.addHeader("Accept", "application/json")
            requestBuilder.addHeader("User-Agent", "Julian-OmniCore-Android/9.0")

            if (tool.httpMethod.equals("POST", ignoreCase = true)) {
                val bodyJson = JSONObject(inputs).toString()
                requestBuilder.url(fullUrl)
                requestBuilder.post(bodyJson.toRequestBody("application/json; charset=utf-8".toMediaType()))
            } else {
                val queryParams = inputs.entries.joinToString("&") { (k, v) ->
                    "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}"
                }
                val urlWithParams = if (queryParams.isNotEmpty()) "$fullUrl?$queryParams" else fullUrl
                requestBuilder.url(urlWithParams)
                requestBuilder.get()
            }

            okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
                val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                val rawBody = response.body?.string().orEmpty()
                val formattedJson = try {
                    JSONObject(rawBody).toString(2)
                } catch (e: Exception) {
                    JSONObject().apply {
                        put("status", response.code)
                        put("url", fullUrl)
                        put("rawResponse", rawBody.take(2000))
                    }.toString(2)
                }

                Result.success(
                    ToolExecutionOutput(
                        toolId = tool.id,
                        statusCode = response.code,
                        latencyMs = elapsed,
                        executionSource = "Live REST Client ($cleanBase)",
                        summaryTitle = "HTTP ${response.code} • ${elapsed}ms",
                        formattedReport = "Dispatched live ${tool.httpMethod} request to $fullUrl\nStatus: HTTP ${response.code}\n\n${rawBody.take(1500)}",
                        rawJson = formattedJson
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Live REST request failed for $fullUrl", e)
            Result.failure(
                IllegalStateException(
                    "Live REST connection to $fullUrl failed (${e.javaClass.simpleName}: ${e.localizedMessage}). " +
                        "Verify your Custom Base URL in API Settings or switch to Hybrid/Native Studio mode."
                )
            )
        }
    }

    fun streamOmniChat(
        history: List<ChatMessage>,
        userPrompt: String,
        useHighThinkingArgon: Boolean
    ): Flow<String> = flow {
        val model = if (useHighThinkingArgon) geminiArgonHighModel else geminiFlashModel
        val sdkHistory: List<Content> = history
            .filter { it.text.isNotBlank() && !it.isStreaming }
            .map { msg ->
                content(role = if (msg.role == "user") "user" else "model") {
                    text(msg.text)
                }
            }

        val chat = model.startChat(history = sdkHistory)
        val accumulated = StringBuilder()

        withTimeout(60_000L) {
            chat.sendMessageStream(userPrompt).collect { chunk ->
                val part = chunk.text.orEmpty()
                if (part.isNotEmpty()) {
                    accumulated.append(part)
                    emit(accumulated.toString())
                }
            }
        }
    }
}
