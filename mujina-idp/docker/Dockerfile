FROM ghcr.io/openconext/openconext-basecontainers/java21:latest
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
