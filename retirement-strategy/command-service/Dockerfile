FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests -pl command-service -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/command-service/target/command-service-1.1.0-posthackathon.jar app.jar
EXPOSE 5477
ENTRYPOINT ["java","-jar","/app/app.jar"]
