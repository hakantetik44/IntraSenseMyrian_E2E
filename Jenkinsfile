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
        ansiColor('xterm')
    }

    stages {
        stage('🧪 Run Tests') {
            steps {
                script {
                    try {
                        sh '''
                            mkdir -p target/{cucumber-reports,allure-results,videos,screenshots}
                            
                            # Set logging levels to reduce verbose output
                            export MAVEN_OPTS="-Dorg.slf4j.simpleLogger.defaultLogLevel=warn -Dorg.slf4j.simpleLogger.log.org.apache=warn"
                            
                            # Run tests with minimal console output and suppress Selenium warnings
                            mvn -B -Dorg.slf4j.simpleLogger.defaultLogLevel=warn \
                                -Djava.util.logging.config.file=logging.properties \
                                -Dselenium.webdriver.logging.level=SEVERE \
                                -Dallure.serve.skip=true \
                                -Dallure.report.open=false \
                                clean test
                        '''
                    } catch (Exception e) {
                        echo """
                            ⚠️ Test Error:
                            🔴 Error Message: ${e.message}
                        """
                        unstable "❌ Test execution failed: ${e.message}"
                    }
                }
            }
        }

        stage('📊 Reports') {
            steps {
                script {
                    try {
                        // Generate Cucumber Report
                        cucumber(
                            buildStatus: 'UNSTABLE',
                            fileIncludePattern: '**/cucumber.json',
                            jsonReportDirectory: 'target/cucumber-reports',
                            reportTitle: 'Intrasense Web UI Test Report'
                        )
                        
                        // Generate Allure Report (without browser)
                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [
                                [key: 'allure.serve.skip', value: 'true'],
                                [key: 'allure.report.open', value: 'false']
                            ],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: 'target/allure-results']],
                            report: false,
                            serve: false
                        ])
                        
                        // Archive test artifacts
                        archiveArtifacts(
                            artifacts: '''
                                target/cucumber-reports/**/*,
                                target/allure-results/**/*,
                                target/videos/**/*,
                                target/screenshots/**/*
                            ''',
                            allowEmptyArchive: true
                        )
                    } catch (Exception e) {
                        echo "⚠️ Report generation failed: ${e.message}"
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "✅ Tests completed successfully"
        }
        unstable {
            echo "⚠️ Tests completed with issues"
        }
        failure {
            echo "❌ Tests failed"
        }
    }
} 