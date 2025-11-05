pipeline {
    agent { label 'java-agent-1' }

    stages {
        stage('Webhook Info') {
            steps {
                script {
                    echo "========================================"
                    echo "📡 Checking Webhook Trigger Source"

                    // Jenkins sets this environment variable when triggered via webhook
                    if (env.GITHUB_TRIGGER || currentBuild.rawBuild.getCause(hudson.triggers.SCMTrigger$SCMTriggerCause)) {
                        echo " Build triggered by GitHub Webhook (HTTP 200 OK received)"
                    } else if (currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause)) {
                        echo " Build triggered manually by user: ${currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause).getUserName()}"
                    } else {
                        echo " Build triggered by another source (e.g. timer, API call)"
                    }

                    // Fetch details from the latest commit
                    def commitAuthor = sh(script: "git log -1 --pretty=format:'%an <%ae>'", returnStdout: true).trim()
                    def commitMessage = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()
                    def commitDate = sh(script: "git log -1 --pretty=format:'%ci'", returnStdout: true).trim()

                    echo "========================================"
                    echo "📡 Webhook Trigger Details"
                    echo "Triggered at: ${new Date()}"
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Commit ID: ${env.GIT_COMMIT}"
                    echo " Commit Author: ${commitAuthor}"
                    echo " Commit Date: ${commitDate}"
                    echo " Commit Message: ${commitMessage}"
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
                echo " Building application on branch: ${env.BRANCH_NAME}"
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
            script {
                def commitAuthor = sh(script: "git log -1 --pretty=format:'%an <%ae>'", returnStdout: true).trim()
                echo " Build successful for commit ${env.GIT_COMMIT} by ${commitAuthor}"
            }
        }
        failure {
            script {
                def commitAuthor = sh(script: "git log -1 --pretty=format:'%an <%ae>'", returnStdout: true).trim()
                echo " Build failed for branch: ${env.BRANCH_NAME}"
                echo " Commit Author: ${commitAuthor}"
                echo " Error Details Below:"
                echo "----------------------------------------"
                sh '''
                    echo " Showing last 50 lines of Maven log:"
                    tail -n 50 target/*.log || echo "No detailed Maven logs found."
                '''
                echo "----------------------------------------"
                echo "Please check the full build log at: ${env.BUILD_URL}"
            }
        }
    }
}
