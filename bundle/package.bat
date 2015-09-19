@echo off
mvn clean  package -Pdev,copy-dependencies,admin-jspc,openfire,copy-plugins