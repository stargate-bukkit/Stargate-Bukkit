pipeline {
    agent any
    tools {
        jdk 'JDK17'
    }
    stages {
        stage('Build') {
            steps {
                echo 'Building...'
                sh 'mvn clean & mvn validate & mvn compile'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing...'
                sh 'mvn test'
            }
        }
        stage('Deploy') {
             steps {
                echo 'Deploying...'
                sh 'mvn verify -Dmaven.test.skip=true'
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
    }
}