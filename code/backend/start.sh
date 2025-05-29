#!/bin/bash

# Start ngrok tunnel in background
ngrok http --config=/etc/ngrok/ngrok.yml $PORT &

# Start the Java backend
exec java -jar /app.jar
