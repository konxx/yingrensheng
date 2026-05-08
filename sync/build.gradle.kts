plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":data:creation"))
    implementation(project(":data:order"))
    implementation(libs.kotlinx.coroutines.core)
}
