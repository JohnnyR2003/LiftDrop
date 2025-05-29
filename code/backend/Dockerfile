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

# Install supervisor and curl
RUN apt-get update && apt-get install -y supervisor curl unzip

# Download and install ngrok
RUN curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null \
    && echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | tee /etc/apt/sources.list.d/ngrok.list \
    && apt-get update && apt-get install -y ngrok

# Create ngrok config file
RUN mkdir -p /etc/ngrok
COPY ngrok.yml /etc/ngrok/ngrok.yml

# Copy built app
COPY --from=build /home/gradle/src/LiftDropApi/build/libs/LiftDropApi-0.0.1-SNAPSHOT.jar /app.jar

# Copy supervisor config
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Set default PORT
ENV PORT=${PORT:-8080}
EXPOSE $PORT

CMD ["/usr/bin/supervisord"]
