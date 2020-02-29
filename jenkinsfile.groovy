pipeline {
    agent any
    environment {
        USERID = 'user15'
        USERGITHUB = 'zears0'
        PROJECTID = 'fluids-analogy-26'
        CLUSTERNAME = 'cluster-1'
        CLUSTERLOCATION = 'asia-southeast1-c'
        CREDENTIALS_ID = 'gke'
    }
    stages {
        stage("Checkout code") {
            steps {
                git branch: 'master',
                    url: "https://github.com/${env.USERGITHUB}/ci-workshop.git"
            }
        }
        stage("Unit test") {
            steps {
                withDockerContainer("peerapach/python:3.6") {
                    sh """
                        cd src
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
                      projectId: env.PROJECTID, 
                      namespace: env.USERID,
                      clusterName: env.CLUSTERNAME, 
                      location: env.CLUSTERLOCATION, 
                      manifestPattern: 'deployment/deployment.yaml', 
                      credentialsId: env.CREDENTIALS_ID, 
                      verifyDeployments: true])
            }
        }
    }    
}
