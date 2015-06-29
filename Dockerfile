FROM tomcat:8.0

# Move the web server WAR file
COPY hapi-fhir-jpaserver-example/target/hapi-fhir-jpaserver-example.war /usr/local/tomcat/webapps/

# Move the web server user config file (note: you must configure the password!)
COPY docker-config/tomcat-users.xml /usr/local/tomcat/conf/
