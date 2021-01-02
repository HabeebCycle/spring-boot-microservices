#191.13MB jdk-alpine
FROM openjdk:12-jdk-alpine

EXPOSE 8080

ADD ./target/product-service.jar product-service.jar

ENTRYPOINT ["java", "-jar", "/product-service.jar"]