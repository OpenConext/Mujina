FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S mujina && adduser -S mujina -G mujina
COPY target/*.jar app.jar
USER mujina
ENTRYPOINT ["java","-jar","/app.jar"]
