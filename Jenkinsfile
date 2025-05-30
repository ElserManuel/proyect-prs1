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
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Publicar Resultados de Pruebas') {
            steps {
                junit 'target/surefire-reports/*.xml'
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
        
        stage('Análisis con SonarCloud') {
            steps {
                script {
                    withSonarQubeEnv('SonarCloud') {
                        withCredentials([
                            string(credentialsId: 'SONAR_PROJECT_KEY', variable: 'SONAR_PROJECT_KEY'),
                            string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')
                        ]) {
                            sh """
                                mvn sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.login=${SONAR_TOKEN} \
                                -Dsonar.host.url=https://sonarcloud.io \
                                -Dsonar.organization=tu-organizacion-sonarcloud
                            """
                        }
                    }
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
