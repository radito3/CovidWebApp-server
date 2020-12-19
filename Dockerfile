FROM maven:3.6.3-openjdk-11 AS maven
COPY . /
RUN mvn -Dmaven.local.repo="/mvn-repository" package

FROM openjdk:11-jdk-slim
WORKDIR /app
COPY --from=maven /target/*.jar /app/server.jar
ENTRYPOINT ["java", "-jar", "/app/server.jar"]
