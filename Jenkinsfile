pipeline {
    agent any

    environment {
        JAVA_HOME = tool 'JDK17'
        PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
        ALLURE_HOME = tool 'Allure'
        MAVEN_OPTS = '-Dallure.serve.skip=true -Dallure.report.open=false'
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
                            
                            # Run tests with video recording and Allure enabled
                            mvn -B -Dallure.results.directory=target/allure-results \
                                -Dvideo.folder=target/videos \
                                -Dscreenshot.folder=target/screenshots \
                                -Dallure.serve.skip=true \
                                -Dallure.report.open=false \
                                clean test
                                
                            # Check if videos were created
                            echo "📹 Checking video recordings..."
                            ls -la target/videos/ || true
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
                        
                        // Generate Allure Report
                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [
                                [key: 'allure.results.directory', value: 'target/allure-results']
                            ],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: 'target/allure-results']],
                            report: true
                        ])
                        
                        // Archive test artifacts
                        archiveArtifacts(
                            artifacts: '''
                                target/cucumber-reports/**/*,
                                target/allure-results/**/*,
                                target/videos/**/*,
                                target/screenshots/**/*,
                                target/allure-report/**/*
                            ''',
                            allowEmptyArchive: true
                        )
                        
                        // Print report locations
                        echo """
                            📊 Test Reports Generated:
                            🥒 Cucumber Report: ${BUILD_URL}cucumber-html-reports/overview-features.html
                            📈 Allure Report: ${BUILD_URL}allure
                        """
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