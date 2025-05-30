pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.4'  // Asegúrate de que este nombre coincida con la configuración en Jenkins
        jdk 'JDK-17'         // Asegúrate de que este nombre coincida con la configuración en Jenkins
    }
    
    environment {
        R2DBC_URL_INFORMATION = credentials('R2DBC_URL_INFORMATION')
        R2DBC_USERNAME_INFORMATION = credentials('R2DBC_USERNAME_INFORMATION')
        R2DBC_PASSWORD_INFORMATION = credentials('R2DBC_PASSWORD_INFORMATION')
        JAVA_HOME = "${tool 'JDK-17'}"
        PATH = "${JAVA_HOME}/bin:${PATH}"
        
        // Variables de SonarQube para el microservicio Information
        SONAR_PROJECT_KEY = 'ElserManuel_proyect-prs1-information'
        SONAR_ORGANIZATION = 'elsermanuel'
    }
    
    stages {
        stage('Verificar Herramientas') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
            }
        }
        
        stage('Clonar Repositorio') {
            steps {
                git branch: 'vg-ms-information',
                url: 'https://github.com/ElserManuel/proyect-prs1.git'
            }
        }
        
        stage('Compilar con Maven') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Ejecutar Pruebas Unitarias') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    // Publicar resultados de pruebas incluso si fallan
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }
        
        stage('Publicar Resultados de Pruebas') {
            steps {
                junit 'target/surefire-reports/*.xml'
            }
        }
        
        stage('Análisis con SonarQube') {
            steps {
                script {
                    withSonarQubeEnv('SonarCloud') {
                        withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                            sh """
                                mvn sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.organization=${SONAR_ORGANIZATION} \
                                -Dsonar.host.url=https://sonarcloud.io \
                                -Dsonar.login=${SONAR_TOKEN} \
                                -Dsonar.java.coveragePlugin=jacoco \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                -Dsonar.junit.reportPaths=target/surefire-reports \
                                -Dsonar.sources=src/main/java \
                                -Dsonar.tests=src/test/java
                            """
                            
                            echo "✅ Análisis de SonarQube completado exitosamente"
                            echo "📊 Revisa los resultados en: https://sonarcloud.io/dashboard?id=${SONAR_PROJECT_KEY}"
                        }
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    timeout(time: 5, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        echo "Quality Gate status: ${qg.status}"
                        
                        // Si no hay Quality Gate configurado (NONE), solo mostrar advertencia
                        if (qg.status == 'NONE') {
                            echo "ADVERTENCIA: No hay Quality Gate configurado en SonarCloud para este proyecto."
                            echo "El análisis de código se completó exitosamente. Configura un Quality Gate en SonarCloud para validaciones automáticas."
                        } else if (qg.status != 'OK') {
                            error "Pipeline abortado debido a fallo en Quality Gate: ${qg.status}"
                        } else {
                            echo "Quality Gate pasado exitosamente!"
                        }
                    }
                }
            }
        }
        
        stage('Generar Artefacto .jar') {
            steps {
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    // Archivar el artefacto generado
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
    }
    
    post {
        always {
            // Limpiar workspace después de la ejecución
            cleanWs()
        }
        success {
            echo 'Pipeline completado con éxito.'
            echo "🎉 Microservicio Information construido exitosamente"
            echo "📦 Artefacto disponible en Jenkins"
            echo "📊 Análisis de calidad disponible en SonarCloud"
        }
        failure {
            echo 'Pipeline fallido.'
            echo "❌ Error en el build del microservicio Information"
        }
        unstable {
            echo 'Pipeline inestable - algunas pruebas fallaron.'
            echo "⚠️ Microservicio Information construido con advertencias"
        }
    }
}
