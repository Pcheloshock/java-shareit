FROM eclipse-temurin:21-jdk-alpine

# Устанавливаем curl и postgresql-client для проверки подключения
RUN apk add --no-cache curl postgresql-client

WORKDIR /app

# Копируем jar файл
COPY target/shareit-0.0.1-SNAPSHOT.jar app.jar

# Команда с ожиданием PostgreSQL
EXPOSE 8080

ENTRYPOINT sh -c 'until pg_isready -h db -U postgres; do \
  echo "Waiting for PostgreSQL..."; \
  sleep 2; \
done; \
echo "PostgreSQL is ready! Starting application..."; \
java -jar app.jar --spring.profiles.active=test'