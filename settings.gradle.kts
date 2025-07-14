plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "webprobe"

include("application", "crawler", "data", "llm", "common")
include("common")
include("crawler")
include("data")
include("llm")
include("application")
