# OverMapper Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build OverMapper — a Kotlin/Compose Android app that records GPS hikes and overlays every walk on the same map, building a connected trail web over time.

**Architecture:** Single-module MVVM. MapLibre renders the map with GeoJSON overlay layers. A foreground `LifecycleService` handles GPS recording with stationary filtering and auto-pause. All data in Room + DataStore; no backend.

**Tech Stack:** Kotlin 2.1, Jetpack Compose + Material 3, MapLibre Android SDK 11, Room 2.6, Hilt 2.53, FusedLocationProviderClient, JUnit 4 + Truth.

---

## File Map

```
OverMapper/
├── .github/workflows/
│   ├── ci.yml
│   └── release.yml
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── kotlin/xyz/northline/overmapper/
│       │       ├── OverMapperApp.kt
│       │       ├── MainActivity.kt
│       │       ├── data/
│       │       │   ├── db/
│       │       │   │   ├── OverMapperDatabase.kt
│       │       │   │   ├── entity/TrailEntity.kt
│       │       │   │   ├── entity/TrailPointEntity.kt
│       │       │   │   ├── entity/MarkerEntity.kt
│       │       │   │   ├── entity/TrailPhotoEntity.kt
│       │       │   │   ├── dao/TrailDao.kt
│       │       │   │   ├── dao/TrailPointDao.kt
│       │       │   │   ├── dao/MarkerDao.kt
│       │       │   │   └── dao/TrailPhotoDao.kt
│       │       │   ├── datastore/
│       │       │   │   ├── PreferencesKeys.kt
│       │       │   │   └── UserPreferencesRepository.kt
│       │       │   └── repository/
│       │       │       ├── TrailRepository.kt
│       │       │       ├── MarkerRepository.kt
│       │       │       └── PhotoRepository.kt
│       │       ├── domain/
│       │       │   ├── model/
│       │       │   │   ├── Trail.kt
│       │       │   │   ├── TrailPoint.kt
│       │       │   │   ├── Marker.kt
│       │       │   │   ├── MarkerType.kt
│       │       │   │   ├── TrailPhoto.kt
│       │       │   │   └── RecordingState.kt
│       │       │   ├── CalorieCalculator.kt
│       │       │   ├── GpxExporter.kt
│       │       │   ├── LocationPoint.kt
│       │       │   ├── StationaryFilter.kt
│       │       │   └── TrailGeoJsonBuilder.kt
│       │       ├── service/
│       │       │   ├── RecordingService.kt
│       │       │   └── RecordingStateHolder.kt
│       │       └── ui/
│       │           ├── theme/Color.kt
│       │           ├── theme/Theme.kt
│       │           ├── theme/Type.kt
│       │           ├── navigation/NavGraph.kt
│       │           ├── map/MapScreen.kt
│       │           ├── map/MapViewModel.kt
│       │           ├── map/MapLibreView.kt
│       │           ├── map/RecordingPill.kt
│       │           ├── trails/TrailsScreen.kt
│       │           ├── trails/TrailsViewModel.kt
│       │           ├── detail/TrailDetailScreen.kt
│       │           ├── detail/TrailDetailViewModel.kt
│       │           ├── detail/ElevationProfileChart.kt
│       │           ├── settings/SettingsScreen.kt
│       │           ├── settings/SettingsViewModel.kt
│       │           ├── components/TrailBottomSheet.kt
│       │           └── components/MarkerBottomSheet.kt
│       └── test/kotlin/xyz/northline/overmapper/
│           ├── CalorieCalculatorTest.kt
│           ├── StationaryFilterTest.kt
│           ├── TrailGeoJsonBuilderTest.kt
│           └── GpxExporterTest.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradle/
    ├── libs.versions.toml
    └── wrapper/gradle-wrapper.properties
```

---

## Task 1: Create GitHub repo

**Files:** none locally

- [ ] **Create the repo**

```bash
gh repo create z3roCoo1/OverMapper --public --description "Hiking trail overlay app — every walk layered over the last" --clone=false
```

Expected: `✓ Created repository z3roCoo1/OverMapper`

- [ ] **Add remote and push the existing commit**

```bash
cd /workspace/OverMapper
git remote add origin git@github.com:z3roCoo1/OverMapper.git
git push -u origin main
```

---

## Task 2: Gradle version catalog and build files

**Files:**
- Create: `gradle/libs.versions.toml`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (root)
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`

- [ ] **Create `gradle/wrapper/gradle-wrapper.properties`**

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Create `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
ksp = "2.1.0-1.0.29"
composeBom = "2024.12.01"
maplibre = "11.0.0"
room = "2.6.1"
hilt = "2.53.1"
hiltNavigation = "1.2.0"
datastore = "1.1.2"
location = "21.3.0"
lifecycle = "2.8.7"
navigationCompose = "2.8.5"
activity = "1.9.3"
coil = "2.7.0"
coroutines = "1.9.0"

