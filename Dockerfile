FROM openjdk:23

WORKDIR /app

COPY build/libs/utility_supervisor-v1.0.0-6-gff36d46.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]