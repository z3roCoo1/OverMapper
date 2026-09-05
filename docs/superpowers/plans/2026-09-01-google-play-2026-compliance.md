# OverMapper: Google Play 2026 Compliance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring OverMapper into compliance with Google Play's target-API-36 deadline (Aug 31, 2026) before it blocks future updates. OverMapper has no Play Billing dependency, so the Billing Library alert in `/mnt/nas/data3/Other/dev_ver` does not apply to this app.

**Architecture:** Two sequential config bumps against the existing Gradle Kotlin DSL + version-catalog setup — AGP/Gradle/SDK floor raise, then a version bump + verification build. No source code changes are expected (no Billing Library usage to migrate).

**Tech Stack:** Kotlin, Android Gradle Plugin (Kotlin DSL), Gradle version catalog (`gradle/libs.versions.toml`), MapLibre Android SDK, Hilt, Room, Jetpack Compose.

## Global Constraints

- Target API level must be 36 (Android 16) or higher — required by Aug 31, 2026 (source: Google Play Console alert, `/mnt/nas/data3/Other/dev_ver`).
- compileSdk must be >= targetSdk, so compileSdk also moves to 36, and `buildToolsVersion` moves to match.
- AGP 8.11.0+ is the first stable AGP line that supports compileSdk 36; it requires Gradle 8.13+ (source: `developer.android.com/build/releases/agp-8-11-0-release-notes`, fetched 2026-09-01).
- Do not change `applicationId` (`com.z3rocool.overmapper`) or the signing config — tied to the live Play Store listing.
- No new test infrastructure is being introduced — verify via existing `./gradlew test` plus a successful `assembleDebug` build, matching what `.github/workflows/ci.yml` / `release.yml` already gate on.
- Do not push a `v*` git tag as part of this plan — that triggers `release.yml`, which uploads directly to the Play Store internal track. Tagging/pushing is a separate, explicit user decision after this plan's work is reviewed.
- Note (not a blocking task here, just a heads-up): OverMapper bundles native `.so` libraries via `maplibre-android:11.0.0`. Android 16 devices increasingly require 16 KB memory-page-aligned native libraries. This plan does not attempt to fix or verify that — flag it to the user as a follow-up if MapLibre-related crashes appear on Android 16 devices after this update ships.

---

### Task 1: Raise SDK/tooling floor to support API 36

**Files:**
- Modify: `/workspace/OverMapper/gradle/wrapper/gradle-wrapper.properties`
- Modify: `/workspace/OverMapper/gradle/libs.versions.toml` (`agp` version)
- Modify: `/workspace/OverMapper/app/build.gradle.kts:18-19,24` (`compileSdk`, `buildToolsVersion`, `targetSdk`)

**Interfaces:**
- Consumes: nothing from other tasks (first task).
- Produces: a project that builds against compileSdk/targetSdk 36 with AGP 8.11.1/Gradle 8.13. Task 2 builds on top of this working state.

- [ ] **Step 1: Install the Android 36 platform and build-tools locally (skip if already installed from the NetCalc plan)**

Run:
```bash
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk "platforms;android-36" "build-tools;36.1.0"
```
Expected: both packages install (or report already installed) without error.

- [ ] **Step 2: Confirm current build works before touching anything (baseline)**

Run: `cd /workspace/OverMapper && ./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`. If this fails for reasons unrelated to this plan, stop and investigate before proceeding.

- [ ] **Step 3: Bump the Gradle wrapper to 8.13**

In `/workspace/OverMapper/gradle/wrapper/gradle-wrapper.properties`, change:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-bin.zip
```
to:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
```

- [ ] **Step 4: Bump AGP to 8.11.1**

In `/workspace/OverMapper/gradle/libs.versions.toml`, change:
```toml
agp = "8.7.3"
```
to:
```toml
agp = "8.11.1"
```

- [ ] **Step 5: Bump compileSdk, buildToolsVersion, and targetSdk to 36**

In `/workspace/OverMapper/app/build.gradle.kts`, change:
```kotlin
android {
    namespace = "xyz.northline.overmapper"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.z3rocool.overmapper"
        minSdk = 26
        targetSdk = 35
```
to:
```kotlin
android {
    namespace = "xyz.northline.overmapper"
    compileSdk = 36
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.z3rocool.overmapper"
        minSdk = 26
        targetSdk = 36
```

- [ ] **Step 6: Rebuild and verify**

Run: `./gradlew assembleDebug --stacktrace`
Expected: `BUILD SUCCESSFUL`. If it fails on a Gradle daemon version mismatch, run `./gradlew --stop` first, then retry.

- [ ] **Step 7: Commit**

```bash
cd /workspace/OverMapper
git add gradle/wrapper/gradle-wrapper.properties gradle/libs.versions.toml app/build.gradle.kts
git commit -m "build: raise compileSdk/targetSdk to 36, AGP to 8.11.1, Gradle to 8.13"
```

---

### Task 2: Bump release version and do a full release-shape verification build

**Files:**
- Modify: `/workspace/OverMapper/app/build.gradle.kts:25-26` (`versionCode`, `versionName`)

**Interfaces:**
- Consumes: the working, compliant build from Task 1.
- Produces: a version-bumped `app/build.gradle.kts` ready for the user to tag and push — this plan does not push the tag.

- [ ] **Step 1: Bump versionCode and versionName**

In `/workspace/OverMapper/app/build.gradle.kts`, change:
```kotlin
        versionCode = 3
        versionName = "1.0.4"
```
to:
```kotlin
        versionCode = 4
        versionName = "1.0.5"
```

- [ ] **Step 2: Full verification build**

Run: `./gradlew test lint assembleDebug`
Expected: `BUILD SUCCESSFUL`, no new lint errors (warnings about the version bump itself are fine).

- [ ] **Step 3: Sanity-check a release bundle build (signing required)**

Run: `KEYSTORE_PASSWORD=<from keystore> KEY_ALIAS=<alias> KEY_PASSWORD=<from keystore> ./gradlew bundleRelease`
Expected: `BUILD SUCCESSFUL`, `app/build/outputs/bundle/release/app-release.aab` produced. If the keystore passwords aren't available in this session, skip this step and rely on CI's `release.yml` to do the signed build when the tag is eventually pushed.

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle.kts
git commit -m "chore: bump version to 1.0.5 (versionCode 4)"
```

- [ ] **Step 5: Stop — do not tag/push yet**

Pushing a `v*` tag triggers `release.yml`, which uploads straight to the Play Store internal track. That's a user decision, not part of this plan. Report back that the code is compliant and ready to ship, and let the user decide when to run:
```bash
git push
git tag v1.0.5
git push --tags
```