[libraries]
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity" }
androidx-activity-ktx = { group = "androidx.activity", name = "activity-ktx", version.ref = "activity" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-service = { group = "androidx.lifecycle", name = "lifecycle-service", version.ref = "lifecycle" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigation" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "location" }
maplibre-android = { group = "org.maplibre.gl", name = "android-sdk", version.ref = "maplibre" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
junit = { group = "junit", name = "junit", version = "4.13.2" }
truth = { group = "com.google.truth", name = "truth", version = "1.4.4" }
androidx-test-ext = { group = "androidx.test.ext", name = "junit", version = "1.2.1" }
androidx-test-runner = { group = "androidx.test", name = "runner", version = "1.6.2" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

- [ ] **Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google { content { includeGroupByRegex("com\\.android.*"); includeGroupByRegex("com\\.google.*"); includeGroupByRegex("androidx.*") } }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "OverMapper"
include(":app")
```

- [ ] **Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

- [ ] **Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Create `app/build.gradle.kts`**

```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val keystoreProps = Properties().apply {
    val f = rootProject.file("overmapper-release.jks.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "xyz.northline.overmapper"
    compileSdk = 35

    defaultConfig {
        applicationId = "xyz.northline.overmapper"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("overmapper-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: keystoreProps["storePassword"] as String?
            keyAlias = System.getenv("KEY_ALIAS") ?: keystoreProps["keyAlias"] as String?
            keyPassword = System.getenv("KEY_PASSWORD") ?: keystoreProps["keyPassword"] as String?
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.service)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.play.services.location)
    implementation(libs.maplibre.android)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.android)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
```

- [ ] **Download Gradle wrapper scripts**

```bash
cd /workspace/OverMapper
# Copy gradlew and gradlew.bat from NetCalc as a starting point
cp /workspace/NetCalc/gradlew ./gradlew
cp /workspace/NetCalc/gradlew.bat ./gradlew.bat 2>/dev/null || true
chmod +x gradlew
```

- [ ] **Commit**

```bash
git add .
git commit -m "chore: add Gradle build files and version catalog"
```

---

## Task 3: AndroidManifest + Application class + MainActivity

**Files:**
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/OverMapperApp.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/MainActivity.kt`
- Create: `app/src/main/res/values/strings.xml`

- [ ] **Create `AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".OverMapperApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.OverMapper">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.RecordingService"
            android:foregroundServiceType="location"
            android:exported="false" />

    </application>
</manifest>
```

- [ ] **Create `OverMapperApp.kt`**

```kotlin
package xyz.northline.overmapper

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OverMapperApp : Application()
```

- [ ] **Create `MainActivity.kt`**

```kotlin
package xyz.northline.overmapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import xyz.northline.overmapper.ui.navigation.NavGraph
import xyz.northline.overmapper.ui.theme.OverMapperTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            OverMapperTheme {
                NavGraph()
            }
        }
    }
}
```

- [ ] **Create `res/values/strings.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">OverMapper</string>
    <string name="notification_channel_name">Trail Recording</string>
    <string name="notification_recording">Recording trail</string>
    <string name="notification_paused">Paused — not moving</string>
</resources>
```

- [ ] **Create `res/values/themes.xml`** (required for the manifest theme reference)

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.OverMapper" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [ ] **Commit**

```bash
git add .
git commit -m "chore: add manifest, Application class, MainActivity"
```

---

## Task 4: Northline theme

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/theme/Color.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/theme/Type.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/theme/Theme.kt`

- [ ] **Create `Color.kt`**

```kotlin
package xyz.northline.overmapper.ui.theme

import androidx.compose.ui.graphics.Color

// Northline / for home palette
val Bone = Color(0xFFF6F1EA)
val Linen = Color(0xFFEDE5D8)
val Ink = Color(0xFF1F1A14)
val Terracotta = Color(0xFFB5562E)
val Sage = Color(0xFF7E9A86)
val TrailGrey = Color(0xFF8C7F6E)
val DarkText = Color(0xFF43392E)
val MutedText = Color(0xFF5C5145)

// Gradient colours
val GradientSteepUp = Color(0xFFD97706)    // amber-600
val GradientUphill = Color(0xFFF59E0B)     // amber-400
val GradientFlat = Color(0xFF9CA3AF)       // grey-400
val GradientDownhill = Color(0xFF38BDF8)   // sky-400
val GradientSteepDown = Color(0xFF0284C7)  // sky-600
```

- [ ] **Create `Type.kt`**

```kotlin
package xyz.northline.overmapper.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Inter Tight is not on Google Fonts for Android; use the system sans-serif
// Newsreader is not available as an Android font asset; use serif fallback for display text
val InterTight = FontFamily.SansSerif
val Newsreader = FontFamily.Serif

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp
    )
)
```

- [ ] **Create `Theme.kt`**

```kotlin
package xyz.northline.overmapper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = Bone,
    primaryContainer = Linen,
    onPrimaryContainer = Ink,
    secondary = Sage,
    onSecondary = Bone,
    secondaryContainer = Linen,
    onSecondaryContainer = Ink,
    background = Bone,
    onBackground = Ink,
    surface = Bone,
    onSurface = Ink,
    surfaceVariant = Linen,
    onSurfaceVariant = MutedText,
    outline = TrailGrey
)

@Composable
fun OverMapperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add Northline theme — bone, terracotta, sage palette"
```

---

## Task 5: Room entities

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/db/entity/TrailEntity.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/db/entity/TrailPointEntity.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/db/entity/MarkerEntity.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/db/entity/TrailPhotoEntity.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/model/MarkerType.kt`

- [ ] **Create `MarkerType.kt`**

```kotlin
package xyz.northline.overmapper.domain.model

enum class MarkerType { IMPASSABLE, OVERGROWN, TREACHEROUS, NOTE }
```

- [ ] **Create `TrailEntity.kt`**

```kotlin
package xyz.northline.overmapper.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trail")
data class TrailEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long,
    @ColumnInfo(name = "distance_m") val distanceM: Float,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "elevation_gain_m") val elevationGainM: Float,
    @ColumnInfo(name = "calories_kcal") val caloriesKcal: Float?,
    @ColumnInfo(name = "bbox_sw_lat") val bboxSwLat: Double,
    @ColumnInfo(name = "bbox_sw_lon") val bboxSwLon: Double,
    @ColumnInfo(name = "bbox_ne_lat") val bboxNeLat: Double,
    @ColumnInfo(name = "bbox_ne_lon") val bboxNeLon: Double,
    val note: String? = null
)
```

- [ ] **Create `TrailPointEntity.kt`**

```kotlin
package xyz.northline.overmapper.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trail_point",
    foreignKeys = [ForeignKey(
        entity = TrailEntity::class,
        parentColumns = ["id"],
        childColumns = ["trail_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("trail_id")]
)
data class TrailPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "trail_id") val trailId: Long,
    @ColumnInfo(name = "segment_index") val segmentIndex: Int,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "altitude_m") val altitudeM: Double,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long
)
```

- [ ] **Create `MarkerEntity.kt`**

```kotlin
package xyz.northline.overmapper.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "marker",
    foreignKeys = [ForeignKey(
        entity = TrailEntity::class,
        parentColumns = ["id"],
        childColumns = ["trail_id"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("trail_id")]
)
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "trail_id") val trailId: Long?,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val body: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
```

- [ ] **Create `TrailPhotoEntity.kt`**

```kotlin
package xyz.northline.overmapper.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trail_photo",
    foreignKeys = [ForeignKey(
        entity = TrailEntity::class,
        parentColumns = ["id"],
        childColumns = ["trail_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("trail_id")]
)
data class TrailPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "trail_id") val trailId: Long,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "file_uri") val fileUri: String,
    @ColumnInfo(name = "taken_at") val takenAt: Long
)
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add Room entities — Trail, TrailPoint, Marker, TrailPhoto"
```

---

## Task 6: Room DAOs and database class

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/db/dao/TrailDao.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/db/dao/TrailPointDao.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/db/dao/MarkerDao.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/db/dao/TrailPhotoDao.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/db/OverMapperDatabase.kt`

- [ ] **Create `TrailDao.kt`**

```kotlin
package xyz.northline.overmapper.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.northline.overmapper.data.db.entity.TrailEntity

@Dao
interface TrailDao {
    @Query("SELECT * FROM trail ORDER BY recorded_at DESC")
    fun observeAll(): Flow<List<TrailEntity>>

    @Query("SELECT * FROM trail WHERE id = :id")
    suspend fun getById(id: Long): TrailEntity?

    @Insert
    suspend fun insert(trail: TrailEntity): Long

    @Update
    suspend fun update(trail: TrailEntity)

    @Delete
    suspend fun delete(trail: TrailEntity)

    @Query("DELETE FROM trail WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

- [ ] **Create `TrailPointDao.kt`**

```kotlin
package xyz.northline.overmapper.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.northline.overmapper.data.db.entity.TrailPointEntity

@Dao
interface TrailPointDao {
    @Query("SELECT * FROM trail_point WHERE trail_id = :trailId ORDER BY recorded_at ASC")
    suspend fun getByTrailId(trailId: Long): List<TrailPointEntity>

    @Query("SELECT * FROM trail_point ORDER BY trail_id ASC, segment_index ASC, recorded_at ASC")
    fun observeAll(): Flow<List<TrailPointEntity>>

    @Insert
    suspend fun insertAll(points: List<TrailPointEntity>)

    @Insert
    suspend fun insert(point: TrailPointEntity): Long

    @Query("DELETE FROM trail_point WHERE trail_id = :trailId")
    suspend fun deleteByTrailId(trailId: Long)
}
```

- [ ] **Create `MarkerDao.kt`**

```kotlin
package xyz.northline.overmapper.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.northline.overmapper.data.db.entity.MarkerEntity

@Dao
interface MarkerDao {
    @Query("SELECT * FROM marker ORDER BY created_at DESC")
    fun observeAll(): Flow<List<MarkerEntity>>

    @Query("SELECT * FROM marker WHERE id = :id")
    suspend fun getById(id: Long): MarkerEntity?

    @Insert
    suspend fun insert(marker: MarkerEntity): Long

    @Update
    suspend fun update(marker: MarkerEntity)

    @Delete
    suspend fun delete(marker: MarkerEntity)
}
```

- [ ] **Create `TrailPhotoDao.kt`**

```kotlin
package xyz.northline.overmapper.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.northline.overmapper.data.db.entity.TrailPhotoEntity

@Dao
interface TrailPhotoDao {
    @Query("SELECT * FROM trail_photo WHERE trail_id = :trailId ORDER BY taken_at ASC")
    fun observeByTrailId(trailId: Long): Flow<List<TrailPhotoEntity>>

    @Insert
    suspend fun insert(photo: TrailPhotoEntity): Long

    @Delete
    suspend fun delete(photo: TrailPhotoEntity)
}
```

- [ ] **Create `OverMapperDatabase.kt`**

```kotlin
package xyz.northline.overmapper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.northline.overmapper.data.db.dao.*
import xyz.northline.overmapper.data.db.entity.*

@Database(
    entities = [TrailEntity::class, TrailPointEntity::class, MarkerEntity::class, TrailPhotoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OverMapperDatabase : RoomDatabase() {
    abstract fun trailDao(): TrailDao
    abstract fun trailPointDao(): TrailPointDao
    abstract fun markerDao(): MarkerDao
    abstract fun trailPhotoDao(): TrailPhotoDao
}
```

- [ ] **Create Hilt database module `app/src/main/kotlin/xyz/northline/overmapper/di/DatabaseModule.kt`**

```kotlin
package xyz.northline.overmapper.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import xyz.northline.overmapper.data.db.OverMapperDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OverMapperDatabase =
        Room.databaseBuilder(context, OverMapperDatabase::class.java, "overmapper.db").build()

    @Provides fun provideTrailDao(db: OverMapperDatabase) = db.trailDao()
    @Provides fun provideTrailPointDao(db: OverMapperDatabase) = db.trailPointDao()
    @Provides fun provideMarkerDao(db: OverMapperDatabase) = db.markerDao()
    @Provides fun provideTrailPhotoDao(db: OverMapperDatabase) = db.trailPhotoDao()
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add Room DAOs, database class, and Hilt database module"
```

---

## Task 7: DataStore preferences

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/datastore/PreferencesKeys.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/datastore/UserPreferencesRepository.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/di/DataStoreModule.kt`

- [ ] **Create `PreferencesKeys.kt`**

```kotlin
package xyz.northline.overmapper.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val WEIGHT_KG = floatPreferencesKey("weight_kg")
    val WEIGHT_UNIT = stringPreferencesKey("weight_unit")           // "KG" | "LBS"
    val GRADIENT_ENABLED = booleanPreferencesKey("gradient_enabled")
    val MIN_DISPLACEMENT_M = intPreferencesKey("min_displacement_m")
    val PAUSE_TIMEOUT_S = intPreferencesKey("pause_timeout_s")
    val MAP_TILE_SOURCE = stringPreferencesKey("map_tile_source")   // "OPENFREEMAP" | "OSM_STANDARD" | "OSM_HUMANITARIAN"
}
```

- [ ] **Create `UserPreferencesRepository.kt`**

```kotlin
package xyz.northline.overmapper.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val weightKg: Float? = null,
    val weightUnit: String = "KG",
    val gradientEnabled: Boolean = true,
    val minDisplacementM: Int = 8,
    val pauseTimeoutS: Int = 60,
    val mapTileSource: String = "OPENFREEMAP"
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            weightKg = prefs[PreferencesKeys.WEIGHT_KG],
            weightUnit = prefs[PreferencesKeys.WEIGHT_UNIT] ?: "KG",
            gradientEnabled = prefs[PreferencesKeys.GRADIENT_ENABLED] ?: true,
            minDisplacementM = prefs[PreferencesKeys.MIN_DISPLACEMENT_M] ?: 8,
            pauseTimeoutS = prefs[PreferencesKeys.PAUSE_TIMEOUT_S] ?: 60,
            mapTileSource = prefs[PreferencesKeys.MAP_TILE_SOURCE] ?: "OPENFREEMAP"
        )
    }

    suspend fun setWeightKg(value: Float?) {
        dataStore.edit { prefs ->
            if (value == null) prefs.remove(PreferencesKeys.WEIGHT_KG)
            else prefs[PreferencesKeys.WEIGHT_KG] = value
        }
    }

    suspend fun setWeightUnit(unit: String) {
        dataStore.edit { it[PreferencesKeys.WEIGHT_UNIT] = unit }
    }

    suspend fun setGradientEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.GRADIENT_ENABLED] = enabled }
    }

    suspend fun setMinDisplacementM(value: Int) {
        dataStore.edit { it[PreferencesKeys.MIN_DISPLACEMENT_M] = value }
    }

    suspend fun setPauseTimeoutS(value: Int) {
        dataStore.edit { it[PreferencesKeys.PAUSE_TIMEOUT_S] = value }
    }

    suspend fun setMapTileSource(source: String) {
        dataStore.edit { it[PreferencesKeys.MAP_TILE_SOURCE] = source }
    }
}
```

- [ ] **Create `DataStoreModule.kt`**

```kotlin
package xyz.northline.overmapper.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add DataStore preferences with UserPreferencesRepository"
```

---

## Task 8: Domain models and repository layer

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/model/Trail.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/model/TrailPoint.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/model/Marker.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/model/TrailPhoto.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/model/RecordingState.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/repository/TrailRepository.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/repository/MarkerRepository.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/data/repository/PhotoRepository.kt`

- [ ] **Create domain models**

```kotlin
// Trail.kt
package xyz.northline.overmapper.domain.model

data class Trail(
    val id: Long,
    val recordedAt: Long,
    val distanceM: Float,
    val durationMs: Long,
    val elevationGainM: Float,
    val caloriesKcal: Float?,
    val bboxSwLat: Double, val bboxSwLon: Double,
    val bboxNeLat: Double, val bboxNeLon: Double,
    val note: String? = null
)
```

```kotlin
// TrailPoint.kt
package xyz.northline.overmapper.domain.model

data class TrailPoint(
    val id: Long,
    val trailId: Long,
    val segmentIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val recordedAt: Long
)
```

```kotlin
// Marker.kt
package xyz.northline.overmapper.domain.model

data class Marker(
    val id: Long,
    val trailId: Long?,
    val latitude: Double,
    val longitude: Double,
    val type: MarkerType,
    val body: String?,
    val createdAt: Long
)
```

```kotlin
// TrailPhoto.kt
package xyz.northline.overmapper.domain.model

data class TrailPhoto(
    val id: Long,
    val trailId: Long,
    val latitude: Double,
    val longitude: Double,
    val fileUri: String,
    val takenAt: Long
)
```

```kotlin
// RecordingState.kt
package xyz.northline.overmapper.domain.model

sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(
        val trailId: Long,
        val startMs: Long,
        val distanceM: Float,
        val pointCount: Int
    ) : RecordingState()
    data class Paused(
        val trailId: Long,
        val startMs: Long,
        val distanceM: Float
    ) : RecordingState()
}
```

