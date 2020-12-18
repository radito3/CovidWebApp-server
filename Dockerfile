FROM maven:3.6.3-openjdk-11 AS maven
COPY . /
RUN mvn -Dmaven.local.repo="/mvn-repository" package

FROM radito3/call-http-jar:v1 AS hc-jar

FROM openjdk:11-jdk-slim
WORKDIR /app
COPY --from=maven /target/*.jar /app/server.jar
COPY --from=hc-jar /*.jar /app/health-check.jar
HEALTHCHECK --interval=15s --timeout=10s --start-period=3m CMD ["java", "-jar", "/app/health-check.jar", "server/public/ping"]
ENTRYPOINT ["java", "-jar", "/app/server.jar"]
