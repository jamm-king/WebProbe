plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":common"))
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