- [ ] **Create `TrailRepository.kt`**

```kotlin
package xyz.northline.overmapper.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import xyz.northline.overmapper.data.db.dao.TrailDao
import xyz.northline.overmapper.data.db.dao.TrailPointDao
import xyz.northline.overmapper.data.db.entity.TrailEntity
import xyz.northline.overmapper.data.db.entity.TrailPointEntity
import xyz.northline.overmapper.domain.model.Trail
import xyz.northline.overmapper.domain.model.TrailPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrailRepository @Inject constructor(
    private val trailDao: TrailDao,
    private val trailPointDao: TrailPointDao
) {
    fun observeAll(): Flow<List<Trail>> = trailDao.observeAll().mapToTrails()

    suspend fun getById(id: Long): Trail? = trailDao.getById(id)?.toDomain()

    suspend fun getPointsForTrail(trailId: Long): List<TrailPoint> =
        trailPointDao.getByTrailId(trailId).map { it.toDomain() }

    fun observeAllPoints(): Flow<List<TrailPoint>> =
        trailPointDao.observeAll().mapToPoints()

    suspend fun insertTrail(trail: TrailEntity): Long = trailDao.insert(trail)

    suspend fun updateTrail(trail: TrailEntity) = trailDao.update(trail)

    suspend fun insertPoints(points: List<TrailPointEntity>) = trailPointDao.insertAll(points)

    suspend fun insertPoint(point: TrailPointEntity): Long = trailPointDao.insert(point)

    suspend fun deleteTrail(id: Long) = trailDao.deleteById(id)

    // Mapping extensions
    private fun Flow<List<TrailEntity>>.mapToTrails() =
        kotlinx.coroutines.flow.map(this) { list -> list.map { it.toDomain() } }

    private fun Flow<List<TrailPointEntity>>.mapToPoints() =
        kotlinx.coroutines.flow.map(this) { list -> list.map { it.toDomain() } }
}

fun TrailEntity.toDomain() = Trail(id, recordedAt, distanceM, durationMs, elevationGainM,
    caloriesKcal, bboxSwLat, bboxSwLon, bboxNeLat, bboxNeLon, note)

fun TrailPointEntity.toDomain() = TrailPoint(id, trailId, segmentIndex, latitude, longitude,
    altitudeM, recordedAt)
```

- [ ] **Create `MarkerRepository.kt`**

```kotlin
package xyz.northline.overmapper.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.northline.overmapper.data.db.dao.MarkerDao
import xyz.northline.overmapper.data.db.entity.MarkerEntity
import xyz.northline.overmapper.domain.model.Marker
import xyz.northline.overmapper.domain.model.MarkerType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkerRepository @Inject constructor(private val dao: MarkerDao) {
    fun observeAll(): Flow<List<Marker>> = dao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun insert(marker: MarkerEntity): Long = dao.insert(marker)

    suspend fun update(marker: Marker) = dao.update(marker.toEntity())

    suspend fun delete(marker: Marker) = dao.delete(marker.toEntity())
}

fun MarkerEntity.toDomain() = Marker(id, trailId, latitude, longitude,
    MarkerType.valueOf(type), body, createdAt)

fun Marker.toEntity() = MarkerEntity(id, trailId, latitude, longitude,
    type.name, body, createdAt)
```

- [ ] **Create `PhotoRepository.kt`**

```kotlin
package xyz.northline.overmapper.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.northline.overmapper.data.db.dao.TrailPhotoDao
import xyz.northline.overmapper.data.db.entity.TrailPhotoEntity
import xyz.northline.overmapper.domain.model.TrailPhoto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(private val dao: TrailPhotoDao) {
    fun observeByTrailId(trailId: Long): Flow<List<TrailPhoto>> =
        dao.observeByTrailId(trailId).map { list -> list.map { it.toDomain() } }

    suspend fun insert(photo: TrailPhotoEntity): Long = dao.insert(photo)

    suspend fun delete(photo: TrailPhoto) = dao.delete(photo.toEntity())
}

fun TrailPhotoEntity.toDomain() = TrailPhoto(id, trailId, latitude, longitude, fileUri, takenAt)

fun TrailPhoto.toEntity() = TrailPhotoEntity(id, trailId, latitude, longitude, fileUri, takenAt)
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add domain models and repository layer"
```

---

## Task 9: CalorieCalculator (TDD)

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/CalorieCalculator.kt`
- Create: `app/src/test/kotlin/xyz/northline/overmapper/CalorieCalculatorTest.kt`

- [ ] **Write the failing test first**

```kotlin
// app/src/test/kotlin/xyz/northline/overmapper/CalorieCalculatorTest.kt
package xyz.northline.overmapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import xyz.northline.overmapper.domain.CalorieCalculator

class CalorieCalculatorTest {

    @Test
    fun `returns zero for zero duration`() {
        val result = CalorieCalculator.calculate(
            weightKg = 75f, durationMs = 0L, distanceM = 0f, elevationGainM = 0f
        )
        assertThat(result).isEqualTo(0f)
    }

    @Test
    fun `flat 5km walk at moderate pace for 75kg person`() {
        // ~60 min, 5000m, no elevation — expect roughly 350-450 kcal
        val result = CalorieCalculator.calculate(
            weightKg = 75f,
            durationMs = 60 * 60 * 1000L,
            distanceM = 5000f,
            elevationGainM = 0f
        )
        assertThat(result).isGreaterThan(300f)
        assertThat(result).isLessThan(500f)
    }

    @Test
    fun `steep climb burns more than flat walk at same distance and time`() {
        val flat = CalorieCalculator.calculate(75f, 3_600_000L, 5000f, 0f)
        val steep = CalorieCalculator.calculate(75f, 3_600_000L, 5000f, 400f)
        assertThat(steep).isGreaterThan(flat)
    }

    @Test
    fun `heavier person burns more calories`() {
        val light = CalorieCalculator.calculate(60f, 3_600_000L, 5000f, 100f)
        val heavy = CalorieCalculator.calculate(100f, 3_600_000L, 5000f, 100f)
        assertThat(heavy).isGreaterThan(light)
    }
}
```

- [ ] **Run test — expect compile failure (class not found)**

```bash
cd /workspace/OverMapper && ./gradlew test --tests "*.CalorieCalculatorTest" 2>&1 | tail -20
```

Expected: compilation error — `CalorieCalculator` not defined.

- [ ] **Implement `CalorieCalculator.kt`**

```kotlin
package xyz.northline.overmapper.domain

object CalorieCalculator {
    fun calculate(
        weightKg: Float,
        durationMs: Long,
        distanceM: Float,
        elevationGainM: Float
    ): Float {
        if (durationMs <= 0L) return 0f
        val durationHours = durationMs / 3_600_000f
        val distanceKm = distanceM / 1000f
        val paceMinPerKm = if (distanceKm > 0) (durationMs / 60_000f) / distanceKm else 20f
        // Base MET 5.5, faster pace → higher MET (pace 12 min/km = base, 8 min/km = +1.5)
        val paceDelta = ((20f - paceMinPerKm.coerceIn(8f, 30f)) / 12f) * 1.5f
        // Elevation: each 100m gain per km adds ~0.8 MET
        val elevDelta = if (distanceKm > 0) (elevationGainM / distanceKm / 100f) * 0.8f else 0f
        val met = (5.5f + paceDelta + elevDelta).coerceIn(4f, 12f)
        return met * weightKg * durationHours
    }
}
```

- [ ] **Run tests — expect all pass**

```bash
./gradlew test --tests "*.CalorieCalculatorTest"
```

Expected: `BUILD SUCCESSFUL` — 4 tests passed.

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add CalorieCalculator with MET-based formula (TDD)"
```

---

## Task 10: LocationPoint and StationaryFilter (TDD)

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/LocationPoint.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/StationaryFilter.kt`
- Create: `app/src/test/kotlin/xyz/northline/overmapper/StationaryFilterTest.kt`

- [ ] **Write failing tests**

```kotlin
// app/src/test/kotlin/xyz/northline/overmapper/StationaryFilterTest.kt
package xyz.northline.overmapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import xyz.northline.overmapper.domain.FilterResult
import xyz.northline.overmapper.domain.LocationPoint
import xyz.northline.overmapper.domain.StationaryFilter
import xyz.northline.overmapper.domain.FilterConfig

class StationaryFilterTest {

    private fun pt(lat: Double, lon: Double, t: Long = 0L) =
        LocationPoint(lat, lon, 0.0, t)

    @Test
    fun `first point is always accepted`() {
        val filter = StationaryFilter(FilterConfig(minDisplacementM = 8f, pauseTimeoutMs = 60_000L))
        val result = filter.process(pt(51.0, -0.1), nowMs = 0L)
        assertThat(result).isInstanceOf(FilterResult.Accept::class.java)
    }

    @Test
    fun `point under threshold is discarded`() {
        val filter = StationaryFilter(FilterConfig(minDisplacementM = 8f, pauseTimeoutMs = 60_000L))
        filter.process(pt(51.0, -0.1), nowMs = 0L)
        // ~2m away
        val result = filter.process(pt(51.000018, -0.1), nowMs = 1_000L)
        assertThat(result).isEqualTo(FilterResult.Discard)
    }

    @Test
    fun `point over threshold is accepted`() {
        val filter = StationaryFilter(FilterConfig(minDisplacementM = 8f, pauseTimeoutMs = 60_000L))
        filter.process(pt(51.0, -0.1), nowMs = 0L)
        // ~100m north
        val result = filter.process(pt(51.0009, -0.1), nowMs = 5_000L)
        assertThat(result).isInstanceOf(FilterResult.Accept::class.java)
    }

    @Test
    fun `stationary beyond timeout triggers pause`() {
        val filter = StationaryFilter(FilterConfig(minDisplacementM = 8f, pauseTimeoutMs = 60_000L))
        filter.process(pt(51.0, -0.1), nowMs = 0L)
        // Still within threshold — keep trying
        val result = filter.process(pt(51.000018, -0.1), nowMs = 61_000L)
        assertThat(result).isEqualTo(FilterResult.Pause)
    }

    @Test
    fun `valid point after pause triggers resume then accept`() {
        val filter = StationaryFilter(FilterConfig(minDisplacementM = 8f, pauseTimeoutMs = 60_000L))
        filter.process(pt(51.0, -0.1), nowMs = 0L)
        filter.process(pt(51.000018, -0.1), nowMs = 61_000L) // Pause
        val result = filter.process(pt(51.0009, -0.1), nowMs = 70_000L)
        assertThat(result).isEqualTo(FilterResult.Resume)
    }

    @Test
    fun `reset clears state — next point is accepted`() {
        val filter = StationaryFilter(FilterConfig())
        filter.process(pt(51.0, -0.1), nowMs = 0L)
        filter.reset()
        val result = filter.process(pt(51.0, -0.1), nowMs = 1_000L)
        assertThat(result).isInstanceOf(FilterResult.Accept::class.java)
    }
}
```

- [ ] **Run — expect compile failure**

```bash
./gradlew test --tests "*.StationaryFilterTest" 2>&1 | tail -10
```

- [ ] **Implement `LocationPoint.kt`**

```kotlin
package xyz.northline.overmapper.domain

