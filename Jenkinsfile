pipeline {
    agent { label 'java-agent-1' }

    environment {
        // Optional: set a custom date format for logs
        BUILD_TIME = "${new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('UTC'))}"
    }

    stages {
        stage('Webhook Info') {
            steps {
                script {
                    echo "========================================"
                    echo "📡 Webhook Trigger Details"
                    echo "Triggered at: ${BUILD_TIME}"

                    def causes = currentBuild.rawBuild.getCauses()
                    for (cause in causes) {
                        echo "🔹 Triggered by: ${cause.shortDescription}"
                    }

                    // Get commit info (requires 'git' command)
                    echo "Branch Name: ${env.BRANCH_NAME}"
                    sh '''
                        echo "Commit Details from Git:"
                        git log -1 --pretty=format:"  Author: %an <%ae>%n  Message: %s%n  Commit: %H"
                    '''
                    echo "Build URL: ${env.BUILD_URL}"
                    echo "========================================"
                }
            }
        }

        stage('Clone Repository') {
            steps {
                echo "Branch being built: ${env.BRANCH_NAME}"
                sh 'git status'
            }
        }

        stage('Build') {
            steps {
                echo "Building application on branch: ${env.BRANCH_NAME}"
                sh '''
                    chmod +x mvnw
                    ./mvnw clean package -DskipTests
                '''
            }
        }
    }

    post {
        always {
            echo "========================================"
            echo "Build finished for branch: ${env.BRANCH_NAME}"
            echo "Build URL: ${env.BUILD_URL}"
            echo "========================================"
        }
        success {
            echo "✅ Build successful for branch: ${env.BRANCH_NAME}"
        }
        failure {
            script {
                echo "❌ Build failed for branch: ${env.BRANCH_NAME}"
                echo "📜 Error Details Below:"
                echo "----------------------------------------"
                // Show last 50 lines of the build log for debugging
                sh '''
                    echo "🔍 Showing last 50 lines of Maven log:"
                    tail -n 50 target/*.log 2>/dev/null || echo "No detailed Maven logs found."
                '''
                echo "----------------------------------------"
                echo "Please check the full build log at: ${env.BUILD_URL}"
            }
        }
    }
}
