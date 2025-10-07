# ---- Build stage (JDK 21) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# copy POM first for dependency layer caching
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# then copy sources and build
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime stage (JRE 21) ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
