FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY application ./application
COPY data ./data
COPY common ./common
COPY crawler ./crawler
COPY llm ./llm

RUN ./gradlew build --no-daemon

EXPOSE 8080
CMD ["java", "-jar", "application/build/libs/application-0.0.1-SNAPSHOT.jar"]
