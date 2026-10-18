# ============================================
# ESTÁGIO 1 — BUILD
# ============================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Diagnóstico: mostrar versão do Java no log do Render
RUN java -version && javac -version && mvn -version

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
# ============================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV SERVER_PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]