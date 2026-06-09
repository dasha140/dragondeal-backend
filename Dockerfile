# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create uploads directory (warning: ephemeral in serverless)
RUN mkdir -p /app/uploads/listings /app/uploads/avatars

COPY --from=builder /app/target/Marketgm-1.0-SNAPSHOT.jar app.jar

# Run as non-root
RUN useradd -r appuser && chown -R appuser:appuser /app
USER appuser

ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
