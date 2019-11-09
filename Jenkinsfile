pipeline {
    agent { 
        docker { 
            image 'maven:latest'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        } 
    }
    stages {
        stage('build') {
            steps {
                sh 'mvn clean install'
            }
        }
    }
    post { 
        always { 
            cleanWs()
        }
    }
}
