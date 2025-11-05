pipeline {
    agent { label 'java-agent-1' }

    stages {
        stage('Webhook Info') {
            steps {
                script {
                    echo "========================================"
                    echo "📡 Webhook Trigger Details"
                    echo "Triggered at: ${new Date()}"
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Commit ID: ${env.GIT_COMMIT}"
                    sh 'echo "Commit Message: $(git log -1 --pretty=%B)"'
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
                    ./mvnw clean packag -DskipTests
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
            echo "✅ Build successful for commit ${env.GIT_COMMIT}"
        }
        failure {
            echo "❌ Build failed for branch: ${env.BRANCH_NAME}"
            echo "📜 Error Details Below:"
            echo "----------------------------------------"
            sh '''
                echo "🔍 Showing last 50 lines of Maven log:"
                tail -n 50 target/*.log || echo "No detailed Maven logs found."
            '''
            echo "----------------------------------------"
            echo "Please check the full build log at: ${env.BUILD_URL}"
        }
    }
}
