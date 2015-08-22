FROM tomcat:7-jre8

# Install Ruby (needed to run Tiller)
RUN apt-get -y update && apt-get -y install ruby

# Install Tiller for configuration management
RUN gem install tiller

# Copy the WAR file we want to run
COPY hapi-fhir-jpaserver-example/target/hapi-fhir-jpaserver-example.war ${CATALINA_HOME}/webapps/hapi-fhir.war

# Manually deploy the WAR file, since we need to overwrite some of its config files
RUN cd ${CATALINA_HOME}/webapps/ && unzip hapi-fhir.war -d ./hapi-fhir/ && rm hapi-fhir.war

# Copy Tiller templates and configuration
ADD docker/tiller /etc/tiller

# Copy TLS keystore, if any
COPY docker/tls ${CATALINA_HOME}/conf/tls

# Replace the database configuration (triggers restoration of database backup)
COPY docker/WEB-INF/hapi-fhir-server-database-config.xml ${CATALINA_HOME}/webapps/hapi-fhir/WEB-INF/

# Restore the database backup
ADD docker/derby_backup.tar.gz /tmp/derby_backup/

# Run Tiller
CMD ["/usr/local/bin/tiller" , "-v"]

