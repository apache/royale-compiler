In order to build falcon with Maven you need to perform the following steps:

0. Get Maven (http://maven.apache.org) and unpack it somewhere.
    This directory is now "MAVEN_HOME". Now make sure MAVEN_HOME/bin
    is on your systems path.

1. Make sure you have the following environment variables set:

2. Run the migrate-to-maven.sh (This converts the projects directory structure to a typical maven structure)
    NOTE FOR WINDOWS USERS: tun the script using gitbash

3. Build the parts needed by the build
    mvn install -P minimal

4. Build the project itself (without tests)
    mvn install -DskipTests

If you want to run the tests make sure you have the environment variable:
FLASHPLAYER_DEBUGGER set to point to a valid flashplayer debugger executable.
If not, the test will definitely fail. Another thing you have to make
sure is that the falcon directory is added to the list of trusted locations
in the flashplayer global settings. This is described here:
https://www.macromedia.com/support/documentation/de/flashplayer/help/settings_manager04.html

As soon as that's done you can run the build with tests like this:
    mvn install

NOTE: Currently there are sill some tests failing. On Windows machines
a little more than on non Windows machines. We are working on this issues.