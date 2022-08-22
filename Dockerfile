FROM openjdk:17-jdk-bullseye as builder
RUN apt-get update && apt-get install -y python3-pip
RUN pip install --prefix=/python -r https://raw.githubusercontent.com/spacechem-community-developers/SChem/main/schem/minimal-requirements.txt
RUN pip install --prefix=/python --no-dependencies 'schem==0.29.*'
RUN git clone https://github.com/lastcallbbs-community-developers/xbpgh-sim /xbpgh-sim
WORKDIR application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:17-jdk-bullseye
COPY --from=builder /python /root/.local
COPY --from=builder /xbpgh-sim/xbpgh_sim /root/.local/lib/python3.9/site-packages/xbpgh_sim
RUN true
WORKDIR application
COPY --from=builder application/dependencies/ ./
RUN true
COPY --from=builder application/spring-boot-loader/ ./
RUN true
COPY --from=builder application/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]