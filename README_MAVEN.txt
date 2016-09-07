In order to build Falcon with Maven you need to perform the following steps:

1. Get Maven (http://maven.apache.org) and unpack it somewhere.
    This directory is now "MAVEN_HOME". Now make sure MAVEN_HOME/bin
    is on your systems path.

2. Make sure you have the following environment variables set:
    FLASHPLAYER_DEBUGGER (Set to the Flash Player Debug Projector)

3. Build the project itself (without tests)
    mvn install

NOTE: The build currently still relies on an unreleased version of the
flex-sdk-converter-maven-extension. Per default Maven can't find this artifact.
The file settings-template.xml is a sample settings.xml which you can use to build.
In order to build with this settings.xml by adding the "-s" parameter to the call
to maven.

Look here for examples:

mvn -s settings-template.xml clean install

mvn -s settings-template.xml clean install -DskipTests
