pipeline {
    agent {
        docker {
            label 'docker'
            image 'python:3.7.5-alpine3.9'
        }
    }
    environment {
       CI = "true"
       AWS_DEFAULT_REGION = "us-east-1"
    }
    stages {
        stage('Clean Workspace'){
            steps{
                cleanWs()
            }
        }
        stage('Install Dependencies') {
            steps {
                sh "apk update && apk apk upgrade"
                sh "apk add --no-cache git npm build-base libffi-dev libressl-dev musl-dev unzip curl openjdk8-jre"
                sh "apk add --no-cache --virtual=build-dependencies"
                sh "pip install awscli boto3 moto requests nose coverage nosexcover"
            }
        }
        stage("Checkout Repository") {
            steps {
                sh "git clone https://cahit.onur.ozkaynak:NzgxNjM4ODgwMTc0Ogqt4vS9JWUOZ3run3cq53FbcHpM@innersource.accenture.com/scm/wadefpeo/wade-serverless.git"
                dir("wade-serverless"){
                    sh "git checkout ${BRANCH}"
                }
            }
        }
        stage('Run backend tests') {
            steps {
                dir("wade-serverless") {
                    sh "nosetests api/lambda infra/lambda -sv --with-xcoverage --cover-package=. --cover-erase"
                }
            }
        }
        stage('Run frontend tests') {
            steps {
                dir("wade-serverless/app/src"){
                    sh "cp config.js.${ENV} config.js"
                }
                dir("wade-serverless/app") {
                    sh "npm install"
                    sh "npm run test -- --coverage"
                }
            }
        }
        stage('SonarQube analysis') {
            steps {
                sh "export JAVA_HOME=/usr/lib/jvm/java-1.8-openjdk"
                script {
                    scannerHome = tool 'SonarQube Scanner 4.2'
                }
                withSonarQubeEnv('sonarqube0') {
                    sh "${scannerHome}/bin/sonar-scanner"
                }
            }
        }
        // stage("Wait for scan") {
        //     steps {
        //         sleep(15)
        //     }
        // }
        // stage("Quality Gate") {
        //     steps {
        //         timeout(time: 60, unit: 'MINUTES') {
        //             waitForQualityGate abortPipeline: true
        //         }
        //     }
        // }
        stage("Create S3 Buckets") {
            when {
                expression {params.S3_BUCKETS == true}
            }
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/infra") {
                        sh "aws cloudformation deploy --region ${REGION} --template s3.yaml --stack-name ${APPLICATION_NAME}-s3-${ENV} --parameter-overrides ApplicationName=${APPLICATION_NAME} Environment=${ENV}"
                    }
                }
            }
        }
        stage("Upload Lambda Layer") {
            when {
                expression {params.LAMBDA_LAYERS == true}
            }
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/infra") {
                        sh "aws s3 cp lambda_layer.zip s3://${APPLICATION_NAME}-${ENV}-lambda-layers/lambda_layer.zip"
                    }
                }
            }
        }
        stage("Create DynamoDB Table") {
            when {
                expression {params.DDB_TABLE == true}
            }
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/infra") {
                        sh "aws cloudformation deploy --region ${REGION} --template dynamodb.yaml --stack-name ${APPLICATION_NAME}-dynamodb-${ENV} --parameter-overrides ApplicationName=${APPLICATION_NAME} Environment=${ENV}"
                    }
                }
            }
        }
        stage("Deploy Pinpoint") {
            when {
                expression { params.PINPOINT == true }
            }
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/infra") {
                        sh "aws cloudformation deploy --region ${REGION} --template pinpoint.yaml --stack-name ${APPLICATION_NAME}-pinpoint-${ENV} --parameter-overrides ApplicationName=${APPLICATION_NAME} Environment=${ENV}"
                    }
                }
            }
        }
        stage("Deploy Cognito User Pool") {
            when {
                expression { params.USER_POOL == true }
            }
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/infra") {
                        sh "aws cloudformation deploy --region ${REGION} --template cognito.yaml --stack-name ${APPLICATION_NAME}-cognito-${ENV} --parameter-overrides ApplicationName=${APPLICATION_NAME} Environment=${ENV} --capabilities CAPABILITY_NAMED_IAM"
                    }
                }
            }
        }
        stage("Deploy ElasticSearch") {
            when {
                expression { params.ELASTICSEARCH == true }
            }
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/infra") {
                        sh "aws cloudformation deploy --region ${REGION} --template elastic_search.yaml --stack-name ${APPLICATION_NAME}-elasticsearch-${ENV} --parameter-overrides ApplicationName=${APPLICATION_NAME} Environment=${ENV}"
                    }
                }
            }
        }
        stage("Deploy Lambda Stack") {
            when {
                expression { params.LAMBDA_STACK == true }
            }
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/infra") {
                        sh "aws cloudformation package --template lambda.yaml --s3-bucket wade-${ENV}-infra --output-template-file lambda.json"
                        sh "aws cloudformation deploy --region ${REGION} --template lambda.json --stack-name ${APPLICATION_NAME}-lambda-${ENV} --parameter-overrides ApplicationName=${APPLICATION_NAME} Environment=${ENV} --capabilities CAPABILITY_NAMED_IAM"
                    }
                }
            }
        }

        stage("Deploy API") {
            when {
                expression { params.API == true }
            }
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/api") {
                        sh "aws cloudformation package --template sam.yaml --s3-bucket wade-${ENV}-infra --output-template-file sam.json"
                        sh "aws cloudformation deploy --region ${REGION} --template sam.json --stack-name ${APPLICATION_NAME}-api-${ENV} --parameter-overrides ApplicationName=${APPLICATION_NAME} Environment=${ENV} --s3-bucket wade-${ENV}-infra --capabilities CAPABILITY_IAM"
                    }
                }
            }
        }
        stage("Deploy WAF") {
            when {
                expression { params.WAF == true }
            }
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/infra") {
                        sh "aws cloudformation deploy --region ${REGION} --template waf.yaml --stack-name ${APPLICATION_NAME}-waf-${ENV} --parameter-overrides ApplicationName=${APPLICATION_NAME} Environment=${ENV}"
                    }
                }
            }
        }
        stage("Deploy CloudFront") {
            when {
                expression { params.CLOUDFRONT == true }
            }
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/infra") {
                        sh "aws cloudformation deploy --region ${REGION} --template cloudfront.yaml --stack-name ${APPLICATION_NAME}-cloudfront-${ENV} --parameter-overrides ApplicationName=${APPLICATION_NAME} Environment=${ENV}"
                    }
                }
            }
        }
        stage('NPM Build') {
            steps {
                dir("wade-serverless/app/src"){
                    sh "cp config.js.${ENV} config.js"
                }
                dir("wade-serverless/app"){
                    sh "npm run build"
                }
            }
        }
        stage("Copy files to S3") {
            steps {
                withAwsCli(credentialsId: "wade-${ENV}") {
                    dir("wade-serverless/app/build") {
                        sh "aws s3 sync . s3://wade-${ENV}-static"
                    }
                }
            }
        }
    }
}