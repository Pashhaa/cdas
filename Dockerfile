FROM openjdk:17-oracle

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8802

ENTRYPOINT ["java", "-jar", "app.jar"]
