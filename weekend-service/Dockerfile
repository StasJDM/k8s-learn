FROM gradle:6.9-jdk11 as builder

WORKDIR weekend-service/

COPY build.gradle ./
COPY src/ ./src/

RUN gradle build

FROM openjdk:11-jre-slim

WORKDIR weekend-service/

COPY --from=builder home/gradle/weekend-service/build/libs/weekend-service-0.1.0-SNAPSHOT.jar .
 
EXPOSE 5678

CMD ["java", "-jar", "/weekend-service/weekend-service-0.1.0-SNAPSHOT.jar"]
