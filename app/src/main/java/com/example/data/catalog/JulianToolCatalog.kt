package com.example.data.catalog

enum class ToolCategory(val label: String, val badgeCode: String) {
    AI("AI", "AI-CORE"),
    DOWNLOAD("DOWNLOAD", "DL-STREAM"),
    INFO("INFO", "INFO-SYS"),
    MAKER("MAKER", "STUDIO-MKR"),
    MUSIC("MUSIC", "AUDIO-DSP"),
    RANDOM("RANDOM", "RNG-FEED"),
    SEARCH("SEARCH", "OMNI-SRCH"),
    TOOLS("TOOLS", "UTIL-OPS"),
    UPLOAD("UPLOAD", "CLOUD-UPL")
}

enum class ToolEngineType(val title: String) {
    NATIVE_CANVAS_BRAT("Native Brat Typography Canvas"),
    NATIVE_CANVAS_STRUK("Native Thermal Receipt Studio"),
    NATIVE_CANVAS_KALENDER("Native Quantum Calendar Canvas"),
    NATIVE_CANVAS_QRCODE("Native QR Matrix Encoder"),
    NATIVE_CANVAS_CARBON("Native Carbon Code Studio"),
    NATIVE_CANVAS_BADGE("Native Visual Studio Canvas"),
    NATIVE_NIK_PARSER("Native 16-Digit NIK Parser"),
    NATIVE_TTS("Native Android Speech Synthesizer"),
    NATIVE_CURRENCY("Native Multi-Currency FX Engine"),
    NATIVE_CRYPTO_UTIL("Native Deterministic Utility Engine"),
    NATIVE_UPLOAD_INSPECTOR("Native Media & Payload Inspector"),
    GEMINI_FLASH("Gemini 3.8 Flash Intelligence"),
    GEMINI_ARGON_HIGH("Gemini 4 Argon (High Thinking)"),
    GEMINI_FLASH_LITE("Gemini 3.5 Flash-Lite Fast Engine"),
    GEMINI_SEARCH_GROUNDED("Gemini 3.8 + Google Search Grounding"),
    GEMINI_MAPS_GROUNDED("Gemini 3.8 + Google Maps Grounding")
}

data class ToolInputField(
    val key: String,
    val label: String,
    val placeholder: String,
    val defaultValue: String,
    val options: List<String> = emptyList(),
    val isMultiline: Boolean = false
)

data class JulianTool(
    val id: String,
    val commandName: String,
    val title: String,
    val category: ToolCategory,
    val endpointPath: String,
    val httpMethod: String,
    val description: String,
    val engineType: ToolEngineType,
    val inputs: List<ToolInputField>
)

object JulianToolCatalog {

