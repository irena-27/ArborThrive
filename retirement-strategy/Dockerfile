#FROM eclipse-temurin:17-jre
#WORKDIR /app
#COPY target/blk-autosave-advanced-1.0.0.jar app.jar
#EXPOSE 5477
#ENTRYPOINT ["java","-jar","/app/app.jar"]

# ---- Build stage (Maven inside Docker) ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 5477
ENTRYPOINT ["java","-jar","/app/app.jar"]