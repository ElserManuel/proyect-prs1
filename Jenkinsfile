pipeline {
    agent any
    environment {
        R2DBC_URL = credentials('R2DBC_URL')
        R2DBC_USERNAME = credentials('R2DBC_USERNAME')
        R2DBC_PASSWORD = credentials('R2DBC_PASSWORD')
        KAFKA_BOOTSTRAP_SERVERS = credentials('KAFKA_BOOTSTRAP_SERVERS')
        KAFKA_USERNAME = credentials('KAFKA_USERNAME')
        KAFKA_PASSWORD = credentials('KAFKA_PASSWORD')
        HOUSING_SERVICE_URL = credentials('HOUSING_SERVICE_URL')
    }
    stages {
        stage('Clonar Repositorio') {
            steps {
                git branch: 'vg-ms-family',
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
