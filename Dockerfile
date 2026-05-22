# Build stage
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

COPY . .
RUN chmod +x mvnw && ./mvnw -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/art-connect-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Render provides PORT dynamically; fallback to 8080 for local runs.
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]

