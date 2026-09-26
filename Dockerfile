# --- Estágio 1: build do .jar ---
# Usa uma imagem com Maven já instalado (o mvnw deste repo está quebrado,
# então não dependemos dele aqui dentro do container).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copia só o pom.xml primeiro para o Docker cachear as dependências
# e não precisar baixá-las de novo a cada mudança de código.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Agora copia o código-fonte e builda o jar (sem rodar os testes).
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Estágio 2: imagem final, só com o JRE + o jar ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /build/target/morkstore-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
