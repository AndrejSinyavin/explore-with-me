FROM amazoncorretto:21-al2023
WORKDIR /application
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
