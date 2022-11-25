
pipeline {
  agent any
    tools {
    maven 'M3'
    }

    environment {
        dockerHubRegistry = 'qkdrmsgh73/service-gateway'
        dockerHubRegistryCredential = '634c3ce0-9c39-469b-86f3-3836d26d2edf'
        dockerImageName = 'service-gateway'
        gitCredentialId = '6261fc8c-759c-4d26-b6ff-29666b2a5625'
        gitSrcUrl = 'git@github.com:KSWA-SWEEP/Formduo-Gateway-Service.git'
        gitManifestUrl = 'git@github.com:KSWA-SWEEP/k8s-manifest.git'
    }


    stages {
        stage('Checkout Application Git Branch') {
            steps {
                git credentialsId: "${gitCredentialId}",
                    url: "${gitSrcUrl}",
                    branch: 'main'
            }
            post {
                    failure {
                      echo 'Repository clone failure !'
                    }
                    success {
                      echo 'Repository clone success !'
                    }
            }
        }

        stage('Maven Jar Build') {
                steps {
                    sh 'mvn clean install -DskipTests=true'
                }
                post {
                        failure {
                          echo 'Maven jar build failure !'
                        }
                        success {
                          echo 'Maven jar build success !'
                        }
                }
        }

        stage('Docker Image Build') {
                steps {
                    sh "docker build . -t ${dockerHubRegistry}:${currentBuild.number}"
                    sh "docker build . -t ${dockerHubRegistry}:latest"
                }
                post {
                        failure {
                          echo 'Docker image build failure !'
                        }
                        success {
                          echo 'Docker image build success !'
                        }
                }
        }

        stage('Docker Image Push') {
                steps {
                    withDockerRegistry([ credentialsId: dockerHubRegistryCredential, url: "" ]) {
                                        sh "docker push ${dockerHubRegistry}:${currentBuild.number}"
                                        sh "docker push ${dockerHubRegistry}:latest"

                                        sleep 10 /* Wait uploading */
                                    }
                }
                post {
                        failure {
                          echo 'Docker Image Push failure !'
                          sh "docker rmi ${dockerHubRegistry}:${currentBuild.number}"
                          sh "docker rmi ${dockerHubRegistry}:latest"
                        }
                        success {
                          echo 'Docker image push success !'
                          sh "docker rmi ${dockerHubRegistry}:${currentBuild.number}"
                          sh "docker rmi ${dockerHubRegistry}:latest"
                        }
                }
        }

        stage('K8S Manifest Update') {
                steps {
                    git credentialsId: "${gitCredentialId}",
                        url: "${gitManifestUrl}",
                        branch: 'main'

                    sh "sed -i 's/${dockerImageName}:.*\$/${dockerImageName}:${currentBuild.number}/g' ./${dockerImageName}/deployment.yaml"
                    sh "git add ./${dockerImageName}/deployment.yaml"
                    sh "git config --global user.email 'panggeunho@gmail.com'"
                    sh "git config --global user.name 'banggeunho'"
                    sh "git commit -m '[UPDATE] config-service ${currentBuild.number} image versioning'"
                    sshagent(credentials: ["${gitCredentialId}"]) {
                        sh "git remote set-url origin ${gitManifestUrl}"
                        sh "git push -u origin main"
                     }
                }
                post {
                        failure {
                          echo 'K8S Manifest Update failure !'
                        }
                        success {
                          echo 'K8S Manifest Update success !'
                        }
                }
        }
    }
}