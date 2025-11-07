FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the pre-built JAR from target directory (for Jenkins local builds)
COPY target/Backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8090
CMD ["java","-jar","/app/app.jar"]