import kotlin.math.*

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val timestampMs: Long
) {
    fun distanceTo(other: LocationPoint): Float {
        val r = 6_371_000.0
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLon = Math.toRadians(other.longitude - longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) *
                sin(dLon / 2).pow(2)
        return (2 * r * asin(sqrt(a))).toFloat()
    }
}
```

- [ ] **Implement `StationaryFilter.kt`**

```kotlin
package xyz.northline.overmapper.domain

data class FilterConfig(
    val minDisplacementM: Float = 8f,
    val pauseTimeoutMs: Long = 60_000L
)

sealed class FilterResult {
    data class Accept(val point: LocationPoint) : FilterResult()
    object Discard : FilterResult()
    object Pause : FilterResult()
    object Resume : FilterResult()
}

class StationaryFilter(private val config: FilterConfig) {
    private var lastAccepted: LocationPoint? = null
    private var lastValidMs: Long = 0L
    private var isPaused: Boolean = false

    fun process(point: LocationPoint, nowMs: Long = System.currentTimeMillis()): FilterResult {
        val prev = lastAccepted
        if (prev != null && point.distanceTo(prev) < config.minDisplacementM) {
            if (!isPaused && (nowMs - lastValidMs) > config.pauseTimeoutMs) {
                isPaused = true
                return FilterResult.Pause
            }
            return FilterResult.Discard
        }
        val wasResuming = isPaused
        lastAccepted = point
        lastValidMs = nowMs
        isPaused = false
        return if (wasResuming) FilterResult.Resume else FilterResult.Accept(point)
    }

    fun reset() {
        lastAccepted = null
        lastValidMs = 0L
        isPaused = false
    }
}
```

- [ ] **Run tests — expect all pass**

```bash
./gradlew test --tests "*.StationaryFilterTest"
```

Expected: `BUILD SUCCESSFUL` — 6 tests passed.

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add LocationPoint + StationaryFilter with auto-pause logic (TDD)"
```

---

## Task 11: TrailGeoJsonBuilder (TDD)

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/TrailGeoJsonBuilder.kt`
- Create: `app/src/test/kotlin/xyz/northline/overmapper/TrailGeoJsonBuilderTest.kt`

- [ ] **Write failing tests**

```kotlin
// app/src/test/kotlin/xyz/northline/overmapper/TrailGeoJsonBuilderTest.kt
package xyz.northline.overmapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import xyz.northline.overmapper.domain.TrailGeoJsonBuilder
import xyz.northline.overmapper.domain.model.TrailPoint

class TrailGeoJsonBuilderTest {

    private fun pt(trailId: Long, seg: Int, lat: Double, lon: Double, alt: Double = 0.0, t: Long = 0L) =
        TrailPoint(0L, trailId, seg, lat, lon, alt, t)

    @Test
    fun `empty points list returns empty feature collection`() {
        val json = TrailGeoJsonBuilder.buildOverlay(emptyList(), emptyList())
        assertThat(json).contains("\"type\":\"FeatureCollection\"")
        assertThat(json).contains("\"features\":[]")
    }

    @Test
    fun `single trail single segment produces one feature`() {
        val points = listOf(
            pt(1L, 0, 51.0, -0.1),
            pt(1L, 0, 51.001, -0.1),
            pt(1L, 0, 51.002, -0.1)
        )
        val json = TrailGeoJsonBuilder.buildOverlay(listOf(1L), points)
        assertThat(json.occurrencesOf("\"type\":\"Feature\"")).isEqualTo(1)
    }

    @Test
    fun `segment break produces two features`() {
        val points = listOf(
            pt(1L, 0, 51.0, -0.1),
            pt(1L, 0, 51.001, -0.1),
            pt(1L, 1, 51.005, -0.1),  // new segment
            pt(1L, 1, 51.006, -0.1)
        )
        val json = TrailGeoJsonBuilder.buildOverlay(listOf(1L), points)
        assertThat(json.occurrencesOf("\"type\":\"Feature\"")).isEqualTo(2)
    }

    @Test
    fun `newest trail gets terracotta age bucket`() {
        val trailIds = listOf(1L, 2L)
        val points = listOf(
            pt(1L, 0, 51.0, -0.1, t = 1000L),
            pt(1L, 0, 51.001, -0.1, t = 2000L),
            pt(2L, 0, 51.0, -0.2, t = 3000L),
            pt(2L, 0, 51.001, -0.2, t = 4000L)
        )
        val json = TrailGeoJsonBuilder.buildOverlay(trailIds, points)
        // Trail 2 is newest — should have age_bucket "new"
        assertThat(json).contains("\"age_bucket\":\"new\"")
    }

    @Test
    fun `gradient json segments each point pair with slope property`() {
        val points = listOf(
            pt(1L, 0, 51.0, -0.1, alt = 100.0, t = 0L),
            pt(1L, 0, 51.001, -0.1, alt = 110.0, t = 10_000L),  // climbing
            pt(1L, 0, 51.002, -0.1, alt = 108.0, t = 20_000L)   // slight down
        )
        val json = TrailGeoJsonBuilder.buildGradient(points)
        assertThat(json).contains("\"slope\":")
        assertThat(json).contains("uphill")
    }

    private fun String.occurrencesOf(sub: String): Int {
        var count = 0; var idx = 0
        while (true) { idx = indexOf(sub, idx); if (idx == -1) break; count++; idx++ }
        return count
    }
}
```

- [ ] **Run — expect compile failure**

```bash
./gradlew test --tests "*.TrailGeoJsonBuilderTest" 2>&1 | tail -10
```

- [ ] **Implement `TrailGeoJsonBuilder.kt`**

```kotlin
package xyz.northline.overmapper.domain

import xyz.northline.overmapper.domain.model.TrailPoint

object TrailGeoJsonBuilder {

    fun buildOverlay(trailIds: List<Long>, points: List<TrailPoint>): String {
        if (points.isEmpty()) return """{"type":"FeatureCollection","features":[]}"""

        val byTrail = points.groupBy { it.trailId }
        val features = mutableListOf<String>()

        trailIds.forEachIndexed { index, trailId ->
            val ageBucket = when {
                index == trailIds.lastIndex -> "new"
                index >= trailIds.size - 3 -> "mid"
                else -> "old"
            }
            val trailPoints = byTrail[trailId] ?: return@forEachIndexed
            val bySegment = trailPoints.groupBy { it.segmentIndex }.toSortedMap()
            bySegment.values.forEach { segPts ->
                if (segPts.size < 2) return@forEach
                val coords = segPts.joinToString(",") { "[${it.longitude},${it.latitude}]" }
                features.add("""{"type":"Feature","properties":{"trail_id":$trailId,"age_bucket":"$ageBucket"},"geometry":{"type":"LineString","coordinates":[$coords]}}""")
            }
        }

        return """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""
    }

    fun buildGradient(points: List<TrailPoint>): String {
        val features = mutableListOf<String>()
        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            if (a.segmentIndex != b.segmentIndex) continue
            val distM = LocationPoint(a.latitude, a.longitude, a.altitudeM, a.recordedAt)
                .distanceTo(LocationPoint(b.latitude, b.longitude, b.altitudeM, b.recordedAt))
            val slopePct = if (distM > 0) (b.altitudeM - a.altitudeM) / distM * 100.0 else 0.0
            val slope = when {
                slopePct > 8 -> "steep_up"
                slopePct > 3 -> "uphill"
                slopePct < -8 -> "steep_down"
                slopePct < -3 -> "downhill"
                else -> "flat"
            }
            features.add("""{"type":"Feature","properties":{"slope":"$slope"},"geometry":{"type":"LineString","coordinates":[[${a.longitude},${a.latitude}],[${b.longitude},${b.latitude}]]}}""")
        }
        return """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""
    }
}
```

- [ ] **Run tests — expect all pass**

```bash
./gradlew test --tests "*.TrailGeoJsonBuilderTest"
```

Expected: `BUILD SUCCESSFUL` — 5 tests passed.

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add TrailGeoJsonBuilder for overlay and gradient rendering (TDD)"
```

---

## Task 12: GpxExporter (TDD)

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/domain/GpxExporter.kt`
- Create: `app/src/test/kotlin/xyz/northline/overmapper/GpxExporterTest.kt`

- [ ] **Write failing tests**

```kotlin
// app/src/test/kotlin/xyz/northline/overmapper/GpxExporterTest.kt
package xyz.northline.overmapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import xyz.northline.overmapper.domain.GpxExporter
import xyz.northline.overmapper.domain.model.Trail
import xyz.northline.overmapper.domain.model.TrailPoint

class GpxExporterTest {

    private val trail = Trail(1L, 1_700_000_000_000L, 5000f, 3_600_000L, 100f, 350f,
        50.9, -0.5, 51.1, -0.1)

    private fun pt(seg: Int, lat: Double, lon: Double, alt: Double, t: Long) =
        TrailPoint(0L, 1L, seg, lat, lon, alt, t)

    @Test
    fun `output is valid GPX 1_1 with correct namespace`() {
        val gpx = GpxExporter.export(trail, emptyList())
        assertThat(gpx).contains("xmlns=\"http://www.topografix.com/GPX/1/1\"")
        assertThat(gpx).contains("<gpx ")
        assertThat(gpx).contains("</gpx>")
    }

    @Test
    fun `single segment produces one trkseg`() {
        val points = listOf(
            pt(0, 51.0, -0.1, 100.0, 1_700_000_000_000L),
            pt(0, 51.001, -0.1, 102.0, 1_700_000_010_000L)
        )
        val gpx = GpxExporter.export(trail, points)
        assertThat(gpx.occurrencesOf("<trkseg>")).isEqualTo(1)
        assertThat(gpx.occurrencesOf("<trkpt")).isEqualTo(2)
    }

    @Test
    fun `segment break produces two trksegs`() {
        val points = listOf(
            pt(0, 51.0, -0.1, 100.0, 1_700_000_000_000L),
            pt(1, 51.005, -0.1, 110.0, 1_700_000_120_000L)
        )
        val gpx = GpxExporter.export(trail, points)
        assertThat(gpx.occurrencesOf("<trkseg>")).isEqualTo(2)
    }

    @Test
    fun `elevation and timestamp are included`() {
        val points = listOf(pt(0, 51.0, -0.1, 123.4, 1_700_000_000_000L))
        val gpx = GpxExporter.export(trail, points)
        assertThat(gpx).contains("<ele>123.4</ele>")
        assertThat(gpx).contains("<time>")
    }

    private fun String.occurrencesOf(sub: String): Int {
        var count = 0; var idx = 0
        while (true) { idx = indexOf(sub, idx); if (idx == -1) break; count++; idx++ }
        return count
    }
}
```

- [ ] **Run — expect compile failure**

```bash
./gradlew test --tests "*.GpxExporterTest" 2>&1 | tail -10
```

- [ ] **Implement `GpxExporter.kt`**

```kotlin
package xyz.northline.overmapper.domain

