def myvar = "ACCOUNT_ID"

//This will not work, "myvar variable is not defined"
//def myprint() {
//	sh "echo ${myvar}"
//}

// This will work
//import groovy.transform.Field
//@Field def myvar = "my-account"

pipeline {
	agent any

	stages {
		stage("build") {
			steps {
				sh "echo ${myvar}"
			}
		}
	}
}