FROM public.ecr.aws/docker/library/maven:3.9.6-amazoncorretto-17@sha256:b64f097a87e94f3fb433649f2a49270564fa626494d7d6bfd8955f32da794210 AS buildtime

WORKDIR /build
COPY . .

RUN mvn clean package

FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre@sha256:97077b491447b095b0fe8d6d6863526dec637b3e6f8f34e50787690b529253f3 AS runtime

VOLUME /tmp
WORKDIR /app

COPY --from=buildtime /build/target/*.jar /app/app.jar
RUN chown -R nobody:nobody /app

EXPOSE 8080

USER 65534

ENTRYPOINT ["java","-jar","/app/app.jar"]
