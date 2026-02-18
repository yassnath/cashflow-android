import java.util.Properties
import org.gradle.api.GradleException

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.kotlin.plugin.serialization")
}

val localProps = Properties().apply {
  val localFile = rootProject.file("local.properties")
  if (localFile.exists()) {
    localFile.inputStream().use { load(it) }
  }
}

fun quoted(value: String): String = "\"${value.replace("\"", "\\\"")}\""
fun requiredLocalProp(key: String): String {
  val value = localProps.getProperty(key)?.trim().orEmpty()
  if (value.isBlank()) {
    throw GradleException("Missing `$key` in local.properties")
  }
  return value
}

android {
  namespace = "com.solvix.tabungan"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.solvix.tabungan"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    val supabaseUrl = requiredLocalProp("SUPABASE_URL")
    val supabaseAnonKey = requiredLocalProp("SUPABASE_ANON_KEY")
    val adminUsername = requiredLocalProp("ADMIN_USERNAME")
    val adminPassword = requiredLocalProp("ADMIN_PASSWORD")
    buildConfigField("String", "SUPABASE_URL", quoted(supabaseUrl))
    buildConfigField("String", "SUPABASE_ANON_KEY", quoted(supabaseAnonKey))
    buildConfigField("String", "ADMIN_USERNAME", quoted(adminUsername))
    buildConfigField("String", "ADMIN_PASSWORD", quoted(adminPassword))
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    isCoreLibraryDesugaringEnabled = true
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  lint {
    // Supabase/Ktor major upgrades require coordinated API migration.
    disable += "NewerVersionAvailable"
  }
}


dependencies {
  val composeBom = platform("androidx.compose:compose-bom:2025.09.01")
  implementation(composeBom)
  androidTestImplementation(composeBom)

  implementation("androidx.core:core-ktx:1.17.0")
  implementation("androidx.activity:activity-compose:1.12.4")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-text")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("androidx.compose.animation:animation")
  implementation("com.google.android.material:material:1.13.0")
  implementation("androidx.biometric:biometric:1.1.0")
  implementation("androidx.work:work-runtime-ktx:2.11.1")
  implementation("androidx.security:security-crypto:1.1.0-alpha06")
  implementation("io.github.jan-tennert.supabase:supabase-kt:2.4.1")
  implementation("io.github.jan-tennert.supabase:gotrue-kt:2.4.1")
  implementation("io.github.jan-tennert.supabase:postgrest-kt:2.4.1")
  implementation("io.ktor:ktor-client-okhttp:2.3.12")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")

  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
}
