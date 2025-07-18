import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm") version "2.1.0" apply false
	kotlin("plugin.spring") version "1.9.22" apply false
	id("org.springframework.boot") version "3.2.0" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
	id ("com.github.ben-manes.versions") version "0.52.0" apply true
}

allprojects {
	group = "com.jammking"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}

	tasks.withType<KotlinCompile>().configureEach {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_17)
		}
	}
	tasks.withType<JavaCompile>().configureEach {
		sourceCompatibility = "17"
		targetCompatibility = "17"
	}
}