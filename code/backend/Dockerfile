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

# Install curl only (from base image repo, no need for apt mirrors)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Download and install ngrok binary directly (no apt)
RUN curl -LO https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz && \
    tar -xvzf ngrok-v3-stable-linux-amd64.tgz -C /usr/local/bin && \
    rm ngrok-v3-stable-linux-amd64.tgz

# Authenticate ngrok with token (optional if you pass via env var instead)
ENV NGROK_AUTHTOKEN=2wAmUFK3ep21ldciAD7Uq3wZYuh_6bDTYUnBu4v4RqDgJ82Xo

# Copy your ngrok config file if needed
RUN mkdir -p /etc/ngrok
COPY ngrok.yml /etc/ngrok/ngrok.yml

# Copy built app
COPY --from=build /home/gradle/src/LiftDropApi/build/libs/LiftDropApi-0.0.1-SNAPSHOT.jar /app.jar

# Create a script to run both ngrok and your app
COPY start.sh /start.sh
RUN chmod +x /start.sh

# Expose app port
ENV PORT=${PORT:-8080}
EXPOSE $PORT

CMD ["/start.sh"]
