FROM openjdk:8-jdk-alpine
ENV SERVICE_HOST localhost

VOLUME /tmp
ADD target/laa-saml-mock-idp-1.0.0.jar app.jar
COPY docker-idp-application.yml config/idp-application.yml

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar","app.jar", "--spring.config.location=config/idp-application.yml", "-DSERVICE_HOST=${SERVICE_HOST}"]
