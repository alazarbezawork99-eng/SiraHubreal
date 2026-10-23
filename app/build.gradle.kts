import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.io.File
import java.io.FileInputStream
import java.util.Properties

fun getEnvProp(propertyName: String): String {
  var value = System.getenv(propertyName)
  if (value.isNullOrBlank()) {
    val envFile = File(rootProject.projectDir, ".env")
    if (envFile.exists()) {
      val props = Properties()
      FileInputStream(envFile).use { props.load(it) }
      value = props.getProperty(propertyName)
    }
  }
  return value?.trim() ?: ""
}

fun resolveFlavorSecret(specificKey: String, fallbackKey: String, flavorName: String): String {
  var valStr = getEnvProp(specificKey)
  if (valStr.isBlank() || valStr.contains("xyzcompany") || valStr.contains("your-supabase")) {
    valStr = getEnvProp(fallbackKey)
  }
  if (valStr.isBlank() || valStr.contains("xyzcompany") || valStr.contains("your-supabase")) {
    throw GradleException(
      "BUILD FAILURE: Missing valid $specificKey (or $fallbackKey) in .env or Gradle properties for '$flavorName' flavor!\n" +
      "Please set $specificKey and $fallbackKey in your .env file."
    )
  }
  return valStr
}

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
  alias(libs.plugins.hilt)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.sirahub.etjobs"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Do not remove or modify these signingConfigs. They are necessary for building, installing,
  // and updating Android apps in AI Studio.
  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Do not remove or modify this signingConfig assignment. It is necessary for Android apps in
      // AI Studio.
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // Use default debug signing config when debug.keystore is not present
      if (file("${rootDir}/debug.keystore").exists()) {
        signingConfig = signingConfigs.getByName("debugConfig")
      }
    }
  }

  flavorDimensions += "environment"
  productFlavors {
    create("staging") {
      dimension = "environment"
      applicationIdSuffix = ".staging"
      val url = resolveFlavorSecret("SUPABASE_STAGING_URL", "SUPABASE_URL", "staging")
      val key = resolveFlavorSecret("SUPABASE_STAGING_ANON_KEY", "SUPABASE_ANON_KEY", "staging")
      buildConfigField("String", "SUPABASE_URL", "\"$url\"")
      buildConfigField("String", "SUPABASE_ANON_KEY", "\"$key\"")
    }
    create("production") {
      dimension = "environment"
      val url = resolveFlavorSecret("SUPABASE_PROD_URL", "SUPABASE_URL", "production")
      val key = resolveFlavorSecret("SUPABASE_PROD_ANON_KEY", "SUPABASE_ANON_KEY", "production")
      buildConfigField("String", "SUPABASE_URL", "\"$url\"")
      buildConfigField("String", "SUPABASE_ANON_KEY", "\"$key\"")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Hilt 2.60.1 and Room 2.8.x transitively request kotlin-stdlib 2.4.x, which the Kotlin 2.2.10
// compiler cannot read (it tops out at metadata 2.3.0). Pin the stdlib to the compiler's own
// version so the whole module compiles against a single, readable Kotlin runtime.
configurations.configureEach {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib")) {
      useVersion(libs.versions.kotlin.get())
      because("Kotlin stdlib must match the Kotlin compiler version (${libs.versions.kotlin.get()}).")
    }
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(platform(libs.supabase.bom))
  implementation(libs.supabase.auth)
  implementation(libs.supabase.postgrest)
  implementation(libs.ktor.client.okhttp)
  implementation(libs.androidx.security.crypto)
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.hilt.android)
  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.room.testing)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
  "ksp"(libs.hilt.compiler)
}
