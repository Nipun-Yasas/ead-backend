FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn --batch-mode dependency:go-offline

COPY src ./src
RUN mvn --batch-mode -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/UserManagementAPP-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java","-jar","/app/app.jar"]
