# ============================================
# ESTÁGIO 1 — BUILD
# ============================================
# ⚠️ Tag 3.9.9 fixa: garante JDK 21 correto.
#    A tag "3.9-eclipse-temurin-21" foi republicada com JDK 17 (bug conhecido).
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Diagnóstico visível no log do Render: confirma JDK 21 no build
RUN echo "=== JAVA BUILD ENV ===" && \
    java -version && \
    javac -version && \
    mvn -version && \
    echo "======================="

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


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Diagnóstico do runtime (aparece no log do Render)
RUN echo "=== JAVA RUNTIME ENV ===" && \
    java -version && \
    echo "========================"

# Usuário não-root (segurança)
RUN addgroup -S app && adduser -S app -G app
USER app

# Copia só o jar do estágio 1
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Fallback de porta (Render injeta $PORT; SERVER_PORT tem prioridade no YAML)
ENV SERVER_PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]