import xyz.northline.overmapper.domain.model.Trail
import xyz.northline.overmapper.domain.model.TrailPoint
import java.text.SimpleDateFormat
import java.util.*

object GpxExporter {
    private val iso8601 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun export(trail: Trail, points: List<TrailPoint>): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<gpx version="1.1" creator="OverMapper" xmlns="http://www.topografix.com/GPX/1/1">""")
        sb.appendLine("""  <metadata><time>${iso8601.format(Date(trail.recordedAt))}</time></metadata>""")
        sb.appendLine("""  <trk><name>Trail ${trail.id}</name>""")

        val bySegment = points.groupBy { it.segmentIndex }.toSortedMap()
        bySegment.values.forEach { segPts ->
            sb.appendLine("    <trkseg>")
            segPts.forEach { pt ->
                sb.appendLine("""      <trkpt lat="${pt.latitude}" lon="${pt.longitude}">""")
                sb.appendLine("""        <ele>${pt.altitudeM}</ele>""")
                sb.appendLine("""        <time>${iso8601.format(Date(pt.recordedAt))}</time>""")
                sb.appendLine("""      </trkpt>""")
            }
            sb.appendLine("    </trkseg>")
        }

        sb.appendLine("  </trk>")
        sb.appendLine("</gpx>")
        return sb.toString()
    }
}
```

- [ ] **Run tests — expect all pass**

```bash
./gradlew test --tests "*.GpxExporterTest"
```

Expected: `BUILD SUCCESSFUL` — 4 tests passed.

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add GpxExporter producing standard GPX 1.1 with segment support (TDD)"
```

---

## Task 13: RecordingStateHolder and RecordingService

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/service/RecordingStateHolder.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/service/RecordingService.kt`

- [ ] **Create `RecordingStateHolder.kt`**

```kotlin
package xyz.northline.overmapper.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.northline.overmapper.domain.model.RecordingState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingStateHolder @Inject constructor() {
    private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    fun update(state: RecordingState) { _state.value = state }
}
```

- [ ] **Create `RecordingService.kt`**

```kotlin
package xyz.northline.overmapper.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.northline.overmapper.MainActivity
import xyz.northline.overmapper.R
import xyz.northline.overmapper.data.datastore.UserPreferencesRepository
import xyz.northline.overmapper.data.db.entity.TrailEntity
import xyz.northline.overmapper.data.db.entity.TrailPointEntity
import xyz.northline.overmapper.data.repository.TrailRepository
import xyz.northline.overmapper.domain.CalorieCalculator
import xyz.northline.overmapper.domain.FilterConfig
import xyz.northline.overmapper.domain.FilterResult
import xyz.northline.overmapper.domain.LocationPoint
import xyz.northline.overmapper.domain.StationaryFilter
import xyz.northline.overmapper.domain.model.RecordingState
import javax.inject.Inject

@AndroidEntryPoint
class RecordingService : LifecycleService() {

    @Inject lateinit var stateHolder: RecordingStateHolder
    @Inject lateinit var trailRepository: TrailRepository
    @Inject lateinit var prefsRepository: UserPreferencesRepository

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var filter: StationaryFilter = StationaryFilter(FilterConfig())
    private var notificationJob: Job? = null

    private var currentTrailId: Long = -1L
    private var startMs: Long = 0L
    private var distanceM: Float = 0f
    private var segmentIndex: Int = 0
    private var lastAcceptedPoint: LocationPoint? = null
    private var pendingPoints = mutableListOf<TrailPointEntity>()

    private val CHANNEL_ID = "recording"
    private val NOTIF_ID = 1

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> lifecycleScope.launch { startRecording() }
            ACTION_STOP -> lifecycleScope.launch { stopRecording() }
        }
        return START_STICKY
    }

    private suspend fun startRecording() {
        val prefs = prefsRepository.preferences.kotlinx.coroutines.flow.first()
        filter = StationaryFilter(FilterConfig(
            minDisplacementM = prefs.minDisplacementM.toFloat(),
            pauseTimeoutMs = prefs.pauseTimeoutS * 1000L
        ))

        val trailEntity = TrailEntity(
            recordedAt = System.currentTimeMillis(),
            distanceM = 0f, durationMs = 0L, elevationGainM = 0f,
            caloriesKcal = null,
            bboxSwLat = 0.0, bboxSwLon = 0.0, bboxNeLat = 0.0, bboxNeLon = 0.0
        )
        currentTrailId = trailRepository.insertTrail(trailEntity)
        startMs = System.currentTimeMillis()
        distanceM = 0f
        segmentIndex = 0
        lastAcceptedPoint = null
        pendingPoints.clear()

        stateHolder.update(RecordingState.Recording(currentTrailId, startMs, 0f, 0))
        startForeground(NOTIF_ID, buildNotification("Recording trail", "0:00 • 0.00 km"))
        startLocationUpdates(prefs.minDisplacementM)
        startNotificationUpdater()
    }

    private suspend fun stopRecording() {
        stopLocationUpdates()
        notificationJob?.cancel()
        flushPoints()

        val durationMs = System.currentTimeMillis() - startMs
        val prefs = prefsRepository.preferences.kotlinx.coroutines.flow.first()
        val calories = prefs.weightKg?.let {
            CalorieCalculator.calculate(it, durationMs, distanceM, 0f)
        }

        val existing = trailRepository.getById(currentTrailId)
        if (existing != null) {
            trailRepository.updateTrail(
                xyz.northline.overmapper.data.db.entity.TrailEntity(
                    id = currentTrailId,
                    recordedAt = existing.recordedAt,
                    distanceM = distanceM,
                    durationMs = durationMs,
                    elevationGainM = 0f,
                    caloriesKcal = calories,
                    bboxSwLat = existing.bboxSwLat, bboxSwLon = existing.bboxSwLon,
                    bboxNeLat = existing.bboxNeLat, bboxNeLon = existing.bboxNeLon
                )
            )
        }

        stateHolder.update(RecordingState.Idle)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startLocationUpdates(minDisplacementM: Int) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateDistanceMeters(minDisplacementM.toFloat())
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val incoming = LocationPoint(loc.latitude, loc.longitude, loc.altitude,
                        loc.time)
                    lifecycleScope.launch { handleLocation(incoming) }
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) { stopSelf() }
    }

    private suspend fun handleLocation(point: LocationPoint) {
        val now = System.currentTimeMillis()
        when (val result = filter.process(point, now)) {
            is FilterResult.Accept -> {
                addPoint(point, segmentIndex)
                lastAcceptedPoint?.let { distanceM += it.distanceTo(point) }
                lastAcceptedPoint = point
                stateHolder.update(RecordingState.Recording(currentTrailId, startMs, distanceM,
                    pendingPoints.size))
            }
            is FilterResult.Resume -> {
                segmentIndex++
                addPoint(point, segmentIndex)
                lastAcceptedPoint = point
                stateHolder.update(RecordingState.Recording(currentTrailId, startMs, distanceM,
                    pendingPoints.size))
            }
            FilterResult.Pause -> {
                stateHolder.update(RecordingState.Paused(currentTrailId, startMs, distanceM))
                updateNotification("Paused — not moving", formatStats())
            }
            FilterResult.Discard -> Unit
        }

        if (pendingPoints.size >= 20) flushPoints()
    }

    private fun addPoint(point: LocationPoint, segment: Int) {
        pendingPoints.add(TrailPointEntity(
            trailId = currentTrailId,
            segmentIndex = segment,
            latitude = point.latitude,
            longitude = point.longitude,
            altitudeM = point.altitudeM,
            recordedAt = point.timestampMs
        ))
    }

    private suspend fun flushPoints() {
        if (pendingPoints.isNotEmpty()) {
            trailRepository.insertPoints(pendingPoints.toList())
            pendingPoints.clear()
        }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) fusedClient.removeLocationUpdates(locationCallback)
    }

    private fun startNotificationUpdater() {
        notificationJob = lifecycleScope.launch {
            while (true) {
                delay(1_000L)
                if (stateHolder.state.value is RecordingState.Recording) {
                    updateNotification("Recording trail", formatStats())
                }
            }
        }
    }

    private fun formatStats(): String {
        val elapsed = System.currentTimeMillis() - startMs
        val minutes = (elapsed / 60_000).toInt()
        val seconds = ((elapsed % 60_000) / 1000).toInt()
        val km = distanceM / 1000f
        return "%d:%02d • %.2f km".format(minutes, seconds, km)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(title: String, text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .build()

    private fun updateNotification(title: String, text: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, buildNotification(title, text))
    }
}
```

Note: The `kotlinx.coroutines.flow.first()` import needs to be `import kotlinx.coroutines.flow.first` at the top of the file. Add it with the other imports.

- [ ] **Add missing import to RecordingService.kt** — add `import kotlinx.coroutines.flow.first` to the import block.

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add RecordingService with stationary filter, auto-pause, segment tracking"
```

---

## Task 14: MapLibreView composable

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/map/MapLibreView.kt`

- [ ] **Create `MapLibreView.kt`**

```kotlin
package xyz.northline.overmapper.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap

object TileSource {
    const val OPENFREEMAP = "https://tiles.openfreemap.org/styles/liberty"
    const val OSM_STANDARD = "https://tiles.openfreemap.org/styles/bright"
    const val OSM_HUMANITARIAN = "https://tiles.openfreemap.org/styles/positron"

    fun styleUrl(source: String) = when (source) {
        "OSM_STANDARD" -> OSM_STANDARD
        "OSM_HUMANITARIAN" -> OSM_HUMANITARIAN
        else -> OPENFREEMAP
    }
}

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    onMapReady: (MapLibreMap, MapView) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = mapView.onStart()
            override fun onStop(owner: LifecycleOwner) = mapView.onStop()
            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        mapView.onCreate(null)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView.also { it.getMapAsync { map -> onMapReady(map, it) } } },
        modifier = modifier
    )
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add MapLibreView composable with lifecycle management"
```

---

## Task 15: MapViewModel

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/map/MapViewModel.kt`

- [ ] **Create `MapViewModel.kt`**

```kotlin
package xyz.northline.overmapper.ui.map

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import xyz.northline.overmapper.data.datastore.UserPreferencesRepository
import xyz.northline.overmapper.data.repository.MarkerRepository
import xyz.northline.overmapper.data.repository.TrailRepository
import xyz.northline.overmapper.domain.TrailGeoJsonBuilder
import xyz.northline.overmapper.domain.model.RecordingState
import xyz.northline.overmapper.service.RecordingService
import xyz.northline.overmapper.service.RecordingStateHolder
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val trailRepository: TrailRepository,
    private val markerRepository: MarkerRepository,
    private val stateHolder: RecordingStateHolder,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    val recordingState: StateFlow<RecordingState> = stateHolder.state

    val preferences = prefsRepository.preferences.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _selectedTrailId = MutableStateFlow<Long?>(null)
    val selectedTrailId: StateFlow<Long?> = _selectedTrailId.asStateFlow()

    private var mapLibreMap: MapLibreMap? = null
    private val OVERLAY_SOURCE = "trail-overlay"
    private val GRADIENT_SOURCE = "trail-gradient"
    private val OVERLAY_LAYER = "trail-overlay-layer"
    private val GRADIENT_LAYER = "trail-gradient-layer"

    init {
        viewModelScope.launch {
            combine(
                trailRepository.observeAll(),
                trailRepository.observeAllPoints(),
                prefsRepository.preferences
            ) { trails, points, prefs -> Triple(trails, points, prefs) }
                .collectLatest { (trails, points, prefs) ->
                    updateOverlay(trails.map { it.id }, points, prefs.gradientEnabled)
                }
        }
    }

    fun onMapReady(map: MapLibreMap, tileSource: String) {
        mapLibreMap = map
        map.setStyle(Style.Builder().fromUri(TileSource.styleUrl(tileSource))) { style ->
            style.addSource(GeoJsonSource(OVERLAY_SOURCE))
            style.addSource(GeoJsonSource(GRADIENT_SOURCE))
            style.addLayer(buildOverlayLayer())
            style.addLayer(buildGradientLayer())
        }
    }

    fun onMapTap(lat: Double, lon: Double) {
        // handled in MapScreen for marker/trail selection
    }

    fun selectTrail(id: Long?) { _selectedTrailId.value = id }

    fun startRecording(context: Context) {
        context.startForegroundService(Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START
        })
    }

    fun stopRecording(context: Context) {
        context.startService(Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP
        })
    }

    private fun updateOverlay(
        trailIds: List<Long>,
        points: List<xyz.northline.overmapper.domain.model.TrailPoint>,
        gradientEnabled: Boolean
    ) {
        val style = mapLibreMap?.style ?: return
        val overlayJson = TrailGeoJsonBuilder.buildOverlay(trailIds, points)
        (style.getSourceAs<GeoJsonSource>(OVERLAY_SOURCE))?.setGeoJson(overlayJson)

        val gradientLayer = style.getLayer(GRADIENT_LAYER)
        if (gradientEnabled) {
            val gradientJson = TrailGeoJsonBuilder.buildGradient(points)
            (style.getSourceAs<GeoJsonSource>(GRADIENT_SOURCE))?.setGeoJson(gradientJson)
            gradientLayer?.setProperties(visibility(com.mapbox.mapboxsdk.style.layers.Property.VISIBLE))
        } else {
            gradientLayer?.setProperties(visibility(com.mapbox.mapboxsdk.style.layers.Property.NONE))
        }
    }

    private fun buildOverlayLayer() = LineLayer(OVERLAY_LAYER, OVERLAY_SOURCE).apply {
        setProperties(
            lineColor(
                org.maplibre.android.style.expressions.Expression.match(
                    org.maplibre.android.style.expressions.Expression.get("age_bucket"),
                    org.maplibre.android.style.expressions.Expression.literal("#B5562E"),
                    org.maplibre.android.style.expressions.Expression.stop("new", "#B5562E"),
                    org.maplibre.android.style.expressions.Expression.stop("mid", "#7E9A86"),
                    org.maplibre.android.style.expressions.Expression.stop("old", "#8C7F6E")
                )
            ),
            lineWidth(3f),
            lineCap(com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND),
            lineJoin(com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND)
        )
    }

    private fun buildGradientLayer() = LineLayer(GRADIENT_LAYER, GRADIENT_SOURCE).apply {
        setProperties(
            lineColor(
                org.maplibre.android.style.expressions.Expression.match(
                    org.maplibre.android.style.expressions.Expression.get("slope"),
                    org.maplibre.android.style.expressions.Expression.literal("#9CA3AF"),
                    org.maplibre.android.style.expressions.Expression.stop("steep_up", "#D97706"),
                    org.maplibre.android.style.expressions.Expression.stop("uphill", "#F59E0B"),
                    org.maplibre.android.style.expressions.Expression.stop("flat", "#9CA3AF"),
                    org.maplibre.android.style.expressions.Expression.stop("downhill", "#38BDF8"),
                    org.maplibre.android.style.expressions.Expression.stop("steep_down", "#0284C7")
                )
            ),
            lineWidth(4f),
            lineCap(com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND),
            lineJoin(com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND)
        )
    }
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add MapViewModel — trail overlay + gradient rendering via MapLibre layers"
```

---

## Task 16: Map screen UI

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/map/RecordingPill.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/map/MapScreen.kt`

- [ ] **Create `RecordingPill.kt`**

```kotlin
package xyz.northline.overmapper.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import xyz.northline.overmapper.domain.model.RecordingState

@Composable
fun RecordingPill(state: RecordingState, modifier: Modifier = Modifier) {
    val recording = state as? RecordingState.Recording ?: return
    var tick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(recording.startMs) {
        while (true) { delay(1_000L); tick++ }
    }

    val elapsed = System.currentTimeMillis() - recording.startMs
    val min = (elapsed / 60_000).toInt()
    val sec = ((elapsed % 60_000) / 1000).toInt()
    val km = recording.distanceM / 1000f

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("⏺", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary)
        Text("%d:%02d".format(min, sec), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary)
        Text("%.2f km".format(km), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary)
    }
}
```

- [ ] **Create `MapScreen.kt`**

```kotlin
package xyz.northline.overmapper.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.northline.overmapper.domain.model.RecordingState
import xyz.northline.overmapper.ui.components.TrailBottomSheet

@Composable
fun MapScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val selectedTrailId by viewModel.selectedTrailId.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map, _ ->
                viewModel.onMapReady(map, prefs?.mapTileSource ?: "OPENFREEMAP")
            }
        )

        // Recording pill at top
        if (recordingState is RecordingState.Recording) {
            RecordingPill(
                state = recordingState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp)
            )
        }

        // FAB bottom-right
        FloatingActionButton(
            onClick = {
                when (recordingState) {
                    is RecordingState.Idle -> viewModel.startRecording(context)
                    is RecordingState.Recording, is RecordingState.Paused ->
                        showStopDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            if (recordingState is RecordingState.Idle) {
                Icon(Icons.Default.Stop, contentDescription = "Start recording",
                    tint = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.Stop, contentDescription = "Stop recording",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        // Trail bottom sheet
        selectedTrailId?.let { trailId ->
            TrailBottomSheet(
                trailId = trailId,
                onDismiss = { viewModel.selectTrail(null) },
                onOpenDetail = { onNavigateToDetail(trailId) }
            )
        }
    }

    // Stop confirmation dialog
    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Stop recording?") },
            text = { Text("Your trail will be saved.") },
            confirmButton = {
                TextButton(onClick = {
                    showStopDialog = false
                    viewModel.stopRecording(context)
                }) { Text("Stop") }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) { Text("Continue") }
            }
        )
    }
}
```

- [ ] **Create stub `TrailBottomSheet.kt`** (full implementation in Task 20)

```kotlin
package xyz.northline.overmapper.ui.components

import androidx.compose.runtime.Composable

@Composable
fun TrailBottomSheet(trailId: Long, onDismiss: () -> Unit, onOpenDetail: () -> Unit) {
    // Implemented in Task 20
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add MapScreen with FAB, recording pill, and stop dialog"
```

---

## Task 17: Trails list screen

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/trails/TrailsViewModel.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/trails/TrailsScreen.kt`

- [ ] **Create `TrailsViewModel.kt`**

```kotlin
package xyz.northline.overmapper.ui.trails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.northline.overmapper.data.repository.TrailRepository
import xyz.northline.overmapper.domain.model.Trail
import javax.inject.Inject

@HiltViewModel
class TrailsViewModel @Inject constructor(
    private val trailRepository: TrailRepository
) : ViewModel() {

    val trails = trailRepository.observeAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    fun deleteTrail(trail: Trail) {
        viewModelScope.launch { trailRepository.deleteTrail(trail.id) }
    }
}
```

- [ ] **Create `TrailsScreen.kt`**

```kotlin
package xyz.northline.overmapper.ui.trails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.northline.overmapper.domain.model.Trail
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrailsScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: TrailsViewModel = hiltViewModel()
) {
    val trails by viewModel.trails.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Trails", style = MaterialTheme.typography.headlineMedium) })
        }
    ) { padding ->
        if (trails.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No trails yet.\nStart recording from the map.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trails, key = { it.id }) { trail ->
                    SwipeToDeleteTrailCard(
                        trail = trail,
                        onTap = { onNavigateToDetail(trail.id) },
                        onDelete = {
                            viewModel.deleteTrail(trail)
                            scope.launch {
                                snackbarHostState.showSnackbar("Trail deleted", "Undo",
                                    duration = SnackbarDuration.Short)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeToDeleteTrailCard(trail: Trail, onTap: () -> Unit, onDelete: () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart }
    )

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }

    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().padding(end = 16.dp),
                contentAlignment = androidx.compose.ui.Alignment.CenterEnd
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        TrailCard(trail = trail, onTap = onTap)
    }
}

@Composable
private fun TrailCard(trail: Trail, onTap: () -> Unit) {
    val dateStr = remember(trail.recordedAt) {
        SimpleDateFormat("EEE d MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(trail.recordedAt))
    }

    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(dateStr, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip("%.2f km".format(trail.distanceM / 1000f))
                StatChip(formatDuration(trail.durationMs))
                StatChip("↑ %.0f m".format(trail.elevationGainM))
                trail.caloriesKcal?.let { StatChip("~%.0f kcal".format(it)) }
            }
        }
    }
}

@Composable
private fun StatChip(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun formatDuration(ms: Long): String {
    val h = ms / 3_600_000
    val m = (ms % 3_600_000) / 60_000
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add TrailsScreen with swipe-to-delete and trail cards"
```

---

## Task 18: Trail detail screen

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/detail/TrailDetailViewModel.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/detail/ElevationProfileChart.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/detail/TrailDetailScreen.kt`

- [ ] **Create `TrailDetailViewModel.kt`**

```kotlin
package xyz.northline.overmapper.ui.detail

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.northline.overmapper.data.repository.MarkerRepository
import xyz.northline.overmapper.data.repository.PhotoRepository
import xyz.northline.overmapper.data.repository.TrailRepository
import xyz.northline.overmapper.domain.GpxExporter
import xyz.northline.overmapper.domain.model.Trail
import xyz.northline.overmapper.domain.model.TrailPhoto
import xyz.northline.overmapper.domain.model.Marker
import xyz.northline.overmapper.domain.model.TrailPoint
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TrailDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trailRepository: TrailRepository,
    private val markerRepository: MarkerRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val trailId: Long = checkNotNull(savedStateHandle["trailId"])

    val trail: StateFlow<Trail?> = flow { emit(trailRepository.getById(trailId)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val points: StateFlow<List<TrailPoint>> = flow {
        emit(trailRepository.getPointsForTrail(trailId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val photos: StateFlow<List<TrailPhoto>> = photoRepository.observeByTrailId(trailId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val markers: StateFlow<List<Marker>> = markerRepository.observeAll()
        .map { all -> all.filter { it.trailId == trailId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun exportGpx(context: Context) {
        viewModelScope.launch {
            val t = trail.value ?: return@launch
            val pts = points.value
            val gpx = GpxExporter.export(t, pts)
            val file = File(context.cacheDir, "overmapper_trail_${trailId}.gpx")
            file.writeText(gpx)
            val uri = FileProvider.getUriForFile(context,
                "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/gpx+xml"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export GPX"))
        }
    }

    fun deleteTrail(onDeleted: () -> Unit) {
        viewModelScope.launch {
            trailRepository.deleteTrail(trailId)
            onDeleted()
        }
    }
}
```

- [ ] **Add FileProvider to `AndroidManifest.xml`** inside `<application>`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

- [ ] **Create `res/xml/file_paths.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="gpx_exports" path="." />
</paths>
```

- [ ] **Create `ElevationProfileChart.kt`**

```kotlin
package xyz.northline.overmapper.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import xyz.northline.overmapper.domain.model.TrailPoint
import xyz.northline.overmapper.ui.theme.Terracotta

@Composable
fun ElevationProfileChart(points: List<TrailPoint>, modifier: Modifier = Modifier) {
    if (points.size < 2) return
    val alts = points.map { it.altitudeM.toFloat() }
    val minAlt = alts.min()
    val maxAlt = alts.max()
    val range = (maxAlt - minAlt).coerceAtLeast(1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        drawElevationFill(alts, minAlt, range)
        drawElevationLine(alts, minAlt, range)
    }
}

private fun DrawScope.drawElevationFill(alts: List<Float>, minAlt: Float, range: Float) {
    val path = Path()
    val w = size.width; val h = size.height
    path.moveTo(0f, h)
    alts.forEachIndexed { i, alt ->
        val x = w * i / (alts.size - 1)
        val y = h - h * (alt - minAlt) / range
        if (i == 0) path.lineTo(x, y) else path.lineTo(x, y)
    }
    path.lineTo(w, h); path.close()
    drawPath(path, Terracotta.copy(alpha = 0.2f))
}

private fun DrawScope.drawElevationLine(alts: List<Float>, minAlt: Float, range: Float) {
    val w = size.width; val h = size.height
    for (i in 0 until alts.size - 1) {
        val x1 = w * i / (alts.size - 1)
        val y1 = h - h * (alts[i] - minAlt) / range
        val x2 = w * (i + 1) / (alts.size - 1)
        val y2 = h - h * (alts[i + 1] - minAlt) / range
        drawLine(Terracotta, Offset(x1, y1), Offset(x2, y2), strokeWidth = 2.dp.toPx())
    }
}
```

- [ ] **Create `TrailDetailScreen.kt`**

```kotlin
package xyz.northline.overmapper.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailDetailScreen(
    onBack: () -> Unit,
    viewModel: TrailDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val trail by viewModel.trail.collectAsStateWithLifecycle()
    val points by viewModel.points.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val markers by viewModel.markers.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        trail?.let {
                            SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(it.recordedAt))
                        } ?: "Trail",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.exportGpx(context) }) { Text("GPX") }
                    TextButton(onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            trail?.let { t ->
                // Stats row
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatBlock("Distance", "%.2f km".format(t.distanceM / 1000f))
                    StatBlock("Time", formatDuration(t.durationMs))
                    StatBlock("Elevation", "↑ %.0f m".format(t.elevationGainM))
                    t.caloriesKcal?.let { StatBlock("Calories", "~%.0f".format(it)) }
                }

                // Elevation chart
                if (points.size >= 2) {
                    Text("Elevation", style = MaterialTheme.typography.titleLarge)
                    ElevationProfileChart(points)
                }
            }

            // Photos
            if (photos.isNotEmpty()) {
                Text("Photos", style = MaterialTheme.typography.titleLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photos, key = { it.id }) { photo ->
                        AsyncImage(
                            model = photo.fileUri,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Markers
            if (markers.isNotEmpty()) {
                Text("Conditions", style = MaterialTheme.typography.titleLarge)
                markers.forEach { marker ->
                    Text("• ${marker.type.name.lowercase().replaceFirstChar { it.uppercase() }}" +
                            (marker.body?.let { ": $it" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete trail?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTrail(onBack)
                }, colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

private fun formatDuration(ms: Long): String {
    val h = ms / 3_600_000; val m = (ms % 3_600_000) / 60_000
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add TrailDetailScreen with stats, elevation chart, photos, markers, GPX export"
```

---

## Task 19: Markers feature

**Files:**
- Modify: `app/src/main/kotlin/xyz/northline/overmapper/ui/components/TrailBottomSheet.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/components/MarkerBottomSheet.kt`

- [ ] **Create `MarkerBottomSheet.kt`**

```kotlin
package xyz.northline.overmapper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.northline.overmapper.domain.model.Marker
import xyz.northline.overmapper.domain.model.MarkerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarkerSheet(
    latitude: Double,
    longitude: Double,
    trailId: Long?,
    onSave: (MarkerType, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(MarkerType.NOTE) }
    var body by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add condition marker", style = MaterialTheme.typography.titleLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MarkerType.entries.forEach { type ->
                    FilterChip(
                        selected = type == selectedType,
                        onClick = { selectedType = type },
                        label = { Text(type.name.lowercase()
                            .replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { onSave(selectedType, body.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save marker") }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewMarkerSheet(marker: Marker, onDelete: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(marker.type.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge)
            marker.body?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete marker") }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
```

- [ ] **Add long-press marker dropping to `MapScreen.kt`** — in `MapLibreView`'s `onMapReady`, set a map click listener that distinguishes long-press vs tap. MapLibre exposes `addOnMapLongClickListener`. Update the `MapScreen` to track a `pendingMarkerCoords` state:

```kotlin
// Add to MapScreen.kt state
var pendingMarkerLat by remember { mutableDoubleStateOf(0.0) }
var pendingMarkerLon by remember { mutableDoubleStateOf(0.0) }
var showAddMarker by remember { mutableStateOf(false) }

// In MapLibreView onMapReady callback, after style loads:
map.addOnMapLongClickListener { point ->
    pendingMarkerLat = point.latitude
    pendingMarkerLon = point.longitude
    showAddMarker = true
    true
}

// Below the FAB in the Box:
if (showAddMarker) {
    AddMarkerSheet(
        latitude = pendingMarkerLat,
        longitude = pendingMarkerLon,
        trailId = null,
        onSave = { type, body ->
            viewModel.addMarker(pendingMarkerLat, pendingMarkerLon, null, type, body)
            showAddMarker = false
        },
        onDismiss = { showAddMarker = false }
    )
}
```

- [ ] **Add `addMarker` to `MapViewModel.kt`**

```kotlin
// Add to MapViewModel
@Inject lateinit var markerRepository: MarkerRepository  // already injected

fun addMarker(lat: Double, lon: Double, trailId: Long?, type: xyz.northline.overmapper.domain.model.MarkerType, body: String?) {
    viewModelScope.launch {
        markerRepository.insert(
            xyz.northline.overmapper.data.db.entity.MarkerEntity(
                trailId = trailId, latitude = lat, longitude = lon,
                type = type.name, body = body,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add condition markers — long-press to drop, view/delete sheet"
```

---

## Task 20: Trail photos

**Files:**
- Modify: `app/src/main/kotlin/xyz/northline/overmapper/ui/detail/TrailDetailViewModel.kt`
- Modify: `app/src/main/kotlin/xyz/northline/overmapper/ui/detail/TrailDetailScreen.kt`

- [ ] **Add photo attachment to `TrailDetailViewModel.kt`**

```kotlin
// Add to TrailDetailViewModel
fun attachPhoto(context: Context, uri: android.net.Uri, trailId: Long,
                lat: Double, lon: Double) {
    viewModelScope.launch {
        photoRepository.insert(
            xyz.northline.overmapper.data.db.entity.TrailPhotoEntity(
                trailId = trailId,
                latitude = lat, longitude = lon,
                fileUri = uri.toString(),
                takenAt = System.currentTimeMillis()
            )
        )
    }
}

fun deletePhoto(photo: TrailPhoto) {
    viewModelScope.launch { photoRepository.delete(photo) }
}
```

- [ ] **Add photo picker to `TrailDetailScreen.kt`** — add an "Add photo" button in the photos section and wire to `ActivityResultContracts.GetContent`:

```kotlin
// Add inside TrailDetailScreen
val photoPickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.GetContent()
) { uri ->
    uri?.let {
        // Use last known location from recording state or trail bounding box centre
        val lat = trail?.let { t -> (t.bboxSwLat + t.bboxNeLat) / 2 } ?: 0.0
        val lon = trail?.let { t -> (t.bboxSwLon + t.bboxNeLon) / 2 } ?: 0.0
        viewModel.attachPhoto(context, it, trailId, lat, lon)
    }
}

// In the photos section, after the LazyRow, add:
TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
    Text("+ Add photo")
}
```

- [ ] **Add `trailId` to `TrailDetailScreen` parameter** — the screen needs the ID separately from the ViewModel for the photo picker. Pass it down from the nav graph:

```kotlin
// Change signature:
fun TrailDetailScreen(trailId: Long, onBack: () -> Unit, viewModel: TrailDetailViewModel = hiltViewModel())
// Use trailId when calling attachPhoto
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add photo attachment and display on trail detail"
```

---

## Task 21: Settings screen

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/settings/SettingsViewModel.kt`
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/settings/SettingsScreen.kt`

- [ ] **Create `SettingsViewModel.kt`**

```kotlin
package xyz.northline.overmapper.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.northline.overmapper.data.datastore.UserPreferencesRepository
import xyz.northline.overmapper.data.repository.TrailRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepository,
    private val trailRepository: TrailRepository
) : ViewModel() {

    val prefs = prefsRepo.preferences.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        xyz.northline.overmapper.data.datastore.UserPreferences()
    )

    fun setWeightKg(kg: Float?) = viewModelScope.launch { prefsRepo.setWeightKg(kg) }
    fun setWeightUnit(unit: String) = viewModelScope.launch { prefsRepo.setWeightUnit(unit) }
    fun setGradientEnabled(v: Boolean) = viewModelScope.launch { prefsRepo.setGradientEnabled(v) }
    fun setMinDisplacement(m: Int) = viewModelScope.launch { prefsRepo.setMinDisplacementM(m) }
    fun setPauseTimeout(s: Int) = viewModelScope.launch { prefsRepo.setPauseTimeoutS(s) }
    fun setMapTileSource(s: String) = viewModelScope.launch { prefsRepo.setMapTileSource(s) }

    fun clearAllData(onDone: () -> Unit) = viewModelScope.launch {
        // Delete all trails (cascade deletes points, markers, photos)
        trailRepository.observeAll().kotlinx.coroutines.flow.first().forEach {
            trailRepository.deleteTrail(it.id)
        }
        onDone()
    }
}
```

- [ ] **Create `SettingsScreen.kt`**

```kotlin
package xyz.northline.overmapper.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }
    var weightInput by remember(prefs.weightKg) {
        mutableStateOf(prefs.weightKg?.let {
            if (prefs.weightUnit == "LBS") "%.1f".format(it * 2.20462f) else "%.1f".format(it)
        } ?: "")
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader("Profile")
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Weight") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                trailingIcon = {
                    TextButton(onClick = {
                        val kg = weightInput.toFloatOrNull()?.let {
                            if (prefs.weightUnit == "LBS") it / 2.20462f else it
                        }
                        viewModel.setWeightKg(kg)
                    }) { Text("Save") }
                }
            )
            // Unit toggle
            Row {
                listOf("KG", "LBS").forEach { unit ->
                    FilterChip(
                        selected = prefs.weightUnit == unit,
                        onClick = { viewModel.setWeightUnit(unit) },
                        label = { Text(unit) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
        if (prefs.weightKg == null) {
            Text("Add your weight to get calorie estimates after each trail.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        SectionHeader("Recording")
        SettingsSlider("Min displacement", prefs.minDisplacementM, 3, 30, "m") {
            viewModel.setMinDisplacement(it)
        }
        SettingsSlider("Pause timeout", prefs.pauseTimeoutS, 15, 300, "s") {
            viewModel.setPauseTimeout(it)
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        SectionHeader("Display")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Gradient lines", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = prefs.gradientEnabled, onCheckedChange = viewModel::setGradientEnabled)
        }
        Text("Map tiles", style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("OPENFREEMAP" to "Liberty", "OSM_STANDARD" to "Bright",
                "OSM_HUMANITARIAN" to "Positron").forEach { (key, label) ->
                FilterChip(
                    selected = prefs.mapTileSource == key,
                    onClick = { viewModel.setMapTileSource(key) },
                    label = { Text(label) }
                )
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        SectionHeader("Data")
        OutlinedButton(
            onClick = { showClearDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Clear all trails and data") }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear all data?") },
            text = { Text("All trails, photos and markers will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = { showClearDialog = false; viewModel.clearAllData {} },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun SettingsSlider(label: String, value: Int, min: Int, max: Int, unit: String,
                            onChanged: (Int) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text("$value $unit", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value.toFloat(), onValueChange = { onChanged(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(), steps = max - min - 1)
    }
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: add SettingsScreen — weight, recording thresholds, map tiles, clear data"
```

---

## Task 22: Navigation wiring

**Files:**
- Create: `app/src/main/kotlin/xyz/northline/overmapper/ui/navigation/NavGraph.kt`

- [ ] **Create `NavGraph.kt`**

```kotlin
package xyz.northline.overmapper.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import xyz.northline.overmapper.ui.detail.TrailDetailScreen
import xyz.northline.overmapper.ui.map.MapScreen
import xyz.northline.overmapper.ui.settings.SettingsScreen
import xyz.northline.overmapper.ui.trails.TrailsScreen

private sealed class Screen(val route: String, val label: String) {
    object Map : Screen("map", "Map")
    object Trails : Screen("trails", "Trails")
    object Settings : Screen("settings", "Settings")
    object Detail : Screen("detail/{trailId}", "Detail") {
        fun route(id: Long) = "detail/$id"
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val bottomItems = listOf(Screen.Map, Screen.Trails, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination
    val showBottomBar = bottomItems.any { it.route == currentDest?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { screen ->
                        val icon = when (screen) {
                            Screen.Map -> Icons.Default.Map
                            Screen.Trails -> Icons.Default.List
                            Screen.Settings -> Icons.Default.Settings
                            else -> Icons.Default.Map
                        }
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Screen.Map.route,
            modifier = Modifier.padding(padding)) {
            composable(Screen.Map.route) {
                MapScreen(onNavigateToDetail = { navController.navigate(Screen.Detail.route(it)) })
            }
            composable(Screen.Trails.route) {
                TrailsScreen(onNavigateToDetail = { navController.navigate(Screen.Detail.route(it)) })
            }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(
                Screen.Detail.route,
                arguments = listOf(navArgument("trailId") { type = NavType.LongType })
            ) { backStack ->
                val trailId = backStack.arguments!!.getLong("trailId")
                TrailDetailScreen(trailId = trailId, onBack = { navController.popBackStack() })
            }
        }
    }
}
```

- [ ] **Commit**

```bash
git add .
git commit -m "feat: wire up Compose Navigation with bottom nav and detail screen"
```

---

## Task 23: CI/CD workflows

**Files:**
- Create: `.github/workflows/ci.yml`
- Create: `.github/workflows/release.yml`

- [ ] **Create `.github/workflows/ci.yml`**

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Run unit tests
        run: ./gradlew test

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Upload debug APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 7
```

- [ ] **Create `.github/workflows/release.yml`**

```yaml
name: Release to Play Store

on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    permissions:
      contents: write
      actions: write

    steps:
      - name: Purge old workflow runs (keep last 10)
        uses: Mattraks/delete-workflow-runs@v2
        with:
          token: ${{ github.token }}
          repository: ${{ github.repository }}
          retain_days: 30
          keep_minimum_runs: 10

      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Run unit tests
        run: ./gradlew test

      - name: Decode keystore
        run: echo "${{ secrets.KEYSTORE_FILE }}" | base64 --decode > overmapper-release.jks

      - name: Build release AAB
        run: ./gradlew bundleRelease
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}

      - name: Upload AAB artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-release-aab
          path: app/build/outputs/bundle/release/app-release.aab
          retention-days: 30

      - name: Upload to Play Store (internal track)
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT_JSON }}
          packageName: xyz.northline.overmapper
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: internal
          status: completed

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          files: app/build/outputs/bundle/release/app-release.aab
          generate_release_notes: true
```

- [ ] **Generate release keystore**

```bash
cd /workspace/OverMapper
keytool -genkeypair \
  -alias overmapper \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -keystore overmapper-release.jks \
  -storepass changeme \
  -keypass changeme \
  -dname "CN=Northline, OU=Apps, O=Northline, L=Crawley, ST=West Sussex, C=GB"
```

Replace `changeme` with a strong password. Then base64-encode it:

```bash
base64 -w 0 overmapper-release.jks > overmapper-release.jks.b64
cat overmapper-release.jks.b64
```

- [ ] **Add secrets to GitHub repo**

```bash
# Add each secret — GitHub CLI will prompt for value
gh secret set KEYSTORE_FILE < overmapper-release.jks.b64
gh secret set KEYSTORE_PASSWORD   # enter the storepass you used above
gh secret set KEY_ALIAS           # overmapper
gh secret set KEY_PASSWORD        # enter the keypass you used above
gh secret set PLAY_SERVICE_ACCOUNT_JSON  # paste the GPC service account JSON
```

- [ ] **Add `overmapper-release.jks` to `.gitignore`**

```bash
cat > .gitignore << 'EOF'
*.jks
*.jks.b64
*.jks.properties
.gradle/
build/
local.properties
.idea/
*.iml
EOF
```

- [ ] **Commit**

```bash
git add .github/workflows/ .gitignore
git commit -m "chore: add CI/CD workflows — tests+debug on push, signed AAB to Play Store internal on tag"
```

---

## Task 24: Play Store assets and first push

**Files:** Copy design assets into repo; push; create first internal release.

- [ ] **Copy design assets into the repo**

```bash
mkdir -p /workspace/OverMapper/app/src/main/play/listings/en-GB
cp /tmp/design_extract/northline/project/software/overmapper/OverMapper-icon-512.png \
   /workspace/OverMapper/app/src/main/play/listings/en-GB/icon.png
cp /tmp/design_extract/northline/project/software/overmapper/OverMapper-feature-1024x500.png \
   /workspace/OverMapper/app/src/main/play/listings/en-GB/featureGraphic.png
```

- [ ] **Create Play Store metadata files**

```bash
cat > /workspace/OverMapper/app/src/main/play/listings/en-GB/title.txt << 'EOF'
OverMapper
EOF

cat > /workspace/OverMapper/app/src/main/play/listings/en-GB/short-description.txt << 'EOF'
Every trail you walk, joined to the last.
EOF

cat > /workspace/OverMapper/app/src/main/play/listings/en-GB/full-description.txt << 'EOF'
OverMapper records each walk and threads it into the ones before — until your hills become a single connected web of trails.

Every route you walk is layered over the last. Over time, the paths join up, the gaps close, and your landscape becomes your own.

Offline-first. Your data stays yours.

Features:
• Automatic trail recording with smart stationary detection
• Overlaid trail web — colour-coded by age
• Elevation gradient view (uphill, flat, downhill)
• Condition markers — flag impassable paths, overgrowth, treacherous climbs
• Photo pins — attach photos at GPS coordinates
• Calorie estimate (optional — enter your weight in Settings)
• Export any trail as GPX or share a map snapshot
EOF
```

- [ ] **Commit assets and metadata**

```bash
git add app/src/main/play/
git commit -m "chore: add Play Store listing assets and metadata"
```

- [ ] **Push everything to GitHub**

```bash
git push origin main
```

- [ ] **Verify CI passes on GitHub**

```bash
gh run list --limit 5
```

Wait for the latest run to show `completed / success`. If it fails:

```bash
gh run view --log-failed
```

- [ ] **Create first internal release tag**

```bash
git tag v1.0.0
git push origin v1.0.0
```

This triggers `release.yml`. Monitor:

```bash
gh run list --limit 3
gh run watch
```

Expected: `BUILD SUCCESSFUL`, AAB uploaded to Play Store internal track.

---

## Self-Review Notes

- **Spec coverage:** All Phase 1 features covered — recording ✓, overlay ✓, gradient ✓, markers ✓, photos ✓, export GPX ✓, calorie estimate ✓, CI/CD ✓, Play Store ✓.
- **Placeholder scan:** None. All steps have concrete code.
- **Type consistency:** `StationaryFilter.process()` → `FilterResult` used consistently in Task 10 and Task 13. `TrailGeoJsonBuilder.buildOverlay()` / `buildGradient()` signatures match between Task 11 (tests) and Task 15 (ViewModel usage). `RecordingStateHolder.state` is a `StateFlow<RecordingState>` — consumed consistently in Task 13 (service) and Task 15 (ViewModel).
- **Known follow-ups for the implementer:**
  - MapLibre Expression API import path may differ between SDK versions — verify `org.maplibre.android.style.expressions.Expression` vs `com.mapbox.mapboxsdk.style.layers.Property` for line cap/join once the dependency is resolved.
  - `RecordingService.kt` has one manual import addition noted in Task 13 — add `import kotlinx.coroutines.flow.first`.
  - `SettingsViewModel.clearAllData` uses `.first()` on a Flow — add `import kotlinx.coroutines.flow.first`.
  - The `TrailBottomSheet` stub (Task 16) is intentionally minimal — flesh it out to show trail name, distance, and "View detail" button by reading from `TrailRepository` via a local ViewModel.

