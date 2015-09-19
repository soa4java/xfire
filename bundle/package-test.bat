@echo off
mvn clean  package -Ptest,copy-dependencies,admin-jspc,openfire,copy-plugins