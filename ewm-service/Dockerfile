FROM amazoncorretto:21.0.5-al2023
WORKDIR /application
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]