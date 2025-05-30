pipeline {
    agent any
    environment {
        R2DBC_URL_PERSON = credentials('R2DBC_URL_PERSON')
        R2DBC_USERNAME_PERSON = credentials('R2DBC_USERNAME_PERSON')
        R2DBC_PASSWORD_PERSON = credentials('R2DBC_PASSWORD_PERSON')
        KAFKA_BOOTSTRAP_SERVERS = credentials('KAFKA_BOOTSTRAP_SERVERS')
        KAFKA_USERNAME = credentials('KAFKA_USERNAME')
        KAFKA_PASSWORD = credentials('KAFKA_PASSWORD')
        FAMILY_SERVICE_URL = credentials('FAMILY_SERVICE_URL')
        API_TOKEN = credentials('API_TOKEN')
    }
    stages {
        stage('Clonar Repositorio') {
            steps {
                git branch: 'vg-ms-person',
                url: 'https://github.com/AlexanderRamosSanchez/vg-ms-project.git'
            }
        }
        stage('Compilar con Maven') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Ejecutar Pruebas Unitarias') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Publicar Resultados de Pruebas') {
            steps {
                junit 'target/surefire-reports/*.xml'
            }
        }
        stage('Generar Artefacto .jar') {
            steps {
                sh 'mvn package'
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
                            sh "mvn sonar:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.login=${SONAR_TOKEN} -X"
                        }
                    }
                }
            }
        }
    }
    post {
        success {
            echo 'Pipeline completado con éxito.'
        }
        failure {
            echo 'Pipeline fallido.'
        }
    }
}