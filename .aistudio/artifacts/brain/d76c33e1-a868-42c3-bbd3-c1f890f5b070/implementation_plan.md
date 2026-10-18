# JULIAN // OMNI-CORE v9.0 — Hyper-Luxury API Playground & Visual App Studio

JULIAN // OMNI-CORE v9.0 is an ultra-luxurious, futuristic Android studio and interactive API playground that unifies **85 specialized tools across 9 categories** (`AI`, `DOWNLOAD`, `INFO`, `MAKER`, `MUSIC`, `RANDOM`, `SEARCH`, `TOOLS`, `UPLOAD`) with a hybrid execution engine combining native on-device visual/utility engines, Firebase AI Logic (Gemini 3.8 Flash, Gemini 4 Argon with High Thinking, and Gemini 3.5 Flash-Lite), and a configurable live REST API client backed by Google Sign-In and Cloud Firestore.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and product choices reflect your confirmed preferences from our clarification phase:

- **Confirmed Decision 1 — Primary Focus on the 85 REST API & Maker Tools**: The studio centers on the complete 85-tool catalog across 9 categories (`AI`, `DOWNLOAD`, `INFO`, `MAKER`, `MUSIC`, `RANDOM`, `SEARCH`, `TOOLS`, `UPLOAD`), with specialized interactive workspaces for visual Maker tools, Bioscience/Info analysis modules, and a dedicated multi-turn Omni-Core Gemini AI Chat drawer.
- **Confirmed Decision 2 — Hybrid Execution Engine (Native + Gemini AI + Custom REST URL)**:
  - **Native Client-Side Engines**: Real Android `Canvas` bitmap rendering and interactive generators for `maker brat`, `maker struk` (thermal receipt designer), `maker kalender`, `maker qrcode` (QR matrix encoder), `maker carbon` (code snippet studio), `tools nikcheck` (16-digit Indonesian NIK parser), `tools tts` (native Android `TextToSpeech`), currency conversion, and drag/pick file upload inspection via the zero-permission Android Photo Picker.
  - **Gemini AI & Grounding Engine**: Powers all `AI`, `SEARCH`, `INFO`, and Bioscience research modules via Firebase AI Logic (`gemini-3.8-flash` for general/grounded tasks, `gemini-4-argon` with `ThinkingLevel.HIGH` for complex reasoning and bioscience synthesis, and `gemini-3.5-flash-lite` for low-latency utility tasks).
  - **Live Custom REST API Client**: Configurable Base URL and API Key header injection via OkHttp/Retrofit so any of the 85 endpoints can also dispatch live HTTP requests to a custom Julian REST server with full JSON payload inspection and latency telemetry.
- **Confirmed Decision 3 — Cloud Persistence via Google Sign-In & Firestore**: User authentication uses Jetpack `CredentialManager` (`GetSignInWithGoogleOption`) and syncs bookmarked/favorite tools and custom REST API endpoint configurations (`baseUrl`, `apiKeyHeader`, preferred execution mode) to Cloud Firestore in real time.

---

## 1. Overview & Core Concept

- **What It Does**: Provides developers, creators, and researchers with a single high-density command center to search, filter, configure, and execute 85 API and creative tools—ranging from instant visual canvas generators (Brat typography, receipts, QR codes, Carbon code cards) and 16-digit NIK demographic parsers to Gemini-powered intelligence, Search/Maps-grounded lookups, Bioscience calculators, and live REST endpoint testing.
- **Target Audience / Persona**: Power users, mobile developers, digital creators, and researchers who want both an instant visual tool studio and a configurable REST API workbench on Android.
- **Key Value**: Eliminates static API documentation by turning every endpoint into a live, interactive studio with tailored form controls, real-time visual previews, and cloud-synced bookmarks and server settings.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Authentication & Session Boot**: Users sign in with Google via Jetpack `CredentialManager` on the **JULIAN // OMNI-CORE** gateway screen. Upon authentication, their cloud-synced bookmarked tools and custom REST server settings load automatically from Firestore.
2. **Command Header & Telemetry Bar**: Displays the `JULIAN // OMNI-CORE v9.0` crest, live system telemetry (active mode badge, response latency in `ms`, bookmarked count), quick-launch Command Palette (`⌘K` / search trigger), view mode toggle (Grid Cards vs. Compact Telemetry Table), and the **API Endpoint Configurator** modal trigger.
3. **9-Category Filtering & Discovery**: Horizontal interactive filter rail with live count badges for `ALL (85)`, `BOOKMARKS`, `AI`, `DOWNLOAD`, `INFO`, `MAKER`, `MUSIC`, `RANDOM`, `SEARCH`, `TOOLS`, and `UPLOAD`.
4. **Interactive Tool Execution Studio (Bottom Sheet / Full-Screen Workspace)**: Tapping any of the 85 tools opens a tailored workspace with:
   - Custom parameter inputs (text prompts, color pickers, receipt line items, 16-digit NIK input, media picker, or REST query params).
   - Execution mode selector (**Hybrid Auto**, **Native / Gemini Studio**, or **Live Custom REST URL**).
   - Visual output canvas (rendered Bitmaps for Maker tools, audio/TTS controls, structured cards for NIK/Bioscience/Search/Maps results, and syntax-highlighted raw JSON inspector with copy/share actions).
