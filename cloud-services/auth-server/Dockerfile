#191.13MB jdk-alpine
FROM openjdk:12-jdk-alpine

EXPOSE 9999

ADD ./target/auth-server.jar auth-server.jar

ENTRYPOINT ["java", "-jar", "/auth-server.jar"]