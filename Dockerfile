FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
 && ./mvnw -B dependency:go-offline

COPY src ./src
# Os testes rodam no CI (GitHub Actions), onde há Docker para o Testcontainers.
RUN ./mvnw -B -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system app \
 && useradd --system --gid app --no-create-home app

COPY --from=build --chown=app:app /app/target/*.jar app.jar
USER app

# A porta real vem da variável PORT (o Render define dinamicamente, ex: 10000)
EXPOSE 8080

# Adicionado MaxRAMPercentage para prevenir OOM Kill em ambientes com RAM limitada
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseSerialGC", "-jar", "app.jar"]