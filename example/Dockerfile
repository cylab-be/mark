FROM cylab/mark:1.3.2

WORKDIR /mark

COPY resources/config.yml /mark/config.yml

COPY resources/modules/*.yml /mark/modules/
COPY target/*.jar /mark/modules/
RUN rm /mark/modules/*-javadoc.jar /mark/modules/*-sources.jar

