# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src

RUN chmod +x mvnw \
 && ./mvnw -B -DskipTests package \
 && cp target/gateway-*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system spring \
 && useradd --system --gid spring --no-create-home spring \
 && mkdir -p /app/logs/archived \
 && chown -R spring:spring /app

COPY --from=build /workspace/app.jar /app/app.jar
RUN chown spring:spring /app/app.jar
USER spring:spring

EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
