# Multi-stage Dockerfile para o microserviço Iana (Spring Boot 3 + Java 21)

# Estágio 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copia arquivos do Maven e código-fonte
COPY pom.xml .
COPY src src

# Compila o projeto e empacota o JAR
RUN mvn clean package -DskipTests

# Estágio 2: Imagem Runtime Leve
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o artefato JAR compilado
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Comando de inicialização da aplicação
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
