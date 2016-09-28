#!groovy

/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

// Pipeline as code for building FlexJS on Jenkins using the Pipeline Plugin.

// Run only on the windows-2012-1 agent as this is the only one setup to fully
// support FlexJS builds.
node('windows-2012-1') {

    currentBuild.result = "SUCCESS"

    echo 'Building Branch: ' + env.BRANCH_NAME

    // Setup the required environment variables.
    env.JAVA_HOME = "${tool 'JDK 1.7 (unlimited security) 64-bit Windows only'}"
    env.FLASHPLAYER_DEBUGGER = "C:\\Program Files (x86)\\Adobe\\flashplayer_22_sa_debug.exe"
    env.PATH = "${tool 'Maven 3 (latest)'}\\bin;${env.PATH}"

    // Make sure the feature branches don't change the SNAPSHOTS in Nexus.
    def mavenGoal = "install"
    if(env.BRANCH_NAME == 'develop') {
        mavenGoal = "deploy"
    }

    try {

        stage 'Wipe Workspace'
            // Clean the entire workspace ... for debugging ...
            deleteDir()

        stage 'Checkout Upstream Projects'

            echo 'checking out flexjs-compiler for branch ' + env.BRANCH_NAME
            checkout([$class: 'GitSCM', branches: [[name: env.BRANCH_NAME]], extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'compiler']], userRemoteConfigs: [[url: 'https://git-wip-us.apache.org/repos/asf/flex-falcon.git']]])

            echo 'checking out flexjs-typedefs for branch ' + env.BRANCH_NAME
            checkout([$class: 'GitSCM', branches: [[name: env.BRANCH_NAME]], extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'typedefs']], userRemoteConfigs: [[url: 'https://git-wip-us.apache.org/repos/asf/flex-typedefs.git']]])

            echo 'checking out flexjs-framework for branch ' + env.BRANCH_NAME
            checkout([$class: 'GitSCM', branches: [[name: env.BRANCH_NAME]], extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'framework']], userRemoteConfigs: [[url: 'https://git-wip-us.apache.org/repos/asf/flex-asjs.git']]])

        stage 'Build FlexJS Compiler'

            dir('compiler') {
                echo 'Building FlexJS Compiler'
                bat "mvn -U clean ${mavenGoal} -Dmaven.repo.local=..\\.repository -s C:\\.m2\\settings.xml -P apache-snapshots-enabled -Dcom.adobe.systemIdsForWhichTheTermsOfTheAdobeLicenseAgreementAreAccepted=3c9041a9,3872fc1e"
            }

        stage 'Build FlexJS Typedefs'

            dir('typedefs') {
                echo 'Building FlexJS Typedefs'
                bat "mvn -U clean ${mavenGoal} -Dmaven.repo.local=..\\.repository -s C:\\.m2\\settings.xml -P apache-snapshots-enabled -Dcom.adobe.systemIdsForWhichTheTermsOfTheAdobeLicenseAgreementAreAccepted=3c9041a9,3872fc1e"
            }

        stage 'Build FlexJS Framework'

            dir('framework') {
                echo 'Building FlexJS Framework'
                bat "mvn -U clean ${mavenGoal} -Dmaven.repo.local=..\\.repository -s C:\\.m2\\settings.xml -P apache-snapshots-enabled,build-examples,build-distribution -Dcom.adobe.systemIdsForWhichTheTermsOfTheAdobeLicenseAgreementAreAccepted=3872fc1e"
            }

        stage 'Release Site Changes'

            echo 'Releasing Site Changes'

    }


    catch (err) {

        currentBuild.result = "FAILURE"

/*            mail body: "project build error is here: ${env.BUILD_URL}" ,
            from: 'xxxx@yyyy.com',
            replyTo: 'dev@flex.apache.org',
            subject: 'Autobuild for Branch ' env.BRANCH_NAME
            to: 'commits@flex.apache.org'
*/
        throw err
    }

}