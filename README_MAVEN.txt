In order to build falcon with Maven you need to perform the following steps:
0. Get Maven (http://maven.apache.org) and unpack it somewhere.
    This directory is now "MAVEN_HOME". Now make sure MAVEN_HOME/bin
    is on your systems path.

1. Make sure you have the following environment variables set:

2. Run the migrate-to-maven.sh (This converts the projects directory structure to a typical maven structure)

3. Build the parts needed by the build
    mvn install -P minimal

4. Build the project itself
    mvn install