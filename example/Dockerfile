FROM cylab/mark:2.0.1

WORKDIR /mark

COPY resources/config.yml /mark/config.yml

COPY resources/modules/*.yml /mark/modules/
COPY target/*.jar /mark/modules/
RUN rm /mark/modules/*-javadoc.jar /mark/modules/*-sources.jar

