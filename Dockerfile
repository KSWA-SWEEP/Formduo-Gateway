FROM openjdk:11-jdk-slim

VOLUME /tmp

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=${USE_PROFILE}", "-jar", "/app.jar"]