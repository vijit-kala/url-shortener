# Use official OpenJDK image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven build output (JAR file)
COPY target/*.jar app.jar

# Expose port (matches your Spring Boot server.port, default 8080)
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java","-jar","app.jar"]