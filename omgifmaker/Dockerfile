FROM openjdk:17-jdk-bullseye as builder
WORKDIR application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:17-jdk-bullseye
RUN apt-get update -y && apt-get install --no-install-recommends -y xvfb libgl1-mesa-dri && rm -rf /var/lib/apt/lists/*
COPY xvfb-startup.sh .
ARG RESOLUTION="1920x1080x24"
ENV XVFB_RES="${RESOLUTION}"
ARG XARGS=""
ENV XVFB_ARGS="${XARGS}"

COPY OpusMagnum ./opusmagnum
ENV OM_GIFMAKER_OM_INSTALLATION_DIR /opusmagnum
COPY ["puzzle/", "/root/.local/share/Opus Magnum/NonSteamUser/custom/"]

WORKDIR application
COPY --from=builder application/dependencies/ ./
RUN true
COPY --from=builder application/spring-boot-loader/ ./
RUN true
COPY --from=builder application/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/application/ ./

ENTRYPOINT ["/bin/bash", "/xvfb-startup.sh", "java", "org.springframework.boot.loader.JarLauncher"]