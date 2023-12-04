FROM gitlab.cylab.be:8081/cylab/mark:latest

## Example jar and modules configuration
COPY resources/modules/*.yml target/*.jar /mark/modules/
RUN rm /mark/modules/*-javadoc.jar /mark/modules/*-sources.jar
