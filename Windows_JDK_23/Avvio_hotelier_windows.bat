@echo off


start cmd /k "java -jar HOTELIERServer.jar"

set NUM_CLIENT=5

for /L %%i in (1,1,%NUM_CLIENT%) do (
    start cmd /k "java -jar HOTELIERClient.jar"
)
