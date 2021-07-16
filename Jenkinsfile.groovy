import groovy.json.JsonOutput

def version = "UNKNOWN"
def commit = "UNKNOWN"
def repo = "UNKNOWN"
def dmsRepo = "dms"
def dmsGatewayRepo = "dms-gateway"
def dmsVersionHelperRepo = "dms-version-helper"
def dockerOrg = "ghcr.io/overture-stack"

def pom(path, target) {
    return [pattern: "${path}/pom.xml", target: "${target}.pom"]
}

def jar(path, target) {
    return [pattern: "${path}/target/*.jar",
            target         : "${target}.jar",
            excludePatterns: ["*-exec.jar"]
            ]
}

def tar(path, target) {
    return [pattern: "${path}/target/*.tar.gz",
            target : "${target}-dist.tar.gz"]
}

def runjar(path, target) {
    return [pattern: "${path}/target/*-exec.jar",
            target : "${target}-exec.jar"]
}

pipeline {
    agent {
        kubernetes {
            label 'dms-executor'
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jdk
    tty: true
    image: adoptopenjdk/openjdk11:jdk-11.0.6_10-alpine-slim
    env: 
      - name: DOCKER_HOST 
        value: tcp://localhost:2375 
  - name: dind-daemon 
    image: docker:18.06-dind
    securityContext: 
        privileged: true 
    volumeMounts: 
      - name: docker-graph-storage 
        mountPath: /var/lib/docker 
  - name: helm
    image: alpine/helm:2.12.3
    command:
    - cat
    tty: true
  - name: docker
    image: docker:18-git
    tty: true
    env: 
      - name: DOCKER_HOST 
        value: tcp://localhost:2375 
  volumes:
  - name: docker-graph-storage 
    emptyDir: {}
"""
        }
    }
    stages {
        stage('Prepare') {
            steps {
                script {
                    commit = sh(returnStdout: true, script: 'git describe --always').trim()
                }
                script {
                    version = readMavenPom().getVersion()
                }
            }
        }
        stage('Test') {
            steps {
                container('jdk') {
                    sh "./mvnw test package"
                }
            }
        }
        stage('Build & Publish Develop') {
            when {
                branch "develop"
            }
            steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureDockerHub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'docker login -u $USERNAME -p $PASSWORD'
                    }
                    sh "docker build --network=host --target client -f  Dockerfile . -t overture/dms:edge -t overture/dms:${version}-${commit}"
                    sh "docker build --network=host --target latest-version-helper -f Dockerfile . -t overture/dms-version-helper:edge"
                    sh "docker push overture/dms:${version}-${commit}"
                    sh "docker push overture/dms:edge"
                    sh "docker push overture/dms-version-helper:edge"
                }
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureBioGithub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'docker login ghcr.io -u $USERNAME -p $PASSWORD'
                    }
                    sh "docker build --network=host --target client -f  Dockerfile . -t ${dockerOrg}/${dmsRepo}:edge -t ${dockerOrg}/${dmsRepo}:${version}-${commit}"
                    sh "docker build --network=host --target latest-version-helper -f Dockerfile . -t ${dockerOrg}/${dmsVersionHelperRepo}:edge"
                    sh "docker build --target insecure --network=host -f ./nginx/path-based/Dockerfile ./nginx/path-based -t ${dockerOrg}/${dmsGatewayRepo}:edge"
                    sh "docker build --target secure --network=host -f ./nginx/path-based/Dockerfile ./nginx/path-based -t ${dockerOrg}/${dmsGatewayRepo}-secure:edge"
                    sh "docker push ${dockerOrg}/${dmsRepo}:${version}-${commit}"
                    sh "docker push ${dockerOrg}/${dmsRepo}:edge"
                    sh "docker push ${dockerOrg}/${dmsGatewayRepo}:edge"
                    sh "docker push ${dockerOrg}/${dmsVersionHelperRepo}:edge"
                }
            }
        }
        stage('Release & tag') {
          when {
            branch "master"
          }
          steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId: 'OvertureBioGithub', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                        sh "git tag ${version}"
                        sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/overture-stack/dms --tags"
                    }
                    withCredentials([usernamePassword(credentialsId:'OvertureBioGithub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'docker login ghcr.io -u $USERNAME -p $PASSWORD'
                    }
                    sh "docker build --network=host --target client -f Dockerfile . -t ${dockerOrg}/${dmsRepo}:latest -t ${dockerOrg}/${dmsRepo}:${version}"
                    sh "docker build --network=host --target latest-version-helper  -f Dockerfile . -t ${dockerOrg}/${dmsVersionHelperRepo}:latest"
                    sh "docker build --target insecure --network=host -f ./nginx/path-based/Dockerfile ./nginx/path-based -t ${dockerOrg}/${dmsGatewayRepo}:latest -t ${dockerOrg}/${dmsGatewayRepo}:${version}"
                    sh "docker build --target secure --network=host -f ./nginx/path-based/Dockerfile ./nginx/path-based -t ${dockerOrg}/${dmsGatewayRepo}-secure:latest -t ${dockerOrg}/${dmsGatewayRepo}-secure:${version}"
                    sh "docker push ${dockerOrg}/${dmsRepo}:${version}"
                    sh "docker push ${dockerOrg}/${dmsGatewayRepo}:${version}"
                    sh "docker push ${dockerOrg}/${dmsGatewayRepo}-secure:${version}"
                    sh "docker push ${dockerOrg}/${dmsGatewayRepo}:latest"
                    sh "docker push ${dockerOrg}/${dmsGatewayRepo}-secure:latest"
                    sh "docker push ${dockerOrg}/${dmsRepo}:latest"
                    sh "docker push ${dockerOrg}/${dmsVersionHelperRepo}:latest"
                }
                container('docker') {
                  withCredentials([usernamePassword(credentialsId:'OvertureDockerHub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                      sh 'docker login -u $USERNAME -p $PASSWORD'
                  }
                  sh "docker build --network=host --target client -f Dockerfile . -t overture/dms:latest -t overture/dms:${version}"
                  sh "docker build --network=host --target latest-version-helper  -f Dockerfile . -t overture/dms-version-helper:latest"
                  sh "docker push overture/dms:${version}"
                  sh "docker push overture/dms:latest"
                  sh "docker push overture/dms-version-helper:latest"
                }
            }
        }

        stage('Destination SNAPSHOT') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'test-develop'
                }
            }
            steps {
                script {
                    repo = "dcc-snapshot/bio/overture"
                }
            }
        }

        stage('Destination release') {
            when {
                anyOf {
                    branch 'master'
                    branch 'test-master'
                }
            }
            steps {
                script {
                    repo = "dcc-release/bio/overture"
                }
            }
        }

        stage('Upload Artifacts') {
            when {
                anyOf {
                    branch 'master'
                    branch 'test-master'
                    branch 'develop'
                    branch 'test-develop'
                }
            }
            steps {
                script {
                    
                    project = "dms"
                    versionName = "$version"
                    subProjects = ['cli']

                    files = []
                    files.add([pattern: "pom.xml", target: "$repo/$project/$versionName/$project-${versionName}.pom"])

                    for (s in subProjects) {
                        name = "${project}-$s"
                        target = "$repo/$name/$versionName/$name-$versionName"
                        files.add(pom(name, target))
                        files.add(jar(name, target))

                        if (s in ['cli']) {
                            files.add(runjar(name, target))
                            files.add(tar(name, target))
                        }
                    }

                    fileSet = JsonOutput.toJson([files: files])
                    pretty = JsonOutput.prettyPrint(fileSet)
                    print("Uploading files=${pretty}")
                }

                rtUpload(serverId: 'artifactory', spec: fileSet)
            }
        }
    }
}