    val allTools: List<JulianTool> = buildList {
        // ==========================================
        // 1. AI CATEGORY (8 Tools)
        // ==========================================
        add(
            JulianTool(
                id = "ai_argon_reasoner",
                commandName = "ai argon",
                title = "Gemini 4 Argon Deep Reasoner",
                category = ToolCategory.AI,
                endpointPath = "/api/ai/argon",
                httpMethod = "POST",
                description = "High-thinking STEM, architecture, and bioscience reasoning engine powered by Gemini 4 Argon.",
                engineType = ToolEngineType.GEMINI_ARGON_HIGH,
                inputs = listOf(
                    ToolInputField("prompt", "Complex Query", "Enter a complex STEM, architecture, or bioscience problem...", "Analyze CRISPR-Cas9 off-target mitigation strategies in vivo and propose a quantitative scoring matrix.", isMultiline = true),
                    ToolInputField("domain", "Reasoning Domain", "Select domain", "Bioscience & Molecular Systems", listOf("Bioscience & Molecular Systems", "Quantum & Distributed Systems", "Algorithmic Cryptography", "General Deep Reasoning"))
                )
            )
        )
        add(
            JulianTool(
                id = "ai_gemini_flash",
                commandName = "ai gemini",
                title = "Gemini 3.8 Flash Omni-Synth",
                category = ToolCategory.AI,
                endpointPath = "/api/ai/gemini",
                httpMethod = "POST",
                description = "Frontier-speed general intelligence for synthesis, summarization, and technical drafting.",
                engineType = ToolEngineType.GEMINI_FLASH,
                inputs = listOf(
                    ToolInputField("prompt", "Prompt Instruction", "Ask Gemini 3.8 Flash anything...", "Draft a high-availability microservices API gateway specification with rate-limiting headers.", isMultiline = true),
                    ToolInputField("tone", "Output Style", "Select output style", "Executive Technical", listOf("Executive Technical", "Concise Bulletins", "JSON Schema Ready", "Academic Research"))
                )
            )
        )
        add(
            JulianTool(
                id = "ai_bioscience_lab",
                commandName = "ai bioscience",
                title = "Bioscience Protocol & Sequence Analyzer",
                category = ToolCategory.AI,
                endpointPath = "/api/ai/bioscience",
                httpMethod = "POST",
                description = "Analyzes DNA/RNA/protein sequences, calculates GC content, and generates wet-lab PCR/ELISA protocols.",
                engineType = ToolEngineType.GEMINI_ARGON_HIGH,
                inputs = listOf(
                    ToolInputField("sequence", "FASTA / Nucleotide Sequence or Target", "Paste DNA/RNA sequence or assay target...", "ATGGCCCTGTGGATGCGCCTCCTGCCCCTGCTGGCGCTGCTGGCCCTCTGGGGACCTGACCCAGCCGCAGCCTTTGTGAACCAACACCTGTGCGGCTCACACCTGGTGGAAGCTCTCTACCTAGTGTGCGGGGAACGAGGCTTCTTCTACACACCCAAGACCCGCCGGGAGGCAGAGGACCTGCAGVG", isMultiline = true),
                    ToolInputField("assayType", "Analysis Mode", "Choose protocol type", "GC Content + Primer Design + Translation", listOf("GC Content + Primer Design + Translation", "Protein Folding & Motif Summary", "PCR MasterMix Protocol", "CRISPR gRNA Target Scan"))
                )
            )
        )
        add(
            JulianTool(
                id = "ai_code_architect",
                commandName = "ai codegen",
                title = "Polyglot Code & Refactor Engine",
                category = ToolCategory.AI,
                endpointPath = "/api/ai/codegen",
                httpMethod = "POST",
                description = "Generates production-ready Kotlin, Rust, TypeScript, and Python modules with complexity analysis.",
                engineType = ToolEngineType.GEMINI_ARGON_HIGH,
                inputs = listOf(
                    ToolInputField("spec", "Feature Specification", "Describe the algorithm or module...", "Implement a lock-free token bucket rate limiter in Kotlin using AtomicLong with nanosecond precision.", isMultiline = true),
                    ToolInputField("language", "Target Language", "Select language", "Kotlin", listOf("Kotlin", "Rust", "TypeScript", "Python", "Go"))
                )
            )
        )
        add(
            JulianTool(
                id = "ai_sql_optimizer",
                commandName = "ai sqlopt",
                title = "SQL Query Planner & Optimizer",
                category = ToolCategory.AI,
                endpointPath = "/api/ai/sqlopt",
                httpMethod = "POST",
                description = "Transforms natural language or slow SQL queries into indexed PostgreSQL/BigQuery execution plans.",
                engineType = ToolEngineType.GEMINI_FLASH,
                inputs = listOf(
                    ToolInputField("query", "Schema or Slow SQL", "Enter SQL or schema description...", "SELECT u.id, COUNT(o.id) FROM users u LEFT JOIN orders o ON u.id = o.user_id WHERE o.created_at > '2026-01-01' GROUP BY u.id HAVING COUNT(o.id) > 5;", isMultiline = true),
                    ToolInputField("dialect", "SQL Dialect", "Select engine", "PostgreSQL 17", listOf("PostgreSQL 17", "Google BigQuery", "SQLite / Room", "ClickHouse"))
                )
            )
        )
        add(
            JulianTool(
                id = "ai_regex_synthesizer",
                commandName = "ai regex",
                title = "Neural Regex Synthesizer",
                category = ToolCategory.AI,
                endpointPath = "/api/ai/regex",
                httpMethod = "POST",
                description = "Synthesizes zero-backtracking regular expressions with token-by-token explanation and test cases.",
                engineType = ToolEngineType.GEMINI_FLASH_LITE,
                inputs = listOf(
                    ToolInputField("patternDesc", "Matching Rule", "Describe what to match...", "Match semantic version strings like v2.14.0-alpha.3+build.409 with named capture groups"),
                    ToolInputField("sampleText", "Test Corpus", "Sample text to validate", "Release v2.14.0-alpha.3+build.409 deployed over v1.9.2")
                )
            )
        )
        add(
            JulianTool(
                id = "ai_prompt_enhancer",
                commandName = "ai promptcraft",
                title = "System Prompt & Persona Architect",
                category = ToolCategory.AI,
                endpointPath = "/api/ai/promptcraft",
                httpMethod = "POST",
                description = "Upgrades raw ideas into structured system instructions with guardrails and few-shot exemplars.",
                engineType = ToolEngineType.GEMINI_FLASH,
                inputs = listOf(
                    ToolInputField("rawPrompt", "Raw Concept", "Enter initial prompt idea...", "Act as an API security auditor reviewing OpenAPI 3.1 specs for BOLA and JWT flaws.", isMultiline = true)
                )
            )
        )
        add(
            JulianTool(
                id = "ai_sentiment_telemetry",
                commandName = "ai sentiment",
                title = "Multilingual Sentiment & Intent Parser",
                category = ToolCategory.AI,
                endpointPath = "/api/ai/sentiment",
                httpMethod = "POST",
                description = "Extracts fine-grained emotional polarity, urgency score, and key entities from customer or social feeds.",
                engineType = ToolEngineType.GEMINI_FLASH_LITE,
                inputs = listOf(
                    ToolInputField("text", "Input Text", "Paste review, ticket, or post...", "The Julian v9.0 SDK cut our p99 latency from 420ms to 19ms, though we still need webhook retry docs!", isMultiline = true)
                )
            )
        )

        // ==========================================
        // 2. DOWNLOAD CATEGORY (8 Tools)
        // ==========================================
        val downloadTools = listOf(
            Triple("dl_tiktok", "download tiktok", "TikTok No-Watermark Stream Inspector" to "/api/download/tiktok"),
            Triple("dl_ig_reel", "download igdl", "Instagram Reels & Carousel Extractor" to "/api/download/igdl"),
            Triple("dl_yt_mp4", "download ytmp4", "YouTube Adaptive MP4 Stream Resolver" to "/api/download/ytmp4"),
            Triple("dl_yt_mp3", "download ytmp3", "YouTube Lossless Audio Transcoder" to "/api/download/ytmp3"),
            Triple("dl_twitter_x", "download twitter", "X / Twitter High-Bitrate Video Resolver" to "/api/download/twitter"),
            Triple("dl_spotify_track", "download spotify", "Spotify Track Metadata & Preview Resolver" to "/api/download/spotify"),
            Triple("dl_pinterest_pin", "download pinterest", "Pinterest UHD Pin & Board Asset Fetcher" to "/api/download/pinterest"),
            Triple("dl_mediafire", "download mediafire", "MediaFire Direct Mirror Link Analyzer" to "/api/download/mediafire")
        )
        downloadTools.forEach { (id, cmd, meta) ->
            add(
                JulianTool(
                    id = id,
                    commandName = cmd,
                    title = meta.first,
                    category = ToolCategory.DOWNLOAD,
                    endpointPath = meta.second,
                    httpMethod = "GET",
                    description = "Resolves CDN headers, stream bitrates, codec manifests, and direct download payload metadata via ${meta.second}.",
                    engineType = ToolEngineType.NATIVE_CRYPTO_UTIL,
                    inputs = listOf(
                        ToolInputField("url", "Target Media URL", "https://...", "https://media.julian-omnicore.dev/watch?v=${id.uppercase()}_90210"),
                        ToolInputField("quality", "Stream Quality Profile", "Select resolution/bitrate", "1080p60 UHD / 320kbps", listOf("4K HDR / FLAC", "1080p60 UHD / 320kbps", "720p HD / 192kbps", "Audio Only Opus"))
                    )
                )
            )
        }

        // ==========================================
        // 3. INFO CATEGORY (8 Tools)
        // ==========================================
        add(
            JulianTool(
                id = "info_gempa",
                commandName = "info gempa",
                title = "BMKG & Global Seismic Telemetry",
                category = ToolCategory.INFO,
                endpointPath = "/api/info/gempa",
                httpMethod = "GET",
                description = "Retrieves real-time tectonic & seismic activity analysis, depth, magnitude, and tsunami potential.",
                engineType = ToolEngineType.GEMINI_SEARCH_GROUNDED,
                inputs = listOf(
                    ToolInputField("region", "Monitoring Zone", "Enter country or fault zone...", "Indonesia Ring of Fire & Sunda Megathrust")
                )
            )
        )
        add(
            JulianTool(
                id = "info_cuaca",
                commandName = "info cuaca",
                title = "Hyper-Local Meteorological Station",
                category = ToolCategory.INFO,
                endpointPath = "/api/info/cuaca",
                httpMethod = "GET",
                description = "Provides atmospheric pressure, UV index, humidity, wind vector, and 24-hour forecast telemetry.",
                engineType = ToolEngineType.GEMINI_SEARCH_GROUNDED,
                inputs = listOf(
                    ToolInputField("city", "City / Coordinates", "Enter city name...", "Jakarta Selatan, Indonesia")
                )
            )
        )
        add(
            JulianTool(
                id = "info_jadwal_sholat",
                commandName = "info jadwalsholat",
                title = "Astronomical Prayer Time Ephemeris",
                category = ToolCategory.INFO,
                endpointPath = "/api/info/jadwalsholat",
                httpMethod = "GET",
                description = "Calculates Imsak, Fajr, Dhuhr, Asr, Maghrib, and Isha solar angles for any city.",
                engineType = ToolEngineType.GEMINI_FLASH_LITE,
                inputs = listOf(
                    ToolInputField("city", "City Location", "Enter city...", "Bandung, Indonesia"),
                    ToolInputField("date", "Target Date", "YYYY-MM-DD", "2026-10-17")
                )
            )
        )
        add(
            JulianTool(
                id = "info_kurs_valas",
                commandName = "info kurs",
                title = "Global FX & Crypto Spot Matrix",
                category = ToolCategory.INFO,
                endpointPath = "/api/info/kurs",
                httpMethod = "GET",
                description = "Real-time cross-currency conversion matrix for IDR, USD, EUR, JPY, SGD, and GBP.",
                engineType = ToolEngineType.NATIVE_CURRENCY,
                inputs = listOf(
                    ToolInputField("amount", "Base Amount", "Enter numeric amount", "1500"),
                    ToolInputField("from", "Source Currency", "Select base currency", "USD", listOf("USD", "IDR", "EUR", "JPY", "SGD", "GBP")),
                    ToolInputField("to", "Target Currency", "Select target currency", "IDR", listOf("IDR", "USD", "EUR", "JPY", "SGD", "GBP"))
                )
            )
        )
        add(
            JulianTool(
                id = "info_ip_whois",
                commandName = "info ipwhois",
                title = "ASN, BGP & GeoIP Intelligence",
                category = ToolCategory.INFO,
                endpointPath = "/api/info/ipwhois",
                httpMethod = "GET",
                description = "Inspects IPv4/IPv6 routing blocks, autonomous system numbers (ASN), ISP, and datacenter geo-coordinates.",
                engineType = ToolEngineType.NATIVE_CRYPTO_UTIL,
                inputs = listOf(
                    ToolInputField("ip", "IPv4 / IPv6 or Hostname", "8.8.8.8", "8.8.8.8")
                )
            )
        )
        add(
            JulianTool(
                id = "info_github_stalk",
                commandName = "info github",
                title = "GitHub Developer Telemetry Profiler",
                category = ToolCategory.INFO,
                endpointPath = "/api/info/github",
                httpMethod = "GET",
                description = "Analyzes open-source contributor velocity, top repositories, and language distribution.",
                engineType = ToolEngineType.GEMINI_SEARCH_GROUNDED,
                inputs = listOf(
                    ToolInputField("username", "GitHub Handle", "Enter username...", "JetBrains")
                )
            )
        )
        add(
            JulianTool(
                id = "info_bioscience_compound",
                commandName = "info pubchem",
                title = "PubChem & Molecular Property Lookup",
                category = ToolCategory.INFO,
                endpointPath = "/api/info/pubchem",
                httpMethod = "GET",
                description = "Retrieves IUPAC nomenclature, molecular weight, SMILES string, Lipinski Rule-of-5, and bioactivity.",
                engineType = ToolEngineType.GEMINI_ARGON_HIGH,
                inputs = listOf(
                    ToolInputField("compound", "Compound Name or Formula", "e.g. Caffeine, Adenosine Triphosphate", "Adenosine Triphosphate (ATP)")
                )
            )
        )
        add(
            JulianTool(
                id = "info_gsmarena_spec",
                commandName = "info gsmarena",
                title = "Mobile SoC & Hardware Spec Sheet",
                category = ToolCategory.INFO,
                endpointPath = "/api/info/gsmarena",
                httpMethod = "GET",
                description = "Extracts complete display, lithography, camera sensor, and battery telemetry for mobile devices.",
                engineType = ToolEngineType.GEMINI_SEARCH_GROUNDED,
                inputs = listOf(
                    ToolInputField("device", "Device Model", "Enter smartphone model...", "Google Pixel 10 Pro XL")
                )
            )
        )

        // ==========================================
        // 4. MAKER CATEGORY (10 Visual Studio Tools)
        // ==========================================
        add(
            JulianTool(
                id = "maker_brat",
                commandName = "maker brat",
                title = "Brat Album Typography Studio",
                category = ToolCategory.MAKER,
                endpointPath = "/api/maker/brat",
                httpMethod = "POST",
                description = "Live client-side Android Canvas engine rendering iconic low-fi stretched Brat typography on acid-lime or obsidian backdrops.",
                engineType = ToolEngineType.NATIVE_CANVAS_BRAT,
                inputs = listOf(
                    ToolInputField("text", "Brat Text", "Type lowercase brat text...", "julian omnicore v9 is so brat"),
                    ToolInputField("theme", "Color Theme", "Select theme", "Acid Lime (#8ACE00)", listOf("Acid Lime (#8ACE00)", "Obsidian Gold (#E5B842)", "Pure White (#FFFFFF)", "Hyper Pink (#FF2A85)")),
                    ToolInputField("blur", "Lo-Fi Pixel Blur", "Select blur intensity", "Authentic Lo-Fi", listOf("Authentic Lo-Fi", "Crisp HD", "Heavy Overdrive"))
                )
            )
        )
        add(
            JulianTool(
                id = "maker_struk",
                commandName = "maker struk",
                title = "Thermal POS Receipt Designer (Struk)",
                category = ToolCategory.MAKER,
                endpointPath = "/api/maker/struk",
                httpMethod = "POST",
                description = "Renders realistic thermal paper receipts with custom merchant header, itemized rows, tax calculation, and barcode footer.",
                engineType = ToolEngineType.NATIVE_CANVAS_STRUK,
                inputs = listOf(
                    ToolInputField("merchant", "Merchant Name", "Store or Cafe Name", "JULIAN CYBER-CAFE // JAKARTA"),
                    ToolInputField("items", "Line Items (Name:Price, comma-separated)", "Item:Price, Item:Price", "Quantum Espresso:45000, Croissant Gold:38000, Neural Cloud API Pass:120000"),
                    ToolInputField("cashier", "Cashier / Terminal ID", "Cashier name", "OMNI-POS-09")
                )
            )
        )
        add(
            JulianTool(
                id = "maker_kalender",
                commandName = "maker kalender",
                title = "Quantum Calendar Poster Studio",
                category = ToolCategory.MAKER,
                endpointPath = "/api/maker/kalender",
                httpMethod = "POST",
                description = "Generates a high-contrast monthly calendar card with highlighted milestone date and custom header.",
                engineType = ToolEngineType.NATIVE_CANVAS_KALENDER,
                inputs = listOf(
                    ToolInputField("monthYear", "Month & Year", "OCTOBER 2026", "OCTOBER 2026"),
                    ToolInputField("highlightDay", "Highlight Day (1-31)", "17", "17"),
                    ToolInputField("eventTitle", "Milestone Caption", "Launch Day", "JULIAN v9.0 OMNI-CORE RELEASE")
                )
            )
        )
        add(
            JulianTool(
                id = "maker_qrcode",
                commandName = "maker qrcode",
                title = "Quantum Matrix QR Code Studio",
                category = ToolCategory.MAKER,
                endpointPath = "/api/maker/qrcode",
                httpMethod = "POST",
                description = "Renders high-density deterministic QR code matrices with finder patterns, timing grids, and custom brand colors.",
                engineType = ToolEngineType.NATIVE_CANVAS_QRCODE,
                inputs = listOf(
                    ToolInputField("payload", "URL or Payload Data", "https://...", "https://julian-omnicore.dev/v9/auth"),
                    ToolInputField("accent", "Matrix Accent Color", "Select color", "Obsidian Gold", listOf("Obsidian Gold", "Quantum Cyan", "Classic Monochrome", "Emerald Matrix"))
                )
            )
        )
        add(
            JulianTool(
                id = "maker_carbon",
                commandName = "maker carbon",
                title = "Carbon Code Snippet Image Studio",
                category = ToolCategory.MAKER,
                endpointPath = "/api/maker/carbon",
                httpMethod = "POST",
                description = "Renders macOS-style dark glass code cards with syntax highlighting, window controls, and line numbers.",
                engineType = ToolEngineType.NATIVE_CANVAS_CARBON,
                inputs = listOf(
                    ToolInputField("title", "Window Filename", "JulianEngine.kt", "JulianOmniCore.kt"),
                    ToolInputField("code", "Source Code Snippet", "Paste code...", "suspend fun executeOmniCore(toolId: String) = withTimeout(60_000L) {\n    val model = Firebase.ai.generativeModel(\"gemini-4-argon\")\n    model.generateContent(\"Activate Julian v9.0 // \$toolId\")\n}", isMultiline = true)
                )
            )
        )
        add(
            JulianTool(
                id = "maker_qc_chat",
                commandName = "maker qc",
                title = "Quoted Chat Bubble Sticker Maker",
                category = ToolCategory.MAKER,
                endpointPath = "/api/maker/qc",
                httpMethod = "POST",
                description = "Renders sleek dark-mode chat quote cards with verified badge, avatar monogram, and timestamp.",
                engineType = ToolEngineType.NATIVE_CANVAS_BADGE,
                inputs = listOf(
                    ToolInputField("sender", "Sender Name", "Julian Architect", "Julian Architect ✓"),
                    ToolInputField("message", "Quote Message", "Type chat message...", "Zero static mocks. 85 real interactive endpoints running at 60fps."),
                    ToolInputField("accent", "Name Color", "Gold or Cyan", "Imperial Gold", listOf("Imperial Gold", "Quantum Cyan", "Emerald"))
                )
            )
        )
        add(
            JulianTool(
                id = "maker_attp",
                commandName = "maker attp",
                title = "Neon Cyber Glitch Banner Maker",
                category = ToolCategory.MAKER,
                endpointPath = "/api/maker/attp",
                httpMethod = "POST",
                description = "Creates high-voltage cyberpunk neon typography banners with chromatic glow layers.",
                engineType = ToolEngineType.NATIVE_CANVAS_BADGE,
                inputs = listOf(
                    ToolInputField("sender", "Top Tagline", "OMNI-CORE // 09", "OMNI-CORE // 09"),
                    ToolInputField("message", "Main Neon Headline", "CYBERPUNK LUXURY", "HYPER-LUXURY API"),
                    ToolInputField("accent", "Glow Spectrum", "Choose neon", "Quantum Cyan", listOf("Quantum Cyan", "Imperial Gold", "Emerald"))
                )
            )
        )
        add(
            JulianTool(
                id = "maker_ktp_pass",
                commandName = "maker idpass",
                title = "Julian VIP Developer Pass Generator",
                category = ToolCategory.MAKER,
                endpointPath = "/api/maker/idpass",
                httpMethod = "POST",
                description = "Generates an Obsidian Gold holographic developer credential badge with clearance level and UID hash.",
                engineType = ToolEngineType.NATIVE_CANVAS_BADGE,
                inputs = listOf(
                    ToolInputField("sender", "Operative Name & Role", "Rizky // Principal Architect", "RIZKY // PRINCIPAL ARCHITECT"),
                    ToolInputField("message", "Clearance & Division", "LEVEL 9 OMNI-ACCESS", "CLEARANCE: LEVEL-9 OMNI-CORE // SEC-OPS"),
                    ToolInputField("accent", "Badge Trim", "Select trim", "Imperial Gold", listOf("Imperial Gold", "Quantum Cyan", "Emerald"))
                )
            )
        )
        add(
            JulianTool(
                id = "maker_tweet_card",
                commandName = "maker tweet",
                title = "Social Post Metric Card Studio",
                category = ToolCategory.MAKER,
                endpointPath = "/api/maker/tweet",
                httpMethod = "POST",
                description = "Synthesizes a clean social broadcast card with engagement counters and verified telemetry.",
                engineType = ToolEngineType.NATIVE_CANVAS_BADGE,
                inputs = listOf(
                    ToolInputField("sender", "Handle", "@julian_omnicore", "@julian_omnicore"),
                    ToolInputField("message", "Post Copy", "Type post...", "Just shipped JULIAN // OMNI-CORE v9.0 on Android with 85 hybrid REST & Maker tools."),
                    ToolInputField("accent", "Highlight", "Select accent", "Quantum Cyan", listOf("Quantum Cyan", "Imperial Gold", "Emerald"))
                )
            )
        )
        add(
            JulianTool(
                id = "maker_fake_notif",
                commandName = "maker notif",
                title = "System Telemetry Alert Card",
                category = ToolCategory.MAKER,
                endpointPath = "/api/maker/notif",
                httpMethod = "POST",
                description = "Renders an enterprise security or deployment alert card with severity badge.",
                engineType = ToolEngineType.NATIVE_CANVAS_BADGE,
                inputs = listOf(
                    ToolInputField("sender", "Subsystem Header", "PRODUCTION CLUSTER // ASIA-SE1", "PRODUCTION CLUSTER // ASIA-SE1"),
                    ToolInputField("message", "Alert Payload", "All 85 nodes healthy", "HTTP 200 OK — Zero-trust Firestore rules verified in 14ms."),
                    ToolInputField("accent", "Status Color", "Select color", "Emerald", listOf("Emerald", "Imperial Gold", "Quantum Cyan"))
                )
            )
        )

        // ==========================================
        // 5. MUSIC CATEGORY (5 Tools)
        // ==========================================
        val musicTools = listOf(
            Triple("music_lirik", "music lirik", "Synchronized Lyrics & Chord Analyzer" to "/api/music/lirik"),
            Triple("music_chord", "music chord", "Guitar & Piano Voicing Transposer" to "/api/music/chord"),
            Triple("music_shazam", "music recognize", "Acoustic Fingerprint & Track Identifier" to "/api/music/recognize"),
            Triple("music_spotify_chart", "music topchart", "Global Viral 50 Audio Telemetry" to "/api/music/topchart"),
            Triple("music_bpm_key", "music bpmkey", "Harmonic Camelot Key & BPM Detector" to "/api/music/bpmkey")
        )
        musicTools.forEach { (id, cmd, meta) ->
            add(
                JulianTool(
                    id = id,
                    commandName = cmd,
                    title = meta.first,
                    category = ToolCategory.MUSIC,
                    endpointPath = meta.second,
                    httpMethod = "GET",
                    description = "Executes musicological analysis, chord progression mapping, and track metadata lookup via ${meta.second}.",
                    engineType = ToolEngineType.GEMINI_FLASH,
                    inputs = listOf(
                        ToolInputField("query", "Track Title, Artist, or Progression", "Enter song or artist...", "Daft Punk - Veridis Quo"),
                        ToolInputField("keyShift", "Transpose / Mode", "Optional semitone shift", "Original Key (0)", listOf("Original Key (0)", "+2 Semitones", "-2 Semitones", "Camelot DJ Notation"))
                    )
                )
            )
        }

        // ==========================================
        // 6. RANDOM CATEGORY (6 Tools)
        // ==========================================
        val randomTools = listOf(
            Triple("random_quotes", "random quotes", "Philosophical & Cybernetic Aphorism Feed" to "/api/random/quotes"),
            Triple("random_fakta", "random fakta", "Verified Scientific & Cosmic Anomaly Fact" to "/api/random/fakta"),
            Triple("random_motivasi", "random motivasi", "High-Performance Stoic Briefing" to "/api/random/motivasi"),
            Triple("random_pantun", "random pantun", "Classical Nusantara Pantun Generator" to "/api/random/pantun"),
            Triple("random_tebakan", "random tebakan", "Logic Riddle & Brainteaser Challenge" to "/api/random/tebakan"),
            Triple("random_bioscience_trivia", "random biofact", "Molecular Biology & Genetics Discovery" to "/api/random/biofact")
        )
        randomTools.forEach { (id, cmd, meta) ->
            add(
                JulianTool(
                    id = id,
                    commandName = cmd,
                    title = meta.first,
                    category = ToolCategory.RANDOM,
                    endpointPath = meta.second,
                    httpMethod = "GET",
                    description = "Streams curated high-signal ${meta.first.lowercase()} payloads with seed reproducibility.",
                    engineType = ToolEngineType.GEMINI_FLASH_LITE,
                    inputs = listOf(
                        ToolInputField("theme", "Sub-Theme / Seed", "Enter optional theme or seed...", "Quantum Computing & Human Potential"),
                        ToolInputField("lang", "Language", "Select language", "Indonesian & English", listOf("Indonesian & English", "English Only", "Bahasa Indonesia"))
                    )
                )
            )
        }

        // ==========================================
        // 7. SEARCH CATEGORY (8 Tools — Search & Maps Grounded)
        // ==========================================
        add(
            JulianTool(
                id = "search_google_grounded",
                commandName = "search google",
                title = "Google Search Grounded Intelligence",
                category = ToolCategory.SEARCH,
                endpointPath = "/api/search/google",
                httpMethod = "GET",
                description = "Live web-grounded synthesis powered by Gemini 3.8 Flash with verified fact extraction.",
                engineType = ToolEngineType.GEMINI_SEARCH_GROUNDED,
                inputs = listOf(
                    ToolInputField("query", "Search Query", "Enter live research topic...", "Latest Android 16 Jetpack Compose adaptive layout improvements 2026")
                )
            )
        )
        add(
            JulianTool(
                id = "search_maps_grounded",
                commandName = "search maps",
                title = "Google Maps Grounded Geo-Locator",
                category = ToolCategory.SEARCH,
                endpointPath = "/api/search/maps",
                httpMethod = "GET",
                description = "Geospatial venue, laboratory, hospital, and transit discovery with coordinates and routing notes.",
                engineType = ToolEngineType.GEMINI_MAPS_GROUNDED,
                inputs = listOf(
                    ToolInputField("locationQuery", "Place or Coordinate Query", "Search venues or landmarks...", "Top biotech research institutes and specialty coffee roasters near SCBD Jakarta")
                )
            )
        )
        val extraSearchTools = listOf(
            Triple("search_wikipedia", "search wiki", "Wikipedia Encyclopedic Deep Digest" to "/api/search/wiki"),
            Triple("search_youtube", "search ytsearch", "YouTube Technical Lecture & Channel Finder" to "/api/search/ytsearch"),
            Triple("search_pinterest", "search pinsearch", "Visual Moodboard & Design Reference Search" to "/api/search/pinsearch"),
            Triple("search_playstore", "search playstore", "Google Play App & Category Intelligence" to "/api/search/playstore"),
            Triple("search_pubmed", "search pubmed", "PubMed & Biomedical Literature Search" to "/api/search/pubmed"),
            Triple("search_arxiv", "search arxiv", "arXiv Preprint & Quantitative Paper Search" to "/api/search/arxiv")
        )
        extraSearchTools.forEach { (id, cmd, meta) ->
            add(
                JulianTool(
                    id = id,
                    commandName = cmd,
                    title = meta.first,
                    category = ToolCategory.SEARCH,
                    endpointPath = meta.second,
                    httpMethod = "GET",
                    description = "Executes indexed semantic search across ${meta.first} with structured results.",
                    engineType = ToolEngineType.GEMINI_SEARCH_GROUNDED,
                    inputs = listOf(
                        ToolInputField("query", "Search Keywords", "Enter search query...", "Transformer attention flash-decoding optimization")
                    )
                )
            )
        }

        // ==========================================
        // 8. TOOLS CATEGORY (17 Tools — Including NIK Parser, TTS, Device Compare, Crypto)
        // ==========================================
        add(
            JulianTool(
                id = "tools_nikcheck",
                commandName = "tools nikcheck",
                title = "16-Digit Indonesian NIK Demographic Parser",
                category = ToolCategory.TOOLS,
                endpointPath = "/api/tools/nikcheck",
                httpMethod = "POST",
                description = "Deterministic 16-digit NIK parser extracting Province, Regency/City code, District, Birth Date, Gender (female +40 rule), Zodiac, Age, and Serial Sequence.",
                engineType = ToolEngineType.NATIVE_NIK_PARSER,
                inputs = listOf(
                    ToolInputField("nik", "16-Digit NIK Number", "Enter 16-digit NIK...", "3174041708980005")
                )
            )
        )
        add(
            JulianTool(
                id = "tools_tts",
                commandName = "tools tts",
                title = "Neural Text-to-Speech Synthesizer",
                category = ToolCategory.TOOLS,
                endpointPath = "/api/tools/tts",
                httpMethod = "POST",
                description = "Synthesizes spoken audio on-device using the native Android TextToSpeech engine with pitch and speech-rate controls.",
                engineType = ToolEngineType.NATIVE_TTS,
                inputs = listOf(
                    ToolInputField("text", "Speech Script", "Enter text to speak aloud...", "Welcome to Julian OmniCore version nine point zero. All eighty five modules are online."),
                    ToolInputField("pitch", "Voice Pitch", "0.8 - 1.4", "1.0", listOf("0.85", "1.0", "1.15", "1.3")),
                    ToolInputField("rate", "Speech Rate", "0.8 - 1.5", "1.0", listOf("0.8", "1.0", "1.2", "1.4"))
                )
            )
        )
        add(
            JulianTool(
                id = "tools_device_compare",
                commandName = "tools compare",
                title = "Flagship Device vs. Device Comparator",
                category = ToolCategory.TOOLS,
                endpointPath = "/api/tools/compare",
                httpMethod = "POST",
                description = "Side-by-side architectural comparison of two smartphones, GPUs, or SoCs across geekbench, thermals, and optics.",
                engineType = ToolEngineType.GEMINI_FLASH,
                inputs = listOf(
                    ToolInputField("deviceA", "Primary Device / SoC", "e.g. Pixel 10 Pro", "Google Pixel 10 Pro"),
                    ToolInputField("deviceB", "Comparison Device / SoC", "e.g. Galaxy S26 Ultra", "Samsung Galaxy S26 Ultra")
                )
            )
        )
        val utilityTools = listOf(
            Triple("tools_base64", "tools base64", "Base64 / URL-Safe Encoder & Decoder" to "/api/tools/base64"),
            Triple("tools_hash", "tools sha256", "SHA-256 / SHA-512 / MD5 Cryptographic Hasher" to "/api/tools/sha256"),
            Triple("tools_jwt_decode", "tools jwt", "JWT Header & Claims Inspector" to "/api/tools/jwt"),
            Triple("tools_uuid_ulid", "tools uuid", "UUIDv4 & Lexicographical Token Generator" to "/api/tools/uuid"),
            Triple("tools_json_pretty", "tools jsonfmt", "Strict JSON Validator & Minifier" to "/api/tools/jsonfmt"),
            Triple("tools_url_codec", "tools urlcodec", "URI Component Parser & Query Encoder" to "/api/tools/urlcodec"),
            Triple("tools_password_entropy", "tools passgen", "High-Entropy Cryptographic Key Generator" to "/api/tools/passgen"),
            Triple("tools_cron_parser", "tools cron", "Cron Schedule Expression Evaluator" to "/api/tools/cron"),
            Triple("tools_color_conv", "tools colorhex", "HEX / RGB / HSL / Compose Color Converter" to "/api/tools/colorhex"),
            Triple("tools_timestamp", "tools epoch", "Unix Epoch & ISO-8601 Temporal Converter" to "/api/tools/epoch"),
            Triple("tools_dns_lookup", "tools dns", "DNS A/AAAA/MX/TXT Record Inspector" to "/api/tools/dns"),
            Triple("tools_http_headers", "tools headers", "HTTP Security Header & CSP Auditor" to "/api/tools/headers"),
            Triple("tools_molarity_calc", "tools molarity", "Bioscience Molarity & Dilution Calculator" to "/api/tools/molarity"),
            Triple("tools_chmod_calc", "tools chmod", "POSIX Octal & Symbolic Permission Matrix" to "/api/tools/chmod")
        )
        utilityTools.forEach { (id, cmd, meta) ->
            val defaultVal = when (id) {
                "tools_jwt_decode" -> "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdWxpYW4tdjkiLCJyb2xlIjoiarchitectIiwiaWF0IjoxNzYwNjg0NDAwfQ.signature"
                "tools_json_pretty" -> "{\"system\":\"JULIAN\",\"version\":9.0,\"modules\":85,\"status\":\"ONLINE\"}"
                "tools_color_conv" -> "#E5B842"
                "tools_molarity_calc" -> "MW=58.44, Mass_g=2.922, Volume_L=0.5"
                "tools_chmod_calc" -> "0755"
                else -> "JULIAN-OMNI-CORE-V9.0-PAYLOAD"
            }
            add(
                JulianTool(
                    id = id,
                    commandName = cmd,
                    title = meta.first,
                    category = ToolCategory.TOOLS,
                    endpointPath = meta.second,
                    httpMethod = "POST",
                    description = "Executes deterministic ${meta.first} computation with zero-latency client-side verification.",
                    engineType = ToolEngineType.NATIVE_CRYPTO_UTIL,
                    inputs = listOf(
                        ToolInputField("input", "Input Payload", "Enter input string or parameters...", defaultVal),
                        ToolInputField("mode", "Operation Mode", "Select mode", "Encode / Analyze", listOf("Encode / Analyze", "Decode / Verify", "Verbose Telemetry"))
                    )
                )
            )
        }

        // ==========================================
        // 9. UPLOAD CATEGORY (15 Tools — Media Picker & Cloud Storage Inspectors)
        // ==========================================
        val uploadTools = listOf(
            Triple("upload_telegraph", "upload telegraph", "Telegraph Instant Media Host Bridge" to "/api/upload/telegraph"),
            Triple("upload_catbox", "upload catbox", "Catbox.moe Permanent Asset Uploader" to "/api/upload/catbox"),
            Triple("upload_pomf2", "upload pomf2", "Pomf2 High-Speed Binary CDN" to "/api/upload/pomf2"),
            Triple("upload_uguu", "upload uguu", "Uguu Ephemeral 48h File Drop" to "/api/upload/uguu"),
            Triple("upload_quax", "upload quax", "Qu.ax Encrypted Media Vault" to "/api/upload/quax"),
            Triple("upload_imgbb", "upload imgbb", "ImgBB Lossless Image Hosting API" to "/api/upload/imgbb"),
            Triple("upload_pixeldrain", "upload pixeldrain", "Pixeldrain High-Bandwidth Object Store" to "/api/upload/pixeldrain"),
            Triple("upload_gofile", "upload gofile", "GoFile Multi-Region Edge Uploader" to "/api/upload/gofile"),
            Triple("upload_tmpfiles", "upload tmpfiles", "TmpFiles Automated Sandbox Bucket" to "/api/upload/tmpfiles"),
            Triple("upload_0x0", "upload 0x0", "0x0.st Minimalist CLI Artifact Host" to "/api/upload/0x0"),
            Triple("upload_litterbox", "upload litterbox", "Litterbox Time-Locked Media Host" to "/api/upload/litterbox"),
            Triple("upload_freeimage", "upload freeimage", "FreeImage.host EXIF-Stripped CDN" to "/api/upload/freeimage"),
            Triple("upload_paste_rs", "upload pasters", "Paste.rs Cryptographic Code Pastebin" to "/api/upload/pasters"),
            Triple("upload_github_gist", "upload gist", "GitHub Gist Snippet Publisher" to "/api/upload/gist"),
            Triple("upload_ipfs_pin", "upload ipfs", "IPFS Content-Addressed CIDv1 Pinner" to "/api/upload/ipfs")
        )
        uploadTools.forEach { (id, cmd, meta) ->
            add(
                JulianTool(
                    id = id,
                    commandName = cmd,
                    title = meta.first,
                    category = ToolCategory.UPLOAD,
                    endpointPath = meta.second,
                    httpMethod = "POST",
                    description = "Supports zero-permission Android Photo Picker selection, SHA-256 checksum calculation, MIME inspection, and multipart upload dispatch to ${meta.second}.",
                    engineType = ToolEngineType.NATIVE_UPLOAD_INSPECTOR,
                    inputs = listOf(
                        ToolInputField("fileName", "Asset Filename", "julian_asset.png", "julian_artifact_${id.removePrefix("upload_")}.png"),
                        ToolInputField("retention", "Retention Policy", "Select TTL", "Permanent / Pinned", listOf("Permanent / Pinned", "72 Hours", "24 Hours", "1 Hour Burn"))
                    )
                )
            )
        }
    }

    fun getByCategory(category: ToolCategory?): List<JulianTool> {
        if (category == null) return allTools
        return allTools.filter { it.category == category }
    }

    fun getCategoryCounts(): Map<ToolCategory, Int> {
        return ToolCategory.entries.associateWith { cat ->
            allTools.count { it.category == cat }
        }
    }
}
