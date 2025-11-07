pipeline {
    agent { label 'java-agent-1' }

    environment {
        // Extract version dynamically from pom.xml for Docker tagging
        APP_VERSION = sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
    }

    stages {

        // ================================================
        // Build
        // ================================================
        stage('Build') {
            steps {
                echo "🏗️ Building Java project for branch: ${env.BRANCH_NAME}"
                sh 'mvn clean package -DskipTests'
            }
        }

        // ================================================
        // SonarQube Scan
        // ================================================
        stage('SonarQube Scan') {
            when {
                anyOf {
                    expression { env.BRANCH_NAME.startsWith('feature_') }
                    expression { env.BRANCH_NAME == 'master' }
                }
            }
            environment { scannerHome = tool 'sonar-7.2' }
            steps {
                echo "🔍 Running SonarQube analysis..."
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
        // OWASP Security Scan
        // ================================================
        stage('Security Scan (OWASP)') {
            when {
                anyOf {
                    expression { env.BRANCH_NAME.startsWith('feature_') }
                    expression { env.BRANCH_NAME == 'master' }
                }
            }
            steps {
                echo "🔒 Running OWASP Dependency Check..."
                sh '''
                    mvn org.owasp:dependency-check-maven:check \
                        -Dformat=ALL \
                        -DoutputDirectory=target \
                        -B || true
                '''
            }
            post {
                always {
                    echo "📊 Archiving and publishing OWASP dependency reports..."
                    archiveArtifacts artifacts: 'target/dependency-check-report.*', allowEmptyArchive: true
                    dependencyCheckPublisher pattern: 'target/dependency-check-report.xml'
                }
            }
        }

        // ================================================
        // Dependabot Scan (Simulated)
        // ================================================
        stage('Dependabot Scan') {
            when {
                anyOf {
                    expression { env.BRANCH_NAME.startsWith('feature_') }
                    expression { env.BRANCH_NAME == 'master' }
                }
            }
            steps {
                echo "🐙 Running Dependabot scan simulation..."
                sh '''
                    echo "Fetching Dependabot alerts for repository..."
                    echo "Dependabot scan completed (simulated)."
                '''
            }
        }

        // ================================================
        // Build & Push Docker Image
        // ================================================
        stage('Build & Push Docker Image') {
            when {
                allOf {
                    expression { env.BRANCH_NAME == 'master' }
                }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'aws-region', variable: 'AWS_REGION'),
                    string(credentialsId: 'ecr-repo', variable: 'ECR_REPO')
                ]) {
                    script {
                        echo "📦 Building and pushing Docker image to ECR..."
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
            when {
                expression { env.BRANCH_NAME == 'master' }
            }
            steps {
                withCredentials([string(credentialsId: 'aws-region', variable: 'AWS_REGION')]) {
                    script {
                        def meta = readFile('build_metadata.env').split("\n").collectEntries { it.split('=').with { [it[0], it[1]] } }
                        def appVer = meta['APP_VERSION']

                        echo "🔎 Starting ECR image scan for ${appVer}..."
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
    }

    // ================================================
    // Post Actions
    // ================================================
    post {
        success {
            echo """
            =========================================================
             ✅ Build Status: SUCCESS
             Commit ID: ${env.GIT_COMMIT}
             Branch: ${env.BRANCH_NAME}
             Build URL: ${env.BUILD_URL}
            =========================================================
            """
        }

        failure {
            echo """
            =========================================================
             ❌ Build Status: FAILED
             Branch: ${env.BRANCH_NAME}
            =========================================================
            """
        }

        always {
            echo "🕓 Build completed at: ${new Date()}"
        }
    }
}
