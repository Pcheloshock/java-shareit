FROM eclipse-temurin:21-jdk-alpine

# Устанавливаем netcat для wait-for-it.sh (nc команда)
RUN apk add --no-cache netcat-openbsd

WORKDIR /app

# Копируем wait-for-it.sh и делаем его исполняемым
COPY wait-for-it.sh .
RUN chmod +x wait-for-it.sh

# Копируем jar файл
COPY target/shareit-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Используем wait-for-it.sh для ожидания PostgreSQL
ENTRYPOINT ["./wait-for-it.sh", "db", "5432", "--", "java", "-jar", "app.jar", "--spring.profiles.active=test"]