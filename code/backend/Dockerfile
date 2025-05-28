FROM eclipse-temurin:21-jdk

# Set the working directory
WORKDIR /app

# Copy Gradle wrapper and build files first (to cache dependencies better)
COPY . .

# Ensure the Gradle wrapper is executable
RUN chmod +x ./gradlew

# Build the application (skipping tests for faster builds)
RUN ./gradlew build -x test

# Copy the generated JAR (adjust path if different)
COPY ./LiftDropApi/build/libs/LiftDropApi-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
