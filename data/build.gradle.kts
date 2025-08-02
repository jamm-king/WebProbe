plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":common"))

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.slf4j:slf4j-api:2.0.17")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.11.0")}

tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}
tasks.test {
    useJUnitPlatform()
    if (System.getenv("DOCKER") == "true") {
        exclude("**/CrawledPageRepositoryTest.class")
    }
}