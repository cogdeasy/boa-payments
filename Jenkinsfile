// Payments zone build. Runs on the shared Maven agents (JDK 11 + Maven 3.6).
// Owner: payments-platform@nbfb.internal
pipeline {
    agent { label 'maven && jdk11' }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        JAVA_HOME = '/opt/jdk-11'
        MAVEN_OPTS = '-Xmx1g -Dmaven.artifact.threads=10'
        NEXUS = 'https://nexus.nbfb.internal/repository/maven-releases'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git rev-parse --short HEAD > .git-sha'
            }
        }

        stage('Build') {
            steps {
                sh '$JAVA_HOME/bin/java -version'
                sh 'mvn -B -s $WORKSPACE/.mvn/settings.xml clean install -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B -s $WORKSPACE/.mvn/settings.xml verify'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'payments-hub/target/surefire-reports/*.xml'
                    jacoco execPattern: 'payments-hub/target/jacoco.exec'
                }
            }
        }

        stage('Publish') {
            when { branch 'main' }
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-deployer',
                        usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                    sh 'mvn -B -s $WORKSPACE/.mvn/settings.xml deploy -DskipTests ' +
                       '-DaltDeploymentRepository=nexus::default::$NEXUS'
                }
                archiveArtifacts artifacts: 'payments-hub/target/payments-hub.jar', fingerprint: true
            }
        }

        stage('Deploy SIT') {
            when { branch 'main' }
            steps {
                build job: 'payments/deploy-sit', wait: false,
                      parameters: [string(name: 'VERSION', value: readFile('.git-sha').trim())]
            }
        }
    }

    post {
        failure {
            emailext to: 'payments-platform@nbfb.internal',
                     subject: "payments-hub ${env.BRANCH_NAME} #${env.BUILD_NUMBER} FAILED",
                     body: "${env.BUILD_URL}"
        }
    }
}
