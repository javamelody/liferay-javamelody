liferay-javamelody
=========================

Plugin for JavaMelody to monitor performance in a [Liferay](http://www.liferay.com/) server 6.1 or later.

See https://github.com/javamelody/javamelody/wiki

Continuous integration: https://javamelody.ci.cloudbees.com/job/liferay-javamelody/

License ASL, http://www.apache.org/licenses/LICENSE-2.0

Please submit github pull requests and github issues.


Downloading and Installing the plugin:
---------------------------------------
 - See [this page](https://github.com/javamelody/javamelody/wiki/LiferayPlugin)


Compiling and Installing the plugin:
---------------------------------------
 - Install maven
 - Clone the repository
 - Compile and test the code, then generate the war:
	-> run "mvn clean install" command in your terminal
 - copy the war file (from the new generated target folder) into the "deploy" directory of your Liferay server and wait a few seconds
 - Liferay automatically deploys the file and removes it from "deploy" 
 - open http://localhost:8080/monitoring in a browser
