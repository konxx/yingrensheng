plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val yrsBackendOrigin = providers.gradleProperty("yrsBackendOrigin")
    .orElse("http://10.0.2.2:3000")
    .get()
    .trimEnd('/')
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "com.yingrensheng.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yingrensheng.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "YRS_BACKEND_ORIGIN", "\"$yrsBackendOrigin\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("com.google.code.gson:gson:2.11.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))
    implementation(project(":data:agency"))
    implementation(project(":data:creation"))
    implementation(project(":data:member"))
    implementation(project(":data:order"))
    implementation(project(":data:project"))
    implementation(project(":data:user"))
    implementation(project(":data:work"))
    implementation(project(":feature:agency"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:create"))
    implementation(project(":feature:editor"))
    implementation(project(":feature:home"))
    implementation(project(":feature:member"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:order"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:works"))
}
