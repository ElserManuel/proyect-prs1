pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.4'  // Asegúrate de que este nombre coincida con la configuración en Jenkins
        jdk 'JDK-17'         // Asegúrate de que este nombre coincida con la configuración en Jenkins
    }
    
    environment {
        R2DBC_URL_PERSON = credentials('R2DBC_URL_PERSON')
        R2DBC_USERNAME_PERSON = credentials('R2DBC_USERNAME_PERSON')
        R2DBC_PASSWORD_PERSON = credentials('R2DBC_PASSWORD_PERSON')
        KAFKA_BOOTSTRAP_SERVERS = credentials('KAFKA_BOOTSTRAP_SERVERS')
        KAFKA_USERNAME = credentials('KAFKA_USERNAME')
        KAFKA_PASSWORD = credentials('KAFKA_PASSWORD')
        FAMILY_SERVICE_URL = credentials('FAMILY_SERVICE_URL')
        API_TOKEN = credentials('API_TOKEN')
        JAVA_HOME = "${tool 'JDK-17'}"
        PATH = "${JAVA_HOME}/bin:${PATH}"
        
        // Variables de SonarQube
        SONAR_PROJECT_KEY = 'ElserManuel_proyect-prs1'
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
                git branch: 'vg-ms-person',
                url: 'https://github.com/AlexanderRamosSanchez/vg-ms-project.git'
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
                        if (qg.status != 'OK') {
                            error "Pipeline abortado debido a fallo en Quality Gate: ${qg.status}"
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
            // Opcional: enviar notificación de éxito
        }
        failure {
            echo 'Pipeline fallido.'
            // Opcional: enviar notificación de fallo
        }
        unstable {
            echo 'Pipeline inestable - algunas pruebas fallaron.'
        }
    }
}
