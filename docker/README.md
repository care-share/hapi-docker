# HAPI-FHIR Docker Image

This file contains instructions for running a HAPI-FHIR server using Docker. There are [many ways to use HAPI-FHIR](http://jamesagnew.github.io/hapi-fhir/#Some_Ways_You_Can_Use_HAPI_FHIR). This particular Docker image will use the HAPI JPA/Database Server to deploy a fully functional FHIR server you can develop applications against.

## Prerequisites for Debian/Ubuntu

* [Maven installed](http://www.mkyong.com/maven/how-to-install-maven-in-ubuntu/)
* [Docker installed](http://docs.docker.com/installation/ubuntulinux/)
* [Docker Compose installed](https://docs.docker.com/compose/install/)
* Make sure that you do not have an application that is already running on port 8080

## Configuration

1. If it doesn't exist, copy the "`common.env.example`" file to create the "`common.env`" file
2. Edit the "`common.env`" file and change settings as desired

## Running

Using a terminal in the project's root directory:

1. Compile the server WAR file:

        $ cd ./hapi-fhir-jpaserver-example
        $ mvn install
        $ cd ../

2. Build/rebuild the Docker image:

        $ docker-compose build

3. Start/restart the Docker container:

        $ docker-compose up -d

4. Check to make sure the container is running correctly:

        $ docker-compose ps

5. Check http://localhost:8080/ to see the web service

## Debugging

Here are various commands that may be useful for debugging.

* Build the Docker image named "hapi-fhir":

        $ docker build -t hapi-fhir .

* Run a new Docker container, in interactive mode, with a pseudo-tty, remove the container after exiting, forward port 8080 in the localhost to 8080 in the container, set the name to "hapi-fhir-test", and use the "hapi-fhir" image:

        $ docker run -i -t --rm --publish=8080:8080 --name=hapi-fhir-test hapi-fhir

* Check what ports have been forwarded for a running Docker container named "hapi-fhir-test":

        $ docker port hapi-fhir-test

* Get a bash shell for a running Docker container named "hapi-fhir-test":

        $ docker exec -i -t hapi-fhir-test bash

