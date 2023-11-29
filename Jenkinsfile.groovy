import groovy.json.JsonOutput

def pom(path, target) {
    return [pattern: "${path}/pom.xml", target: "${target}.pom"]
}

def jar(path, target) {
    return [pattern: "${path}/target/*.jar",
            target         : "${target}.jar",
            excludePatterns: ['*-exec.jar']
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

String podSpec = '''
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
    - name: node
      image: node:20
      tty: true
      env:
        - name: HOME
          value: /home/jenkins/agent
      resources:
        requests:
          memory: 128Mi
          cpu: 200m
        limits:
          memory: 256Mi
          cpu: 400m
  securityContext:
    runAsUser: 1000
  volumes:
    - name: docker-graph-storage
      emptyDir: {}
'''

pipeline {
    agent {
        kubernetes {
            yaml podSpec
        }
    }

    environment {
        appName = 'dms'
        gatewaySuffix = '-gateway'
        helperSuffix = '-version-helper'
        dockerHubImageName = "overture/${appName}"
        gitHubRegistry = 'ghcr.io'
        gitHubRepo = "overture-stack/${appName}"
        githubImageName = "${gitHubRegistry}/${gitHubRepo}"

        commit = sh(
            returnStdout: true,
            script: 'git describe --always'
        ).trim()

        version = sh(
            returnStdout: true,
            script: "echo ${readMavenPom().version}"
        ).trim()

        slackNotificationsUrl = credentials('OvertureSlackJenkinsWebhookURL')
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    stages {
        stage('Test') {
            steps {
                container('jdk') {
                    sh './mvnw test package'
                }
            }
        }

        stage('Build images') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'feature/jbrowseIntegeration'
                }
            }
            steps {
                container('docker') {
                    sh "docker build \
                        --target client \
                        --network=host \
                        -f Dockerfile \
                        -t ${appName} ."

                    sh "docker build \
                        --target latest-version-helper \
                        --network=host \
                        -f Dockerfile \
                        -t ${appName}${helperSuffix} ."
                }
            }
        }

        stage('Tag git version') {
            when {
                branch 'main'
            }
            steps {
                container('docker') {
                    withCredentials([usernamePassword(
                        credentialsId: 'OvertureBioGithub',
                        passwordVariable: 'GIT_PASSWORD',
                        usernameVariable: 'GIT_USERNAME'
                    )]) {
                        // if the tag exists, fail the job to prevent overwriting docker images
                        sh "git tag ${version}"
                        sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${gitHubRepo} --tags"
                    }
                }
            }
        }

        stage('Publish Images') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'feature/jbrowseIntegeration'
                }
            }
            parallel {
                stage('...to dockerhub') {
                    // still necessary because the helper script relies on the dockerhub api to get versions
                    // TODO: figure out a workaround that doesn't rely on dockerhub
                    steps {
                        container('docker') {
                            withCredentials([usernamePassword(
                                credentialsId:'OvertureBioDockerHub',
                                usernameVariable: 'USERNAME',
                                passwordVariable: 'PASSWORD'
                            )]) {
                                sh 'docker login -u $USERNAME -p $PASSWORD'

                                script {
                                    if (env.BRANCH_NAME ==~ 'main') { //push latest and version tags
                                        sh "docker tag dms ${dockerHubImageName}:${version}"
                                        sh "docker push ${dockerHubImageName}:${version}"

                                        sh "docker tag dms ${dockerHubImageName}:latest"
                                        sh "docker push ${dockerHubImageName}:latest"
                                    } else { // push commit tag for develop and any other branches
                                        sh "docker tag dms ${dockerHubImageName}:${commit}"
                                        sh "docker push ${dockerHubImageName}:${commit}"
                                    }

                                    if (env.BRANCH_NAME ==~ 'develop') { // push edge tag
                                        sh "docker tag dms ${dockerHubImageName}:edge"
                                        sh "docker push ${dockerHubImageName}:edge"
                                    }
                                }
                            }
                        }
                    }
                }

                stage('...to github') {
                    steps {
                        container('docker') {
                            withCredentials([usernamePassword(
                                credentialsId:'OvertureBioGithub',
                                usernameVariable: 'USERNAME',
                                passwordVariable: 'PASSWORD'
                            )]) {
                                sh 'docker login ghcr.io -u $USERNAME -p $PASSWORD'

                                script {
                                    sh "docker build \
                                        --target insecure \
                                        --network=host \
                                        -f ./nginx/path-based/Dockerfile \
                                        ./nginx/path-based \
                                        -t ${githubImageName}${gatewaySuffix}:${commit}"

                                    sh "docker build \
                                        --target secure \
                                        --network=host \
                                        -f ./nginx/path-based/Dockerfile \
                                        ./nginx/path-based \
                                        -t ${githubImageName}${gatewaySuffix}-secure:${commit}"

                                    if (env.BRANCH_NAME ==~ 'main') { //push latest and version tags
                                        sh "docker tag dms ${githubImageName}:${version}"
                                        sh "docker push ${githubImageName}:${version}"

                                        sh "docker tag dms ${githubImageName}:latest"
                                        sh "docker push ${githubImageName}:latest"

                                        sh "docker tag dms-version-helper ${githubImageName}${helperSuffix}:${version}"
                                        sh "docker push ${githubImageName}${helperSuffix}:${version}"

                                        sh "docker tag dms-version-helper ${githubImageName}${helperSuffix}:latest"
                                        sh "docker push ${githubImageName}${helperSuffix}:latest"

                                        sh "docker tag \
                                            ${githubImageName}${gatewaySuffix}:${commit} \
                                            ${githubImageName}${gatewaySuffix}:${version}"
                                        sh "docker push ${githubImageName}${gatewaySuffix}:${version}"

                                        sh "docker tag \
                                            ${githubImageName}${gatewaySuffix}:${commit} \
                                            ${githubImageName}${gatewaySuffix}:latest"
                                        sh "docker push ${githubImageName}${gatewaySuffix}:latest"

                                        sh "docker tag \
                                            ${githubImageName}${gatewaySuffix}-secure:${commit} \
                                            ${githubImageName}${gatewaySuffix}-secure:${version}"
                                        sh "docker push ${githubImageName}${gatewaySuffix}-secure:${version}"

                                        sh "docker tag \
                                            ${githubImageName}${gatewaySuffix}-secure:${commit} \
                                            ${githubImageName}${gatewaySuffix}-secure:latest"
                                        sh "docker push ${githubImageName}${gatewaySuffix}-secure:latest"
                                    } else { // push commit tag for develop and any other branches
                                        sh "docker tag dms ${githubImageName}:${commit}"
                                        sh "docker push ${githubImageName}:${commit}"

                                        sh "docker tag dms-version-helper ${githubImageName}${helperSuffix}:${commit}"
                                        sh "docker push ${githubImageName}${helperSuffix}:${commit}"

                                        sh "docker push ${githubImageName}${gatewaySuffix}:${commit}"

                                        sh "docker push ${githubImageName}${gatewaySuffix}-secure:${commit}"
                                    }

                                    if (env.BRANCH_NAME ==~ 'develop') { // push edge tag
                                        sh "docker tag dms ${githubImageName}:edge"
                                        sh "docker push ${githubImageName}:edge"

                                        sh "docker tag dms-version-helper ${githubImageName}${helperSuffix}:edge"
                                        sh "docker push ${githubImageName}${helperSuffix}:edge"

                                        sh "docker tag \
                                            ${githubImageName}${gatewaySuffix}:${commit} \
                                            ${githubImageName}${gatewaySuffix}:edge"
                                        sh "docker push ${githubImageName}${gatewaySuffix}:edge"

                                        sh "docker tag \
                                            ${githubImageName}${gatewaySuffix}-secure:${commit} \
                                            ${githubImageName}${gatewaySuffix}-secure:edge"
                                        sh "docker push ${githubImageName}${gatewaySuffix}-secure:edge"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        failure {
            container('node') {
                script {
                    if (env.BRANCH_NAME ==~ /(develop|main|\S*[Tt]est\S*)/) {
                        sh "curl \
                            -X POST \
                            -H 'Content-type: application/json' \
                            --data '{ \
                                \"text\":\"Build Failed: ${env.JOB_NAME}#${commit} \
                                \n[Build ${env.BUILD_NUMBER}] (${env.BUILD_URL})\" \
                            }' \
                            ${slackNotificationsUrl}"
                    }
                }
            }
        }

        fixed {
            container('node') {
                script {
                    if (env.BRANCH_NAME ==~ /(develop|main|\S*[Tt]est\S*)/) {
                        sh "curl \
                            -X POST \
                            -H 'Content-type: application/json' \
                            --data '{ \
                                \"text\":\"Build Fixed: ${env.JOB_NAME}#${commit} \
                                \n[Build ${env.BUILD_NUMBER}] (${env.BUILD_URL})\" \
                            }' \
                            ${slackNotificationsUrl}"
                    }
                }
            }
        }

        success {
            container('node') {
                script {
                    if (env.BRANCH_NAME ==~ /(\S*[Tt]est\S*)/) {
                        sh "curl \
                            -X POST \
                            -H 'Content-type: application/json' \
                            --data '{ \
                                \"text\":\"Build tested: ${env.JOB_NAME}#${commit} \
                                \n[Build ${env.BUILD_NUMBER}] (${env.BUILD_URL})\" \
                            }' \
                            ${slackNotificationsUrl}"
                    }
                }
            }
        }
    }
}
