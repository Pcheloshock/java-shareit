#!/bin/bash
echo "Waiting for server to be ready..."
for i in {1..60}; do
  if curl -s http://localhost:9090/actuator/health > /dev/null; then
    echo "Server is ready!"
    exit 0
  fi
  echo "Waiting... ($i/60)"
  sleep 1
done
echo "Timeout waiting for server"
exit 1
