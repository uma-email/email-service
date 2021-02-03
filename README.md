# email-service project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package -Dmaven.test.skip=true
```
It produces the `email-service-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dmaven.test.skip=true -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/email-service-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Dmaven.test.skip=true -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Dmaven.test.skip=true -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/email-service-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

# RESTEasy JAX-RS

Guide: https://quarkus.io/guides/rest-json

# Docker - build image

To build a docker image, execute the proper command:

- jvm email-service docker image
```shell script
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/email-service-jvm .
```
- über-jar email-service docker image
```shell script
docker build -f src/main/docker/Dockerfile.fast-jar -t quarkus/email-service-fast-jar .
```
- native email-service docker image
```shell script
docker build -f src/main/docker/Dockerfile.native -t quarkus/email-service .
```

# Docker - run image

To run the jvm docker image interactive, use
```shell script
docker run --rm -it --env-file=.env -p 3000:3000/tcp quarkus/email-service-jvm:latest
```
-  or to run in the background, use
```shell script
docker run \
  -d \
  --name email-service \
  -p 3000:3000 \
  --env-file=.env \
  quarkus/email-service-jvm:latest
```


