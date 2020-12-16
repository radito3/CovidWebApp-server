FROM maven:3.6.3-openjdk-11 AS maven
COPY . /
RUN mvn -Dmaven.local.repo="/mvn-repository" package

FROM openjdk:11-jdk-slim
WORKDIR /app
COPY --from=maven /target/*.jar /app/server.jar
#this will be removed when all 3 components of the project are containerized
EXPOSE 8080
# curl won't be present in the image, so make a minimal jar that does an http request for a given input arg
#HEALTHCHECK --interval=15s --timeout=10s --start-period=3m \
# CMD curl --fail http://localhost:8080/public/ping || exit 1
ENTRYPOINT ["java", "-jar", "/app/server.jar"]
