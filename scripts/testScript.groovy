pipeline {
    agent{node('master')}
    stages {
        stage('uno') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop dcImgBIA"
                            sh "echo '${password}' | sudo -S docker container rm dcImgBIA"
                        } catch (Exception e) {
                            print '1st time? lets scip'
                        }
                    }
                }
                script {
                    echo 'Update from repository'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: '	BykovIAGitHub', url: 'https://github.com/BykovIA/devOpsTestRep.git']]])
                }
            }
        }
        stage ('dos'){
            steps{
                script{
                     withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t bykovImgNginx"
                        sh "echo '${password}' | sudo -S docker run -d -p 5757:80 --name dcImgBIA -v /home/adminci/is_mount_dir:/stat bykovImgNginx"
                    }
                }
            }
        }
        stage ('tres'){
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        
                        sh "echo '${password}' | sudo -S docker exec -t dcImgBIA bash -c 'df -h > /stat/stats.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t dcImgBIA bash -c 'top -n 1 -b >> /stat/stats.txt'"
                    }
                }
            }
        }        
    }    
}
