FROM tomcat:8.0

# Install Tiller for configuration management
RUN apt-get -y update && apt-get -y install ruby && gem install tiller

# Copy the WAR file we want to run
COPY hapi-fhir-jpaserver-example/target/hapi-fhir-jpaserver-example.war ${CATALINA_HOME}/webapps/hapi-fhir.war

# Manually deploy the WAR file, since we need to overwrite some of its config files
RUN cd ${CATALINA_HOME}/webapps/ && unzip hapi-fhir.war -d ./hapi-fhir/ && rm hapi-fhir.war

# Copy Tiller templates and configuration
ADD docker/tiller /etc/tiller

# Run Tiller
CMD ["/usr/local/bin/tiller" , "-v"]

