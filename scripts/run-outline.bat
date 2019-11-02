For /f "tokens=*" %%a in ('dir outline-*.jar /b') do (set filename=%%a)
java -jar %filename%