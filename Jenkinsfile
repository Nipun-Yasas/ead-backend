pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=$WORKSPACE/.m2/repository"
        DOCKER_IMAGE_NAME = "ead-backend"
        DOCKER_IMAGE_TAG = "${BUILD_NUMBER}"
        SERVER_PORT = "8090"
        FRONTEND_URL = "http://localhost:5173"
        JWT_EXPIRATION = "86400000"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Load Credentials') {
            steps {
                script {
                    withCredentials([
                        usernamePassword(credentialsId: 'database-credentials',
                            usernameVariable: 'DB_USERNAME',
                            passwordVariable: 'DB_PASSWORD'),
                        string(credentialsId: 'database-url', variable: 'DB_URL'),
                        string(credentialsId: 'jwt-secret', variable: 'JWT_SECRET_KEY')
                    ]) {
                        env.DATASOURCE_URL = DB_URL
                        env.DATASOURCE_USERNAME = DB_USERNAME
                        env.DATASOURCE_PASSWORD = DB_PASSWORD
                        env.JWT_SECRET = JWT_SECRET_KEY
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests=true'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    sh """
                        docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} .
                        docker tag ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ${DOCKER_IMAGE_NAME}:latest
                    """
                    env.DOCKER_IMAGE_ID = sh(
                        script: "docker images -q ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}",
                        returnStdout: true
                    ).trim()
                }
            }
        }

        stage('Docker Test Run') {
            steps {
                script {
                    sh """
                        docker run -d --name test-${BUILD_NUMBER} \
                        -p 8091:8090 \
                        -e DATASOURCE_URL='${env.DATASOURCE_URL}' \
                        -e DATASOURCE_USERNAME='${env.DATASOURCE_USERNAME}' \
                        -e DATASOURCE_PASSWORD='${env.DATASOURCE_PASSWORD}' \
                        -e JWT_SECRET='${env.JWT_SECRET}' \
                        -e JWT_EXPIRATION='${JWT_EXPIRATION}' \
                        -e SERVER_PORT='${SERVER_PORT}' \
                        -e FRONTEND_URL='${FRONTEND_URL}' \
                        ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}

                        sleep 15
                        docker ps | grep test-${BUILD_NUMBER} || (docker logs test-${BUILD_NUMBER}; exit 1)
                    """
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Build Info') {
            steps {
                script {
                    echo "✅ Build complete"
                    echo "JAR file: $(ls target/*.jar | grep -v original)"
                    echo "Docker Image: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                    echo "Image ID: ${env.DOCKER_IMAGE_ID}"
                }
            }
        }
    }

    post {
        always {
            sh "docker rm -f test-${BUILD_NUMBER} || true"
            cleanWs()
        }
        success {
            echo "✅ Pipeline Success!"
        }
        failure {
            echo "❌ Pipeline Failed - check logs!"
        }
    }
}
