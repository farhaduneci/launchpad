# Repository Guidelines

## Project Overview

**Launchpad** is a minimalist, text-only Android home screen replacement. It surfaces up to 10 fixed tap slots (no icons, no search), a secondary "Chest" for rarely-used apps, and a full alphabetical app list on swipe-up. No Google APIs, no tracking, no internet permission. Single-module Kotlin/Jetpack Compose app targeting Android 9+ (minSdk 28). MIT licensed.

---

## Architecture & Data Flow

**Pattern**: MVVM — flat, no Clean Architecture layers (no Use Cases).

```
MainActivity
  └─ LaunchpadApp (root Composable, gesture handler, screen router)
       ├─ HomeScreen           (clock / date / battery / 6 invisible tap zones)
       ├─ AppDrawerScreen      (scrollable app list, rename, chest)
       ├─ SettingsScreen       (all user preferences)
       ├─ AppPickerDialog      (slot assignment)
       └─ ColorPickerDialog    (custom HSV Canvas picker)

LauncherViewModel (AndroidViewModel)
  ├─ MutableStateFlow<T> for every setting and UI state
  ├─ MutableSharedFlow<LauncherEvent> for one-shot events (CloseDrawer)
  ├─ BroadcastReceiver — battery level (ACTION_BATTERY_CHANGED)
  ├─ BroadcastReceiver — package changes (ADDED / REMOVED / REPLACED)
  ├─ coroutine clock loop — ticks every 10 s via viewModelScope
  └─ PrefsRepository (constructed directly, no DI)

PrefsRepository
  └─ SharedPreferences("launcher_prefs") — all settings, slot→pkg map, chest set, renames
```

**Data flow**: UI calls ViewModel methods → ViewModel updates StateFlow → Composables collect via `collectAsState()` → recomposition. One-shot events travel through `SharedFlow` collected in `LaunchedEffect`.

**Navigation**: manual boolean flags (`isDrawerOpen`, `showSettings`, `showPickerSlot: String?`) in `LiteralLauncherApp.kt`. Screen presence controlled with `AnimatedVisibility`. No Jetpack Navigation, no Fragments, no NavHost.

---

## Key Directories

```
app/src/main/java/fumi/day/launchpad/
  ├─ MainActivity.kt               entry point (ComponentActivity)
  ├─ ui/
  │    ├─ LiteralLauncherApp.kt    root Composable, gesture + routing
  │    ├─ LauncherViewModel.kt     single ViewModel, all app state
  │    ├─ screen/
  │    │    ├─ HomeScreen.kt
  │    │    ├─ AppDrawerScreen.kt
  │    │    └─ SettingsScreen.kt
  │    └─ component/
  │         ├─ AppPickerDialog.kt
  │         └─ ColorPickerDialog.kt
  ├─ data/
  │    ├─ PrefsRepository.kt       SharedPreferences CRUD
  │    └─ PrefsKeys.kt             all pref key constants + ALL_SLOT_IDS list
  └─ util/
       ├─ AppLauncher.kt           launchMuDirect(), expandNotifications()
       └─ Extensions.kt           findActivity(), parseColor(), colorToHex(), safeLower(), toTitleCase()

app/src/main/AndroidManifest.xml
gradle/libs.versions.toml          central version catalog
app/build.gradle.kts               all SDK levels, signing, R8, dependencies
```

---

## Development Commands

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing keys in local.properties)
./gradlew assembleRelease

# Host-side unit tests (no device needed)
./gradlew :app:testDebugUnitTest

