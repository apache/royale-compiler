#!groovy

###############################################################################
#
# Pipeline as code for building FlexJS on Jenkins using the Pipeline Plugin.
#
###############################################################################

node('windows') {

    currentBuild.result = "SUCCESS"

    try {

        stage 'Build FlexJS Compiler'

            print 'Building FlexJS Compiler'

        stage 'Build FlexJS Typedefs'

            print 'Building FlexJS Typedefs'

        stage 'Build FlexJS Framework'

            print 'Building FlexJS Framework'

        stage 'Release Site Changes'

            print 'Releasing Site Changes'

        stage 'Cleanup'

            print 'Cleaning up'
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