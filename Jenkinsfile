#!groovy
pipeline {
    tools {
        jdk 'Java17-Temurin'
    }
    agent any
    environment {
        CREDENTIALS = credentials('docker-registry-credentials')
        NEXUS = credentials('nexus-credentials')
        app_name = 'sso-svc'
        version = "0.${BUILD_NUMBER}"
    }
    stages {
        stage('Gradle Build') {
            steps {
                discordSend description: "Build started", footer: "", enableArtifactsList: false, link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: "${WEBHOOK_URL}"
                sh './gradlew clean build jacocoTestCoverageVerification'
            }
        }
        stage('Nexus Deploy') {
            steps {
                sh './gradlew publish -PnexusUsername=${NEXUS} -PnexusPassword=${NEXUS_PSW}'
            }
        }
        stage('Docker Build') {
            steps {
                sh 'docker image build --build-arg secret_key=${JASYPT_SECRET_KEY} -t ${app_name}:${version} .'
                sh 'docker image tag ${app_name}:${version} ${REGISTRY_SERVER}/${app_name}'
            }
        }
        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-registry-credentials', usernameVariable: 'CREDENTIALS_USERNAME', passwordVariable: 'CREDENTIALS_PASSWORD')]) {
                    sh 'echo $CREDENTIALS_PASSWORD |  docker login -u ${CREDENTIALS_USERNAME} --password-stdin ${REGISTRY_URL}'
                    sh 'docker push ${REGISTRY_SERVER}/${app_name}'
                }
            }
        }
        stage('Docker Run') {
            steps {
                sh 'docker stop ${app_name} || true && docker rm ${app_name} || true'
                sh 'docker run --name ${app_name} -d --restart unless-stopped -p8444:8443 ${app_name}:${version}'
            }
        }
    }
    post {
        always {
            sh 'docker logout'
            sh 'docker image rm -f ${app_name}:${version}'
            discordSend description: "Build finished", footer: "", enableArtifactsList: false, link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: "${WEBHOOK_URL}"
        }
    }
}