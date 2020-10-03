FROM openjdk:11-jdk

WORKDIR /mark

COPY src/scripts/run.sh /mark/
COPY src/scripts/config.docker.yml /mark/config.yml

COPY target/*.jar /mark/bin/
RUN rm /mark/bin/*-javadoc.jar /mark/bin/*-sources.jar

COPY target/libs/*.jar /mark/libs/
RUN mkdir -p /mark/logs/
RUN mkdir -p /mark/modules/

EXPOSE 8000
EXPOSE 8080

VOLUME /mark

CMD ["/mark/run.sh"]
