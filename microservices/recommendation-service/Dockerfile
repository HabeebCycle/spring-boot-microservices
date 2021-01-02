#191.13MB jdk-alpine
FROM openjdk:12-jdk-alpine

EXPOSE 8080

COPY ./target/recommendation-service.jar recommendation-service.jar

ENTRYPOINT ["java", "-jar", "/recommendation-service.jar"]