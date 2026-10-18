# ============================================
# Stage 1 — Build (JDK 21)
# ============================================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 1) Copia só o necessário para baixar dependências (cache layer)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
 && ./mvnw -B -DskipTests dependency:go-offline

# 2) Agora copia o código e builda
COPY src ./src
RUN ./mvnw -B -DskipTests clean package

# ============================================
# Stage 2 — Runtime (JRE 21)
# ============================================
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]