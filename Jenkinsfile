pipeline {
    agent { 
        docker { 
            image 'maven:latest'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        } 
    }
    stages {
        /*stage('Unit Tests & Sonar Scan') {
            steps {
                sh 'mvn test'
                withSonarQubeEnv('evn-sonar') {
                  sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar'
                }
            }
            post {
                success {
                    echo "Sonar Scan Complete, Evaluating Quality Gate"
                    script	{
                        timeout(time: 15, unit: 'MINUTES') {
                            def qg = waitForQualityGate()
                            print("Quality Gate: " + qg.status)
                            if (!(qg.status == 'OK' || qg.status == 'WARN')) {
                                error "Pipeline aborted due to quality gate failure: ${qg.status}"
                            }
                        }
                    }
                }
                failure {
                    echo "sonarqube-scan stage failed"
                }
            }
        }*/
        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }
    }
    post { 
        always { 
            cleanWs()
        }
    }
}
