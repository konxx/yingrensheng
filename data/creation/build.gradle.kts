plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":data:project"))
    implementation("com.google.code.gson:gson:2.11.0")
    implementation(libs.kotlinx.coroutines.core)
}
