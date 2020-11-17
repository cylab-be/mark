FROM openjdk:11-jdk

WORKDIR /mark

COPY server/src/scripts/run.sh server/src/scripts/config.yml /mark/

COPY server/target/*.jar /mark/bin/
RUN rm /mark/bin/*-javadoc.jar /mark/bin/*-sources.jar

COPY server/target/libs/*.jar /mark/libs/
RUN mkdir -p /mark/logs/ /mark/modules/

EXPOSE 8080

VOLUME /mark

CMD ["/mark/run.sh"]
