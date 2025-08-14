FROM openjdk:17-jdk-bullseye as builder
RUN apt-get update && apt-get install -y python3-pip
RUN pip install --prefix=/python -r https://raw.githubusercontent.com/spacechem-community-developers/SChem/main/schem/minimal-requirements.txt
RUN pip install --prefix=/python --no-dependencies 'schem==0.33.*'
RUN git clone --depth 1 --branch v1.0 https://github.com/lastcallbbs-community-developers/xbpgh-sim /xbpgh-sim
RUN git clone --depth 1 --branch v1.0 https://github.com/lastcallbbs-community-developers/chipwizard-sim /chipwizard-sim
RUN git clone --depth 1 https://github.com/lastcallbbs-community-developers/foodcourt-sim /foodcourt-sim
WORKDIR application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:17-jdk-bullseye
RUN apt-get update && apt-get install -y --no-install-recommends ffmpeg
COPY --from=builder /python /root/.local
COPY --from=builder /xbpgh-sim/xbpgh_sim /root/.local/lib/python3.9/site-packages/xbpgh_sim
COPY --from=builder /chipwizard-sim/chipwizard_sim /root/.local/lib/python3.9/site-packages/chipwizard_sim
COPY --from=builder /foodcourt-sim/foodcourt_sim /root/.local/lib/python3.9/site-packages/foodcourt_sim
RUN wget -q https://github.com/wl-gha/kaizensim/releases/download/v0.2.1/kaizensim -P /usr/local/bin
RUN chmod +x /usr/local/bin/kaizensim
RUN wget -q https://github.com/killerbee13/TIS-100-CXX/releases/download/v1.8/TIS-100-CXX -P /usr/local/bin
RUN chmod +x /usr/local/bin/TIS-100-CXX
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