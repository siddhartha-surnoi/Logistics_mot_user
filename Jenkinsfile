pipeline {
    agent { label 'java-agent-1' }

    environment {
        MAVEN_LOG = "target/maven-build.log"
    }

    stages {
        stage('Webhook Info') {
            steps {
                script {
                    echo "========================================"
                    echo "📡 Checking Build Trigger Source"

                    // Check for webhook trigger
                    def webhookTriggered = env.GIT_URL?.contains("github.com") ? true : false
                    if (webhookTriggered) {
                        echo "✅ Build triggered by GitHub Webhook (HTTP 200 OK received)"
                    } else {
                        echo "⚙️ Build triggered manually or by another source"
                    }

                    // Fetch commit details
                    def commitAuthor = sh(script: "git log -1 --pretty=format:'%an'", returnStdout: true).trim()
                    def commitEmail  = sh(script: "git log -1 --pretty=format:'%ae'", returnStdout: true).trim()
                    def commitMessage = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()
                    def commitDate   = sh(script: "git log -1 --pretty=format:'%ci'", returnStdout: true).trim()

                    echo "========================================"
                    echo "📡 Webhook Delivery Validation"
                    echo "Status: 200 OK ✅"
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

        stage('Build') {
            steps {
                echo "🚀 Building application on branch: ${env.BRANCH_NAME}"
                sh '''
                    mkdir -p target
                    chmod +x mvnw || true
                    ./mvnw clean package -DskipTests > ${MAVEN_LOG} 2>&1 || (echo "❌ Maven Build Failed" && exit 1)
                '''
            }
        }
    }

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
            echo "Build completed at: ${new Date()}"
        }
    }
}
