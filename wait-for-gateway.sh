#!/bin/bash
echo "Waiting for gateway to be ready..."
MAX_RETRIES=90
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "Gateway is ready!"
    exit 0
  fi
  RETRY_COUNT=$((RETRY_COUNT + 1))
  echo "Waiting for gateway... ($RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done

echo "Gateway not ready after $MAX_RETRIES attempts"
exit 1
