pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'JDK17'
    }

    environment {
        GITHUB_REPO = 'https://github.com/hakantetik44/IntraSenseMyrian_E2E.git'
        JAVA_HOME = tool 'JDK17'
        PATH = "${env.JAVA_HOME}/bin:${tool 'maven'}/bin:${env.PATH}"
        MAVEN_OPTS = "-Duser.home=${env.WORKSPACE}"
    }

    options {
        // Build'i 1 saat sonra otomatik olarak sonlandır
        timeout(time: 1, unit: 'HOURS')
        // Aynı anda sadece bir build çalıştır
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                git branch: 'main',
                    url: env.GITHUB_REPO,
                    credentialsId: 'github-credentials'
            }
        }

        stage('Setup Environment') {
            steps {
                script {
                    try {
                        sh '''
                            echo "JAVA_HOME: $JAVA_HOME"
                            echo "PATH: $PATH"
                            java -version
                            mvn -version
                            git --version
                        '''
                    } catch (Exception e) {
                        error "Environment setup failed: ${e.message}"
                    }
                }
            }
        }

        stage('Install Dependencies') {
            steps {
                withMaven(maven: 'maven', jdk: 'JDK17') {
                    script {
                        try {
                            sh 'mvn clean install -DskipTests'
                        } catch (Exception e) {
                            error "Dependencies installation failed: ${e.message}"
                        }
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                withMaven(maven: 'maven', jdk: 'JDK17') {
                    script {
                        try {
                            sh '''
                                mkdir -p target/{cucumber-reports,allure-results,videos,screenshots}
                                
                                if ! command -v ffmpeg &> /dev/null; then
                                    echo "Installing FFmpeg..."
                                    if [[ "$OSTYPE" == "darwin"* ]]; then
                                        brew install ffmpeg
                                    else
                                        sudo apt-get update && sudo apt-get install -y ffmpeg
                                    fi
                                fi
                                
                                mvn clean test
                            '''
                        } catch (Exception e) {
                            unstable "Test execution failed: ${e.message}"
                        }
                    }
                }
            }
        }

        stage('Generate Reports') {
            steps {
                withMaven(maven: 'maven', jdk: 'JDK17', mavenOpts: '-Duser.home=${WORKSPACE}') {
                    script {
                        try {
                            sh '''
                                mkdir -p test-reports
                                cp -r target/cucumber-reports/* test-reports/ || true
                                cp -r target/surefire-reports test-reports/ || true
                                cp -r target/allure-results test-reports/ || true
                                zip -r test-reports.zip test-reports/
                            '''
                            
                            // Allure CLI kullanarak rapor oluştur
                            sh '''
                                ALLURE_VERSION="2.25.0"
                                ALLURE_PATH="${WORKSPACE}/allure-${ALLURE_VERSION}"
                                
                                # Allure CLI'yi indir ve kur
                                if [ ! -d "${ALLURE_PATH}" ]; then
                                    curl -o allure.tgz -Ls https://repo.maven.apache.org/maven2/io/qameta/allure/allure-commandline/${ALLURE_VERSION}/allure-commandline-${ALLURE_VERSION}.tgz
                                    tar -xzf allure.tgz -C "${WORKSPACE}"
                                    rm allure.tgz
                                fi
                                
                                # Allure raporu oluştur
                                ${ALLURE_PATH}/bin/allure generate target/allure-results --clean -o allure-report
                            '''
                            
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'allure-report',
                                reportFiles: 'index.html',
                                reportName: 'Allure Report',
                                reportTitles: ''
                            ])
                        } catch (Exception e) {
                            echo "Report generation failed: ${e.message}"
                        }
                    }
                }
            }
        }

        stage('Generate Xray Results') {
            steps {
                withCredentials([string(credentialsId: 'xray-api-key', variable: 'XRAY_API_KEY')]) {
                    script {
                        sh """
                            echo "Checking test results..."
                            if [ ! -f "target/cucumber-reports/cucumber.json" ]; then
                                echo "Error: cucumber.json not found!"
                                exit 1
                            fi
                            
                            echo "Uploading results to Xray Test Execution: SMF-2"
                            curl -v -H "Content-Type: application/json" \
                                 -H "Authorization: Bearer ${XRAY_API_KEY}" \
                                 -X POST \
                                 --data @target/cucumber-reports/cucumber.json \
                                 "https://xray.cloud.getxray.app/api/v2/import/execution/cucumber/SMF-2" 2>&1 | tee xray-response.log
                            
                            if grep -q "error" xray-response.log; then
                                echo "Error uploading to Xray:"
                                cat xray-response.log
                                exit 1
                            else
                                echo "Successfully uploaded test results to Xray"
                            fi
                        """
                        
                        archiveArtifacts artifacts: 'xray-response.log', allowEmptyArchive: true
                    }
                }
            }
        }

        stage('Archive Results') {
            steps {
                script {
                    try {
                        // Test sonuçlarını arşivle
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
                        
                        // Cucumber raporu oluştur
                        cucumber(
                            buildStatus: 'UNSTABLE',
                            fileIncludePattern: '**/cucumber.json',
                            jsonReportDirectory: 'target/cucumber-reports',
                            reportTitle: 'Intrasense Web UI Test Report'
                        )
                    } catch (Exception e) {
                        echo "Archiving results failed: ${e.message}"
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                try {
                    // Allure raporu zaten oluşturuldu, sadece temizlik yap
                    cleanWs()
                } catch (Exception e) {
                    echo "Workspace cleanup failed: ${e.message}"
                }
            }
        }
        success {
            echo "✅ Pipeline completed successfully"
        }
        failure {
            echo "❌ Pipeline failed"
        }
        unstable {
            echo "⚠️ Pipeline is unstable"
        }
    }
} 