# ============================================
# ESTÁGIO 1 — BUILD
# Compila o projeto e gera o .jar
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia só o pom.xml primeiro (cache de dependências)
COPY pom.xml .
RUN mvn -B -Dmaven.wagon.http.retryHandler.count=5 \
           -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
           dependency:go-offline

# Agora copia o código e builda
COPY src ./src
RUN mvn -B -Dmaven.wagon.http.retryHandler.count=5 \
           -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
           clean package -DskipTests

# ============================================
# ESTÁGIO 2 — RUNTIME
# Só o jar, imagem enxuta
# ============================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuário não-root (segurança)
RUN addgroup -S app && adduser -S app -G app
USER app

# Copia só o jar do estágio 1
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]