5. **Multi-Turn Omni-Core AI & Bioscience Assistant**: Accessible via a persistent floating action pill or dedicated tab, offering multi-turn conversational threads with model selection (`gemini-3.8-flash`, `gemini-4-argon` with High Thinking, `gemini-3.5-flash-lite`) and Search/Maps grounding indicators.

### Visual Identity & Theme
- **Aesthetic Direction**: **Obsidian Gold & Quantum Cyan — Hyper-Luxury Cyberpunk Enterprise**. Deep layered obsidian surfaces with subtle animated particle/grid canvas backdrops, crisp 1dp specular borders, and high-contrast data typography.
- **Color Palette & Mood**:
  - `--background`: `#07090E` (Deep Obsidian Void)
  - `--surface`: `#0F131C` (Elevated Carbon Glass)
  - `--surface-variant`: `#171D2B` (Interactive Module Card Surface)
  - `--primary`: `#E5B842` (Imperial Obsidian Gold — primary accents, crests, active category badges)
  - `--secondary`: `#00F0FF` (Quantum Cyan — live telemetry, streaming states, active execution indicators)
  - `--tertiary`: `#10B981` (Emerald Telemetry — HTTP 200 OK & verified states)
  - `--error`: `#FF3B5C` (Crimson Alert — error boundaries & validation warnings)
- **Typography & Hierarchy**:
  - **Display / Headers**: `Orbitron` / `Space Grotesk` (bundled locally via `font-util`) for futuristic technical headers, category badges, and telemetry counters.
  - **Body / UI Controls**: `Plus Jakarta Sans` for crisp legibility across 85 tool descriptions and form labels.
  - **Code / JSON / Telemetry**: `JetBrains Mono` for endpoint paths (`/api/maker/brat`), latency readouts, NIK breakdowns, and raw JSON payloads.
- **Component Styling & Layout**: Adaptive Material 3 layout using `BoxWithConstraints` to switch seamlessly between single/dual-column grids on handhelds and multi-pane list-detail layouts on tablets/foldables, respecting `WindowInsets.safeDrawing` and `WindowInsets.navigationBars`.
- **Interactive Feedback & Motion**: Spring-driven card elevation, animated particle canvas background, live latency counter during execution, and instant toast/snackbar confirmation when bookmarking tools or copying payloads.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Hybrid Execution Architecture over Static Mocks**
  - *Chosen Approach*: Combine deterministic native Android engines (Canvas bitmap drawing for `brat`, `struk`, `kalender`, `qrcode`, `carbon`; native `TextToSpeech`; deterministic 16-digit NIK parsing; real hashing/encoding) with Firebase AI Logic (`gemini-3.8-flash` / `gemini-4-argon` / `gemini-3.5-flash-lite`) and a real OkHttp client targeting the user's configured REST Base URL.
  - *Why*: Guarantees every single one of the 85 tools produces authentic, useful output out-of-the-box while still functioning as a real REST API testing client when pointed at a custom Julian backend.
  - *Alternatives Considered*: Pure REST client only (would fail when the user does not have a live server URL running) or static mock JSON (violates real functionality expectations).
- **Decision 2: Scoped Cloud Persistence via Firestore**
  - *Chosen Approach*: Store user bookmarks (`users/{userId}/bookmarks/{toolId}`) and custom endpoint settings (`users/{userId}/settings/apiConfig`) in Cloud Firestore, gated behind Google Sign-In (`CredentialManager`).
  - *Why*: Directly fulfills your preference to sync bookmarked tools and custom API endpoint settings across devices with zero-trust per-user security rules.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram
