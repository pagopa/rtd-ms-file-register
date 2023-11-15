FROM maven:3.9.3-amazoncorretto-17@sha256:4ab7db7bd5f95e58b0ba1346ff29d6abdd9b73e5fd89c5140edead8b037386ff AS buildtime

WORKDIR /build
COPY . .

RUN mvn clean package

FROM amazoncorretto:17.0.9-alpine3.18@sha256:5c009904e51559c23b3a026c1c93c14d3abfb94ed140207e7e694d3e2362dd0a AS runtime

VOLUME /tmp
WORKDIR /app

COPY --from=buildtime /build/target/*.jar /app/app.jar
RUN chown -R nobody:nobody /app

EXPOSE 8080

USER 65534

ENTRYPOINT ["java","-jar","/app/app.jar"]
