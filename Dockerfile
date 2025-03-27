FROM eclipse-temurin:22-alpine

COPY backend/target/internet_supplier_management_system_app.jar /internet_supplier_management_system_app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/internet_supplier_management_system_app.jar"]