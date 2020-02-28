pipeline {
    agent any
    environment {
        USERID = 'user1'
   //     PROJECT_ID = 'PROJECTID'
   //     CLUSTER_NAME = 'CLUSTERNAME'
   //     LOCATION = 'CLUSTERLOCATION'
        CREDENTIALS_ID = 'gke'
    }
    stages {
        stage("Checkout code") {
            steps {
                git branch: 'master',
                    url: "https://github.com/peerapach/ci-workshop.git"
            }
        }
        stage("Unit test") {
            steps {
                withDockerContainer("peerapach/python:3.6") {
                    sh """
                        python -m unittest
                    """
                }
            }
        }        
        stage("Build image") {
            steps {
                sh """
                    sed -i 's/USERID/${USERID}/g' Dockerfile
                """
                script {
                    myapp = docker.build("cicdday/${USERID}-hello:${env.BUILD_ID}")
                }
            }
        }
        stage("Push image") {
            steps {
                script {
                    docker.withRegistry('', 'dockerhub') {
                        myapp.push("${env.BUILD_ID}")
                    }
                }
            }
        }        
        stage('Deploy to GKE') {
            steps{
                sh """
                    sed -i 's/#USER#/${USERID}/g' deployment/deployment.yaml
                    sed -i 's/#APPUSER#/${USERID}-hello:${env.BUILD_ID}/g' deployment/deployment.yaml
                    sed -i 's/#DOCKER-HUB-USERNAME#/cicdday/g' deployment/deployment.yaml
                """
                
                step([$class: 'KubernetesEngineBuilder', 
                      projectId: params.PROJECTID, 
                      namespace: env.USERID,
                      clusterName: params.CLUSTERNAME, 
                      location: params.CLUSTERLOCATION, 
                      manifestPattern: 'deployment/deployment.yaml', 
                      credentialsId: env.CREDENTIALS_ID, 
                      verifyDeployments: true])
            }
        }
    }    
}
