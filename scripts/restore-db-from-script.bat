@echo off

set PATH_TO_H2_JAR="PATH_TO_H2_JAR"
set DB_URL="jdbc:h2:tcp://host:port/dbname"
set DB_USER="DB_USER"
set DB_PASSWORD="DB_PASSWORD"
set PATH_TO_ZIPPED_SCRIPT="PATH_TO_ZIPPED_SCRIPT"

java -cp %PATH_TO_H2_JAR% org.h2.tools.RunScript -url %DB_URL% -user %DB_USER% -password %DB_PASSWORD% -script %PATH_TO_ZIPPED_SCRIPT% -options compression zip