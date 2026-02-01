# Этап 1: Сборка
FROM maven:3.9.12-eclipse-temurin-21-noble AS build
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости (кешируем этот слой)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем jar
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Запуск
FROM eclipse-temurin:21-jre-noble
WORKDIR /app

# Создаем не-root пользователя для безопасности (best practice 2026)
RUN useradd -m springuser
USER springuser

# Копируем только готовый jar из первого этапа
COPY --from=build /app/target/*.jar app.jar

# Открываем порт
EXPOSE 8080

# Настройки JVM для работы в контейнере
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
