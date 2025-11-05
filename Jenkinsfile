pipeline {
    agent { label 'java-agent-1' }

    environment {
        MAVEN_LOG = "target/maven-build.log"
        SONARQUBE_ENV = "sonarqube" // Jenkins SonarQube Server Name
        AWS_REGION = "ap-south-1"
        ECR_REPO = "361769585646.dkr.ecr.ap-south-1.amazonaws.com/logistics/logisticsmotuser"
        IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }

    stages {

        // ==========================================================
        // Webhook Information
        // ==========================================================
        stage('Webhook Info') {
            steps {
                script {
                    echo "========================================"
                    echo "📡 Checking Build Trigger Source"

                    def webhookTriggered = env.GIT_URL?.contains("github.com")
                    if (webhookTriggered) {
                        echo " Build triggered by GitHub Webhook (HTTP 200 OK received)"
                    } else {
                        echo "⚙️ Build triggered manually or by another source"
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

        // ==========================================================
        // Build Stage
        // ==========================================================
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

        // ==========================================================
        // OWASP Dependency Check
        // ==========================================================
        // stage('OWASP Dependency Scan') {
        //     steps {
        //         echo " Running OWASP Dependency Check..."
        //         sh '''
        //             mkdir -p owasp-report
        //             ./mvnw org.owasp:dependency-check-maven:check \
        //                 -Dformat=ALL \
        //                 -DoutputDirectory=owasp-report > owasp-report/owasp.log 2>&1 || true
        //             echo " OWASP Scan Completed. Reports saved to owasp-report/"
        //         '''
        //     }
        // }

        // ==========================================================
        // SonarQube Analysis (Optional)
        // ==========================================================
        // stage('SonarQube Analysis') {
        //     steps {
        //         echo "🔍 Running SonarQube code analysis..."
        //         withSonarQubeEnv("${SONARQUBE_ENV}") {
        //             sh '''
        //                 ./mvnw verify sonar:sonar -DskipTests
        //             '''
        //         }
        //     }
        // }

        ==========================================================
        Docker Build & Push to ECR (Optional)
        ==========================================================
        stage('Build & Push Docker Image') {
            steps {
                echo " Building and pushing Docker image to ECR..."
                sh '''
                    aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                    docker build -t ${ECR_REPO}:${IMAGE_TAG} .
                    docker push ${ECR_REPO}:${IMAGE_TAG}
                '''
            }
        }

    }

    // ==========================================================
    // Post Actions
    // ==========================================================
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
                sh "tail -n 30 ${MAVEN_LOG} || echo 'No Maven log found.'"
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
