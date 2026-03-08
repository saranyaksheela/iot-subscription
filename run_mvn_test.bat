@echo off
cd /d %~dp0
mvn -DskipTests=false test
