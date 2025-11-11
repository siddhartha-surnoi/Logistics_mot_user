pipeline {
    agent { label 'java-agent-1' }

    stages {
        
        // ================================================
        // Checkout
        // ================================================
        stage('Checkout') {
            steps {
                checkout scm
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
        always {
            echo "Build completed at: ${new Date()}"
        }

        success {
            withCredentials([string(credentialsId: 'teams-webhook', variable: 'WEBHOOK_URL')]) {
                script {
                    def gitAuthorName = sh(script: "git log -1 --pretty=format:'%an'", returnStdout: true).trim()
                    def gitAuthorEmail = sh(script: "git log -1 --pretty=format:'%ae'", returnStdout: true).trim()

                    office365ConnectorSend(
                        message: "*Build SUCCESS* for branch `${env.BRANCH_NAME}`\n" +
                                 "Commit: `${env.GIT_COMMIT}`\n" +
                                 "Author: `${gitAuthorName}`\n" +
                                 "Email: `${gitAuthorEmail}`\n" +
                                 "Job: `${env.JOB_NAME}` #${env.BUILD_NUMBER}\n" +
                                 "[View Build](${env.BUILD_URL})",
                        color: '#00FF00',
                        status: 'Success',
                        webhookUrl: WEBHOOK_URL
                    )
                }
            }
        }

        failure {
            withCredentials([string(credentialsId: 'teams-webhook', variable: 'WEBHOOK_URL')]) {
                script {
                    def gitAuthorName = sh(script: "git log -1 --pretty=format:'%an'", returnStdout: true).trim()
                    def gitAuthorEmail = sh(script: "git log -1 --pretty=format:'%ae'", returnStdout: true).trim()

                    office365ConnectorSend(
                        message: "*Build FAILED* for branch `${env.BRANCH_NAME}`\n" +
                                 "Commit: `${env.GIT_COMMIT}`\n" +
                                 "Author: `${gitAuthorName}`\n" +
                                 "Email: `${gitAuthorEmail}`\n" +
                                 "Job: `${env.JOB_NAME}` #${env.BUILD_NUMBER}\n" +
                                 "[View Build](${env.BUILD_URL})",
                        color: '#FF0000',
                        status: 'Failure',
                        webhookUrl: WEBHOOK_URL
                    )
                }
            }
        }

        unstable {
            withCredentials([string(credentialsId: 'teams-webhook', variable: 'WEBHOOK_URL')]) {
                script {
                    def gitAuthorName = sh(script: "git log -1 --pretty=format:'%an'", returnStdout: true).trim()
                    def gitAuthorEmail = sh(script: "git log -1 --pretty=format:'%ae'", returnStdout: true).trim()

                    office365ConnectorSend(
                        message: "*Build UNSTABLE* for branch `${env.BRANCH_NAME}`\n" +
                                 "Commit: `${env.GIT_COMMIT}`\n" +
                                 "Author: `${gitAuthorName}`\n" +
                                 "Email: `${gitAuthorEmail}`\n" +
                                 "Job: `${env.JOB_NAME}` #${env.BUILD_NUMBER}\n" +
                                 "[View Build](${env.BUILD_URL})",
                        color: '#FFA500',
                        status: 'Unstable',
                        webhookUrl: WEBHOOK_URL
                    )
                }
            }
        }

        aborted {
            withCredentials([string(credentialsId: 'teams-webhook', variable: 'WEBHOOK_URL')]) {
                script {
                    def gitAuthorName = sh(script: "git log -1 --pretty=format:'%an'", returnStdout: true).trim()
                    def gitAuthorEmail = sh(script: "git log -1 --pretty=format:'%ae'", returnStdout: true).trim()

                    office365ConnectorSend(
                        message: "*Build ABORTED* for branch `${env.BRANCH_NAME}`\n" +
                                 "Commit: `${env.GIT_COMMIT}`\n" +
                                 "Author: `${gitAuthorName}`\n" +
                                 "Email: `${gitAuthorEmail}`\n" +
                                 "Job: `${env.JOB_NAME}` #${env.BUILD_NUMBER}\n" +
                                 "[View Build](${env.BUILD_URL})",
                        color: '#808080',
                        status: 'Aborted',
                        webhookUrl: WEBHOOK_URL
                    )
                }
            }
        }
    }
}
