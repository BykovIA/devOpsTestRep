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
                            sh "echo '${password}' | sudo -S docker stop dc_img_bia"
                            sh "echo '${password}' | sudo -S docker container rm dc_img_bia"
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
                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t bykov_img_nginx"
                        currentBuild.result = 'FAILURE'
                        sh "echo '${password}' | sudo -S docker run -d -p 5757:80 --name dc_img_bia -v /home/adminci/is_mount_dir:/stat bykov_img_nginx"
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
                        
                        sh "echo '${password}' | sudo -S docker exec -t dc_img_bia bash -c 'df -h > /stat/stats.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t dc_img_bia bash -c 'top -n 1 -b >> /stat/stats.txt'"
                    }
                }
            }
        }        
    }    
}
