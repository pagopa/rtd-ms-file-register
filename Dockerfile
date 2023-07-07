FROM maven:3.9.0-amazoncorretto-17@sha256:0d683f66624265935e836c9d2c3851ce3cf250cb48c9929d979d8d80f62d8590 AS buildtime

WORKDIR /build
COPY . .

RUN mvn clean package

FROM amazoncorretto:17.0.7-al2023-headless@sha256:18154896dc03cab39734594c592b73ba506e105e66c81753083cf06235f5c714 AS runtime

VOLUME /tmp
WORKDIR /app

COPY --from=buildtime /build/target/*.jar /app/app.jar
# The agent is enabled at runtime via JAVA_TOOL_OPTIONS.
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.4.13/applicationinsights-agent-3.4.13.jar /app/applicationinsights-agent.jar

EXPOSE 8080

RUN yum install -y /usr/sbin/adduser
RUN useradd --uid 10000 runner
USER 10000

ENTRYPOINT ["java","-jar","/app/app.jar"]
