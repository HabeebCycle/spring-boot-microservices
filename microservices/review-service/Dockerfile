#191.13MB jdk-alpine
FROM openjdk:12-jdk-alpine

EXPOSE 8080

ADD ./target/review-service.jar review-service.jar

ENTRYPOINT ["java", "-jar", "/review-service.jar"]