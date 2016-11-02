node {
    stage('SCM') {
        git 'https://github.com/ReggieYang/VulnerabilitiesPrediction.git'
    }

    stage('build') {
        def mvnHome = tool 'Maven3'
        sh "${mvnHome}/bin/mvn -B clean package"
    }


}
