# Stage 1: Cache Gradle dependencies (only when build files change)
FROM gradle:8-jdk21 AS cache
ENV GRADLE_USER_HOME=/home/gradle/.gradle
WORKDIR /home/gradle/app

# Copy entire project for dependency resolution
# Using Docker-specific settings that excludes waltid-applications
COPY --chown=gradle:gradle . .
RUN chmod +x gradlew

# Replace settings.gradle.kts with Docker version that excludes waltid-applications
COPY waltid-services/waltid-wallet-api/settings.gradle.kts.docker ./settings.gradle.kts

# Download dependencies (this layer will be cached unless build files change)
# Using BuildKit cache mount for persistent Gradle cache
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/caches \
    ./gradlew :waltid-services:waltid-wallet-api:dependencies --no-daemon || true

# Stage 2: Build Application
FROM gradle:8-jdk21 AS build
ENV GRADLE_USER_HOME=/home/gradle/.gradle
WORKDIR /home/gradle/src

# Copy entire project
COPY --chown=gradle:gradle . .

# Make gradlew executable
RUN chmod +x gradlew

# Replace settings.gradle.kts with Docker version that excludes waltid-applications
COPY waltid-services/waltid-wallet-api/settings.gradle.kts.docker ./settings.gradle.kts

# Build with cache mounts for faster rebuilds
# Using BuildKit cache mounts to persist Gradle cache between builds
# Use gradlew from project root to ensure proper plugin resolution
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/caches \
    ./gradlew :waltid-services:waltid-wallet-api:buildFatJar --build-cache --parallel --no-daemon

# Stage 3: Runtime image with slim JRE
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /waltid-wallet-api

# Copy the runnable jar from the build stage and give it a stable name
COPY --from=build /home/gradle/src/waltid-services/waltid-wallet-api/build/libs/waltid-wallet-api-*.jar ./waltid-wallet-api.jar

# Pre-create directories that will later be mounted as volumes
RUN mkdir -p config data

EXPOSE 7001
ENTRYPOINT ["java", "-jar", "/waltid-wallet-api/waltid-wallet-api.jar"]
