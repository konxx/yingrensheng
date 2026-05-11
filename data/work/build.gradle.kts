plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation("com.google.code.gson:gson:2.11.0")
    implementation(project(":data:project"))
    implementation(libs.kotlinx.coroutines.core)
}
