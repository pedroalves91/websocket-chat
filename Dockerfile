FROM openjdk:17-alpine

EXPOSE 80

RUN mkdir /app

COPY target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]