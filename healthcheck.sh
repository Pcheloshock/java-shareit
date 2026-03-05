#!/bin/bash
echo "Checking if server is accessible..."

# Проверяем через имя контейнера (для Docker)
for i in {1..30}; do
  if curl -s http://shareit-server:9090/actuator/health > /dev/null; then
    echo "Server is ready on shareit-server:9090!"
    exit 0
  fi
  echo "Waiting for shareit-server:9090... ($i/30)"
  sleep 2
done

echo "Server not accessible via container name, trying localhost..."
for i in {1..30}; do
  if curl -s http://localhost:9090/actuator/health > /dev/null; then
    echo "Server is ready on localhost:9090!"
    exit 0
  fi
  echo "Waiting for localhost:9090... ($i/30)"
  sleep 2
done

echo "Server not accessible"
exit 1
