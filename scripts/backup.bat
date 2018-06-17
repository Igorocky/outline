@echo off

set DIR_TO_STORE_BACKUP_TO="DIR_TO_STORE_BACKUP_TO"
set DB_URL="jdbc:h2:tcp://host:port/dbname"
set DB_USER="DB_USER"
set DB_PASSWORD="DB_PASSWORD"
set DIR_WITH_IMAGES="D:\Books\math\DIR_WITH_IMAGES"
set PATH_TO_H2_JAR="PATH_TO_H2_JAR"
set PATH_TO_7Z_EXE="PATH_TO_7Z_EXE"


For /f "tokens=1-3 delims=/. " %%a in ('date /t') do (set mydate=%%c_%%b_%%a)
For /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a_%%b)

set CURR_TIME=%mydate%__%mytime%
set CURR_BACKUP_DIR=%DIR_TO_STORE_BACKUP_TO%\%CURR_TIME%

mkdir %CURR_BACKUP_DIR%

java -cp %PATH_TO_H2_JAR% org.h2.tools.Script -url %DB_URL% -user %DB_USER% -password %DB_PASSWORD% -script %CURR_BACKUP_DIR%/%DB_USER%-db.zip -options compression zip

%PATH_TO_7Z_EXE% a -r %CURR_BACKUP_DIR%/%DB_USER%-images.zip %DIR_WITH_IMAGES%/*

pause