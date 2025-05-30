#
# Build stage
#
FROM gradle:jdk21-jammy AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle :LiftDropApi:build -x test --no-daemon

LABEL org.name="liftdrop"
#
# Package stage
#
FROM eclipse-temurin:21-jdk-jammy
COPY --from=build /home/gradle/src/LiftDropApi/build/libs/LiftDropApi-0.0.1-SNAPSHOT.jar app.jar
# Use PORT from environment variable with fallback to 8080
ENV PORT=${PORT:-8080}
EXPOSE $PORT
ENTRYPOINT ["java","-jar","/app.jar"]
