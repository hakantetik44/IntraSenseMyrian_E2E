pipeline {
    agent any

    environment {
        JAVA_HOME = tool 'JDK17'
        PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
        ALLURE_HOME = tool 'Allure'
        XRAY_CREDENTIALS = credentials('xray-api-key')
    }

    tools {
        maven 'maven'
        jdk 'JDK17'
        allure 'Allure'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timestamps()
        timeout(time: 1, unit: 'HOURS')
        disableConcurrentBuilds()
    }

    stages {
        stage('🔍 Initialize') {
            steps {
                script {
                    sh '''
                        echo "🔧 Environment Information:"
                        echo "☕ JAVA_HOME: $JAVA_HOME"
                        echo "🛠️ PATH: $PATH"
                        echo "📝 Java Version:"
                        java -version
                        echo "🏗️ Maven Version:"
                        mvn -version
                    '''
                }
            }
        }

        stage('🧪 Run Tests') {
            steps {
                script {
                    try {
                        echo "📂 Creating Test Directories..."
                        sh '''
                            echo "🗂️ Setting up directories..."
                            mkdir -p target/{cucumber-reports,allure-results,videos,screenshots}
                            
                            if ! command -v ffmpeg &> /dev/null; then
                                echo "📥 Installing FFmpeg..."
                                if [[ "$OSTYPE" == "darwin"* ]]; then
                                    echo "🍎 macOS detected..."
                                    which brew || /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
                                    export PATH="/usr/local/bin:$PATH"
                                    brew install ffmpeg || true
                                else
                                    echo "🐧 Linux detected..."
                                    sudo apt-get update && sudo apt-get install -y ffmpeg
                                fi
                            fi
                            
                            echo "🚀 Running Web Tests..."
                            echo "⚡ Executing Maven Tests..."
                            mvn clean test
                        '''

                        echo "📊 Checking Test Results:"
                        sh '''
                            echo "🥒 Cucumber Reports:"
                            ls -la target/cucumber-reports/ || true
                            echo "📈 Allure Results:"
                            ls -la target/allure-results/ || true
                        '''
                    } catch (Exception e) {
                        echo """
                            ⚠️ Test Error:
                            🔴 Error Message: ${e.message}
                            🏗️ Build: ${BUILD_NUMBER}
                        """
                        unstable "❌ Test execution failed: ${e.message}"
                    }
                }
            }
        }

        stage('📊 Generate Reports') {
            steps {
                script {
                    try {
                        sh '''
                            echo "📁 Creating report directories..."
                            mkdir -p test-reports
                            echo "📋 Copying Cucumber reports..."
                            cp -r target/cucumber-reports/* test-reports/ || true
                            echo "📋 Copying Surefire reports..."
                            cp -r target/surefire-reports test-reports/ || true
                            echo "📋 Copying Allure results..."
                            cp -r target/allure-results test-reports/ || true
                            echo "🗜️ Compressing reports..."
                            zip -r test-reports.zip test-reports/
                        '''
                        
                        echo "📈 Generating Allure report..."
                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: 'target/allure-results']]
                        ])
                    } catch (Exception e) {
                        echo "⚠️ Report generation failed: ${e.message}"
                    }
                }
            }
        }

        stage('📤 Generate Xray Results') {
            steps {
                script {
                    try {
                        sh """
                            echo "🔍 Checking test results..."
                            if [ ! -f "target/cucumber-reports/cucumber.json" ]; then
                                echo "❌ Error: cucumber.json not found!"
                                exit 1
                            fi
                            
                            echo "📤 Preparing to upload results to Xray..."
                            echo "🔑 Authenticating with Xray..."
                            
                            # Create authentication payload
                            echo '{
                                "client_id": "${XRAY_CREDENTIALS_USR}",
                                "client_secret": "${XRAY_CREDENTIALS_PSW}"
                            }' > auth.json
                            
                            # Get Xray API token with retry mechanism
                            MAX_RETRIES=3
                            RETRY_COUNT=0
                            while [ \$RETRY_COUNT -lt \$MAX_RETRIES ]; do
                                echo "🔄 Authentication attempt \$((RETRY_COUNT + 1)) of \$MAX_RETRIES"
                                
                                XRAY_TOKEN=\$(curl -s -w '\\n%{http_code}' \\
                                    -H "Content-Type: application/json" \\
                                    -X POST \\
                                    --data @auth.json \\
                                    --retry 3 \\
                                    --retry-delay 5 \\
                                    --retry-max-time 30 \\
                                    --connect-timeout 10 \\
                                    "https://xray.cloud.getxray.app/api/v2/authenticate" | {
                                        read RESPONSE
                                        read STATUS
                                        if [ "\$STATUS" = "200" ]; then
                                            echo "\$RESPONSE"
                                            return 0
                                        else
                                            echo ""
                                            return 1
                                        fi
                                    })
                                
                                if [ ! -z "\$XRAY_TOKEN" ]; then
                                    echo "✅ Successfully authenticated with Xray"
                                    break
                                else
                                    echo "⚠️ Authentication failed, retrying..."
                                    RETRY_COUNT=\$((RETRY_COUNT + 1))
                                    if [ \$RETRY_COUNT -lt \$MAX_RETRIES ]; then
                                        sleep 10
                                    fi
                                fi
                            done
                            
                            if [ -z "\$XRAY_TOKEN" ]; then
                                echo "❌ Failed to authenticate with Xray after \$MAX_RETRIES attempts"
                                exit 1
                            fi
                            
                            # Remove auth file
                            rm -f auth.json
                            
                            echo "📤 Uploading test results to Xray..."
                            UPLOAD_RESPONSE=\$(curl -s -w '\\n%{http_code}' \\
                                -H "Content-Type: application/json" \\
                                -H "Authorization: Bearer \$XRAY_TOKEN" \\
                                -X POST \\
                                --data @target/cucumber-reports/cucumber.json \\
                                --retry 3 \\
                                --retry-delay 5 \\
                                --retry-max-time 30 \\
                                --connect-timeout 10 \\
                                "https://xray.cloud.getxray.app/api/v2/import/execution/cucumber/SMF-2" | tee xray-response.log)
                            
                            UPLOAD_STATUS=\$(echo "\$UPLOAD_RESPONSE" | tail -n1)
                            if [ "\$UPLOAD_STATUS" = "200" ] || [ "\$UPLOAD_STATUS" = "201" ]; then
                                echo "✅ Successfully uploaded test results to Xray"
                            else
                                echo "❌ Error uploading to Xray (Status: \$UPLOAD_STATUS):"
                                cat xray-response.log
                                exit 1
                            fi
                        """
                        
                        echo "📋 Archiving Xray response..."
                        archiveArtifacts artifacts: 'xray-response.log', allowEmptyArchive: true
                    } catch (Exception e) {
                        echo """
                            ⚠️ Xray upload failed:
                            🔴 Error: ${e.message}
                            📝 Stack trace: ${e.printStackTrace()}
                        """
                        unstable "❌ Xray upload failed"
                    }
                }
            }
        }

        stage('📦 Archive Results') {
            steps {
                script {
                    try {
                        echo "📦 Archiving test artifacts..."
                        archiveArtifacts(
                            artifacts: '''
                                test-reports.zip,
                                target/cucumber-reports/**/*,
                                target/allure-results/**/*,
                                target/videos/**/*,
                                target/screenshots/**/*
                            ''',
                            allowEmptyArchive: true
                        )
                        
                        echo "🥒 Generating Cucumber report..."
                        cucumber(
                            buildStatus: 'UNSTABLE',
                            fileIncludePattern: '**/cucumber.json',
                            jsonReportDirectory: 'target/cucumber-reports',
                            reportTitle: 'Intrasense Web UI Test Report'
                        )
                    } catch (Exception e) {
                        echo "⚠️ Archiving results failed: ${e.message}"
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                try {
                    sh '''
                        echo "🧹 Cleaning up workspace..."
                        rm -rf target/allure-report || true
                    '''
                    
                    echo "📊 Generating final Allure report..."
                    allure([
                        includeProperties: true,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'target/allure-results']]
                    ])

                    echo "📦 Archiving final artifacts..."
                    archiveArtifacts artifacts: '''
                        target/allure-results/**/*,
                        target/cucumber-reports/**/*,
                        target/videos/**/*,
                        target/screenshots/**/*
                    ''', allowEmptyArchive: true

                    echo """
                        📊 Test Results Summary:
                        🌿 Branch: ${env.BRANCH_NAME ?: 'unknown'}
                        🏗️ Build Status: ${currentBuild.currentResult}
                        🔢 Build Number: #${BUILD_NUMBER}
                        ⏱️ Duration: ${currentBuild.durationString}
                    """
                } catch (Exception e) {
                    echo "⚠️ Post-build actions failed: ${e.message}"
                } finally {
                    echo "🧹 Cleaning workspace..."
                    cleanWs()
                }
            }
        }
        success {
            echo """
                ✅ Pipeline completed successfully
                🎉 All tests passed
                🚀 Build #${BUILD_NUMBER} is ready
            """
        }
        failure {
            echo """
                ❌ Pipeline failed
                💔 Build #${BUILD_NUMBER} failed
                🔍 Please check the logs for details
            """
        }
        unstable {
            echo """
                ⚠️ Pipeline is unstable
                🟡 Build #${BUILD_NUMBER} is unstable
                🔍 Some tests may have failed
            """
        }
    }
} 