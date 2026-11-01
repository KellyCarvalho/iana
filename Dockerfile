# Multi-stage Dockerfile para o Iana (Frontend Angular 18 + Backend Spring Boot 3 / Java 21)

# Estágio 1: Build do Frontend Angular
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Estágio 2: Build do Backend Spring Boot (incluindo o bundle do Angular em static/)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY src src
COPY --from=frontend-builder /frontend/dist/iana-frontend/browser/ src/main/resources/static/

RUN mvn clean package -DskipTests

# Estágio 3: Imagem Runtime Leve
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