# Instrumented tests (requires connected device/emulator)
./gradlew :app:connectedAndroidTest
```

**Signing**: release signing keys must be in `local.properties` (not committed). No signing config = assembleRelease will fail.

---

## Code Conventions & Common Patterns

**Naming**
- `PascalCase` — classes, objects, Composables
- `camelCase` — functions, properties
- `SCREAMING_SNAKE_CASE` — top-level constants (`SLOT_CLOCK`, `PREF_BG_COLOR`)
- `_` prefix for private backing flows: `private val _currentTime = MutableStateFlow(…)` exposed as `val currentTime = _currentTime.asStateFlow()`

**Composables**
- Screens in `ui/screen/`, reusable dialogs in `ui/component/`
- One public declaration per file, file named after it
- `HomeScreen` is stateless (all state passed in); `AppDrawerScreen` / `SettingsScreen` collect from ViewModel directly
- `LiteralLauncherApp.kt` is the only place local `mutableStateOf` UI state lives

**Async**
- Kotlin Coroutines + `StateFlow` / `SharedFlow` exclusively — no RxJava, no callbacks
- `viewModelScope.launch` for ViewModel coroutines
- `LaunchedEffect(Unit)` in Composables to collect SharedFlow events

**Text helpers** (all in `util/Extensions.kt`)
- `safeLower(text)` — Locale-aware lowercase; used for sorting and UI chrome labels ("chest", "done", etc.)
- `toTitleCase(text)` — capitalises first letter of each word; used for all **app name** display sites
- Apply `toTitleCase` to any new site that renders an app name; apply `safeLower` to UI labels

**Sizing / Layout**
- All sizes are derived: `screenW * fraction * globalScale` — never hardcoded dp
- `globalScale` is a user-controlled multiplier stored in SharedPreferences

**Theming**
- `Theme.Launchpad` in `res/values/themes.xml` is the XML theme (window bg, status/nav bar colors)
- There is no Compose `MaterialTheme` wrapper at the root — colors come from user preferences via `parseColor(hex)` on the ViewModel
- `MaterialTheme.typography.*` is used inside `SettingsScreen`; `MaterialTheme.colorScheme` is not

**DI**: none. `ViewModelProvider(this)[LauncherViewModel::class.java]` in MainActivity; `PrefsRepository(app)` constructed directly in ViewModel. Do not introduce Hilt/Koin.

**Error handling**
- Non-critical paths use silent catch: `catch (_: Exception) {}` (notification expansion, activity transitions)

---

## Important Files

| File | Role |
|---|---|
| `app/src/main/java/fumi/day/launchpad/MainActivity.kt` | Entry point; hosts Compose, hides system bars |
| `app/src/main/java/fumi/day/launchpad/ui/LiteralLauncherApp.kt` | Root Composable; all screen routing and gesture logic |
| `app/src/main/java/fumi/day/launchpad/ui/LauncherViewModel.kt` | Single ViewModel; all StateFlow state; BroadcastReceivers |
| `app/src/main/java/fumi/day/launchpad/data/PrefsKeys.kt` | Domain constants; slot IDs (`tl ml bl tr mr br double clock date battery`) |
| `app/src/main/java/fumi/day/launchpad/data/PrefsRepository.kt` | SharedPreferences CRUD layer |
| `app/src/main/java/fumi/day/launchpad/util/Extensions.kt` | `safeLower()`, `toTitleCase()`, `parseColor()`, `colorToHex()`, `findActivity()` |
| `app/src/main/AndroidManifest.xml` | Two intent-filters: HOME+DEFAULT (launcher) and LAUNCHER (regular app lists) |
| `gradle/libs.versions.toml` | All dependency versions (single source of truth) |
| `app/build.gradle.kts` | SDK levels, R8, signing, dependency declarations |
| `app/proguard-rules.pro` | Strips `Log.*` calls in release; suppress kotlin.reflect warnings |

---

## Runtime / Tooling Preferences

- **Build tool**: Gradle 9.3.1 (wrapper pinned with SHA-256 checksum)
- **AGP**: 9.1.0
- **Kotlin**: 2.3.10
- **Compose BOM**: 2026.03.00
- **JVM heap**: 2048m (`org.gradle.jvmargs` in `gradle.properties`)
- **Kotlin style**: `official` (`kotlin.code.style=official`)
- **Kotlin compiler plugins**: only `org.jetbrains.kotlin.plugin.compose` — no kapt, no KSP
- **minSdk 28** — `java.time.*` usable directly without core library desugaring
- **Package**: `com.farhaduneci.launchpad`
- **applicationId**: `com.farhaduneci.launchpad`

---

## Testing & QA

**Current state**: only Android Studio scaffold tests exist — no application logic is covered.

| Suite | Runner | Command |
|---|---|---|
| Unit (host JVM) | JUnit 4 | `./gradlew :app:testDebugUnitTest` |
| Instrumented (device) | AndroidJUnit4 | `./gradlew :app:connectedAndroidTest` |

**Frameworks wired in** (dependencies present):
- JUnit 4, `androidx.test.ext:junit`, Espresso Core, `androidx.compose.ui:ui-test-junit4`

No coverage tooling (JaCoCo/Kover absent).

New tests go in:
- `app/src/test/java/fumi/day/launchpad/` — host JVM (pure logic)
- `app/src/androidTest/java/fumi/day/launchpad/` — on-device (Compose UI, integration)
