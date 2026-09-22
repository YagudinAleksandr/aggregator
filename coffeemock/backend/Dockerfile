# syntax=docker/dockerfile:1

# ---------- Сборка ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY coffeemock-domain/pom.xml      coffeemock-domain/
COPY coffeemock-application/pom.xml coffeemock-application/
COPY coffeemock-adapters/pom.xml    coffeemock-adapters/
COPY coffeemock-domain/src          coffeemock-domain/src
COPY coffeemock-application/src     coffeemock-application/src
COPY coffeemock-adapters/src        coffeemock-adapters/src

# settings.xml с токеном для GitHub Packages передаётся как build secret
# и не попадает ни в один слой образа.
# Кэш ~/.m2/repository переживает пересборки.
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    --mount=type=cache,target=/root/.m2/repository \
    mvn -B -ntp package -DskipTests -pl coffeemock-adapters -am

# Распаковываем fat-jar: зависимости (lib/) и код (app.jar) лягут разными слоями
RUN java -Djarmode=tools -jar coffeemock-adapters/target/coffeemock.jar \
    extract --destination /workspace/extracted

# ---------- Запуск ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app

# Сначала редко меняющиеся зависимости, потом код приложения
COPY --from=build --chown=app:app /workspace/extracted/lib    lib
COPY --from=build --chown=app:app /workspace/extracted/*.jar  app.jar

USER app
EXPOSE 8101

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
