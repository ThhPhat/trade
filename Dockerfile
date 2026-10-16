# ===== Stage 1: Build =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== Stage 2: Run =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render dynamic port and strict memory optimization for Free Tier (512MB RAM)
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Xmx180m -Xms80m -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=32m -Xss384k -XX:+UseSerialGC -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
