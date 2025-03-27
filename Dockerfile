FROM eclipse-temurin:21-alpine
WORKDIR /app
COPY backend/target/internet_supplier_management_system_app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]