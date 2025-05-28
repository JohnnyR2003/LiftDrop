FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .

# Build the application
RUN ./gradlew build --no-daemon

COPY LiftDropApi/build/libs/LiftDropApi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]