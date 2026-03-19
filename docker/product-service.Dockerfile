FROM gradle:8.10-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle :product-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/product-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
