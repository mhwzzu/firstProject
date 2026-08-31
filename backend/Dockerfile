FROM maven:3.8.8-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:8-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=postgres
COPY --from=build /app/target/together-journey-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
