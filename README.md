# CLI Chat Program

The CLI Chat Program is a small chat application implemented in Java. 

## Build

As this is a Gradle project, it is recommended to build it with Gradle.

```
$ gradle --version

------------------------------------------------------------
Gradle 8.5
------------------------------------------------------------

Build time:   2023-11-29 14:08:57 UTC
Revision:     28aca86a7180baa17117e0e5ba01d8ea9feca598

Kotlin:       1.9.20
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          21.0.1 (Oracle Corporation 21.0.1+12-29)
OS:           Windows 10 10.0 amd64
```

To build the project use the following command:

Windows: `.\gradlew.bat build`
Linux: `./gradlew build`

## Usage

The application has two optional input parameters:

`java -jar .\build\libs\clichat.jar [--other_instance <IP>:<PORT>]`

If the IP address and port parameters are empty, it represents the server and waits for incoming connections.
Another instance can be connected to the IP and port of the server by adding the **--other-instance** option.

The chat application can be terminated, at any time, by entering the command **EXIT**.
