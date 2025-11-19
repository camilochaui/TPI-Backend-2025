# 1. Usar una imagen base de Java 17 estable (Eclipse Temurin sobre Ubuntu Focal).
FROM eclipse-temurin:17-jdk-focal

# 2. Definir un argumento para la ruta del archivo JAR.
ARG JAR_FILE=target/*.jar

# 3. Copiar el archivo JAR construido al contenedor.
COPY ${JAR_FILE} app.jar

# 4. Comando para ejecutar la aplicación cuando el contenedor se inicie.
ENTRYPOINT ["java","-jar","/app.jar"]
