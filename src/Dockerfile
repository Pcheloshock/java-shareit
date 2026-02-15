FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/shareit-0.0.1-SNAPSHOT.jar app.jar

# Скрипт для ожидания базы данных
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

EXPOSE 8080

ENTRYPOINT ["./wait-for-it.sh", "db:5432", "-t", "60", "--", "java", "-Dspring.profiles.active=postgres", "-jar", "app.jar"]