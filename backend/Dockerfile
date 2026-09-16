FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package
FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y --no-install-recommends wkhtmltopdf && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/contractapi-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
