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
        runAsUser: 0
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
    - name: HOME
      value: /home/jenkins/agent
  securityContext:
    runAsUser: 1000
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
                branch 'jbrowseintegeration'
            }
            steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureDockerHub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'docker login -u $USERNAME -p $PASSWORD'
                    }
                    sh "docker build --network=host --target client -f  Dockerfile . -t overture/dms:jbrowse-${commit}"
                    sh "docker push overture/dms:jbrowse-${commit}"

                    sh "docker build --network=host --target latest-version-helper -f Dockerfile . -t overture/dms-version-helper:jbrowse-${commit}"
                    sh "docker push overture/dms-version-helper:jbrowse-${commit}"
                }
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureBioGithub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'docker login ghcr.io -u $USERNAME -p $PASSWORD'
                    }
                    sh "docker tag jbrowse-${commit} ${dockerOrg}/${dmsRepo}:jbrowse-${commit}"
                    sh "docker push ${dockerOrg}/${dmsRepo}:jbrowse-${commit}"

                    sh "docker tag overture/dms-version-helper:jbrowse-${commit} ${dockerOrg}/${dmsVersionHelperRepo}:jbrowse-${commit}"
                    sh "docker push ${dockerOrg}/${dmsGatewayRepo}:jbrowse-${commit}"

                    sh "docker build --target insecure --network=host -f ./nginx/path-based/Dockerfile ./nginx/path-based -t ${dockerOrg}/${dmsGatewayRepo}:jbrowse-${commit}"
                    sh "docker push ${dockerOrg}/${dmsGatewayRepo}-secure:jbrowse-${commit}"

                    sh "docker build --target secure --network=host -f ./nginx/path-based/Dockerfile ./nginx/path-based -t ${dockerOrg}/${dmsGatewayRepo}-secure:edge"
                    sh "docker push ${dockerOrg}/${dmsVersionHelperRepo}:jbrowse-${commit}"
                }
            }
        }
    }
}
