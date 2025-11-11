@Library('jenkins-shared-library-aiml') _

def config = [
    project           : "logistics",
    component         : "backend_user",
    repo              : "https://github.com/siddhartha-surnoi/Logistics_mot_user.git",
    agentLabel        : "java-agent-1",

    //  Optional: pass your specific ECR credential ID
    ecrRepoCredential : "ecr-repo_user"
    
]

Logistics_microservicePipeline(config)
