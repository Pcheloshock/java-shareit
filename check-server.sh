#!/bin/bash
echo "Checking server availability..."

# Проверяем через имя контейнера
for i in {1..30}; do
  if curl -s http://server:9090/actuator/health > /dev/null; then
    echo "Server is ready on server:9090!"
    exit 0
  fi
  echo "Waiting for server:9090... ($i/30)"
  sleep 2
done

echo "Server not accessible"
exit 1
