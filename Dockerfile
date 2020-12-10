FROM maven:3.6.3-openjdk-11 AS maven
WORKDIR /app
RUN mvn -Dmaven.local.repo="/app/mvn-repository" package
FROM openjdk:11-jdk-slim
WORKDIR /app
COPY --from=maven /app/target/$JAR_NAME.jar /app
#this will be removed when all 3 components of the project are containerized
EXPOSE 3000
CMD java -jar /app/$JAR_NAME.jar
