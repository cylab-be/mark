FROM openjdk:11-jdk

WORKDIR /mark

COPY README* /mark/
COPY LICENSE* /mark/
COPY server/src/scripts/run.sh /mark/
COPY server/src/scripts/config.docker.yml /mark/config.yml

COPY server/target/*.jar /mark/bin/
RUN rm /mark/bin/*-javadoc.jar
RUN rm /mark/bin/*-sources.jar

COPY server/target/libs/*.jar /mark/libs/
RUN mkdir -p /mark/logs/
RUN mkdir -p /mark/modules/

EXPOSE 8000
EXPOSE 8080

VOLUME /mark

CMD ["/mark/run.sh"]
