FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r etour && useradd -r -g etour etour

COPY images ./images

RUN mkdir -p /app/logs

COPY --from=build /build/target/*.jar app.jar

RUN chown -R etour:etour /app
USER etour

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/api/categories/roots >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