```
┌────────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (Edge-to-Edge)                         │
│         configureAppCheck(this, intent) -> AuthGate (Google Sign-In)       │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │ Authenticated (uid)
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                     JulianOmniCoreScreen (Compose UI)                      │
│  ┌──────────────────────┐ ┌────────────────────────┐ ┌──────────────────┐  │
│  │ Telemetry & Command  │ │ 9-Category Filter Rail │ │ Multi-Turn AI &  │  │
│  │ Bar + URL ConfigDlg  │ │ + Grid / Table Toggle  │ │ Thinking Drawer  │  │
│  └──────────┬───────────┘ └───────────┬────────────┘ └────────┬─────────┘  │
│             └─────────────────────────┼───────────────────────┘            │
│                                       ▼                                    │
│             ┌─────────────────────────────────────────────────┐            │
│             │   Interactive Tool Execution Studio (Modal)     │            │
│             │  Canvas Preview │ Audio/TTS │ JSON Inspector    │            │
│             └─────────────────────────┬───────────────────────┘            │
└───────────────────────────────────────┼────────────────────────────────────┘
                                        │
                                        ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                        JulianViewModel (StateFlow)                         │
└───────┬───────────────────────────────┼────────────────────────────┬───────┘
        │                               │                            │
        ▼                               ▼                            ▼
┌──────────────────────┐   ┌─────────────────────────┐   ┌───────────────────┐
│ FirestoreRepository  │   │  HybridExecutionEngine  │   │ GeminiAiService   │
│ (R.string.firestore_ │   │ ┌─────────────────────┐ │   │ (Firebase.ai)     │
│  database_id)        │   │ │ Native Visual/Util  │ │   │ - gemini-3.8-flash│
│ - Bookmarks Flow     │   │ │ Canvas, QR, NIK, TTS│ │   │ - gemini-4-argon  │
│ - ApiConfig Doc      │   │ └─────────────────────┘ │   │   (Thinking HIGH) │
└──────────────────────┘   │ ┌─────────────────────┐ │   │ - gemini-3.5-lite │
                           │ │ OkHttp Dynamic REST │ │   │ - Search & Maps   │
                           │ │ Client (Custom URL) │ │   │   Grounding       │
                           │ └─────────────────────┘ │   └───────────────────┘
                           └─────────────────────────┘
```

### Data Model & State
- **Catalog Registry (`ToolCatalog`)**: Immutable registry of all **85 tools** across the **9 categories** (`AI` [10], `DOWNLOAD` [10], `INFO` [10], `MAKER` [10], `MUSIC` [6], `RANDOM` [8], `SEARCH` [9], `TOOLS` [17], `UPLOAD` [15] — with exact category counts matching the Julian specification), each defining its `id`, `name`, `category`, `endpointPath`, `httpMethod`, `description`, `inputFields`, and `engineType`.
- **Firestore Entities**:
  - `BookmarkedTool` (`users/{userId}/bookmarks/{toolId}`): `toolId: String`, `category: String`, `note: String`, `userId: String`, `createdAt: Timestamp?`, `updatedAt: Timestamp?`.
  - `UserApiConfig` (`users/{userId}/settings/apiConfig`): `baseUrl: String`, `apiKeyHeader: String`, `executionMode: String`, `enableHighThinking: Boolean`, `userId: String`, `createdAt: Timestamp?`, `updatedAt: Timestamp?`.

### Interactive Component & State Mapping
- **Global Search & Command Palette**: Instant reactive filtering across all 85 tool names, endpoint paths, and category tags, plus filter chips for Bookmarks and Categories.
- **View Mode Switcher**: Toggles between the **Luxury Bento Grid** (cards with category badges, endpoint paths, and quick-bookmark stars) and the **Telemetry Table View** (compact monospace rows showing method, path, engine type, and quick-launch button).
- **Tool Execution Studio**:
  - Validates required inputs before execution.
  - Renders native Android `Bitmap` outputs directly in Compose for `maker brat`, `maker struk`, `maker kalender`, `maker qrcode`, and `maker carbon`.
  - Executes 16-digit Indonesian NIK parsing (`tools nikcheck`) into Province, Regency/City code, Birth Date, Gender, and Serial Sequence.
  - Invokes `Firebase.ai` with streaming and structured output for AI, Bioscience, Search-grounded, and Maps-grounded tools, displaying both rich formatted cards and raw JSON telemetry.
