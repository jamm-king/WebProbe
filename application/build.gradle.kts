import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

    implementation(project(":common"))
    implementation(project(":crawler"))
    implementation(project(":data"))
    implementation(project(":llm"))
}

tasks.named<BootJar>("bootJar") {
    isZip64 = true

    requiresUnpack("com.microsoft.playwright:playwright")
    requiresUnpack("com.microsoft.playwright:driver")
    requiresUnpack("com.microsoft.playwright:driver-bundle")

    requiresUnpack("**/playwright-*.jar")
    requiresUnpack("**/driver-*.jar")
    requiresUnpack("**/driver-bundle-*.jar")
}
