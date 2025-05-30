# Etapa de construcción
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno para la configuración
ENV R2DBC_URL_PERSON=${R2DBC_URL_PERSON}
ENV R2DBC_USERNAME_PERSON=${R2DBC_USERNAME_PERSON}
ENV R2DBC_PASSWORD_PERSON=${R2DBC_PASSWORD_PERSON}
ENV KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS}
ENV KAFKA_USERNAME=${KAFKA_USERNAME}
ENV KAFKA_PASSWORD=${KAFKA_PASSWORD}
ENV FAMILY_SERVICE_URL=${FAMILY_SERVICE_URL}

# Exponer el puerto
EXPOSE 8081

# Comando de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]
