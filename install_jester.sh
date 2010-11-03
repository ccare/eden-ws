#!/bin/sh
cd /var/tmp/
wget http://grester.svn.sourceforge.net/viewvc/grester/tags/1.0.1/jester-1.37.jar?revision=62 -O jester-1.37.jar
mvn install:install-file -Dfile=jester-1.37.jar -DgroupId=jester -DartifactId=jester -Dversion=1.37 -Dpackaging=jar
