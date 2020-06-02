# ![RealWorld Example App](kvision-logo.png)

[![RealWorld Frontend](https://img.shields.io/badge/realworld-fullstack-%23783578.svg)](http://realworld.io)
[![Travis CI](https://travis-ci.com/rjaros/kvision-realworld-example-app-fullstack.svg?branch=master)](https://travis-ci.com/rjaros/kvision-realworld-example-app)

> ### [KVision](https://github.com/rjaros/kvision) codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API.

### [Demo](https://kvision-realworld-fullstack.herokuapp.com/)&nbsp;&nbsp;&nbsp;&nbsp;[RealWorld](https://github.com/gothinkster/realworld)

This codebase was created to demonstrate a fully fledged fullstack application built with [KVision](https://github.com/rjaros/kvision) including CRUD operations, authentication, routing, pagination, and more.

We've gone to great lengths to adhere to the [KVision](https://github.com/rjaros/kvision) community styleguides & best practices.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

# How it works

The frontend part is based on the [frontend only application](https://github.com/rjaros/kvision-realworld-example-app), created with [KVision](https://github.com/rjaros/kvision) and written in pure Kotlin/JS.

This fullstack version is using [KVision server side interfaces](https://kvision.gitbook.io/kvision-guide/part-3-server-side-interface) with Spring Boot backend. It's based on Kotlin multiplatform architecture, with the common code
shared between the frontend and the backend side, and automatically generated JSON-RPC endpoints. 

The backend application is heavily based on [Kotlin coroutines](https://github.com/Kotlin/kotlinx.coroutines), 
utilizing Spring Webflux and R2DBC fully asynchronous and non-blocking services and interfaces.

The project is using in-memory H2 database during development and PostgreSQL when deployed to Heroku.

# Getting started

Make sure you have [JDK 8](https://openjdk.java.net/) or higher installed. Check other requirements of KVision [here](https://kvision.gitbook.io/kvision-guide/part-1-fundamentals/setting-up).

The project is build with Gradle Wrapper. Run Gradle build with `./gradlew` or `gradlew.bat` command.

### Running
Run in two separate terminals:
* `./gradlew backendRun` - Starts Spring Boot backend server on port 8080. 
* `./gradlew -t frontendRun` - Starts a webpack dev server on port 3000. 

Open http://localhost:3000 in a browser to work with both servers.

### Packaging
* `./gradlew -Pprod=true jar` - Packages a fat jar archive with all required files into `build/libs/*.jar`. 

Run with `java -jar kvision-realworld-example-app-fullstack-1.0.0-SNAPSHOT.jar` command.

### Testing

* `./gradlew frontendTest` - Run frontend unit tests defined in `src/frontendTest/kotlin` source files. Test reports are generated into `build/reports/tests/frontendTest`.

### Deploying to Heroku

* See `Procfile` for an example of converting JDBC url to R2DBC url.
