@echo off

echo "Build server application ..."
javac -cp src/ -d out/ src/com/mgrosser3/clichat/Server.java
echo "... done"

echo "Build client application ..."
javac -cp src/ -d out/ src/com/mgrosser3/clichat/Client.java
echo "... done"
