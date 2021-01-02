#191.13MB jdk-alpine
FROM openjdk:12-jdk-alpine

EXPOSE 8080

ADD ./target/product-composite-service.jar product-composite-service.jar

ENTRYPOINT ["java", "-jar", "/product-composite-service.jar"]