# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install necessary libraries for JavaFX on Linux
RUN apt-get update && apt-get install -y \
    libgtk-3-0 \
    libgl1-mesa-glx \
    libglu1-mesa \
    && rm -rf /var/lib/apt/lists/*

# Copy the fat JAR from the build stage
COPY --from=build /app/target/ProjetQcm-1.0-SNAPSHOT.jar app.jar

# Expose the port (if your app uses one, e.g. for a web server part, otherwise this is just documentation)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
