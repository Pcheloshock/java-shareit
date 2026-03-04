#!/bin/bash
echo "Waiting for gateway to be ready..."
for i in {1..30}; do
  if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "Gateway is ready!"
    exit 0
  fi
  echo "Waiting... ($i/30)"
  sleep 1
done
echo "Timeout waiting for gateway"
exit 1
