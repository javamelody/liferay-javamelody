liferay-javamelody
=========================

Plugin for JavaMelody to monitor performance in a [Liferay](http://www.liferay.com/) server 6.1 or later.

See http://javamelody.googlecode.com

Continuous integration: https://javamelody.ci.cloudbees.com/job/liferay-javamelody/

License LGPL, http://www.gnu.org/licenses/lgpl-3.0.txt

Please submit github pull requests and github issues.


Downloading and Installing the plugin:
---------------------------------------
 - download the latest liferay-javamelody-hook war file from [releases](https://github.com/evernat/liferay-javamelody/releases)
 - copy the file into the "deploy" directory of your Liferay server and wait a few seconds
 - Liferay automatically deploys the file and removes it from "deploy" 
 - open http://localhost:8080/monitoring in a browser
 - Authentication and portal "Administrator" role is required to access this page.


Compiling and Installing the plugin:
---------------------------------------
 - Install maven
 - Clone the repository
 - Compile and test the code, then generate the jar:
	-> run "mvn clean install" command in your terminal
 - copy the war file (in the new generated target folder) into the "deploy" directory of your Liferay server and wait a few seconds
 - Liferay automatically deploys the file and removes it from "deploy" 
 - open http://localhost:8080/monitoring in a browser
