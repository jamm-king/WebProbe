FROM eclipse-temurin:17-jdk

ENV DOCKER=true

RUN apt-get update && apt-get install -y \
    wget gnupg ca-certificates curl unzip \
    libnss3 libatk-bridge2.0-0 libgtk-3-0 \
    libx11-xcb1 libxcomposite1 libxdamage1 libxrandr2 libgbm1 \
    libasound2t64 libpangocairo-1.0-0 libatspi2.0-0 libxshmfence1 \
    --no-install-recommends && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY application ./application
COPY data ./data
COPY common ./common
COPY crawler ./crawler
COPY llm ./llm

ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

RUN ./gradlew assemble --no-daemon

EXPOSE 8080
CMD ["java", "-jar", "application/build/libs/application-0.0.1-SNAPSHOT.jar"]
