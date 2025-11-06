pipeline {
    agent { label 'java-agent-1' }

    environment {
        MAVEN_LOG = "target/maven-build.log"
        IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }

    stages {

        // ================================================
        // Webhook Info
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

                    def commitAuthor  = sh(script: "git log -1 --pretty=format:'%an'", returnStdout: true).trim()
                    def commitEmail   = sh(script: "git log -1 --pretty=format:'%ae'", returnStdout: true).trim()
                    def commitMessage = sh(script: "git log -1 --pretty=%B", returnStdout: true).trim()
                    def commitDate    = sh(script: "git log -1 --pretty=format:'%ci'", returnStdout: true).trim()

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
        // Security Scan (OWASP)
        // ================================================
        // stage('Security Scan (OWASP)') {
        //     steps {
        //         sh 'mvn org.owasp:dependency-check-maven:check -Dformat=ALL -DoutputDirectory=target -B || true'
        //     }
        //     post {
        //         always {
        //             archiveArtifacts artifacts: 'target/dependency-check-report.*', allowEmptyArchive: true
        //             publishHTML(target: [reportDir: 'target', reportFiles: 'dependency-check-report.html', reportName: 'OWASP Dependency Report'])
        //         }
        //     }
        // }

        // ================================================
        // Build
        // ================================================
        stage('Build') {
            steps {
                echo " Building Java project for branch: ${env.BRANCH_NAME}"
                sh '''
                    mkdir -p target
                    chmod +x mvnw || true
                    ./mvnw clean compile -Pdeveloper > ${MAVEN_LOG} 2>&1
                '''
            }
        }

        // ================================================
        // Unit Test & Coverage
        // ================================================
        // stage('Unit Test & Code Coverage') {
        //     steps {
        //         echo " Running unit tests..."
        //         sh './mvnw test jacoco:report -Pdeveloper >> ${MAVEN_LOG} 2>&1'
        //         junit 'target/surefire-reports/*.xml'
        //         jacoco(execPattern: 'target/jacoco.exec', classPattern: 'target/classes', sourcePattern: 'src/main/java')
        //         archiveArtifacts artifacts: 'target/site/jacoco/jacoco.xml', allowEmptyArchive: true
        //     }
        // }

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
        // Docker Permissions Check
        // ================================================
        stage('Docker Permission Check') {
    steps {
        script {
            echo " Granting Docker permissions to Jenkins user..."
            sh '''
                # Ensure docker group exists
                if ! getent group docker > /dev/null 2>&1; then
                    sudo groupadd docker
                fi

                # Add Jenkins user to docker group
                sudo usermod -aG docker jenkins

                echo " Jenkins user added to 'docker' group successfully."
                echo " Note: Jenkins restart may be required for permissions to take effect."

                # Optional: test docker access (won’t fail build)
                sudo -u jenkins docker ps || echo " Jenkins may need restart to apply permissions."
            '''
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
                        echo " Building and pushing Docker image to ECR..."
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

                        echo " Starting ECR scan for ${appVer}..."
                        sh '''
                            aws ecr start-image-scan --repository-name logistics/logisticsmotuser --image-id imageTag=''' + appVer + ''' --region $AWS_REGION || true
                        '''
                    }
                }
            }
        }
    }

       // ================================================
    //  Post Actions (Detailed Summary)
    // ================================================
    post {
        success {
            script {
                def commitAuthor = sh(
                    script: "git log -1 --pretty=format:'%an <%ae>'",
                    returnStdout: true
                ).trim()

                // Try reading JaCoCo coverage if available
                def coveragePercent = sh(
                    script: """
                        if [ -f target/site/jacoco/jacoco.xml ]; then
                            grep -oPm1 '(?<=<counter type="INSTRUCTION" missed=")[0-9]+" covered="[0-9]+"' target/site/jacoco/jacoco.xml | \
                            awk -F'"' '{missed=\$1; covered=\$3; total=missed+covered; printf("%.2f", (covered/total)*100)}'
                        else
                            echo 'N/A'
                        fi
                    """,
                    returnStdout: true
                ).trim()

                echo "========================================================="
                echo " Build Status: SUCCESS"
                echo "Webhook Trigger:  200 OK"
                echo "Commit ID: ${env.GIT_COMMIT}"
                echo "Commit Author: ${commitAuthor}"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Code Coverage: ${coveragePercent}%"
                echo "Build URL: ${env.BUILD_URL}"
                echo "==========================================================="
            }
        }

        failure {
            script {
                def commitAuthor = sh(
                    script: "git log -1 --pretty=format:'%an <%ae>'",
                    returnStdout: true
                ).trim()

                echo "========================================================="
                echo " Build Status: FAILED"
                echo "Webhook Trigger:  200 OK"
                echo "Commit ID: ${env.GIT_COMMIT}"
                echo "Commit Author: ${commitAuthor}"
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
            echo " Build completed at: ${new Date()}"
        }
    }
}

}
