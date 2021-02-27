#191.13MB jdk-alpine
FROM openjdk:12-jdk-alpine

EXPOSE 8888

ADD ./target/config-server.jar config-server.jar

ENTRYPOINT ["java", "-jar", "/config-server.jar"]