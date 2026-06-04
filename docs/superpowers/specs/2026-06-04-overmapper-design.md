# OverMapper — Design Spec
_2026-06-04_

## Overview

OverMapper is an Android hiking app. The USP is that every recorded walk is overlaid on top of all previous walks, building an interconnected web of trails across the map over time. No GPX import — the app records routes itself. Offline-first; all data stays on-device.

Developer: Northline (`xyz.northline.overmapper`)  
Store: Google Play — initial release to internal test track  
Brand: Northline / for home palette (bone, terracotta, sage, ink)

---

## Architecture

**Single-module MVVM.** One `:app` module. No multi-module complexity for a solo project.

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Map | MapLibre Android SDK + OpenFreeMap tiles |
| State | ViewModel + StateFlow |
| Storage | Room (trails, points, markers, photos) + DataStore (preferences) |
| DI | Hilt |
| GPS recording | Foreground `Service` + `FusedLocationProviderClient` |
| Min SDK | 26 |
| Target SDK | 35 |

**Edge-to-edge:** `enableEdgeToEdge()` from `androidx.activity:activity-ktx` called in `onCreate`. No `setStatusBarColor`, `setNavigationBarColor`, or `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES`. Insets handled via `WindowInsetsCompat` throughout.

---

## Features — Phase 1 (this spec)

### 1. Trail Recording

A foreground `Service` records GPS points from `FusedLocationProviderClient`. A persistent notification shows elapsed time and distance. Recording is started/stopped via a FAB on the map screen.

**Stationary filtering (two layers):**
- OS level: `minUpdateDistanceMeters = 8` on the `LocationRequest` — the OS does not wake the service until the user has moved at least 8 m.
- App level: secondary `Location.distanceTo(lastAcceptedPoint)` check discards GPS drift that passes the OS filter.

**Auto-pause state machine:**
```
RECORDING → PAUSED (no valid point for > 60 s) → RECORDING (next valid point)
```
Notification updates to "Paused — not moving" in the PAUSED state. Both thresholds are user-configurable.

**Segment breaks:** On `PAUSED → RECORDING` transition the service increments `segment_index` on subsequent `TrailPoint` rows. The map renderer lifts the pen between segments — no straight line drawn across a café stop.

Points are saved to Room incrementally so a crash loses at most one flush batch.

### 2. Trail Overlay

All recorded trails are rendered simultaneously on the map as MapLibre `GeoJsonSource` + `LineLayer` layers. Colour-coded by age:
- Terracotta `#B5562E` — most recent trail
- Sage `#7E9A86` — previous trails
- Trail-grey `#8C7F6E` — older trails

Line width tapers with age. The newest trail always renders on top. Trails are grouped by `segment_index` within a single GeoJSON `FeatureCollection`; MapLibre handles gap rendering natively via multi-segment LineStrings.

### 3. Elevation Gradient

Each trail segment is coloured by slope using the `altitude_m` delta between consecutive `TrailPoint` rows:
- Steep uphill — warm amber
- Flat — neutral grey
- Steep downhill — cool teal

Toggle-able globally in Settings (default on). When enabled, gradient overrides the age-based colour for that trail.

### 4. Condition Markers

Long-press anywhere on the map to drop a marker. Tap a recorded trail to place a marker on it. Marker types:
- Impassable (heavy rain)
- Overgrown
- Treacherous climb
- General note

Markers persist in Room, shown as icon pins on the map. Tap to view/edit body text.

`trail_id` is nullable — markers can exist independently of a specific recorded trail.

### 5. Trail Photos

Attach photos to a trail from camera or gallery. Stored as local content URIs in Room, rendered as thumbnail pins at their GPS coordinates. Tap to open full-screen. Photos are attached at point of capture (camera) or at the user's current location (gallery pick).

### 6. Export / Share

From Trail Detail:
- **Export GPX** — standard GPX 1.1 with OverMapper extensions for marker type and segment index. Shared via Android share sheet.
- **Share PNG** — screenshot of the current map view for that trail, shared via share sheet.

No backend, no link sharing in Phase 1.

### 7. Calorie Estimate

Calculated on trail completion:
```
Calories ≈ MET × weight_kg × duration_hours
```
MET is derived from average pace and elevation gain per km (baseline 5.5, scaled up for steeper/faster). Displayed with a `~` prefix on Trail Detail. If weight is not set, the calorie row shows "Add weight in Settings for calorie estimate" (tappable).

---

## Screens

Single-activity, Compose Navigation, bottom nav with three destinations.

### Map (home)
Full-screen MapLibre map. All trails rendered. FAB bottom-right: start/stop recording. While recording: live trail draws in real-time, recording pill (elapsed time, distance, pace) overlaid at top. Tap trail → bottom sheet with metadata. Long-press → drop marker. Tap marker → view/edit. Tap photo pin → full-screen photo.

### Trails
Chronological list of all recorded trails. Card shows date, distance, duration, elevation gain, trail shape thumbnail. Tap → Trail Detail. Swipe-to-delete with undo snackbar.

### Trail Detail
Full-screen map zoomed to trail with segments highlighted. Bottom sheet: stats (distance, time, elevation profile graph), calorie estimate, photos horizontal scroll, markers list. Actions: Export GPX, Share PNG, Delete.

