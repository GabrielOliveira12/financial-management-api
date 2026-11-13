#!/bin/sh
set -e

mkdir -p /app/logs
chown -R spring:spring /app/logs

exec su spring -s /bin/sh -c "export PATH=/opt/java/openjdk/bin:\$PATH; cd /app && exec java -jar app.jar"
