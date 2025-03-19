FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/coursework-0.0.1-SNAPSHOT.jar coursework.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "coursework.jar"]
