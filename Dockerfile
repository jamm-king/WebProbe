# ---- Builder: Create JAR with Gradle ----
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Gradle memory/concurrency settings (heap 2–3GB recommended)
# Disable daemon/parallel: reduces peak memory usage in container builds
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx3g -XX:MaxMetaspaceSize=768m -Dfile.encoding=UTF-8 -Duser.country=KR -Duser.language=ko" \
    GRADLE_USER_HOME=/root/.gradle

# Inject gradle.properties into project root to enforce settings
#  - Keeps consistency and also benefits Docker build cache
RUN printf '%s\n' \
  'org.gradle.jvmargs=-Xmx3g -XX:MaxMetaspaceSize=768m -Dfile.encoding=UTF-8' \
  'org.gradle.parallel=false' \
  'org.gradle.daemon=false' \
  > /app/gradle.properties

# Cache optimization: copy scripts/config first
COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

# Copy source code
COPY application ./application
COPY data ./data
COPY common ./common
COPY crawler ./crawler
COPY llm ./llm

# Prevent corrupted caches + build
RUN rm -rf /root/.gradle/caches /root/.m2/repository/com/microsoft/playwright && \
    ./gradlew --no-daemon --refresh-dependencies \
      :application:bootJar -x test --max-workers=1

# ---- Runtime: Playwright dedicated runtime ----
FROM mcr.microsoft.com/playwright/java:v1.54.0-noble

# Add Korean/emoji fonts
USER root
RUN apt-get update && apt-get install -y --no-install-recommends \
    fonts-noto-cjk fonts-noto-color-emoji \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy only application JAR
COPY --from=builder /app/application/build/libs/application-0.0.1-SNAPSHOT.jar app.jar

# Playwright related environment
ENV JAVA_TOOL_OPTIONS="-Dloader.debug=true -Dplaywright.log=debug -Dplaywright.tmpdir=/pwtmp"
RUN mkdir -p /pwtmp && chmod 1777 /pwtmp

USER pwuser

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]