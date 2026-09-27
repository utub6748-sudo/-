plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.astro.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.astro.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures { compose = true; buildConfig = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    buildTypes {
        release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") }
        debug { applicationIdSuffix = ".debug"; versionNameSuffix = "-debug" }
    }

    val localProperties = java.util.Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
    val supabaseUrl = localProperties.getProperty("ASTRO_SUPABASE_URL", "")
    val supabaseKey = localProperties.getProperty("ASTRO_SUPABASE_KEY", "")
    val aiBaseUrl = localProperties.getProperty("ASTRO_AI_BASE_URL", "")
    fun quoted(value: String) = "\"${value.replace("\"", "\\\"")}\""
    buildTypes.configureEach {
        buildConfigField("String", "SUPABASE_URL", quoted(supabaseUrl))
        buildConfigField("String", "SUPABASE_KEY", quoted(supabaseKey))
        buildConfigField("String", "AI_BASE_URL", quoted(aiBaseUrl))
    }
}

kotlin { jvmToolchain(17) }

val composeBom = platform("androidx.compose:compose-bom:2026.09.00")

dependencies {
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    implementation("androidx.work:work-runtime-ktx:2.11.0")

    val room = "2.8.5"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    implementation("io.github.jan-tennert.supabase:auth-kt:3.8.0")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.8.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.8.0")
    implementation("io.ktor:ktor-client-android:3.3.0")

    implementation("com.squareup.okhttp3:okhttp:5.1.0")
}
