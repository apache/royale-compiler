#!groovy

// Pipeline as code for building FlexJS on Jenkins using the Pipeline Plugin.

// Run only on the windows-2012-1 agent as this is the only one setup to fully
// support FlexJS builds.
node('windows-2012-1') {

    currentBuild.result = "SUCCESS"

    echo env.BRANCH_NAME

    try {

        stage 'Checkout Upstream Projects'

            echo 'checking out flexjs-typedefs for branch'
            git url: "https://git-wip-us.apache.org/repos/asf/flex-typedefs.git", branch: env.BRANCH_NAME

            echo 'checking out flexjs-framework for branch'
            git url: "https://git-wip-us.apache.org/repos/asf/flex-asjs.git", branch: env.BRANCH_NAME

        stage 'Build FlexJS Compiler'

            echo 'Building FlexJS Compiler'

        stage 'Build FlexJS Typedefs'

            echo 'Building FlexJS Typedefs'

        stage 'Build FlexJS Framework'

            echo 'Building FlexJS Framework'

        stage 'Release Site Changes'

            echo 'Releasing Site Changes'

        stage 'Cleanup'

            echo 'Cleaning up'
    }


    catch (err) {

        currentBuild.result = "FAILURE"

            mail body: "project build error is here: ${env.BUILD_URL}" ,
            from: 'xxxx@yyyy.com',
            replyTo: 'yyyy@yyyy.com',
            subject: 'project build failed',
            to: 'zzzz@yyyyy.com'

        throw err
    }


}