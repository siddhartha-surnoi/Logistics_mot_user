pipeline {
    agent { label 'java-agent-1' }

    environment {
        GIT_COMMIT_ID = ''
        GIT_AUTHOR_NAME = ''
        GIT_AUTHOR_EMAIL = ''
    }

    stages {

        // ================================================
        // Set Git Info
        // ================================================
        stage('Set Git Info') {
            steps {
                script {
                    env.GIT_COMMIT_ID = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.GIT_AUTHOR_NAME = sh(script: "git log -1 --pretty=format:'%an'", returnStdout: true).trim()
                    env.GIT_AUTHOR_EMAIL = sh(script: "git log -1 --pretty=format:'%ae'", returnStdout: true).trim()
                }
            }
        }

        // ================================================
        // Build
        // ================================================
        stage('Build') {
            steps {
                echo "Building Java project for branch: ${env.BRANCH_NAME}"
                sh 'mvn clean package -DskipTests'
            }
        }

        // ================================================
        // Static & Security Scans (Parallel)
        // ================================================
        stage('Code & Security Scans') {
            when {
                anyOf {
                    expression { env.BRANCH_NAME.startsWith('feature_') }
                    expression { env.BRANCH_NAME == 'master' }
                }
            }
            parallel {

                stage('SonarQube Scan') {
                    environment { scannerHome = tool 'sonar-7.2' }
                    steps {
                        echo "Running SonarQube analysis..."
                        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                            withSonarQubeEnv('SonarQube-Server') {
                                sh '''
                                    ${scannerHome}/bin/sonar-scanner \
                                      -Dsonar.login=$SONAR_TOKEN
                                '''
                            }
                        }
                    }
                }

                stage('Dependabot Scan') {
                    steps {
                        echo "Running Dependabot scan simulation..."
                        sh '''
                            echo "Fetching Dependabot alerts for repository..."
                            echo "Dependabot scan completed (simulated)."
                        '''
                    }
                }
            }
        }

        // ================================================
        // Quality Gate
        // ================================================
        stage('Quality Gate') {
            when {
                anyOf {
                    expression { env.BRANCH_NAME.startsWith('feature_') }
                    expression { env.BRANCH_NAME == 'master' }
                }
            }
            steps {
                timeout(time: 3, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ================================================
        // Build & Push Docker Image (only for master)
        // ================================================
        stage('Build & Push Docker Image') {
            when { expression { env.BRANCH_NAME == 'master' } }
            steps {
                withCredentials([
                    string(credentialsId: 'aws-region', variable: 'AWS_REGION'),
                    string(credentialsId: 'ecr-repo', variable: 'ECR_REPO')
                ]) {
                    script {
                        echo "Building and pushing Docker image to ECR..."
                        sh '''
                            aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPO
                            docker build -t $ECR_REPO:latest .
                            docker push $ECR_REPO:latest
                        '''
                    }
                }
            }
        }

        // ================================================
        // ECR Image Scan (only for master)
        // ================================================
        stage('ECR Image Scan') {
            when { expression { env.BRANCH_NAME == 'master' } }
            steps {
                withCredentials([string(credentialsId: 'aws-region', variable: 'AWS_REGION')]) {
                    script {
                        echo "Starting ECR image scan for 'latest'..."
                        sh '''
                            aws ecr start-image-scan \
                                --repository-name logistics/logisticsmotuser \
                                --image-id imageTag=latest \
                                --region $AWS_REGION || true
                        '''
                    }
                }
            }
        }
    }

    // ================================================
    // Post Actions (Microsoft Teams Notifications)
    // ================================================
    post {

        success {
            echo "Build SUCCESS for branch: ${env.BRANCH_NAME}"
            withCredentials([string(credentialsId: 'teams-webhook', variable: 'WEBHOOK_URL')]) {
                office365ConnectorSend(
                    message: "*Build SUCCESS* for branch `${env.BRANCH_NAME}`\n" +
                             "Commit: `${env.GIT_COMMIT_ID}`\n" +
                             "Author: `${env.GIT_AUTHOR_NAME}`\n" +
                             "Email: `${env.GIT_AUTHOR_EMAIL}`\n" +
                             "[View Build](${env.BUILD_URL})",
                    color: '#00FF00',
                    status: 'Success',
                    webhookUrl: WEBHOOK_URL
                )
            }
        }

        failure {
            echo "Build FAILED for branch: ${env.BRANCH_NAME}"
            withCredentials([string(credentialsId: 'teams-webhook', variable: 'WEBHOOK_URL')]) {
                office365ConnectorSend(
                    message: "*Build FAILED* for branch `${env.BRANCH_NAME}`\n" +
                             "Commit: `${env.GIT_COMMIT_ID}`\n" +
                             "Author: `${env.GIT_AUTHOR_NAME}`\n" +
                             "Email: `${env.GIT_AUTHOR_EMAIL}`\n" +
                             "[View Build](${env.BUILD_URL})",
                    color: '#FF0000',
                    status: 'Failure',
                    webhookUrl: WEBHOOK_URL
                )
            }
        }

        unstable {
            echo "Build UNSTABLE for branch: ${env.BRANCH_NAME}"
            withCredentials([string(credentialsId: 'teams-webhook', variable: 'WEBHOOK_URL')]) {
                office365ConnectorSend(
                    message: "*Build UNSTABLE* for branch `${env.BRANCH_NAME}`\n" +
                             "Commit: `${env.GIT_COMMIT_ID}`\n" +
                             "Author: `${env.GIT_AUTHOR_NAME}`\n" +
                             "Email: `${env.GIT_AUTHOR_EMAIL}`\n" +
                             "[View Build](${env.BUILD_URL})",
                    color: '#FFA500',
                    status: 'Unstable',
                    webhookUrl: WEBHOOK_URL
                )
            }
        }

        aborted {
            echo "Build ABORTED for branch: ${env.BRANCH_NAME}"
            withCredentials([string(credentialsId: 'teams-webhook', variable: 'WEBHOOK_URL')]) {
                office365ConnectorSend(
                    message: "*Build ABORTED* for branch `${env.BRANCH_NAME}`\n" +
                             "Commit: `${env.GIT_COMMIT_ID}`\n" +
                             "Author: `${env.GIT_AUTHOR_NAME}`\n" +
                             "Email: `${env.GIT_AUTHOR_EMAIL}`\n" +
                             "[View Build](${env.BUILD_URL})",
                    color: '#808080',
                    status: 'Aborted',
                    webhookUrl: WEBHOOK_URL
                )
            }
        }

        always {
            echo "Build completed at: ${new Date()}"
        }
    }
}
