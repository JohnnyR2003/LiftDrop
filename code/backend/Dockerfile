FROM eclipse-temurin:21-jdk

# Set the working directory
WORKDIR /app

# Copy Gradle wrapper and build files first (to cache dependencies better)
COPY ./code/backend/gradlew ./gradlew
COPY ./code/backend/gradle ./gradle
COPY ./code/backend/build.gradle.kts ./build.gradle.kts
COPY ./code/backend/settings.gradle.kts ./settings.gradle.kts

# Ensure the Gradle wrapper is executable
RUN chmod +x ./gradlew

# Copy the rest of the source code
COPY ./code/backend/src ./src

# Build the application (skipping tests for faster builds)
RUN ./gradlew build -x test

# Copy the generated JAR (adjust path if different)
COPY ./build/libs/LiftDropApi-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
