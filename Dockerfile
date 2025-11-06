FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy all files and build in one step (simpler, more reliable)
COPY . .
RUN mvn --batch-mode clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/Backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java","-jar","/app/app.jar"]
