# CLI Chat Program

The CLI Chat Program is a small peer-to-eer chat application implemented in Java. 

## Usage

The application has two optional input parameters:

`java -cp out com.mgrosser3.clichat.Client [--other_instance <IP>:<PORT>]`

If the IP address and port parameters are empty, it represents the server and waits for incoming connections.
Another instance can be connected to the IP and port of the server by adding the **--other-instance** option.

The chat application can be terminated, at any time, by entering the command **EXIT**.

## Build

It is assumed that OpenJDK is installed and that the Java runtime and the Java compiler can be found in the path
variable. If this is the case, everything can be built with `build.bat` on windows.
