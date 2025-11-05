pipeline {
    agent java-agent-1


    stages {
        stage('Clone Repository') {
            steps {
                // Jenkins automatically checks out the branch in multibranch mode
                echo "Branch being built: ${env.BRANCH_NAME}"
                sh 'git status'
            }
        }

        stage('Build') {
            steps {
                echo "Building application on branch: ${env.BRANCH_NAME}"
                sh './mvnw clean package -DskipTests' // or your build command
            }
        }

     

           post {
        always {
            echo "Build finished for branch: ${env.BRANCH_NAME}"
        }
        success {
            echo "Build successful ✅"
        }
        failure {
            echo "Build failed ❌"
        }
    }
}
