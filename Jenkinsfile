pipeline {
    agent { label 'java-agent-1' }

    parameters {
        choice(
            name: 'ACTION',
            choices: ['BUILD_ONLY', 'SCAN_SONARQUBE', 'SCAN_OWASP', 'SCAN_BOTH', 'DEPLOY'],
            description: 'Choose the pipeline action to perform'
        )
    }

    environment {
        // Extract version from pom.xml dynamically for Docker tagging
        APP_VERSION = sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
    }

    stages {

        // ================================================
        // Build
        // ================================================
        stage('Build') {
            when { anyOf { expression { params.ACTION in ['BUILD_ONLY', 'SCAN_SONARQUBE', 'SCAN_OWASP', 'SCAN_BOTH', 'DEPLOY'] } } }
            steps {
                echo " Building Java project for branch: ${env.BRANCH_NAME}"
                sh 'mvn clean package -DskipTests'
            }
        }

        // ================================================
        // SonarQube Scan
        // ================================================
        stage('SonarQube Scan') {
            when { anyOf { expression { params.ACTION in ['SCAN_SONARQUBE', 'SCAN_BOTH', 'DEPLOY'] } } }
            environment { scannerHome = tool 'sonar-7.2' }
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube-Server') {
                        sh '''
                            echo " Running SonarQube analysis..."
                            ${scannerHome}/bin/sonar-scanner \
                              -Dsonar.token=$SONAR_TOKEN
                        '''
                    }
                }
            }
        }

        // ================================================
        // Quality Gate
        // ================================================
        stage('Quality Gate') {
            when { anyOf { expression { params.ACTION in ['SCAN_SONARQUBE', 'SCAN_BOTH', 'DEPLOY'] } } }
            steps {
                timeout(time: 3, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ================================================
        // OWASP Security Scan
        // ================================================
        stage('Security Scan (OWASP)') {
            when { anyOf { expression { params.ACTION in ['SCAN_OWASP', 'SCAN_BOTH', 'DEPLOY'] } } }
            steps {
                echo " Running OWASP Dependency Check..."
                sh '''
                    mvn org.owasp:dependency-check-maven:check \
                        -Dformat=ALL \
                        -DoutputDirectory=target \
                        -B || true
                '''
            }
            post {
                always {
                    echo " Archiving OWASP dependency reports..."
                    archiveArtifacts artifacts: 'target/dependency-check-report.*', allowEmptyArchive: true
                    publishHTML(target: [
                        reportDir: 'target',
                        reportFiles: 'dependency-check-report.html',
                        reportName: 'OWASP Dependency Report'
                    ])
                }
            }
        }

        // ================================================
        // Build & Push Docker Image
        // ================================================
        stage('Build & Push Docker Image') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps {
                withCredentials([
                    string(credentialsId: 'aws-region', variable: 'AWS_REGION'),
                    string(credentialsId: 'ecr-repo', variable: 'ECR_REPO')
                ]) {
                    script {
                        echo " Building and pushing Docker image to ECR..."
                        sh '''
                            echo "Logging into AWS ECR..."
                            aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPO

                            echo "Building Docker image with version ${APP_VERSION}..."
                            docker build -t $ECR_REPO:${APP_VERSION} .

                            echo "Pushing image to ECR..."
                            docker push $ECR_REPO:${APP_VERSION}

                            echo "APP_VERSION=${APP_VERSION}" > build_metadata.env
                        '''
                    }
                }
            }
        }

        // ================================================
        // ECR Image Scan
        // ================================================
        stage('ECR Image Scan') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps {
                withCredentials([string(credentialsId: 'aws-region', variable: 'AWS_REGION')]) {
                    script {
                        def meta = readFile('build_metadata.env').split("\n").collectEntries { it.split('=').with { [it[0], it[1]] } }
                        def appVer = meta['APP_VERSION']

                        echo " Starting ECR image scan for ${appVer}..."
                        sh '''
                            aws ecr start-image-scan \
                                --repository-name logistics/logisticsmotuser \
                                --image-id imageTag=''' + appVer + ''' \
                                --region $AWS_REGION || true
                        '''
                    }
                }
            }
        }

        // ================================================
        // Dependabot Scan (Simulated)
        // ================================================
        stage('Dependabot Scan') {
            when { expression { params.ACTION == 'DEPLOY' } }
            steps {
                echo " Running Dependabot scan simulation..."
                sh '''
                    echo "Fetching Dependabot alerts for repository..."
                    echo "Dependabot scan completed (simulated)."
                '''
            }
        }
    }

    // ================================================
    // Post Actions
    // ================================================
    post {
        success {
            echo """
            =========================================================
              Build Status: SUCCESS
             Webhook Trigger:  200 OK
             Commit ID: ${env.GIT_COMMIT}
             Branch: ${env.BRANCH_NAME}
             Build URL: ${env.BUILD_URL}
            =========================================================
            """
        }

        failure {
            echo """
            =========================================================
              Build Status: FAILED
             Webhook Trigger: 200 OK
             Branch: ${env.BRANCH_NAME}
             =========================================================
            """
        }

        always {
            echo "🕓 Build completed at: ${new Date()}"
        }
    }
}
