import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.9.0" apply false
	kotlin("plugin.spring") version "1.9.22" apply false
	id("org.springframework.boot") version "3.2.0" apply false
	id("io.spring.dependency-management") version "1.1.3" apply false
}

allprojects {
	group = "com.jammking"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}

	tasks.withType<KotlinCompile>().configureEach {
		kotlinOptions.jvmTarget = "17"
	}
	tasks.withType<JavaCompile>().configureEach {
		sourceCompatibility = "17"
		targetCompatibility = "17"
	}
}
