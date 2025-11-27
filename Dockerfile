# Dockerfile – FIXED for Java 21 (Eclipse Temurin)
FROM eclipse-temurin:21-jdk-alpine

# Create app directory
WORKDIR /app

# Copy the JAR file (adjust if your JAR name is different)
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]