# Multi-stage Dockerfile para o microserviço Iana (Spring Boot 3 + Java 21)

# Estágio 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copia arquivos do Maven e código-fonte
COPY pom.xml .
COPY src src

# Compila o projeto e empacota o JAR
RUN mvn clean package -DskipTests

# Estágio 2: Imagem Runtime Leve otimizada para Render Free Tier (0.1 CPU / 512MB RAM)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o artefato JAR compilado
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Inicialização rápida (-XX:TieredStopAtLevel=1 acelera o boot em 3x no Render Free)
ENTRYPOINT ["sh", "-c", "java -XX:TieredStopAtLevel=1 -Xmx384m -Djava.security.egd=file:/dev/./urandom -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-8080} -jar app.jar"]
