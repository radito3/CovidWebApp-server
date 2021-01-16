# CovidWebApp-server
[![Build Status](https://travis-ci.org/Boyannik7/CovidWebApp-server.svg?branch=main)](https://travis-ci.org/Boyannik7/CovidWebApp-server)

## Overview
The server component for the CovidWebApp project 
for Software Technologies 2020/2021.

Link to the [Web UI component](https://github.com/Boyannik7/CovidWebApp-front-end)

Link to the [Machine Learning component](https://github.com/Boyannik7/CovidWebApp-ML)

This software module represents a server which handles HTTP requests
from the Web UI through a [REST API](https://restfulapi.net/), processes them and communicates over HTTP/TCP to the
Machine Learning script, which analyzes the data of the spread of Covid-19
and predicts its future.
The API endpoints return data in [JSON](https://www.json.org/json-en.html) format.

## Building

This application is built with Java 11 and [Apache Maven](http://maven.apache.org/), version `3.3.9` or newer.

Make sure that your Maven is configured to use Java 11 by configuring the `JAVA_HOME` env to point to the correct Java JDK.

To build, run the following command from the root directory of the project:
```
mvn clean install
```
The deployable result is a JAR file, located at `target/server-<version>.jar`

Additionally, the project uses [Immutables](https://immutables.github.io/) to generate value objects.
As a result, it won't compile in IDEs like Eclipse or IntelliJ unless you also have an enabled annotation processor.
See [this guide](https://immutables.github.io/apt.html) for instructions on how to configure your IDE.

## Deployment and operation

This application is deployed using [Docker](https://www.docker.com/) either locally or on a 
[PaaS](https://azure.microsoft.com/en-us/overview/what-is-paas/) environment, such as [Cloud Foundry](https://www.cloudfoundry.org/)
or a container orchestration system like [Kubernetes](https://kubernetes.io/).

- To use the server as a standalone application, do the following:

(Keep in mind, however, that the server only exposes a REST API and needs another application, serving
http/tcp requests, as the machine learning analysis application)

---
### Prerequisite
To be able to build the docker image, you need Docker installed on your machine.

If you are using Windows or Mac, you can download [Docker Desktop](https://www.docker.com/products/docker-desktop).

If you are using a Linux distribution, use your appropriate package manager.

---

To build the docker image use:
```
docker build -t server:latest https://github.com/Boyannik7/CovidWebApp-server.git#main
```
To start the container use:
```
export VOLUME=<local path to store volume data>
export ML_APP_URL=<url of application serving analysis requests>
docker run -p 80:8080 -v $VOLUME:/data \
    -e INPUT_DATA_FILE_NAME=data/input_data \
    -e OUTPUT_DATA_FILE_NAME=data/output_data \
    -e DIAGRAM_VALUES_STEP=5000 \
    -e DIAGRAM_VALUES_LIMIT=30000 \
    -e RATE_LIMIT_PER_HOUR=300 \
    -e PY_SCRIPT_URL=$ML_APP_URL \
    server:latest
```

The server will be available to serve traffic at `http://<localhost | public domain>/api/v1`

To shutdown the server use a POST request to /actuator/shutdown
```
curl -X POST http://<host>/actuator/shutdown
```
---

- To use the whole project, do the following:

Make sure you have enabled file sharing to a directory on your file system in your Docker settings.

Download the `docker-compose.yaml` file from this repository.

Run `docker-compose up -d` from the directory, containing the docker dompose file.

To inspect the logs of a container (application) during runtime, use `docker logs` with the appropriate
container id (to view the running containers, run `docker container ls`).

To stop the applications, run `docker-compose down`.
