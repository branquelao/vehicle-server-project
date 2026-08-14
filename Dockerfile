# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN chmod +x mvnw

# Cache mount do repositório Maven local: persiste entre builds independente do
# cache de camadas do Docker, sem precisar do dependency:go-offline (que tenta
# baixar todo o BOM herdado do Flyway — incluindo suporte a bancos que este
# projeto não usa — e deixa o build extremamente lento).
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -B

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Roda como usuário não-root por segurança
RUN useradd --create-home --shell /bin/bash appuser

COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/uploads && chown -R appuser:appuser /app

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
