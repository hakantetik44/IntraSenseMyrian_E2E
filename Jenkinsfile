pipeline {
    agent any

    environment {
        JAVA_HOME = tool 'JDK17'
        PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
        ALLURE_HOME = tool 'Allure'
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
                                    echo "🍎 macOS detected, using Homebrew..."
                                    brew install ffmpeg
                                else
                                    echo "🐧 Linux detected, using apt..."
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
                withCredentials([string(credentialsId: 'xray-api-key', variable: 'XRAY_API_KEY')]) {
                    script {
                        sh """
                            echo "🔍 Checking test results..."
                            if [ ! -f "target/cucumber-reports/cucumber.json" ]; then
                                echo "❌ Error: cucumber.json not found!"
                                exit 1
                            fi
                            
                            echo "📤 Uploading results to Xray Test Execution: SMF-2"
                            echo "🔑 Authenticating with Xray..."
                            curl -v -H "Content-Type: application/json" \
                                 -H "Authorization: Bearer ${XRAY_API_KEY}" \
                                 -X POST \
                                 --data @target/cucumber-reports/cucumber.json \
                                 "https://xray.cloud.getxray.app/api/v2/import/execution/cucumber/SMF-2" 2>&1 | tee xray-response.log
                            
                            if grep -q "error" xray-response.log; then
                                echo "❌ Error uploading to Xray:"
                                cat xray-response.log
                                exit 1
                            else
                                echo "✅ Successfully uploaded test results to Xray"
                            fi
                        """
                        
                        echo "📋 Archiving Xray response..."
                        archiveArtifacts artifacts: 'xray-response.log', allowEmptyArchive: true
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