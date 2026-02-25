FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests -pl query-service -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/query-service/target/query-service-1.1.0-posthackathon.jar app.jar
EXPOSE 5478
ENTRYPOINT ["java","-jar","/app/app.jar"]
