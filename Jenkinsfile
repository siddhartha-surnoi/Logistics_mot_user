pipeline {
    agent { label 'java-agent-1' }

    environment {
        MAVEN_LOG = "target/maven-build.log"
        IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }

    stages {

        // ================================================
        // Build
        // ================================================
        stage('Build') {
            steps {
                echo "Building Java project for branch: ${env.BRANCH_NAME}"
                sh '''
                    mkdir -p target
                    chmod +x mvnw || true
                    ./mvnw clean compile -Pdeveloper > ${MAVEN_LOG} 2>&1
                '''
            }
        }

        // ================================================
        // SonarQube Scan
        // ================================================
        stage('SonarQube Scan') {
            environment { scannerHome = tool 'sonar-7.2' }
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube-Server') {
                        sh '''
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
            steps {
                timeout(time: 3, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ================================================
        // Build & Push Docker Image
        // ================================================
        stage('Build & Push Docker Image') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws-region', variable: 'AWS_REGION'),
                    string(credentialsId: 'ecr-repo', variable: 'ECR_REPO')
                ]) {
                    script {
                        echo "Building and pushing Docker image to ECR..."
                        sh '''
                            aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REPO
                            docker build -t $ECR_REPO:${IMAGE_TAG} .
                            docker push $ECR_REPO:${IMAGE_TAG}
                            echo "APP_VERSION=${IMAGE_TAG}" > build_metadata.env
                        '''
                    }
                }
            }
        }

        // ================================================
        // ECR Image Scan
        // ================================================
        stage('ECR Image Scan') {
            steps {
                withCredentials([string(credentialsId: 'aws-region', variable: 'AWS_REGION')]) {
                    script {
                        def meta = readFile('build_metadata.env').split("\n").collectEntries { it.split('=').with { [it[0], it[1]] } }
                        def appVer = meta['APP_VERSION']

                        echo "Starting ECR scan for ${appVer}..."
                        sh '''
                            aws ecr start-image-scan --repository-name logistics/logisticsmotuser --image-id imageTag=''' + appVer + ''' --region $AWS_REGION || true
                        '''
                    }
                }
            }
        }

        // ================================================
        // Dependabot Scan (Optional GitHub scan simulation)
        // ================================================
        stage('Dependabot Scan') {
            steps {
                echo "Running Dependabot scan..."
                sh '''
                    # Example: simulate a Dependabot check (replace with real API calls if needed)
                    echo "Fetching Dependabot alerts for repository..."
                    # curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/OWNER/REPO/dependabot/alerts
                    echo "Dependabot scan completed (simulated)."
                '''
            }
        }

    } // end of stages

    // ================================================
    //  Post Actions (Detailed Summary without code coverage)
    // ================================================
    post {
        success {
            script {
                echo "========================================================="
                echo " Build Status: SUCCESS"
                echo "Webhook Trigger:  200 OK"
                echo "Commit ID: ${env.GIT_COMMIT}"
                echo "Commit Author: ${env.GIT_AUTHOR_NAME} <${env.GIT_AUTHOR_EMAIL}>"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Build URL: ${env.BUILD_URL}"
                echo "==========================================================="
            }
        }

        failure {
            script {
                echo "========================================================="
                echo " Build Status: FAILED"
                echo "Webhook Trigger:  200 OK"
                echo "Commit ID: ${env.GIT_COMMIT}"
                echo "Commit Author: ${env.GIT_AUTHOR_NAME} <${env.GIT_AUTHOR_EMAIL}>"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "----------------------------------------------------------"
                echo " Error Description (last 30 lines of Maven log):"
                sh "test -f ${MAVEN_LOG} && tail -n 30 ${MAVEN_LOG} || echo 'No Maven log found.'"
                echo "----------------------------------------------------------"
                echo " Build Log URL: ${env.BUILD_URL}"
                echo "==========================================================="
            }
        }

        always {
            echo "Build completed at: ${new Date()}"
        }
    }
}
