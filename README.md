# NR Toggle — Quick Settings Tile (HyperOS)

A rooted Quick Settings tile that toggles between **5G NR Only** and **LTE/Auto** on **HyperOS (MIUI 14+)**.

- **Root is mandatory** — the app will not function without su access
- No launcher icon — invisible in the app drawer, only accessible via QS panel
- Uses `cmd phone set-allowed-network-types-for-users` (HyperOS-native, immediate effect)
- No airplane mode toggle required — no bluetooth side effects
- Tiny APK — zero external dependencies, pure Android SDK

---

## Requirements

- **Root access** (KernelSU / Magisk) — the app calls `su -c` internally
- **HyperOS** (MIUI 14 / HyperOS 1.0+) — tested on **HyperOS 1.0, Poco F3 / Redmi K40**
- Android 13+ (API 33) for `cmd phone` support
- Android Studio Hedgehog (2023.1) or later
- JDK 17
- Gradle 8.2+

## Build

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

No launcher icon appears. To add the tile:
1. Pull down notification shade → tap the **pencil/edit** icon
2. Scroll to find **"NR Toggle"** tile
3. Drag it to your active tiles
4. Tap to toggle — grant root when prompted by KernelSU/Magisk

---

## How It Works

### The Command That Works on HyperOS

On HyperOS the old methods (`settings put global preferred_network_mode`, `service call phone 75`) are effectively **no-ops** — they exit 0 but don't change the radio.

The working command:

```
cmd phone set-allowed-network-types-for-users -s <subId> <20-char binary bitmask>
```

| Mode | Bitmask | Description |
|------|---------|-------------|
| **NR Only** | `10000000000000000000` | 5G Standalone — enables only NR bit (19) |
| **LTE/Auto** | `01001111101111111111` | Enables GSM/WCDMA/LTE, disables NR |

The app sends this to both subIds (0 and 1) for dual-SIM support.

### Lifecycle

1. Tap tile → `onClick()` spawns a background thread
2. `runAsRoot()` executes the `cmd phone` command via `su -c`
3. On failure → command silently fails (no UI feedback possible)
4. On success → waits 3s, reads back `get-allowed-network-types-for-users` for verification
5. Updates tile icon (NR / LTE) and label

### Icons

Both icons are hand-drawn stroke-based vector drawables (24×24 viewport, strokeWidth=2) — no text rendering, pure paths for "LTE" and "NR" letters.

---

## Known Limitations

- **HyperOS only** — will not work on AOSP / LineageOS (different radio HAL)
- Root access is **required** — app silently fails if root is missing/denied
- Dynamic network type detection is not implemented (no `TelephonyManager` listening)

---

## Project Structure

```
NRToggle/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/hrishi/nrtoggle/
│       │   └── NRToggleTileService.kt
│       └── res/
│           ├── drawable/
│           │   ├── ic_tile.xml
│           │   ├── ic_5g_nr.xml
│           │   └── ic_lte_auto.xml
│           └── values/strings.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```
