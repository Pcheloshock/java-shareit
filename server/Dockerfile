FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y curl
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=docker"]