### Settings (top-bar overflow, not a bottom nav item)
Sections:
- **Profile** — weight (kg or lbs toggle). Optional; used only for calorie estimate.
- **Recording** — min displacement (default 8 m), pause timeout (default 60 s).
- **Display** — gradient lines toggle, map tile source (OpenFreeMap / OSM Standard / OSM Humanitarian).
- **Data** — export all trails, clear all data (destructive, confirmation required).

First-launch: single permissions prompt (location + notifications). No onboarding beyond that.

---

## Data Model

### Room

```sql
Trail
  id              INTEGER PRIMARY KEY
  recorded_at     INTEGER  -- epoch ms
  distance_m      REAL
  duration_ms     INTEGER
  elevation_gain_m REAL
  calories_kcal   REAL     -- nullable; null if weight not set at time of recording
  bounding_box    TEXT     -- JSON {swLat, swLon, neLat, neLon}
  note            TEXT     -- nullable

TrailPoint
  id              INTEGER PRIMARY KEY
  trail_id        INTEGER  REFERENCES Trail(id)
  segment_index   INTEGER
  latitude        REAL
  longitude       REAL
  altitude_m      REAL
  recorded_at     INTEGER  -- epoch ms

Marker
  id              INTEGER PRIMARY KEY
  trail_id        INTEGER  REFERENCES Trail(id)  -- nullable
  latitude        REAL
  longitude       REAL
  type            TEXT     -- IMPASSABLE | OVERGROWN | TREACHEROUS | NOTE
  body            TEXT     -- nullable
  created_at      INTEGER  -- epoch ms

TrailPhoto
  id              INTEGER PRIMARY KEY
  trail_id        INTEGER  REFERENCES Trail(id)
  latitude        REAL
  longitude       REAL
  file_uri        TEXT     -- local content URI
  taken_at        INTEGER  -- epoch ms
```

### DataStore

```
weight_kg           Float?   (null = not set)
weight_unit         Enum     KG | LBS
gradient_enabled    Boolean  (default true)
min_displacement_m  Int      (default 8)
pause_timeout_s     Int      (default 60)
map_tile_source     Enum     OPENFREEMAP | OSM_STANDARD | OSM_HUMANITARIAN
```

---

## CI/CD & Release

**Repo:** `z3roCoo1/OverMapper` on GitHub (public).  
**Package:** `xyz.northline.overmapper`  
**Keystore:** `overmapper-release.jks` (new, separate from NetCalc)

**`ci.yml`** — triggers on push to `main` and PRs to `main`:
1. JDK 17 (Temurin)
2. `./gradlew test`
3. `./gradlew assembleDebug`
4. Upload debug APK artifact (7-day retention)

**`release.yml`** — triggers on `v*` tags:
1. JDK 17
2. `./gradlew test`
3. Decode keystore from `KEYSTORE_FILE` secret (base64)
4. `./gradlew bundleRelease`
5. Upload AAB to Play Store **internal test track** via `r0adkll/upload-google-play@v1`
6. Create GitHub Release with auto-generated notes

Secrets required (same names as NetCalc):
`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `PLAY_SERVICE_ACCOUNT_JSON`

---

## Play Store Listing

| Field | Value |
|---|---|
| App name | OverMapper |
| Short description | Every trail you walk, joined to the last. |
| Category | Health & Fitness |
| Developer | Northline |
| Icon | `OverMapper-icon-512.png` (512×512, opaque bone bg) |
| Feature graphic | `OverMapper-feature-1024x500.png` (1024×500) |

**Full description:**
> OverMapper records each walk and threads it into the ones before — until your hills become a single connected web of trails.
>
> Every route you walk is layered over the last. Over time, the paths join up, the gaps close, and your landscape becomes your own.
>
> Offline-first. Your data stays yours.
>
> Features:
> - Automatic trail recording with smart stationary detection
> - Overlaid trail web — colour-coded by age
> - Elevation gradient view (uphill, flat, downhill)
> - Condition markers — flag impassable paths, overgrowth, treacherous climbs
> - Photo pins — attach photos at GPS coordinates
> - Calorie estimate (optional — enter your weight in Settings)
> - Export any trail as GPX or share a map snapshot

---

## Design Assets (from Northline design bundle)

Source files in `/tmp/design_extract/northline/project/software/overmapper/`:
- `OverMapper-icon-512.png` — app icon, 512×512
- `OverMapper-feature-1024x500.png` — Play Store feature graphic, 1024×500
- `icon.html` — editable icon source
- `feature.html` — editable feature graphic source
- `trail-art.js` — shared trail-web art generator (for in-app use if desired)

**Colour palette:**
| Token | Hex | Usage |
|---|---|---|
| bone | `#F6F1EA` | Background |
| linen | `#EDE5D8` | Surface / cards |
| ink | `#1F1A14` | Primary text |
| terracotta | `#B5562E` | Newest trail, accent, CTA |
| sage | `#7E9A86` | Previous trails |
| trail-grey | `#8C7F6E` | Older trails, secondary text |

**Typography:**
- Display / headings: Newsreader (serif, italic emphasis)
- UI / body: Inter Tight
- Labels / specs: JetBrains Mono

---

## Out of Scope (Phase 2)

- Custom route drawing + draw-and-follow navigation
- Link-based sharing (requires backend)
- Multi-device sync
- Website redesign (handled separately)
