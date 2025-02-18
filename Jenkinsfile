pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'JDK17'
    }

    environment {
        GITHUB_REPO = 'https://github.com/hakantetik44/IntraSenseMyrian_E2E.git'
    }

    options {
        // Build'i 1 saat sonra otomatik olarak sonlandır
        timeout(time: 1, unit: 'HOURS')
        // Aynı anda sadece bir build çalıştır
        disableConcurrentBuilds()
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    // Workspace'i temizle
                    cleanWs()
                    // Repository'yi çek
                    checkout scm
                }
            }
        }

        stage('Install Dependencies') {
            steps {
                script {
                    try {
                        sh 'mvn clean install -DskipTests'
                    } catch (Exception e) {
                        error "Dependencies yüklenemedi: ${e.message}"
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    try {
                        sh """
                            # Test klasörlerini oluştur
                            mkdir -p target/cucumber-reports
                            mkdir -p target/allure-results
                            mkdir -p target/videos
                            mkdir -p target/screenshots

                            # FFmpeg'in yüklü olduğunu kontrol et
                            if ! command -v ffmpeg &> /dev/null; then
                                echo "FFmpeg bulunamadı. Yükleniyor..."
                                if [[ "$OSTYPE" == "darwin"* ]]; then
                                    brew install ffmpeg
                                elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
                                    sudo apt-get update && sudo apt-get install -y ffmpeg
                                fi
                            fi

                            # Testleri çalıştır
                            mvn clean test
                        """
                        currentBuild.result = 'SUCCESS'
                    } catch (Exception e) {
                        currentBuild.result = 'UNSTABLE'
                        echo "Test execution failed: ${e.message}"
                    }
                }
            }
        }

        stage('Generate Reports') {
            steps {
                script {
                    try {
                        sh """
                            # Report klasörlerini oluştur
                            mkdir -p test-reports
                            
                            # Raporları kopyala
                            cp -r target/cucumber-reports/* test-reports/ || true
                            cp -r target/surefire-reports test-reports/ || true
                            cp -r target/allure-results test-reports/ || true
                            
                            # Raporları arşivle
                            zip -r test-reports.zip test-reports/
                        """
                        
                        // Allure raporu oluştur
                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: 'target/allure-results']]
                        ])
                    } catch (Exception e) {
                        echo "Report generation failed: ${e.message}"
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
                // Allure raporunu aç
                try {
                    sh 'mvn allure:serve -Dallure.serve.port=8080'
                } catch (Exception e) {
                    echo "Failed to serve Allure report: ${e.message}"
                }
                
                // Workspace'i temizle
                cleanWs()
            }
        }
        success {
            echo "✅ Pipeline başarıyla tamamlandı"
        }
        failure {
            echo "❌ Pipeline başarısız oldu"
        }
        unstable {
            echo "⚠️ Pipeline kararsız durumda"
        }
    }
} 