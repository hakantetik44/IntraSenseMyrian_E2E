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
        stage('Cleanup Workspace') {
            steps {
                cleanWs()
                checkout scm
            }
        }
        
        stage('Install Dependencies') {
            steps {
                script {
                    try {
                        sh '''
                            mvn clean install -DskipTests \
                                -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                                -B -V
                        '''
                    } catch (Exception e) {
                        echo "Dependency installation error: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                        throw e
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    try {
                        sh '''
                            mkdir -p target/{cucumber-reports,allure-results,videos,screenshots}
                            
                            mvn test \
                                -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
                                -Dcucumber.plugin="pretty,html:target/cucumber-reports/cucumber.html,json:target/cucumber-reports/cucumber.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" \
                                -Dallure.results.directory=target/allure-results \
                                -Dvideo.folder=target/videos \
                                -Dscreenshot.folder=target/screenshots \
                                -B -V
                                
                            echo "📹 Checking video recordings..."
                            ls -la target/videos/ || true
                        '''
                    } catch (Exception e) {
                        echo "Test execution error: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                        throw e
                    }
                }
            }
        }

        stage('Generate Reports') {
            steps {
                script {
                    try {
                        // Copy Cucumber reports to Allure directory
                        sh '''
                            mkdir -p target/allure-results/cucumber
                            cp target/cucumber-reports/* target/allure-results/cucumber/ || true
                        '''
                        
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
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: 'target/allure-results']]
                        ])
                        
                    } catch (Exception e) {
                        echo "Report generation error: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                        throw e
                    }
                }
            }
        }
    }
    
    post {
        always {
            script {
                // Collect test results
                def testResults = []
                
                // Read JUnit test results
                def junitResults = junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                testResults << [
                    name: 'JUnit',
                    total: junitResults.totalCount,
                    failed: junitResults.failCount,
                    skipped: junitResults.skipCount,
                    passed: junitResults.passCount
                ]
                
                // Archive Allure and Cucumber reports
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
                
                // Generate test summary
                def summary = """
╔═══════════════════════════╗
║   Test Result Summary     ║
╚═══════════════════════════╝

📊 Reports:
"""
                testResults.each { result ->
                    summary += """
🔍 ${result.name} Results:
   ✅ Passed: ${result.passed}
   ❌ Failed: ${result.failed}
   ⏭️ Skipped: ${result.skipped}
   📝 Total: ${result.total}
"""
                }
                
                // Print results
                echo summary
                
                // Print report locations
                echo """
📊 Test Reports Generated:
🥒 Cucumber Report: ${BUILD_URL}cucumber-html-reports/overview-features.html
📈 Allure Report: ${BUILD_URL}allure
"""
                
                // Clean workspace
                cleanWs()
            }
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