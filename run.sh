#!/bin/bash
set -e

echo "Waiting for services to be ready..."
cd ../..
./check-server.sh

echo "Services are ready. Running tests..."

cd server
mvn test

cd ../gateway
mvn test

echo "All tests completed successfully!"
