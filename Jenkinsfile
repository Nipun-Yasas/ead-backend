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
        
        // non-sensitive configs
        JWT_EXPIRATION = "86400000"
        MAIL_MAILER = "smtp"
        MAIL_HOST = "smtp.gmail.com"
        MAIL_PORT = "587"
        MAIL_ENCRYPTION = "tls"
        MAIL_FROM_NAME = "AutoCare"
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
                        string(credentialsId: 'jwt-secret', variable: 'JWT_SECRET_KEY'),
                        string(credentialsId: 'gemini-api-key', variable: 'GEMINI_API_KEY'),
                        usernamePassword(credentialsId: 'mail-credentials',
                            usernameVariable: 'MAIL_USERNAME',
                            passwordVariable: 'MAIL_PASSWORD'),
                        string(credentialsId: 'mail-from-address', variable: 'MAIL_FROM_ADDRESS')
                    ]) {
                        env.DATASOURCE_URL = DB_URL
                        env.DATASOURCE_USERNAME = DB_USERNAME
                        env.DATASOURCE_PASSWORD = DB_PASSWORD
                        env.JWT_SECRET = JWT_SECRET_KEY
                        env.GEMINI_API_KEY = GEMINI_API_KEY
                        env.MAIL_USERNAME = MAIL_USERNAME
                        env.MAIL_PASSWORD = MAIL_PASSWORD
                        env.MAIL_FROM_ADDRESS = MAIL_FROM_ADDRESS
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                // Compile only, tests run in next stage
                sh './mvnw clean compile'
            }
        }

        stage('Test') {
            steps {
                // Run tests and package if tests pass
                sh './mvnw package'
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
                    // Verify JAR exists before Docker build
                    sh 'ls -lh target/*.jar'
                    
                    // Build Docker image
                    sh """
                        docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} .
                        docker tag ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ${DOCKER_IMAGE_NAME}:latest
                    """
                    env.DOCKER_IMAGE_ID = sh(
                        script: "docker images -q ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}",
                        returnStdout: true
                    ).trim()
                    
                    echo "Docker image built: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                    echo "Docker image ID: ${env.DOCKER_IMAGE_ID}"
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
                        -e GEMINI_API_KEY='${env.GEMINI_API_KEY}' \
                        -e MAIL_MAILER='${MAIL_MAILER}' \
                        -e MAIL_HOST='${MAIL_HOST}' \
                        -e MAIL_PORT='${MAIL_PORT}' \
                        -e MAIL_USERNAME='${env.MAIL_USERNAME}' \
                        -e MAIL_PASSWORD='${env.MAIL_PASSWORD}' \
                        -e MAIL_ENCRYPTION='${MAIL_ENCRYPTION}' \
                        -e MAIL_FROM_ADDRESS='${env.MAIL_FROM_ADDRESS}' \
                        -e MAIL_FROM_NAME='${MAIL_FROM_NAME}' \
                        ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}

                        echo "🚀 Docker container started: test-${BUILD_NUMBER}"
                        echo ""
                        
                        echo "⏳ Waiting 15 seconds for Spring Boot initialization..."
                        sleep 15
                        
                        echo ""
                        echo "==================== VERIFICATION CHECKS ===================="
                        echo ""
                        
                        # Check 1: Container still running?
                        echo "✓ Check 1: Is container still running?"
                        if docker ps | grep -q test-${BUILD_NUMBER}; then
                            echo "  ✅ PASS - Container is running"
                        else
                            echo "  ❌ FAIL - Container stopped/crashed!"
                            echo ""
                            echo "Full container logs:"
                            docker logs test-${BUILD_NUMBER}
                            exit 1
                        fi
                        echo ""
                        
                        # Check 2: Any errors in logs?
                        echo "✓ Check 2: Checking for errors in logs..."
                        if docker logs test-${BUILD_NUMBER} 2>&1 | grep -qi "error\\|exception\\|failed"; then
                            echo "  ⚠️  WARNING - Errors found in logs (may be non-critical)"
                            docker logs test-${BUILD_NUMBER} 2>&1 | grep -i "error\\|exception\\|failed" | tail -5
                        else
                            echo "  ✅ PASS - No errors in logs"
                        fi
                        echo ""
                        
                        # Check 3: Tomcat started?
                        echo "✓ Check 3: Did Tomcat web server start?"
                        if docker logs test-${BUILD_NUMBER} 2>&1 | grep -q "Tomcat started"; then
                            echo "  ✅ PASS - Tomcat started successfully"
                        else
                            echo "  ❌ FAIL - Tomcat not started yet"
                            echo "  Last 30 lines of logs:"
                            docker logs --tail 30 test-${BUILD_NUMBER}
                            exit 1
                        fi
                        echo ""
                        
                        # Check 4: Spring Boot application started?
                        echo "✓ Check 4: Did Spring Boot application complete startup?"
                        if docker logs test-${BUILD_NUMBER} 2>&1 | grep -q "Started BackendApplication"; then
                            echo "  ✅ PASS - Spring Boot application started successfully"
                            # Extract startup time
                            STARTUP_TIME=\$(docker logs test-${BUILD_NUMBER} 2>&1 | grep "Started BackendApplication" | grep -oE '[0-9]+\\.[0-9]+ seconds' || echo "unknown")
                            echo "  ⏱️  Startup time: \$STARTUP_TIME"
                        else
                            echo "  ❌ FAIL - Spring Boot not fully started"
                            echo "  Last 30 lines of logs:"
                            docker logs --tail 30 test-${BUILD_NUMBER}
                            exit 1
                        fi
                        echo ""
                        
                        # Check 5: Database connection
                        echo "✓ Check 5: Is database connected?"
                        if docker logs test-${BUILD_NUMBER} 2>&1 | grep -q "HikariPool.*Start completed"; then
                            echo "  ✅ PASS - Database connection pool initialized"
                        else
                            echo "  ❌ FAIL - Database connection failed"
                            docker logs test-${BUILD_NUMBER} 2>&1 | grep -i "hikari\\|database\\|connection" | tail -10
                            exit 1
                        fi
                        echo ""
                        
                        echo "=============================================================="
                        echo "✅ ALL CHECKS PASSED! Container is healthy and ready."
                        echo "=============================================================="
                        echo ""
                        echo "📋 Last 20 lines of logs:"
                        docker logs --tail 20 test-${BUILD_NUMBER}
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
                    // capture jar file name(s) using sh and avoid Groovy string interpolation issues with $()
                    def jarFile = sh(script: "ls target/*.jar | grep -v original || true", returnStdout: true).trim()
                    echo "JAR file: ${jarFile}"
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
