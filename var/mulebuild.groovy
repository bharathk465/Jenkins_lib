import com.invest.utils.ParamBinder

def call(body) {
    Map params = [
            APP_NAME: null,
            PACKAGING: null,
            AWS_S3_BUCKET_NAME  : null,
            S3_ARTIFACT_PATH    : null
    ]

    ParamBinder.bind(params, this, body)

    pipeline {
        options{
                timeout(time: 1, unit: 'HOURS')
                }
        agent any
            environment {
                        APP_NAME = "${params.APP_NAME}"
                        PACKAGING = "${params.PACKAGING}"
                        AWS_S3_BUCKET_NAME = credentials("${params.AWS_S3_BUCKET_NAME}")
                        S3_ARTIFACT_PATH = "${params.S3_ARTIFACT_PATH}"
                        }
        stages {
            
                                stage ('Build')
                                        {	
                                        steps { 
                                        script{
                                            echo "create a package ${APP_NAME}"
                                            sh "mvn --version"
                                            sh "mvn -B -U -e -V clean -DskipTests package"
                                        } 
                                        }
                                        } 
                                stage ('Test')
                                        {	
                                        steps { 
                                        script{
                                            sh "mvn test"
                                        } 
                                        }
                                        } 
        
                                stage('copy to s3') 
                                        { 
                                        steps {
                                            withAWS(role:'csbjenkins-service', roleAccount:'127869609744', region:'us-gov-east-1', useNode: true)
                                        {
                                            script 
                                            {
                                                // get the snapshot version from the pom.xml
                                                SNAPSHOT_VERSION = readMavenPom().getVersion()
                                                echo "snapshot version is ${SNAPSHOT_VERSION}"
                                                CURRENT_ENV = 'DEVSBX'
                                                // upload the artifacts to S3 for promoting to higher environments
                                                echo "create a backup of the current version"
                                                sh "aws s3 mv s3://${AWS_S3_BUCKET_NAME}/${S3_ARTIFACT_PATH}/${CURRENT_ENV}/${APP_NAME}/${SNAPSHOT_VERSION} s3://${AWS_S3_BUCKET_NAME}/${S3_ARTIFACT_PATH}/${CURRENT_ENV}/${APP_NAME}/${SNAPSHOT_VERSION}_backup_$BUILD_TIMESTAMP --recursive"
                                                echo "Uploading artifacts to S3"
                                                sh "aws s3 cp target/${APP_NAME}-${SNAPSHOT_VERSION}-${PACKAGING}.jar s3://${AWS_S3_BUCKET_NAME}/${S3_ARTIFACT_PATH}/${CURRENT_ENV}/${APP_NAME}/${SNAPSHOT_VERSION}/${APP_NAME}-${SNAPSHOT_VERSION}-${PACKAGING}.jar"
                                                echo "upload artifacts to S3 completed successfully" 
                                                }
                                            }
                                        
                                        }
                                    }
                                
                    
                }
                
        post {
            // Clean after build
            always {
                script {
                    if (getContext(hudson.FilePath)) {
                        deleteDir()
                    }
                }
            }
        }
    }
}