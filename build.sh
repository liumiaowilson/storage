#!/bin/bash
if [[ ! -d ./apache-tomcat ]];then
    echo "apache-tomcat is not found"
    exit 1
fi

mvn clean package

rm -rf ./apache-tomcat/webapps/ROOT
cp ./target/storage-1.0.war ./apache-tomcat/webapps/ROOT.war
