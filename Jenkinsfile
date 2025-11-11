@Library('jenkins-shared-library-aiml') _

// ======================================================
// Project-specific configuration
// ======================================================
def config = [
    project           : "logistics",                           // Project name
    component         : "backend_user",                        // Microservice name
   // repo              : "https://github.com/siddhartha-surnoi/Logistics_mot_user.git",
    agentLabel        : "java-agent-1",                        // Jenkins agent to use
 //   awsRegion         : "us-east-2",                           // AWS region for ECR

    // AWS credentials ID configured in Jenkins Credentials (type: AWS)
 //   awsCredentialsId  : "aws-credentials",                     // IAM user with ECR permissions

    // Optional: Teams Webhook for notifications
    teamsWebhookId    : "teams-webhook"
]

// ======================================================
// Trigger the shared pipeline
// ======================================================
Logistics_microservicePipeline(config)
