FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy everything (including LiftDropApi and build files)
COPY . .

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Build only the LiftDropApi module (skip tests for speed)
RUN ./gradlew :LiftDropApi:build -x test

# Copy the generated JAR
COPY LiftDropApi/build/libs/LiftDropApi-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
