FROM openjdk:18-jdk-slim

WORKDIR /app

COPY build/libs/gateway-0.0.1-SNAPSHOT.jar gateway.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "gateway.jar"]