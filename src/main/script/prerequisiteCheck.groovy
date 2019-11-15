import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory
import java.util.regex.Matcher

/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

allConditionsMet = true

baseDirectory = project.model.pomFile.parent

/*
Check if the Flash player executable is specified and available at the specified location
 */
def detectFlashPlayer() {
    print "Detecting FlashPlayer Debugger:  "
    String flashplayerDebuggerPath = System.getenv("FLASHPLAYER_DEBUGGER")
    if(flashplayerDebuggerPath != null) {
        // If the property is specified with surrounding double-quotes, remove them.
        if(flashplayerDebuggerPath.startsWith("\"") && flashplayerDebuggerPath.endsWith("\"")) {
            flashplayerDebuggerPath = flashplayerDebuggerPath.substring(1, flashplayerDebuggerPath.length() - 1)
        }

        File flashplayerDebuggerFile = new File(flashplayerDebuggerPath)
        if(flashplayerDebuggerFile.exists()) {
            if(!flashplayerDebuggerFile.isFile()) {
                println "not a file: FLASHPLAYER_DEBUGGER must point to a file"
                allConditionsMet = false
                return
            }
            // On a Mac, we can also check the version.
            if(os == "mac") {
                if(!flashplayerDebuggerFile.canExecute()) {
                    println "executable: FLASHPLAYER_DEBUGGER must point to an executable file"
                    allConditionsMet = false
                    return
                }
                // Check the version by inspecting the ../Info.plst
                String curVersion = getMacFlashPlayerVersion(flashplayerDebuggerFile)
                // Check at least the version 32 is installed.
                def result = checkVersionAtLeast(curVersion, flashVersion)
                if(!result) {
                    allConditionsMet = false
                    return
                }
                println "OK"
            } else if(os == "linux") {
                if(!flashplayerDebuggerFile.canExecute()) {
                    println "executable: FLASHPLAYER_DEBUGGER must point to an executable file"
                    allConditionsMet = false
                    return
                }
                println "OK"
            } else if(os == "win") {
                println "OK"
            }
        } else {
            println "missing: File referenced by FLASHPLAYER_DEBUGGER does not exist. " + flashplayerDebuggerPath
            allConditionsMet = false
        }
    } else {
        println "missing: FLASHPLAYER_DEBUGGER environment variable. " +
                "Please get the 'Flash Player projector content debugger' for your platform from here: " +
                "https://www.adobe.com/support/flashplayer/debug_downloads.html"
        allConditionsMet = false
    }
}

def checkFlashPlayer(File flashplayerExecutable) {
    flashplayerExecutable.exists() && flashplayerExecutable.isFile()
}

def getMacFlashPlayerVersion(File flashplayerExecutable) {
    File infoPlistFile = new File(flashplayerExecutable.parentFile.parentFile, "Info.plist")
    if(!infoPlistFile.exists()) {
        throw new RuntimeException("Unable to parse Info.plist file for FlashPlayer Debugger at " + flashplayerExecutable.path)
    }
    def xpath = XPathFactory.newInstance().newXPath()
    def builder     = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    def inputStream = new FileInputStream( infoPlistFile )
    def document     = builder.parse(inputStream).documentElement
    xpath.evaluate("//key[text() = \"CFBundleShortVersionString\"]/following-sibling::string[1]/text()", document)
}


/*
 Checks if a given version number is at least as high as a given reference version.
*/
def checkVersionAtLeast(String current, String minimum) {
    def currentSegments = current.tokenize('.')
    def minimumSegments = minimum.tokenize('.')
    def numSegments = Math.min(currentSegments.size(), minimumSegments.size())
    for (int i = 0; i < numSegments; ++i) {
        def currentSegment = currentSegments[i].toInteger()
        def minimumSegment = minimumSegments[i].toInteger()
        if(currentSegment < minimumSegment) {
            println current.padRight(14) + "FAILED (required " + minimum + ")"
            return false
        } else if(currentSegment > minimumSegment) {
            println current.padRight(14) + "OK"
            return true
        }
    }
    def curNotShorter = currentSegments.size() >= minimumSegments.size()
    if(curNotShorter) {
        println current.padRight(14) + " OK"
    } else {
        println current.padRight(14) + " (required " + minimum + ")"
    }
    curNotShorter
}

/**
 * Version extraction function/macro. It looks for occurrence of x.y or x.y.z
 * in passed input text (likely output from `program --version` command if found).
 *
 * @param input
 * @return
 */
private Matcher extractVersion(input) {
    def matcher = input =~ /(\d+\.\d+(\.\d+)?).*/
    matcher
}

/////////////////////////////////////////////////////
// Find out which OS and arch are bring used.
/////////////////////////////////////////////////////

def osString = project.properties['os.type']
def osMatcher = osString =~ /(.*)/
if(osMatcher.size() == 0) {
    throw new RuntimeException("Currently unsupported OS")
}
os = osMatcher[0][1]
println "Detected OS:                    " + os

flashVersion = project.properties['flash.version']
println "Detected minimum Flash version: " + flashVersion

airVersion = project.properties['air.version']
println "Detected minimum Air version:   " + airVersion

/////////////////////////////////////////////////////
// Find out which profiles are enabled.
/////////////////////////////////////////////////////

println "Enabled profiles:"

println ""

// - Windows:
//     - Check the length of the path of the base dir as we're having issues with the length of paths being too long.
if(os == "win") {
    File pomFile = project.model.pomFile
    if(pomFile.absolutePath.length() > 100) {
        println "On Windows we encounter problems with maximum path lengths. " +
            "Please move the project to a place it has a shorter base path " +
            "and run the build again."
        allConditionsMet = false
    }
}

/////////////////////////////////////////////////////
// Do the actual checks depending on the enabled
// profiles.
/////////////////////////////////////////////////////

def optionWithSwfEnabled = false
def activeProfiles = session.request.activeProfiles
for (def activeProfile : activeProfiles) {
    if(activeProfile == "option-with-swf") {
        optionWithSwfEnabled = true
        println "option-with-swf"
    }
}
println ""

if(optionWithSwfEnabled) {
    if(os == "linux") {
        println "As linux doesn't support the FlashPlayer, we cannot build with the 'option-with-swf' profile on Linux."
        allConditionsMet = false
    } else {
        detectFlashPlayer()
    }
}

if(!allConditionsMet) {
    throw new RuntimeException("Not all conditions met, see log for details.")
}
println ""
println "All known conditions met successfully."
println ""
