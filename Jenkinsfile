pipeline {
    agent any
    
    tools {
        // Define the Maven tool - make sure this matches your Jenkins tool configuration
        maven 'Maven-3.9'
        // Define the JDK tool - make sure this matches your Jenkins tool configuration  
        jdk 'JDK-21'
    }
    
    environment {
        // Define environment variables
        MAVEN_OPTS = '-Dmaven.repo.local=$WORKSPACE/.m2/repository'
        
        // Docker Configuration
        DOCKER_IMAGE_NAME = 'ead-backend'
        DOCKER_IMAGE_TAG = "${BUILD_NUMBER}"
        DOCKER_REGISTRY = 'your-registry.com' // Change this to your Docker registry
        
        // Non-sensitive configuration
        SERVER_PORT = '8090'
        FRONTEND_URL = 'http://localhost:5173'
        JWT_EXPIRATION = '86400000'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code from repository...'
                checkout scm
            }
        }
        
        stage('Setup Credentials') {
            steps {
                echo 'Setting up secure credentials...'
                script {
                    // Load credentials securely from Jenkins credential store
                    withCredentials([
                        usernamePassword(credentialsId: 'database-credentials', 
                                       usernameVariable: 'DB_USERNAME', 
                                       passwordVariable: 'DB_PASSWORD'),
                        string(credentialsId: 'database-url', variable: 'DB_URL'),
                        string(credentialsId: 'jwt-secret', variable: 'JWT_SECRET_KEY'),
                        usernamePassword(credentialsId: 'docker-registry-credentials',
                                       usernameVariable: 'DOCKER_USERNAME',
                                       passwordVariable: 'DOCKER_PASSWORD')
                    ]) {
                        // Store credentials in environment variables for this build
                        env.DATASOURCE_URL = "${DB_URL}"
                        env.DATASOURCE_USERNAME = "${DB_USERNAME}"
                        env.DATASOURCE_PASSWORD = "${DB_PASSWORD}"
                        env.JWT_SECRET = "${JWT_SECRET_KEY}"
                        
                        echo 'Credentials loaded successfully (values masked for security)'
                        echo "Database URL configured: ${DB_URL ? 'Yes' : 'No'}"
                        echo "JWT Secret configured: ${JWT_SECRET_KEY ? 'Yes' : 'No'}"
                    }
                }
            }
        }
        
        stage('Clean Workspace') {
            steps {
                echo 'Cleaning workspace and Maven cache...'
                sh 'rm -rf target/'
                sh 'rm -rf $WORKSPACE/.m2/repository'
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building the Spring Boot application...'
                sh 'chmod +x mvnw'
                sh './mvnw clean compile -DskipTests=true'
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running unit tests...'
                sh './mvnw test'
            }
            post {
                always {
                    // Archive test results
                    junit 'target/surefire-reports/*.xml'
                    
                    // Test reports are archived via junit step
                    echo 'Test reports archived successfully'
                }
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging the application...'
                sh './mvnw package -DskipTests=true'
            }
        }
        
        stage('Archive Artifacts') {
            steps {
                echo 'Archiving build artifacts...'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                
                // Also archive the original jar if it exists
                script {
                    if (fileExists('target/*.jar.original')) {
                        archiveArtifacts artifacts: 'target/*.jar.original', fingerprint: true
                    }
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                script {
                    // Build Docker image
                    def dockerImage = docker.build("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}")
                    
                    // Also tag as latest
                    sh "docker tag ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ${DOCKER_IMAGE_NAME}:latest"
                    
                    // Display image information
                    sh "docker images | grep ${DOCKER_IMAGE_NAME}"
                    
                    // Store image ID for later use
                    env.DOCKER_IMAGE_ID = sh(
                        script: "docker images -q ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}",
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Docker Test') {
            steps {
                echo 'Testing Docker image...'
                script {
                    // Use credentials securely in Docker test
                    withCredentials([
                        usernamePassword(credentialsId: 'database-credentials', 
                                       usernameVariable: 'DB_USERNAME', 
                                       passwordVariable: 'DB_PASSWORD'),
                        string(credentialsId: 'database-url', variable: 'DB_URL'),
                        string(credentialsId: 'jwt-secret', variable: 'JWT_SECRET_KEY')
                    ]) {
                        // Test if the Docker image runs correctly
                        sh """
                            echo 'Testing Docker container startup...'
                            
                            # Run container in background for testing
                            docker run -d --name test-container-${BUILD_NUMBER} \
                                -p 8091:8090 \
                                -e DATASOURCE_URL='${DB_URL}' \
                                -e DATASOURCE_USERNAME='${DB_USERNAME}' \
                                -e DATASOURCE_PASSWORD='${DB_PASSWORD}' \
                                -e JWT_SECRET='${JWT_SECRET_KEY}' \
                                -e JWT_EXPIRATION='${JWT_EXPIRATION}' \
                                -e SERVER_PORT='8090' \
                                -e FRONTEND_URL='${FRONTEND_URL}' \
                                ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}
                            
                            # Wait for container to start
                            sleep 15
                            
                            # Check if container is running
                            docker ps | grep test-container-${BUILD_NUMBER}
                            
                            # Check container logs (without exposing secrets)
                            echo 'Container startup status:'
                            docker logs test-container-${BUILD_NUMBER} | grep -i "started\\|error\\|exception" | head -10
                            
                            # Test health endpoint if available
                            echo 'Testing application health...'
                            sleep 5
                            curl -f http://localhost:8091/actuator/health || echo 'Health endpoint not available or not ready'
                            
                            # Cleanup test container
                            docker stop test-container-${BUILD_NUMBER}
                            docker rm test-container-${BUILD_NUMBER}
                        """
                    }
                }
            }
        }
        
        stage('Docker Push') {
            when {
                // Only push on main/master branch or when manually triggered
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'dev'
                }
            }
            steps {
                echo 'Pushing Docker image to registry...'
                script {
                    // Use Docker registry credentials securely
                    withCredentials([
                        usernamePassword(credentialsId: 'docker-registry-credentials',
                                       usernameVariable: 'DOCKER_USERNAME',
                                       passwordVariable: 'DOCKER_PASSWORD')
                    ]) {
                        // Login to Docker registry securely
                        sh 'echo $DOCKER_PASSWORD | docker login $DOCKER_REGISTRY -u $DOCKER_USERNAME --password-stdin'
                        
                        // Tag images for registry
                        sh "docker tag ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                        sh "docker tag ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:latest"
                        
                        // Push to registry
                        sh "docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                        sh "docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:latest"
                        
                        echo "✅ Successfully pushed to registry!"
                        echo "Image: ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                        
                        // Logout for security
                        sh 'docker logout $DOCKER_REGISTRY'
                    }
                    
                    // Also save image as tar file for local backup
                    sh "docker save ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} -o ${DOCKER_IMAGE_NAME}-${DOCKER_IMAGE_TAG}.tar"
                    archiveArtifacts artifacts: "${DOCKER_IMAGE_NAME}-${DOCKER_IMAGE_TAG}.tar", fingerprint: true
                }
            }
        }
        
        stage('Build Info') {
            steps {
                echo 'Displaying build information...'
                sh 'echo "Build completed successfully!"'
                sh 'echo "Artifact location: target/"'
                sh 'ls -la target/*.jar'
                
                script {
                    def jarFile = sh(script: 'ls target/*.jar | grep -v original | head -1', returnStdout: true).trim()
                    echo "Main JAR file: ${jarFile}"
                    
                    def jarSize = sh(script: "du -h ${jarFile} | cut -f1", returnStdout: true).trim()
                    echo "JAR file size: ${jarSize}"
                    
                    // Docker image information
                    echo "Docker Image: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                    echo "Docker Image ID: ${env.DOCKER_IMAGE_ID}"
                    
                    def imageSize = sh(
                        script: "docker images ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} --format 'table {{.Size}}' | tail -1",
                        returnStdout: true
                    ).trim()
                    echo "Docker Image Size: ${imageSize}"
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline execution completed.'
            
            // Clean up Docker resources
            script {
                // Remove any leftover test containers
                sh """
                    docker ps -a | grep test-container-${BUILD_NUMBER} | awk '{print \$1}' | xargs -r docker rm -f
                    
                    # Clean up dangling images to save space
                    docker image prune -f
                """
            }
            
            // Clean up workspace to save disk space
            cleanWs(
                cleanWhenAborted: true,
                cleanWhenFailure: true,
                cleanWhenNotBuilt: true,
                cleanWhenSuccess: true,
                cleanWhenUnstable: true,
                deleteDirs: true
            )
        }
        
        success {
            echo '✅ Build completed successfully!'
            
            // You can add notifications here (email, Slack, etc.)
            // emailext (
            //     subject: "✅ Build Success: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
            //     body: "Build completed successfully!\n\nJob: ${env.JOB_NAME}\nBuild: ${env.BUILD_NUMBER}\nBuild URL: ${env.BUILD_URL}",
            //     to: "your-email@example.com"
            // )
        }
        
        failure {
            echo '❌ Build failed!'
            
            // You can add failure notifications here
            // emailext (
            //     subject: "❌ Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
            //     body: "Build failed!\n\nJob: ${env.JOB_NAME}\nBuild: ${env.BUILD_NUMBER}\nBuild URL: ${env.BUILD_URL}\n\nPlease check the console output for details.",
            //     to: "your-email@example.com"
            // )
        }
        
        unstable {
            echo '⚠️ Build is unstable (tests may have failed)!'
        }
    }
}
