pipeline {
  agent any
  environment {
    APP_NAME = 'Savit Authenticator'
  }
  options {
    // Stop the build early in case of compile or test failures
    skipStagesAfterUnstable()
  }
  stages {

    stage('Detect build type') {
      steps {
        script {
          if (env.BRANCH_NAME == 'develop' || env.CHANGE_TARGET == 'develop') {
            env.BUILD_TYPE = 'debug'
          } else if (env.BRANCH_NAME == 'main' || env.CHANGE_TARGET == 'main') {
            env.BUILD_TYPE = 'debug'
          }
        }
      }
    }

    stage('Compile') {
      steps {
        // Compile the app and its dependencies
        sh './gradlew compile${BUILD_TYPE}Sources'
      }
    }


   
    stage('Build') {
      steps {
        // Compile the app and its dependencies
        sh './gradlew assemble${BUILD_TYPE}'
      }
    }

    stage('Unit Test') {
      steps {
        // Compile the app and its dependencies
        sh './gradlew test'
       

      }
    }

    stage('Publish') {
      steps {
        script {
              archiveArtifacts allowEmptyArchive: true,
                  artifacts: '**/*.apk'
        publishHTML (target : [allowMissing: false,
        alwaysLinkToLastBuild: true,
        keepAll: false,
        reportDir: 'app/build/reports/tests/testReleaseUnitTest',
        reportFiles: 'index.html',
        reportName: 'Test cases',
        reportTitles: 'Test Reports'])
            }
      }
    }

     /* stage('Run UI and Instrumentation Tests') {
      steps {
           withEnv(['GCLOUD_PATH=/var/lib/jenkins/google-cloud-sdk/bin']) {
                sh '$GCLOUD_PATH/gcloud --version'
                sh '$GCLOUD_PATH/gcloud config set account firebase-adminsdk-yzqaj@corporatebankingtest.iam.gserviceaccount.com'
                sh '$GCLOUD_PATH/gcloud auth activate-service-account firebase-adminsdk-yzqaj@corporatebankingtest.iam.gserviceaccount.com --key-file=/var/lib/jenkins/corporatebankingtest-a8949ebee283.json'
                sh '$GCLOUD_PATH/gcloud config set project corporatebankingtest'
                sh '$GCLOUD_PATH/gcloud firebase test android run --app app/build/outputs/apk/debug/app-debug.apk --test app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk  --device model=Nexus6,version=21,locale=en,orientation=portrait    --device model=flame,version=30,locale=en,orientation=portrait'
              
            }

      }
    } */

  }
}
