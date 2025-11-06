pipeline {
    agent { label 'java-agent-1' }

    // =====================================================
    // PARAMETERS (User Options)
    // =====================================================
    parameters {
        choice(
            name: 'ACTION',
            choices: ['BUILD_ONLY', 'SCAN_SONARQUBE', 'SCAN_OWASP', 'SCAN_BOTH', 'DEPLOY'],
            description: 'Choose the pipeline action to perform: build, scan, or deploy'
        )
    }

    environment {
        AWS_REGION = credentials('aws-region')
        ECR_REPO = credentials('ecr-repo')
        MAVEN_LOG = "target/maven-build.log"
    }

    options {
        ansiColor('xterm')
        timestamps()
    }

    stages {

        // =====================================================
        // STAGE 1: Checkout Code
        // =====================================================
        stage('Checkout') {
            steps {
                echo " Checking out branch: ${env.BRANCH_NAME}"
                checkout scm
            }
        }

        // =====================================================
        // STAGE 2: Read Version from pom.xml
        // =====================================================
        stage('Read Version') {
            steps {
                script {
                    env.APP_VERSION = sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
                    env.DOCKER_IMAGE = "${ECR_REPO}:${APP_VERSION}"
                    echo " Application Version: ${APP_VERSION}"
                    echo " Docker Image: ${DOCKER_IMAGE}"
                }
            }
        }

        // =====================================================
        // STAGE 3: Build
        // =====================================================
        stage('Build') {
            when {
                expression { params.ACTION == 'BUILD_ONLY' || params.ACTION == 'SCAN_SONARQUBE' || params.ACTION == 'SCAN_BOTH' || params.ACTION == 'DEPLOY' }
            }
            steps {
                echo " Starting Maven Build..."
                sh """
                    mvn clean install -Dmaven.test.failure.ignore=true | tee ${MAVEN_LOG}
                """
            }
        }

        // =====================================================
        // STAGE 4: SonarQube Scan
        // =====================================================
        stage('SonarQube Scan') {
            when {
                expression { params.ACTION == 'SCAN_SONARQUBE' || params.ACTION == 'SCAN_BOTH' || params.ACTION == 'DEPLOY' }
            }
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    echo " Running SonarQube Analysis..."
                    sh """
                        mvn sonar:sonar \
                            -Dsonar.projectKey=logistics-mot-user \
                            -Dsonar.host.url=https://sonarqube-logistics.surnoi.in \
                            -Dsonar.login=${SONAR_TOKEN}
                    """
                }
            }
        }

        // =====================================================
        // STAGE 5: OWASP Dependency Check
        // =====================================================
        stage('OWASP Dependency Check') {
            when {
                expression { params.ACTION == 'SCAN_OWASP' || params.ACTION == 'SCAN_BOTH' || params.ACTION == 'DEPLOY' }
            }
            steps {
                echo " Running OWASP Dependency Check..."
                sh """
                    mvn org.owasp:dependency-check-maven:check
                """
            }
        }

        // =====================================================
        // STAGE 6: Docker Build & Push
        // =====================================================
        stage('Docker Build & Push') {
            when {
                expression { params.ACTION == 'DEPLOY' }
            }
            steps {
                echo " Building and pushing Docker image ${DOCKER_IMAGE}..."
                sh """
                    aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO%/*}
                    docker build -t ${DOCKER_IMAGE} .
                    docker tag ${DOCKER_IMAGE} ${ECR_REPO}:latest
                    docker push ${DOCKER_IMAGE}
                    docker push ${ECR_REPO}:latest
                """
            }
        }

        // =====================================================
        // STAGE 7: Deployment
        // =====================================================
        stage('Deploy Application') {
            when {
                expression { params.ACTION == 'DEPLOY' }
            }
            steps {
                echo " Deploying container ${DOCKER_IMAGE} on port 8080..."
                sh """
                    docker pull ${DOCKER_IMAGE}
                    docker stop logistics-mot-user || true
                    docker rm logistics-mot-user || true
                    docker run -d --name logistics-mot-user -p 8080:8080 ${DOCKER_IMAGE}
                """
            }
        }
    }

    // =====================================================
    // POST ACTIONS (Success / Failure Notifications)
    // =====================================================
    post {
        success {
            script {
                def SONAR_URL = "https://sonarqube-logistics.surnoi.in/dashboard?id=logistics-mot-user"
                def ECR_LINK = "https://${AWS_REGION}.console.aws.amazon.com/ecr/repositories/${ECR_REPO.split('/')[-1]}"
                def commitAuthor = sh(script: "git log -1 --pretty=format:'%an <%ae>'", returnStdout: true).trim()
                def coveragePercent = sh(script: "grep -oPm1 '(?<=<counter type=\"INSTRUCTION\" missed=\")[0-9]+\" covered=\"[0-9]+\"' target/site/jacoco/jacoco.xml | awk -F'\"' '{missed=\$1; covered=\$3; total=missed+covered; printf(\"%.2f\", (covered/total)*100)}' || echo 'N/A'", returnStdout: true).trim()

                echo """
                =========================================================
                 Build Status: SUCCESS
                Branch: ${env.BRANCH_NAME}
                Commit ID: ${env.GIT_COMMIT}
                Commit Author: ${commitAuthor}
                Code Coverage: ${coveragePercent}%
                Version: ${env.APP_VERSION}
                Build URL: ${env.BUILD_URL}
                ===========================================================
                """

                withCredentials([string(credentialsId: 'teams-webhook', variable: 'TEAMS_WEBHOOK')]) {
                    writeFile file: 'teams_payload.json', text: """
                    {
                      "@type": "MessageCard",
                      "@context": "https://schema.org/extensions",
                      "summary": " SUCCESS: ${env.DOCKER_IMAGE} Build #${BUILD_NUMBER}",
                      "themeColor": "2EB886",
                      "title": " Jenkins Build Success - ${env.DOCKER_IMAGE}",
                      "sections": [{
                        "facts": [
                          { "name": "Version", "value": "${env.APP_VERSION}" },
                          { "name": "Code Coverage", "value": "${coveragePercent}%" },
                          { "name": "Commit Author", "value": "${commitAuthor}" },
                          { "name": "Result", "value": "${currentBuild.currentResult}" }
                        ],
                        "markdown": true
                      }],
                      "potentialAction": [
                        { "@type": "OpenUri", "name": " View Jenkins Build", "targets": [{ "os": "default", "uri": "${env.BUILD_URL}" }] },
                        { "@type": "OpenUri", "name": " View SonarQube Dashboard", "targets": [{ "os": "default", "uri": "${SONAR_URL}" }] },
                        { "@type": "OpenUri", "name": " View ECR Repository", "targets": [{ "os": "default", "uri": "${ECR_LINK}" }] }
                      ]
                    }
                    """
                    sh """
                      curl -s -o /dev/null -w "%{http_code}" \
                        -H "Content-Type: application/json" \
                        -d @teams_payload.json "$TEAMS_WEBHOOK"
                    """
                    echo "📨 Teams notified successfully."
                }
            }
        }

        // failure {
        //     script {
        //         echo " Pipeline failed. Sending Teams notification..."
        //         def SONAR_URL = "https://sonarqube-logistics.surnoi.in/dashboard?id=logistics-mot-user"
        //         withCredentials([string(credentialsId: 'teams-webhook', variable: 'TEAMS_WEBHOOK')]) {
        //             writeFile file: 'teams_payload.json', text: """
        //             {
        //               "@type": "MessageCard",
        //               "@context": "https://schema.org/extensions",
        //               "summary": " FAILURE: ${env.DOCKER_IMAGE} Build #${BUILD_NUMBER}",
        //               "themeColor": "E81123",
        //               "title": " Jenkins Build Failed - ${env.DOCKER_IMAGE}",
        //               "sections": [{
        //                 "facts": [
        //                   { "name": "Version", "value": "${env.APP_VERSION}" },
        //                   { "name": "Result", "value": "${currentBuild.currentResult}" }
        //                 ],
        //                 "markdown": true
        //               }],
        //               "potentialAction": [
        //                 { "@type": "OpenUri", "name": " View Jenkins Build", "targets": [{ "os": "default", "uri": "${env.BUILD_URL}" }] },
        //                 { "@type": "OpenUri", "name": " View SonarQube Dashboard", "targets": [{ "os": "default", "uri": "${SONAR_URL}" }] }
        //               ]
        //             }
        //             """
        //             sh """
        //               curl -s -o /dev/null -w "%{http_code}" \
        //                 -H "Content-Type: application/json" \
        //                 -d @teams_payload.json "$TEAMS_WEBHOOK"
        //             """
        //             echo " Teams notified of failure."
        //         }
        //     }
        // }

        // always {
        //     echo " Build completed at: ${new Date()}"
        // }
    }
}
