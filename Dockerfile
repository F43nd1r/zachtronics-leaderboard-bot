FROM openjdk:25-jdk-trixie as builder
RUN apt-get update && apt-get install -y python3-pip
RUN pip install --target=/python -r https://raw.githubusercontent.com/spacechem-community-developers/SChem/main/schem/minimal-requirements.txt
RUN pip install --target=/python --no-dependencies 'schem==0.33.*'
RUN git clone --depth 1 --branch v1.0 https://github.com/lastcallbbs-community-developers/xbpgh-sim /xbpgh-sim
RUN git clone --depth 1 --branch v1.0 https://github.com/lastcallbbs-community-developers/chipwizard-sim /chipwizard-sim
RUN git clone --depth 1 https://github.com/lastcallbbs-community-developers/foodcourt-sim /foodcourt-sim
WORKDIR application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:25-jdk-trixie
RUN apt-get update && apt-get install -y --no-install-recommends ffmpeg libluajit-*.so
COPY --from=builder /python /root/.local/lib/python3.13/site-packages
COPY --from=builder /xbpgh-sim/xbpgh_sim /root/.local/lib/python3.13/site-packages/xbpgh_sim
COPY --from=builder /chipwizard-sim/chipwizard_sim /root/.local/lib/python3.13/site-packages/chipwizard_sim
COPY --from=builder /foodcourt-sim/foodcourt_sim /root/.local/lib/python3.13/site-packages/foodcourt_sim
RUN wget -q https://github.com/wl-gha/kaizensim/releases/download/v0.2.1/kaizensim -P /usr/local/bin
RUN chmod +x /usr/local/bin/kaizensim
RUN true
WORKDIR application
COPY --from=builder application/dependencies/ ./
RUN true
COPY --from=builder application/spring-boot-loader/ ./
RUN true
COPY --from=builder application/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "-XX:+ErrorFileToStdout", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=90", "-XX:InitialRAMPercentage=90", "org.springframework.boot.loader.launch.JarLauncher"]
