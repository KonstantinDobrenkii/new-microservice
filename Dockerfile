FROM gcr.io/distroless/java21-debian12

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/home/iwmsservice/app.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
COPY target/lib /home/iwmsservice/lib

# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /home/iwmsservice/app.jar
