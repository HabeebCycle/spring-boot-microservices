#191.13MB jdk-alpine
FROM openjdk:12-jdk-alpine

EXPOSE 8080

ADD ./target/api-gateway.jar api-gateway.jar

ENTRYPOINT ["java", "-jar", "/api-gateway.jar"]