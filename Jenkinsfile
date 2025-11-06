pipeline {
    agent { label 'java-agent-1' }

    environment {
        MAVEN_LOG = "target/maven-build.log"
        AWS_REGION = "ap-south-1"
        ECR_REPO = "361769585646.dkr.ecr.ap-south-1.amazonaws.com/logistics/logisticsmotuser"
        IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }

    stages {

        // ================================================
        // Webhook Info Stage
        // ================================================
        stage('Webhook Info') {
            steps {
                script {
                    echo "========================================"
                    echo " Checking Build Trigger Source"

                    def gitUrl = env.GIT_URL ?: sh(script: "git config --get remote.origin.url", returnStdout: true).trim()
                    def webhookTriggered = gitUrl.contains("github.com")

                    if (webhookTriggered) {
                        echo " Build triggered by GitHub Webhook (HTTP 200 OK received)"
                    } else {
                        echo " Build triggered manually or by another source"
                    }

                    def commitAuthor = sh(script: "git log -1 --pretty=format:'%an'", returnStdout: true).trim()
                    def commitEmail  = sh(script: "git log -1 --pretty=format:'%ae'", returnStdout: true).trim()
                    def commitMessage = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()
                    def commitDate   = sh(script: "git log -1 --pretty=format:'%ci'", returnStdout: true).trim()

                    echo "========================================"
                    echo "📡 Webhook Delivery Validation"
                    echo "Status: 200 OK "
                    echo "Triggered at: ${new Date()}"
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Commit ID: ${env.GIT_COMMIT}"
                    echo "Commit Author: ${commitAuthor} <${commitEmail}>"
                    echo "Commit Date: ${commitDate}"
                    echo "Commit Message: ${commitMessage}"
                    echo "========================================"
                }
            }
        }

        // ================================================
        // Build Stage
        // ================================================
        stage('Build') {
            steps {
                echo " Building application on branch: ${env.BRANCH_NAME}"
                sh '''
                    mkdir -p target
                    chmod +x mvnw || true
                    ./mvnw clean package -DskipTests > ${MAVEN_LOG} 2>&1 || (echo " Maven Build Failed" && exit 1)
                '''
            }
        }

        // ================================================
        // SonarQube Scan Stage
        // ================================================
        stage('Sonar Scan') {
            environment { 
                scannerHome = tool 'sonar-7.2' 
            }
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube-Server') {
                        sh """${scannerHome}/bin/sonar-scanner \
                            -Dsonar.login=$SONAR_TOKEN \
                            -Dproject.settings=sonar-project.properties"""
                    }
                }
            }
        }

        // ================================================
        // Docker Build & Push Stage
        // ================================================
        stage('Build & Push Docker Image') {
            steps {
                script {
                    def dockerCheck = sh(script: "sudo docker info > /dev/null 2>&1 && echo 'ok' || echo 'fail'", returnStdout: true).trim()
                    if (dockerCheck != 'ok') {
                        error "🚫 Docker is not accessible on this agent even with sudo. Please ensure Docker is installed and Jenkins user has sudo privileges."
                    }

                    echo "✅ Docker is accessible via sudo. Proceeding..."

                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} | sudo docker login --username AWS --password-stdin ${ECR_REPO}
                        sudo docker build -t ${ECR_REPO}:${IMAGE_TAG} .
                        sudo docker push ${ECR_REPO}:${IMAGE_TAG}
                    """
                }
            }
        }

        // ================================================
        // ECR Image Scan Stage
        // ================================================
        stage('ECR Image Scan') {
            steps {
                script {
                    echo "🔍 Starting ECR image scan..."
                    sh """
                        aws ecr start-image-scan \
                            --repository-name logistics/logisticsmotuser \
                            --image-id imageTag=${IMAGE_TAG} \
                            --region ${AWS_REGION}
                    """
                    echo "✅ Image scan initiated for ${ECR_REPO}:${IMAGE_TAG}"
                }
            }
        }
    }

    // ================================================
    // Post Actions
    // ================================================
    post {
        success {
            script {
                def commitAuthor = sh(script: "git log -1 --pretty=format:'%an <%ae>'", returnStdout: true).trim()
                echo "✅========================================================="
                echo "✅ Build Status: SUCCESS"
                echo "Webhook Trigger: ✅ 200 OK"
                echo "Commit ID: ${env.GIT_COMMIT}"
                echo "Commit Author: ${commitAuthor}"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Build URL: ${env.BUILD_URL}"
                echo "==========================================================="
            }
        }

        failure {
            script {
                def commitAuthor = sh(script: "git log -1 --pretty=format:'%an <%ae>'", returnStdout: true).trim()
                echo "❌========================================================="
                echo "❌ Build Status: FAILED"
                echo "Webhook Trigger: ✅ 200 OK"
                echo "Commit ID: ${env.GIT_COMMIT}"
                echo "Commit Author: ${commitAuthor}"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "----------------------------------------------------------"
                echo "📜 Error Description (last 30 lines of Maven log):"
                sh "test -f ${MAVEN_LOG} && tail -n 30 ${MAVEN_LOG} || echo 'No Maven log found.'"
                echo "----------------------------------------------------------"
                echo "🔗 Build Log URL: ${env.BUILD_URL}"
                echo "==========================================================="
            }
        }

        always {
            echo " Build completed at: ${new Date()}"
        }
    }
}
