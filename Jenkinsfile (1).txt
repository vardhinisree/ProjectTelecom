pipeline {
    agent any

	tools{
		maven 'maven'
	}

	
    stages {
            stage('Compile and Clean') { 
                steps {
                       bat 'mvn compile'
                      }
            }
       
	        stage('Junit5 Test') { 
                 steps {
	                bat 'mvn test'
                  }
            }

	    stage('Jacoco Coverage Report') {
        	     steps{
            		//jacoco()
            		echo 'TestCoverage'
		          }
	        }
		stage('SonarQube'){
			steps{
				//sh label: '', script: '''mvn sonar:sonar \
				//-Dsonar.host.url=http://localhost:9000 \
			//	-Dsonar.login=squ_8530695a8134c03865b10fb062f50f2493c0986a'''
			echo 'Sonar Code Scanning '
				}	
   			}
        stage('Maven Build') { 
            steps {
                bat 'mvn clean install'
                  }
            }
stage('docker test') { 
	
	    steps {
        bat 'docker ps'
    }
}
        stage('Build Docker image'){
           steps {
		
                      //   	docker build -t nodejs-server -f Dockerfile.arg --build-arg UBUNTU_VERSION=18.04
		             //--build-arg CUDA_VERSION=10.0
                     //bat 'docker build -t  docker.repository.esi.adp.com/clientcentral/training:docker_jenkins_springboot:${BUILD_NUMBER} .'
           	    bat 'docker build -t  sampleproject .'
		         }
             }
	     stage('Docker Login'){
            steps {
		    
              echo "docker login from console"
               // docker login hub.docker.com -u arunajava567@gmail.com -p $aruna708*
		//    script {
               //     withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                //        sh 'docker login -u "$arunajava567@gmail.com" -p "$aruna708*"'
               //     }
		    
           // }                
        }
	     }
        stage('Docker Tag'){
            steps {
		    
          	      bat 'docker tag sampleproject  aruna708/sampleproject:latest'
            }                
        }
        stage('Docker Push'){
	
           steps {
	//	 withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials1', usernameVariable: 'aruna708', passwordVariable: 'Aruna708*')]) {
        //            sh '''
        //                echo "$Aruna708*" | docker login -u "$aruna708" --password-stdin 
         //               docker push aruna708/sampleproject:latest
         //          '''
          //      }
		   bat 'docker push aruna708/sampleproject'
               
            }
        }
        stage('Docker deploy'){
	
            steps {
                bat 'docker run -itd -p  8086:8086 sampleproject'
             }
        }
    
     
    }
}
