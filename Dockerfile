# ============================================================
# Stage 1: Build the Spring Boot application using Maven
# ============================================================

FROM eclipse-temurin:25-jdk AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper files
COPY mvnw .
COPY .mvn .mvn

# Copy pom.xml
COPY pom.xml .

# Copy source code
COPY src src

# Give executable permission
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests


# ============================================================
# Stage 2: Runtime Image
# ============================================================

FROM eclipse-temurin:25-jre

# Set working directory
WORKDIR /app

# Copy generated jar
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 9090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]